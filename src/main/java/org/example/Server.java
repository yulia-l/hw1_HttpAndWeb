package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    int threadPoolSize;
    final static List<String> VALID_PATHS =List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public Server(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void runServer(int port) {
        final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        try {
            final var serverSocket = new ServerSocket(9999);
            while(true) {
                final var clientSocket = serverSocket.accept();
                executorService.submit(() -> handleConnection(clientSocket));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void handleConnection(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            //строка запроса только для чтения для простоты
            //должно быть в форме GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                socket.close();
            }

            final var path = parts[1];
            if (!VALID_PATHS.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
            }

            final var filePath = Path.of(".", "public", path); //создается объект класса Path по указанному пути
            final var mimeType = Files.probeContentType(filePath); //определяется тип содержимого файла

            //специальный чехол для classic
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                //меняет время на актуальное
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
}
