package com.dsc.sharededitor.runtime.socket;

import com.dsc.sharededitor.domain.connection.ConnectionGateway;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnectionHandler implements Runnable {

    private final Socket clientSocket;
    private final ConnectionGateway connectionGateway;

    public ClientConnectionHandler(Socket clientSocket,
                                   ConnectionGateway connectionGateway) {
        this.clientSocket = clientSocket;
        this.connectionGateway = connectionGateway;
    }

    @Override
    public void run() {
        String sessionId =
                clientSocket.getRemoteSocketAddress().toString();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()));

                PrintWriter writer =
                        new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            writer.println("[Server] 연결되었습니다.");

            String line;

            while ((line = reader.readLine()) != null) {

                Object result =
                        connectionGateway.handleMessage(sessionId, line);

                if (result != null) {
                    writer.println(result.toString());
                }
            }

        } catch (Exception e) {
            System.out.println("[ClientConnectionHandler] 예외 발생: "
                    + e.getMessage());
        }
    }
}