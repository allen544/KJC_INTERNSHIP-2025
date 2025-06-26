package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.file.*;

public class StaticFileHandler implements HttpHandler {
  private final String basePath;

  public StaticFileHandler(String basePath) {
    this.basePath = basePath;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getPath();
    if (path.equals("/")) path = "/index.html";

    File file = new File(basePath + path);
    if (!file.exists()) {
      exchange.sendResponseHeaders(404, 0);
      exchange.close();
      return;
    }

    byte[] bytes = Files.readAllBytes(file.toPath());
    exchange.getResponseHeaders().add("Content-Type", "text/html");
    exchange.sendResponseHeaders(200, bytes.length);
    OutputStream os = exchange.getResponseBody();
    os.write(bytes);
    os.close();
  }
}
