package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    int threadPoolSize;
    final static List<String> VALID_PATHS =List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public Server(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void runServer(int port) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            while(true) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleConnection(clientSocket));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void handleConnection(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())) {

            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                socket.close();
            }

            Map<String, String> queryParams = Request.getQueryParams(parts[1]);

            String path;
            if(parts[1].contains("?")) {
                path = parts[1].substring(0, parts[1].indexOf("?"));
            } else path = parts[1];

            if (!VALID_PATHS.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
            }

            final var filePath = FileSystems.getDefault().getPath("public", path);
            final var mimeType = Files.probeContentType(filePath);

            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
        e.printStackTrace();
        }
    }

    public void addHandler(String requestType, String path, Handler handler) {
        if(requestType.equals("GET") && isValidPath(path)) {
            handler.handleRequest();
        }
    }

    private boolean isValidPath(String path) {
        return VALID_PATHS.contains(path);
    }

}
