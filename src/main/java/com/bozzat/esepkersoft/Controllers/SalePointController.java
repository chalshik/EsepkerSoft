package com.bozzat.esepkersoft.Controllers;

import com.bozzat.esepkersoft.Services.ProductService;
import com.bozzat.esepkersoft.ViewModel.POSViewModel;
import com.bozzat.esepkersoft.ViewModel.SaleItemViewModel;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

import java.util.Objects;

public class SalePointController {
    // Constants
    private static final String PIECE_UNIT = "шт";
    private static final NumberStringConverter NUMBER_CONVERTER = new NumberStringConverter();

    // Services
    ProductService productService = new ProductService();
    private POSViewModel posService = new POSViewModel(productService);

    // FXML components
    @FXML private Label selectedProductName;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private Label kgUnitLabel;
    @FXML private Label pieceUnitLabel;
    @FXML private StackPane unitContainer;
    @FXML private TextField barcodeScannerField;
    @FXML private TableView<SaleItemViewModel> productsTable;
    @FXML private TableColumn<SaleItemViewModel, String> nameColumn;
    @FXML private TableColumn<SaleItemViewModel, Double> priceColumn;
    @FXML private TableColumn<SaleItemViewModel, Double> quantityColumn;
    @FXML private TableColumn<SaleItemViewModel, String> unitColumn;
    @FXML private TableColumn<SaleItemViewModel, Double> totalColumn;
    @FXML private Label totalLabel;
    @FXML private Label changeLabel;
    @FXML private Button payButton;
    @FXML private DialogPane paymentDialog;
    @FXML private VBox paymentNotification;
    @FXML private Label notificationText;
    @FXML private Button minusbtn;
    @FXML private Button plusbtn;
    @FXML private Button deletebtn;
    @FXML private TextField receivedField;

    @FXML
    public void initialize() {

        setUpTableColumns();
        setUpBindings();
        setUpEventHandlers();
        setUpSelectionHandling();

    }

    private void setUpTableColumns() {
        nameColumn.setCellValueFactory(cd -> cd.getValue().nameProperty());
        priceColumn.setCellValueFactory(cd -> cd.getValue().priceProperty().asObject());
        quantityColumn.setCellValueFactory(cd -> cd.getValue().quantityProperty().asObject());
        unitColumn.setCellValueFactory(cd -> cd.getValue().unitTypeProperty());
        totalColumn.setCellValueFactory(cd -> cd.getValue().totalProperty().asObject());

        productsTable.setItems(posService.getSaleItems());
    }

    private void setUpBindings() {
        selectedProductName.textProperty().bind(posService.nameProperty());
        priceField.textProperty().bind(posService.priceProperty().asString());
        quantityField.textProperty().bindBidirectional(posService.quantityProperty(), new NumberStringConverter());
        totalLabel.textProperty().bind(posService.totalOfTotalsProperty().asString());
        pieceUnitLabel.visibleProperty().bind(Bindings.createBooleanBinding(() -> PIECE_UNIT.equals(posService.unitTypeProperty().get()),
                posService.unitTypeProperty()
        ));
        kgUnitLabel.visibleProperty().bind(pieceUnitLabel.visibleProperty().not());
    }

    private void setUpEventHandlers() {
        barcodeScannerField.setOnAction(e -> processBarcodeInput());
        deletebtn.setOnAction(e -> deleteCurrentItem());
        minusbtn.setOnAction(e -> adjustQuantity(-1));
        plusbtn.setOnAction(e -> adjustQuantity(1));
        payButton.setOnAction(e -> showPaymentDialog());
    }

    private void setUpSelectionHandling() {
        // ViewModel -> Table selection
        posService.currentItemProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> updateTableSelection(newVal));
        });

        // Table selection -> ViewModel
        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (shouldUpdateViewModel(newVal)) {
                posService.setCurrentItem(newVal);
            }
        });
    }

    /**
     * Updates table selection based on ViewModel changes
     * Handles thread safety and null cases properly
     */
    private void updateTableSelection(SaleItemViewModel newItem) {
        MultipleSelectionModel<SaleItemViewModel> selectionModel = productsTable.getSelectionModel();

        if (newItem == null) {
            if (selectionModel.getSelectedItem() != null) {
                selectionModel.clearSelection();
            }
            return;
        }

        // Only update if needed to avoid unnecessary events
        if (!isSameItem(selectionModel.getSelectedItem(), newItem)) {
            int itemIndex = productsTable.getItems().indexOf(newItem);
            if (itemIndex >= 0) {
                selectionModel.select(itemIndex);
            }
        }
    }

    /**
     * Determines if ViewModel should be updated from table selection changes
     */
    private boolean shouldUpdateViewModel(SaleItemViewModel newItem) {
        // Skip if selection was cleared
        if (newItem == null) return false;

        // Get current item safely
        SaleItemViewModel currentVmItem = posService.currentItemProperty().get();

        // Only update if different items
        return !isSameItem(currentVmItem, newItem);
    }

    /**
     * Safe item comparison using business key (barcode)
     * More reliable than equals() for model objects
     */
    private boolean isSameItem(SaleItemViewModel a, SaleItemViewModel b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return Objects.equals(a.getBarcode(), b.getBarcode());
    }

    private void processBarcodeInput() {
        posService.processBarcodeInput(barcodeScannerField.getText());
        barcodeScannerField.clear();
    }


    public void showPaymentDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setDialogPane(paymentDialog);
        dialog.setTitle("Оплата");
        dialog.show();
    }

    public void deleteCurrentItem() {
        posService.deleteCurrentItem();
        productsTable.getSelectionModel().clearSelection();
    }

    private void adjustQuantity(double delta) {
        posService.setQuantity(posService.getQuantity() + delta);
    }
}
