package com.bozzat.esepkersoft.Controllers;

import com.bozzat.esepkersoft.Services.ProductService;
import com.bozzat.esepkersoft.ViewModel.POSViewModel;
import com.bozzat.esepkersoft.ViewModel.SaleItemViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

import java.util.Objects;

public class SalePointController {
    // Constants
    private static final String PIECE_UNIT = "шт";
    private static final String KG_UNIT = "кг"; // alternative measuring unit
    private static final NumberStringConverter NUMBER_CONVERTER = new NumberStringConverter();

    // Services
    ProductService productService = new ProductService();
    private POSViewModel posViewModel = new POSViewModel(productService);

    // FXML components
    @FXML private BorderPane borderPane;
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
    // Removed receivedField injection as its functionality now resides in the payment dialog.

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Scene scene = borderPane.getScene();
            if (scene != null) {
                scene.setOnKeyPressed(event ->{
                    if (event.getCode() == KeyCode.SHIFT) {
                        handlePayButtonAction();
                    }
                });
            }
        });
        setUpTableColumns();
        setUpBindings();
        setUpEventHandlers();
        setUpSelectionHandling();
        setUpGlobalShortcuts();
    }

    private void setUpTableColumns() {
        nameColumn.setCellValueFactory(cd -> cd.getValue().nameProperty());
        priceColumn.setCellValueFactory(cd -> cd.getValue().priceProperty().asObject());
        quantityColumn.setCellValueFactory(cd -> cd.getValue().quantityProperty().asObject());
        unitColumn.setCellValueFactory(cd -> cd.getValue().unitTypeProperty());
        totalColumn.setCellValueFactory(cd -> cd.getValue().totalProperty().asObject());

        productsTable.setItems(posViewModel.getSaleItems());

        // Set up a row factory so that when a row is selected and Enter is pressed,
        // focus returns to the barcodeScannerField.
        productsTable.setRowFactory(tableView -> {
            TableRow<SaleItemViewModel> row = new TableRow<>();
            row.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ENTER) {
                        barcodeScannerField.requestFocus();
                        event.consume();
                    }
                }
            });
            return row;
        });
    }

    private void setUpBindings() {
        selectedProductName.textProperty().bind(posViewModel.nameProperty());
        priceField.textProperty().bind(posViewModel.priceProperty().asString());
        quantityField.textProperty().bindBidirectional(posViewModel.quantityProperty(), new NumberStringConverter());
        totalLabel.textProperty().bind(posViewModel.totalOfTotalsProperty().asString());
        pieceUnitLabel.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                        PIECE_UNIT.equals(posViewModel.unitTypeProperty().get()),
                posViewModel.unitTypeProperty()
        ));
        kgUnitLabel.visibleProperty().bind(pieceUnitLabel.visibleProperty().not());
    }

    private void setUpEventHandlers() {
        // Barcode input: when action fired (e.g. Enter key), process barcode and focus amount field.
        barcodeScannerField.setOnAction(e -> {
            processBarcodeInput();
            quantityField.requestFocus();
        });
        // Remove additional key events that reference the received field.
        // (Previously, arrow keys would shift focus to the received field.)

        // Amount (quantity) field: on Enter, shift focus to barcode input.
        quantityField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                barcodeScannerField.requestFocus();
                e.consume();
            }
            // Allow deletion with DELETE key:
            else if (e.getCode() == KeyCode.DELETE) {
                deleteCurrentItem();
                e.consume();
            }
        });

        // Button actions.
        deletebtn.setOnAction(e -> deleteCurrentItem());
        minusbtn.setOnAction(e -> adjustQuantity(-1));
        plusbtn.setOnAction(e -> adjustQuantity(1));
        payButton.setOnAction(e -> handlePayButtonAction());
    }

    private void setUpSelectionHandling() {
        // ViewModel -> Table selection
        posViewModel.currentItemProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> updateTableSelection(newVal));
        });

        // Table selection -> ViewModel
        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (shouldUpdateViewModel(newVal)) {
                posViewModel.setCurrentItem(newVal);
            }
        });
    }

    /**
     * Add global (or application-wide) keyboard shortcuts.
     * In this case, we add a shortcut for toggling the measuring unit.
     */
    private void setUpGlobalShortcuts() {
        // Listen to the scene once available (e.g., on the root container)
        unitContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    // Ctrl+M toggles measuring unit.
                    if (event.isControlDown() && event.getCode() == KeyCode.M) {
                        toggleMeasuringUnit();
                        event.consume();
                    }
                });
            }
        });
    }

    /**
     * Toggle the measuring unit between piece (шт) and kilogram (кг).
     */
    private void toggleMeasuringUnit() {
        if (PIECE_UNIT.equals(posViewModel.unitTypeProperty().get())) {
            posViewModel.unitTypeProperty().set(KG_UNIT);
        } else {
            posViewModel.unitTypeProperty().set(PIECE_UNIT);
        }
    }

    private void handlePayButtonAction() {
        if (productsTable.getItems().isEmpty()) {
            showEmptyTableWarning();
            return;
        }
        showPaymentDialog();
    }

    private void showEmptyTableWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка оплаты");
        alert.setHeaderText("Нет товаров для оплаты");
        alert.setContentText("Добавьте товары в чек перед совершением оплаты.");
        alert.showAndWait();
    }

    /**
     * Updates table selection based on ViewModel changes.
     * Handles thread safety and null cases properly.
     */
    private void updateTableSelection(SaleItemViewModel newItem) {
        MultipleSelectionModel<SaleItemViewModel> selectionModel = productsTable.getSelectionModel();

        if (newItem == null) {
            if (selectionModel.getSelectedItem() != null) {
                selectionModel.clearSelection();
            }
            return;
        }

        // Only update if needed to avoid unnecessary events.
        if (!isSameItem(selectionModel.getSelectedItem(), newItem)) {
            int itemIndex = productsTable.getItems().indexOf(newItem);
            if (itemIndex >= 0) {
                selectionModel.select(itemIndex);
            }
        }
    }

    /**
     * Determines if the ViewModel should be updated from table selection changes.
     */
    private boolean shouldUpdateViewModel(SaleItemViewModel newItem) {
        if (newItem == null) return false; // Skip if selection was cleared.
        SaleItemViewModel currentVmItem = posViewModel.currentItemProperty().get();
        return !isSameItem(currentVmItem, newItem);
    }

    /**
     * Safe item comparison using business key (barcode).
     * More reliable than equals() for model objects.
     */
    private boolean isSameItem(SaleItemViewModel a, SaleItemViewModel b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return Objects.equals(a.getBarcode(), b.getBarcode());
    }

    private void processBarcodeInput() {
        posViewModel.processBarcodeInput(barcodeScannerField.getText());
        barcodeScannerField.clear();
    }

    public void showPaymentDialog() {
        try {
            // Load PaymentDialog.fxml dynamically.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bozzat/esepkersoft/PaymentDialog.fxml"));
            DialogPane dialogPane = loader.load();

            // Retrieve the controller from the loaded FXML and pass the shared view model.
            PaymentDialogController paymentController = loader.getController();
            paymentController.setViewModel(posViewModel);  // Pass the view model.

            // Create the dialog and set its DialogPane.
            Dialog<String> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Оплата");
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteCurrentItem() {
        posViewModel.deleteCurrentItem();
        productsTable.getSelectionModel().clearSelection();
    }

    private void adjustQuantity(double delta) {
        posViewModel.setQuantity(posViewModel.getQuantity() + delta);
    }
}
