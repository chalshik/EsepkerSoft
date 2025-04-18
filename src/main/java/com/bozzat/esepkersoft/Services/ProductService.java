package com.bozzat.esepkersoft.Services;

import com.bozzat.esepkersoft.Models.Product;
import com.bozzat.esepkersoft.Models.StockEntry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ProductService {
    private final dbManager db = dbManager.getInstance();

    public Product getProductByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return null;
        }

        String query = "SELECT * FROM products WHERE barcode = ?";
        List<Map<String, Object>> results = db.executeGet(query, barcode.trim());

        if (results.isEmpty()) {
            return null;
        }

        Map<String, Object> productData = results.get(0);
        Product product = new Product();

        // Safely extract the 'id' value
        Object idObj = productData.get("id");
        if (idObj == null) {
            throw new RuntimeException("Product ID not found.");
        }
        product.setId(((Number) idObj).intValue());

        // Safely extract the 'name' value
        product.setName((String) productData.get("name"));

        // Safely extract the 'barcode' value
        product.setBarcode((String) productData.get("barcode"));

        // Safely extract 'category_id' and handle possible null
        Object categoryIdObj = productData.get("category_id");
        if (categoryIdObj != null) {
            product.setCategoryId(((Number) categoryIdObj).intValue());
        } else {
            product.setCategoryId(0);  // Or some default value or handling
        }

        // Safely extract the 'unit_type'
        product.setUnitType((String) productData.get("unit_type"));

        // Safely extract 'current_price'
        Object priceObj = productData.get("current_price");
        if (priceObj != null) {
            product.setCurrentPrice(((Number) priceObj).doubleValue());
        } else {
            product.setCurrentPrice(0.0);  // Handle default value if needed
        }

        // Safely parse 'created_at' string to LocalDateTime with custom formatter
        String createdAtStr = (String) productData.get("created_at");
        if (createdAtStr != null) {
            // Define the format of your date string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try {
                product.setCreatedAt(LocalDateTime.parse(createdAtStr, formatter));
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse 'created_at' date: " + e.getMessage());
            }
        } else {
            product.setCreatedAt(LocalDateTime.now());  // Or a default value
        }

        return product;
    }
    public Product addProduct(Product product) {
        if (product == null ||
                product.getBarcode() == null || product.getBarcode().trim().isEmpty() ||
                product.getName() == null || product.getName().trim().isEmpty() ||
                product.getUnitType() == null || product.getUnitType().trim().isEmpty() ||
                product.getCurrentPrice() <= 0) {
            return null;
        }

        try {
            // Start transaction
            db.executeSet("BEGIN TRANSACTION");

            String query = "INSERT INTO products " +
                    "(name, barcode, unit_type, current_price) " +
                    "VALUES (?, ?, ?, ?)";
            System.out.println(product.getName().trim() +
                    product.getBarcode().trim() +
                    product.getUnitType() +
                    product.getCurrentPrice());
            if (!db.executeSet(query,
                    product.getName().trim(),
                    product.getBarcode().trim(),
                    product.getUnitType(),
                    product.getCurrentPrice())) {
                System.out.println(product.getName().trim() +
                        product.getBarcode().trim() +
                        product.getUnitType() +
                        product.getCurrentPrice());
                throw new Exception("Failed to insert product");
            }

            // Get the complete product data
            Product createdProduct = getProductByBarcode(product.getBarcode());
            if (createdProduct == null) {
                throw new Exception("Failed to retrieve created product");
            }

            // Commit transaction
            db.executeSet("COMMIT");

            return createdProduct;

        } catch (Exception e) {
            System.err.println("Product addition failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return null;
        }
    }

    public boolean updateProduct(Product product) {
        if (product == null || product.getId() <= 0) {
            return false;
        }

        try {
            // Start transaction
            db.executeSet("BEGIN TRANSACTION");
            
            // Get the existing product to check if price has changed
          

            String query = "UPDATE products SET " +
                    "barcode = ?, name = ?, " +
                    "unit_type = ?, current_price = ? " +
                    "WHERE id = ?";

            if (!db.executeSet(query,
                    product.getBarcode(),
                    product.getName(),
                    product.getUnitType(),
                    product.getCurrentPrice(),
                    product.getId())) {
                throw new Exception("Failed to update product");
            }
            
            // Commit transaction
            db.executeSet("COMMIT");
            return true;
            
        } catch (Exception e) {
            System.err.println("Product update failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return false;
        }
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



    private boolean updateStockBalance(int productId, double quantityChange) {
        try {
            // Check if stock balance exists
            List<Map<String, Object>> existingBalance = db.executeGet(
                    "SELECT quantity FROM stock_balances WHERE product_id = ?",
                    productId
            );

            if (existingBalance.isEmpty()) {
                // Only allow positive quantities for new balances
                if (quantityChange < 0) {
                    return false;
                }
                
                // Create new stock balance
                String insertQuery = "INSERT INTO stock_balances (product_id, quantity, updated_at) " +
                        "VALUES (?, ?, datetime('now', 'localtime'))";
                return db.executeSet(insertQuery, productId, quantityChange);
            } else {
                // Get current quantity
                double currentQuantity = ((Number) existingBalance.get(0).get("quantity")).doubleValue();
                
                // Check if the resulting quantity would be negative
                if ((currentQuantity + quantityChange) < 0) {
                    return false;
                }
                
                // Update existing stock balance
                String updateQuery = "UPDATE stock_balances SET quantity = quantity + ?, updated_at = datetime('now', 'localtime') " +
                        "WHERE product_id = ?";
                return db.executeSet(updateQuery, quantityChange, productId);
            }
        } catch (Exception e) {
            System.err.println("Stock balance update failed: " + e.getMessage());
            return false;
        }
    }

    private boolean addStockEntry(int productId, double quantity, double purchasePrice, Integer supplierId, String note) {
        String query = "INSERT INTO stock_entries " +
                "(product_id, quantity, purchase_price, supplier_id, arrival_date, note) " +
                "VALUES (?, ?, ?, ?, datetime('now', 'localtime'), ?)";
        
        return db.executeSet(query, productId, quantity, purchasePrice, supplierId, note != null ? note : "");
    }

    public boolean addBatchEntry(Product product, StockEntry stockEntry) {
        if (product == null || stockEntry == null || 
            product.getBarcode() == null || product.getBarcode().isEmpty() ||
            stockEntry.getQuantity() <= 0 || stockEntry.getPurchasePrice() <= 0) {
            return false;
        }

        try {
            // Start transaction
            db.executeSet("BEGIN TRANSACTION");

            // Verify product exists
            Product existingProduct = getProductByBarcode(product.getBarcode());
            if (existingProduct == null) {
                throw new Exception("Product not found with barcode: " + product.getBarcode());
            }

            // Add stock entry
            if (!addStockEntry(existingProduct.getId(), 
                               stockEntry.getQuantity(), 
                               stockEntry.getPurchasePrice(), 
                               stockEntry.getSupplierId(),
                               stockEntry.getNote())) {
                throw new Exception("Failed to add stock entry");
            }

            // Update stock balance
            if (!updateStockBalance(existingProduct.getId(), stockEntry.getQuantity())) {
                throw new Exception("Failed to update stock balance");
            }

            // Commit transaction
            db.executeSet("COMMIT");
            return true;

        } catch (Exception e) {
            System.err.println("Batch entry addition failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return false;
        }
    }

    public Product registerNewProduct(Product product, StockEntry stockEntry) {
        if (product == null || stockEntry == null || 
            product.getBarcode() == null || product.getBarcode().isEmpty() ||
            stockEntry.getQuantity() <= 0 || stockEntry.getPurchasePrice() <= 0) {
            return null;
        }

        try {
            // Start transaction
            Product createdProduct = addProduct(product);
            db.executeSet("BEGIN TRANSACTION");

            // Add the product

            if (createdProduct == null) {
                throw new Exception("Failed to add product");
            }

            // Add stock entry
            if (!addStockEntry(createdProduct.getId(), 
                               stockEntry.getQuantity(), 
                               stockEntry.getPurchasePrice(), 
                               stockEntry.getSupplierId(),
                               stockEntry.getNote())) {
                throw new Exception("Failed to add stock entry");
            }

            // Update stock balance
            if (!updateStockBalance(createdProduct.getId(), stockEntry.getQuantity())) {
                throw new Exception("Failed to update stock balance");
            }

            // Commit transaction
            db.executeSet("COMMIT");
            System.out.println("Success");
            return createdProduct;

        } catch (Exception e) {
            System.err.println("Product registration failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return null;
        }
    }

  

    /**
     * Get current stock quantity for a product
     * @return current stock quantity or -1 if product doesn't exist
     */
    public double getStockQuantity(int productId) {
        if (productId <= 0) {
            return -1;
        }

        List<Map<String, Object>> results = db.executeGet(
                "SELECT quantity FROM stock_balances WHERE product_id = ?", 
                productId
        );

        if (results.isEmpty()) {
            return 0; // No stock record means zero quantity
        }

        return ((Number) results.get(0).get("quantity")).doubleValue();
    }
}
