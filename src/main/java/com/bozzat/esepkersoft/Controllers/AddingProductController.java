package com.bozzat.esepkersoft.Controllers;

import com.bozzat.esepkersoft.Models.Supplier;
import com.bozzat.esepkersoft.ViewModel.AddingProductViewModel;
import com.bozzat.esepkersoft.ViewModel.BarcodeStatus;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import javafx.beans.property.*;

public class AddingProductController {
    // FXML Declarations =================================================================
    // Header UI Elements
    @FXML private Label messageLabel;
    @FXML private Text titleText;

    // Product Information Form Elements
    @FXML private Label barcodeStatusIcon;
    @FXML private TextField productBarcodeField;
    @FXML private TextField productNameField;
    @FXML private ComboBox<String> productTypeCombo;
    @FXML private TextField retailPriceField;
    @FXML private TextField purchasePriceField;
    @FXML private ComboBox<Supplier> supplierCombo;
    @FXML private TextField batchQuantityField;

    // Error Labels
    @FXML private Label productTypeErrorLabel;
    @FXML private Label retailPriceEmptyErrorLabel;
    @FXML private Label barcodeErrorLabel;
    @FXML private Label productNameErrorLabel;
    @FXML private Label batchQuantityEmptyErrorLabel;
    @FXML private Label purchasePriceEmptyErrorLabel;

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

    // Action Buttons
    @FXML private Button submitButton;
    @FXML private Button editRetailPriceButton;

    // ViewModel ========================================================================
    AddingProductViewModel addingProductViewModel = new AddingProductViewModel();

    // Initialization ===================================================================
    @FXML
    private void initialize() {
        setupEventHandlers();
        setupUIComponents();
        setupBindingsAndListeners();
        setTextFormatters();
    }

    private void setupEventHandlers() {
        productBarcodeField.setOnAction(e -> searchBarcode());
        submitButton.setOnAction(e -> addingProductViewModel.registerBatch());
    }

    private void setupUIComponents() {
        productTypeCombo.getItems().addAll(addingProductViewModel.getProductTypes());
        supplierCombo.setItems(addingProductViewModel.getSuppliers());
        supplierCombo.setConverter(new SupplierStringConverter());
    }

    private void setupBindingsAndListeners() {
        addingProductViewModel.barcodeStatusProperty().addListener((obs, oldValue, newValue) -> {
            updateBarcodeFieldStyle(newValue);
        });
        initializeBindings();
        initializeErrorBindings();
    }

    // Core Functionality ==============================================================
    private void searchBarcode() {
        addingProductViewModel.searchProduct();
    }

    // UI Style Management =============================================================
    private void updateBarcodeFieldStyle(BarcodeStatus status) {
        productBarcodeField.getStyleClass().removeAll("barcode-found", "barcode-new", "barcode-error");
        barcodeStatusIcon.getStyleClass().removeAll("status-icon-found", "status-icon-new", "status-icon-error");

        switch (status) {
            case FOUND:
                barcodeStatusIcon.getStyleClass().add("status-icon-found");
                productBarcodeField.getStyleClass().add("barcode-found");
                break;
            case NEW:
                barcodeStatusIcon.getStyleClass().add("status-icon-new");
                productBarcodeField.getStyleClass().add("barcode-new");
                break;
            case ERROR:
                barcodeStatusIcon.getStyleClass().add("status-icon-error");
                productBarcodeField.getStyleClass().add("barcode-error");
                break;
        }
        barcodeStatusIcon.setVisible(true);
    }

    // Data Bindings ===================================================================
    private void initializeBindings() {
        // Simple property bindings
        productBarcodeField.textProperty().bindBidirectional(addingProductViewModel.barcodeFieldProperty());
        productNameField.textProperty().bindBidirectional(addingProductViewModel.productNameProperty());
        productTypeCombo.valueProperty().bindBidirectional(addingProductViewModel.selectedProductTypeProperty());
        supplierCombo.valueProperty().bindBidirectional(addingProductViewModel.selectedSupplierProperty());

        // Numeric bindings with converter
        NumberStringConverter numberConverter = new NumberStringConverter();
        retailPriceField.textProperty().bindBidirectional(addingProductViewModel.retailPriceProperty(), numberConverter);
        batchQuantityField.textProperty().bindBidirectional(addingProductViewModel.batchQuantityProperty(), numberConverter);
        purchasePriceField.textProperty().bindBidirectional(addingProductViewModel.purchasePriceProperty(), numberConverter);

        // UI state bindings
        productNameField.editableProperty().bind(addingProductViewModel.productNameEditableProperty());
        productTypeCombo.disableProperty().bind(addingProductViewModel.productTypeSelectionDisabledProperty());
        retailPriceField.editableProperty().bind(addingProductViewModel.productRetailPriceEditableProperty());
    }

