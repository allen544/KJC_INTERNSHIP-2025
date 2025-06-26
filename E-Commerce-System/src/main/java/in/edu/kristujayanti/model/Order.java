package in.edu.kristujayanti.model;

import java.util.Date;
import java.util.List;

public class Order {
  public String id;
  public String userId;
  public List<String> productIds;
  public double totalAmount;
  public String status;
  public Date date;
}
