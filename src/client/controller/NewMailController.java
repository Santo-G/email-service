package client.controller;

import utils.Email;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.event.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import client.model.ClientModel;

public class NewMailController {

    @FXML
    private Button invioButton;
    @FXML
    private TextField mittente;
    @FXML
    private TextField destinatario;
    @FXML
    private TextField oggetto;
    @FXML
    private TextArea testoMail;
    private ClientModel model;


    /**
     * invocated by ClientViewController. This method manage the required action.
     * According to action's type compile mail's fields making editable or not
     * Exemple: in reply you couldn't write a recipient
     * In all cases, sender is not editable
     */

    public void inizialize(ClientModel model, String nome, int id, String da, String a, String ogg, String t, String action) {
        this.model = model;
        try {
            switch (action) {
                case "scrivi":
                    mittente.setText(nome);
                    mittente.setEditable(false);
                    break;
                case "rispondi":
                    mittente.setText(nome);
                    mittente.setEditable(false);
                    destinatario.setText(da);
                    oggetto.setText("Re: " + ogg);
                    destinatario.setEditable(false);
                    oggetto.setEditable(false);
                    break;
                case "inoltra":
                    mittente.setText(nome);
                    oggetto.setText("Fwd: " + ogg);
                    testoMail.setText(t);
                    mittente.setEditable(false);
                    oggetto.setEditable(false);
                    testoMail.setEditable(false);
                    break;
                case "replyall":
                    mittente.setText(nome);
                    mittente.setEditable(false);
                    String[] aa = a.split(",");
                    if (aa.length == 1) {
                        destinatario.setText(da);
                    } else {
                        int iter=0;
                        while(iter<aa.length){
                            if(nome.equals(aa[iter])){
                               iter++;
                            }
                            else{
                                destinatario.setText(da+","+aa[iter]);
                                destinatario.setEditable(false);
                                iter++;
                            }
                        }

                    }
                    oggetto.setText("Re: " + ogg);
                    oggetto.setEditable(false);
                    break;
            }
            testoMail.setWrapText(true);

            invioButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String testo = testoMail.getText();
                    Date d = Date.from(Instant.now());
                    String dest= destinatario.getText();
                    String[] destinatari= dest.split(",");
                    String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
                    Pattern p = Pattern.compile(regex);
                    boolean correct=true;
                    int iter=0;
                    // controlla se ogni destinatario inserito rispetta il pattern
                    while(correct && iter<destinatari.length){
                        Matcher m = p.matcher(destinatari[iter]);
                        boolean matchFound = m.matches();
                        if(matchFound==false){      // se la mail è sbagliata viene settato il boolean
                            correct=false;
                        }
                        iter++;
                    }

                    if (correct) {
                        Email e = new Email(id, mittente.getText(), destinatario.getText(),
                                oggetto.getText(), testo, d, false);
                        boolean emailSended=model.sendMail(e);
                        if(emailSended) {
                            Alert a = new Alert(Alert.AlertType.INFORMATION, "Mail inviata con successo!", ButtonType.OK);
                            a.setHeaderText(null);
                            a.show();
                            Stage stage = (Stage) invioButton.getScene().getWindow();
                            stage.close();
                        }

                    }
                    else{
                        Alert a = new Alert(Alert.AlertType.INFORMATION, "Indirizzo destinatario non corretto", ButtonType.OK);
                        a.setHeaderText(null);
                        a.show();
                    }
                }
            });
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }
}

