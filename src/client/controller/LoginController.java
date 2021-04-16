package client.controller;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import client.model.ClientModel;

public class LoginController {

    @FXML
    private Button login;
    @FXML
    private ComboBox comboMail;
    @FXML
    private TextField mailField;

    private ClientModel model;
    private ClientViewController mailController;

    public void initialize(ClientModel model) {
        this.model = model;
        comboMail.getItems().addAll("client0@mailclient.it", "client1@mailclient.it", "client2@mailclient.it");
        comboMail.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                login.setDisable(false);
            }
        });
        mailField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                login.setDisable(false);
            }
        });
        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (comboMail.getValue() != null) {
                    model.connect((String) comboMail.getValue());
                    try {
                        showClientView(event);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                    if (!model.serverOnline()) {
                        Alert a = new Alert(Alert.AlertType.ERROR, "Client not connected to the server", ButtonType.OK);
                        a.setHeaderText(null);
                        a.show();
                    }
                } else if (mailField != null) {
                    String mail = mailField.getText();
                    String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(mail);
                    boolean matchFound = m.matches();
                    if (matchFound) {
                        model.connect(mailField.getText());
                        try {
                            showClientView(event);
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }
                        if (!model.serverOnline()) {
                            Alert a = new Alert(Alert.AlertType.ERROR, "Client not connected to the server", ButtonType.OK);
                            a.setHeaderText(null);
                            a.show();
                        }
                    } else {
                        mailField.getStyleClass().add("error");
                        Alert a = new Alert(Alert.AlertType.ERROR, "Incorrect mail. Retry", ButtonType.OK);
                        a.setHeaderText(null);
                        a.show();
                    }
                }
            }
        });
    }

    /**
     * Method that shows mail box and changes Scene
     */
    public void showClientView(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../view/ClientView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        scene.getStylesheets().add(getClass().getResource("../view/ClientView.css").toExternalForm());
        window.setScene(scene);
        window.show();
        mailController = loader.getController();

        if (comboMail.getValue() != null)
            mailController.initialize(model, (String) comboMail.getValue());
        else if (mailField != null)
            mailController.initialize(model, (String) mailField.getText());

        window.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (model.closeConnection()) {
                    Alert al = new Alert(Alert.AlertType.ERROR, "Sudden disconnection", ButtonType.CLOSE);
                    al.setHeaderText(null);
                    al.show();
                }
            }
        });
    }
}

