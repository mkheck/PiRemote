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
package org.thehecklers.piremote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Observable;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.thehecklers.piremote.comms.Serial;
import org.thehecklers.piremote.comms.ReadingPublisher;
import org.thehecklers.piremote.model.Reading;
import org.thehecklers.piremote.ws.WsControlClient;
import org.thehecklers.piremote.ws.WsDataClient;

/**
 * PiRemote is the Main Class to run in a remote Raspberry Pi
 * 
 * It has these main tasks:
 *  - Start a log file into which all status updates, comm, & readings are recorded
 *  - Start serial communication with Arduino(s)
 *  - Start WebSocket communication in two ways: 
 *    - Data: readings from Arduino(s) go to the cloud
 *    - Control: commands from the cloud are passed to the Arduino(s)
 *  - Disconnect on close
 * 
 * @author Mark Heckler & José Pereda - 2014; Mark Heckler - 2015+
 */

public class PiRemote extends Observable {
    private static final short NODE0 = 0;   // Default 0==Mark's utility shed concentrator
    
    private boolean isConnected;
    private String readBuffer = "";
    private String curCmd = "";
    private final Serial serial = new Serial();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private int nodeId;
    private int pubFreq = 5; // Default to publishing every fifth reading (overridable)
    private int readingCount = 1;

    Properties applicationProps = new Properties();

    private boolean hasWsConnection;
    //private boolean hasMqttConnection;
    
    private static PrintStream remoteLog = null;

    private WsControlClient wsControl = null;
    private WsDataClient wsData = null;
    private ReadingPublisher readingMqtt;

    public boolean connect() throws Exception {
        // Initialize the log (PrintStream with autoflush)
        // ALWAYS start the logging FIRST!
        remoteLog = new PrintStream(new FileOutputStream(new File("SerialReadings.log")), true);

        // Load application properties
        loadProperties();

        // Log detected ports
        Serial.listPorts();

        String nodeProp = getProperty("nodeId");
        if (nodeProp.isEmpty()) {
//            // Get out of here!
//            logIt("ERROR: Property 'nodeId' missing from PiRemote.properties file.");
//            Exception e = new Exception("ERROR: Property 'nodeId' missing from PiRemote.properties file.");
//            throw e;
            logIt("Default nodeId=0");
            nodeId = 0; // It's a default for a reason. :)
        } else {
            logIt("Loaded nodeId from properties: '" + nodeId + "'");
            nodeId = Integer.parseInt(nodeProp);
            
            String portName = getProperty("serialPort");
            if (portName.isEmpty()) {
                // Get out of here!
                logIt("ERROR: Property 'serialPort' missing from PiRemote.properties file.");
                Exception e = new Exception("ERROR: Property 'serialPort' missing from PiRemote.properties file.");
                throw e;
            } else {
                try {
                    logIt("Connecting to serial port " + portName);
                    executor.submit(new PiRemote.SerialThread(portName));
                    //isConnected = true; This is set in the serial.connect() method for realz...
                } catch (Exception e) {
                    logIt("Exception: Connection to serial port " + portName + " failed: "
                            + e.getMessage());
                    isConnected = false;
                }
            }
        }

        String uriWebSocket = getProperty("uriWebSocket");
        if (uriWebSocket.isEmpty()) {
            hasWsConnection = false;
//            // Get out of here!
//            logIt("ERROR: Property 'uriWebSocket' missing from PiRemote.properties file.");
//            Exception e = new Exception("ERROR: Property 'uriWebSocket' missing from PiRemote.properties file.");
//            throw e;
        } else {
            hasWsConnection = true;

            wsData = new WsDataClient(uriWebSocket);
            this.addObserver(wsData);

            wsControl = new WsControlClient(uriWebSocket, String.valueOf(nodeId), this);
            this.addObserver(wsControl);
        }
        
        String uriReadingMqtt = getProperty("uriMQTT");
        if (uriReadingMqtt.isEmpty()) {
            //hasMqttConnection = false;
//            // Get out of here!
//            logIt("ERROR: Property 'uriReadingsMqtt' missing from PiRemote.properties file.");
//            Exception e = new Exception("ERROR: Property 'uriReadingsMqtt' missing from PiRemote.properties file.");
//            throw e;
        } else {
            //hasMqttConnection = true;
            readingMqtt = new ReadingPublisher(uriReadingMqtt, String.valueOf(nodeId), "reading");
            this.addObserver(readingMqtt);
        }
        
        String freqProp = getProperty("pubFreq");
        if (!freqProp.isEmpty()) {
            pubFreq = Integer.parseInt(freqProp);
        }
    
        return isConnected;
    }

    public boolean disconnect() {
        logIt("Closing serial port");

        if (this.countObservers() > 0) {
            logIt("Disconnecting observers");
            this.deleteObservers();
        }
        
        if (hasWsConnection) {
            logIt("Stopping websockets");
            wsControl.disconnect();
            wsData.disconnect();
        }
        //readingMqtt. (MAH: No close/disconnect method)

        if (isConnected) {
            logIt("Closing serial port");
            isConnected = serial.disconnect();
            executor.shutdownNow();
        }
        
        if (remoteLog != null) {
            logIt("Disconnecting, closing log. Goodbye for now!");
            remoteLog.close();
        }
        
        return isConnected;
    }

