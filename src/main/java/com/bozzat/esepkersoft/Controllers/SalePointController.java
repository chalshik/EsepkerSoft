package com.bozzat.esepkersoft.Controllers;

import com.bozzat.esepkersoft.ViewModel.POSViewModel;
import com.bozzat.esepkersoft.ViewModel.SaleItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

public class SalePointController {

    @FXML
    private Label selectedProductName;

    @FXML
    private TextField priceField;

    @FXML
    private TextField quantityField;

    @FXML
    private Label kgUnitLabel;

    @FXML
    private Label pieceUnitLabel;

    @FXML
    private StackPane unitContainer;

    @FXML
    private TextField barcodeScannerField;

    @FXML
    private TableView<SaleItem> productsTable;

    @FXML
    private TableColumn<SaleItem, String> nameColumn;

    @FXML
    private TableColumn<SaleItem, Double> priceColumn;

    @FXML
    private TableColumn<SaleItem, Double> quantityColumn;

    @FXML
    private TableColumn<SaleItem, String> unitColumn;

    @FXML
    private TableColumn<SaleItem, Double> totalColumn;

    @FXML
    private Label totalLabel;

    @FXML
    private Label changeLabel;

    @FXML
    private Button payButton;

    @FXML
    private DialogPane paymentDialog;

    @FXML
    private VBox paymentNotification;

    @FXML
    private Label notificationText;

    @FXML
    private Button minusbtn;

    @FXML
    private Button plusbtn;

    @FXML
    private Button deletebtn;

    @FXML
    private TextField receivedField;

    private ObservableList<SaleItem> saleItems = FXCollections.observableArrayList();
    private POSViewModel posService = new POSViewModel(saleItems);
    // Initialize method (optional)
    @FXML
    public void initialize() {

        totalLabel.textProperty().bind(posService.totalOfTotalsProperty().asString());

        //
        receivedField.setOnAction(event -> {
            calculateChange();
        });
        //
        deletebtn.setOnAction(event -> {
            deleteItem();
        });
// табел
        nameColumn.setCellValueFactory(cd -> cd.getValue().nameProperty());
        priceColumn.setCellValueFactory(cd -> cd.getValue().priceProperty().asObject());
        quantityColumn.setCellValueFactory(cd -> cd.getValue().quantityProperty().asObject());
        unitColumn.setCellValueFactory(cd -> cd.getValue().unitTypeProperty());


        totalColumn.setCellValueFactory(cd -> cd.getValue().totalProperty().asObject());

        productsTable.setItems(saleItems);
// барцоде
        barcodeScannerField.setOnAction(event -> {
            handleBarcode();
        });

// binding selected row properties
        selectedProductName.textProperty().bind(posService.nameProperty());
        priceField.textProperty().bind(posService.priceProperty().asString());
        quantityField.textProperty().bindBidirectional(posService.quantityProperty(), new NumberStringConverter());
// bind unit selection properties

// add listener to selected
        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            posService.getSelectedRow(oldItem, newItem);
        });
// commit payment button
        payButton.setOnAction(event -> {
            showPaymentDialog();
        });
        //
        minusbtn.setOnAction(event -> {
            minusOne();
        });
        plusbtn.setOnAction( event -> {
            plusOne();
        });
        //
        posService.unitTypeProperty().addListener((obs, oldValue, newValue) -> {
            changeUnitLabel(newValue);
        });
        kgUnitLabel.setVisible(false);
        pieceUnitLabel.setVisible(true);
    }

    public void handleBarcode() {
        int index = posService.handleBarcode(barcodeScannerField.getText());
        Platform.runLater(() -> {
            if (index != -1) {
                productsTable.getSelectionModel().select(index);
                productsTable.scrollTo(index);
            } else {
                productsTable.getSelectionModel().clearSelection();
            }
        });
    }


    public void showPaymentDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setDialogPane(paymentDialog);
        dialog.setTitle("Оплата");
        dialog.show();
    }

    public void minusOne() {
        posService.quantityProperty().set(posService.quantityProperty().get() - 1);
    }

    public void plusOne() {
        posService.quantityProperty().set(posService.quantityProperty().get() + 1);
    }

    public void deleteItem() {
        posService.deleteItem();
    }

    public void calculateChange() {
        changeLabel.setText(String.valueOf(posService.calculateChange(Double.parseDouble(receivedField.textProperty().get()))));
    }

    public void changeUnitLabel(String unitType) {
        if ("шт".equals(unitType)) {
            kgUnitLabel.setVisible(false);
            pieceUnitLabel.setVisible(true);
        } else {
            kgUnitLabel.setVisible(true);
            pieceUnitLabel.setVisible(false);
        }
    }


}
