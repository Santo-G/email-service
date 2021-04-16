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
    private Button sendButton;
    @FXML
    private TextField sender;
    @FXML
    private TextField recipient;
    @FXML
    private TextField subject;
    @FXML
    private TextArea mailText;
    private ClientModel model;

    /**
     * Invocated by ClientViewController, this method manages the required action.
     * According to the type of action it compiles mail fields
     */
    public void inizialize(ClientModel model, String name, int id, String from, String to, String subj, String tx, String action) {
        this.model = model;
        try {
            switch (action) {
                case "write":
                    sender.setText(name);
                    sender.setEditable(false);
                    break;
                case "reply":
                    sender.setText(name);
                    sender.setEditable(false);
                    recipient.setText(from);
                    subject.setText("Re: " + subj);
                    recipient.setEditable(false);
                    subject.setEditable(false);
                    break;
                case "forward":
                    sender.setText(name);
                    subject.setText("Fwd: " + subj);
                    mailText.setText(tx);
                    sender.setEditable(false);
                    subject.setEditable(false);
                    mailText.setEditable(false);
                    break;
                case "replyall":
                    sender.setText(name);
                    sender.setEditable(false);
                    String[] aa = to.split(",");
                    if (aa.length == 1) {
                        recipient.setText(from);
                    } else {
                        int iter=0;
                        while(iter<aa.length){
                            if(name.equals(aa[iter])){
                               iter++;
                            }
                            else{
                                recipient.setText(from+","+aa[iter]);
                                recipient.setEditable(false);
                                iter++;
                            }
                        }
                    }
                    subject.setText("Re: " + subj);
                    subject.setEditable(false);
                    break;
            }
            mailText.setWrapText(true);

            sendButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String text = mailText.getText();
                    Date date = Date.from(Instant.now());
                    String recipient= NewMailController.this.recipient.getText();
                    String[] recipients= recipient.split(",");
                    String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
                    Pattern p = Pattern.compile(regex);
                    boolean correct=true;
                    int iter=0;
                    while(correct && iter<recipients.length){
                        Matcher m = p.matcher(recipients[iter]);
                        boolean matchFound = m.matches();
                        if(matchFound==false){
                            correct=false;
                        }
                        iter++;
                    }
                    if (correct) {
                        Email e = new Email(id, sender.getText(), NewMailController.this.recipient.getText(),
                                subject.getText(), text, date, false);
                        boolean emailSended=model.sendMail(e);
                        if(emailSended) {
                            Alert a = new Alert(Alert.AlertType.INFORMATION, "Mail successfully sent!", ButtonType.OK);
                            a.setHeaderText(null);
                            a.show();
                            Stage stage = (Stage) sendButton.getScene().getWindow();
                            stage.close();
                        }

                    }
                    else{
                        Alert a = new Alert(Alert.AlertType.INFORMATION, "Recipient address incorrect", ButtonType.OK);
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

