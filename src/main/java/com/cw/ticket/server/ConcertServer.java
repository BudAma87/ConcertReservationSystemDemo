package com.cw.ticket.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import com.cw.ticket.zookeeper.ZooKeeperClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;

import java.util.Collections;
import java.util.List;


public class ConcertServer {

	public static void main(String[] args) throws Exception {
	    int port = 50051; // default port
	    if (args.length > 0) {
	        port = Integer.parseInt(args[0]);
	    }

	    String zkConnect = "localhost:2181"; // default ZooKeeper port
	    String lockPath = "/leader-election";

	    // Connect to ZooKeeper
	    ZooKeeperClient zkClient = new ZooKeeperClient(zkConnect, 3000, event -> {
	        if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
	            System.out.println("✅ Connected to ZooKeeper");
	        }
	    });

	    // Create root path if it doesn't exist
	    if (!zkClient.checkExists(lockPath)) {
	        zkClient.getZooKeeper().create(lockPath, new byte[0],
	                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	    }

	    // Create an ephemeral sequential znode
	    String myNode = zkClient.createEphemeralSequential(lockPath + "/node_");
	    System.out.println("🆔 My znode: " + myNode);

	    // Determine the leader by sorting the children
	    List<String> children = zkClient.getChildren(lockPath);
	    Collections.sort(children);
	    String leader = lockPath + "/" + children.get(0);

	    boolean isLeader = myNode.equals(leader);
	    System.out.println(isLeader ? "👑 I am the leader." : "🔁 I am a follower.");

	    if (!isLeader) {
	        System.out.println("Standing by. Not starting gRPC server.");
	        return;
	    }

	    // Start gRPC server only if leader
	    Server server = ServerBuilder.forPort(port)
	            .addService(new TicketServiceImpl()) // no changes here yet
	            .build();

	    server.start();
	    System.out.println("🎤 Concert Ticket Server started on port " + port);
	    server.awaitTermination();
	}

}

