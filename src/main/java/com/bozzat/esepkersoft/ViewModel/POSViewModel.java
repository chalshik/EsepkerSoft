package com.bozzat.esepkersoft.ViewModel;

import com.bozzat.esepkersoft.Models.Product;
import com.bozzat.esepkersoft.Models.Sale;
import com.bozzat.esepkersoft.Services.ProductService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

public class POSViewModel {
    private StringProperty barcode;
    private StringProperty name;
    private DoubleProperty price;
    private DoubleProperty quantity;
    private StringProperty unitType;
    private DoubleProperty total;
    private ObservableList<SaleItemViewModel> saleItemViewModels;
    private ProductService productService = new ProductService();
    private ObjectProperty<SaleItemViewModel> currentItem = new SimpleObjectProperty<>();
    private DoubleProperty totalOfTotals = new SimpleDoubleProperty();
    public POSViewModel(ObservableList<SaleItemViewModel> saleItemViewModels) {
        barcode = new SimpleStringProperty();
        name = new SimpleStringProperty();
        price = new SimpleDoubleProperty();
        quantity = new SimpleDoubleProperty();
        unitType = new SimpleStringProperty("шт");
        total = new SimpleDoubleProperty();

        this.saleItemViewModels = saleItemViewModels;

        // Bind total = price * quantity
        total.bind(price.multiply(quantity));

        //
        totalOfTotals.bind(Bindings.createDoubleBinding(() -> {
            return saleItemViewModels.stream().map(SaleItemViewModel::getTotal).mapToDouble(Double::doubleValue).sum();
        }, saleItemViewModels));
    }

    public Integer handleBarcode(String barcode) {
        Product saleItem = productService.getProductByBarcode(barcode);
        if (saleItem == null) {
            productNotFound();
            return -1;
        }
        for (SaleItemViewModel item : saleItemViewModels) {
            if (item.getBarcode().equals(saleItem.getBarcode())) {
                item.setQuantity(item.getQuantity() + 1);
                return saleItemViewModels.indexOf(item);
            }
        }
        saleItemViewModels.add(new SaleItemViewModel(saleItem.getBarcode(), saleItem.getName(), saleItem.getCurrentPrice(), 1.0, saleItem.getUnitType()));
        int index = saleItemViewModels.size() - 1;
        return index;
    }

    public void getSelectedRow(SaleItemViewModel oldItem, SaleItemViewModel newItem) {
        if (oldItem != null) {
            // Unbind from old selection
            unbindOldItem();
        }
        if (newItem != null) {
            barcode.bindBidirectional(newItem.barcodeProperty());
            name.bindBidirectional(newItem.nameProperty());
            quantity.bindBidirectional(newItem.quantityProperty());
            unitType.bindBidirectional(newItem.unitTypeProperty());
            price.bindBidirectional(newItem.priceProperty());
            currentItem.set(newItem);
        } else if (oldItem != null && newItem == null){
            cartTableIsEmpty();
        } else {
            productNotFound();
        }
    }

    private void productNotFound() {
        unbindOldItem();
        barcode.set(null);
        name.set("Продукт не найден");
        quantity.set(0);
        unitType.set("шт");
        price.set(0);
    }

    private void cartTableIsEmpty() {
        unbindOldItem();
        barcode.set(null);
        name.set("");
        quantity.set(0);
        unitType.set("шт");
        price.set(0);
    }

    public void deleteItem() {

        SaleItemViewModel currentItem = this.currentItem.get();
        if (currentItem != null) {
            saleItemViewModels.remove(currentItem);
            System.out.println(currentItem.getName());
        }
    }
    public Double calculateChange(Double rcvAmount) {
        return totalOfTotals.get() - rcvAmount;
    }

    private void unbindOldItem() {
        if (this.currentItem.get() != null) {
            SaleItemViewModel currentItem = this.currentItem.get();
            barcode.unbindBidirectional(currentItem.barcodeProperty());
            name.unbindBidirectional(currentItem.nameProperty());
            quantity.unbindBidirectional(currentItem.quantityProperty());
            unitType.unbindBidirectional(currentItem.unitTypeProperty());
            price.unbindBidirectional(currentItem.priceProperty());
        }
    }

    // Property getters
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public DoubleProperty quantityProperty() { return quantity; }
    public StringProperty unitTypeProperty() { return unitType; }
    public DoubleProperty totalProperty() { return total; }
    public DoubleProperty totalOfTotalsProperty() { return totalOfTotals;}
}
