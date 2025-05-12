package com.cw.ticket.tx;

public interface DistributedTxListener {
    void onGlobalCommit();
    void onGlobalAbort();
}
