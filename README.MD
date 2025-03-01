# GRPC DEMO

A grpc service demo with java spring boot.

## Protobuf Definition

When it comes to grpc service, you must be defined your service proto first. It's like an api contract.

```proto
syntax = "proto3";

option java_multiple_files = true;
option java_package = "org.springframework.grpc.sample.proto";
option java_outer_classname = "HelloWorldProto";

// The greeting service definition.
service Simple {
  // Sends a message and server will response message like rest
  rpc SayHello (HelloRequest) returns (HelloReply) {
  }

  // Send a greeting and server response multiple times
  // Server will countdown and send multiple message
  rpc ServerStreamHello(HelloRequest) returns (stream HelloReply) {}

  // Client send multiple message and server response one time
  // server will give response
  // that count total message that send by client
  rpc ClientStreamHello(stream HelloRequest) returns (HelloReply) {}

  // Both server and client can send message multiple times
  // At the end of streaming,
  // server will send message that count total message that send by client
  rpc BidirectionalStreamHello(stream HelloRequest) returns (stream HelloReply) {}
}

// The request message
message HelloRequest {
  string message = 1;
}

// The response message
message HelloReply {
  string message = 1;
}
```

## Build
Build service and generated class

```shell
./mvnw clean package
```

## Running
Run service as usual spring boot

```shell
./mvnw spring-boot:run
```

You can try it out using a gRPC client like `grpcurl`:

```shell
grpcurl -d '{"message":"Hi"}' -plaintext localhost:9090 Simple.SayHello
```
