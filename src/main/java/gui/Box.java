package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

class Box {

    private static boolean confirmAnswer;
    static void displayAlertBox(String aTitle, String aMessage) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(aTitle);
        window.setMinWidth(300);
        window.setMinHeight(200);

        Label message = new Label();
        message.setText(aMessage);

        Button clsoeButton = new Button("Close");
        clsoeButton.setOnAction(e -> window.close());

        VBox layout = new VBox(10);
        layout.setSpacing(20);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(message, clsoeButton);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.setResizable(false);
        window.show();
        //window.showAndWait();
    }

    static boolean displayConfirmBox(String aTitle, String aMessage) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(aTitle);
        window.setMinWidth(300);
        window.setMinHeight(200);

        Button yesButton = new Button("YES");
        Button noButton = new Button("NO");
        yesButton.setOnAction(e -> {
            confirmAnswer = true;
            window.close();
        });
        noButton.setOnAction(e -> {
            confirmAnswer = false;
            window.close();
        });

        Label message = new Label();
        message.setText(aMessage);


        VBox layoutText = new VBox(10);
        HBox layoutButton = new HBox(10);
        layoutButton.setPadding(new Insets(20, 0, 0, 0));
        layoutButton.getChildren().addAll(yesButton, noButton);
        layoutText.getChildren().addAll(message, layoutButton);
        layoutText.setAlignment(Pos.CENTER);
        layoutButton.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layoutText);
        window.setScene(scene);
        window.setResizable(false);
        window.showAndWait();

        return confirmAnswer;
    }

}
