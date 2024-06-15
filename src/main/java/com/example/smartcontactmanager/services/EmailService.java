package com.example.smartcontactmanager.services;

import java.util.Properties;

import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {
    public  boolean sendEmail(String to , String subject, String message){

        String from = "akhil.barthwal005@gmail.com";

        boolean flag = false;

        // Variable for gmail
        String host = "smtp.gmail.com";  // smtp means simple message transfer protocol.

        //getting System properties
        Properties properties = System.getProperties();
        System.out.println(properties);

        // setting important information to properties object

        properties.put("mail.smtp.host",host);
        properties.put("mail.smtp.port","465");
        properties.put("mail.smtp.ssl.enable","true");
        properties.put("mail.smtp.auth","true");

        // Step 1 : get Session object

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("akhil.barthwal005@gmail.com", "kjmjsdrkzkbpqeop");
            }
        });

        session.setDebug(true); // if any error occur it will show it on terminal


        // Step 2 : compose the message [text,multimedia]

        MimeMessage mimeMessage = new MimeMessage(session);

        try {
            // from email
            mimeMessage.setFrom(from);

            // adding recipient to message
            mimeMessage.addRecipient(Message.RecipientType.TO,new InternetAddress(to)); // if we have to send this mail to multiple address then we have to pass an array and Message.Recipient. now after dot we can use To , CC or more which we used in mail.

            // adding subject to message
            mimeMessage.setSubject(subject);

            // adding text

//            mimeMessage.setText(message);
            mimeMessage.setContent(message,"text/html");



            // Step 3: send the message using Transport class.
            Transport.send(mimeMessage);
            System.out.println("Send success.......");
            flag = true;





        }
        catch (Exception e){
            e.printStackTrace();
        }

        return  flag;
    }
}
