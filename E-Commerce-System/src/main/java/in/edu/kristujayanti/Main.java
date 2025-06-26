package in.edu.kristujayanti;

import com.sun.net.httpserver.HttpServer;
import in.edu.kristujayanti.handlers.SampleHandler;
import in.edu.kristujayanti.handlers.StaticFileHandler;

import java.net.InetSocketAddress;

public class Main {
  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    server.createContext("/api", new SampleHandler());
    server.createContext("/", new StaticFileHandler("src/main/resources/public"));
    server.setExecutor(null);
    server.start();
    System.out.println("Server started at http://localhost:8080");
  }
}
