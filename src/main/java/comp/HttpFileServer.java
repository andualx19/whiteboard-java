package comp;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpFileServer {
    private static final Map<String, byte[]> fileCache = new HashMap<>();
    private static final Map<String, String> mimeTypes = new HashMap<>();

    static {
        mimeTypes.put("html", "text/html; charset=UTF-8");
        mimeTypes.put("css", "text/css; charset=UTF-8");
        mimeTypes.put("js", "application/javascript; charset=UTF-8");
        mimeTypes.put("png", "image/png");
    }

    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/whiteboard.html";

            String cleanPath = path.substring(1);
            byte[] responseData;

            if (fileCache.containsKey(cleanPath)) {
                responseData = fileCache.get(cleanPath);
            } else {
                File file = new File("src/main/guest/" + cleanPath);
                if (file.exists() && !file.isDirectory()) {
                    responseData = Files.readAllBytes(file.toPath());
                    fileCache.put(cleanPath, responseData);
                } else {
                    String err = "404 - File not found";
                    exchange.sendResponseHeaders(404, err.length());
                    exchange.getResponseBody().write(err.getBytes());
                    exchange.close();
                    return;
                }
            }

            String extension = cleanPath.substring(cleanPath.lastIndexOf(".") + 1);
            String contentType = mimeTypes.getOrDefault(extension, "text/plain");

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Cache-Control", "max-age=3600");

            exchange.sendResponseHeaders(200, responseData.length);
            exchange.getResponseBody().write(responseData);

            exchange.close();
        });

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("HTTP PORT: " + port);
    }
}
