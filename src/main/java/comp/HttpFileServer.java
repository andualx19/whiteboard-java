package comp;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class HttpFileServer {
    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/"))
                path = "/whiteboard.html";

            File index = new File("src/main/guest/" + path.substring(1));

            if (!index.exists()) {
                System.out.println("File not found: " + index.getAbsolutePath());
                String err = "404 - File not found!";
                exchange.sendResponseHeaders(404, err.length());
                exchange.getResponseBody().write(err.getBytes());
            } else {
                byte[] data = Files.readAllBytes(index.toPath());

                String contentType = "text/plain";
                if (path.endsWith(".html")) contentType = "text/html";
                else if (path.endsWith(".css")) contentType = "text/css";
                else if (path.endsWith(".js")) contentType = "application/javascript";

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, data.length);
                exchange.getResponseBody().write(data);
            }
            exchange.close();
        });

        server.start();
        System.out.println("HTTP PORT: " + port);
    }
}
