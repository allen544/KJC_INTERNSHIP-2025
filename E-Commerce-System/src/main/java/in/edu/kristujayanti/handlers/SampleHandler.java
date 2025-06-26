package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.*;
import com.google.gson.Gson;
import in.edu.kristujayanti.model.Order;
import in.edu.kristujayanti.services.SampleService;

import java.io.*;
import java.net.URI;
import java.util.List;

public class SampleHandler implements HttpHandler {
  private final SampleService service = new SampleService();
  private final Gson gson = new Gson();

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getPath();
    String method = exchange.getRequestMethod();
    URI uri = exchange.getRequestURI();
    String response = "";

    if (method.equalsIgnoreCase("POST") && path.equals("/api/orders")) {
      Order order = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), Order.class);
      service.placeOrder(order);
      response = "Order Placed";
    } else if (method.equalsIgnoreCase("PUT") && path.startsWith("/api/orders/") && path.endsWith("/status")) {
      String id = path.split("/")[3];
      String status = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), String.class);
      service.updateOrderStatus(id, status);
      response = "Status Updated";
    } else if (method.equalsIgnoreCase("GET") && path.startsWith("/api/orders/user/")) {
      String userId = path.split("/")[4];
      List<Order> orders = service.getOrdersByUser(userId);
      response = gson.toJson(orders);
    } else if (method.equalsIgnoreCase("GET") && path.startsWith("/api/sales")) {
      String query = uri.getQuery(); // product=p1
      String productId = query.split("=")[1];
      double sales = service.getTotalSales(productId);
      response = "{\"totalSales\":" + sales + "}";
    } else {
      response = "Invalid Endpoint";
    }

    exchange.sendResponseHeaders(200, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }
}
