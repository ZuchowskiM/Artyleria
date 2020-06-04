package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Artillery Game");
        Scene scene = new Scene(root,550,1000);
        scene.getStylesheets().add("dark-theme.css");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("cannon-icon.png"));

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
