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
package org.thehecklers.piremote.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Reading is a bean to store the values coming from the Arduino
 * 
 * @author Mark Heckler & José Pereda - 2014; Mark Heckler - 2015+
 */
public class Reading {
    public static final int HUMIDITY = 0, 
            TEMPERATURE = 1,
            VOLTAGE = 2,
            CURRENT = 3,
            STATUS = 4;

    private final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    private Integer id;
    private short node;
    private double hum, temp, volts, amps;
    //private long pressure;
    private int status;

    public Reading() {
        //this.id = -1;
        this.node = -1;
        this.hum = -1d;
        this.temp = -1d;
        this.volts = -1d;
        this.amps = -1d;
        //this.pressure = -1l;
        this.status = 0;
    }

    public Reading(Integer id, short node, double hum, double temp, 
            double volts, double amps, int status) {
        this.id = id;
        this.node = node;
        this.hum = hum;
        this.temp = temp;
        this.volts = volts;
        this.amps = amps;
        //this.pressure = pressure;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public short getNode() {
        return node;
    }

    public void setNode(short node) {
        this.node = node;
    }

    public double getHum() {
        return hum;
    }

    public void setHum(double hum) {
        this.hum = hum;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getVolts() {
        return volts;
    }

    public void setVolts(double volts) {
        this.volts = volts;
    }

    public double getAmps() {
        return amps;
    }

    public void setAmps(double amps) {
        this.amps = amps;
    }

//    public long getPressure() {
//        return pressure;
//    }

//    public void setPressure(long pressure) {
//        this.pressure = pressure;
//    }
    
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public String toJson() {
        String json = "";
        
        try {
            json = objectWriter.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(Reading.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return json;
    }
    
    @Override
    public String toString() {
        return "Id=" + id + ", node=" + node + 
                ", hum=" + hum + ", temp=" + temp + 
                ", volts=" + volts + ", current=" + amps +
                //", pressure=" + pressure +
                ", status" + status + ".";
    }
}
