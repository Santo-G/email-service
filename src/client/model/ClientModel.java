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


    /* Metodo che apre la connesione del socket al server */
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


    /* Metodo che indica se la connessione da parte del client è stata interrotta */
    public boolean closeConnection() {
        boolean close = false;
        try {
            if (!s.isClosed()) {    // se la socket non è chiusa
                Object[] obj = {"connection"};
                try {
                    out.writeObject(obj);   // invia sullo stream output un array di Object contenente la request "connection"
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


    /* Metodo che verifica se il server è online */
    public boolean serverOnline() {
        return this.s != null && this.s.isConnected();  // se la socket non è chiusa ed è connessa ritorna true
    }


    /* Metodo che carica le mail ricevute dal server nell'observable list. */
    public void loadMail() {
        try {
            // prende le mail ricevute dal server e inizializza l'array list con queste ultime
            ArrayList<Email> emails = (ArrayList<Email>) in.readObject();
            Collections.reverse(emails);
            observableEmail.setAll(emails);
        } catch (IOException | ClassNotFoundException es) {
            System.out.println("loadmail: " + es.getMessage());
        }
    }


    /* Metodo che ritorna la lista delle mail. */
    public ObservableList<Email> getMail() {
        return observableEmail;
    }


    /* Metodo che aggiunge un listener all'observable list */
    public void addEmailObserver(ListChangeListener<Email> cl) {
        observableEmail.addListener(cl);
    }


    /* Metodo che permette di inviare una mail. */
    public boolean sendMail(Email e)  {
        Object[] obj = {e, "send"};
        try {
            out.writeObject(obj);   // invia al server un array di object con mail e request "send"
            out.flush();

            return true;
        } catch (IOException ex) {
            serverLost(); // se non riesce l'invio chiama il metodo serverLost che lancia un alert di errore
            return false;
        }
    }


    /* Metodo che permette di cancellare una mail. */
    public void deleteMail(Email e) {
        Object[] obj = {e, "delete"};
        try {
            out.writeObject(obj);
            out.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }


    /* Metodo che richiede al server di impostare come letta una mail non ancora letta. */
    public void readMail(Email e) {
        Object[] obj = {e, "read"};
        try {
            out.writeObject(obj);
            out.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }


    /* Metodo che effettua il logout di un client, chiude gli stream di I/O e il socket. */
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


    /* Metodo che aggiorna la lista delle mail */
    public void refresh() {
        loadMail();     // inizializza l'observableList di mail con quelle ricevute dal server
        Runnable refresh = new UpdateMailThread(this);      // crea un runnable passando se stesso (il model)
        Thread t = new Thread(refresh);     // passa il runnable al Thread
        t.setDaemon(true);      // setta a Daemon il Thread e lo fa partire
        t.start();
    }


    // lancia l'alert "server disconnesso"  ---> richiamato da sendMail se l'invio non è avvenuto
    private void serverLost() {
        Alert a = new Alert(Alert.AlertType.ERROR, "Server disconnesso!", ButtonType.CLOSE);
        a.setHeaderText(null);
        a.show();
    }


    /* Classe innestata che permette periodicamente di effettuare il pool delle mail in arrivo */
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
