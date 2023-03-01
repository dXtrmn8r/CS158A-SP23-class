package edu.sjsu.cs158a.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class WebDump {
    public static void main(String args[]) {
        try {
            URL url = new URL("https://www.wikipedia.com/wiki/Linux");
            InputStream is = url.openStream();
            byte[] bytes = new byte[1024];
            int rc;
            while ((rc = is.read(bytes)) > 0) {
                System.out.write(bytes, 0, rc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}