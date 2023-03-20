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
        try {
            System.out.println(s);
            var bris = new BufferedReader(new InputStreamReader(s.getInputStream()));
            var bros = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            bros.append("Hello! welcome to server!\n");
            System.out.println("Client says: " + bris.readLine());
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                s.close();
            } catch (IOException e) {}
        }
    }
    public static void main(String[] args) throws IOException {
        try (var ss = new ServerSocket(3333)) {
            while (true) {
                handleClient(ss.accept());
            }
        }
    }
}
