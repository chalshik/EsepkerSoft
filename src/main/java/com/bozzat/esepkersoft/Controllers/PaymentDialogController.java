package com.bozzat.esepkersoft.Controllers;

import com.bozzat.esepkersoft.Models.Sale;
import com.bozzat.esepkersoft.Models.SaleItem;
import com.bozzat.esepkersoft.Services.SaleService;
import com.bozzat.esepkersoft.ViewModel.POSViewModel;
import com.bozzat.esepkersoft.ViewModel.SaleItemViewModel;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javax.xml.stream.FactoryConfigurationError;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class PaymentDialogController {
    @FXML private Button cashButton;
    @FXML private Label totalLabel;
    @FXML private Button cardButton;
    @FXML private Pane cashPaymentPane;
    @FXML private TextField receivedField;
    @FXML private Label changeLabel;
    @FXML private Pane cardPaymentPane;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;
    @FXML private POSViewModel posViewModel;

        // ... [existing FXML injections] ...

    public void setViewModel(POSViewModel posViewModel) {
        this.posViewModel = posViewModel;
        initializeBindings();
        paymentMethodsProperties();
        configureInputValidation();
        initializePaymentPanels();
        setupButtonActions();
    }

    private void configureInputValidation() {
        // TextFormatter for currency input validation
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("^\\d*\\.?\\d{0,2}$")) {
                return change;
            }
            return null;
        };

        receivedField.setTextFormatter(new TextFormatter<>(filter));
        receivedField.setText("0.00");
    }

    private void initializePaymentPanels() {
        cashPaymentPane.setVisible(false);
        cardPaymentPane.setVisible(false);
    }

    private void paymentMethodsProperties() {
        cashButton.setOnAction(e -> togglePaymentMethod("cash"));
        cardButton.setOnAction(e -> togglePaymentMethod("card"));
    }

    private void togglePaymentMethod(String method) {
        boolean isCash = method.equals("cash");
        cashPaymentPane.setVisible(isCash);
        cardPaymentPane.setVisible(!isCash);
        posViewModel.setPaymentMethod(method);

        if (isCash) {
            receivedField.requestFocus();
        }
    }

    private void initializeBindings() {
        // Total amount binding
        totalLabel.textProperty().bind(posViewModel.totalOfTotalsProperty().asString("%.2f"));

        // Change calculation binding
        changeLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            try {
                double received = parseReceivedAmount();
                double total = posViewModel.totalOfTotalsProperty().get();
                if (received<total) {
                    return "Не Хватает";
                }
                return String.format("%.2f", Math.max(received - total, 0));

            } catch (NumberFormatException e) {
                return " ";
            }
        }, receivedField.textProperty(), posViewModel.totalOfTotalsProperty()));
    }

    private double parseReceivedAmount() throws NumberFormatException {
        String text = receivedField.getText();
        if (text.isEmpty() || text.equals(".")) return 0.0;
        return Double.parseDouble(text);
    }
    private void setupButtonActions() {
        // Cancel Button Action
        cancelButton.setOnAction(event -> {
            // Close the payment dialog without saving
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });

        // Confirm Button Action
        confirmButton.setOnAction(event -> {
            try {
                // Validate payment method
                String paymentMethod = posViewModel.getPaymentMethod();
                if (paymentMethod == null || paymentMethod.isEmpty()) {
                    showAlert("Ошибка", "Пожалуйста, выберите способ оплаты");
                    return;
                }

                // Validate cash payment
                if (paymentMethod.equals("cash")) {
                    double received = parseReceivedAmount();
                    double total = posViewModel.totalOfTotalsProperty().get();

                    if (received < total) {
                        showAlert("Ошибка", "Полученная сумма меньше общей суммы");
                        return;
                    }
                }
                // Create sale object
                Sale sale = new Sale(
                        paymentMethod,
                        posViewModel.totalOfTotalsProperty().get()
                );

                // Prepare sale data
                List<SaleItem> saleItems = new ArrayList<>();
                ObservableList<SaleItemViewModel> items = posViewModel.getSaleItems();

                for (SaleItemViewModel itemVM : items) {
                    saleItems.add(new SaleItem(
                            sale.getId(),
                            itemVM.getProductId(),
                            itemVM.getQuantity(),
                            itemVM.getPrice()
                    ));
                }

                // Process sale through service
                SaleService saleService = new SaleService();
                boolean success = saleService.addSale(saleItems, sale);

                if (success) {
                    // Clear current sale data
                    posViewModel.getSaleItems().clear();
                    posViewModel.setPaymentMethod(null);

                    // Close the dialog
                    Stage stage = (Stage) confirmButton.getScene().getWindow();
                    posViewModel.resetFields();
                    stage.close();

                    // Show success message
                    showAlert("Успешно", "Продажа успешно завершена");
                } else {
                    showAlert("Ошибка", "Не удалось завершить продажу");
                }

            } catch (Exception e) {
                showAlert("Ошибка", "Произошла ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}