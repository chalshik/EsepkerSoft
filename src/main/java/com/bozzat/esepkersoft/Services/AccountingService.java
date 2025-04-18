package com.bozzat.esepkersoft.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AccountingService provides comprehensive financial tracking and reporting capabilities.
 * This service combines data from sales, purchases, and expenses to give a complete
 * picture of money flow throughout the business.
 */
public class AccountingService {
    private final dbManager db = dbManager.getInstance();

    /**
     * Retrieves a chronological money flow history combining all financial transactions
     * (sales, stock entries, and expenses) within a specified date range.
     * 
     * Calculation method:
     * 1. Collects all sales transactions (positive cash flow)
     * 2. Collects all stock purchase transactions (negative cash flow)
     * 3. Collects all expense transactions (negative cash flow)
     * 4. Combines all transactions and sorts them chronologically by transaction date
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of Maps containing transaction data with the following fields:
     *   - id: Unique identifier with prefix (e.g., "SALE-123", "STOCK-45", "EXP-67")
     *   - transactionDate: Date and time of the transaction
     *   - type: Transaction type ("SALE", "STOCK_ENTRY", or "EXPENSE")
     *   - description: Human-readable description of the transaction
     *   - amount: Transaction amount (positive for income, negative for expenses)
     *   - flowType: Cash flow direction ("INCOME" or "EXPENSE")
     *   - Additional fields depending on transaction type:
     *     - Sales: paymentMethod, note
     *     - Stock entries: unitPrice, quantity, note
     *     - Expenses: category, note
     */
    public List<Map<String, Object>> getMoneyFlowHistory(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new ArrayList<>(); // Return empty list for invalid date range
        }
        
        List<Map<String, Object>> cashFlow = new ArrayList<>();
        
        // Get sales (income)
        cashFlow.addAll(getSalesTransactions(startDate, endDate));
        
        // Get stock entries (expense)
        cashFlow.addAll(getStockEntryTransactions(startDate, endDate));
        
        // Get other expenses
        cashFlow.addAll(getExpenseTransactions(startDate, endDate));
        
        // Sort all transactions by date
        cashFlow.sort((a, b) -> {
            LocalDateTime dateA = LocalDateTime.parse((String) a.get("transactionDate"));
            LocalDateTime dateB = LocalDateTime.parse((String) b.get("transactionDate"));
            return dateA.compareTo(dateB);
        });
        
