package edu.sjsu.cs158a.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class WebDump {
    public static void main(String args[]) {
        try {
            URL url = new URL("https://www.wikipedia.com/wiki/Linux");
            InputStream is = url.openStream();      // input stream
            byte[] bytes = new byte[1024];          // 1 kilobyte at a time
            int rc;                                 // return code
            while ((rc = is.read(bytes)) > 0) {     // read bytes as long as there are any to read
                System.out.write(bytes, 0, rc);     // write to stdin
                                                    // BE CAREFUL! bytes CAN'T always be mapped to String
                                                    // but since HTML code is in String
                                                    // System.out.print(new String(bytes, 0, rc)); should be okay
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
