/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.uniroma1.msecs.ay2019.e3;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author biar
 */
public class FlightListener implements MessageListener {
    private TopicConnection connection;
    private final static Pattern statusPattern = Pattern.compile(".* : (.*)");


    public FlightListener() {
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            Context ctx = new InitialContext(props);

            ConnectionFactory connectionFactory = (ConnectionFactory)ctx.lookup("ConnectionFactory");
            connection = (TopicConnection)connectionFactory.createConnection();
            TopicSession session = (TopicSession)connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = (Destination)ctx.lookup("dynamicTopics/Flights");
            TopicSubscriber subscriber = session.createSubscriber((Topic)destination);
            subscriber.setMessageListener(this);
        } catch (JMSException | NamingException err) {
            err.printStackTrace();
        }
    }


    public void onMessage(Message msg) {
        try {
            String flight = msg.getStringProperty("flight");
            String text = ((TextMessage)msg).getText();

            Matcher matcher = statusPattern.matcher(text);
            if (matcher.find()) {
                String status = matcher.group(1);
                System.out.println(String.format("%s -> %s", flight, status));
            }
        } catch (JMSException err) {
            err.printStackTrace();
        }
    }


    public void start() {
        try {
            connection.start();
        } catch (JMSException err) {
            err.printStackTrace();
        }
    }


    public void stop() {
        try {
            connection.stop();
        } catch (JMSException err) {
            err.printStackTrace();
        }
    }
}
