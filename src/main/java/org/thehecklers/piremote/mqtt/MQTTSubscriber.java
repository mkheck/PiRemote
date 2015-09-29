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
package org.thehecklers.piremote.mqtt;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author Mark Heckler (mark.heckler@gmail.com, @mkheck)
 */
public class MQTTSubscriber implements MqttCallback {
    private MqttAsyncClient client;
    private final String address;
    private byte[] payLoad;
    private boolean isImageReady = false;

//    public MQTTSubscriber(ImageView iView) {
    public MQTTSubscriber(String address) {
        this.address = address;
        try {
            client = new MqttAsyncClient("tcp://localhost:1883", "videoviewport");
            client.setCallback(this);
            client.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken imt) {
                    try {
                        client.subscribe("a4jvideodata/image", 0);
                    } catch (MqttException ex) {
                        Logger.getLogger(MQTTSubscriber.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                @Override
                public void onFailure(IMqttToken imt, Throwable thrwbl) {
                    System.out.println("Unable to connect VideoViewport client: " + thrwbl.getLocalizedMessage());
                }                
            });
        } catch (MqttException ex) {
            //Logger.getLogger(A4jVideoViewport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void connectionLost(Throwable thrwbl) {
        System.out.println("Connection lost. Error: " + thrwbl.getLocalizedMessage());
    }

    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception {
        payLoad = mm.getPayload();
    }
    
    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        System.out.println("Message delivery complete.");
    }    
}
