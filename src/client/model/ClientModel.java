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
     * Method that opens connection to server
     */
    public boolean connect(String em) {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            s = new Socket(ip, 1050);
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
            out.writeObject(em);
            out.flush();
            isConnect = in.readBoolean();
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
     * Method that verifies server status
     */
    public boolean serverOnline() {
        return this.s != null && this.s.isConnected();
    }

    /**
     * Method that loads server mail list
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
     * Method that returns observable list of email
     */
    public ObservableList<Email> getMail() {
        return observableEmail;
    }

    /**
     * Method that add a listener to observable list
     */
    public void addEmailObserver(ListChangeListener<Email> cl) {
        observableEmail.addListener(cl);
    }

    /**
     * Method that sends mail
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
     * Method that deletes email
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
     * Method that sets a single mail as read
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
     * Method that logouts the client: close socket and I/O streams
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
     * Method that updates mailing list
     */
    public void refresh() {
        loadMail();
        Runnable refresh = new UpdateMailThread(this);
        Thread t = new Thread(refresh);
        t.setDaemon(true);
        t.start();
    }


    /**
     * Method that sends an alert if server is disconnected
     */
    private void serverLost() {
        Alert a = new Alert(Alert.AlertType.ERROR, "Server disconnected!", ButtonType.CLOSE);
        a.setHeaderText(null);
        a.show();
    }


    /**
     * Nested class that periodically pulls mails
     */
    private class UpdateMailThread extends Thread {
        private ClientModel m;
        public UpdateMailThread(ClientModel m) {
            this.m = m;
        }

        @Override
        public void run() {
            try {
                while (isConnect) {
                    Thread.sleep(2000);
                    ArrayList<Email> email = (ArrayList<Email>) in.readObject();
                    if (email.size() != observableEmail.size()) {
                        observableEmail.setAll(email);
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
