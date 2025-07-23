package com.controle.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label; // Adicionado para o fullScreenHintLabel

import java.io.IOException;

public class MenuController extends BaseController { // EXTENDE BASECONTROLLER

    // O atributo 'primaryStage' agora esta na BaseController
    // @FXML protected Label fullScreenHintLabel; // Já esta declarado na BaseController

    // O metodo 'setPrimaryStage' esta na BaseController. Não precisa sobrescrever aqui se não tiver lógica extra.
    // O metodo 'showAlert' foi movido para a BaseController. Chame-o diretamente.

    @FXML
    private void handleManageCategories(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/CategoryView.fxml"));
            Parent root = loader.load();

            CategoryController categoryController = loader.getController();
            categoryController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Categorias");
            primaryStage.show();
            applyFullScreen(); // CHAMA applyFullScreen da BaseController
            showFullScreenHintTemporarily("Pressione ESC para sair.", 3000); // CHAMA showFullScreenHintTemporarily da BaseController
        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de categorias: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela de categorias."); // CHAMA showAlert da BaseController
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
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Transações");
            primaryStage.show();
            applyFullScreen(); // CHAMA applyFullScreen da BaseController
            showFullScreenHintTemporarily("Pressione ESC para sair.", 3000); // CHAMA showFullScreenHintTemporarily da BaseController
        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de transações: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela de transações."); // CHAMA showAlert da BaseController
        }
    }

    @FXML
    private void handleViewReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/ReportsView.fxml"));
            Parent root = loader.load();

            ReportsController reportsController = loader.getController();
            reportsController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Relatórios");
            primaryStage.show();
            applyFullScreen(); // CHAMA applyFullScreen da BaseController
            showFullScreenHintTemporarily("Pressione ESC para sair.", 3000); // CHAMA showFullScreenHintTemporarily da BaseController
        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de relatórios: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela de relatórios."); // CHAMA showAlert da BaseController
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // AQUI: Implementacao do metodo abstrato clearAllErrors da BaseController
    @Override
    protected void clearAllErrors() {
    }
}