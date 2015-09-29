/*
 * The MIT License
 *
 * Copyright 2015 Mark A. Heckler
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.thehecklers.piremote.comms;

import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.thehecklers.piremote.model.Reading;
import org.thehecklers.piremote.mqtt.MQTTPublisher;

/**
 *
 * @author Mark Heckler (mark.heckler@gmail.com, @mkheck)
 */
public class ReadingPublisher extends MQTTPublisher implements Observer {
    private String serverUri;
    private String clientId;
    private MqttClient client;
    private final MqttConnectOptions connectOptions;
    private final MqttMessage msg;

    public ReadingPublisher(String serverUri, String clientId, 
            String topTopic) {
        super(topTopic);

        this.serverUri = serverUri;
        this.clientId = clientId;

        connectOptions = new MqttConnectOptions();
        msg = new MqttMessage();
        
        try {
            //client = new MqttClient(serverUri, clientId);
            //client.connect();            
            client = new MqttClient(serverUri, topTopic);
            client.connect(connectOptions);
        } catch (MqttException ex) {
            Logger.getLogger(ReadingPublisher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void publish(String msgText) {
        setMessage(msgText);
        publish();
    }

    @Override
    public void publish() {
        try {
            // General message (pre-loaded from publish(String msgText) above in all likelihood
            msg.setPayload(String.valueOf(this.getMessage()).getBytes());
            client.publish(this.getTopTopic(), msg);
        } catch (MqttException ex) {
            Logger.getLogger(ReadingPublisher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void publish(Reading reading) {
        try {
            // Cycle through members of the Reading bean...
            msg.setPayload(String.valueOf(reading.getHum()).getBytes());
            client.publish(this.getTopTopic() + "/" + "hum", msg);
            msg.setPayload(String.valueOf(reading.getTemp()).getBytes());
            client.publish(this.getTopTopic() + "/" + "temp", msg);
            msg.setPayload(String.valueOf(reading.getVolts()).getBytes());
            client.publish(this.getTopTopic() + "/" + "volts", msg);
            msg.setPayload(String.valueOf(reading.getLuminosity()).getBytes());
            client.publish(this.getTopTopic() + "/" + "luminosity", msg);
            msg.setPayload(String.valueOf(reading.getWindDir()).getBytes());
            client.publish(this.getTopTopic() + "/" + "winddir", msg);
            msg.setPayload(String.valueOf(reading.getWindSpeed()).getBytes());
            client.publish(this.getTopTopic() + "/" + "windspeed", msg);
            msg.setPayload(String.valueOf(reading.getRainfall()).getBytes());
            client.publish(this.getTopTopic() + "/" + "rainfall", msg);
            msg.setPayload(String.valueOf(reading.getPressure()).getBytes());
            client.publish(this.getTopTopic() + "/" + "pressure", msg);
            msg.setPayload(String.valueOf(reading.getStatus()).getBytes());
            client.publish(this.getTopTopic() + "/" + "status", msg);
        } catch (MqttException ex) {
            Logger.getLogger(ReadingPublisher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        publish((Reading) arg);
    }
}
