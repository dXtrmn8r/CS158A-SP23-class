package edu.sjsu.cs158a.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class UDPReceiver {
    public static void main(String[] arg) throws IOException {
        try (var sock = new DatagramSocket(2323)) {
            System.out.println(sock.getLocalSocketAddress());
            System.out.println(sock.getRemoteSocketAddress());
            sock.setSoTimeout(5000);
            var bytes = new byte[512];
            var bb = ByteBuffer.wrap(bytes);
            var packet = new DatagramPacket(bytes, bytes.length);
            while (true) {
                try {
                    sock.receive(packet);
                } catch (SocketTimeoutException e) {
                    System.out.println("i'm bored");
                    System.exit(0);
                }
                var len = packet.getLength();
                bb.position(0);
                while (bb.position() < len) {
                    System.out.println("bb pos " + bb.position());
                    var rand = bb.getInt();
                    System.out.println("bb pos " + bb.position());
//                System.out.println(packet.getSocketAddress() + " Got: " + new String(bytes, 0, len));
                    System.out.println(packet.getSocketAddress() + " Got: " + rand);
                }
            }
        }
    }
}
