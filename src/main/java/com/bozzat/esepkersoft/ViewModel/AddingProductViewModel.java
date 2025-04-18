package com.bozzat.esepkersoft.ViewModel;

import com.bozzat.esepkersoft.Models.Product;
import com.bozzat.esepkersoft.Models.StockEntry;
import com.bozzat.esepkersoft.Models.Supplier;
import com.bozzat.esepkersoft.Services.ProductService;
import com.bozzat.esepkersoft.Services.SupplierService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AddingProductViewModel {
    // Constants & Collections ======================================================
    private final ObservableList<String> productTypes = FXCollections.observableArrayList("kg", "pc");
    private final ObservableList<Supplier> suppliers = FXCollections.observableList(
            new SupplierService().getAllSuppliers()
    );

    // Service Dependencies =========================================================
    private final ProductService productService = new ProductService();
    private final SupplierService supplierService = new SupplierService();

    // Data Properties ==============================================================
    // Product Information
    private final StringProperty selectedProductType = new SimpleStringProperty();
    private final StringProperty barcodeField = new SimpleStringProperty();
    private final StringProperty productName = new SimpleStringProperty();
    private final DoubleProperty retailPrice = new SimpleDoubleProperty();

    // Batch Information
    private final DoubleProperty batchQuantity = new SimpleDoubleProperty();
    private final DoubleProperty purchasePrice = new SimpleDoubleProperty();
    private final ObjectProperty<Supplier> selectedSupplier = new SimpleObjectProperty<>();

    // State Properties =============================================================
    private final ObjectProperty<BarcodeStatus> barcodeStatus = new SimpleObjectProperty<>();
    private final ObjectProperty<Product> currentProduct = new SimpleObjectProperty<>();

    // Editability Properties
    private final BooleanProperty productNameEditable = new SimpleBooleanProperty(true);
    private final BooleanProperty productTypeSelectionDisabled = new SimpleBooleanProperty(false);
    private final BooleanProperty productRetailPriceEditable = new SimpleBooleanProperty(true);

    // Error Properties
    private final BooleanProperty barcodeError = new SimpleBooleanProperty();
    private final BooleanProperty productNameError = new SimpleBooleanProperty();
    private final BooleanProperty productTypeError = new SimpleBooleanProperty();
    private final BooleanProperty retailPriceError = new SimpleBooleanProperty();
    private final BooleanProperty batchQuantityError = new SimpleBooleanProperty();
    private final BooleanProperty purchasePriceError = new SimpleBooleanProperty();

    // Constructor & Initialization =================================================
    public AddingProductViewModel() {
        initializeListeners();
    }

    private void initializeListeners() {
        barcodeStatus.addListener((obs, oldV, newV) -> updateFields(newV));
    }

    // Core Functionality ===========================================================
    public void searchProduct() {
        Product product = productService.getProductByBarcode(barcodeField.get());
        barcodeStatus.set(BarcodeStatus.INITIAL);

        if (product != null) {
            handleExistingProduct(product);
        } else {
            handleNewProduct();
        }
    }

    public void registerBatch() {
        if (!validateFields()) return;

        Integer supplierId = selectedSupplier.get() != null
                ? selectedSupplier.get().getId()
                : null;
        StockEntry stockEntry = new StockEntry(
                batchQuantity.get(),
                purchasePrice.get(),
                supplierId
        );

        if (currentProduct.get() != null) {
            productService.addBatchEntry(currentProduct.get(), stockEntry);
        } else {
            Product newProduct = new Product(
                    productName.get(),
                    barcodeField.get(),
                    selectedProductType.get(),
                    retailPrice.get()
            );
            productService.registerNewProduct(newProduct, stockEntry);
        }
        clearFields();
    }

    // State Management =============================================================
    public void clearFields() {
        // Product Information
        selectedProductType.set(null);
        barcodeField.set("");
        productName.set("");
        retailPrice.set(0.0);

        // Batch Information
        batchQuantity.set(0.0);
        purchasePrice.set(0.0);
        selectedSupplier.set(null);

        // System State
        barcodeStatus.set(BarcodeStatus.INITIAL);
        currentProduct.set(null);

        // Editability
        resetEditability();

        // Errors
        resetErrors();
    }

    private void clearProductInformationFields() {
        selectedProductType.set("");
        productName.set("");
        retailPrice.set(0.0);
    }

    // Validation ===================================================================
    private boolean validateFields() {
        boolean hasError = false;

        hasError |= checkEmpty(barcodeField, barcodeError);
        hasError |= checkEmpty(productName, productNameError);
        hasError |= checkEmpty(selectedProductType, productTypeError);
        hasError |= checkPositive(retailPrice, retailPriceError);
        hasError |= checkPositive(batchQuantity, batchQuantityError);
        hasError |= checkPositive(purchasePrice, purchasePriceError);

        return !hasError;
    }

    private boolean checkEmpty(StringProperty prop, BooleanProperty error) {
        boolean isEmpty = prop.get() == null || prop.get().trim().isEmpty();
        error.set(isEmpty);
        return isEmpty;
    }

    private boolean checkPositive(DoubleProperty prop, BooleanProperty error) {
        boolean isInvalid = prop.get() <= 0;
        error.set(isInvalid);
        return isInvalid;
    }

    // Editability Management ========================================================
    private void setEditableTrue() {
        productNameEditable.set(true);
        productTypeSelectionDisabled.set(false);
        productRetailPriceEditable.set(true);
    }

    private void setEditableFalse() {
        productNameEditable.set(false);
        productTypeSelectionDisabled.set(true);
        productRetailPriceEditable.set(false);
    }

    private void resetEditability() {
        productNameEditable.set(true);
        productTypeSelectionDisabled.set(false);
        productRetailPriceEditable.set(true);
    }

    // Helper Methods ================================================================
    private void handleExistingProduct(Product product) {
        barcodeStatus.set(BarcodeStatus.FOUND);
        currentProduct.set(product);
        productName.set(product.getName());
        selectedProductType.set(product.getUnitType());
        retailPrice.set(product.getCurrentPrice());
        setEditableFalse();
    }

    private void handleNewProduct() {
        barcodeStatus.set(BarcodeStatus.NEW);
        clearProductInformationFields();
        setEditableTrue();
        currentProduct.set(null);
    }

    private void updateFields(BarcodeStatus barcodeStatus) {
        // Implementation pending
    }

    private void resetErrors() {
        barcodeError.set(false);
        productNameError.set(false);
        productTypeError.set(false);
        retailPriceError.set(false);
        batchQuantityError.set(false);
        purchasePriceError.set(false);
    }

    // Property Accessors ============================================================
    public ObjectProperty<BarcodeStatus> barcodeStatusProperty() { return barcodeStatus; }
    public ObjectProperty<Supplier> selectedSupplierProperty() { return selectedSupplier; }
    public StringProperty barcodeFieldProperty() { return barcodeField; }
    public DoubleProperty purchasePriceProperty() { return purchasePrice; }
    public DoubleProperty retailPriceProperty() { return retailPrice; }
    public DoubleProperty batchQuantityProperty() { return batchQuantity; }
    public StringProperty productNameProperty() { return productName; }
    public StringProperty selectedProductTypeProperty() { return selectedProductType; }
    public BooleanProperty productNameEditableProperty() { return productNameEditable; }
    public BooleanProperty productTypeSelectionDisabledProperty() { return productTypeSelectionDisabled; }
    public BooleanProperty productRetailPriceEditableProperty() { return productRetailPriceEditable; }
    public BooleanProperty purchasePriceErrorProperty() { return purchasePriceError; }
    public BooleanProperty batchQuantityErrorProperty() { return batchQuantityError; }
    public BooleanProperty retailPriceErrorProperty() { return retailPriceError; }
    public BooleanProperty productTypeErrorProperty() { return productTypeError; }
    public BooleanProperty productNameErrorProperty() { return productNameError; }
    public BooleanProperty barcodeErrorProperty() { return barcodeError; }

    // Getters ======================================================================
    public ObservableList<Supplier> getSuppliers() { return suppliers; }
    public ObservableList<String> getProductTypes() { return productTypes; }
}