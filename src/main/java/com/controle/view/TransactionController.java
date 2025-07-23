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
import javafx.application.Platform;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class TransactionController extends BaseController { // EXTENDE BASECONTROLLER

    // AQUI: selectedTransactionId fica em TransactionController
    private int selectedTransactionId = 0;

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

    @FXML private Button addTransactionButton;
    @FXML private Button updateTransactionButton;
    @FXML private Button deleteTransactionButton;
    @FXML private Button newTransactionButton;

    @FXML private Label descriptionErrorLabel;
    @FXML private Label valueErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label typeErrorLabel;
    @FXML private Label categoryErrorLabel;

    @FXML private TextField searchField;


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
        colValue.setCellFactory(column -> new TableCell<Transacao, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                    setText(currencyFormat.format(item));
                }
            }
        });

        colDate.setCellValueFactory(new PropertyValueFactory<>("data"));
        colType.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoria"));
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

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadTransactions(newValue);
        });

        loadTransactions("");
        setFormMode(false);
        clearAllErrors(); // CHAMA clearAllErrors da BaseController

        transactionTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTransactionDetails(newValue));
    }

    /**
     * Exibe os detalhes da transacao selecionada nos campos de entrada.
     * @param transacao A transacao selecionada, ou null se a selecao for limpa.
     */
    private void showTransactionDetails(Transacao transacao) {
        clearAllErrors(); // CHAMA clearAllErrors da BaseController
        if (transacao != null) {
            selectedTransactionId = transacao.getId();
            descriptionField.setText(transacao.getDescricao());
            valueField.setText(String.format(Locale.US, "%.2f", transacao.getValor()));
            datePicker.setValue(transacao.getData());
            typeComboBox.getSelectionModel().select(transacao.getTipo());
            categoryComboBox.getSelectionModel().select(transacao.getCategoria());
            setFormMode(true);
        } else {
            handleNewTransaction(null);
        }
    }

    /**
     * Alterna o modo do formulario entre "Adicionar" e "Editar/Excluir".
     * @param isEditing true para modo de edicao/exclusao, false para modo de adicao.
     */
    private void setFormMode(boolean isEditing) {
        addTransactionButton.setDisable(isEditing);
        updateTransactionButton.setDisable(!isEditing);
        deleteTransactionButton.setDisable(!isEditing);
        newTransactionButton.setDisable(false);
    }

    /**
     * Limpa os campos de entrada e redefine o formulario para o modo de adicao.
     * @param event O evento de acao (clique do botao).
     */
    @FXML
    private void handleNewTransaction(ActionEvent event) {
        descriptionField.clear();
        valueField.clear();
        datePicker.setValue(null);
        typeComboBox.getSelectionModel().clearSelection();
        categoryComboBox.getSelectionModel().clearSelection();
        selectedTransactionId = 0;
        transactionTable.getSelectionModel().clearSelection();
        setFormMode(false);
        clearAllErrors(); // CHAMA clearAllErrors da BaseController
        searchField.clear();
        loadTransactions("");
    }


    /**
     * Adiciona uma nova transacao ou atualiza uma existente.
     * O comportamento depende do valor de selectedTransactionId.
     * @param event O evento de acao.
     */
    @FXML
    private void handleAddOrUpdateTransaction(ActionEvent event) {
        clearAllErrors(); // CHAMA clearAllErrors da BaseController

        String description = descriptionField.getText();
        double value;
        try {
            value = Double.parseDouble(valueField.getText());
        } catch (NumberFormatException e) {
            showFieldError(valueField, valueErrorLabel, "Valor inválido. Use números e '.' para decimais."); // CHAMA showFieldError da BaseController
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados."); // CHAMA showAlert da BaseController
            return;
        }
        LocalDate date = datePicker.getValue();
        TipoCategoria type = typeComboBox.getSelectionModel().getSelectedItem();
        Categoria category = categoryComboBox.getSelectionModel().getSelectedItem();

        boolean isValid = true;
        if (description == null || description.trim().isEmpty()) {
            showFieldError(descriptionField, descriptionErrorLabel, "Descrição é obrigatória."); // CHAMA showFieldError da BaseController
            isValid = false;
        }
        if (value <= 0) {
            showFieldError(valueField, valueErrorLabel, "Valor deve ser positivo."); // CHAMA showFieldError da BaseController
            isValid = false;
        }
        if (date == null) {
            showFieldError(datePicker, dateErrorLabel, "Data é obrigatória."); // CHAMA showFieldError da BaseController
            isValid = false;
        } else if (date.isAfter(LocalDate.now())) {
            showFieldError(datePicker, dateErrorLabel, "Data não pode ser futura."); // CHAMA showFieldError da BaseController
            isValid = false;
        }
        if (type == null) {
            showFieldError(typeComboBox, typeErrorLabel, "Tipo é obrigatório."); // CHAMA showFieldError da BaseController
            isValid = false;
        }
        if (category == null) {
            showFieldError(categoryComboBox, categoryErrorLabel, "Categoria é obrigatória."); // CHAMA showFieldError da BaseController
            isValid = false;
        }
        // Validacao adicional: o tipo da transacao deve corresponder ao tipo da categoria
        if (type != null && category != null && category.getTipo() != type) {
            showFieldError(categoryComboBox, categoryErrorLabel, "Tipo da categoria não corresponde ao tipo da transação."); // CHAMA showFieldError da BaseController
            isValid = false;
        }


        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados."); // CHAMA showAlert da BaseController
            return;
        }

        try {
            if (selectedTransactionId == 0) {
                service.adicionarTransacao(description, value, date, type, category.getNome());
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação adicionada com sucesso!"); // CHAMA showAlert da BaseController
            } else {
                Transacao transacaoAtualizada = new Transacao(selectedTransactionId, description, value, date, type, category);
                service.atualizarTransacao(transacaoAtualizada, category.getNome());
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação atualizada com sucesso!"); // CHAMA showAlert da BaseController
            }
            loadTransactions(searchField.getText());
            handleNewTransaction(null);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage()); // CHAMA showAlert da BaseController
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro Inesperado", "Ocorreu um erro inesperado: " + e.getMessage()); // CHAMA showAlert da BaseController
            e.printStackTrace();
        }
    }

    /**
     * Exclui a transacao selecionada da tabela.
     * @param event O evento de acao.
     */
    @FXML
    private void handleDeleteTransaction(ActionEvent event) {
        if (selectedTransactionId == 0) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Por favor, selecione uma transação na tabela para excluir."); // CHAMA showAlert da BaseController
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Exclusão");
        confirmAlert.setHeaderText("Excluir Transação?");
        confirmAlert.setContentText("Tem certeza que deseja excluir a transação selecionada?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.excluirTransacao(selectedTransactionId);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação excluída com sucesso!"); // CHAMA showAlert da BaseController
                loadTransactions(searchField.getText());
                handleNewTransaction(null);
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Erro ao Excluir", "Ocorreu um erro ao excluir a transação: " + e.getMessage()); // CHAMA showAlert da BaseController
                e.printStackTrace();
            }
        }
    }


    /**
     * Carrega todas as categorias do banco de dados e as adiciona ao ComboBox de categorias.
     */
    private void loadCategoriesForComboBox() {
        categoriesList.clear();
        categoriesList.addAll(service.listarTodasCategorias());
        categoryComboBox.setItems(categoriesList);
    }

    /**
     * Filtra as categorias no ComboBox de categorias com base no TipoCategoria selecionado.
     * @param selectedType O tipo de categoria selecionado (RECEITA ou DESPESA).
     */
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
        categoryComboBox.getSelectionModel().clearSelection(); // Limpa a selecao para forcar nova escolha
    }

    /**
     * Carrega todas as transações do banco de dados e as exibe na TableView.
     * NOVO: Agora aceita um termo de busca
     * @param searchTerm Termo para filtrar as transacoes.
     */
    private void loadTransactions(String searchTerm) {
        transactionsData.clear();
        transactionsData.addAll(service.listarTransacoesPorTermo(searchTerm));
    }


    /**
     * Manipula a ação de voltar para o menu principal.
     * @param event O evento de acao.
     */
    @FXML
    private void handleGoBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/MainMenuView.fxml"));
            Parent root = loader.load();

            MenuController menuController = loader.getController();
            menuController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm()); // Carregando CSS
            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Menu Principal");
            primaryStage.show();
            Platform.runLater(() -> primaryStage.setMaximized(true)); // Reaplicar maximizacao
        } catch (IOException e) {
            System.err.println("Erro ao carregar o menu principal: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar o menu principal.");
        }
    }

    /**
     * Exibe um alerta para o usuario.
     * @param type Tipo do alerta (WARNING, INFORMATION, ERROR).
     * @param title Titulo do alerta.
     * @param message Mensagem do alerta.
     */
    public void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Métodos Auxiliares para Feedback de Validação (Idênticos aos do CategoryController) ---

    /**
     * Exibe uma mensagem de erro abaixo de um campo e aplica o estilo de erro.
     * @param control O controle (TextField, ComboBox, DatePicker) que tem o erro.
     * @param errorLabel O Label onde a mensagem de erro sera exibida.
     * @param message A mensagem de erro.
     */
    public void showFieldError(Control control, Label errorLabel, String message) {
        // Verifica se o controle já tem a classe, para não adicionar múltiplas vezes
        if (!control.getStyleClass().contains("text-field-error")) {
            control.getStyleClass().add("text-field-error");
        }
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Limpa a mensagem de erro de um campo e remove o estilo de erro.
     * @param control O controle (TextField, ComboBox, DatePicker) cujo erro deve ser limpo.
     * @param errorLabel O Label associado a mensagem de erro.
     */
    public void clearFieldError(Control control, Label errorLabel) {
        control.getStyleClass().remove("text-field-error");
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    /**
     * Limpa todos os erros visíveis no formulário de transação.
     */
    public void clearAllErrors() {
        clearFieldError(descriptionField, descriptionErrorLabel);
        clearFieldError(valueField, valueErrorLabel);
        clearFieldError(datePicker, dateErrorLabel);
        clearFieldError(typeComboBox, typeErrorLabel);
        clearFieldError(categoryComboBox, categoryErrorLabel);
    }
}