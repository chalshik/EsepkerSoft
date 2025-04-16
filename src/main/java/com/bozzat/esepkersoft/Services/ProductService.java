package com.bozzat.esepkersoft.Services;

import com.bozzat.esepkersoft.Models.Product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ProductService {
    private final dbManager db = dbManager.getInstance();

    // ===== Product Registration & Initial Setup =====
    public boolean registerNewProduct(Product product, double initialQuantity, double purchasePrice, int supplierId) {
        if (product == null || initialQuantity <= 0 || purchasePrice <= 0) {
            return false;
        }

        try {
            db.executeSet("BEGIN TRANSACTION");

            // 1. Add product
            if (!addProduct(product)) {
                throw new Exception("Failed to add product");
            }

            // 2. Get product ID
            int productId = getLastInsertedId();
            if (productId <= 0) {
                throw new Exception("Failed to get product ID");
            }

            // 3. Add initial stock
            if (!addStockEntry(productId, initialQuantity, purchasePrice, supplierId)) {
                throw new Exception("Failed to add initial stock");
            }

            // 4. Update stock balance
            if (!updateStockBalance(productId, initialQuantity)) {
                throw new Exception("Failed to update stock balance");
            }

            db.executeSet("COMMIT");
            return true;

        } catch (Exception e) {
            System.err.println("Product registration failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return false;
        }
    }

    // ===== Stock Management =====
    public boolean addStockBatch(int productId, double quantity, double purchasePrice, int supplierId) {
        if (productId <= 0 || quantity <= 0 || purchasePrice <= 0) {
            return false;
        }

        try {
            db.executeSet("BEGIN TRANSACTION");

            // 1. Add stock entry
            if (!addStockEntry(productId, quantity, purchasePrice, supplierId)) {
                throw new Exception("Failed to add stock entry");
            }

            // 2. Update stock balance
            if (!updateStockBalance(productId, quantity)) {
                throw new Exception("Failed to update stock balance");
            }

            db.executeSet("COMMIT");
            return true;

        } catch (Exception e) {
            System.err.println("Stock addition failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return false;
        }
    }

    public boolean reduceStock(int productId, double quantity, String reason) {
        if (productId <= 0 || quantity <= 0) {
            return false;
        }

        try {
            db.executeSet("BEGIN TRANSACTION");

            // 1. Verify sufficient stock
            double currentStock = getCurrentStock(productId);
            if (currentStock < quantity) {
                throw new Exception("Insufficient stock available");
            }

            // 2. Add stock entry (negative quantity)
            if (!addStockEntry(productId, -quantity, 0, 0)) {
                throw new Exception("Failed to record stock reduction");
            }

            // 3. Update stock balance
            if (!updateStockBalance(productId, -quantity)) {
                throw new Exception("Failed to update stock balance");
            }

            db.executeSet("COMMIT");
            return true;

        } catch (Exception e) {
            System.err.println("Stock reduction failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return false;
        }
    }

    // ===== Product Information =====
    public Product getProductByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return null;
        }

        String query = "SELECT p.*, sb.quantity as current_stock " +
                      "FROM products p " +
                      "LEFT JOIN stock_balances sb ON p.id = sb.product_id " +
                      "WHERE p.barcode = ?";

        List<Map<String, Object>> results = db.executeGet(query, barcode.trim());
        if (results.isEmpty()) {
            return null;
        }

        return mapToProduct(results.get(0));
    }

    public List<Product> getAllProducts() {
        String query = "SELECT p.*, sb.quantity as current_stock " +
                      "FROM products p " +
                      "LEFT JOIN stock_balances sb ON p.id = sb.product_id " +
                      "ORDER BY p.name";

        List<Map<String, Object>> results = db.executeGet(query);
        return results.stream()
                .map(this::mapToProduct)
                .toList();
    }

    public double getCurrentStock(int productId) {
        String query = "SELECT quantity FROM stock_balances WHERE product_id = ?";
        List<Map<String, Object>> results = db.executeGet(query, productId);
        
        if (results.isEmpty()) {
            return 0.0;
        }
        return ((Number) results.get(0).get("quantity")).doubleValue();
    }

    // ===== Helper Methods =====
    private int getLastInsertedId() {
        List<Map<String, Object>> result = db.executeGet("SELECT last_insert_rowid() as id");
        if (result.isEmpty()) {
            return 0;
        }
        return ((Number) result.get(0).get("id")).intValue();
    }

    private Product mapToProduct(Map<String, Object> row) {
        Product product = new Product();
        product.setId(((Number) row.get("id")).intValue());
        product.setName((String) row.get("name"));
        product.setBarcode((String) row.get("barcode"));
        product.setCategoryId(((Number) row.get("category_id")).intValue());
        product.setUnitType((String) row.get("unit_type"));
        product.setCurrentPrice(((Number) row.get("current_price")).doubleValue());
        product.setCreatedAt(LocalDateTime.parse((String) row.get("created_at")));
        
        // Add current stock if available
        if (row.containsKey("current_stock")) {
            product.setCurrentStock(((Number) row.get("current_stock")).doubleValue());
        }
        
        return product;
    }

    private boolean addStockEntry(int productId, double quantity, double purchasePrice, int supplierId) {
        String query = "INSERT INTO stock_entries " +
                "(product_id, quantity, purchase_price, supplier_id, arrival_date) " +
                "VALUES (?, ?, ?, ?, datetime('now', 'localtime'))";
        
        return db.executeSet(query, productId, quantity, purchasePrice, supplierId);
    }

    private boolean updateStockBalance(int productId, double quantityChange) {
        List<Map<String, Object>> existingBalance = db.executeGet(
                "SELECT quantity FROM stock_balances WHERE product_id = ?",
                productId
        );

        if (existingBalance.isEmpty()) {
            String insertQuery = "INSERT INTO stock_balances (product_id, quantity, updated_at) " +
                    "VALUES (?, ?, datetime('now', 'localtime'))";
            return db.executeSet(insertQuery, productId, quantityChange);
        } else {
            String updateQuery = "UPDATE stock_balances SET quantity = quantity + ?, updated_at = datetime('now', 'localtime') " +
                    "WHERE product_id = ?";
            return db.executeSet(updateQuery, quantityChange, productId);
        }
    }

    private boolean addProduct(Product product) {
        if (product == null ||
                product.getBarcode() == null || product.getBarcode().trim().isEmpty() ||
                product.getName() == null || product.getName().trim().isEmpty() ||
                product.getUnitType() == null || product.getUnitType().trim().isEmpty() ||
                product.getCurrentPrice() <= 0) {
            return false;
        }

        String query = "INSERT INTO products " +
                "(name, barcode, category_id, unit_type, current_price) " +
                "VALUES (?, ?, ?, ?, ?)";

        return db.executeSet(query,
                product.getName().trim(),
                product.getBarcode().trim(),
                product.getCategoryId(),
                product.getUnitType(),
                product.getCurrentPrice()
        );
    }

    public boolean updateProduct(Product product) {
        if (product == null || product.getId() <= 0) {
            return false;
        }

        String query = "UPDATE products SET " +
                "barcode = ?, name = ?, " +
                "unit_type = ?, current_price = ? " +
                "WHERE id = ?";

        return db.executeSet(query,
                product.getBarcode(),
                product.getName(),
                product.getUnitType(),
                product.getCurrentPrice(),
                product.getId()
        );
    }

    public boolean deleteProduct(long productId) {
        if (productId <= 0) {
            return false;
        }

        try {
            // Start transaction
            db.executeSet("BEGIN TRANSACTION");

            // Check if product exists in stock_balances
            List<Map<String, Object>> stockCheck = db.executeGet(
                    "SELECT quantity FROM stock_balances WHERE product_id = ?",
                    productId
            );

            if (!stockCheck.isEmpty()) {
                double currentStock = ((Number) stockCheck.get(0).get("quantity")).doubleValue();
                if (currentStock > 0) {
                    System.err.println("Cannot delete product with remaining stock");
                    db.executeSet("ROLLBACK");
                    return false;
                }
            }

            // Delete the product (ON DELETE CASCADE will handle related records)
            if (!db.executeSet("DELETE FROM products WHERE id = ?", productId)) {
                throw new Exception("Failed to delete product");
            }

            // Commit transaction
            db.executeSet("COMMIT");
            return true;

        } catch (Exception e) {
            System.err.println("Product deletion failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return false;
        }
    }
}
