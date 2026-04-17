package com.dsc.sharededitor.runtime.socket;

public class ConsoleClientRunner {

    public static void main(String[] args) {
        ConsoleClient client = new ConsoleClient("localhost", 12345);
        client.start();
    }
}