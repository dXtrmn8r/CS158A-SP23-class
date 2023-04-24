package edu.sjsu.cs158a;

import picocli.CommandLine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketOption;
import java.net.SocketOptions;
import java.net.StandardSocketOptions;

@CommandLine.Command
public class Blast {
    @CommandLine.Command(name = "client")
    public int client(@CommandLine.Parameters(paramLabel = "server") String server,
                      @CommandLine.Parameters(paramLabel = "port") int port) throws IOException {
        var s = new Socket(server, port);
        byte[] zeros = new byte[4096];
        var os = s.getOutputStream();
        System.out.println("dumping");
        for (int i = 0; i < 100; i++) {
            System.out.printf("sending block %d\n", i);
            os.write(zeros);
        }
        System.out.println("done");
        return 0;
    }

    @CommandLine.Command(name = "server")
    public int server(@CommandLine.Parameters(paramLabel = "port") int port) throws IOException, InterruptedException {
        var ss = new ServerSocket(port);
        var s = ss.accept();
        var in = s.getInputStream();
        var bytes = new byte[4000];
        for (int i = 4 ; i > 0; i--) {
            System.out.println(i);
            in.read(bytes);
            Thread.sleep(1000);
        }
        int block = 0;
        while (true) {
            System.out.println("Reading block " + block++);
            Thread.sleep(1000);
        }
    }


    public static void main(String[] args) {
        new CommandLine(new Blast()).execute(args);
    }

}
