package com.dsc.sharededitor.runtime.socket;

import com.dsc.sharededitor.domain.auth.InMemoryUserStore;
import com.dsc.sharededitor.domain.broadcast.MessageBroadcaster;
import com.dsc.sharededitor.domain.connection.ConnectionGateway;
import com.dsc.sharededitor.domain.connection.ConnectionHandler;
import com.dsc.sharededitor.domain.message.MessageParser;
import com.dsc.sharededitor.domain.session.SessionManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SocketServerRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {

        InMemoryUserStore userStore = new InMemoryUserStore();
        SessionManager sessionManager = new SessionManager();
        ConnectionHandler connectionHandler =
                new ConnectionHandler(userStore, sessionManager);

        MessageParser messageParser = new MessageParser();
        MessageBroadcaster messageBroadcaster =
                new MessageBroadcaster(sessionManager);

        ClientOutputRegistry clientOutputRegistry =
                new ClientOutputRegistry();

        ConnectionGateway connectionGateway =
                new ConnectionGateway(
                        messageParser,
                        connectionHandler,
                        messageBroadcaster,
                        clientOutputRegistry
                );

        SocketServer socketServer =
                new SocketServer(12345, connectionGateway, clientOutputRegistry);

        Thread serverThread = new Thread(socketServer::start);
        serverThread.start();
    }
}