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
package org.thehecklers.piremote.ws;

import org.thehecklers.piremote.PiRemote;
import org.thehecklers.piremote.model.Reading;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.thehecklers.piremote.PiRemote.logIt;

/**
 * WSDataClient connects with Cloud application service via WebSocket. It
 * provides the means by which to send readings from PiRemote to the Cloud.
 *
 * @author Mark Heckler - 2015
 */
@ClientEndpoint
public class WsDataClient implements Observer {
    private Session session = null;
    private WebSocketContainer container;
    private boolean isConnected = false;
    private String uriWeb;

    public WsDataClient() {
        // No-parameter constructor for testing
        //logIt("In no parameter constructor.");
    }
    
    public WsDataClient(String uriWeb) {
        this.uriWeb = uriWeb.endsWith("/") ? uriWeb + "data" : uriWeb + "/" + "data";
        try {
            connectToWebSocketServer();
        } catch (Exception e) {
            logIt("Error connecting to Data WebSocketServer: " + e.getLocalizedMessage());
        }
    }

    private void connectToWebSocketServer() throws Exception {
        try {
            container = ContainerProvider.getWebSocketContainer();

            logIt("Connecting to " + uriWeb);
            session = container.connectToServer(WsDataClient.class, URI.create(uriWeb));
        } catch (Exception e) {
            //(IOException | DeploymentException | IllegalStateException e)
            logIt("Error connecting, " + uriWeb + ": " + e.getLocalizedMessage());
            isConnected = false;
            return;
        }
        
        isConnected = true;
        logIt("Data WebSocket connected: " + uriWeb);
    }

    public void disconnect() {
        if (session != null) {
            if (session.isOpen()) {
                try {
                    session.close();
                } catch (IOException ex) {
                    Logger.getLogger(PiRemote.class.getName()).log(Level.SEVERE, null, ex);
                }
                logIt("Disconnecting: Data WebSocket session now closed");
            } else {
                logIt("Disconnecting: Data WebSocket session was already closed");
            }
        }
    }
    
    @OnOpen
    public void onOpen(Session session) {
        logIt("Data WebSocket connected to endpoint: " + session.getBasicRemote());
    }
 
    @OnMessage
    public void onMessage(String message) {
        logIt(message);
    }

    @OnError
    public void onError(Throwable t) {
        logIt("Error in Data WebSocketController: " + t.toString());
    }

    @Override
    public void update(Observable o, Object arg) {
        if (isConnected) {
            try {
                String message = ((Reading) arg).toJson();
                if (session != null
                        && session.isOpen()
                        && session.getBasicRemote() != null) {
                    // WebSocket send to server
                    session.getBasicRemote().sendText(message);
                    isConnected = true;
                } else {
                    if (session == null) {
                        logIt("ERROR posting reading to Data WebSocket with session NULL");
                    } else {
                        if (!session.isOpen()) {
                            logIt("ERROR posting reading to Data WebSocket with session CLOSED");
                        } else {
                            if (session.getBasicRemote() == null) {
                                logIt("ERROR posting reading to Data WebSocket with BasicRemote NULL");
                            }
                        }
                    }
                    // These cases all denote lack of/failed connection
                    isConnected = false;
                }
            } catch (IOException | IllegalStateException e) {
                logIt("ERROR posting reading to Data WebSocket: " + e.getLocalizedMessage());
                isConnected = false;
            }
        } else {
            logIt("Trying to reconnect to Data WebSocket...");
            try {
                connectToWebSocketServer();
            } catch (Exception e) {
                logIt("Error connecting to Data WebSocket: " + e.getLocalizedMessage());
            }
        }
    }

//    @Override
//    public void onOpen(Session sn, EndpointConfig ec) {
//        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        System.out.println("Opening /data endpoint...");
//    }
}
