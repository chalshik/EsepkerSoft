package com.bozzat.esepkersoft.Controllers;

import com.bozzat.esepkersoft.Models.Supplier;
import com.bozzat.esepkersoft.Services.ProductService;
import com.bozzat.esepkersoft.Services.SupplierService;
import com.bozzat.esepkersoft.ViewModel.AddingProductViewModel;
import com.bozzat.esepkersoft.ViewModel.BarcodeStatus;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class AddingProductController {
    // Header UI Elements
    @FXML private Label messageLabel;
    @FXML private Text titleText;

    // Product Information Form Elements
    @FXML private Label barcodeStatusIcon;
    @FXML private TextField productBarcodeField;
    @FXML private Label barcodeErrorLabel;
    @FXML private TextField productNameField;
    @FXML private Label productNameErrorLabel;
    @FXML private ComboBox<String> productTypeCombo;
    @FXML private Label productTypeErrorLabel;
    @FXML private TextField retailPriceField;
    @FXML private Button editRetailPriceButton;
    @FXML private Label retailPriceEmptyErrorLabel;
    @FXML private Label retailPriceNonPositiveErrorLabel;

    // Batch Information Form Elements
    @FXML private TextField batchQuantityField;
    @FXML private Label batchQuantityEmptyErrorLabel;
    @FXML private Label batchQuantityNonPositiveErrorLabel;
    @FXML private ComboBox<Supplier> supplierCombo;
    @FXML private TextField purchasePriceField;
    @FXML private Label purchasePriceEmptyErrorLabel;
    @FXML private Label purchasePriceNonPositiveErrorLabel;

    // Summary Section Elements
    @FXML private Accordion advancedAccordion;
    @FXML private Label summaryProductName;
    @FXML private Label summaryProductType;
    @FXML private Label summaryBatchQuantity;
    @FXML private Label summarySupplier;
    @FXML private Label summaryPurchasePrice;
    @FXML private Label summaryRetailPrice;
    @FXML private Label summaryUnitProfit;
    @FXML private Label summaryBatchCost;
    @FXML private Label summaryTotalProfit;
    @FXML private Label priceErrorLabel;

    // Action Button
    @FXML private Button submitButton;
    // ViewModel
    AddingProductViewModel addingProductViewModel = new AddingProductViewModel();
    @FXML
    private void initialize() {
        productBarcodeField.setOnAction(e -> {
            searchBarcode();
        });
        setTextFormatters();
        productTypeCombo.getItems().addAll(addingProductViewModel.getProductTypes());
        supplierCombo.setItems(addingProductViewModel.getSuppliers());
        supplierCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Supplier supplier) {
                return supplier != null ? supplier.getName() : "";
            }

            @Override
            public Supplier fromString(String string) {
                // Not needed unless you allow editing
                return null;
            }
        });

        addingProductViewModel.barcodeStatusProperty().addListener((obs, oldValue, newValue) -> {
            updateBarcodeFieldStyle(newValue);
        });

        initializeBindings();
    }

    private void searchBarcode() {
        addingProductViewModel.searchProduct();
    }

    private void updateBarcodeFieldStyle(BarcodeStatus status) {
        productBarcodeField.getStyleClass().removeAll("barcode-found", "barcode-new", "barcode-error");
        barcodeStatusIcon.getStyleClass().removeAll("status-icon-found", "status-icon-new", "status-icon-error");

        switch (status) {
            case FOUND:
                barcodeStatusIcon.getStyleClass().add("status-icon-found");
                barcodeStatusIcon.setVisible(true);
                productBarcodeField.getStyleClass().add("barcode-found");
                break;
            case NEW:
                barcodeStatusIcon.getStyleClass().add("status-icon-new");
                barcodeStatusIcon.setVisible(true);
                productBarcodeField.getStyleClass().add("barcode-new");
                break;
            case ERROR:
                barcodeStatusIcon.getStyleClass().add("status-icon-error");
                barcodeStatusIcon.setVisible(true);
                productBarcodeField.getStyleClass().add("barcode-error");
                break;
            default:
                break;
        }
    }

    private void initializeBindings() {
        productBarcodeField.textProperty().bindBidirectional(addingProductViewModel.barcodeFieldProperty());
        productNameField.textProperty().bindBidirectional(addingProductViewModel.productNameProperty());
        productTypeCombo.valueProperty().bindBidirectional(addingProductViewModel.selectedProductTypeProperty());
        NumberStringConverter numberStringConverter = new NumberStringConverter();
        retailPriceField.textProperty().bindBidirectional(addingProductViewModel.retailPriceProperty(), numberStringConverter);
        batchQuantityField.textProperty().bindBidirectional(addingProductViewModel.batchQuantityProperty(), numberStringConverter);
        purchasePriceField.textProperty().bindBidirectional(addingProductViewModel.purchasePriceProperty(), numberStringConverter);
        supplierCombo.valueProperty().bindBidirectional(addingProductViewModel.selectedSupplierProperty());
        productNameField.editableProperty().bind(addingProductViewModel.productNameEditableProperty());
        productTypeCombo.disableProperty().bind(addingProductViewModel.productTypeSelectionDisabledProperty());
        retailPriceField.editableProperty().bind(addingProductViewModel.productRetailPriceEditableProperty());
    }

    private void setTextFormatters() {
        // Only integers (no decimal)
        setIntegerFormatter(productBarcodeField);

        // Allow decimal numbers (like prices)
        setDecimalFormatter(batchQuantityField);
        setDecimalFormatter(retailPriceField);
        setDecimalFormatter(purchasePriceField);
    }

    private void setIntegerFormatter(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));
    }

    private void setDecimalFormatter(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*(\\.\\d{0,2})?")) { // up to 2 decimals
                return change;
            }
            return null;
        }));
    }

}
