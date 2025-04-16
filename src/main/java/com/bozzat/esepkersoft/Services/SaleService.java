package com.bozzat.esepkersoft.Services;

import com.bozzat.esepkersoft.Models.Sale;
import com.bozzat.esepkersoft.Models.SaleItem;

import java.util.List;
import java.util.Map;

public class SaleService {
    private dbManager db = dbManager.getInstance();

    public boolean addSale(List<SaleItem> saleItems, Sale sale) {
        if (saleItems == null || saleItems.isEmpty()) {
            System.err.println("Sale must contain at least one item");
            return false;
        }

        if (sale == null || sale.getPaymentMethod() == null || sale.getPaymentMethod().trim().isEmpty()) {
            System.err.println("Payment method is required");
            return false;
        }

        try {
            // Start transaction
            db.executeSet("BEGIN TRANSACTION");

            // 1. Insert the sale record
            String insertSaleQuery = "INSERT INTO sales (payment_method, total_amount, sale_time, comment) VALUES (?, ?, datetime('now', 'localtime'), ?)";
            if (!db.executeSet(insertSaleQuery, sale.getPaymentMethod(), sale.getTotalAmount(), sale.getComment())) {
                throw new Exception("Failed to insert sale record");
            }

            // 2. Get the generated sale ID
            List<Map<String, Object>> result = db.executeGet("SELECT last_insert_rowid() as id");
            if (result.isEmpty()) {
                throw new Exception("Failed to retrieve sale ID");
            }
            int saleId = ((Number) result.get(0).get("id")).intValue();

            // 3. Process each sale item
            for (SaleItem item : saleItems) {
                // Set the sale ID on the item
                item.setSaleId(saleId);

                // Verify product exists and has sufficient stock
                if (!verifyProductAndStock(item.getProductId(), item.getQuantity())) {
                    throw new Exception("Insufficient stock for product: " + item.getProductId());
                }

                // 4. Update inventory (direct deduction)
                if (!updateInventory(item.getProductId(), -item.getQuantity())) {
                    throw new Exception("Failed to update inventory for product: " + item.getProductId());
                }

                // 5. Insert sale item
                String insertItemQuery = "INSERT INTO sale_items " +
                        "(sale_id, product_id, quantity, unit_price) " +
                        "VALUES (?, ?, ?, ?)";

                if (!db.executeSet(insertItemQuery,
                        item.getSaleId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice())) {
                    throw new Exception("Failed to insert sale item");
                }
            }

            // Commit transaction
            db.executeSet("COMMIT");
            return true;

        } catch (Exception e) {
            System.err.println("Sale failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return false;
        }
    }

    public boolean deleteSale(long saleId) {
        try {
            // Start transaction
            db.executeSet("BEGIN TRANSACTION");

            // 1. Get all sale items
            List<Map<String, Object>> items = db.executeGet(
                    "SELECT product_id, quantity FROM sale_items WHERE sale_id = ?",
                    saleId
            );

            // 2. Restore inventory for each item
            for (Map<String, Object> item : items) {
                int productId = ((Number) item.get("product_id")).intValue();
                double quantity = ((Number) item.get("quantity")).doubleValue();

                // Add back to inventory
                if (!updateInventory(productId, quantity)) {
                    throw new Exception("Failed to restore inventory for product: " + productId);
                }
            }

            // 3. Delete sale items
            if (!db.executeSet("DELETE FROM sale_items WHERE sale_id = ?", saleId)) {
                throw new Exception("Failed to delete sale items");
            }

            // 4. Delete sale record
            if (!db.executeSet("DELETE FROM sales WHERE id = ?", saleId)) {
                throw new Exception("Failed to delete sale record");
            }

            // Commit transaction
            db.executeSet("COMMIT");
            return true;

        } catch (Exception e) {
            System.err.println("Sale deletion failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return false;
        }
    }

    // Helper methods
    private boolean verifyProductAndStock(int productId, double requiredQuantity) {
        List<Map<String, Object>> result = db.executeGet(
                "SELECT quantity FROM inventory WHERE product_id = ?",
                productId
        );

        if (result.isEmpty()) {
            System.err.println("Product not found in inventory: " + productId);
            return false;
        }

        double currentStock = ((Number) result.get(0).get("quantity")).doubleValue();
        return currentStock >= requiredQuantity;
    }

    private boolean updateInventory(int productId, double quantityChange) {
        String query = "INSERT OR REPLACE INTO inventory (product_id, quantity, last_updated) " +
                "VALUES (?, COALESCE((SELECT quantity FROM inventory WHERE product_id = ?), 0) + ?, datetime('now', 'localtime'))";

        return db.executeSet(query, productId, productId, quantityChange);
    }
}