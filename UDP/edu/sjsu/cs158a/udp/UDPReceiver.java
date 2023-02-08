package edu.sjsu.cs158a.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPReceiver {
    public static void main(String[] arg) throws IOException {
        var sock = new DatagramSocket(2323);
        var bytes = new byte[512];
        var packet = new DatagramPacket(bytes, bytes.length);
        while (true) {
            sock.receive(packet);
            System.out.println("Got: " + new String(bytes));
        }
    }
}
