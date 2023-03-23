package edu.sjsu.cs158a;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static void handleClient(Socket s) {
        try (s) {
            System.out.println(s);
            var bris = new BufferedReader(new InputStreamReader(s.getInputStream()));
            var bros = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            System.out.println("sending welcome");
            bros.append("Hello! welcome to server!\n").flush();
            System.out.println("sent welcome");
            System.out.println("Client says: " + bris.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        try (var ss = new ServerSocket(3333)) {
            System.out.println("Listing on " + ss);
            while (true) {
                final Socket s = ss.accept();
                new Thread(() -> {
                    handleClient(s);
                }).start();
            }
        }
    }
}
