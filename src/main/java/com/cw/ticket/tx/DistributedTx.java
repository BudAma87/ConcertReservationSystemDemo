package com.cw.ticket.tx;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import com.cw.ticket.zookeeper.ZooKeeperClient;

public abstract class DistributedTx implements Watcher {

    public static final String VOTE_COMMIT = "vote_commit";
    public static final String VOTE_ABORT = "vote_abort";
    public static final String GLOBAL_COMMIT = "global_commit";
    public static final String GLOBAL_ABORT = "global_abort";

    protected static String zooKeeperUrl;
    protected String currentTransaction;
    protected ZooKeeperClient client;
    protected DistributedTxListener listener;

    public static void setZooKeeperURL(String url) {
        zooKeeperUrl = url;
    }

    public DistributedTx(DistributedTxListener listener) {
        this.listener = listener;
    }

    public void start(String transactionId, String participantId) throws Exception {
        client = new ZooKeeperClient(zooKeeperUrl, 5000, this);
        onStartTransaction(transactionId, participantId);
    }

    protected abstract void onStartTransaction(String transactionId, String participantId);

    @Override
    public void process(WatchedEvent event) {
        // Will be overridden by participants
    }

    public ZooKeeperClient getClient() {
        return client;
    }

    public String getCurrentTransaction() {
        return currentTransaction;
    }
}
