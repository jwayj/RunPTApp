package com.example.myapplication.ui.popups;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;

public class LocalWebServer extends NanoHTTPD {

    public LocalWebServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String html = "<html><body><h1>Welcome to Local Web Server</h1></body></html>";
        return newFixedLengthResponse(Response.Status.OK, "text/html", html);
    }
}
