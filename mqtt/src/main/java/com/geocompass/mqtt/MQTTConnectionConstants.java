package com.geocompass.mqtt;

/**
 * Created by gxsn on 2018/6/8.
 */

public class MQTTConnectionConstants {
    
    /**
     * We're using this base number to make it less likely that any of these
     * constans will overlap with constants defined in the activity or by MQTT.
     */
    private static final int CONSTANTS_BASE = 846751925;
    
   
    public static final int STATE_NONE = CONSTANTS_BASE + 0;
    
   
    public static final int STATE_CONNECTING = CONSTANTS_BASE + 1;
    
   
    public static final int STATE_CONNECTED = CONSTANTS_BASE + 2;
    
   
    public static final int STATE_CONNECTION_FAILED = CONSTANTS_BASE + 3;
    
 
    public static final int STATE_CHANGE = CONSTANTS_BASE + 5;
    
  
    public static final int MQTT_RAW_PUBLISH = CONSTANTS_BASE + 10;
    
   
    public static final int MQTT_RAW_READ = CONSTANTS_BASE + 11;
    
    
    /**
     * Helper to resolve a connection state name.
     *
     * @param state The connection state to translate
     * @return The connection state name
     */
    public static String resolveStateName(int state) {
        switch (state) {
            case STATE_NONE:
                return "STATE_NONE";
            case STATE_CONNECTING:
                return "STATE_CONNECTING";
            case STATE_CONNECTED:
                return "STATE_CONNECTED";
            case STATE_CONNECTION_FAILED:
                return "STATE_CONNECTION_FAILED";
            
            default:
                return "UNDEFINED STATE";
        }
    }
    
    
    
}