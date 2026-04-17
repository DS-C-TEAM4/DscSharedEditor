package com.dsc.sharededitor.runtime.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConsoleClient {

    private final String host;
    private final int port;
    private String currentUsername;

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

            Thread receiveThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = serverReader.readLine()) != null) {
                        System.out.println();
                        System.out.println("[Server -> Client] " + response);
                        System.out.print("선택: ");
                    }
                } catch (IOException e) {
                    System.out.println();
                    System.out.println("[Client] 서버 수신 종료");
                    System.out.print("선택: ");
                }
            });

            receiveThread.setDaemon(true);
            receiveThread.start();

            while (true) {
                printMenu();

                String command = consoleReader.readLine();

                if ("1".equals(command)) {
                    login(writer, consoleReader);

                } else if ("2".equals(command)) {
                    logout(writer);

                } else if ("3".equals(command)) {
                    sendRawJson(writer, consoleReader);

                } else if ("0".equals(command)) {
                    if (currentUsername != null) {
                        String logoutRequest =
                                "{\"type\":\"LOGOUT_REQUEST\",\"username\":\""
                                        + currentUsername
                                        + "\"}";
                        writer.println(logoutRequest);
                    }

                    System.out.println("[Client] 종료합니다.");
                    break;

                } else {
                    System.out.println("[Client] 올바른 번호를 입력하세요.");
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException("클라이언트 실행 실패", e);
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== 메뉴 ===");
        System.out.println("1. 로그인");
        System.out.println("2. 로그아웃");
        System.out.println("3. 직접 JSON 입력");
        System.out.println("0. 종료");
        System.out.print("선택: ");
    }

    private void login(PrintWriter writer,
                       BufferedReader consoleReader) throws IOException {

        System.out.print("아이디: ");
        String username = consoleReader.readLine();

        System.out.print("비밀번호: ");
        String password = consoleReader.readLine();

        String request =
                "{\"type\":\"LOGIN_REQUEST\",\"username\":\""
                        + username
                        + "\",\"password\":\""
                        + password
                        + "\"}";

        writer.println(request);
        currentUsername = username;
    }

    private void logout(PrintWriter writer) {

        if (currentUsername == null) {
            System.out.println("[Client] 현재 로그인된 사용자가 없습니다.");
            return;
        }

        String request =
                "{\"type\":\"LOGOUT_REQUEST\",\"username\":\""
                        + currentUsername
                        + "\"}";

        writer.println(request);
        currentUsername = null;
    }

    private void sendRawJson(PrintWriter writer,
                             BufferedReader consoleReader) throws IOException {

        System.out.print("JSON 입력: ");
        String rawJson = consoleReader.readLine();

        writer.println(rawJson);
    }
}