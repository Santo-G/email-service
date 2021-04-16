package client;

import client.controller.LoginController;
import client.model.ClientModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("view/Login.fxml"));
        Parent root = loader.load();
        stage.setTitle("Mail Client");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        Image icon= new Image("./utils/icon.png");
        stage.getIcons().add(icon);
        scene.getStylesheets().add(getClass().getResource("view/Login.css").toExternalForm());
        stage.show();
        ClientModel model = new ClientModel();
        LoginController controller = loader.getController();
        controller.initialize(model);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

