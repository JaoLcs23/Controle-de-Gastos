package com.controle.view;

import com.controle.model.Categoria;
import com.controle.model.TipoCategoria;
import com.controle.model.Transacao;
import com.controle.service.GastoPessoalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class TransactionController {

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML private TextField descriptionField;
    @FXML private TextField valueField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<TipoCategoria> typeComboBox;
    @FXML private ComboBox<Categoria> categoryComboBox;
    @FXML private TableView<Transacao> transactionTable;
    @FXML private TableColumn<Transacao, Integer> colId;
    @FXML private TableColumn<Transacao, String> colDescription;
    @FXML private TableColumn<Transacao, Double> colValue;
    @FXML private TableColumn<Transacao, LocalDate> colDate;
    @FXML private TableColumn<Transacao, TipoCategoria> colType;
    @FXML private TableColumn<Transacao, Categoria> colCategory;

    private GastoPessoalService service;
    private ObservableList<Transacao> transactionsData = FXCollections.observableArrayList();
    private ObservableList<Categoria> categoriesList = FXCollections.observableArrayList();

    public TransactionController() {
        this.service = new GastoPessoalService();
    }

    @FXML
    public void initialize() {
        typeComboBox.getItems().setAll(TipoCategoria.values());

        loadCategoriesForComboBox();

        typeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterCategoriesByType(newValue);
        });

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("data"));
        colType.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoria")); // Pega o objeto Categoria
        colCategory.setCellFactory(column -> new TableCell<Transacao, Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNome());
                }
            }
        });

        transactionTable.setItems(transactionsData);

        loadTransactions();
    }

    private void loadCategoriesForComboBox() {
        categoriesList.clear();
        categoriesList.addAll(service.listarTodasCategorias());
        categoryComboBox.setItems(categoriesList);
    }

    private void filterCategoriesByType(TipoCategoria selectedType) {
        if (selectedType == null) {
            categoryComboBox.setItems(categoriesList);
            return;
        }
        ObservableList<Categoria> filteredList = FXCollections.observableArrayList();
        for (Categoria cat : categoriesList) {
            if (cat.getTipo() == selectedType) {
                filteredList.add(cat);
            }
        }
        categoryComboBox.setItems(filteredList);
        categoryComboBox.getSelectionModel().clearSelection();
    }

    private void loadTransactions() {
        transactionsData.clear();
        transactionsData.addAll(service.listarTodasTransacoes());
    }

    @FXML
    private void handleAddTransaction(ActionEvent event) {
        String description = descriptionField.getText();
        double value;
        try {
            value = Double.parseDouble(valueField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Valor Inválido", "Por favor, insira um valor numérico válido.");
            return;
        }
        LocalDate date = datePicker.getValue();
        TipoCategoria type = typeComboBox.getSelectionModel().getSelectedItem();
        Categoria category = categoryComboBox.getSelectionModel().getSelectedItem();

        if (description.isEmpty() || date == null || type == null || category == null) {
            showAlert(Alert.AlertType.WARNING, "Campos Vazios", "Por favor, preencha todos os campos e selecione o tipo e a categoria.");
            return;
        }

        try {
            service.adicionarTransacao(description, value, date, type, category.getNome());
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação adicionada com sucesso!");
            clearFields();
            loadTransactions();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao Adicionar", e.getMessage());
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro Inesperado", "Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        descriptionField.clear();
        valueField.clear();
        datePicker.setValue(null);
        typeComboBox.getSelectionModel().clearSelection();
        categoryComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleGoBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/MainMenuView.fxml"));
            Parent root = loader.load();

            MenuController menuController = loader.getController();
            menuController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Menu Principal");
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar o menu principal: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar o menu principal.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}