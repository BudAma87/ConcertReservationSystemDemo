package com.cw.ticket.tx;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.nio.charset.StandardCharsets;

public class DistributedTxParticipant extends DistributedTx {

    private static final String PARTICIPANT_PREFIX = "/txp_";
    private String transactionRoot;

    public DistributedTxParticipant(DistributedTxListener listener) {
        super(listener);
    }

    @Override
    protected void onStartTransaction(String transactionId, String participantId) {
        try {
            transactionRoot = "/" + transactionId;
            currentTransaction = transactionRoot + PARTICIPANT_PREFIX + participantId;

            // Create participant node under root
            client.getZooKeeper().create(currentTransaction, new byte[0],
                    org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);

            // Watch root node for final decision
            client.getZooKeeper().exists(transactionRoot, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void voteCommit() {
        try {
            if (currentTransaction != null) {
                client.getZooKeeper().setData(
                        currentTransaction,
                        VOTE_COMMIT.getBytes(StandardCharsets.UTF_8),
                        -1
                );
                System.out.println("✅ Participant voted COMMIT: " + currentTransaction);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void voteAbort() {
        try {
            if (currentTransaction != null) {
                client.getZooKeeper().setData(
                        currentTransaction,
                        VOTE_ABORT.getBytes(StandardCharsets.UTF_8),
                        -1
                );
                System.out.println("❌ Participant voted ABORT: " + currentTransaction);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            if (event.getType() == Watcher.Event.EventType.NodeDataChanged &&
                event.getPath().equals(transactionRoot)) {

                byte[] data = client.getZooKeeper().getData(transactionRoot, true, null);
                String decision = new String(data);

                if (GLOBAL_COMMIT.equals(decision)) {
                    listener.onGlobalCommit();
                } else if (GLOBAL_ABORT.equals(decision)) {
                    listener.onGlobalAbort();
                } else {
                    System.out.println("⚠️ Unknown decision received: " + decision);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
