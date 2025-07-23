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
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class TransactionController {

    private Stage primaryStage;
    private int selectedTransactionId = 0;

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

        // Listener para o campo de busca
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadTransactions(newValue); // Recarrega as transações filtradas pelo novo valor
        });

        loadTransactions(""); // Carrega as transacoes iniciais
        setFormMode(false); // Inicializa no modo de adicao
        clearAllErrors(); // Garante que os labels de erro comecem vazios

        // Listener para detectar seleção na tabela de transações
        transactionTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTransactionDetails(newValue));
    }

    //Exibe os detalhes da transação selecionada nos campos de entrada
    private void showTransactionDetails(Transacao transacao) {
        clearAllErrors(); // Limpa erros ao selecionar um item
        if (transacao != null) {
            selectedTransactionId = transacao.getId();
            descriptionField.setText(transacao.getDescricao());
            valueField.setText(String.format(Locale.US, "%.2f", transacao.getValor())); // Usa Locale.US para evitar virgula como decimal
            datePicker.setValue(transacao.getData());
            typeComboBox.getSelectionModel().select(transacao.getTipo());
            categoryComboBox.getSelectionModel().select(transacao.getCategoria());
            setFormMode(true); // Entra no modo de edicao/exclusao
        } else {
            handleNewTransaction(null); // Limpa os campos se nada estiver selecionado
        }
    }

    //Alterna o modo do formulário entre "Adicionar" e "Editar/Excluir"
    private void setFormMode(boolean isEditing) {
        addTransactionButton.setDisable(isEditing);
        updateTransactionButton.setDisable(!isEditing);
        deleteTransactionButton.setDisable(!isEditing);
        newTransactionButton.setDisable(false); // Sempre habilitado
    }

    //Limpa os campos de entrada e redefine o formulário para o modo de adição
    @FXML
    private void handleNewTransaction(ActionEvent event) {
        descriptionField.clear();
        valueField.clear();
        datePicker.setValue(null);
        typeComboBox.getSelectionModel().clearSelection();
        categoryComboBox.getSelectionModel().clearSelection();
        selectedTransactionId = 0; // Reinicia o ID para indicar nova transacao
        transactionTable.getSelectionModel().clearSelection(); // Limpa a selecao da tabela
        setFormMode(false); // Volta para o modo de adicao
        clearAllErrors(); // Limpa erros ao iniciar um novo formulario
        searchField.clear(); // Limpa o campo de busca
        loadTransactions(""); // Recarrega todas as transacoes
    }

    //Adiciona uma nova transação ou atualiza uma existente
    @FXML
    private void handleAddOrUpdateTransaction(ActionEvent event) {
        clearAllErrors(); // Limpa erros anteriores

        String description = descriptionField.getText();
        double value;
        try {
            value = Double.parseDouble(valueField.getText());
        } catch (NumberFormatException e) {
            showFieldError(valueField, valueErrorLabel, "Valor inválido. Use números e '.' para decimais.");
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados.");
            return;
        }
        LocalDate date = datePicker.getValue();
        TipoCategoria type = typeComboBox.getSelectionModel().getSelectedItem();
        Categoria category = categoryComboBox.getSelectionModel().getSelectedItem();

        boolean isValid = true;
        if (description == null || description.trim().isEmpty()) {
            showFieldError(descriptionField, descriptionErrorLabel, "Descrição é obrigatória.");
            isValid = false;
        }
        if (value <= 0) {
            showFieldError(valueField, valueErrorLabel, "Valor deve ser positivo.");
            isValid = false;
        }
        if (date == null) {
            showFieldError(datePicker, dateErrorLabel, "Data é obrigatória.");
            isValid = false;
        } else if (date.isAfter(LocalDate.now())) {
            showFieldError(datePicker, dateErrorLabel, "Data não pode ser futura.");
            isValid = false;
        }
        if (type == null) {
            showFieldError(typeComboBox, typeErrorLabel, "Tipo é obrigatório.");
            isValid = false;
        }
        if (category == null) {
            showFieldError(categoryComboBox, categoryErrorLabel, "Categoria é obrigatória.");
            isValid = false;
        }
        // Validacao adicional: o tipo da transacao deve corresponder ao tipo da categoria
        if (type != null && category != null && category.getTipo() != type) {
            showFieldError(categoryComboBox, categoryErrorLabel, "Tipo da categoria não corresponde ao tipo da transação.");
            isValid = false;
        }

        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados.");
            return;
        }

        try {
            if (selectedTransactionId == 0) { // Modo de Adicao
                service.adicionarTransacao(description, value, date, type, category.getNome());
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação adicionada com sucesso!");
            } else { // Modo de Edicao
                Transacao transacaoAtualizada = new Transacao(selectedTransactionId, description, value, date, type, category);
                service.atualizarTransacao(transacaoAtualizada, category.getNome());
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação atualizada com sucesso!");
            }
            loadTransactions(searchField.getText()); // Recarrega a lista com o filtro atual
            handleNewTransaction(null); // Limpa e volta para o modo de adicao
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro Inesperado", "Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Exclui a transação selecionada da tabela
    @FXML
    private void handleDeleteTransaction(ActionEvent event) {
        if (selectedTransactionId == 0) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Por favor, selecione uma transação na tabela para excluir.");
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
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação excluída com sucesso!");
                loadTransactions(searchField.getText()); // Recarrega a lista com o filtro atual
                handleNewTransaction(null); // Limpa e volta para o modo de adicao
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Erro ao Excluir", "Ocorreu um erro ao excluir a transação: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //Carrega todas as categorias do banco de dados e as adiciona ao ComboBox de categorias
    private void loadCategoriesForComboBox() {
        categoriesList.clear();
        categoriesList.addAll(service.listarTodasCategorias());
        categoryComboBox.setItems(categoriesList);
    }

    //Filtra as categorias no ComboBox de categorias com base no TipoCategoria selecionado
    private void filterCategoriesByType(TipoCategoria selectedType) {
        if (selectedType == null) {
            categoryComboBox.setItems(categoriesList); // Mostra todas se nenhum tipo selecionado
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

    //Carrega todas as transações do banco de dados e as exibe na TableView
    private void loadTransactions(String searchTerm) {
        transactionsData.clear();
        transactionsData.addAll(service.listarTransacoesPorTermo(searchTerm)); // Usa o novo metodo de servico
    }


    //Manipula a ação de voltar para o menu principal
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
        } catch (IOException e) {
            System.err.println("Erro ao carrega" +
                    "r o menu principal: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar o menu principal.");
        }
    }

    //Exibe um alerta para o usuario
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Métodos Auxiliares para Feedback de Validação (Idênticos aos do CategoryController) ---

    //Exibe uma mensagem de erro abaixo de um campo e aplica o estilo de erro
    private void showFieldError(Control control, Label errorLabel, String message) {
        // Verifica se o controle já tem a classe, para não adicionar múltiplas vezes
        if (!control.getStyleClass().contains("text-field-error")) {
            control.getStyleClass().add("text-field-error"); // Adiciona classe CSS de erro
        }
        errorLabel.setText(message);
        errorLabel.setVisible(true); // Torna o label visivel
    }

    //Limpa a mensagem de erro de um campo e remove o estilo de erro
    private void clearFieldError(Control control, Label errorLabel) {
        control.getStyleClass().remove("text-field-error"); // Remove classe CSS de erro
        errorLabel.setText("");
        errorLabel.setVisible(false); // Torna o label invisivel
    }

    //Limpa todos os erros visíveis no formulário de transação
    private void clearAllErrors() {
        clearFieldError(descriptionField, descriptionErrorLabel);
        clearFieldError(valueField, valueErrorLabel);
        clearFieldError(datePicker, dateErrorLabel);
        clearFieldError(typeComboBox, typeErrorLabel);
        clearFieldError(categoryComboBox, categoryErrorLabel);
    }
}