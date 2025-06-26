package in.edu.kristujayanti.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBUtil {
  private static final String URI = "mongodb://localhost:27017";
  private static final String DB_NAME = "ecommerce";

  private static final MongoClient client = MongoClients.create(URI);
  public static final MongoDatabase database = client.getDatabase(DB_NAME);
}
