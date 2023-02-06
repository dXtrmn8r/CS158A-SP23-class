package edu.sjsu.cs158a;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HelloUDP {
    final static private short HELLO = 1;
    final static private short TRANSFER = 2;
    final static private short CHECKSUM = 3;
    final static private short ERROR = 5;
    final static private short CHECK_STANDINGS = 10;
    private static final int retryLimit = 10;

    static private void checkError(ByteBuffer bb) {
        if (bb.getShort() == ERROR) {
            // this runtime exception will bubble up and end the program
            throw new RuntimeException(new String(bb.array(), bb.position(), bb.remaining()));
        }
        bb.position(0);
    }

    // send a packet and wait for a reply
    private static void sendRecv(DatagramSocket dsock, DatagramPacket dpack, int length) throws IOException {
        int count = 0;
        // make a copy of the packet in case we have to resend it
        // TODO: it would probably be better to have a send packet and
        //       a receive packet.
        byte[] bytes = new byte[dpack.getLength()];
        System.arraycopy(dpack.getData(), 0, bytes, 0, bytes.length);
        DatagramPacket resendPacket = new DatagramPacket(bytes, bytes.length);

        // extract out key fields to make sure we get the correct reply
        ByteBuffer bb = ByteBuffer.wrap(resendPacket.getData());
        int conversation = -1;
        int offset = -1;
        short type = bb.getShort();
        if (type == TRANSFER || type == CHECKSUM) {
            // TRANSFER and CHECKSUM will have the conversation id
            conversation = bb.getInt();
        }
        if (type == TRANSFER) {
            // only TRANSFER has an offset
            offset = bb.getInt();
        }

        // make bb point to the the buffer that we are sending and
        // receiving from
        bb = ByteBuffer.wrap(dpack.getData());
        while (true) {
            if (count++ > retryLimit) {
                throw new IOException(String.format("More than %d retries", retryLimit));
            }
            try {
                // System.out.println("Sending " + (int)resendPacket.getData()[1]);
                dsock.send(resendPacket);
                while (true) {
                    dpack.setLength(length);
                    dsock.receive(dpack);
                    bb.rewind();
                    bb.limit(dpack.getLength());
                    checkError(bb);
                    short recvType = bb.getShort();
                    int recvConversation = -1;
                    int recvOffset = -1;
                    if (type == TRANSFER || type == CHECKSUM) {
                        recvConversation = bb.getInt();
                    }
                    if (type == TRANSFER) {
                        recvOffset = bb.getInt();
                    }
                    if (type != recvType || (conversation != recvConversation) || (offset != recvOffset)) {
                        System.out.printf("dup packet: %d ?= %d, %d ?= %d, %d ?= %d\n", type, recvType, conversation, recvConversation, offset, recvOffset);
                        continue;
                    }
                    // if we get here, we are done, so break out
                    break;
                }
                // System.out.println("Received " + (int)dpack.getData()[1]);
                return;
            } catch (SocketTimeoutException e) {
                System.out.println("timeout");
            }
        }
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length != 1) {
            System.out.println("USAGE: HelloUDP file_to_send");
            System.exit(2);
        }
        DatagramSocket dsock = new DatagramSocket();
        // max ping from my house was 13ms, so 4 times that should
        // be pretty safe
        dsock.setSoTimeout(60);
        dsock.connect(InetAddress.getByName("cs-reed-01.cs.sjsu.edu"), 1234);
        // no packet should be bigger than 110, but perhaps a bigger error
        // packet could come back
        byte[] bytes = new byte[200];
        DatagramPacket dpack = new DatagramPacket(bytes, bytes.length);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        // just put your name. no SSID
        bb.putShort(HELLO).put("hello, i am ben reed".getBytes()).flip();
        dpack.setLength(bb.remaining());
        sendRecv(dsock, dpack, bytes.length);
        long startClock = System.currentTimeMillis();
        bb.clear();
        bb.limit(dpack.getLength());
        checkError(bb);
        bb.getShort();
        int conversationId = bb.getInt();

        // extract out the name of who we are talking to
        String mess = new String(bytes, bb.position(), bb.remaining());
        String[] parts = mess.split(" ", 4);
        if (!mess.startsWith("hello, i am ")) {
            throw new RuntimeException("got bad response: " + mess);
        }

        byte[] buff = new byte[100];
        System.out.println(new File(".").getCanonicalPath());
        DigestInputStream fis = new DigestInputStream(new FileInputStream(args[0]), MessageDigest.getInstance("SHA-256"));
        int rc;
        int offset = 0;
        while ((rc = fis.read(buff)) > 0) {
            // System.out.println("sending offset: " + offset);
            bb.clear().putShort(TRANSFER).putInt(conversationId).putInt(offset).put(buff, 0, rc).flip();
            dpack.setLength(bb.remaining());
            sendRecv(dsock, dpack, bytes.length);
            bb.clear();
            bb.limit(dpack.getLength());
            checkError(bb);
            offset += rc;
        }

        bb.clear().putShort(CHECKSUM).putInt(conversationId).put(fis.getMessageDigest().digest(), 0, 8).flip();
        dpack.setLength(bb.remaining());
        sendRecv(dsock, dpack, bytes.length);
        bb.clear();
        bb.limit(dpack.getLength());
        checkError(bb);
        bb.getShort(); // skip the type
        bb.getInt(); // skip the conv id
        if (bb.get() == 0) {
            System.out.printf("success! Took about %dms\n", System.currentTimeMillis() - startClock);
        } else {
            System.out.println("failure: " + new String(bytes, bb.position(), bb.remaining()));
            System.exit(2);
        }

        // check the current standings
        // bb.rewind().putShort(CHECK_STANDINGS).flip();
        // pack.setLength(bb.remaining());
        // sendRecv(dsock, dpack, bytes.length);
        // System.out.printf("Standings:\n%s\n", new String(bytes, 2, dpack.getLength() - 2));
    }
}
