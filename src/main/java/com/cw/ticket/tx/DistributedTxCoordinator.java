package com.cw.ticket.tx;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

public class DistributedTxCoordinator extends DistributedTx {

    public DistributedTxCoordinator(DistributedTxListener listener) {
        super(listener);
    }

    @Override
    protected void onStartTransaction(String transactionId, String participantId) {
        try {
            currentTransaction = "/" + transactionId;

            client.getZooKeeper().create(
                    currentTransaction,
                    "".getBytes(StandardCharsets.UTF_8),
                    org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean perform() throws KeeperException, InterruptedException {
        List<String> children = client.getChildren(currentTransaction);
        boolean result = true;

        for (String child : children) {
            String childPath = currentTransaction + "/" + child;
            byte[] data = client.getZooKeeper().getData(childPath, false, null);
            String vote = new String(data);

            if (!VOTE_COMMIT.equals(vote)) {
                result = false;
                break;
            }
        }

        if (result) {
            sendGlobalCommit();
        } else {
            sendGlobalAbort();
        }

        currentTransaction = null;
        return result;
    }

    public void sendGlobalCommit() throws KeeperException, InterruptedException {
        if (currentTransaction != null) {
            client.getZooKeeper().setData(
                    currentTransaction,
                    GLOBAL_COMMIT.getBytes(StandardCharsets.UTF_8),
                    -1
            );
            listener.onGlobalCommit();
        }
    }

    public void sendGlobalAbort() throws KeeperException, InterruptedException {
        if (currentTransaction != null) {
            client.getZooKeeper().setData(
                    currentTransaction,
                    GLOBAL_ABORT.getBytes(StandardCharsets.UTF_8),
                    -1
            );
            listener.onGlobalAbort();
        }
    }
}
