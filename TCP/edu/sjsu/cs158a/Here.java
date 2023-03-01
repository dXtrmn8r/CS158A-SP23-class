package edu.sjsu.cs158a;

import java.io.IOException;
import java.net.Socket;

public class Here {
    public static void main(String[] args) throws IOException {
        var sock = new Socket("cs-reed-02.class.homeofcode.com", 3333);
        var out = sock.getOutputStream();
        var in = sock.getInputStream();
        var len = in.read();
        var bytes = in.readNBytes(len);
        System.out.printf("%s\n", new String(bytes, 0, len));
        final int myid = 3_827_384;
        var myidBytes = new byte[4];
        myidBytes[0] = myid >> 24;
        myidBytes[1] = (byte)((myid >> 16) & 0xff);
        myidBytes[2] = (byte)((myid >> 8) & 0xff);
        myidBytes[3] = (byte)(myid & 0xff);
        var name = "mr frog";
        var nameBytes = name.getBytes();
        out.write(myidBytes);
        out.write(nameBytes.length);
        out.write(nameBytes);
        var pin = in.readNBytes(4);
        System.out.println(new String(pin));

    }
}
