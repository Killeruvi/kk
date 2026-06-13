package com.CrimsonVeilDev.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Manages external authentication state and configuration
 * Allows users to enable/disable login from outside virtual environment
 */
public class ExternalAuthManager {
    
    private static final String TAG = "ExternalAuthManager";
    private static final String PREFS_NAME = "external_auth_prefs";
    private static final String KEY_EXTERNAL_AUTH_ENABLED = "external_auth_enabled";
    private static final String KEY_EXTERNAL_LOGIN_TOKEN = "external_login_token";
    private static final String KEY_AUTH_PORT = "auth_port";
    private static final String KEY_LAST_AUTH_TIME = "last_auth_time";
    private static final String KEY_AUTH_SESSION_ID = "auth_session_id";
    
    private final SharedPreferences sharedPreferences;
    private final Context context;
    private static ExternalAuthManager instance;
    
    private ExternalAuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ExternalAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new ExternalAuthManager(context);
        }
        return instance;
    }
    
    /**
     * Check if external authentication is enabled
     */
    public boolean isExternalAuthEnabled() {
        boolean enabled = sharedPreferences.getBoolean(KEY_EXTERNAL_AUTH_ENABLED, false);
        Log.d(TAG, "External auth enabled: " + enabled);
        return enabled;
    }
    
    /**
     * Enable/disable external authentication
     */
    public void setExternalAuthEnabled(boolean enabled) {
        Log.d(TAG, "Setting external auth enabled: " + enabled);
        sharedPreferences.edit()
                .putBoolean(KEY_EXTERNAL_AUTH_ENABLED, enabled)
                .putLong(KEY_LAST_AUTH_TIME, System.currentTimeMillis())
                .apply();
    }
    
    /**
     * Generate and store authentication token
     */
    public String generateAuthToken() {
        String token = generateUniqueToken();
        sharedPreferences.edit()
                .putString(KEY_EXTERNAL_LOGIN_TOKEN, token)
                .putLong(KEY_LAST_AUTH_TIME, System.currentTimeMillis())
                .apply();
        Log.d(TAG, "Generated new auth token");
        return token;
    }
    
    /**
     * Validate authentication token
     */
    public boolean validateAuthToken(String token) {
        String storedToken = sharedPreferences.getString(KEY_EXTERNAL_LOGIN_TOKEN, "");
        boolean isValid = storedToken.equals(token) && !storedToken.isEmpty();
        Log.d(TAG, "Token validation result: " + isValid);
        return isValid;
    }
    
    /**
     * Get stored authentication token
     */
    public String getAuthToken() {
        return sharedPreferences.getString(KEY_EXTERNAL_LOGIN_TOKEN, "");
    }
    
    /**
     * Generate unique token
     */
    private String generateUniqueToken() {
        return "AUTH_" + System.currentTimeMillis() + "_" + 
               (int)(Math.random() * 100000);
    }
    
    /**
     * Get authentication port
     */
    public int getAuthPort() {
        return sharedPreferences.getInt(KEY_AUTH_PORT, 8888);
    }
    
    /**
     * Set authentication port
     */
    public void setAuthPort(int port) {
        sharedPreferences.edit()
                .putInt(KEY_AUTH_PORT, port)
                .apply();
        Log.d(TAG, "Auth port set to: " + port);
    }
    
    /**
     * Get last authentication time
     */
    public long getLastAuthTime() {
        return sharedPreferences.getLong(KEY_LAST_AUTH_TIME, 0);
    }
    
    /**
     * Check if authentication session is still valid (24 hours expiry)
     */
    public boolean isSessionValid() {
        long lastAuthTime = getLastAuthTime();
        long currentTime = System.currentTimeMillis();
        long twentyFourHours = 24 * 60 * 60 * 1000;
        
        boolean isValid = (currentTime - lastAuthTime) < twentyFourHours;
        Log.d(TAG, "Session valid: " + isValid);
        return isValid;
    }
    
    /**
     * Get current session ID
     */
    public String getSessionId() {
        String sessionId = sharedPreferences.getString(KEY_AUTH_SESSION_ID, "");
        if (sessionId.isEmpty()) {
            sessionId = "SESSION_" + System.currentTimeMillis();
            sharedPreferences.edit()
                    .putString(KEY_AUTH_SESSION_ID, sessionId)
                    .apply();
        }
        return sessionId;
    }
    
    /**
     * Clear all authentication data
     */
    public void clearAuthData() {
        sharedPreferences.edit()
                .remove(KEY_EXTERNAL_LOGIN_TOKEN)
                .remove(KEY_LAST_AUTH_TIME)
                .remove(KEY_AUTH_SESSION_ID)
                .apply();
        Log.d(TAG, "Authentication data cleared");
    }
    
    /**
     * Revoke external authentication completely
     */
    public void revokeExternalAuth() {
        setExternalAuthEnabled(false);
        clearAuthData();
        Log.d(TAG, "External authentication revoked");
    }
}
