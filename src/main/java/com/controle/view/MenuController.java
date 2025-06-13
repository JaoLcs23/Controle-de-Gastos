package com.controle.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MenuController {

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleManageCategories(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/CategoryView.fxml"));
            Parent root = loader.load();

            CategoryController categoryController = loader.getController();
            categoryController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Categorias");
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de categorias: " + e.getMessage());
            e.printStackTrace();
            // TODO: Exibir um alerta para o usuario
        }
    }

    @FXML
    private void handleManageTransactions(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/TransactionView.fxml"));
            Parent root = loader.load();

            TransactionController transactionController = loader.getController();
            transactionController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Transações");
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de transações: " + e.getMessage());
            e.printStackTrace();
            // TODO: Exibir um alerta para o usuario
        }
    }

    //Manipula a ação de navegar para a tela de relatórios
    @FXML
    private void handleViewReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/ReportsView.fxml"));
            Parent root = loader.load();

            ReportsController reportsController = loader.getController();
            reportsController.setPrimaryStage(primaryStage); // Passa o Stage

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Relatórios");
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de relatórios: " + e.getMessage());
            e.printStackTrace();
            // TODO: Exibir um alerta para o usuario
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}