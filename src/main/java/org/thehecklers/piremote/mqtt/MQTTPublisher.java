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

/**
 *
 * @author Mark Heckler (mark.heckler@gmail.com, @mkheck)
 */
public abstract class MQTTPublisher {
    private String topTopic = "";
    private String subTopic = "";
    private String msgText = "";

    public MQTTPublisher(String topTopic, String subTopic) {
        this.topTopic = topTopic;
        this.subTopic = subTopic;
    }

    public MQTTPublisher(String topTopic) {
        this.topTopic = topTopic;
    }

    public String getTopTopic() {
        return topTopic;
    }

    public String getSubTopic() {
        return subTopic;
    }

    public String getMessage() {
        return msgText;
    }
    
    public void setTopTopic(String topTopic) {
        this.topTopic = topTopic;
    }
    
    public void setSubTopic(String subTopic) {
        this.subTopic = subTopic;
    }
    
    public void setMessage(String msgText) {
        this.msgText = msgText;
    }
    
    public abstract void publish();
    
    public void publish(String msgText) {
        setMessage(msgText);
        publish();
    }
    
    public void publish(String subTopic, String msgText) {
        setSubTopic(subTopic);
        publish(msgText);
    }
    
    void publish(String topTopic, String subTopic, String msgText) {
        setTopTopic(topTopic);
        publish(subTopic, msgText);
    }
}

