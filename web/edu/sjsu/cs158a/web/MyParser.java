package edu.sjsu.cs158a.web;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.StringReader;

public class MyParser {
    final static String exampleString = "<head><title>This is the title</title></head>\n" +
                                        "Here is a link <a href=\"/cool.png\">cool</a>\n";
    static class MyParserCallback extends HTMLEditorKit.ParserCallback {
        @Override
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            System.out.println("Got tag: "+ t + " with attributes: " + a);
            // to check for specific tags
            System.out.println("Is title: " + HTML.Tag.TITLE.equals(t));
            // or
            // System.out.println("Is title: " + (HTML.Tag.TITLE == t))
            // Also, refer to HTML.Attribute HREF on JDK API;
            super.handleStartTag(t, a, pos);
        }

        @Override
        public void handleText(char[] data, int pos) {
            System.out.println("Got text: " + new String(data));
            super.handleText(data, pos);
        }
    }
    public static void main(String args[]) throws Exception {
        System.out.println(exampleString);
        ParserDelegator delegator = new ParserDelegator();
        delegator.parse(new StringReader(exampleString), new MyParserCallback(), true);
    }
}
