package com.cw.ticket.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

public class ZooKeeperClient {

    private final ZooKeeper zooKeeper;

    public ZooKeeperClient(String connectString, int sessionTimeout, Watcher watcher) throws IOException {
        this.zooKeeper = new ZooKeeper(connectString, sessionTimeout, watcher);
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public boolean checkExists(String path) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, false);
        return (stat != null);
    }

    public String createEphemeralSequential(String pathPrefix) throws KeeperException, InterruptedException {
        return zooKeeper.create(pathPrefix, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public List<String> getChildren(String path) throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(path, false);
    }
}
