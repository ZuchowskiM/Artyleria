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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Artillery Game");
        Scene scene = new Scene(root,550,1000);
        scene.getStylesheets().add("dark-theme.css");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("cannon-icon.png"));

        Controller controller = loader.getController();
        controller.setMainStage(primaryStage);

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
