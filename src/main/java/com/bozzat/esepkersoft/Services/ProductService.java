package com.bozzat.esepkersoft.Services;

import com.bozzat.esepkersoft.Models.Product;

import java.time.LocalDateTime;
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
        product.setId(((Number) productData.get("id")).intValue());
        product.setName((String) productData.get("name"));
        product.setBarcode((String) productData.get("barcode"));
        product.setCategoryId(((Number) productData.get("category_id")).intValue());
        product.setUnitType((String) productData.get("unit_type"));
        product.setCurrentPrice(((Number) productData.get("current_price")).doubleValue());
        product.setCreatedAt(LocalDateTime.parse((String) productData.get("created_at")));
        
        return product;
    }

    public boolean addProduct(Product product) {
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

    public List<Product> getAllProducts() {
        String query = "SELECT * FROM products ORDER BY name";
        List<Map<String, Object>> results = db.executeGet(query);

        return results.stream()
                .map(row -> {
                    Product product = new Product();
                    product.setId(((Number) row.get("id")).intValue());
                    product.setName((String) row.get("name"));
                    product.setBarcode((String) row.get("barcode"));
                    product.setCategoryId(((Number) row.get("category_id")).intValue());
                    product.setUnitType((String) row.get("unit_type"));
                    product.setCurrentPrice(((Number) row.get("current_price")).doubleValue());
                    product.setCreatedAt(LocalDateTime.parse((String) row.get("created_at")));
                    return product;
                })
                .toList();
    }


}
