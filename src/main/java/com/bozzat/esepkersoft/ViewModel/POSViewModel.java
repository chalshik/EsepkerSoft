package com.bozzat.esepkersoft.ViewModel;

import com.bozzat.esepkersoft.Models.Product;
import com.bozzat.esepkersoft.Models.Sale;
import com.bozzat.esepkersoft.Models.SaleItem;
import com.bozzat.esepkersoft.Services.ProductService;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Optional;

public class POSViewModel {
    // Constants
    private static final String DEFAULT_UNIT_TYPE = "шт";
    private static final String PRODUCT_NOT_FOUND_MESSAGE = "Продукт не найден";

    // Properties
    private final StringProperty barcode = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final DoubleProperty quantity = new SimpleDoubleProperty();
    private final StringProperty unitType = new SimpleStringProperty(DEFAULT_UNIT_TYPE);
    private final DoubleProperty total = new SimpleDoubleProperty();
    private final DoubleProperty totalOfTotals = new SimpleDoubleProperty();
    private final ObjectProperty<SaleItemViewModel> currentItem = new SimpleObjectProperty<>();
    private StringProperty paymentMethod = new SimpleStringProperty();

    // Collections
    private final ObservableList<SaleItemViewModel> saleItems;

    // Services
    private final ProductService productService;

    public POSViewModel(ProductService productService) {
        this.saleItems = FXCollections.observableArrayList(
                item ->
                        new Observable[] { item.totalProperty() }
        );
        this.productService = productService;
        initializeBindings();
        setUpSelectionHandling();
        // Bind total = price * quantity
    }

    private void initializeBindings() {
        total.bind(price.multiply(quantity));

        //
        totalOfTotals.bind(Bindings.createDoubleBinding(() ->
                saleItems.stream()
                        .mapToDouble(SaleItemViewModel::getTotal)
                        .sum(),
                saleItems));
    }

    private void setUpSelectionHandling() {
        currentItem.addListener((obs, oldValue, newValue) -> {
            unbindFromItem(oldValue);
            bindToItem(newValue);
            if (newValue == null) {
                resetFields();
                System.out.println("yes im here");
            }
        });
    }
    public void processBarcodeInput(String barcode) {
        Product product = productService.getProductByBarcode(barcode);
        if (product == null) {
            currentItem.set(null);
            handleBarcodeNotFound();
            return;
        }

        findExistingItem(product.getBarcode()).ifPresentOrElse(
                this::incrementItemQuantity,
                () -> addNewItem(product)
        );
    }

    public Optional<SaleItemViewModel> findExistingItem(String barcode) {
        return saleItems.stream()
                .filter(item -> item.getBarcode().equals(barcode))
                .findFirst();
    }

    private void addNewItem(Product product) {
        SaleItemViewModel saleItem = new SaleItemViewModel(
                product.getId(),
                product.getBarcode(),
                product.getName(),
                product.getCurrentPrice(),
                1.0,
                product.getUnitType());
        saleItems.add(saleItem);
        currentItem.set(saleItem);
    }

    private void unbindFromItem(SaleItemViewModel item) {
        if (item != null) {
            barcode.unbindBidirectional(item.barcodeProperty());
            name.unbindBidirectional(item.nameProperty());
            quantity.unbindBidirectional(item.quantityProperty());
            unitType.unbindBidirectional(item.unitTypeProperty());
            price.unbindBidirectional(item.priceProperty());
        }
    }

    private void bindToItem(SaleItemViewModel item) {
        if (item != null ) {
            barcode.bindBidirectional(item.barcodeProperty());
            name.bindBidirectional(item.nameProperty());
            quantity.bindBidirectional(item.quantityProperty());
            unitType.bindBidirectional(item.unitTypeProperty());
            price.bindBidirectional(item.priceProperty());
        }
    }

    public void resetFields() {
        barcode.set(null);
        name.set("");
        quantity.set(0);
        unitType.set(DEFAULT_UNIT_TYPE);
        price.set(0);
    }

    private void handleBarcodeNotFound() {
        resetFields();
        name.set(PRODUCT_NOT_FOUND_MESSAGE);
    }

    private void incrementItemQuantity(SaleItemViewModel item) {
        currentItem.set(item);
        item.setQuantity(item.getQuantity() + 1);
    }


    public void deleteCurrentItem() {
        if (currentItem.get() != null) {
            saleItems.remove(currentItem.get());
            currentItem.set(null);
        }
    }

    public double calculateChange(double rcvAmount) {
        return rcvAmount - totalOfTotals.get();
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod.set(paymentMethod);
    }


    // Property getters
    public ObservableList<SaleItemViewModel> getSaleItems() { return saleItems; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public DoubleProperty quantityProperty() { return quantity; }
    public StringProperty unitTypeProperty() { return unitType; }
    public DoubleProperty totalProperty() { return total; }
    public DoubleProperty totalOfTotalsProperty() { return totalOfTotals;}
    public ObjectProperty<SaleItemViewModel> currentItemProperty() { return currentItem; }
    public void setCurrentItem(SaleItemViewModel newItem) { currentItem.set(newItem);}
    public void setQuantity(double delta) { quantity.set(delta); }
    public Double getQuantity() { return quantity.get(); }
    public StringProperty paymentMethodProperty() { return paymentMethod; }
    public String getPaymentMethod() {return paymentMethod.get(); }
}
