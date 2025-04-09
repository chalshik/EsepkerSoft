package com.bozzat.esepkersoft.Services;

import com.bozzat.esepkersoft.Models.Movement;
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
                if (!verifyProductAndStock(item.getProductId(), item.getQuantity())) {
                    throw new Exception("Insufficient stock for product ID: " + item.getProductId());
                }

                // 4. Record inventory movement (outbound)
                Movement movement = new Movement(
                        0,
                        item.getProductId(),
                        "sale",
                        -item.getQuantity(), // Negative for outbound
                        item.getPrice(),
                        "SALE-" + saleId,
                        null,
                        null
                );

                if (!recordMovement(movement)) {
                    throw new Exception("Failed to record movement for product ID: " + item.getProductId());
                }

                // Get the generated movement ID
                result = db.executeGet("SELECT last_insert_rowid() as id");
                if (result.isEmpty()) {
                    throw new Exception("Failed to retrieve movement ID");
                }
                long movementId = ((Number) result.get(0).get("id")).longValue();

                // 5. Update inventory
                if (!updateInventory(item.getProductId(), -item.getQuantity())) {
                    throw new Exception("Failed to update inventory for product ID: " + item.getProductId());
                }

                // 6. Insert sale item with movement reference
                String insertItemQuery = "INSERT INTO sale_items " +
                        "(sale_id, product_id, quantity, price, movement_id) " +
                        "VALUES (?, ?, ?, ?, ?)";

                if (!db.executeSet(insertItemQuery,
                        saleId,
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        movementId)) {
                    throw new Exception("Failed to insert sale item");
                }
            }

            // Commit transaction if all operations succeeded
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

            // 1. Get all sale items with their movement IDs
            List<Map<String, Object>> items = db.executeGet(
                    "SELECT product_id, quantity, movement_id FROM sale_items WHERE sale_id = ?",
                    saleId
            );

            // 2. Reverse each movement and update inventory
            for (Map<String, Object> item : items) {
                int productId = ((Number) item.get("product_id")).intValue();
                double quantity = ((Number) item.get("quantity")).doubleValue();
                long movementId = ((Number) item.get("movement_id")).longValue();

                // Create return movement
                Movement returnMovement = new Movement(
                        0,
                        productId,
                        "return",
                        quantity, // Positive for return
                        null, // Original price not needed for returns
                        "RETURN-SALE-" + saleId,
                        null,
                        "Sale cancellation"
                );

                if (!recordMovement(returnMovement)) {
                    throw new Exception("Failed to record return movement");
                }

                // Update inventory
                if (!updateInventory(productId, quantity)) {
                    throw new Exception("Failed to update inventory for return");
                }

                // Delete the original movement
                if (!db.executeSet("DELETE FROM movements WHERE id = ?", movementId)) {
                    throw new Exception("Failed to delete original movement");
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
    private boolean verifyProductAndStock(long productId, double requiredQuantity) {
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

    private boolean updateInventory(long productId, double quantityChange) {
        // Update or insert inventory record
        String query = "INSERT OR REPLACE INTO inventory (product_id, quantity, last_updated) " +
                "VALUES (?, COALESCE((SELECT quantity FROM inventory WHERE product_id = ?), 0) + ?, datetime('now', 'localtime'))";

        return db.executeSet(query, productId, productId, quantityChange);
    }

    private boolean recordMovement(Movement movement) {
        String query = "INSERT INTO movements " +
                "(product_id, type, quantity, price_per_unit, reference_id, notes, movement_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))";

        return db.executeSet(query,
                movement.getProductId(),
                movement.getType(),
                movement.getQuantity(),
                movement.getPricePerUnit(),
                movement.getReferenceId(),
                movement.getNotes()
        );
    }
}