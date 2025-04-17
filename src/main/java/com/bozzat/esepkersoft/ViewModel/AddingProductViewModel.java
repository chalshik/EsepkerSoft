package com.bozzat.esepkersoft.ViewModel;

import com.bozzat.esepkersoft.Models.Product;
import com.bozzat.esepkersoft.Models.StockEntry;
import com.bozzat.esepkersoft.Models.Supplier;
import com.bozzat.esepkersoft.Services.ProductService;
import com.bozzat.esepkersoft.Services.SupplierService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class AddingProductViewModel {
    private final ObservableList<String> productTypes = FXCollections.observableArrayList("kg", "pc");
    // Product information
    private final StringProperty selectedProductType = new SimpleStringProperty();
    private final StringProperty barcodeField = new SimpleStringProperty();
    private final StringProperty productName = new SimpleStringProperty();
    private final DoubleProperty retailPrice = new SimpleDoubleProperty();
    // Batch Information
    private final DoubleProperty batchQuantity = new SimpleDoubleProperty();
    private final DoubleProperty purchasePrice = new SimpleDoubleProperty();
    private final ObjectProperty<Supplier> selectedSupplier= new SimpleObjectProperty<>();
    private final ObjectProperty<BarcodeStatus> barcodeStatus = new SimpleObjectProperty<>();
    private final ObjectProperty<Product> currentProduct = new SimpleObjectProperty<>();

    // Editability properties
    private final BooleanProperty productNameEditable = new SimpleBooleanProperty(true);
    private final BooleanProperty productTypeSelectionDisabled = new SimpleBooleanProperty(false);
    private final BooleanProperty productRetailPriceEditable = new SimpleBooleanProperty(true);




    // Services
    SupplierService supplierService = new SupplierService();
    ProductService productService = new ProductService();
    private final ObservableList<Supplier> suppliers = FXCollections.observableList(supplierService.getAllSuppliers());

    public AddingProductViewModel() {
        initializeListeners();
    }

    private void initializeListeners() {
        barcodeStatus.addListener((obs, oldV, newV) -> {
            updateFields(newV);
        });
    }
    private void updateFields(BarcodeStatus barcodeStatus) {

    }


    public void searchProduct() {
        Product product = productService.getProductByBarcode(barcodeField.get());
        barcodeStatus.set(BarcodeStatus.INITIAL);
        if (product != null) {
            barcodeStatus.set(BarcodeStatus.FOUND);
            currentProduct.set(product);
            productName.set(product.getName());
            selectedProductType.set(product.getUnitType());
            retailPrice.set(product.getCurrentPrice());
            setEditableFalse();
        } else {
            barcodeStatus.set(BarcodeStatus.NEW);
            clearProductInformationFields();
            setEditableTrue();
            currentProduct.set(null);
        }
    }

    public void clearProductInformationFields() {
        selectedProductType.set("");
        productName.set("");
        retailPrice.set(0.0);
    }

    public void setEditableTrue() {
        productNameEditable.set(true);
        productTypeSelectionDisabled.set(false);
        productRetailPriceEditable.set(true);
    }


    public void setEditableFalse() {
        productNameEditable.set(false);
        productTypeSelectionDisabled.set(true);
        productRetailPriceEditable.set(false);
    }

    public ObjectProperty<BarcodeStatus> barcodeStatusProperty() {
        return barcodeStatus;
    }
    public ObjectProperty<Supplier> selectedSupplierProperty() {
        return selectedSupplier;
    }

    public ObservableList<Supplier> getSuppliers() {
        return suppliers;
    }
    public StringProperty barcodeFieldProperty() {
        return barcodeField;
    }

    public DoubleProperty purchasePriceProperty() {
        return purchasePrice;
    }

    public DoubleProperty retailPriceProperty() {
        return retailPrice;
    }

    public DoubleProperty batchQuantityProperty() {
        return batchQuantity;
    }

    public StringProperty productNameProperty() {
        return productName;
    }
    public StringProperty selectedProductTypeProperty() {
        return selectedProductType;
    }

    public ObservableList<String> getProductTypes() {
        return productTypes;
    }
    // Getters
    public BooleanProperty productNameEditableProperty() {
        return productNameEditable;
    }

    public BooleanProperty productTypeSelectionDisabledProperty() {
        return productTypeSelectionDisabled;
    }

    public BooleanProperty productRetailPriceEditableProperty() {
        return productRetailPriceEditable;
    }

}
