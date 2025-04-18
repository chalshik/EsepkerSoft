package com.bozzat.esepkersoft.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticService {
    private final dbManager db = dbManager.getInstance();
    
    /**
     * Retrieves the top 10 most sold products within a specified date range
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of Maps containing product info and sales data
     */
    public List<Map<String, Object>> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new ArrayList<>(); // Return empty list for invalid date range
        }
        
        String query = "SELECT " +
                "p.id, p.name, p.unit_type, p.current_price, " +
                "SUM(si.quantity) as total_quantity, " +
                "SUM(si.quantity * si.unit_price) as total_revenue " +
                "FROM products p " +
                "JOIN sales_items si ON p.id = si.product_id " +
                "JOIN sales s ON si.sale_id = s.id " +
                "WHERE s.sale_time BETWEEN ? AND ? " +
                "GROUP BY p.id " +
                "ORDER BY total_quantity DESC " +
                "LIMIT 10";
        
        List<Map<String, Object>> results = db.executeGet(query, 
                startDate.toString(), 
                endDate.toString());
        
        List<Map<String, Object>> topProducts = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> item = new HashMap<>();
            
            // Product info
            item.put("id", ((Number) row.get("id")).intValue());
            item.put("name", (String) row.get("name"));
            item.put("unitType", (String) row.get("unit_type"));
            item.put("currentPrice", ((Number) row.get("current_price")).doubleValue());
            
            // Sales data
            double totalQuantity = ((Number) row.get("total_quantity")).doubleValue();
            double totalRevenue = ((Number) row.get("total_revenue")).doubleValue();
            
            item.put("totalQuantity", totalQuantity);
            item.put("totalRevenue", totalRevenue);
            
            // Calculate average unit price during the period
            item.put("averageUnitPrice", totalQuantity > 0 ? totalRevenue / totalQuantity : 0);
            
            topProducts.add(item);
        }
        
        return topProducts;
    }
    
    /**
     * Retrieves the top 10 most profitable products within a specified date range
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of Maps containing product info and profit data
     */
    public List<Map<String, Object>> getMostProfitableProducts(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new ArrayList<>(); // Return empty list for invalid date range
        }
        
        String query = "SELECT " +
                "p.id, p.name, p.unit_type, p.current_price, " +
                "SUM(si.quantity) as total_quantity, " +
                "SUM(si.quantity * si.unit_price) as total_revenue, " +
                "AVG(se.purchase_price) as avg_purchase_price " +
                "FROM products p " +
                "JOIN sales_items si ON p.id = si.product_id " +
                "JOIN sales s ON si.sale_id = s.id " +
                "LEFT JOIN stock_entries se ON p.id = se.product_id " +
                "WHERE s.sale_time BETWEEN ? AND ? " +
                "GROUP BY p.id " +
                "ORDER BY (total_revenue - (total_quantity * avg_purchase_price)) DESC " +
                "LIMIT 10";
        
        List<Map<String, Object>> results = db.executeGet(query, 
                startDate.toString(), 
                endDate.toString());
        
        List<Map<String, Object>> profitableProducts = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> item = new HashMap<>();
            
            // Product info
            item.put("id", ((Number) row.get("id")).intValue());
            item.put("name", (String) row.get("name"));
            item.put("unitType", (String) row.get("unit_type"));
            item.put("currentPrice", ((Number) row.get("current_price")).doubleValue());
            
            // Sales and cost data
            double totalQuantity = ((Number) row.get("total_quantity")).doubleValue();
            double totalRevenue = ((Number) row.get("total_revenue")).doubleValue();
            double avgPurchasePrice = row.get("avg_purchase_price") != null ? 
                    ((Number) row.get("avg_purchase_price")).doubleValue() : 0;
            
            // Calculate profit
            double totalCost = totalQuantity * avgPurchasePrice;
            double totalProfit = totalRevenue - totalCost;
            double profitMargin = totalRevenue > 0 ? (totalProfit / totalRevenue) * 100 : 0;
            
            item.put("totalQuantity", totalQuantity);
            item.put("totalRevenue", totalRevenue);
            item.put("avgPurchasePrice", avgPurchasePrice);
            item.put("totalCost", totalCost);
            item.put("totalProfit", totalProfit);
            item.put("profitMargin", profitMargin);
            
            profitableProducts.add(item);
        }
        
        return profitableProducts;
    }
    
    /**
     * Retrieves slow-moving products that have very few sales within a specified date range
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @param maxSales The maximum number of sales to consider a product as slow-moving (default 3)
     * @return List of Maps containing product info and slow-moving metrics
     */
    public List<Map<String, Object>> getSlowMovingProducts(LocalDateTime startDate, LocalDateTime endDate, int maxSales) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new ArrayList<>(); // Return empty list for invalid date range
        }
        
        // First get products with stock
        String query = "SELECT " +
                "p.id, p.name, p.unit_type, p.current_price, " +
                "COALESCE(sb.quantity, 0) as current_stock, " +
                "COUNT(DISTINCT si.sale_id) as sale_count, " +
                "SUM(si.quantity) as total_quantity " +
                "FROM products p " +
                "LEFT JOIN stock_balances sb ON p.id = sb.product_id " +
                "LEFT JOIN sales_items si ON p.id = si.product_id " +
                "LEFT JOIN sales s ON si.sale_id = s.id AND s.sale_time BETWEEN ? AND ? " +
                "GROUP BY p.id " +
                "HAVING (sale_count <= ? OR sale_count IS NULL) AND current_stock > 0 " +
                "ORDER BY sale_count ASC, current_stock DESC";
        
        List<Map<String, Object>> results = db.executeGet(query, 
                startDate.toString(), 
                endDate.toString(),
                maxSales);
        
        List<Map<String, Object>> slowMovingProducts = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> item = new HashMap<>();
            
            // Product info
            item.put("id", ((Number) row.get("id")).intValue());
            item.put("name", (String) row.get("name"));
            item.put("unitType", (String) row.get("unit_type"));
            item.put("currentPrice", ((Number) row.get("current_price")).doubleValue());
            
            // Stock and sales data
            double currentStock = ((Number) row.get("current_stock")).doubleValue();
            int saleCount = row.get("sale_count") != null ? ((Number) row.get("sale_count")).intValue() : 0;
            double totalQuantity = row.get("total_quantity") != null ? 
                    ((Number) row.get("total_quantity")).doubleValue() : 0;
            
            item.put("currentStock", currentStock);
            item.put("saleCount", saleCount);
            item.put("totalQuantity", totalQuantity);
            
            // Calculate days of stock based on current sales rate
            long daysBetween = java.time.Duration.between(startDate, endDate).toDays();
            double dailySales = daysBetween > 0 ? totalQuantity / daysBetween : 0;
            double daysOfStock = dailySales > 0 ? currentStock / dailySales : Double.POSITIVE_INFINITY;
            
            item.put("daysOfStock", daysOfStock == Double.POSITIVE_INFINITY ? "∞" : Math.round(daysOfStock));
            
            slowMovingProducts.add(item);
        }
        
        return slowMovingProducts;
    }
    
    /**
     * Overloaded method that defaults max sales to 3
     */
    public List<Map<String, Object>> getSlowMovingProducts(LocalDateTime startDate, LocalDateTime endDate) {
        return getSlowMovingProducts(startDate, endDate, 3);
    }
    
    /**
     * Calculates overall business performance metrics within a specified date range
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return Map containing various financial metrics
     */
    public Map<String, Object> getBusinessMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new HashMap<>(); // Return empty map for invalid date range
        }
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Calculate total revenue
        String revenueQuery = "SELECT SUM(total_amount) as total_revenue " +
                "FROM sales " +
                "WHERE sale_time BETWEEN ? AND ?";
        
        List<Map<String, Object>> revenueResult = db.executeGet(revenueQuery, 
                startDate.toString(), 
                endDate.toString());
        
        double totalRevenue = 0;
        if (!revenueResult.isEmpty() && revenueResult.get(0).get("total_revenue") != null) {
            totalRevenue = ((Number) revenueResult.get(0).get("total_revenue")).doubleValue();
        }
        metrics.put("totalRevenue", totalRevenue);
        
        // Calculate total cost of goods sold
        String cogsQuery = "SELECT SUM(si.quantity * se.purchase_price) as total_cost " +
                "FROM sales s " +
                "JOIN sales_items si ON s.id = si.sale_id " +
                "JOIN products p ON si.product_id = p.id " +
                "LEFT JOIN (" +
                "    SELECT product_id, AVG(purchase_price) as purchase_price " +
                "    FROM stock_entries " +
                "    GROUP BY product_id" +
                ") se ON p.id = se.product_id " +
                "WHERE s.sale_time BETWEEN ? AND ?";
        
        List<Map<String, Object>> cogsResult = db.executeGet(cogsQuery, 
                startDate.toString(), 
                endDate.toString());
        
        double totalCost = 0;
        if (!cogsResult.isEmpty() && cogsResult.get(0).get("total_cost") != null) {
            totalCost = ((Number) cogsResult.get(0).get("total_cost")).doubleValue();
        }
        metrics.put("totalCostOfGoodsSold", totalCost);
        
        // Calculate gross profit and margin
        double grossProfit = totalRevenue - totalCost;
        double grossMargin = totalRevenue > 0 ? (grossProfit / totalRevenue) * 100 : 0;
        
        metrics.put("grossProfit", grossProfit);
        metrics.put("grossMarginPercentage", grossMargin);
        
        // Calculate total number of transactions
        String transactionQuery = "SELECT COUNT(*) as transaction_count " +
                "FROM sales " +
                "WHERE sale_time BETWEEN ? AND ?";
        
        List<Map<String, Object>> transactionResult = db.executeGet(transactionQuery, 
                startDate.toString(), 
                endDate.toString());
        
        int transactionCount = 0;
        if (!transactionResult.isEmpty()) {
            transactionCount = ((Number) transactionResult.get(0).get("transaction_count")).intValue();
        }
        metrics.put("transactionCount", transactionCount);
        
        // Calculate average transaction value
        double avgTransactionValue = transactionCount > 0 ? totalRevenue / transactionCount : 0;
        metrics.put("averageTransactionValue", avgTransactionValue);
        
        // Calculate total items sold
        String itemsQuery = "SELECT SUM(si.quantity) as items_sold " +
                "FROM sales s " +
                "JOIN sales_items si ON s.id = si.sale_id " +
                "WHERE s.sale_time BETWEEN ? AND ?";
        
        List<Map<String, Object>> itemsResult = db.executeGet(itemsQuery, 
                startDate.toString(), 
                endDate.toString());
        
        double itemsSold = 0;
        if (!itemsResult.isEmpty() && itemsResult.get(0).get("items_sold") != null) {
            itemsSold = ((Number) itemsResult.get(0).get("items_sold")).doubleValue();
        }
        metrics.put("totalItemsSold", itemsSold);
        
        // Calculate expenses
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
        metrics.put("totalExpenses", totalExpenses);
        
        // Calculate net profit
        double netProfit = grossProfit - totalExpenses;
        double netMargin = totalRevenue > 0 ? (netProfit / totalRevenue) * 100 : 0;
        
        metrics.put("netProfit", netProfit);
        metrics.put("netMarginPercentage", netMargin);
        
        // Add date range information
        metrics.put("startDate", startDate.toString());
        metrics.put("endDate", endDate.toString());
        long daysBetween = java.time.Duration.between(startDate, endDate).toDays();
        metrics.put("periodDays", daysBetween);
        
        return metrics;
    }
    
    /**
     * Get daily revenue breakdown within a specified date range
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of Maps containing daily revenue data
     */
    public List<Map<String, Object>> getDailyRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new ArrayList<>(); // Return empty list for invalid date range
        }
        
        String query = "SELECT " +
                "DATE(sale_time) as sale_date, " +
                "COUNT(*) as transaction_count, " +
                "SUM(total_amount) as daily_revenue " +
                "FROM sales " +
                "WHERE sale_time BETWEEN ? AND ? " +
                "GROUP BY DATE(sale_time) " +
                "ORDER BY sale_date";
        
        List<Map<String, Object>> results = db.executeGet(query, 
                startDate.toString(), 
                endDate.toString());
        
        List<Map<String, Object>> dailyRevenue = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> item = new HashMap<>();
            
            item.put("date", (String) row.get("sale_date"));
            item.put("transactionCount", ((Number) row.get("transaction_count")).intValue());
            item.put("revenue", ((Number) row.get("daily_revenue")).doubleValue());
            
            dailyRevenue.add(item);
        }
        
        return dailyRevenue;
    }
    
    /**
     * Get monthly revenue breakdown within a specified date range
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of Maps containing monthly revenue data
     */
    public List<Map<String, Object>> getMonthlyRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new ArrayList<>(); // Return empty list for invalid date range
        }
        
        String query = "SELECT " +
                "strftime('%Y-%m', sale_time) as month, " +
                "COUNT(*) as transaction_count, " +
                "SUM(total_amount) as monthly_revenue, " +
                "AVG(total_amount) as avg_transaction_value " +
                "FROM sales " +
                "WHERE sale_time BETWEEN ? AND ? " +
                "GROUP BY strftime('%Y-%m', sale_time) " +
                "ORDER BY month";
        
        List<Map<String, Object>> results = db.executeGet(query, 
                startDate.toString(), 
                endDate.toString());
        
        List<Map<String, Object>> monthlyRevenue = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> item = new HashMap<>();
            
            item.put("month", (String) row.get("month"));
            item.put("transactionCount", ((Number) row.get("transaction_count")).intValue());
            item.put("revenue", ((Number) row.get("monthly_revenue")).doubleValue());
            item.put("averageTransactionValue", ((Number) row.get("avg_transaction_value")).doubleValue());
            
            monthlyRevenue.add(item);
        }
        
        return monthlyRevenue;
    }
    
    /**
     * Get revenue breakdown by payment method within a specified date range
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of Maps containing payment method revenue data
     */
    public List<Map<String, Object>> getRevenueByPaymentMethod(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new ArrayList<>(); // Return empty list for invalid date range
        }
        
        String query = "SELECT " +
                "payment_method, " +
                "COUNT(*) as transaction_count, " +
                "SUM(total_amount) as total_revenue, " +
                "AVG(total_amount) as avg_transaction_value " +
                "FROM sales " +
                "WHERE sale_time BETWEEN ? AND ? " +
                "GROUP BY payment_method " +
                "ORDER BY total_revenue DESC";
        
        List<Map<String, Object>> results = db.executeGet(query, 
                startDate.toString(), 
                endDate.toString());
        
        List<Map<String, Object>> paymentMethodRevenue = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> item = new HashMap<>();
            
            item.put("paymentMethod", (String) row.get("payment_method"));
            item.put("transactionCount", ((Number) row.get("transaction_count")).intValue());
            item.put("revenue", ((Number) row.get("total_revenue")).doubleValue());
            item.put("averageTransactionValue", ((Number) row.get("avg_transaction_value")).doubleValue());
            
            // Calculate percentage of total
            double methodRevenue = ((Number) row.get("total_revenue")).doubleValue();
            double totalRevenue = results.stream()
                    .mapToDouble(r -> ((Number) r.get("total_revenue")).doubleValue())
                    .sum();
            double percentage = totalRevenue > 0 ? (methodRevenue / totalRevenue) * 100 : 0;
            
            item.put("percentageOfTotal", percentage);
            
            paymentMethodRevenue.add(item);
        }
        
        return paymentMethodRevenue;
    }
    public List<Map<String, Object>> getRevenueByCategory(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return new ArrayList<>(); // Return empty list for invalid date range
        }
        
        String query = "SELECT " +
                "c.name as category_name, " +
                "COUNT(DISTINCT s.id) as transaction_count, " +
                "SUM(si.quantity) as items_sold, " +
                "SUM(si.quantity * si.unit_price) as total_revenue " +
                "FROM sales s " +
                "JOIN sales_items si ON s.id = si.sale_id " +
                "JOIN products p ON si.product_id = p.id " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE s.sale_time BETWEEN ? AND ? " +
                "GROUP BY c.name " +
                "ORDER BY total_revenue DESC";
        
        List<Map<String, Object>> results = db.executeGet(query, 
                startDate.toString(), 
                endDate.toString());
        
        List<Map<String, Object>> categoryRevenue = new ArrayList<>();
        double totalRev = results.stream()
                .mapToDouble(r -> ((Number) r.get("total_revenue")).doubleValue())
                .sum();
        
        for (Map<String, Object> row : results) {
            Map<String, Object> item = new HashMap<>();
            
            // Use "Uncategorized" for null category name
            String catName = row.get("category_name") != null ? 
                    (String) row.get("category_name") : "Uncategorized";
            
            item.put("categoryName", catName);
            item.put("transactionCount", ((Number) row.get("transaction_count")).intValue());
            item.put("itemsSold", ((Number) row.get("items_sold")).doubleValue());
            item.put("revenue", ((Number) row.get("total_revenue")).doubleValue());
            
            // Calculate percentage of total
            double categoryRevenuePct = totalRev > 0 ? 
                    (((Number) row.get("total_revenue")).doubleValue() / totalRev) * 100 : 0;
            item.put("percentageOfTotal", categoryRevenuePct);
            
            categoryRevenue.add(item);
        }
        
        return categoryRevenue;
    }
}