package com.cw.ticket.client;

import ds.ticket.TicketRequest;
import ds.ticket.TicketResponse;
import ds.ticket.TicketServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ReservationClient {
    public static void main(String[] args) {
        // Connect to the gRPC server on port 50051
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("127.0.0.1", 50051)
                .usePlaintext()
                .build();

        // Create a blocking stub to call the service
        TicketServiceGrpc.TicketServiceBlockingStub stub =
                TicketServiceGrpc.newBlockingStub(channel);

        // Build a test reservation request
        TicketRequest request = TicketRequest.newBuilder()
                .setEventId("E123")
                .setSeatType("VIP")
                .setIncludeAfterParty(true)
                .build();

        // Send the request and receive response
        TicketResponse response = stub.reserveTickets(request);
        System.out.println("Client received: " + response.getMessage());

        // Close the channel
        channel.shutdown();
    }
}
