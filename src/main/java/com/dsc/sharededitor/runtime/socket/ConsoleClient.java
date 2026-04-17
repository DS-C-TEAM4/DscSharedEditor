package com.dsc.sharededitor.runtime.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConsoleClient {

    private final String host;
    private final int port;

    public ConsoleClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try (
                Socket socket = new Socket(host, port);
                BufferedReader serverReader =
                        new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedReader consoleReader =
                        new BufferedReader(new InputStreamReader(System.in));
                PrintWriter writer =
                        new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println("[Client] 서버에 연결되었습니다.");
            System.out.println("[Client] JSON 메시지를 한 줄로 입력하세요.");

            Thread receiveThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = serverReader.readLine()) != null) {
                        System.out.println("[Server -> Client] " + response);
                    }
                } catch (IOException e) {
                    System.out.println("[Client] 서버 수신 종료: " + e.getMessage());
                }
            });

            receiveThread.start();

            String input;
            while ((input = consoleReader.readLine()) != null) {
                writer.println(input);
            }

        } catch (IOException e) {
            throw new IllegalStateException("클라이언트 실행에 실패했습니다.", e);
        }
    }
}