package com.bozzat.esepkersoft.Controllers;

import com.bozzat.esepkersoft.Models.Supplier;
import com.bozzat.esepkersoft.ViewModel.AddingProductViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

public class AddingProductController {
    
    private AddingProductViewModel viewModel = new AddingProductViewModel();
    // Form Inputs
    @FXML private TextField productBarcodeField;
    @FXML private TextField productNameField;
    @FXML private ComboBox<String> productTypeCombo;
    @FXML private TextField batchQuantityField;
    @FXML private ComboBox<Supplier> supplierCombo;
    @FXML private TextField purchasePriceField;
    @FXML private TextField retailPriceField;
    @FXML private Button submitButton;
    @FXML private Label messageLabel;

    // Summary Outputs
    @FXML private Label summaryProductName;
    @FXML private Label summaryProductType;
    @FXML private Label summaryBatchQuantity;
    @FXML private Label summarySupplier;
    @FXML
    private Label summaryPurchasePrice;
    @FXML private Label summaryRetailPrice;
    @FXML private Label summaryUnitProfit;
    @FXML private Label summaryBatchCost;
    @FXML private Label summaryTotalProfit;

    @FXML
    public void initialize() {
        initializeBindings();
    }

    private void initializeBindings(){
        // Bind UI controls to ViewModel properties

        productBarcodeField.textProperty().bindBidirectional(viewModel.productBarcodeProperty());
        productNameField.textProperty().bindBidirectional(viewModel.productNameProperty());
        productTypeCombo.valueProperty().bindBidirectional(viewModel.productTypeProperty());
        batchQuantityField.textProperty().bindBidirectional(viewModel.batchQuantityProperty(), new NumberStringConverter());
        purchasePriceField.textProperty().bindBidirectional(viewModel.purchasePriceProperty(), new NumberStringConverter());
        retailPriceField.textProperty().bindBidirectional(viewModel.retailPriceProperty(), new NumberStringConverter());
        supplierCombo.setItems(viewModel.getSupplierList());
        supplierCombo.setConverter(new StringConverter<Supplier>() {
            @Override
            public String toString(Supplier supplier) {
                return supplier != null ? supplier.getName() : "";
            }

            @Override
            public Supplier fromString(String s) {
                return null;
            }
        });
        viewModel.supplierProperty().bind(supplierCombo.valueProperty());
        messageLabel.textProperty().bind(viewModel.messageProperty());

        summaryProductName.textProperty().bind(viewModel.productNameProperty());

        summaryUnitProfit.textProperty().bind(viewModel.unitProfitProperty().asString());
        summaryBatchCost.textProperty().bind(viewModel.totalBatchCostProperty().asString());
        summaryTotalProfit.textProperty().bind(viewModel.totalPotentialProfitProperty().asString());

        supplierCombo.setItems(viewModel.getSupplierList());
        productTypeCombo.getItems().addAll("piece", "kg");
    }
}
