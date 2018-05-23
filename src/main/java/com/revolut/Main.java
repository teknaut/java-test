package com.revolut;

import com.revolut.http.WebServer;

import java.util.logging.Logger;

public class Main{

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            new WebServer(8080);
        } catch (Exception ioe) {
            log.severe("Couldn't start server:\n" + ioe);
        }
    }



}
