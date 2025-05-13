# Concert Reservation System

The Concert Reservation System is a distributed application that uses a two-phase commit (2PC) protocol for ticket reservations. It leverages ZooKeeper for distributed coordination and gRPC for communication between clients and servers.

## Features

- **Distributed Transactions**: Implements a two-phase commit protocol for ensuring consistency across distributed nodes.
- **Leader Election**: Uses ZooKeeper to elect a leader node for coordinating transactions.
- **gRPC Communication**: Provides a gRPC-based API for clients to reserve tickets.
- **ZooKeeper Integration**: Manages distributed state and coordination using ZooKeeper.

## Project Structure

```
.classpath
.gitignore
.project
pom.xml
.settings/
src/
  main/
    java/
      com/
        cw/
          ticket/
            client/          # gRPC client implementation
            server/          # gRPC server and leader election logic
            tx/              # Distributed transaction logic
            zookeeper/       # ZooKeeper client utilities
    proto/                   # Protocol Buffers definitions
    resources/               # Application resources
  test/
    java/                    # Unit tests
    resources/               # Test resources
target/                      # Compiled classes and build artifacts
```

## Prerequisites

- Java 8 or higher
- Apache ZooKeeper
- Maven
- gRPC

## Getting Started

### 1. Build the Project

Use Maven to build the project:

```sh
mvn clean install
```

### 2. Start ZooKeeper

Ensure ZooKeeper is running on `localhost:2181` or update the connection string in the code.

### 3. Run the Server

Start the server:

```sh
java -cp target/concert-reservation-system-0.0.1-SNAPSHOT.jar com.cw.ticket.server.ConcertServer
```

### 4. Run the Client

Run the client to make a reservation:

```sh
java -cp target/concert-reservation-system-0.0.1-SNAPSHOT.jar com.cw.ticket.client.ReservationClient
```

### 5. Test the Application

The client will send a reservation request, and the server will process it using the two-phase commit protocol.

## Key Components

### Distributed Transactions

- **Coordinator**: `DistributedTxCoordinator` manages the 2PC protocol.
- **Participant**: `DistributedTxParticipant` votes on transactions and listens for global decisions.

### ZooKeeper Integration

- **ZooKeeper Client**: `ZooKeeperClient` provides utility methods for interacting with ZooKeeper.

### gRPC API

- **Ticket Service**: `TicketServiceImpl` implements the gRPC service for ticket reservations.

## Example Workflow

1. A client sends a reservation request to the server.
2. The leader server initiates a two-phase commit transaction.
3. Participants vote to commit or abort based on business logic.
4. The leader sends a global commit or abort decision.
5. The transaction is finalized, and the client receives a response.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.

## Acknowledgments

- Apache ZooKeeper
- gRPC
- Java