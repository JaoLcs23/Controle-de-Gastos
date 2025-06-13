package com.controle.app;

import com.controle.view.MenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Carrega o arquivo FXML do menu principal como a tela inicial
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/MainMenuView.fxml"));
        Parent root = loader.load();

        // Obtém o controlador do FXML carregado
        MenuController menuController = loader.getController();
        // Passa o Stage principal para o controlador do menu para que ele possa gerenciar a troca de telas
        menuController.setPrimaryStage(primaryStage);

        Scene scene = new Scene(root);

        primaryStage.setTitle("Controle de Gastos Pessoais - Menu Principal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}