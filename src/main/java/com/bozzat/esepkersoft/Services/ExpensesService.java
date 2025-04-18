package com.bozzat.esepkersoft.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExpensesService handles the management of expense categories and expense records.
 * This service provides functionality to track all non-inventory expenses
 * in the business with proper categorization.
 */
public class ExpensesService {
    private final dbManager db = dbManager.getInstance();
    
    /**
     * Creates a new expense category for classifying expenses.
     * 
     * Calculation method:
     * 1. Validates that the category name is not empty
     * 2. Checks if a category with the same name already exists (prevents duplicates)
     * 3. Starts a database transaction
     * 4. Inserts a new record into the expense_categories table
     * 5. Retrieves the newly generated ID using last_insert_rowid()
     * 6. Commits the transaction if successful, or rolls back on failure
     *
     * @param name The name of the expense category (required, must be unique)
     * @param description Optional description of the category
     * @return The ID of the newly created category if successful, or -1 if:
     *   - The name is null or empty
     *   - A category with the same name already exists
     *   - Database insertion fails
     */
    public int createExpenseCategory(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            return -1; // Invalid name
        }
        
        // Check if category with same name already exists
        String checkQuery = "SELECT id FROM expense_categories WHERE name = ?";
        List<Map<String, Object>> existingCategory = db.executeGet(checkQuery, name);
        
        if (!existingCategory.isEmpty()) {
            return -1; // Category already exists
        }
        
        // Start transaction
        db.executeSet("BEGIN TRANSACTION");
        
