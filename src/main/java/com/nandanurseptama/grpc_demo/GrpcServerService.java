package com.nandanurseptama.grpc_demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.grpc.sample.proto.HelloReply;
import org.springframework.grpc.sample.proto.HelloRequest;
import org.springframework.grpc.sample.proto.SimpleGrpc;
import org.springframework.stereotype.Service;

import io.grpc.stub.StreamObserver;

@Service
public class GrpcServerService extends SimpleGrpc.SimpleImplBase {

    private static Log log = LogFactory.getLog(GrpcServerService.class);

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        final String clientMessage = req.getMessage();

        log.info("Hello " + clientMessage);

        // validate if clientMessage is empty or not
        if (clientMessage.isEmpty()) {
            throw new IllegalArgumentException("Bad message: message cannot empty");
        }

        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello this is server. Your message \"" + clientMessage + "\" was delivered")
                .build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serverStreamHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        final String clientMessage = req.getMessage();

        int count = 0;
        while (count < 10) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello this is server. Your message \""
                    + clientMessage + "\" will be delivered in " + (10 - count) + " seconds").build();
            responseObserver.onNext(reply);
            count++;
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                responseObserver.onError(e);
                return;
            }
        }
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello this is server. Your message \""
                + clientMessage + "\" was delivered").build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<HelloRequest> clientStreamHello(StreamObserver<HelloReply> responseObserver) {
        return new StreamObserver<HelloRequest>() {
            private int totalMessage = 0;

            // read request from stream
            @Override
            public void onNext(HelloRequest value) {
                log.info("server receive message : " + value.getMessage());
                // count message
                totalMessage++;
            }

            // throw error
            @Override
            public void onError(Throwable t) {
                log.info("getting error when process request. reason : " + t.getMessage());
                responseObserver.onError(new Exception("failed to process message", t));
            }

            // handler when client end stream
            @Override
            public void onCompleted() {
                log.info("client end streaming with " + totalMessage + " messages");
                // send response when cleint end stream
                HelloReply reply = HelloReply.newBuilder()
                        .setMessage("You send " + totalMessage + " messages and it will be delivered").build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<HelloRequest> bidirectionalStreamHello(StreamObserver<HelloReply> responseObserver) {
        return new StreamObserver<HelloRequest>() {
            private int totalMessage = 0;

            // handling when receive message
            @Override
            public void onNext(HelloRequest value) {
                totalMessage++;
                log.info("server receive message : " + value.getMessage());
                HelloReply reply = HelloReply.newBuilder()
                        .setMessage("You send a message and it will be delivered").build();
                responseObserver.onNext(reply);
            }

            // handling on error
            @Override
            public void onError(Throwable t) {
                log.info("getting error when process request. reason : " + t.getMessage());
                responseObserver.onError(new Exception("failed to process message", t));
            }

            // handler when client end stream
            @Override
            public void onCompleted() {
                log.info("client end streaming with " + totalMessage + " messages");
                HelloReply reply = HelloReply.newBuilder()
                        .setMessage("You've send " + totalMessage + " messages").build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

        };
    }

}
