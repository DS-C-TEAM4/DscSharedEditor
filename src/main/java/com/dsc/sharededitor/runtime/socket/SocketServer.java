package com.dsc.sharededitor.runtime.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.dsc.sharededitor.domain.connection.ConnectionGateway;

public class SocketServer {

    private final int port;
    private final ConnectionGateway connectionGateway;
    private final ClientOutputRegistry clientOutputRegistry;

    public SocketServer(int port,
                        ConnectionGateway connectionGateway,
                        ClientOutputRegistry clientOutputRegistry) {
        this.port = port;
        this.connectionGateway = connectionGateway;
        this.clientOutputRegistry = clientOutputRegistry;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[SocketServer] 서버 시작 - 포트: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SocketServer] 클라이언트 연결 수락: " + clientSocket.getRemoteSocketAddress());

                ClientConnectionHandler handler =
                        new ClientConnectionHandler(
                                clientSocket,
                                connectionGateway,
                                clientOutputRegistry
                        );
                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            throw new IllegalStateException("소켓 서버 실행에 실패했습니다.", e);
        }
    }
}