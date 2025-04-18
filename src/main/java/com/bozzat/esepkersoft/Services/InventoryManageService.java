package com.bozzat.esepkersoft.Services;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryManageService {
    private final dbManager db = dbManager.getInstance();
    
    /**
     * Returns inventory information with only essential fields:
     * - Product name
     * - Current price
     * - Stock quantity
     * - Unit type
     * - Last batch date (most recent stock entry)
     * 
     * @param ascending If true, sorts in ascending order; if false, sorts in descending order
     * @return List of Maps containing simplified inventory information
     */
    public List<Map<String, Object>> getInventory(boolean ascending) {
        String query = "SELECT " +
                "p.id, p.name, p.current_price, p.unit_type, " +
                "COALESCE(sb.quantity, 0) as stock_quantity, " +
                "(SELECT MAX(se.arrival_date) FROM stock_entries se WHERE se.product_id = p.id) as last_batch_date " +
                "FROM products p " +
                "LEFT JOIN stock_balances sb ON p.id = sb.product_id " +
                "ORDER BY p.name " + (ascending ? "ASC" : "DESC");
                
        List<Map<String, Object>> results = db.executeGet(query);
        List<Map<String, Object>> inventory = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> item = new HashMap<>();
            
            // Essential product info
            item.put("name", (String) row.get("name"));
            item.put("unitType", (String) row.get("unit_type"));
            item.put("currentPrice", ((Number) row.get("current_price")).doubleValue());
            
            // Stock quantity
            double quantity = ((Number) row.get("stock_quantity")).doubleValue();
            item.put("quantityInStock", quantity);
            
            // Last batch date
            if (row.get("last_batch_date") != null) {
                item.put("lastBatchDate", (String) row.get("last_batch_date"));
            } else {
                item.put("lastBatchDate", "");
            }
            
            inventory.add(item);
        }
        
        return inventory;
    }
    
    /**
     * Overloaded method that defaults to ascending order
     * @return List of Maps containing simplified inventory information
     */
    public List<Map<String, Object>> getInventory() {
        return getInventory(true);
    }
    
    /**
     * Returns inventory information filtered by category ID
     * 
     * @param categoryId The category ID to filter by
     * @param ascending If true, sorts in ascending order; if false, sorts in descending order
     * @return List of Maps containing inventory information for the specified category
     */
    public List<Map<String, Object>> getInventoryByCategory(int categoryId, boolean ascending) {
        if (categoryId <= 0) {
            return getInventory(ascending); // Return all inventory if invalid category ID
        }
        
        String query = "SELECT " +
                "p.id, p.name, p.current_price, p.unit_type, " +
                "COALESCE(sb.quantity, 0) as stock_quantity, " +
                "(SELECT MAX(se.arrival_date) FROM stock_entries se WHERE se.product_id = p.id) as last_batch_date " +
                "FROM products p " +
                "LEFT JOIN stock_balances sb ON p.id = sb.product_id " +
                "WHERE p.category_id = ? " +
                "ORDER BY p.name " + (ascending ? "ASC" : "DESC");
                
        List<Map<String, Object>> results = db.executeGet(query, categoryId);
        List<Map<String, Object>> inventory = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> item = new HashMap<>();
            
            // Essential product info
            item.put("name", (String) row.get("name"));
            item.put("unitType", (String) row.get("unit_type"));
            item.put("currentPrice", ((Number) row.get("current_price")).doubleValue());
            
            // Stock quantity
            double quantity = ((Number) row.get("stock_quantity")).doubleValue();
            item.put("quantityInStock", quantity);
            
            // Last batch date
            if (row.get("last_batch_date") != null) {
                item.put("lastBatchDate", (String) row.get("last_batch_date"));
            } else {
                item.put("lastBatchDate", "");
            }
            
            inventory.add(item);
        }
        
        return inventory;
    }
    
    /**
     * Overloaded method that defaults to ascending order
     * @param categoryId The category ID to filter by
     * @return List of Maps containing inventory information for the specified category
     */
    public List<Map<String, Object>> getInventoryByCategory(int categoryId) {
        return getInventoryByCategory(categoryId, true);
    }
}
