package edu.sjsu.cs158a;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        var s = new Socket("::1", 3333);
        System.out.println(s);
        var bris = new BufferedReader(new InputStreamReader(s.getInputStream()));
        var bros = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        System.out.println("from server: " + bris.readLine());
        bros.append("goodbye\n");
    }
}
