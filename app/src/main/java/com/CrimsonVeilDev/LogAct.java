package com.CrimsonVeilDev;

import android.app.Activity;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;

import com.CrimsonVeilDev.utils.Downtwo;
import com.CrimsonVeilDev.utils.Prefs;

import org.lsposed.lsparanoid.Obfuscate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Obfuscate
public class LogAct extends AppCompatActivity {

    static {
        try {
            System.loadLibrary("CrimsonVeilDev");
        } catch (UnsatisfiedLinkError ignored) {
        }
    }

    private Prefs prefs;
    private final String USER = "USER";

    private EditText textUsername;
    private Button btnLogin;
    private ImageView pasteBtn;
    private TextView getKey;
    private Dialog loadingDialog;

    // --- Cinematic Animation UI Properties
    private View moonSystem;
    private View moonOrbit;
    private View loginCardGlow;
    private View loginCard;
    private View cardContainer;
    
    // --- Modern Focus Responsive Architecture Layout References
    private View inputFieldWrap;
    private TextView animatedHint;
    private List<ValueAnimator> runningButtonAnimators = new ArrayList<>();

    // --- Permissions Definitions
    private static final int REQUEST_MANAGE_STORAGE_PERMISSION = 100;
    private static final int REQUEST_MANAGE_UNKNOWN_APP_SOURCES = 200;
    private static final String PREFS_NAME = "com.CrimsonVeilDev.prefs";
    private static final String PREF_PERMISSIONS_GRANTED = "permissions_granted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // VPN Guard rails initialization check
        if (iskokofm()) {
            new AlertDialog.Builder(LogAct.this)
                    .setTitle("VPN Detected")
                    .setMessage("Please disable VPN to continue")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
            return;
        }

        setContentView(R.layout.activity_login);
        prefs = new Prefs(this);

        // 🔑 System Permission Verification Core
        checkAndRequestPermissions();

        // Bind standard components
        textUsername = findViewById(R.id.userkey);
        btnLogin = findViewById(R.id.login);
        pasteBtn = findViewById(R.id.paste);
        getKey = findViewById(R.id.GetKey);

        // Bind Animation Framework Elements
        moonSystem = findViewById(R.id.moonSystem);
        moonOrbit = findViewById(R.id.moonOrbit);
        loginCardGlow = findViewById(R.id.loginCardGlow);
        loginCard = findViewById(R.id.loginCard);
        cardContainer = findViewById(R.id.cardContainer);
        
        // Bind Focus & Hint Animation Properties
        inputFieldWrap = findViewById(R.id.inputLayoutWrap);
        animatedHint = findViewById(R.id.animatedHint);

        // Restore active identity cache keys
        if (textUsername != null) {
            textUsername.setText(prefs.getSt(USER, ""));
            // Synchronize the floating hint position manually if cached text exists
            if (!textUsername.getText().toString().trim().isEmpty() && animatedHint != null) {
                float dpOffset = -34 * getResources().getDisplayMetrics().density;
                animatedHint.setTranslationY(dpOffset);
                animatedHint.setScaleX(0.85f);
                animatedHint.setScaleY(0.85f);
                animatedHint.setAlpha(0.9f);
                animatedHint.setTextColor(Color.parseColor("#FFFF2A2A"));
            }
        }

        // Initialize background animation systems
        startAtmosphericGlowSystem();

        // Trigger cinematic entrance translation sequencing
        executeGothicEntranceSequence();

        // Initialize Input Focus Shifter Hooks
        setupInputFocusPipeline();

