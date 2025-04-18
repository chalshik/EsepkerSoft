package com.bozzat.esepkersoft.Services;

import com.bozzat.esepkersoft.Models.Supplier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SupplierService {
    private final dbManager db = dbManager.getInstance();
    
   
    public Supplier addSupplier(Supplier supplier) {
        if (supplier == null || 
            supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            return null;
        }

        try {
            // Start transaction
            db.executeSet("BEGIN TRANSACTION");

            String query = "INSERT INTO suppliers (name, contact_info) VALUES (?, ?)";

            if (!db.executeSet(query,
                    supplier.getName().trim(),
                    supplier.getContactInfo() != null ? supplier.getContactInfo().trim() : "")) {
                throw new Exception("Failed to insert supplier");
            }

            // Get the generated supplier ID
            List<Map<String, Object>> result = db.executeGet("SELECT last_insert_rowid() as id");
            if (result.isEmpty()) {
                throw new Exception("Failed to retrieve supplier ID");
            }
            int supplierId = ((Number) result.get(0).get("id")).intValue();

            // Get the complete supplier data
            Supplier createdSupplier = getSupplierById(supplierId);
            if (createdSupplier == null) {
                throw new Exception("Failed to retrieve created supplier");
            }

            // Commit transaction
            db.executeSet("COMMIT");
            return createdSupplier;

        } catch (Exception e) {
            System.err.println("Supplier addition failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return null;
        }
    }
    

    public boolean updateSupplier(Supplier supplier) {
        if (supplier == null || supplier.getId() <= 0 || 
            supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            return false;
        }

        try {
            // Start transaction
            db.executeSet("BEGIN TRANSACTION");
            
            // Check if supplier exists
            Supplier existingSupplier = getSupplierById(supplier.getId());
            if (existingSupplier == null) {
                throw new Exception("Supplier not found");
            }

            String query = "UPDATE suppliers SET name = ?, contact_info = ? WHERE id = ?";

            if (!db.executeSet(query,
                    supplier.getName().trim(),
                    supplier.getContactInfo() != null ? supplier.getContactInfo().trim() : "",
                    supplier.getId())) {
                throw new Exception("Failed to update supplier");
            }
            
            // Commit transaction
            db.executeSet("COMMIT");
            return true;
            
        } catch (Exception e) {
            System.err.println("Supplier update failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return false;
        }
    }
    
    /**
     * Deletes a supplier if not used in any stock entries
     * @param supplierId The ID of the supplier to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteSupplier(int supplierId) {
        if (supplierId <= 0) {
            return false;
        }

        try {
            // Start transaction
            db.executeSet("BEGIN TRANSACTION");
            
            // Check if supplier exists
            Supplier existingSupplier = getSupplierById(supplierId);
            if (existingSupplier == null) {
                throw new Exception("Supplier not found");
            }
            
            // Check if supplier is used in stock entries
            List<Map<String, Object>> usageCheck = db.executeGet(
                    "SELECT COUNT(*) as count FROM stock_entries WHERE supplier_id = ?", 
                    supplierId
            );
            
            if (!usageCheck.isEmpty() && ((Number) usageCheck.get(0).get("count")).intValue() > 0) {
                throw new Exception("Cannot delete supplier that is associated with stock entries");
            }

            if (!db.executeSet("DELETE FROM suppliers WHERE id = ?", supplierId)) {
                throw new Exception("Failed to delete supplier");
            }
            
            // Commit transaction
            db.executeSet("COMMIT");
            return true;
            
        } catch (Exception e) {
            System.err.println("Supplier deletion failed: " + e.getMessage());
            db.executeSet("ROLLBACK");
            return false;
        }
    }
    
    /**
     * Retrieves a supplier by ID
     * @param supplierId The ID of the supplier to retrieve
     * @return The supplier object or null if not found
     */
    public Supplier getSupplierById(int supplierId) {
        if (supplierId <= 0) {
            return null;
        }

        String query = "SELECT * FROM suppliers WHERE id = ?";
        List<Map<String, Object>> results = db.executeGet(query, supplierId);

        if (results.isEmpty()) {
            return null;
        }

        return mapToSupplier(results.get(0));
    }
    
    /**
     * Retrieves all suppliers
     * @return List of all suppliers ordered by name
     */
    public List<Supplier> getAllSuppliers() {
        String query = "SELECT * FROM suppliers ORDER BY name";
        List<Map<String, Object>> results = db.executeGet(query);
        
        List<Supplier> suppliers = new ArrayList<>();
        for (Map<String, Object> row : results) {
            suppliers.add(mapToSupplier(row));
        }
        
        return suppliers;
    }
    
    /**
     * Searches for suppliers by name
     * @param searchTerm The search term to look for in supplier names
     * @return List of matching suppliers
     */
    public List<Supplier> searchSuppliersByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllSuppliers();
        }
        
        String query = "SELECT * FROM suppliers WHERE name LIKE ? ORDER BY name";
        List<Map<String, Object>> results = db.executeGet(query, "%" + searchTerm.trim() + "%");
        
        List<Supplier> suppliers = new ArrayList<>();
        for (Map<String, Object> row : results) {
            suppliers.add(mapToSupplier(row));
        }
        
        return suppliers;
    }
    
    /**
     * Helper method to map database row to Supplier object
     */
    private Supplier mapToSupplier(Map<String, Object> data) {
        Supplier supplier = new Supplier();
        supplier.setId(((Number) data.get("id")).intValue());
        supplier.setName((String) data.get("name"));
        supplier.setContactInfo((String) data.get("contact_info"));
        
        if (data.get("created_at") != null) {
            supplier.setCreatedAt(LocalDateTime.parse((String) data.get("created_at")));
        }
        
        return supplier;
    }
} 