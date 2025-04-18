package com.bozzat.esepkersoft.Services;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service class for exporting JavaFX TableView data to CSV files.
 * Uses standard Java I/O for CSV file generation.
 */
public class ExcelService {
    
    /**
     * Exports data from a TableView to a CSV file
     * 
     * @param <T> The type of items in the TableView
     * @param tableView The TableView containing the data to export
     * @param fileName Optional custom file name (without extension)
     * @return File object of the created CSV file, or null if export failed
     */
    public <T> File exportTableViewToCSV(TableView<T> tableView, String fileName) {
        if (tableView == null || tableView.getItems().isEmpty()) {
            // No data to export
            return null;
        }
        
        // Create file name with timestamp if not provided
        String outputFileName = fileName;
        if (outputFileName == null || outputFileName.trim().isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            outputFileName = "export_" + timestamp;
        }
        
        // Ensure filename has the .csv extension
        if (!outputFileName.toLowerCase().endsWith(".csv")) {
            outputFileName += ".csv";
        }
        
        // Create the CSV file
        File csvFile = new File(outputFileName);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            // Get all columns
            List<TableColumn<T, ?>> columns = tableView.getColumns();
            
            // Write header row
            StringBuilder headerRow = new StringBuilder();
            for (int i = 0; i < columns.size(); i++) {
                String columnName = columns.get(i).getText();
                headerRow.append(escapeCSV(columnName));
                if (i < columns.size() - 1) {
                    headerRow.append(",");
                }
            }
            writer.write(headerRow.toString());
            writer.newLine();
            
            // Write data rows
            List<T> items = tableView.getItems();
            for (T item : items) {
                StringBuilder dataRow = new StringBuilder();
                for (int i = 0; i < columns.size(); i++) {
                    TableColumn<T, ?> column = columns.get(i);
                    Object cellData = column.getCellData(item);
                    dataRow.append(escapeCSV(cellData != null ? cellData.toString() : ""));
                    if (i < columns.size() - 1) {
                        dataRow.append(",");
                    }
                }
                writer.write(dataRow.toString());
                writer.newLine();
            }
            
            return csvFile;
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Overloaded method that generates a default filename
     */
    public <T> File exportTableViewToCSV(TableView<T> tableView) {
        return exportTableViewToCSV(tableView, null);
    }
    
    /**
     * Escapes a string for CSV format
     * If the string contains commas, quotes, or newlines, it will be enclosed in quotes
     * Any quotes in the string will be doubled
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        // Check if we need to escape this value
        boolean needsEscaping = value.contains("\"") || value.contains(",") || value.contains("\n");
        
        if (needsEscaping) {
            // Replace double quotes with two double quotes
            String escaped = value.replace("\"", "\"\"");
            // Enclose the value in quotes
            return "\"" + escaped + "\"";
        }
        
        return value;
    }
}
