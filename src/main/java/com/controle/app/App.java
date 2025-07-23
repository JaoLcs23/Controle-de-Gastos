package com.controle.app;

import com.controle.view.MenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = null;
        Scene scene = null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/MainMenuView.fxml"));
            root = loader.load();

            MenuController menuController = loader.getController();
            if (menuController != null) {
                menuController.setPrimaryStage(primaryStage);
            } else {
                System.err.println("ERRO: MenuController não foi obtido do FXMLLoader!");
            }

            scene = new Scene(root, 700, 750); // Com largura e altura fixas

            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());

            primaryStage.setTitle("Controle de Gastos Pessoais - Menu Principal");
            primaryStage.setScene(scene); // Define a cena para o palco

            // AQUI: Deixar a janela em modo FULL SCREEN
            primaryStage.show();
            Platform.runLater(() -> primaryStage.setFullScreen(true));

            // AQUI: REMOVE a dica padrao
            // primaryStage.setFullScreenExitHint("ESC para sair.");

        } catch (IOException e) {
            System.err.println("ERRO FATAL: Erro de IO ao carregar FXML ou CSS: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Inicialização");
            alert.setHeaderText("Não foi possível iniciar a aplicação.");
            alert.setContentText("Verifique se os arquivos de interface estão no local correto.\nDetalhes: " + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("ERRO FATAL: Erro inesperado durante a inicialização: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro Inesperado");
            alert.setHeaderText("Um erro inesperado ocorreu ao iniciar.");
            alert.setContentText("Detalhes: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}