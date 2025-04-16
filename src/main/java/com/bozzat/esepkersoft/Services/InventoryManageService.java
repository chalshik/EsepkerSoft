package com.bozzat.esepkersoft.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryManageService {
    private final dbManager db = dbManager.getInstance();
    
    /**
     * Returns all products with their current stock balances
     * @return List of Maps containing complete product and stock information
     */
    public List<Map<String, Object>> getInventory() {
        String query = "SELECT " +
                "p.id, p.name, p.barcode, p.category_id, p.unit_type, " +
                "p.current_price, p.created_at, " +
                "COALESCE(sb.quantity, 0) as stock_quantity, " +
                "c.name as category_name " +
                "FROM products p " +
                "LEFT JOIN stock_balances sb ON p.id = sb.product_id " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "ORDER BY p.name";
                
        List<Map<String, Object>> results = db.executeGet(query);
        List<Map<String, Object>> inventory = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> item = new HashMap<>();
            
            // Product details
            item.put("id", ((Number) row.get("id")).intValue());
            item.put("name", (String) row.get("name"));
            item.put("barcode", (String) row.get("barcode"));
            
            // Category information
            if (row.get("category_id") != null) {
                item.put("categoryId", ((Number) row.get("category_id")).intValue());
                item.put("categoryName", row.get("category_name"));
            } else {
                item.put("categoryId", 0);
                item.put("categoryName", "");
            }
            
            // Product attributes
            item.put("unitType", (String) row.get("unit_type"));
            item.put("currentPrice", ((Number) row.get("current_price")).doubleValue());
            
            // Stock quantity
            double quantity = ((Number) row.get("stock_quantity")).doubleValue();
            item.put("stockQuantity", quantity);
            
            // Timestamps
            if (row.get("created_at") != null) {
                item.put("createdAt", (String) row.get("created_at"));
            }
            
            inventory.add(item);
        }
        
        return inventory;
    }
}
