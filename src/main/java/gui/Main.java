package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Controller myController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("A STATIC PROGRAM ANALYSIS");
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            myController.closeProgram();
        });

        //Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
        Parent root = loader.load();
        root.getStylesheets().add(getClass().getResource("/gui/stylesheet/myStyleSheet.css").toExternalForm());
        myController = loader.getController();
        myController.setStage(primaryStage);

        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
