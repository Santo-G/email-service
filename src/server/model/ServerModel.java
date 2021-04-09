package server.model;

import utils.Email;
import java.io.*;
import java.text.*;
import java.util.*;

public class ServerModel {
    private HashMap<String, ArrayList<Email>> serverMailList= new HashMap<>();

    /**
     * Method that initializes the model component with client's .txt files
     */
    public void initModel(String mailClient) {
        switch(mailClient){
            case "client0@mailclient.it":
                String userMail1 = "src/client0@mailclient.it.txt";
                ArrayList<Email> emailListUser = new ArrayList<>();
                String keyUser = "client0@mailclient.it";
                try{
                    BufferedReader br1 = new BufferedReader(new FileReader(userMail1));
                    emailListUser = getMailingList(keyUser, br1);
                    serverMailList.put(keyUser, emailListUser);
                }catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                break;
            case "client1@mailclient.it":
                String userMail2 = "src/client1@mailclient.it.txt";
                ArrayList<Email> emailListUser2 = new ArrayList<>();
                String keyUser2 = "client1@mailclient.it";
                try{
                    BufferedReader br2 = new BufferedReader(new FileReader(userMail2));
                    emailListUser2 = getMailingList(keyUser2, br2);
                    serverMailList.put(keyUser2, emailListUser2);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                break;
            case "client2@mailclient.it":
                String userMail3 = "src/client2@mailclient.it.txt";
                ArrayList<Email> emailListUser3 = new ArrayList<>();
                String keyUser3 = "client2@mailclient.it";
                try {
                    BufferedReader br3 = new BufferedReader(new FileReader(userMail3));
                    emailListUser3 = getMailingList(keyUser3, br3);
                    serverMailList.put(keyUser3, emailListUser3);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                break;
            default:
                String mailInsertedUser= "src/"+mailClient+".txt";
                File f= new File(mailInsertedUser);
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ArrayList<Email> emailListInsertedUser = new ArrayList<>();
                String insertedUser= mailClient;
                try {
                    BufferedReader brInsertedUser= new BufferedReader((new FileReader(mailInsertedUser)));
                    emailListInsertedUser = getMailingList(insertedUser, brInsertedUser);
                    serverMailList.put(insertedUser, emailListInsertedUser);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                break;

        }
    }

    /**
     * Method that returns the mail list of a single client
     */
    public synchronized ArrayList<Email> getEmail(String user) {
        return serverMailList.get(user);
    }

    /**
     * Method that inserts the new mail in the recipient's ArrayList and updates his file
     */
    public synchronized boolean sendMail(Email em) {
        String d = em.getRecipient();
        String[] dest = d.split(",");
        for (int i = 0; i < dest.length; i++) {
            if (!checkEmail(dest[i])) {
                return false;
            } else {
                String file = "src/" + dest[i] + ".txt";
                int id = increaseId(dest[i]);
                em.setId(id);
                try {
                    BufferedWriter br = new BufferedWriter(new FileWriter(file));
                    serverMailList.get(dest[i]).add(em);
                    for (String key : serverMailList.keySet()) {
                        if (dest[i].equals(key)) {
                            ArrayList<Email> list = (ArrayList<Email>) (serverMailList.get(key)).clone();
                            for(Email mail : list) {
                                Date mailForwardingDate = mail.getData();
                                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                String formattedDate = dateFormat.format(mailForwardingDate);
                                br.write(mail.getId() + "§" + mail.getSender() + "§" + mail.getRecipient() + "§"
                                        + mail.getSubject()
                                        + "§" + mail.getText() + "§" + formattedDate + "§" + mail.isRead() + "\n");
                                br.flush();
                            }
                        }
                    }
                    br.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
        return true;
    }

    /**
     * Method that increments mail id
     */
    public synchronized int increaseId(String user) {
        ArrayList<Email> e = serverMailList.get(user);
        int id = 0;
        if (e.isEmpty()) {
            id = 1;
        } else {
            int d = e.get(e.size() - 1).getId();
            id = d + 1;
            return id;
        }
        return id;
    }

    /**
     * Method that marks a single mail as read
     */
    public synchronized void readMail(String user, Email e) {
        ArrayList<Email> list = serverMailList.get(user);
        if (list.contains(e)) {
            for (Email em : list) {
                if (em.getId() == e.getId()) {
                    e.setIsRead(true);
                }
            }
        }
    }

    /**
     * Method that deletes a single mail from a client's file
     */
    public synchronized void deleteMail(String user, Email e) {
        try {
            String file = "src/" + user + ".txt";
            BufferedWriter br = new BufferedWriter(new FileWriter(file));
            ArrayList<Email> em = serverMailList.get(user);
            for (int i = 0; i < em.size(); i++) {
                int id = em.get(i).getId();
                if (id == e.getId()) {
                    em.remove(i);
                    ArrayList<Email> list = (ArrayList<Email>) em.clone();
                    for (Email mail : list) {
                        Date mailForwardingDate = mail.getData();
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        String formattedDate = dateFormat.format(mailForwardingDate);
                        br.write(mail.getId() + "§" + mail.getSender() + "§" + mail.getRecipient() + "§"
                                + mail.getSubject()
                                + "§" + mail.getText() + "§" + formattedDate + "§" + mail.isRead() + "\n");
                        br.flush();
                    }
                    br.close();
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Method that returns the updated mail list
     */
    public synchronized ArrayList<Email> updateMailingList(String user) {
        ArrayList<Email> e = new ArrayList<>();
        String file = "src/" + user + ".txt";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] email = line.split("§");
                Email m;
                boolean read = Boolean.parseBoolean(email[6]);
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date data = format.parse(email[5]);
                int id = Integer.parseInt(email[0]);
                m = new Email(id, email[1], email[2], email[3], email[4], data, read);
                e.add(m);
            }
            br.close();
        } catch (IOException | ParseException ex) {
            System.out.println(ex.getMessage());
        }
        Collections.reverse(e);
        return e;
    }

    /**
     * Method that verifies that a recipient exists
     */
    public boolean checkEmail(String user) {
        boolean check = false;
        for (String key : serverMailList.keySet()) {
            if (user.equals(key)) {
                check = true;
            }
        }
        return check;
    }

    /**
     * Method that reads the .txt file and returns the mail list
     */
    private ArrayList<Email> getMailingList(String keyUser, BufferedReader br) {
        String line;
        ArrayList<Email> mail = new ArrayList<>();
        try {
            while ((line = br.readLine()) != null) {
                String[] email = line.split("§");
                Email m;
                boolean read = Boolean.parseBoolean(email[6]);
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date data = format.parse(email[5]);
                String[] dests = email[2].split(",");
                int id = Integer.parseInt(email[0]);
                m = new Email(id, email[1], email[2], email[3], email[4], data, read);
                mail.add(0,m);
            }
            br.close();
        } catch (IOException | ParseException e) {
            System.out.println(e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No mail found");
        }
        Collections.reverse(mail);
        return mail;
    }
}
