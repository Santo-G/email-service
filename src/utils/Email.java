package utils;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Email implements Serializable {

    private int id;
    private String sender, recipient, subject, text;
    private Date data;
    private boolean isRead;

    public Email(int id, String m, String d, String a, String t, Date dt, boolean isRead) {
        this.id = id;
        this.sender = m;
        this.recipient = d;
        this.subject = a;
        this.text = t;
        this.data = dt;
        this.isRead = isRead;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getDate() {
        String date = " ";
        DateFormat format = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm");
        Date d = getData();
        date = format.format(d);
        return date;
    }

    @Override
    public String toString() {
        return getSender() + " " + getSubject() + " " + getRecipient() + " " + getText() + " " + getDate();
    }
}