    // Error Handling ==================================================================
    private void initializeErrorBindings() {
        // Error label visibility
        bindErrorLabel(barcodeErrorLabel, addingProductViewModel.barcodeErrorProperty());
        bindErrorLabel(productNameErrorLabel, addingProductViewModel.productNameErrorProperty());
        bindErrorLabel(productTypeErrorLabel, addingProductViewModel.productTypeErrorProperty());
        bindErrorLabel(retailPriceEmptyErrorLabel, addingProductViewModel.retailPriceErrorProperty());
        bindErrorLabel(batchQuantityEmptyErrorLabel, addingProductViewModel.batchQuantityErrorProperty());
        bindErrorLabel(purchasePriceEmptyErrorLabel, addingProductViewModel.purchasePriceErrorProperty());

        // Error style bindings
        bindErrorStyle(productBarcodeField, addingProductViewModel.barcodeErrorProperty());
        bindErrorStyle(productNameField, addingProductViewModel.productNameErrorProperty());
        bindErrorStyle(productTypeCombo, addingProductViewModel.productTypeErrorProperty());
        bindErrorStyle(retailPriceField, addingProductViewModel.retailPriceErrorProperty());
        bindErrorStyle(batchQuantityField, addingProductViewModel.batchQuantityErrorProperty());
        bindErrorStyle(purchasePriceField, addingProductViewModel.purchasePriceErrorProperty());
        bindErrorStyle(supplierCombo, addingProductViewModel.productTypeErrorProperty());

        // Error clearing listeners
        setupErrorClearingListeners();
    }

    private void setupErrorClearingListeners() {
        productBarcodeField.textProperty().addListener((obs, old, neu) ->
                addingProductViewModel.barcodeErrorProperty().set(false));
        productNameField.textProperty().addListener((obs, old, neu) ->
                addingProductViewModel.productNameErrorProperty().set(false));
        productTypeCombo.valueProperty().addListener((obs, old, neu) ->
                addingProductViewModel.productTypeErrorProperty().set(false));
        retailPriceField.textProperty().addListener((obs, old, neu) ->
                addingProductViewModel.retailPriceErrorProperty().set(false));
        batchQuantityField.textProperty().addListener((obs, old, neu) ->
                addingProductViewModel.batchQuantityErrorProperty().set(false));
        purchasePriceField.textProperty().addListener((obs, old, neu) ->
                addingProductViewModel.purchasePriceErrorProperty().set(false));
        supplierCombo.valueProperty().addListener((obs, old, neu) ->
                addingProductViewModel.productTypeErrorProperty().set(false));
    }

    private void bindErrorLabel(Label lbl, BooleanProperty errorProp) {
        lbl.visibleProperty().bind(errorProp);
        lbl.managedProperty().bind(errorProp);
    }

    private void bindErrorStyle(Control ctl, BooleanProperty errorProp) {
        errorProp.addListener((obs, wasErr, isErr) -> {
            ObservableList<String> styles = ctl.getStyleClass();
            if (isErr && !styles.contains("error-field")) {
                styles.add("error-field");
            } else {
                styles.remove("error-field");
            }
        });
    }

    // Input Formatters ================================================================
    private void setTextFormatters() {
        setIntegerFormatter(productBarcodeField);
        setDecimalFormatter(batchQuantityField);
        setDecimalFormatter(retailPriceField);
        setDecimalFormatter(purchasePriceField);
    }

    private void setIntegerFormatter(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));
    }

    private void setDecimalFormatter(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*(\\.\\d{0,2})?") ? change : null));
    }

    // Helper Classes ==================================================================
    private class SupplierStringConverter extends StringConverter<Supplier> {
        @Override
        public String toString(Supplier supplier) {
            return supplier != null ? supplier.getName() : "";
        }

        @Override
        public Supplier fromString(String string) {
            return null; // Not needed for non-editable ComboBox
        }
    }
}