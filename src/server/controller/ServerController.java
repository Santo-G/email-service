package server.controller;

import utils.Email;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import javafx.event.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.*;
import server.model.ServerModel;

public class ServerController {

    @FXML
    private TextArea areaclient;
    @FXML
    private Button cleanLog;
    @FXML
    private AnchorPane anchor;
    private ServerModel serverModel;
    private ServerSocket serverSocket;
    private int numActiveClients = 0;
    private Map<String, ClientRequestHandle> clientOnline = new HashMap<>();
    private boolean connection;

    Date date = Date.from(Instant.now());
    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    String actualDate = dateFormat.format(date);

    /**
     * Method that opens server socket and manages connection
     */
    public void connect() {
        try {
            serverSocket = new ServerSocket(1050);
            areaclient.appendText("[" + actualDate + "]" + " Server connected: " + serverSocket + "\n");
            areaclient.appendText("[" + actualDate + "]" + " Active clients: " + numActiveClients + "\n");
            Runnable r = new ClientAccept(serverSocket);
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            areaclient.appendText("Server already online!");
        }
    }

    /**
     * Thread that accepts connections of incoming clients
     */
    private class ClientAccept extends Thread {
        ServerSocket serverSocket;

        public ClientAccept(ServerSocket socket) {
            this.serverSocket = socket;
        }

        @Override
        public void run() {
            Socket s = null;
            connection = true;
            try {
                while (connection) {
                    s = serverSocket.accept();
                    Runnable r = new server.controller.ServerController.ClientRequestHandle(s);
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.start();
                    if (s.isClosed()) {
                        connection = false;
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }

    /**
     * Thread that manages every client's request
     */
    private class ClientRequestHandle extends Thread {
        private String user;
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public ClientRequestHandle(Socket s) throws IOException {
            this.socket = s;
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            try {
                String mailclient = (String) in.readObject();
                serverModel.initModel(mailclient);
                out.writeBoolean(connection);
                out.flush();
                user = mailclient;
                String request = "";
                date = Date.from(Instant.now());
                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                actualDate = dateFormat.format(date);
                clientOnline.put(user, this);
                numActiveClients++;
                areaclient.appendText("[" + actualDate + "]" + " Active clients: " + numActiveClients + "\n");
                areaclient.appendText("[" + actualDate + "]" + " Client connected: " + mailclient + " " + socket + "\n");
                out.writeObject(serverModel.getEmail(mailclient));
                out.flush();
                while (!request.equals("logout")) {
                    Object[] obj = (Object[]) in.readObject();
                    if (obj.length == 1) {
                        request = (String) obj[0];
                        switch (request) {
                            case "logout":
                                date = Date.from(Instant.now());
                                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                actualDate = dateFormat.format(date);
                                clientOnline.remove(user);
                                areaclient.appendText("[" + actualDate + "]" + " " + mailclient + ": Client disconnected" + "\n");
                                numActiveClients--;
                                break;
                            case "connection":
                                date = Date.from(Instant.now());
                                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                actualDate = dateFormat.format(date);
                                clientOnline.remove(user);
                                areaclient.appendText("[" + actualDate + "]" + " " + mailclient + ": Sudden disconnection" + "\n");
                                numActiveClients--;
                                break;
                        }
                    } else if (obj.length == 2) {
                        request = (String) obj[1];
                        switch (request) {
                            case "read":
                                date = Date.from(Instant.now());
                                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                actualDate = dateFormat.format(date);
                                serverModel.readMail(mailclient, (Email) obj[0]);
                                areaclient.appendText("[" + actualDate + "]" + " " + mailclient + ":" + " Mail " + obj[0] + " read" + "\n");
                                break;
                            case "send":
                                date = Date.from(Instant.now());
                                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                actualDate = dateFormat.format(date);
                                Email m = (Email) obj[0];
                                String d = m.getRecipient();
                                String[] dest = d.split(",");
                                if (serverModel.sendMail(m)) {
                                    areaclient.appendText("[" + actualDate + "]" + " " + mailclient + ":" + " Mail " + obj[0] + " sent " + "\n");
                                    for (int i = 0; i < dest.length; i++) {
                                        areaclient.appendText("[" + actualDate + "]" + " " + dest[i] + ":" + " Mail " + obj[0] + " received" + "\n");
                                        server.controller.ServerController.ClientRequestHandle cl;
                                        cl = clientOnline.get(dest[i]);
                                        if (cl != null) {
                                            cl.updateClientMailList();
                                            areaclient.appendText("[" + actualDate + "]" + " " + cl.user + ":" + " New mails avaiable" + "\n");
                                        }
                                    }
                                } else {
                                    areaclient.appendText("[" + actualDate + "]" + " " + mailclient + ":" + " Recipient " + d + " non existent " + "\n");
                                    areaclient.appendText("[" + actualDate + "]" + " " + mailclient + ":" + " Mail " + obj[0] + " not sent" + "\n");
                                }
                                break;
                            case "delete":
                                date = Date.from(Instant.now());
                                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                actualDate = dateFormat.format(date);
                                serverModel.deleteMail(mailclient, (Email) obj[0]);
                                areaclient.appendText("[" + actualDate + "]" + " " + mailclient + ":" + " Mail " + obj[0] + "  deleted" + "\n");
                                break;
                        }
                    }
                }
                date = Date.from(Instant.now());
                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                actualDate = dateFormat.format(date);
                areaclient.appendText("[" + actualDate + "]" + " Active clients: " + numActiveClients + "\n");
                out.writeBoolean(connection);
                out.flush();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            } catch (ClassNotFoundException ex) {
                System.out.println(ex.getException());
            } finally {
                try {
                    if (socket != null && in != null && out != null) {
                        socket.close();
                        in.close();
                        out.close();
                    }
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }

        public void updateClientMailList() {
            try {
                ArrayList<Email> e = serverModel.updateMailingList(user);
                out.writeObject(e);
                out.flush();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

}
