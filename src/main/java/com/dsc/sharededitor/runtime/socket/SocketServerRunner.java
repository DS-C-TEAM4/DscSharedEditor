package com.dsc.sharededitor.runtime.socket;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SocketServerRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        SocketServer socketServer = new SocketServer(12345);

        Thread serverThread = new Thread(socketServer::start);
        serverThread.setDaemon(false);
        serverThread.start();
    }
}