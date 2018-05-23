package com.revolut;

import com.revolut.http.WebServer;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Main{

    public static void main(String[] args) {
        try {
            new WebServer(8080);
        } catch (Exception ioe) {
            log.error("Couldn't start server:\n" + ioe);
        }
    }



}
