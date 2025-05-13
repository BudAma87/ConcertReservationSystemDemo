package com.cw.ticket.server;

import java.util.UUID;

import com.cw.ticket.tx.DistributedTx;
import com.cw.ticket.tx.DistributedTxCoordinator;
import com.cw.ticket.tx.DistributedTxListener;
import com.cw.ticket.tx.DistributedTxParticipant;

import ds.ticket.TicketRequest;
import ds.ticket.TicketResponse;
import ds.ticket.TicketServiceGrpc;
import io.grpc.stub.StreamObserver;

public class TicketServiceImpl extends TicketServiceGrpc.TicketServiceImplBase implements DistributedTxListener {

    private DistributedTx transaction;
    private final boolean isLeader;
    private final int port;
    private TicketRequest pendingRequest;

    private boolean transactionStatus = false;

    public TicketServiceImpl() {
        this.port = 50051; // Or pass dynamically if needed
        this.isLeader = true; // Default (you can pass in via constructor in future)
        DistributedTx.setZooKeeperURL("localhost:2181");

        if (isLeader) {
            transaction = new DistributedTxCoordinator(this);
        } else {
            transaction = new DistributedTxParticipant(this);
        }
    }

    @Override
    public void reserveTickets(TicketRequest request, StreamObserver<TicketResponse> responseObserver) {
        try {
            pendingRequest = request;
            String transactionId = request.getEventId() + "-" + UUID.randomUUID();

            transaction.start(transactionId, "node-" + port);

            if (isLeader) {
                System.out.println("👑 Leader initiating 2PC transaction...");

                // Simulate business logic:
                if (request.getIncludeAfterParty()) {
                    ((DistributedTxCoordinator) transaction).perform();
                } else {
                    ((DistributedTxCoordinator) transaction).sendGlobalAbort();
                }
            } else {
                System.out.println("🔁 Follower voting...");

                if (request.getIncludeAfterParty()) {
                    ((DistributedTxParticipant) transaction).voteCommit();
                } else {
                    ((DistributedTxParticipant) transaction).voteAbort();
                }
            }

            transactionStatus = true;

        } catch (Exception e) {
            e.printStackTrace();
            transactionStatus = false;
        }

        TicketResponse response = TicketResponse.newBuilder()
                .setSuccess(transactionStatus)
                .setMessage(transactionStatus ? "Reservation processed." : "Reservation failed.")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void onGlobalCommit() {
        System.out.println("✅ COMMIT received: Booking confirmed.");
        updateReservation();
    }

    @Override
    public void onGlobalAbort() {
        System.out.println("❌ ABORT received: Booking canceled.");
        pendingRequest = null;
    }

    private void updateReservation() {
        if (pendingRequest != null) {
            System.out.println("🎫 Reserved " + pendingRequest.getSeatType() +
                    " for event " + pendingRequest.getEventId() +
                    (pendingRequest.getIncludeAfterParty() ? " + After Party" : ""));
            pendingRequest = null;
        }
    }
}

