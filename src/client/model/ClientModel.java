package client.model;

import utils.Email;

import java.io.*;
import java.net.*;
import java.util.*;

import javafx.collections.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ClientModel {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket s;
    private ObservableList<Email> observableEmail = FXCollections.observableArrayList();
    private boolean isConnect;


    /**
     * open connection to server with socket
     */
    public boolean connect(String em) {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            s = new Socket(ip, 1050);
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
            out.writeObject(em);        // invia la mail scelta al server
            out.flush();
            isConnect = in.readBoolean();       // il server invia il boolean connection per dire se è stabilita la connessione
        } catch (IOException e) {
            System.out.println("Connection: " + e.getMessage());
        }
        return isConnect;
    }


    public boolean closeConnection() {
        boolean close = false;
        try {
            if (!s.isClosed()) {
                Object[] obj = {"connection"};
                try {
                    out.writeObject(obj);
                    out.flush();
                    close = true;
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return close;
    }


    /**
     * verify if server is online
     */
    public boolean serverOnline() {
        return this.s != null && this.s.isConnected();  // se la socket non è chiusa ed è connessa ritorna true
    }


    /**
     * load server's mail in observervable List
     */
    public void loadMail() {
        try {
            ArrayList<Email> emails = (ArrayList<Email>) in.readObject();
            Collections.reverse(emails);
            observableEmail.setAll(emails);
        } catch (IOException | ClassNotFoundException es) {
            System.out.println("loadmail: " + es.getMessage());
        }
    }


    /**
     * Return observable list of email
     */
    public ObservableList<Email> getMail() {
        return observableEmail;
    }


    /**
     * add a listener to observable list
     */
    public void addEmailObserver(ListChangeListener<Email> cl) {
        observableEmail.addListener(cl);
    }


    /**
     * send mail passed as parameter
     */
    public boolean sendMail(Email e) {
        Object[] obj = {e, "send"};
        try {
            out.writeObject(obj);
            out.flush();

            return true;
        } catch (IOException ex) {
            serverLost();
            return false;
        }
    }


    /**
     * delete email passed as parameter
     */
    public void deleteMail(Email e) {
        Object[] obj = {e, "delete"};
        try {
            out.writeObject(obj);
            out.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }


    /**
     * set mail as read
     */
    public void readMail(Email e) {
        Object[] obj = {e, "read"};
        try {
            out.writeObject(obj);
            out.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }


    /**
     * this method logout the client: close socket and I/O streams
     */
    public boolean logout() {
        Object[] obj = {"logout"};
        try {
            out.writeObject(obj);
            out.flush();
            isConnect = in.readBoolean();
            if (isConnect) {
                if (s != null && in != null && out != null) {
                    isConnect = false;
                    s.close();
                    in.close();
                    out.close();
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return isConnect;
    }


    /**
     * update mailing list
     */
    public void refresh() {
        loadMail();
        Runnable refresh = new UpdateMailThread(this);
        Thread t = new Thread(refresh);
        t.setDaemon(true);
        t.start();
    }


    /**
     * alert if server is disconnected
     */
    private void serverLost() {
        Alert a = new Alert(Alert.AlertType.ERROR, "Server disconnesso!", ButtonType.CLOSE);
        a.setHeaderText(null);
        a.show();
    }


    /**
     * nested classe that periodically pull mails
     * */
    private class UpdateMailThread extends Thread {

        private ClientModel m;

        public UpdateMailThread(ClientModel m) {
            this.m = m;
        }

        @Override
        public void run() {
            try {
                while (isConnect) {
                    Thread.sleep(2000);     // ogni 2 secondi legge l'arrayList di email dal server
                    ArrayList<Email> email = (ArrayList<Email>) in.readObject();

                    // se la dimensione dell'arrayList è diversa da quella posseduta dal model
                    if (email.size() != observableEmail.size()) {
                        observableEmail.setAll(email);  // imposta l'observableMailList a quella ricevuta dal server
                    }
                }
            } catch (InterruptedException | EOFException e) {
                System.out.println(e.getMessage());
            } catch (IOException | ClassNotFoundException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
