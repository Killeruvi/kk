package com.CrimsonVeilDev.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.CrimsonVeilDev.R;
import com.CrimsonVeilDev.auth.ExternalAuthManager;
import com.CrimsonVeilDev.auth.ExternalAuthService;

/**
 * Settings Activity for External Authentication
 * Allows users to toggle external login feature on/off
 * Displays current auth token and status
 */
public class LoginSettingsActivity extends AppCompatActivity {
    
    private static final String TAG = "LoginSettingsActivity";
    private ExternalAuthManager authManager;
    private TextView statusText;
    private TextView tokenText;
    private Switch authToggle;
    private Button generateTokenBtn;
    private Button clearAuthBtn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_settings);
        
        // Set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(0xFF050508);
        }
        
        authManager = ExternalAuthManager.getInstance(this);
        
        // Initialize UI components
        initializeUI();
        updateUIState();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeUI() {
        // Create main layout
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(30, 30, 30, 30);
        
        // Set background
        GradientDrawable bgDrawable = new GradientDrawable();
        bgDrawable.setColor(0xFF0A0A0F);
        mainLayout.setBackground(bgDrawable);
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("EXTERNAL LOGIN SETTINGS");
        titleText.setTextColor(0xFFFF1A1A);
        titleText.setTextSize(18f);
        titleText.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        titleText.setGravity(Gravity.CENTER);
        titleText.setShadowLayer(5f, 0, 0, 0xFFCC0000);
        
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 30);
        mainLayout.addView(titleText, titleParams);
        
        // Description
        TextView descriptionText = new TextView(this);
        descriptionText.setText("Enable external login to allow authentication from outside the virtual environment");
        descriptionText.setTextColor(0xFF999999);
        descriptionText.setTextSize(12f);
        descriptionText.setLineSpacing(1.5f, 1.0f);
        
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descParams.setMargins(0, 0, 0, 25);
        mainLayout.addView(descriptionText, descParams);
        
        // Toggle Switch Container
        LinearLayout toggleContainer = createToggleContainer();
        LinearLayout.LayoutParams toggleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toggleParams.setMargins(0, 0, 0, 25);
        mainLayout.addView(toggleContainer, toggleParams);
        
        // Status Section
        TextView statusLabelText = new TextView(this);
        statusLabelText.setText("STATUS:");
        statusLabelText.setTextColor(0xFFFF1A1A);
        statusLabelText.setTextSize(13f);
        statusLabelText.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        
        LinearLayout.LayoutParams statusLabelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusLabelParams.setMargins(0, 0, 0, 10);
        mainLayout.addView(statusLabelText, statusLabelParams);
        
        // Status Display
        statusText = new TextView(this);
        statusText.setTextColor(0xFF00FF00);
        statusText.setTextSize(12f);
        statusText.setPadding(15, 15, 15, 15);
        
        GradientDrawable statusBg = new GradientDrawable();
        statusBg.setColor(0xFF1A1A1A);
        statusBg.setCornerRadius(4f);
        statusBg.setStroke(1, 0xFF333333);
        statusText.setBackground(statusBg);
        
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusParams.setMargins(0, 0, 0, 25);
        mainLayout.addView(statusText, statusParams);
        
        // Token Section
        TextView tokenLabelText = new TextView(this);
        tokenLabelText.setText("AUTHENTICATION TOKEN:");
        tokenLabelText.setTextColor(0xFFFF1A1A);
        tokenLabelText.setTextSize(13f);
        tokenLabelText.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        
        LinearLayout.LayoutParams tokenLabelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tokenLabelParams.setMargins(0, 0, 0, 10);
        mainLayout.addView(tokenLabelText, tokenLabelParams);
        
        // Token Display
        tokenText = new TextView(this);
        tokenText.setTextColor(0xFFAAAAAA);
        tokenText.setTextSize(11f);
        tokenText.setTypeface(Typeface.MONOSPACE);
        tokenText.setPadding(15, 15, 15, 15);
        
        GradientDrawable tokenBg = new GradientDrawable();
        tokenBg.setColor(0xFF1A1A1A);
        tokenBg.setCornerRadius(4f);
        tokenBg.setStroke(1, 0xFF333333);
        tokenText.setBackground(tokenBg);
        
        LinearLayout.LayoutParams tokenParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tokenParams.setMargins(0, 0, 0, 25);
        mainLayout.addView(tokenText, tokenParams);
        
        // Button Container
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        
        // Generate Token Button
        generateTokenBtn = createStyledButton("GENERATE NEW TOKEN");
        generateTokenBtn.setOnClickListener(v -> generateNewToken());
        
        LinearLayout.LayoutParams genBtnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 100);
        genBtnParams.setMargins(0, 0, 0, 15);
        buttonContainer.addView(generateTokenBtn, genBtnParams);
        
        // Clear Auth Button
        clearAuthBtn = createStyledButton("CLEAR AUTHENTICATION", 0xFF4A0000);
        clearAuthBtn.setOnClickListener(v -> clearAuthentication());
        
        LinearLayout.LayoutParams clearBtnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 100);
        clearBtnParams.setMargins(0, 0, 0, 15);
        buttonContainer.addView(clearAuthBtn, clearBtnParams);
        
        // Back Button
        Button backBtn = createStyledButton("BACK", 0xFF1F0505);
        backBtn.setOnClickListener(v -> finish());
        
        LinearLayout.LayoutParams backBtnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 100);
        buttonContainer.addView(backBtn, backBtnParams);
        
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mainLayout.addView(buttonContainer, containerParams);
        
        setContentView(mainLayout);
    }
    
    /**
     * Create toggle switch container
     */
    private LinearLayout createToggleContainer() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        
        // Label
        TextView label = new TextView(this);
        label.setText("Enable External Login:");
        label.setTextColor(0xFFCCCCCC);
        label.setTextSize(14f);
        
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        labelParams.setMargins(0, 0, 15, 0);
        container.addView(label, labelParams);
        
        // Toggle Switch
        authToggle = new Switch(this);
        authToggle.setChecked(authManager.isExternalAuthEnabled());
        authToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleToggleChange(isChecked);
            }
        });
        
        LinearLayout.LayoutParams toggleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(authToggle, toggleParams);
        
        return container;
    }
    
    /**
     * Create styled button
     */
    private Button createStyledButton(String text) {
        return createStyledButton(text, 0xFF5A0000);
    }
    
    /**
     * Create styled button with custom color
     */
    private Button createStyledButton(String text, int bgColor) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(11f);
        button.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        
        GradientDrawable bgDrawable = new GradientDrawable();
        bgDrawable.setColor(bgColor);
        bgDrawable.setCornerRadius(4f);
        button.setBackground(bgDrawable);
        
        return button;
    }
    
    /**
     * Handle toggle switch change
     */
    private void handleToggleChange(boolean isEnabled) {
        if (isEnabled) {
            // Enable external auth
            authManager.setExternalAuthEnabled(true);
            
            // Start authentication service
            Intent serviceIntent = new Intent(this, ExternalAuthService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            
            // Generate initial token
            String token = authManager.generateAuthToken();
            Log.d(TAG, "External auth enabled with token: " + token);
            Toast.makeText(this, "External login enabled", Toast.LENGTH_SHORT).show();
            
        } else {
            // Disable external auth
            authManager.setExternalAuthEnabled(false);
            
            // Stop authentication service
            Intent serviceIntent = new Intent(this, ExternalAuthService.class);
            stopService(serviceIntent);
            
            Log.d(TAG, "External auth disabled");
            Toast.makeText(this, "External login disabled", Toast.LENGTH_SHORT).show();
        }
        
        updateUIState();
    }
    
    /**
     * Generate new authentication token
     */
    private void generateNewToken() {
        if (!authManager.isExternalAuthEnabled()) {
            Toast.makeText(this, "Enable external login first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String newToken = authManager.generateAuthToken();
        Log.d(TAG, "New token generated: " + newToken);
        Toast.makeText(this, "New token generated", Toast.LENGTH_SHORT).show();
        updateUIState();
    }
    
    /**
     * Clear all authentication
     */
    private void clearAuthentication() {
        authManager.revokeExternalAuth();
        authToggle.setChecked(false);
        
        // Stop service
        Intent serviceIntent = new Intent(this, ExternalAuthService.class);
        stopService(serviceIntent);
        
        Log.d(TAG, "Authentication cleared");
        Toast.makeText(this, "Authentication cleared", Toast.LENGTH_SHORT).show();
        updateUIState();
    }
    
    /**
     * Update UI state based on current auth status
     */
    private void updateUIState() {
        boolean isEnabled = authManager.isExternalAuthEnabled();
        
        // Update toggle
        authToggle.setOnCheckedChangeListener(null); // Remove listener temporarily
        authToggle.setChecked(isEnabled);
        authToggle.setOnCheckedChangeListener((buttonView, isChecked) -> handleToggleChange(isChecked));
        
        // Update status
        if (isEnabled) {
            statusText.setText("✓ ACTIVE - External login is enabled");
            statusText.setTextColor(0xFF00FF00);
            generateTokenBtn.setEnabled(true);
        } else {
            statusText.setText("✗ INACTIVE - External login is disabled");
            statusText.setTextColor(0xFFFF0000);
            generateTokenBtn.setEnabled(false);
        }
        
        // Update token display
        String token = authManager.getAuthToken();
        if (token.isEmpty()) {
            tokenText.setText("No token generated");
        } else {
            tokenText.setText(token);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateUIState();
    }
}
