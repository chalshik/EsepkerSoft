package com.bozzat.esepkersoft.Services;

import com.bozzat.esepkersoft.Models.Product;

import java.util.List;
import java.util.Map;

public class ProductService {
    private dbManager db = dbManager.getInstance();

    public  Product getProductByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return null;
        }

        String query = "SELECT * FROM products WHERE barcode = ?";
        List<Map<String, Object>> results = db.executeGet(query, barcode.trim());

        if (results.isEmpty()) {
            return null;
        }

        Map<String, Object> productData = results.get(0);
        return new Product(
                ((Number) productData.get("id")).intValue(),
                (String) productData.get("barcode"),
                (String) productData.get("name"),
                (String) productData.get("unit_type"),
                ((Number) productData.get("current_price")).doubleValue()
        );
    }

    public boolean addProduct(Product product) {
        if (product == null ||
                product.getBarcode() == null || product.getBarcode().trim().isEmpty() ||
                product.getName() == null || product.getName().trim().isEmpty() ||
                product.getCurrentPrice() <= 0) {
            return false;
        }



        String query = "INSERT INTO products " +
                "(barcode, name, unit_type, current_price) " +
                "VALUES (?, ?, ?, ?, ?)";

        return db.executeSet(query,
                product.getBarcode().trim(),
                product.getName().trim(),
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

        // First check if product exists in inventory
        List<Map<String, Object>> inventoryCheck = db.executeGet(
                "SELECT quantity FROM inventory WHERE product_id = ?",
                productId
        );

        if (!inventoryCheck.isEmpty()) {
            double currentStock = ((Number) inventoryCheck.get(0).get("quantity")).doubleValue();
            if (currentStock > 0) {
                System.err.println("Cannot delete product with remaining stock");
                return false;
            }
        }

        // Delete product (cascade will handle inventory)
        return db.executeSet("DELETE FROM products WHERE id = ?", productId);
    }

    public List<Product> getAllProducts() {
        String query = "SELECT * FROM products ORDER BY name";
        List<Map<String, Object>> results = db.executeGet(query);

        return results.stream()
                .map(row -> new Product(
                        ((Number) row.get("id")).intValue(),
                        (String) row.get("barcode"),
                        (String) row.get("name"),
                        (String) row.get("unit_type"),
                        ((Number) row.get("current_price")).doubleValue()
                ))
                .toList();
    }

    public List<Product> searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProducts();
        }

        String query = "SELECT * FROM products " +
                "WHERE barcode LIKE ? OR name LIKE ? " +
                "ORDER BY name";

        String likeTerm = "%" + searchTerm.trim() + "%";
        List<Map<String, Object>> results = db.executeGet(query, likeTerm, likeTerm, likeTerm);

        return results.stream()
                .map(row -> new Product(
                        ((Number) row.get("id")).intValue(),
                        (String) row.get("barcode"),
                        (String) row.get("name"),
                        (String) row.get("unit_type"),
                        ((Number) row.get("current_price")).doubleValue()
                ))
                .toList();
    }

    public boolean updateProductPrice(long productId, double newPrice) {
        if (productId <= 0 || newPrice <= 0) {
            return false;
        }

        return db.executeSet(
                "UPDATE products SET current_price = ? WHERE id = ?",
                newPrice, productId
        );
    }
}
