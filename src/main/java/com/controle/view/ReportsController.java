package com.controle.view;

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
import java.util.Map;

//Controlador da tela de relatórios financeiros
public class ReportsController {

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML private Label totalBalanceLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TableView<CategorySummary> categoryExpensesTable;
    @FXML private TableColumn<CategorySummary, String> colCategoryName;
    @FXML private TableColumn<CategorySummary, Double> colCategoryAmount;

    private GastoPessoalService service;
    private ObservableList<CategorySummary> categorySummaryData = FXCollections.observableArrayList();

    public ReportsController() {
        this.service = new GastoPessoalService();
    }

    @FXML
    public void initialize() {
        colCategoryName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colCategoryAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        categoryExpensesTable.setItems(categorySummaryData);

        updateTotalBalance();
        // Define as datas padrão para o relatório de categorias
        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        endDatePicker.setValue(LocalDate.now());

        // Carrega os dados reais do banco de dados ao iniciar a tela
        handleGenerateCategoryReport(null);
    }

    //Atualiza o label do balanço total
    private void updateTotalBalance() {
        double balance = service.calcularBalancoTotal();
        totalBalanceLabel.setText(String.format("R$ %.2f", balance));
        if (balance >= 0) {
            totalBalanceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: green;");
        } else {
            totalBalanceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: red;");
        }
    }

    //Gera o relatório de despesas por categoria com base nas datas selecionadas
    @FXML
    private void handleGenerateCategoryReport(ActionEvent event) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        System.out.println("Relatório de Categorias: Data Início = " + startDate + ", Data Fim = " + endDate);

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.WARNING, "Datas Inválidas", "Por favor, selecione um período de datas válido.");
            return;
        }

        try {
            Map<String, Double> expensesMap = service.calcularDespesasPorCategoria(startDate, endDate);

            System.out.println("Relatório de Categorias: Mapa de despesas recebido (tamanho) = " + expensesMap.size());
            expensesMap.forEach((k, v) -> System.out.println("  " + k + ": " + v));

            categorySummaryData.clear();
            expensesMap.forEach((name, amount) -> categorySummaryData.add(new CategorySummary(name, amount)));

            categoryExpensesTable.refresh(); // Força o refresh da tabela

            System.out.println("Relatório de Categorias: categorySummaryData populado (tamanho) = " + categorySummaryData.size());

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao Gerar Relatório", "Ocorreu um erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
        }
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
            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Menu Principal");
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar o menu principal: " + e.getMessage());
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

    // Classe auxiliar para o TableView de resumo de categorias
    public static class CategorySummary {
        private final String categoryName;
        private final Double totalAmount;

        public CategorySummary(String categoryName, Double totalAmount) {
            this.categoryName = categoryName;
            this.totalAmount = totalAmount;
        }

        // Adicionando getters explícitos
        public String getCategoryName() {
            return categoryName;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        @Override
        public String toString() {
            return "CategorySummary{" +
                    "categoryName='" + categoryName + '\'' +
                    ", totalAmount=" + totalAmount +
                    '}';
        }
    }
}