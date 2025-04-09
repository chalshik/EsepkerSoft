package com.bozzat.esepkersoft.ViewModel;

import javafx.beans.property.*;

public class SaleItemViewModel {
    private final StringProperty barcode = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final DoubleProperty quantity = new SimpleDoubleProperty();
    private final StringProperty unitType = new SimpleStringProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();

    public SaleItemViewModel(String barcode, String name, double price, double quantity, String unitType) {
        this.barcode.set(barcode);
        this.name.set(name);
        this.price.set(price);
        this.quantity.set(quantity);
        this.unitType.set(unitType);
        // Bind show price to unit
        bindPriceAndTotal();
    }

    private void bindPriceAndTotal() {
        // Bind price based on the unit
        // Bind total to price * quantity
        total.bind(price.multiply(quantity));
    }

    // Getters for properties (needed for TableView)
    public StringProperty barcodeProperty() { return barcode; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public DoubleProperty quantityProperty() { return quantity; }
    public DoubleProperty totalProperty() { return total; }
    public StringProperty unitTypeProperty() { return unitType;}
    // Optional: Getters for values
    public String getBarcode() { return barcode.get(); }
    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public double getQuantity() { return quantity.get(); }
    public double getTotal() { return total.get(); }



    // Optional: Setters
    public void setBarcode(String barcode) { this.barcode.set(barcode); }
    public void setName(String name) { this.name.set(name); }
    public void setPrice(double price) { this.price.set(price); }
    public void setQuantity(double quantity) { this.quantity.set(quantity); }
}
