package com.dsc.sharededitor.runtime.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnectionHandler implements Runnable {

    private final Socket clientSocket;

    public ClientConnectionHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            writer.println("[Server] 연결되었습니다.");

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[ClientConnectionHandler] 수신 메시지: " + line);
                writer.println("[Server] 아직 메시지 처리 로직은 연결되지 않았습니다.");
            }
        } catch (IOException e) {
            System.out.println("[ClientConnectionHandler] 연결 처리 중 예외 발생: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("[ClientConnectionHandler] 소켓 종료 중 예외 발생: " + e.getMessage());
            }
        }
    }
}