package in.edu.kristujayanti.services;

import com.mongodb.client.*;
import in.edu.kristujayanti.db.MongoDBUtil;
import in.edu.kristujayanti.model.Order;
import com.google.gson.Gson;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class SampleService {
  private final MongoCollection<Document> collection = MongoDBUtil.database.getCollection("orders");
  private final Gson gson = new Gson();

  public void placeOrder(Order order) {
    order.date = new Date();
    Document doc = Document.parse(gson.toJson(order));
    collection.insertOne(doc);
  }

  public void updateOrderStatus(String id, String status) {
    collection.updateOne(eq("_id", new ObjectId(id)), new Document("$set", new Document("status", status)));
  }

  public List<Order> getOrdersByUser(String userId) {
    List<Order> orders = new ArrayList<>();
    FindIterable<Document> docs = collection.find(eq("userId", userId));
    for (Document doc : docs) {
      orders.add(gson.fromJson(doc.toJson(), Order.class));
    }
    return orders;
  }

  public double getTotalSales(String productId) {
    List<Document> pipeline = Arrays.asList(
      new Document("$match", new Document("productIds", productId)),
      new Document("$group", new Document("_id", null).append("total", new Document("$sum", "$totalAmount")))
    );
    AggregateIterable<Document> result = collection.aggregate(pipeline);
    return result.first() != null ? result.first().getDouble("total") : 0.0;
  }
}
