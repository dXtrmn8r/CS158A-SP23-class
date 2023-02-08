package edu.sjsu.cs158a.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPSender {
    public static void main(String[] arg) throws IOException {
        var sock = new DatagramSocket();
        var bytes = "hello!".getBytes();
        var packet = new DatagramPacket(bytes, bytes.length);
        packet.setAddress(InetAddress.getByName("127.0.0.1"));
        packet.setPort(2323);
        sock.send(packet);
    }
}
