package com.bozzat.esepkersoft.ViewModel;


import com.bozzat.esepkersoft.Models.Product;
import com.bozzat.esepkersoft.Models.StockEntry;
import com.bozzat.esepkersoft.Models.Supplier;
import com.bozzat.esepkersoft.Services.ProductService;
import com.bozzat.esepkersoft.Services.SupplierService;
import javafx.beans.property.*;
import javafx.collections.*;

public class AddingProductViewModel {

    // Input
    private final StringProperty productBarcode = new SimpleStringProperty();
    private Boolean existingProduct = false;
    private final StringProperty productName = new SimpleStringProperty();
    private final StringProperty productType = new SimpleStringProperty();
    private final IntegerProperty batchQuantity = new SimpleIntegerProperty();
    private final ObjectProperty<Supplier> supplier = new SimpleObjectProperty<Supplier>();
    private final DoubleProperty purchasePrice = new SimpleDoubleProperty();
    private final DoubleProperty retailPrice = new SimpleDoubleProperty();

    // Computed properties
    private final ReadOnlyDoubleWrapper unitProfit = new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper totalBatchCost = new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper totalPotentialProfit = new ReadOnlyDoubleWrapper();

    // Message property to display validation or success messages
    private final StringProperty message = new SimpleStringProperty();
    private SupplierService supplierService = new SupplierService();

    // List of suppliers for the ComboBox
    private final ObservableList<Supplier> supplierList = FXCollections.observableList(supplierService.getAllSuppliers());
    // Models
    private Product product = new Product();

    // Services
    private ProductService productService = new ProductService();

    public AddingProductViewModel() {
        // Bind computed properties: these update automatically when the related properties change.
        unitProfit.bind(retailPrice.subtract(purchasePrice));
        totalBatchCost.bind(purchasePrice.multiply(batchQuantity));
        totalPotentialProfit.bind(unitProfit.multiply(batchQuantity));
    }

    public void searchProduct() {
        product = productService.getProductByBarcode(productBarcode.get());
        if (product == null) {
            existingProduct = false;
        } else {
            existingProduct = true;
            productName.set(product.getName());
            productType.set(product.getUnitType());
            retailPrice.set(product.getCurrentPrice());
        }
    }

    public void registerBatch() {


        if (existingProduct) {
            StockEntry stockEntry = new StockEntry(batchQuantity.get(), purchasePrice.get(), supplier.get().getId());
            productService.addBatchEntry(product,stockEntry);
        } else {
            StockEntry stockEntry = new StockEntry(batchQuantity.get(), purchasePrice.get(), supplier.get().getId());
            productService.registerNewProduct(product, stockEntry);
        }
    }


    private void newProductBatch() {

    }
    // Getters for input properties
    public StringProperty productNameProperty() {
        return productName;
    }

    public StringProperty productTypeProperty() {
        return productType;
    }

    public IntegerProperty batchQuantityProperty() {
        return batchQuantity;
    }

    public ObjectProperty<Supplier> supplierProperty() {
        return supplier;
    }

    public DoubleProperty purchasePriceProperty() {
        return purchasePrice;
    }

    public DoubleProperty retailPriceProperty() {
        return retailPrice;
    }

    // Getters for computed properties
    public ReadOnlyDoubleProperty unitProfitProperty() {
        return unitProfit.getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty totalBatchCostProperty() {
        return totalBatchCost.getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty totalPotentialProfitProperty() {
        return totalPotentialProfit.getReadOnlyProperty();
    }

    // Message property: used to display errors or success messages.
    public StringProperty messageProperty() {
        return message;
    }

    public void setMessage(String msg) {
        message.set(msg);
    }

    // Supplier list methods
    public ObservableList<Supplier> getSupplierList() {
        return supplierList;
    }


    // The method called from the controller on form submission.
    // It performs basic validation and updates the message accordingly.
    public void submitForm() {
        // Validate Product Name
        if (productName.get() == null || productName.get().trim().isEmpty()) {
            setMessage("Product name is required!");
            return;
        }
        // Validate Batch Quantity
        if (batchQuantity.get() <= 0) {
            setMessage("Batch quantity must be greater than 0!");
            return;
        }
        // Validate Purchase Price
        if (purchasePrice.get() <= 0) {
            setMessage("Please enter a valid purchase price!");
            return;
        }
        // Validate Retail Price
        if (retailPrice.get() <= 0) {
            setMessage("Please enter a valid retail price!");
            return;
        }
        // All validations passed; report success.
        setMessage("Product batch registered successfully!");
        // Optionally, you can reset properties here if desired.
    }

    public String getProductBarcode() {
        return productBarcode.get();
    }

    public StringProperty productBarcodeProperty() {
        return productBarcode;
    }
}
