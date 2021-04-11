package client.controller;

import utils.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.value.*;
import javafx.collections.ListChangeListener;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import client.model.ClientModel;

public class ClientViewController {

    @FXML
    private SplitPane splitPane;
    @FXML
    private Label a;
    @FXML
    private Label da;
    @FXML
    private Label ogg;
    @FXML
    private Label date;
    @FXML
    private Button scrivi;
    @FXML
    private MenuItem rispondi;
    @FXML
    private MenuItem replyall;
    @FXML
    private MenuItem inoltra;
    @FXML
    private Button elimina;
    @FXML
    private Button logout;
    @FXML
    private TextArea textarea;
    @FXML
    private AnchorPane anchorpanevisualizza;
    @FXML
    private Label clientLabel;
    @FXML
    private TableView<Email> tableview;
    @FXML
    private TableColumn<Email, String> dest;
    @FXML
    private TableColumn<Email, String> mittente;
    @FXML
    private TableColumn<Email, String> oggetto;
    @FXML
    private TableColumn<Email, Date> data;
    private String nome;
    private ClientModel model;
    private int id;

    /** inizialize model and mail */
    public void initialize(ClientModel model, String nome) {
        this.model = model;
        this.nome = nome;

        try {
            tableview.setPlaceholder(new Label("No mail in mailbox"));
            tableview.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            clientLabel.setText(nome);
            model.refresh();
            mittente.setCellValueFactory(new PropertyValueFactory<Email, String>("Mittente"));
            // vengono inizializzate anche la restanti colonne della TableView
            dest.setCellValueFactory(new PropertyValueFactory<Email, String>("Destinatario"));
            oggetto.setCellValueFactory(new PropertyValueFactory<Email, String>("Oggetto"));
            data.setCellFactory(column -> {
                TableCell<Email, Date> cell = new TableCell<Email, Date>() {
                    private SimpleDateFormat format = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm");

                    @Override
                    protected void updateItem(Date item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            this.setText(format.format(item));
                        }
                    }
                };
                return cell;
            });
            data.setCellValueFactory(new PropertyValueFactory<Email, Date>("Data"));

            tableview.getItems().setAll(model.getMail());
            model.addEmailObserver(new ListChangeListener<Email>() {
                @Override
                public void onChanged(Change c) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert a = new Alert(Alert.AlertType.INFORMATION, "Nuove mail da leggere!", ButtonType.OK);
                            a.setHeaderText(null);
                            a.show();
                        }
                    });

                    if (c.next()) {
                        if (c.wasAdded()) {
                            tableview.getItems().setAll(model.getMail());
                        }
                    }
                }
            });

            tableview.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Email>() {
                @Override
                public void changed(ObservableValue<? extends Email> observable, Email oldValue, Email newValue) {
                    anchorpanevisualizza.setDisable(false);
                    textarea.clear();
                    if (newValue != null) {
                        if (!newValue.isRead()) {
                            model.readMail(newValue);
                            newValue.setIsRead(true);
                        }
                        a.setText(newValue.getRecipient());
                        da.setText(newValue.getSender());
                        ogg.setText(newValue.getSubject());
                        Date d = newValue.getData();
                        SimpleDateFormat format = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm");
                        date.setText(format.format(d));
                        textarea.appendText(newValue.getText());
                        textarea.setWrapText(true);
                    } else {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                a.setText("");
                                da.setText("");
                                ogg.setText("");
                                date.setText("");
                                textarea.clear();
                            }
                        });
                    }
                }
            });



            scrivi.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        showNewMailView(event);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            });

            rispondi.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        showReplyView(event);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            });

            replyall.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        showReplyAllView(event);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            });

            inoltra.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        showForwardView(event);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            });

            elimina.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        // richiama deleteMail passando l'elemento selezionato nella tabella
                        model.deleteMail(tableview.getSelectionModel().getSelectedItem());
                        int x = tableview.getSelectionModel().getSelectedIndex();   // si ricava l'indice dell'elemento
                        tableview.getItems().remove(x);     // si rimuove dalla tabella

                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println(e.getMessage());
                    }
                }
            });

            logout.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    model.logout();     // richiama il metodo logout di model e mostra un alert
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Logout effettuato!", ButtonType.OK);
                    a.setHeaderText(null);
                    a.show();
                    Stage stage = (Stage) scrivi.getScene().getWindow();
                    stage.close();
                }
            });
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    /**return mail subject*/
    public String getSubject() {
        String o = " ";
        String og = ogg.getText();
        String[] ogg = og.split(": ");
        for (int i = 0; i < ogg.length; i++) {
            o = ogg[i];
        }
        return o;
    }

    /** this methods show newMail screen and recall initialize's controller method passing parameter
     *
    *  reply ---> default: sender | recipient | subject | actions "reply/reply all"
    *  forward ---> default: sender | subject | action "forward"
    *  new mail --->  default: sender | action "write"   */

    public void showReplyView(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../view/NewMail.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = new Stage();
        scene.getStylesheets().add(getClass().getResource("../view/NewMail.css").toExternalForm());
        window.setScene(scene);
        window.show();
        NewMailController s = loader.getController();
        s.inizialize(model, nome, id, da.getText(), a.getText(), getSubject(), " ", "rispondi");
    }

    public void showNewMailView(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../view/NewMail.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = new Stage();
        scene.getStylesheets().add(getClass().getResource("../view/NewMail.css").toExternalForm());
        window.setScene(scene);
        window.show();
        NewMailController s = loader.getController();
        s.inizialize(model, nome, id, da.getText(), " ", " ", " ", "scrivi");
    }

    public void showForwardView(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../view/NewMail.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = new Stage();
        scene.getStylesheets().add(getClass().getResource("../view/NewMail.css").toExternalForm());
        window.setScene(scene);
        window.show();
        NewMailController s = loader.getController();
        s.inizialize(model, nome, id, da.getText(), " ", getSubject(), textarea.getText(), "inoltra");
    }

    public void showReplyAllView(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../view/NewMail.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = new Stage();
        scene.getStylesheets().add(getClass().getResource("../view/NewMail.css").toExternalForm());
        window.setScene(scene);
        window.show();
        NewMailController s = loader.getController();
        s.inizialize(model, nome, id, da.getText(), a.getText(), getSubject(), " ", "replyall");
    }
}

