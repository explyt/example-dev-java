package io.aiven.klaw.service;

import java.util.HashMap;
import java.util.Map;

public final class InventoryService {
  private final Map<String, Integer> stock = new HashMap<>();

  public boolean reserve(String sku, int qty) {
    Integer available = stock.get(sku);
    if (available == null || available < qty) {
      return false;
    }
    stock.put(sku, available - qty);
    return true;
  }

  public void addInbound(String sku, int qty) {
    if (qty <= 0) throw new IllegalArgumentException("qty > 0");
    stock.merge(sku, qty, Integer::sum);
  }

  public int available(String sku) {
    return stock.getOrDefault(sku, 0);
  }
}
