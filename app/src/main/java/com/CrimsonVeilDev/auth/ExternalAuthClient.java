package com.CrimsonVeilDev.auth;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * External Authentication Client
 * Used by external applications to authenticate with the CrimsonVeil app
 */
public class ExternalAuthClient {
    
    private static final String TAG = "ExternalAuthClient";
    private final String host;
    private final int port;
    
    public ExternalAuthClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    /**
     * Authenticate with token
     * @param token Authentication token
     * @return Session ID if successful, null otherwise
     */
    public String authenticate(String token) {
        try {
            Socket socket = new Socket(host, port);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            
            // Send authentication request
            writer.println("AUTH:" + token);
            
            // Read response
            String response = reader.readLine();
            Log.d(TAG, "Authentication response: " + response);
            
            if (response != null && response.startsWith("AUTH_SUCCESS:")) {
                String sessionId = response.substring(13);
                socket.close();
                return sessionId;
            }
            
            socket.close();
            return null;
            
        } catch (Exception e) {
            Log.e(TAG, "Authentication error", e);
            return null;
        }
    }
    
    /**
     * Request new token (requires current valid token)
     * @param currentToken Current valid token
     * @return New token if successful, null otherwise
     */
    public String requestNewToken(String currentToken) {
        try {
            Socket socket = new Socket(host, port);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            
            // Send token request
            writer.println("GET_TOKEN:" + currentToken);
            
            // Read response
            String response = reader.readLine();
            
            if (response != null && response.startsWith("TOKEN:")) {
                String newToken = response.substring(6);
                socket.close();
                return newToken;
            }
            
            socket.close();
            return null;
            
        } catch (Exception e) {
            Log.e(TAG, "Token request error", e);
            return null;
        }
    }
    
    /**
     * Health check / ping
     * @return true if service is running and responsive
     */
    public boolean ping() {
        try {
            Socket socket = new Socket(host, port);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            
            // Send ping
            writer.println("PING");
            
            // Read response
            String response = reader.readLine();
            socket.close();
            
            return response != null && response.startsWith("PONG");
            
        } catch (Exception e) {
            Log.e(TAG, "Ping error", e);
            return false;
        }
    }
}
