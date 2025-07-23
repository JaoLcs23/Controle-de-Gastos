package com.controle.view;

import com.controle.model.Categoria;
import com.controle.model.TipoCategoria;
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
import javafx.application.Platform;

import java.io.IOException;
import java.util.Optional;

public class CategoryController extends BaseController { // EXTENDE BASECONTROLLER

    // AQUI: selectedCategoryId fica em CategoryController
    private int selectedCategoryId = 0;

    @FXML private TextField categoryNameField;
    @FXML private ComboBox<TipoCategoria> categoryTypeComboBox;
    @FXML private TableView<Categoria> categoryTable;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colName;
    @FXML private TableColumn<Categoria, TipoCategoria> colType;

    @FXML private Button addCategoryButton;
    @FXML private Button updateCategoryButton;
    @FXML private Button deleteCategoryButton;
    @FXML private Button newCategoryButton;

    @FXML private Label categoryNameErrorLabel;
    @FXML private Label categoryTypeErrorLabel;

    @FXML private TextField searchField;


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

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadCategories(newValue);
        });

        loadCategories("");
        setFormMode(false);
        clearAllErrors(); // CHAMA clearAllErrors() implementado aqui

        categoryTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showCategoryDetails(newValue));
    }

    private void showCategoryDetails(Categoria categoria) {
        clearAllErrors();
        if (categoria != null) {
            selectedCategoryId = categoria.getId();
            categoryNameField.setText(categoria.getNome());
            categoryTypeComboBox.getSelectionModel().select(categoria.getTipo());
            setFormMode(true);
        } else {
            handleNewCategory(null);
        }
    }

    private void setFormMode(boolean isEditing) {
        addCategoryButton.setDisable(isEditing);
        updateCategoryButton.setDisable(!isEditing);
        deleteCategoryButton.setDisable(!isEditing);
        newCategoryButton.setDisable(false);
    }

    @FXML
    private void handleNewCategory(ActionEvent event) {
        categoryNameField.clear();
        categoryTypeComboBox.getSelectionModel().clearSelection();
        selectedCategoryId = 0;
        categoryTable.getSelectionModel().clearSelection();
        setFormMode(false);
        clearAllErrors();
        searchField.clear();
        loadCategories("");
    }

    @FXML
    private void handleAddOrUpdateCategory(ActionEvent event) {
        clearAllErrors();

        String name = categoryNameField.getText();
        TipoCategoria type = categoryTypeComboBox.getSelectionModel().getSelectedItem();

        boolean isValid = true;
        if (name == null || name.trim().isEmpty()) {
            showFieldError(categoryNameField, categoryNameErrorLabel, "Nome da categoria é obrigatório.");
            isValid = false;
        }
        if (type == null) {
            showFieldError(categoryTypeComboBox, categoryTypeErrorLabel, "Selecione o tipo da categoria.");
            isValid = false;
        }

        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados.");
            return;
        }

        try {
            if (selectedCategoryId == 0) {
                service.adicionarCategoria(name, type);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Categoria '" + name + "' adicionada com sucesso!");
            } else {
                Categoria categoriaAtualizada = new Categoria(selectedCategoryId, name, type);
                service.atualizarCategoria(categoriaAtualizada);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Categoria '" + name + "' atualizada com sucesso!");
            }
            loadCategories(searchField.getText());
            handleNewCategory(null);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro Inesperado", "Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteCategory(ActionEvent event) {
        if (selectedCategoryId == 0) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Por favor, selecione uma categoria na tabela para excluir.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Exclusão");
        confirmAlert.setHeaderText("Excluir Categoria?");
        confirmAlert.setContentText("Tem certeza que deseja excluir a categoria selecionada?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.excluirCategoria(selectedCategoryId);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Categoria excluída com sucesso!");
                loadCategories(searchField.getText());
                handleNewCategory(null);
            }
            catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Erro ao Excluir", "Ocorreu um erro ao excluir a categoria: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadCategories(String searchTerm) {
        categoriasData.clear();
        categoriasData.addAll(service.listarCategoriasPorTermo(searchTerm));
    }

    @FXML
    private void handleGoBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/MainMenuView.fxml"));
            Parent root = loader.load();

            MenuController menuController = loader.getController();
            menuController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Menu Principal");
            primaryStage.show();
            applyFullScreen(); // CHAMA applyFullScreen da BaseController
            showFullScreenHintTemporarily("Pressione ESC para sair.", 3000); // CHAMA showFullScreenHintTemporarily da BaseController
        } catch (IOException e) {
            System.err.println("Erro ao carregar o menu principal: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar o menu principal.");
        }
    }

    // AQUI: Implementacao do metodo abstrato clearAllErrors da BaseController
    @Override
    protected void clearAllErrors() {
        clearFieldError(categoryNameField, categoryNameErrorLabel);
        clearFieldError(categoryTypeComboBox, categoryTypeErrorLabel);
    }
}