        return cashFlow;
    }
    
    /**
     * Helper method that retrieves all sales transactions within a date range.
     * 
     * Calculation method:
     * 1. Joins the sales, sales_items, and products tables
     * 2. Filters by date range
     * 3. Groups by sale ID to consolidate multiple items per sale
     * 4. Uses GROUP_CONCAT to combine product names and quantities
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of Maps containing sales transactions with formatted descriptions
     */
    private List<Map<String, Object>> getSalesTransactions(LocalDateTime startDate, LocalDateTime endDate) {
        String query = "SELECT " +
                "s.id, " +
                "s.sale_time as transaction_date, " +
                "s.total_amount, " +
                "s.payment_method, " +
                "s.comment, " +
                "GROUP_CONCAT(p.name || ' x' || si.quantity, ', ') as items_sold " +
                "FROM sales s " +
                "JOIN sales_items si ON s.id = si.sale_id " +
                "JOIN products p ON si.product_id = p.id " +
                "WHERE s.sale_time BETWEEN ? AND ? " +
                "GROUP BY s.id " +
                "ORDER BY s.sale_time";
        
        List<Map<String, Object>> results = db.executeGet(query, 
                startDate.toString(), 
                endDate.toString());
        
        List<Map<String, Object>> salesTransactions = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> transaction = new HashMap<>();
            
            transaction.put("id", "SALE-" + ((Number) row.get("id")).intValue());
            transaction.put("transactionDate", (String) row.get("transaction_date"));
            transaction.put("type", "SALE");
            transaction.put("description", "Sale: " + row.get("items_sold"));
            transaction.put("amount", ((Number) row.get("total_amount")).doubleValue());
            transaction.put("paymentMethod", (String) row.get("payment_method"));
            transaction.put("note", row.get("comment"));
            transaction.put("flowType", "INCOME");
            
            salesTransactions.add(transaction);
        }
        
        return salesTransactions;
    }
    
    /**
     * Helper method that retrieves all stock entry (inventory purchase) transactions
     * within a date range.
     * 
     * Calculation method:
     * 1. Joins the stock_entries, products, and suppliers tables
     * 2. Filters by date range
     * 3. Calculates total cost per entry (quantity * purchase_price)
     * 4. Formats description with product name, quantity, and supplier
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of Maps containing stock entry transactions as expenses
     */
    private List<Map<String, Object>> getStockEntryTransactions(LocalDateTime startDate, LocalDateTime endDate) {
        String query = "SELECT " +
                "se.id, " +
                "se.arrival_date as transaction_date, " +
                "p.name as product_name, " +
                "se.quantity, " +
                "se.purchase_price, " +
                "(se.quantity * se.purchase_price) as total_cost, " +
                "s.name as supplier_name, " +
                "se.note " +
                "FROM stock_entries se " +
                "JOIN products p ON se.product_id = p.id " +
                "LEFT JOIN suppliers s ON se.supplier_id = s.id " +
                "WHERE se.arrival_date BETWEEN ? AND ? " +
                "ORDER BY se.arrival_date";
        
        List<Map<String, Object>> results = db.executeGet(query, 
                startDate.toString(), 
                endDate.toString());
        
        List<Map<String, Object>> stockTransactions = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> transaction = new HashMap<>();
            
            double quantity = ((Number) row.get("quantity")).doubleValue();
            double unitPrice = ((Number) row.get("purchase_price")).doubleValue();
            double totalCost = ((Number) row.get("total_cost")).doubleValue();
            String productName = (String) row.get("product_name");
            String supplierInfo = row.get("supplier_name") != null ? 
                    " from " + (String) row.get("supplier_name") : "";
            
            transaction.put("id", "STOCK-" + ((Number) row.get("id")).intValue());
            transaction.put("transactionDate", (String) row.get("transaction_date"));
            transaction.put("type", "STOCK_ENTRY");
            transaction.put("description", "Purchase: " + productName + " x" + quantity + supplierInfo);
            transaction.put("amount", -totalCost); // Negative as it's an expense
            transaction.put("unitPrice", unitPrice);
            transaction.put("quantity", quantity);
            transaction.put("note", row.get("note"));
            transaction.put("flowType", "EXPENSE");
            
            stockTransactions.add(transaction);
        }
        
        return stockTransactions;
    }
    
    /**
     * Helper method that retrieves all general expense transactions within a date range.
     * 
     * Calculation method:
     * 1. Joins the expenses and expense_categories tables
     * 2. Filters by date range
     * 3. Formats description with category name and expense description
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of Maps containing expense transactions
     */
    private List<Map<String, Object>> getExpenseTransactions(LocalDateTime startDate, LocalDateTime endDate) {
        String query = "SELECT " +
                "e.id, " +
                "e.expense_date as transaction_date, " +
                "e.amount, " +
                "ec.name as category_name, " +
                "e.description " +
                "FROM expenses e " +
                "JOIN expense_categories ec ON e.category_id = ec.id " +
                "WHERE e.expense_date BETWEEN ? AND ? " +
                "ORDER BY e.expense_date";
        
        List<Map<String, Object>> results = db.executeGet(query, 
                startDate.toString(), 
                endDate.toString());
        
        List<Map<String, Object>> expenseTransactions = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> transaction = new HashMap<>();
            
            double amount = ((Number) row.get("amount")).doubleValue();
            String categoryName = (String) row.get("category_name");
            String description = row.get("description") != null ? 
                    (String) row.get("description") : "";
            
            transaction.put("id", "EXP-" + ((Number) row.get("id")).intValue());
            transaction.put("transactionDate", (String) row.get("transaction_date"));
            transaction.put("type", "EXPENSE");
            transaction.put("description", categoryName + ": " + description);
            transaction.put("amount", -amount); // Negative as it's an expense
            transaction.put("category", categoryName);
            transaction.put("note", description);
            transaction.put("flowType", "EXPENSE");
            
            expenseTransactions.add(transaction);
        }
        
        return expenseTransactions;
    }
    
    /**
     * Provides a financial summary of cash flow within a specified date range,
     * broken down by type (sales, purchases, expenses).
     * 
     * Calculation method:
     * 1. Calculates total sales (income) from the sales table
     * 2. Calculates total inventory purchases from the stock_entries table
     * 3. Calculates total operational expenses from the expenses table
     * 4. Computes total inflow (sales)
     * 5. Computes total outflow (purchases + expenses)
     * 6. Calculates net cash flow (inflow - outflow)
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return Map containing the following financial summary metrics:
     *   - startDate: Beginning of the period
     *   - endDate: End of the period
     *   - totalSales: Total sales revenue
     *   - totalPurchases: Total inventory purchase costs
     *   - totalExpenses: Total operational expenses
     *   - totalInflow: Total money coming in (sales)
     *   - totalOutflow: Total money going out (purchases + expenses)
     *   - netCashFlow: Total cash flow (inflow - outflow)
     */
    public Map<String, Object> getCashFlowSummary(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new HashMap<>(); // Return empty map for invalid date range
        }
        
        Map<String, Object> summary = new HashMap<>();
        
        // Get total sales
        String salesQuery = "SELECT SUM(total_amount) as total_sales " +
                "FROM sales " +
                "WHERE sale_time BETWEEN ? AND ?";
        
        List<Map<String, Object>> salesResult = db.executeGet(salesQuery, 
                startDate.toString(), 
                endDate.toString());
        
        double totalSales = 0;
        if (!salesResult.isEmpty() && salesResult.get(0).get("total_sales") != null) {
            totalSales = ((Number) salesResult.get(0).get("total_sales")).doubleValue();
        }
        
        // Get total stock purchases
        String purchasesQuery = "SELECT SUM(quantity * purchase_price) as total_purchases " +
                "FROM stock_entries " +
                "WHERE arrival_date BETWEEN ? AND ?";
        
        List<Map<String, Object>> purchasesResult = db.executeGet(purchasesQuery, 
                startDate.toString(), 
                endDate.toString());
        
        double totalPurchases = 0;
        if (!purchasesResult.isEmpty() && purchasesResult.get(0).get("total_purchases") != null) {
            totalPurchases = ((Number) purchasesResult.get(0).get("total_purchases")).doubleValue();
        }
        
        // Get total expenses
        String expensesQuery = "SELECT SUM(amount) as total_expenses " +
                "FROM expenses " +
                "WHERE expense_date BETWEEN ? AND ?";
        
        List<Map<String, Object>> expensesResult = db.executeGet(expensesQuery, 
                startDate.toString(), 
                endDate.toString());
        
        double totalExpenses = 0;
        if (!expensesResult.isEmpty() && expensesResult.get(0).get("total_expenses") != null) {
            totalExpenses = ((Number) expensesResult.get(0).get("total_expenses")).doubleValue();
        }
        
        // Calculate net cash flow
        double totalInflow = totalSales;
        double totalOutflow = totalPurchases + totalExpenses;
        double netCashFlow = totalInflow - totalOutflow;
        
        // Build summary
        summary.put("startDate", startDate.toString());
        summary.put("endDate", endDate.toString());
        summary.put("totalSales", totalSales);
        summary.put("totalPurchases", totalPurchases);
        summary.put("totalExpenses", totalExpenses);
        summary.put("totalInflow", totalInflow);
        summary.put("totalOutflow", totalOutflow);
        summary.put("netCashFlow", netCashFlow);
        
        return summary;
    }
    
    /**
     * Get cash flow balance by date within the specified range,
     * showing the running balance after each transaction date.
     * 
     * Calculation method:
     * 1. Retrieves all transactions in chronological order using getMoneyFlowHistory()
     * 2. Groups transactions by date (ignoring time component)
     * 3. Calculates a running balance by summing all transaction amounts
     * 4. For each date, stores the final balance value after all transactions
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of Maps containing:
     *   - date: The transaction date (YYYY-MM-DD format)
     *   - balance: The running cash balance after all transactions on that date
     *   
     * The list is sorted by date in ascending order, showing how cash balance
     * evolves over time.
     */
    public List<Map<String, Object>> getCashFlowBalanceByDate(LocalDateTime startDate, LocalDateTime endDate) {
        // Get all transactions in chronological order
        List<Map<String, Object>> transactions = getMoneyFlowHistory(startDate, endDate);
        
        // Group transactions by date and calculate daily balance
        Map<String, Double> dailyBalances = new HashMap<>();
        double runningBalance = 0;
        
        for (Map<String, Object> transaction : transactions) {
            String transactionDate = ((String) transaction.get("transactionDate")).substring(0, 10); // Get just the date part
            double amount = ((Number) transaction.get("amount")).doubleValue();
            
            runningBalance += amount;
            
            // Update or set the balance for this date
            dailyBalances.put(transactionDate, runningBalance);
        }
        
        // Convert to list of maps for return
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : dailyBalances.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("balance", entry.getValue());
            result.add(item);
        }
        
        // Sort by date
        result.sort((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")));
        
        return result;
    }
}
