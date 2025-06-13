package com.controle.view;

import com.controle.model.Categoria;
import com.controle.model.TipoCategoria;
import com.controle.service.GastoPessoalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Importar FXMLLoader
import javafx.scene.Parent;   // Importar Parent
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage; // Importar Stage
import java.io.IOException; // Importar IOException

// Controlador da tela de gerenciamento de categorias
public class CategoryController {

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML private TextField categoryNameField;
    @FXML private ComboBox<TipoCategoria> categoryTypeComboBox;
    @FXML private TableView<Categoria> categoryTable;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colName;
    @FXML private TableColumn<Categoria, TipoCategoria> colType;

    private GastoPessoalService service;
    private ObservableList<Categoria> categoriasData = FXCollections.observableArrayList();

    public CategoryController() {
        this.service = new GastoPessoalService();
    }

    @FXML
    public void initialize() {
        categoryTypeComboBox.getItems().setAll(TipoCategoria.values());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colType.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        categoryTable.setItems(categoriasData);

        loadCategories();
    }

    private void loadCategories() {
        categoriasData.clear();
        categoriasData.addAll(service.listarTodasCategorias());
    }

    @FXML
    private void handleAddCategory(ActionEvent event) {
        String name = categoryNameField.getText();
        TipoCategoria type = categoryTypeComboBox.getSelectionModel().getSelectedItem();

        if (name.isEmpty() || type == null) {
            showAlert(Alert.AlertType.WARNING, "Campos Vazios", "Por favor, preencha o nome e selecione o tipo da categoria.");
            return;
        }

        try {
            service.adicionarCategoria(name, type);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Categoria '" + name + "' adicionada com sucesso!");
            categoryNameField.clear();
            categoryTypeComboBox.getSelectionModel().clearSelection();
            loadCategories();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao Adicionar", e.getMessage());
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro Inesperado", "Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // CORREÇÃO AQUI: Implementando a lógica de voltar para o menu principal
    @FXML
    private void handleGoBack(ActionEvent event) {
        try {
            // Carrega o arquivo FXML do menu principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/MainMenuView.fxml"));
            Parent root = loader.load();

            // Obtém o controlador do FXML do menu principal
            MenuController menuController = loader.getController();
            // Passa a referência do Stage principal para o MenuController
            menuController.setPrimaryStage(primaryStage);

            // Cria uma nova cena com o layout do menu principal
            Scene scene = new Scene(root);

            // Define a nova cena no Stage principal
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