        try {
            // Insert new category
            String query = "INSERT INTO expense_categories (name, description, created_at) VALUES (?, ?, ?)";
            boolean success = db.executeSet(query, 
                    name, 
                    description != null ? description : "", 
                    LocalDateTime.now().toString());
            
            if (!success) {
                db.executeSet("ROLLBACK");
                return -1;
            }
            
            // Get the newly created category ID
            String idQuery = "SELECT last_insert_rowid() as id";
            List<Map<String, Object>> result = db.executeGet(idQuery);
            
            if (result.isEmpty()) {
                db.executeSet("ROLLBACK");
                return -1;
            }
            
            // Commit transaction
            db.executeSet("COMMIT");
            
            return ((Number) result.get(0).get("id")).intValue();
        } catch (Exception e) {
            db.executeSet("ROLLBACK");
            return -1;
        }
    }
    
    /**
     * Deletes an expense category by ID, if it's not in use.
     * 
     * Calculation method:
     * 1. Validates that the category ID is positive
     * 2. Checks if the category exists in the database
     * 3. Verifies the category isn't in use by any expense records
     * 4. Deletes the category if all conditions are met
     *
     * @param categoryId The ID of the category to delete
     * @return True if deletion was successful, false if:
     *   - The ID is invalid (≤ 0)
     *   - The category doesn't exist
     *   - The category is in use by one or more expense records
     *   - Database deletion fails
     */
    public boolean deleteExpenseCategory(int categoryId) {
        if (categoryId <= 0) {
            return false; // Invalid ID
        }
        
        // Check if category exists
        String checkQuery = "SELECT id FROM expense_categories WHERE id = ?";
        List<Map<String, Object>> existingCategory = db.executeGet(checkQuery, categoryId);
        
        if (existingCategory.isEmpty()) {
            return false; // Category doesn't exist
        }
        
        // Check if category is used in expenses
        String usageQuery = "SELECT COUNT(*) as count FROM expenses WHERE category_id = ?";
        List<Map<String, Object>> usageResult = db.executeGet(usageQuery, categoryId);
        
        if (!usageResult.isEmpty() && ((Number) usageResult.get(0).get("count")).intValue() > 0) {
            return false; // Category is in use, cannot delete
        }
        
        // Delete category
        String query = "DELETE FROM expense_categories WHERE id = ?";
        return db.executeSet(query, categoryId);
    }
    
    /**
     * Adds a new expense record with category classification.
     * 
     * Calculation method:
     * 1. Validates input parameters (amount must be positive, category must exist)
     * 2. Starts a database transaction
     * 3. Inserts a new record into the expenses table
     * 4. Retrieves the newly generated ID using last_insert_rowid()
     * 5. Commits the transaction if successful, or rolls back on failure
     *
     * @param amount The expense amount (must be positive)
     * @param categoryId The ID of the expense category (must exist)
     * @param description Optional description of the expense
     * @param expenseDate The date when the expense occurred (defaults to current date/time if null)
     * @return The ID of the newly created expense record if successful, or -1 if:
     *   - The amount is not positive
     *   - The category ID is invalid or doesn't exist
     *   - Database insertion fails
     */
    public int addExpense(double amount, int categoryId, String description, LocalDateTime expenseDate) {
        if (amount <= 0 || categoryId <= 0) {
            return -1; // Invalid input
        }
        
        // Check if category exists
        String checkQuery = "SELECT id FROM expense_categories WHERE id = ?";
        List<Map<String, Object>> existingCategory = db.executeGet(checkQuery, categoryId);
        
        if (existingCategory.isEmpty()) {
            return -1; // Category doesn't exist
        }
        
        // Use current date/time if not provided
        if (expenseDate == null) {
            expenseDate = LocalDateTime.now();
        }
        
        // Start transaction
        db.executeSet("BEGIN TRANSACTION");
        
        try {
            // Insert new expense
            String query = "INSERT INTO expenses (amount, category_id, description, expense_date, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
            
            boolean success = db.executeSet(query, 
                    amount, 
                    categoryId, 
                    description != null ? description : "", 
                    expenseDate.toString(),
                    LocalDateTime.now().toString());
            
            if (!success) {
                db.executeSet("ROLLBACK");
                return -1;
            }
            
            // Get the newly created expense ID
            String idQuery = "SELECT last_insert_rowid() as id";
            List<Map<String, Object>> result = db.executeGet(idQuery);
            
            if (result.isEmpty()) {
                db.executeSet("ROLLBACK");
                return -1;
            }
            
            // Commit transaction
            db.executeSet("COMMIT");
            
            return ((Number) result.get(0).get("id")).intValue();
        } catch (Exception e) {
            db.executeSet("ROLLBACK");
            return -1;
        }
    }
    
    /**
     * Deletes an expense record by ID.
     * 
     * Calculation method:
     * 1. Validates that the expense ID is positive
     * 2. Checks if the expense exists in the database
     * 3. Deletes the expense record if found
     *
     * @param expenseId The ID of the expense to delete
     * @return True if deletion was successful, false if:
     *   - The ID is invalid (≤ 0)
     *   - The expense doesn't exist
     *   - Database deletion fails
     */
    public boolean deleteExpense(int expenseId) {
        if (expenseId <= 0) {
            return false; // Invalid ID
        }
        
        // Check if expense exists
        String checkQuery = "SELECT id FROM expenses WHERE id = ?";
        List<Map<String, Object>> existingExpense = db.executeGet(checkQuery, expenseId);
        
        if (existingExpense.isEmpty()) {
            return false; // Expense doesn't exist
        }
        
        // Delete expense
        String query = "DELETE FROM expenses WHERE id = ?";
        return db.executeSet(query, expenseId);
    }
    
    /**
     * Retrieves a list of all expense categories.
     * 
     * Calculation method:
     * Executes a simple SELECT query on the expense_categories table,
     * ordering results alphabetically by name.
     *
     * @return List of Maps containing category data with the following fields:
     *   - id: The category ID
     *   - name: The category name
     *   - description: The category description
     *   - created_at: The date and time when the category was created
     */
    public List<Map<String, Object>> getAllExpenseCategories() {
        String query = "SELECT id, name, description, created_at FROM expense_categories ORDER BY name";
        return db.executeGet(query);
    }
    
    /**
     * Retrieves expenses within a specified date range, including category information.
     * 
     * Calculation method:
     * 1. Validates that the date range is valid
     * 2. Joins the expenses and expense_categories tables
     * 3. Filters by date range
     * 4. Orders results by expense date in descending order (newest first)
     * 5. Transforms database results into a consistent map structure
     *
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of Maps containing expense data with the following fields:
     *   - id: The expense ID
     *   - amount: The expense amount
     *   - expenseDate: The date of the expense
     *   - description: The expense description
     *   - createdAt: The date when the expense was recorded
     *   - categoryId: The ID of the expense category
     *   - categoryName: The name of the expense category
     */
    public List<Map<String, Object>> getExpenses(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new ArrayList<>(); // Return empty list for invalid date range
        }
        
        String query = "SELECT " +
                "e.id, " +
                "e.amount, " +
                "e.expense_date, " +
                "e.description, " +
                "e.created_at, " +
                "ec.id as category_id, " +
                "ec.name as category_name " +
                "FROM expenses e " +
                "JOIN expense_categories ec ON e.category_id = ec.id " +
                "WHERE e.expense_date BETWEEN ? AND ? " +
                "ORDER BY e.expense_date DESC";
        
        List<Map<String, Object>> results = db.executeGet(query, 
                startDate.toString(), 
                endDate.toString());
        
        List<Map<String, Object>> expenses = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> expense = new HashMap<>();
            
            expense.put("id", ((Number) row.get("id")).intValue());
            expense.put("amount", ((Number) row.get("amount")).doubleValue());
            expense.put("expenseDate", (String) row.get("expense_date"));
            expense.put("description", (String) row.get("description"));
            expense.put("createdAt", (String) row.get("created_at"));
            expense.put("categoryId", ((Number) row.get("category_id")).intValue());
            expense.put("categoryName", (String) row.get("category_name"));
            
            expenses.add(expense);
        }
        
        return expenses;
    }
    
    /**
     * Retrieves expenses filtered by both category and date range.
     * 
     * Calculation method:
     * 1. Validates that the category ID is positive and date range is valid
     * 2. Filters the expenses table by category ID and date range
     * 3. Orders results by expense date in descending order (newest first)
     * 4. Transforms database results into a consistent map structure
     *
     * @param categoryId The ID of the category to filter by
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of Maps containing expense data with the following fields:
     *   - id: The expense ID
     *   - amount: The expense amount
     *   - expenseDate: The date of the expense
     *   - description: The expense description
     *   - createdAt: The date when the expense was recorded
     *   - categoryId: The ID of the expense category
     */
    public List<Map<String, Object>> getExpensesByCategory(int categoryId, LocalDateTime startDate, LocalDateTime endDate) {
        if (categoryId <= 0 || startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new ArrayList<>(); // Return empty list for invalid input
        }
        
        String query = "SELECT " +
                "e.id, " +
                "e.amount, " +
                "e.expense_date, " +
                "e.description, " +
                "e.created_at " +
                "FROM expenses e " +
                "WHERE e.category_id = ? AND e.expense_date BETWEEN ? AND ? " +
                "ORDER BY e.expense_date DESC";
        
        List<Map<String, Object>> results = db.executeGet(query, 
                categoryId,
                startDate.toString(), 
                endDate.toString());
        
        List<Map<String, Object>> expenses = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> expense = new HashMap<>();
            
            expense.put("id", ((Number) row.get("id")).intValue());
            expense.put("amount", ((Number) row.get("amount")).doubleValue());
            expense.put("expenseDate", (String) row.get("expense_date"));
            expense.put("description", (String) row.get("description"));
            expense.put("createdAt", (String) row.get("created_at"));
            expense.put("categoryId", categoryId);
            
            expenses.add(expense);
        }
        
        return expenses;
    }
}
