package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    final static int PORT = 9999;
    final static int THREAD_POOL_SIZE = 64;
    public static void main(String[] args) {
        Server server = new Server(THREAD_POOL_SIZE);
        server.runServer(PORT);

    }
}