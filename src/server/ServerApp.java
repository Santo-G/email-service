package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import server.model.ServerModel;

public class ServerApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/ServerView.fxml"));
        Parent root = (Parent) loader.load();
        stage.setTitle("Mail Server");
        Image icon= new Image("./utils/server-icon.png");
        stage.getIcons().add(icon);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("view/ServerView.css").toExternalForm());
        stage.show();
        ServerModel model = new ServerModel();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