    public static void logIt(String reading) {
        if(remoteLog != null){
            remoteLog.println(reading);
        } else {
            System.out.println(reading);
        }
    }

    
    /*
        Configuration file methods - begin
    */
    private void loadProperties() {
        FileInputStream in = null;
        File propFile = new File("PiRemote.properties");

        if (!propFile.exists()) {
            // If it doesn't exist, create it.
            try {
                propFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            in = new FileInputStream(propFile);
            applicationProps.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getProperty(String propKey) {
        if (applicationProps.containsKey(propKey)) {
            return applicationProps.getProperty(propKey, "");
        } else {
            logIt("ERROR: Property not found: '" + propKey + "'.");
        }
        return "";
    }
    /*
        Configuration file methods - end
    */

    private Reading createBeanFromReading(String reading) {
        Reading newBean = new Reading();

        newBean.setNode((short)nodeId);

        // Remove braces from reading "set"
        reading = reading.substring(1, reading.length() - 2);
        
        String[] values = reading.split("\\,");
        for (int x = 0; x < values.length; x++) {
            try {
                if(nodeId == NODE0) {
                    switch (x) {
                        case Reading.HUMIDITY:
                            newBean.setHum(Double.parseDouble(values[x]) / 100);
                            break;
                        case Reading.TEMPERATURE:
                            newBean.setTemp(Double.parseDouble(values[x]) / 100);
                            break;
                        case Reading.VOLTAGE:
                            newBean.setVolts(Double.parseDouble(values[x]) / 1000);
                            break;
                        case Reading.LUMINOSITY:
                            newBean.setLum(Double.parseDouble(values[x]) / 100);
                            break;
                        case Reading.WINDDIR:
                            newBean.setWindDir(Integer.parseInt(values[x]));
                            break;
                        case Reading.WINDSPEED:
                            newBean.setWindSpeed(Double.parseDouble(values[x]) / 100);
                            break;
                        case Reading.RAINFALL:
                            newBean.setRainfall(Double.parseDouble(values[x]) / 100);
                            break;
                        case Reading.PRESSURE:
                            newBean.setPressure(Long.parseLong(values[x]));
                            break;
                        case Reading.STATUS: 
                            newBean.setStatus(Integer.parseInt(values[x]));
                            break;
                    }
                } else { //if (nodeId == NODE1){
                    //
                }
            } catch (NumberFormatException nfe) {
                logIt("Error parsing " + reading);
            }
        }

        return newBean;
    }
    
    public void addToCommand(String cmd) {
        curCmd += cmd;
    }
    
    @Override
    public void notifyObservers(Object arg) {
        super.notifyObservers(arg);
    }

    private class SerialThread implements Runnable, SerialPortEventListener {

        public SerialThread(String portName) {
            logIt("Creating SerialThread for port " + portName);
            try {
                isConnected = serial.connect(portName, this);
            } catch (Exception ex) {
                Logger.getLogger(PiRemote.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) { // Data is available
                try {
                    // Read all available data from serial port and add to buffer
                    readBuffer += serial.getSerialPort().readString(event.getEventValue());
                    if (readBuffer.contains("\n")) {
                        // Remove NewLine character
                        readBuffer = readBuffer.substring(0, readBuffer.length()-1);
                        if (readingCount % pubFreq != 0) {
                            logIt(readBuffer);  // Write entry to file w/o annotation
                            readingCount++;
                        } else {
                            // Only publish if we want to publish (by count)
                            setChanged();
                            //notifyObservers(readBuffer);
                            Reading reading = createBeanFromReading(readBuffer);
                            if (reading.getHum() > -1d) { // Valid reading (MAH: revisit, refactor)
                                notifyObservers(reading);
                            }

                            logIt("--> " + readBuffer);  // Write published entry to file (w/annotation)
                            readingCount = 1;   // Reset counter
                        }
                        readBuffer = "";
                    }
                } catch (SerialPortException ex) {
                    logIt("Exception reading serial port: " + ex.getLocalizedMessage());
                }
            } else if (event.isCTS()) {     // CTS line has changed state
                if (event.getEventValue() == 1) { // Line is ON
                    logIt("CTS ON");
                } else {
                    logIt("CTS OFF");
                }
            } else if (event.isDSR()) {     // DSR line has changed state
                if (event.getEventValue() == 1) { // Line is ON
                    logIt("DSR ON");
                } else {
                    logIt("DSR OFF");
                }
            }
        }        

        @Override
        public void run() {
            while (isConnected) {
                if (!curCmd.isEmpty()) {
                    logIt("curCmd=='" + curCmd + "'");
                    
                    try {
                        if (serial.getSerialPort().writeString(curCmd)) {
                            curCmd = "";
                        }   // If it didn't write, don't clear buffer (else condition)
                    } catch (Exception ex) {
                        Logger.getLogger(PiRemote.class.getName()).log(Level.SEVERE, null, ex);
                        logIt("Exception writing to serial port: " + ex.getLocalizedMessage());
//                    } finally {
//                        setChanged();
                    }
                }
            }
        }
    }
    public static void main(String[] args) {
        final PiRemote remote = new PiRemote();
        try {
            remote.connect();
        } catch (Exception e) {
            // Can't call logIt() here b/c it's opened in serial.connect()!
            System.out.println("ERROR connecting: " + e.getMessage());
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Disconnecting via shutdown hook");
                remote.disconnect();
        }));
    }
}
