package com.CrimsonVeilDev.auth;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * External Authentication Service
 * Runs a local server that handles authentication requests from outside the virtual environment
 * When enabled, allows devices/apps to authenticate using token-based authentication
 */
public class ExternalAuthService extends Service {
    
    private static final String TAG = "ExternalAuthService";
    private static final int DEFAULT_PORT = 8888;
    
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private ExternalAuthManager authManager;
    private final IBinder binder = new LocalBinder();
    
    public class LocalBinder extends Binder {
        ExternalAuthService getService() {
            return ExternalAuthService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ExternalAuthService created");
        authManager = ExternalAuthManager.getInstance(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand - External auth service starting");
        
        // Check if external auth is enabled
        if (!authManager.isExternalAuthEnabled()) {
            Log.d(TAG, "External auth is disabled, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }
        
        // Start authentication server in background thread
        if (!isRunning) {
            startAuthenticationServer();
        }
        
        return START_STICKY;
    }
    
    /**
     * Start the authentication server
     */
    private void startAuthenticationServer() {
        new Thread(() -> {
            try {
                int port = authManager.getAuthPort();
                serverSocket = new ServerSocket(port);
                isRunning = true;
                
                Log.d(TAG, "Authentication server started on port " + port);
                
                while (isRunning && !serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        
                        // Handle client connection in separate thread
                        new Thread(() -> handleClientConnection(clientSocket)).start();
                        
                    } catch (Exception e) {
                        if (isRunning) {
                            Log.e(TAG, "Error accepting client connection", e);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error starting authentication server", e);
                isRunning = false;
            }
        }).start();
    }
    
    /**
     * Handle incoming client authentication request
     */
    private void handleClientConnection(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            
            // Read authentication request
            String request = reader.readLine();
            Log.d(TAG, "Received auth request: " + (request != null ? request.substring(0, Math.min(50, request.length())) : "null"));
            
            if (request != null && request.startsWith("AUTH:")) {
                String token = request.substring(5).trim();
                
                // Validate token
                if (authManager.validateAuthToken(token)) {
                    // Token is valid
                    writer.println("AUTH_SUCCESS:" + authManager.getSessionId());
                    Log.d(TAG, "Authentication successful");
                    
                    // Send additional data
                    writer.println("SESSION_ID:" + authManager.getSessionId());
                    writer.println("TIMESTAMP:" + System.currentTimeMillis());
                    
                } else {
                    // Token is invalid
                    writer.println("AUTH_FAILED:Invalid token");
                    Log.d(TAG, "Authentication failed - invalid token");
                }
            } else if (request != null && request.startsWith("GET_TOKEN:")) {
                // Request for new token (requires device to be already authenticated)
                String currentToken = request.substring(10).trim();
                
                if (authManager.validateAuthToken(currentToken)) {
                    String newToken = authManager.generateAuthToken();
                    writer.println("TOKEN:" + newToken);
                    Log.d(TAG, "New token generated");
                } else {
                    writer.println("ERROR:Invalid current token");
                }
            } else if (request != null && request.startsWith("PING")) {
                // Health check
                writer.println("PONG:" + authManager.getSessionId());
                Log.d(TAG, "Ping received, pong sent");
            } else {
                writer.println("ERROR:Invalid request format");
                Log.d(TAG, "Invalid request format received");
            }
            
            writer.flush();
            clientSocket.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling client connection", e);
        }
    }
    
    /**
     * Stop the authentication server
     */
    private void stopAuthenticationServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closing server socket", e);
        }
        Log.d(TAG, "Authentication server stopped");
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "ExternalAuthService destroyed");
        stopAuthenticationServer();
        super.onDestroy();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    /**
     * Public method to get current token
     */
    public String getCurrentToken() {
        return authManager.getAuthToken();
    }
    
    /**
     * Public method to enable/disable external auth
     */
    public void setExternalAuthEnabled(boolean enabled) {
        authManager.setExternalAuthEnabled(enabled);
        if (enabled) {
            startAuthenticationServer();
        } else {
            stopAuthenticationServer();
        }
    }
    
    /**
     * Public method to check if service is running
     */
    public boolean isServiceRunning() {
        return isRunning;
    }
}
