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

    // Error properties
    private final BooleanProperty barcodeError = new SimpleBooleanProperty();
    private final BooleanProperty productNameError = new SimpleBooleanProperty();
    private final BooleanProperty productTypeError = new SimpleBooleanProperty();
    private final BooleanProperty retailPriceError = new SimpleBooleanProperty();
    private final BooleanProperty batchQuantityError = new SimpleBooleanProperty();
    private final BooleanProperty purchasePriceError = new SimpleBooleanProperty();



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

    public void registerBatch() {
        Integer supplierId = selectedSupplier.get() != null ? selectedSupplier.get().getId() : null;
        if (validateFields()) {
            if (currentProduct.get() != null) {
                System.out.println(currentProduct);
                StockEntry stockEntry = new StockEntry(batchQuantity.get(), purchasePrice.get(), supplierId);
                productService.addBatchEntry(currentProduct.get()
                        , stockEntry);
                System.out.println("almost success");
            } else {
                StockEntry stockEntry = new StockEntry(batchQuantity.get(), purchasePrice.get(), supplierId);
                Product newProduct = new Product(productName.get(), barcodeField.get(), selectedProductType.get(), retailPrice.get());
                productService.registerNewProduct(newProduct, stockEntry);
            }
        } else {

        }
    }

    private boolean validateFields() {
        boolean hasError = false;

        // Barcode field
        boolean isBarcodeEmpty = barcodeField.get() == null || barcodeField.get().trim().isEmpty();
        barcodeError.set(isBarcodeEmpty);
        hasError |= isBarcodeEmpty;

        // Product name
        boolean isProductNameEmpty = productName.get() == null || productName.get().trim().isEmpty();
        productNameError.set(isProductNameEmpty);
        hasError |= isProductNameEmpty;

        // Product type
        boolean isProductTypeEmpty = selectedProductType.get() == null || selectedProductType.get().trim().isEmpty();
        productTypeError.set(isProductTypeEmpty);
        hasError |= isProductTypeEmpty;

        // Retail price
        boolean isRetailPriceInvalid = retailPrice.get() <= 0;
        retailPriceError.set(isRetailPriceInvalid);
        hasError |= isRetailPriceInvalid;

        // Batch quantity
        boolean isBatchQuantityInvalid = batchQuantity.get() <= 0;
        batchQuantityError.set(isBatchQuantityInvalid);
        hasError |= isBatchQuantityInvalid;

        // Purchase price
        boolean isPurchasePriceInvalid = purchasePrice.get() <= 0;
        purchasePriceError.set(isPurchasePriceInvalid);
        hasError |= isPurchasePriceInvalid;

        return !hasError;
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


    // getters

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

    public boolean isPurchasePriceError() {
        return purchasePriceError.get();
    }

    public BooleanProperty purchasePriceErrorProperty() {
        return purchasePriceError;
    }

    public boolean isBatchQuantityError() {
        return batchQuantityError.get();
    }

    public BooleanProperty batchQuantityErrorProperty() {
        return batchQuantityError;
    }

    public boolean isRetailPriceError() {
        return retailPriceError.get();
    }

    public BooleanProperty retailPriceErrorProperty() {
        return retailPriceError;
    }

    public boolean isProductTypeError() {
        return productTypeError.get();
    }

    public BooleanProperty productTypeErrorProperty() {
        return productTypeError;
    }

    public boolean isProductNameError() {
        return productNameError.get();
    }

    public BooleanProperty productNameErrorProperty() {
        return productNameError;
    }

    public boolean isBarcodeError() {
        return barcodeError.get();
    }

    public BooleanProperty barcodeErrorProperty() {
        return barcodeError;
    }
}