        // Link Interactions Handlers
        if (getKey != null) {
            getKey.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(GetKey()));
                startActivity(intent);
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                // TACTILE IMPACT MICRO-POP: Compresses button slightly on click before triggering actions
                v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(80).withEndAction(() -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).withEndAction(() -> {
                        
                        String userKey = textUsername.getText().toString().trim();
                        if (!userKey.isEmpty()) {
                            if (iskokofm()) {
                                new AlertDialog.Builder(LogAct.this)
                                        .setTitle("VPN Detected")
                                        .setMessage("Please disable VPN to continue")
                                        .setPositiveButton("OK", null)
                                        .show();
                                return;
                            }
                            prefs.setSt(USER, userKey);
                            
                            // Initialize Intense Glowing Loader State Transformation
                            startButtonLoadingAnimation(btnLogin);
                            
                            // FIXED: Pass Activity instead of Context
                            Login(LogAct.this, userKey);
                        } else {
                            // High attention shake or flash instead of ugly system error balloons
                            textUsername.requestFocus();
                            Toast.makeText(this, "Please enter your access key", Toast.LENGTH_SHORT).show();
                        }
                        
                    }).start();
                }).start();
            });
        }

        if (pasteBtn != null) {
            pasteBtn.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    ClipData clip = clipboard.getPrimaryClip();
                    if (clip != null && clip.getItemCount() > 0) {
                        CharSequence clipText = clip.getItemAt(0).coerceToText(this);
                        String pasted = clipText != null ? clipText.toString() : "";
                        if (pasted.length() > 5) {
                            textUsername.setText(pasted);
                            // Push the floating text hint away manually post-paste ingestion
                            if (animatedHint != null) {
                                float dpOffset = -34 * getResources().getDisplayMetrics().density;
                                animatedHint.animate().translationY(dpOffset).scaleX(0.85f).scaleY(0.85f).alpha(0.9f).setDuration(200).start();
                                animatedHint.setTextColor(Color.parseColor("#FFFF2A2A"));
                            }
                        } else {
                            Toast.makeText(this, "Invalid key in clipboard", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(this, "Clipboard empty", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Set up dynamic monitoring for input fields to morph backgrounds and slide hint text.
     */
    private void setupInputFocusPipeline() {
        if (textUsername == null || animatedHint == null || inputFieldWrap == null) return;

        textUsername.setOnFocusChangeListener((v, hasFocus) -> {
            inputFieldWrap.setSelected(hasFocus); // Forces custom XML state selectors to trigger instantly

            String currentText = textUsername.getText().toString().trim();

            if (hasFocus || !currentText.isEmpty()) {
                // ANIMATION UP: Move title above input lane parameters gracefully
                float dpOffset = -34 * getResources().getDisplayMetrics().density;
                animatedHint.animate()
                        .translationY(dpOffset)
                        .scaleX(0.85f)
                        .scaleY(0.85f)
                        .alpha(0.9f)
                        .setDuration(250)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                animatedHint.setTextColor(Color.parseColor("#FFFF2A2A"));
            } else {
                // ANIMATION DOWN: Collapse field text back to center placeholder track
                animatedHint.animate()
                        .translationY(0f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .alpha(0.4f)
                        .setDuration(250)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                animatedHint.setTextColor(Color.parseColor("#66FFFFFF"));
            }
        });
    }

    /**
     * Swaps the login button background structure to an intense neon red glow
     * and sets up high-frequency infinite breathing scale loops.
     */
    private void startButtonLoadingAnimation(final Button loginButton) {
        if (loginButton == null) return;

        // Clean out stale loop handles if existing
        stopButtonLoadingAnimation(loginButton);

        // Apply energetic crimson glow asset
        loginButton.setBackgroundResource(R.drawable.gothic_button_loading_bg);
        loginButton.setText("LOADING...");
        loginButton.setEnabled(false);

        // Alpha Energy Flicker Hum Loop
        ObjectAnimator glowPulse = ObjectAnimator.ofFloat(loginButton, "alpha", 1.0f, 0.65f);
        glowPulse.setDuration(700);
        glowPulse.setRepeatMode(ValueAnimator.REVERSE);
        glowPulse.setRepeatCount(ValueAnimator.INFINITE);
        glowPulse.setInterpolator(new AccelerateDecelerateInterpolator());
        glowPulse.start();
        runningButtonAnimators.add(glowPulse);

        // Physical Pressure Pulse Expansion Loop
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(loginButton, "scaleX", 1.0f, 1.025f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(loginButton, "scaleY", 1.0f, 1.025f);
        
        scaleX.setDuration(700);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        
        scaleY.setDuration(700);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        scaleX.start();
        scaleY.start();
        
        runningButtonAnimators.add(scaleX);
        runningButtonAnimators.add(scaleY);
    }

    /**
     * Clears loop properties from buttons on system resets or exceptions.
     */
    private void stopButtonLoadingAnimation(final Button loginButton) {
        for (ValueAnimator animator : runningButtonAnimators) {
            if (animator != null) animator.cancel();
        }
        runningButtonAnimators.clear();
        
        if (loginButton != null) {
            loginButton.setEnabled(true);
            loginButton.setAlpha(1.0f);
            loginButton.setScaleX(1.0f);
            loginButton.setScaleY(1.0f);
            loginButton.setBackgroundResource(R.drawable.gothic_button_bg);
            loginButton.setText("LOGIN");
        }
    }

    /**
     * Loops a smooth, hardware-accelerated breathing background alpha glow
     * and initializes the continuous rotation of the orbital ring spinner.
     */
    private void startAtmosphericGlowSystem() {
        if (moonOrbit != null) {
            ObjectAnimator orbitSpinner = ObjectAnimator.ofFloat(moonOrbit, "rotation", 0f, 360f);
            orbitSpinner.setDuration(2200);
            orbitSpinner.setInterpolator(new LinearInterpolator());
            orbitSpinner.setRepeatCount(ValueAnimator.INFINITE);
            orbitSpinner.start();
        }

        if (loginCardGlow != null) {
            ObjectAnimator cardBreathe = ObjectAnimator.ofFloat(loginCardGlow, "alpha", 0.15f, 0.65f);
            cardBreathe.setDuration(3000);
            cardBreathe.setRepeatMode(ValueAnimator.REVERSE);
            cardBreathe.setRepeatCount(ValueAnimator.INFINITE);
            cardBreathe.start();
        }
    }

    /**
     * Handles the physical displacement calculation, scaling the moon down 
     * and gliding it elegantly from the absolute center to the top right container corner.
     */
    private void executeGothicEntranceSequence() {
        if (loginCard == null || moonSystem == null || moonOrbit == null) return;

        final View titleView = findViewById(R.id.title);
        final View loginButton = findViewById(R.id.login);
        final View getKeyLink = findViewById(R.id.GetKey);

        // Initial State: Hide the main card background panel cleanly
        loginCard.setAlpha(0f);
        loginCard.setScaleX(0.95f);
        loginCard.setScaleY(0.95f);

        // Initial State: Hide and shift down all the form elements slightly for a subtle lift effect
        if (titleView != null) { titleView.setAlpha(0f); titleView.setTranslationY(30f); }
        if (inputFieldWrap != null) { inputFieldWrap.setAlpha(0f); inputFieldWrap.setTranslationY(40f); }
        if (loginButton != null) { loginButton.setAlpha(0f); loginButton.setTranslationY(50f); }
        if (getKeyLink != null) { getKeyLink.setAlpha(0f); getKeyLink.setTranslationY(60f); }

        final View anchor = findViewById(R.id.moonTargetAnchor);

        moonSystem.postDelayed(() -> {
            if (anchor == null) return;

            // Calculate travel metrics for the Gemstone
            int[] anchorLocation = new int[2];
            anchor.getLocationInWindow(anchorLocation);

            int[] moonLocation = new int[2];
            moonSystem.getLocationInWindow(moonLocation);

            float deltaX = anchorLocation[0] - moonLocation[0] + ((anchor.getWidth() - moonSystem.getWidth()) / 2f);
            float deltaY = anchorLocation[1] - moonLocation[1] + ((anchor.getHeight() - moonSystem.getHeight()) / 2f);

            // Gemstone Flight Animators
            ObjectAnimator flyX = ObjectAnimator.ofFloat(moonSystem, "translationX", deltaX);
            ObjectAnimator flyY = ObjectAnimator.ofFloat(moonSystem, "translationY", deltaY);
            ObjectAnimator shrinkScaleX = ObjectAnimator.ofFloat(moonSystem, "scaleX", 1f, 0.70f);
            ObjectAnimator shrinkScaleY = ObjectAnimator.ofFloat(moonSystem, "scaleY", 1f, 0.70f);

            // Base Panel Reveal
            ObjectAnimator formReveal = ObjectAnimator.ofFloat(loginCard, "alpha", 0f, 1f);
            ObjectAnimator formExpandX = ObjectAnimator.ofFloat(loginCard, "scaleX", 0.95f, 1f);
            ObjectAnimator formExpandY = ObjectAnimator.ofFloat(loginCard, "scaleY", 0.95f, 1f);

            // --- STAGGERED INTERNAL ENTRIES ---
            AnimatorSet elementCascade = new AnimatorSet();
            List<android.animation.Animator> internalAnims = new ArrayList<>();

            if (titleView != null) {
                ObjectAnimator a = ObjectAnimator.ofFloat(titleView, "alpha", 0f, 1f);
                ObjectAnimator t = ObjectAnimator.ofFloat(titleView, "translationY", 30f, 0f);
                a.setDuration(500); t.setDuration(500);
                internalAnims.add(a); internalAnims.add(t);
            }
            if (inputFieldWrap != null) {
                ObjectAnimator a = ObjectAnimator.ofFloat(inputFieldWrap, "alpha", 0f, 1f);
                ObjectAnimator t = ObjectAnimator.ofFloat(inputFieldWrap, "translationY", 40f, 0f);
                a.setDuration(500); t.setDuration(500);
                a.setStartDelay(150); t.setStartDelay(150); // Small offset delay
                internalAnims.add(a); internalAnims.add(t);
            }
            if (loginButton != null) {
                ObjectAnimator a = ObjectAnimator.ofFloat(loginButton, "alpha", 0f, 1f);
                ObjectAnimator t = ObjectAnimator.ofFloat(loginButton, "translationY", 50f, 0f);
                a.setDuration(500); t.setDuration(500);
                a.setStartDelay(300); t.setStartDelay(300); // Medium offset delay
                internalAnims.add(a); internalAnims.add(t);
            }
            if (getKeyLink != null) {
                ObjectAnimator a = ObjectAnimator.ofFloat(getKeyLink, "alpha", 0f, 1f);
                ObjectAnimator t = ObjectAnimator.ofFloat(getKeyLink, "translationY", 60f, 0f);
                a.setDuration(500); t.setDuration(500);
                a.setStartDelay(450); t.setStartDelay(450); // Final offset delay
                internalAnims.add(a); internalAnims.add(t);
            }
            elementCascade.playTogether(internalAnims);

            // Master Animation Set Coordinator
            AnimatorSet masterSet = new AnimatorSet();
            masterSet.playTogether(flyX, flyY, shrinkScaleX, shrinkScaleY, formReveal, formExpandX, formExpandY, elementCascade);
            masterSet.setDuration(1500);
            masterSet.setInterpolator(new AccelerateDecelerateInterpolator());
            
            masterSet.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    super.onAnimationEnd(animation);
                    
                    // Infinite Gemstone Shimmer Loop
                    ObjectAnimator gemPulseX = ObjectAnimator.ofFloat(moonSystem, "scaleX", 0.70f, 0.75f);
                    ObjectAnimator gemPulseY = ObjectAnimator.ofFloat(moonSystem, "scaleY", 0.70f, 0.75f);

                    gemPulseX.setDuration(2200);
                    gemPulseX.setRepeatMode(ValueAnimator.REVERSE);
                    gemPulseX.setRepeatCount(ValueAnimator.INFINITE);
                    gemPulseX.setInterpolator(new AccelerateDecelerateInterpolator());

                    gemPulseY.setDuration(2200);
                    gemPulseY.setRepeatMode(ValueAnimator.REVERSE);
                    gemPulseY.setRepeatCount(ValueAnimator.INFINITE);
                    gemPulseY.setInterpolator(new AccelerateDecelerateInterpolator());

                    gemPulseX.start();
                    gemPulseY.start();
                }
            });
            
            masterSet.start();

            // Reduce orbit tracker path to a soft background aura glow
            ObjectAnimator.ofFloat(moonOrbit, "alpha", 1f, 0.25f).setDuration(1200).start();

        }, 2200); // 2.2 second lock window for center loader state
    }

    // 🔧 Android Sandbox Permissions Infrastructure
    private void checkAndRequestPermissions() {
        android.content.SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (!isStoragePermissionGranted()) {
            requestStoragePermissionDirect();
        } else if (!canRequestPackageInstalls()) {
            requestUnknownAppPermissionsDirect();
        } else {
            sharedPrefs.edit().putBoolean(PREF_PERMISSIONS_GRANTED, true).apply();
        }
    }

    private boolean isStoragePermissionGranted() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager();
    }

    private void requestStoragePermissionDirect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivityForResult(intent, REQUEST_MANAGE_STORAGE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_MANAGE_STORAGE_PERMISSION);
        }
    }

    private boolean canRequestPackageInstalls() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || getPackageManager().canRequestPackageInstalls();
    }

    private void requestUnknownAppPermissionsDirect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_MANAGE_UNKNOWN_APP_SOURCES);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MANAGE_STORAGE_PERMISSION) {
            checkAndRequestPermissions();
        } else if (requestCode == REQUEST_MANAGE_UNKNOWN_APP_SOURCES) {
            if (canRequestPackageInstalls()) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }

    private boolean iskokofm() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network activeNetwork = cm.getActiveNetwork();
                if (activeNetwork != null) {
                    NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
                    if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        return true;
                    }
                }
            } else {
                try {
                    java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
                    while (interfaces != null && interfaces.hasMoreElements()) {
                        java.net.NetworkInterface intf = interfaces.nextElement();
                        String name = intf.getName();
                        if (name != null) {
                            if (name.startsWith("tun") || name.startsWith("ppp") || name.startsWith("tap")) {
                                return true;
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return false;
    }

    // 🔑 Secure Handshake System Verification Routing
    // FIXED: Changed parameter from Context to Activity
    private void Login(final Activity activity, final String userKey) {
        showLoadingDialog("Checking key...", false);

        Handler loginHandler = new Handler(msg -> {
            dismissLoadingDialog();
            if (msg.what == 0) {
                startDownload(activity); // FIXED: Pass Activity
            } else if (msg.what == 1) {
                // If authentication drops or encounters an error, stop button animations cleanly
                stopButtonLoadingAnimation(btnLogin);
                showLoadingDialog((String) msg.obj, true);
            }
            return true;
        });

        new Thread(() -> {
            // FIXED: Pass Activity instead of Context
            String result = Check(activity, userKey);
            if ("OK".equals(result)) {
                loginHandler.sendEmptyMessage(0);
            } else {
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = result;
                loginHandler.sendMessage(msg);
            }
        }).start();
    }

    // FIXED: Changed parameter from Context to Activity
    private void startDownload(Activity activity) {
        showLoadingDialog("Downloading files...", false);

        Downtwo task = new Downtwo(activity, success -> {
            dismissLoadingDialog();
            if (success) {
                // FIXED: Use activity directly, not getApplicationContext()
                Intent i = new Intent(activity, MAct.class);
                activity.startActivity(i);
            } else {
                stopButtonLoadingAnimation(btnLogin);
                Toast.makeText(activity, "Download failed!", Toast.LENGTH_SHORT).show();
            }
        });

        task.setProgressListener(progress -> runOnUiThread(() -> {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                ProgressBar progressBar = loadingDialog.findViewById(R.id.progressBar);
                TextView progressText = loadingDialog.findViewById(R.id.progressText);

                if (progressBar != null) {
                    progressBar.setIndeterminate(false);
                    progressBar.setMax(100);
                    progressBar.setProgress(progress);
                }

                if (progressText != null) {
                    progressText.setText("Downloading... " + progress + "%");
                }
            }
        }));

        task.execute(Downtwo.Link());
    }
    
    private void showLoadingDialog(String message, boolean isError) {
        if (loadingDialog == null) {
            loadingDialog = new Dialog(this);
            loadingDialog.setContentView(R.layout.ios_loading);
            loadingDialog.setCancelable(false);
            if (loadingDialog.getWindow() != null) {
                loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        }

        View dialogContainer = loadingDialog.findViewById(R.id.dialogContainer);
        ImageView loadingIcon = loadingDialog.findViewById(R.id.loadingIcon);
        TextView loadingText = loadingDialog.findViewById(R.id.loadingText);
        ProgressBar progressBar = loadingDialog.findViewById(R.id.progressBar);
        TextView progressText = loadingDialog.findViewById(R.id.progressText);
        Button okButton = loadingDialog.findViewById(R.id.okButton);

        // --- SETUP VISIBILITY MATRIX ---
        if (isError) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (progressText != null) progressText.setVisibility(View.GONE);
            if (okButton != null) {
                okButton.setVisibility(View.VISIBLE);
                okButton.setOnClickListener(v -> dismissLoadingDialog());
            }
            if (loadingText != null) loadingText.setText("Login failed: " + message);
        } else {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            if (progressText != null) progressText.setVisibility(View.VISIBLE);
            if (okButton != null) okButton.setVisibility(View.GONE);
            if (loadingText != null) loadingText.setText(message != null ? message : "Loading...");
        }

        // --- TRIGGER FLUID ANIMATION SUITE ---
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();

            // 1. Physical Scale Expansion Pop for the whole alert frame container
            if (dialogContainer != null) {
                dialogContainer.setAlpha(0f);
                dialogContainer.setScaleX(0.80f);
                dialogContainer.setScaleY(0.80f);
                
                dialogContainer.animate()
                    .alpha(1f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(350)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            }

            // 2. Continuous mystical infinite spin on the top gem core icon
            if (loadingIcon != null) {
                loadingIcon.setRotation(0f);
                ObjectAnimator iconSpinner = ObjectAnimator.ofFloat(loadingIcon, "rotation", 0f, 360f);
                iconSpinner.setDuration(3000);
                iconSpinner.setInterpolator(new LinearInterpolator());
                iconSpinner.setRepeatCount(ValueAnimator.INFINITE);
                iconSpinner.start();
                
                // Tag it so it stops cleanly if the dialog dismisses
                loadingIcon.setTag(iconSpinner);
            }
            
            // 3. Staggered text fade in slide for internal status elements
            if (loadingText != null && progressText != null) {
                loadingText.setAlpha(0f);
                loadingText.setTranslationY(15f);
                progressText.setAlpha(0f);
                progressText.setTranslationY(15f);

                loadingText.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start();
                progressText.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(200).start();
            }
        } else {
            // If the dialog is already open and just updating text values mid-flight
            if (loadingText != null && !isError) {
                loadingText.setText(message);
            }
        }
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            ImageView loadingIcon = loadingDialog.findViewById(R.id.loadingIcon);
            if (loadingIcon != null && loadingIcon.getTag() instanceof ObjectAnimator) {
                ((ObjectAnimator) loadingIcon.getTag()).cancel(); // Halts loop gracefully
            }
            loadingDialog.dismiss();
        }
    }

    // --- Native JNI Interface Link Bridges
    // FIXED: Changed parameter from Context to Activity
    private static native String Check(Activity activity, String userKey);
    private native String GetKey();
}