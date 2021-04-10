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
    // private Map<String, ClientRequestHandle> clientOnline = new HashMap<>();
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
    }
}
