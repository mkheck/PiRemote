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

import com.google.gson.Gson;

/** Reading is a bean to store the values coming from the Arduino
 * 
 * @author Mark Heckler & José Pereda - 2014; Mark Heckler - 2015+
 */
public class Reading {
    public static final int HUMIDITY = 0, 
            TEMPERATURE = 1,
            VOLTAGE = 2,
            LUMINOSITY = 3,
            WINDDIR = 4,
            WINDSPEED = 5,
            RAINFALL = 6,
            PRESSURE = 7,
            STATUS = 8;

    private final Gson gson = new Gson();

    private Integer id;
    private short node;
    private double hum, temp, volts, lum, windSpeed, rainfall;
    private long pressure;
    private int windDir, status;

    public Reading() {
        //this.id = -1;
        this.node = -1;
        this.hum = -1d;
        this.temp = -1d;
        this.volts = -1d;
        this.lum = -1d;
        this.windDir = -1;
        this.windSpeed = -1d;
        this.rainfall = -1d;
        this.pressure = -1l;
        this.status = 0;
    }

    public Reading(Integer id, short node, double hum, double temp, 
            double volts, double lum, int windDir, double windSpeed, 
            double rainfall, long pressure, int status) {
        this.id = id;
        this.node = node;
        this.hum = hum;
        this.temp = temp;
        this.volts = volts;
        this.lum = lum;
        this.windDir = windDir;
        this.windSpeed = windSpeed;
        this.rainfall = rainfall;
        this.pressure = pressure;
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

    public double getLuminosity() {
        return lum;
    }

    public void setLuminosity(double lum) {
        this.lum = lum;
    }

    public int getWindDir() {
        return windDir;
    }
    
    public void setWindDir(int windDir) {
        this.windDir = windDir;                
    }

    public double getWindSpeed() {
        return windSpeed;
    }
    
    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }
    
    public double getRainfall() {
        return rainfall;
    }
    
    public void setRainfall(double rainfall) {
        this.rainfall = rainfall;
    }
    
    public long getPressure() {
        return pressure;
    }

    public void setPressure(long pressure) {
        this.pressure = pressure;
    }
    
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public String toJson() {
        return gson.toJson(this);
    }
    
    @Override
    public String toString() {
        return "Id=" + id + ", node=" + node + 
                ", hum=" + hum + ", temp=" + temp + 
                ", volts=" + volts + ", luminosity=" + lum + 
                ", windDir=" + windDir + ", windSpeed=" + windSpeed + 
                ", rainfall=" + rainfall + ", pressure=" + pressure +
                ", status" + status + ".";
    }
}
