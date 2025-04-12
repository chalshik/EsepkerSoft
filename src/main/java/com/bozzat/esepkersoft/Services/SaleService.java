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
            String insertSaleQuery = "INSERT INTO sales (payment_method, total, date) VALUES (?, ?, datetime('now', 'localtime'))";
            if (!db.executeSet(insertSaleQuery, sale.getPaymentMethod(), sale.getTotal())) {
                throw new Exception("Failed to insert sale record");
            }

            // 2. Get the generated sale ID
            List<Map<String, Object>> result = db.executeGet("SELECT last_insert_rowid() as id");
            if (result.isEmpty()) {
                throw new Exception("Failed to retrieve sale ID");
            }
            long saleId = ((Number) result.get(0).get("id")).longValue();

            // 3. Process each sale item
            for (SaleItem item : saleItems) {
                // Verify product exists and has sufficient stock
                if (!verifyProductAndStock(item.getBarcode(), item.getQuantity())) {
                    throw new Exception("Insufficient stock for barcode: " + item.getBarcode());
                }

                // 4. Update inventory (direct deduction)
                if (!updateInventory(item.getBarcode(), -item.getQuantity())) {
                    throw new Exception("Failed to update inventory for barcode: " + item.getBarcode());
                }

                // 5. Insert sale item
                String insertItemQuery = "INSERT INTO sale_items " +
                        "(sale_id, barcode, quantity, price) " +
                        "VALUES (?, ?, ?, ?)";

                if (!db.executeSet(insertItemQuery,
                        saleId,
                        item.getBarcode(),
                        item.getQuantity(),
                        item.getPrice())) {
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
                    "SELECT barcode, quantity FROM sale_items WHERE sale_id = ?",
                    saleId
            );

            // 2. Restore inventory for each item
            for (Map<String, Object> item : items) {
                String barcode = (String) item.get("barcode");
                double quantity = ((Number) item.get("quantity")).doubleValue();

                // Add back to inventory
                if (!updateInventory(barcode, quantity)) {
                    throw new Exception("Failed to restore inventory for barcode: " + barcode);
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
    private boolean verifyProductAndStock(String barcode, double requiredQuantity) {
        List<Map<String, Object>> result = db.executeGet(
                "SELECT quantity FROM inventory WHERE barcode = ?",
                barcode
        );

        if (result.isEmpty()) {
            System.err.println("Product not found in inventory: " + barcode);
            return false;
        }

        double currentStock = ((Number) result.get(0).get("quantity")).doubleValue();
        return currentStock >= requiredQuantity;
    }

    private boolean updateInventory(String barcode, double quantityChange) {
        String query = "INSERT OR REPLACE INTO inventory (barcode, quantity, last_updated) " +
                "VALUES (?, COALESCE((SELECT quantity FROM inventory WHERE barcode = ?), 0) + ?, datetime('now', 'localtime'))";

        return db.executeSet(query, barcode, barcode, quantityChange);
    }
}