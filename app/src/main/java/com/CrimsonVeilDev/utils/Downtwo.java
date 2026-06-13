package com.CrimsonVeilDev.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.os.Build;
import android.graphics.Color;

import com.CrimsonVeilDev.R;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.animation.ValueAnimator;
import android.view.ViewGroup;

public class Downtwo {
    private static final String TAG = "Downtwo";
    private static final String LOG_FILE_NAME = "downtwo_log.txt";
    private static final String LOG_DIR_NAME = "CrimsonVeilDev";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    // --- Native JNI Core Links ---
    public static native String Version();
    public static native String Link();

    // --- UI Dialog for live logging ---
    private Dialog progressDialog;
    private TextView dialogMessageView;
    private boolean dialogShowing = false;
    
    // Progress bar components
    private View progressFill;
    private TextView progressPercent;
    
    // Animation components
    private ValueAnimator scanAnimator;
    private ValueAnimator titleGlowAnimator;

    public interface Callback {
        void onComplete(boolean success);
    }

    public interface ProgressListener {
        void onProgress(int percent);
    }

    private final Activity activity;
    private final Callback callback;
    private ProgressListener progressListener;
    private File logFile;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String serverVersion = "0.0";
    private static final String PREF_NAME = "com.CrimsonVeilDev.download";
    private static final String PREF_VERSION_KEY = "version";
    private static final String HOSTOP_ZIP = "CrymsonCrystal.zip";
    private static final String LOADER_DIR_NAME = "loader";
    
    private long startTime = 0;
    private int lastDownloaded = 0;

    public Downtwo(Activity activity) {
        this(activity, null);
    }

    public Downtwo(Activity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
        initializeLogFile();
        logInfo("Downtwo initialized");
    }

    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }

    private void initializeLogFile() {
        try {
            String state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                Log.w(TAG, "External storage not available, falling back to internal storage");
                File filesDir = activity.getFilesDir();
                File logDir = new File(filesDir, LOG_DIR_NAME);
                if (!logDir.exists()) {
                    logDir.mkdirs();
                }
                logFile = new File(logDir, LOG_FILE_NAME);
            } else {
                File externalDir = Environment.getExternalStorageDirectory();
                File logDir = new File(externalDir, LOG_DIR_NAME);
                if (!logDir.exists()) {
                    boolean created = logDir.mkdirs();
                    Log.d(TAG, "Created external log directory: " + logDir.getAbsolutePath() + ", Success: " + created);
                }
                logFile = new File(logDir, LOG_FILE_NAME);
            }
            
            if (!logFile.exists()) {
                logFile.createNewFile();
                writeToLogFile("=== Downtwo Log Started ===");
                writeToLogFile("Log file created at: " + getCurrentTimestamp());
                writeToLogFile("Log file path: " + logFile.getAbsolutePath());
            }
            
            Log.i(TAG, "Log file initialized at: " + logFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize log file", e);
        }
    }

    private void writeToLogFile(String message) {
        if (logFile == null) return;
        
        try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
            String logEntry = "[" + getCurrentTimestamp() + "] " + message + "\n";
            fos.write(logEntry.getBytes());
            fos.flush();
        } catch (Exception e) {
            Log.e(TAG, "Failed to write to log file", e);
        }
    }

    private String getCurrentTimestamp() {
        return DATE_FORMAT.format(new Date());
    }

    private void logInfo(String message) {
        Log.i(TAG, message);
        writeToLogFile("[INFO] " + message);
    }

    private void logError(String message, Throwable error) {
        String errorDetails = message;
        if (error != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            error.printStackTrace(pw);
            errorDetails += " - " + error.getMessage() + "\n" + sw.toString();
        }
        Log.e(TAG, errorDetails);
        writeToLogFile("[ERROR] " + errorDetails);
    }

    private void logWarning(String message) {
        Log.w(TAG, message);
        writeToLogFile("[WARN] " + message);
    }

    private void logDebug(String message) {
        Log.d(TAG, message);
        writeToLogFile("[DEBUG] " + message);
    }

    public void execute(final String downloadUrl) {
        logInfo("Executing download/update process - URL: " + (downloadUrl != null ? downloadUrl : "null"));
        
        showOrUpdateDialog("Starting update check...", false);

        executorService.execute(() -> {
            try {
                if (downloadUrl == null || downloadUrl.isEmpty()) {
                    logError("Invalid URL provided", null);
                    postResult("Invalid URL provided");
                    return;
                }

                logDebug("Fetching server version from: " + Version());
                serverVersion = getServerVersion();
                if (serverVersion == null) {
                    logError("Failed to retrieve server version", null);
                    postResult("Failed to retrieve server version");
                    return;
                }

                SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String localVersion = prefs.getString(PREF_VERSION_KEY, "0.0");
                logInfo("Version check - Server: " + serverVersion + ", Local: " + localVersion);

                if (!localVersion.equals(serverVersion)) {
                    logInfo("Update needed - Starting download and extraction");
                    String result = downloadAndExtract(downloadUrl);
                    postResult(result);
                } else {
                    logInfo("No update needed - Already on latest version");
                    postResult("No Update Available");
                }
            } catch (Exception e) {
                logError("Execution worker pool exception", e);
                postResult("Background error: " + e.getMessage());
            }
        });
    }

    private void postResult(final String result) {
        mainHandler.post(() -> {
            boolean success = (result == null) || "No Update Available".equals(result);
            logInfo("Posting result - Success: " + success + ", Result: " + result);

            try {
                if (callback != null) {
                    callback.onComplete(success);
                    logDebug("Callback executed - Success: " + success);
                }
            } catch (Exception e) {
                logError("Callback invocation handling fault", e);
            }

            if (!success) {
                showOrUpdateDialog("❌ Error: " + result, true);
                logError("Process failed with result: " + result, new Exception(result));
            } else if (result != null && result.equals("No Update Available")) {
                showOrUpdateDialog("✅ Already up to date", true);
                logInfo("Process completed - Already up to date");
            } else {
                showOrUpdateDialog("✅ Update completed successfully", true);
                logInfo("Process completed - Update successful");
            }

            if (logFile != null) {
                logInfo("Log file saved to: " + logFile.getAbsolutePath());
                updateDialogMessage("📄 Log saved to: " + logFile.getAbsolutePath());
            }

            executorService.shutdown();
        });
    }

    private String getServerVersion() {
        String versionUrl = Version();
        logDebug("Fetching server version from URL: " + versionUrl);
        
        try {
            URL url = new URL(versionUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.connect();

            int responseCode = connection.getResponseCode();
            logDebug("HTTP connection established - Response code: " + responseCode);
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logError("HTTP error: " + responseCode, null);
                updateDialogMessage("❌ Failed to get server version (HTTP " + responseCode + ")");
                return null;
            }

            InputStream inputStream = connection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            String version = scanner.hasNextLine() ? scanner.nextLine().trim() : "0.0";

            scanner.close();
            inputStream.close();
            connection.disconnect();

            logInfo("Server version retrieved: " + version);
            updateDialogMessage("📡 Server version: " + version);
            return version;
        } catch (Exception e) {
            logError("Error fetching server version", e);
            updateDialogMessage("❌ Failed to get server version");
            return null;
        }
    }

    private String downloadAndExtract(String urlString) {
        logInfo("Starting download and extraction - URL: " + urlString);
        File pathOutput = null;
        startTime = System.currentTimeMillis();
        lastDownloaded = 0;
        
        try {
            updateDialogMessage("🌐 Connecting to GitHub...");
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logError("Download failed with HTTP code: " + responseCode, null);
                return "Download failed: HTTP " + responseCode;
            }

            int totalSize = connection.getContentLength();
            int downloaded = 0;
            
            logInfo("Download started - Total size: " + totalSize + " bytes");
            if (totalSize > 0) {
                updateDialogMessage("📦 Download size: " + (totalSize / 1024 / 1024) + " MB");
            }

            InputStream input = connection.getInputStream();
            File pathBase = new File(activity.getFilesDir().getPath());
            if (!pathBase.exists()) {
                boolean created = pathBase.mkdirs();
                logDebug("Created base directory - Path: " + pathBase.getAbsolutePath() + ", Success: " + created);
            }

            pathOutput = new File(pathBase, HOSTOP_ZIP);
            FileOutputStream output = new FileOutputStream(pathOutput);
            byte[] data = new byte[8192];
            int count;
            long lastLogTime = System.currentTimeMillis();
            int lastPercent = 0;

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
                downloaded += count;

                if (totalSize > 0) {
                    final int percent = (int) ((downloaded * 100L) / totalSize);
                    
                    if (percent != lastPercent) {
                        lastPercent = percent;
                        
                        if (progressListener != null) {
                            mainHandler.post(() -> progressListener.onProgress(percent));
                        }
                        
                        mainHandler.post(() -> updateBloodProgress(percent));
                        
                        if (percent % 10 == 0 && percent > 0) {
                            updateDialogMessage("⬇️ Downloading... " + percent + "%");
                        }
                        
                        // Update speed and ETA
                        if (percent % 2 == 0 || percent == 100) {
                            updateSpeedAndETA(downloaded, totalSize);
                        }
                    }
                }
                
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastLogTime > 5000) {
                    int percent = totalSize > 0 ? (int) ((downloaded * 100L) / totalSize) : 0;
                    logDebug("Download progress - " + percent + "% (" + downloaded + "/" + totalSize + " bytes)");
                    lastLogTime = currentTime;
                }
            }

            output.close();
            input.close();
            connection.disconnect();

            logInfo("Download complete - Downloaded: " + downloaded + " bytes");
            updateDialogMessage("📦 Download complete! Extracting...");
            updateBloodProgress(100);

            File loaderDirectory = new File(pathBase, LOADER_DIR_NAME);
            if (!loaderDirectory.exists()) {
                boolean created = loaderDirectory.mkdirs();
                logDebug("Created loader directory - Path: " + loaderDirectory.getAbsolutePath() + ", Success: " + created);
            }

            logDebug("Extracting ZIP file - Source: " + pathOutput.getAbsolutePath() + ", Destination: " + loaderDirectory.getAbsolutePath());
            updateDialogMessage("🔓 Decrypting and extracting...");
            
            ZipFile zipFile = new ZipFile(pathOutput);
            zipFile.setPassword("Crymson-Crystal-Dev".toCharArray());
            zipFile.extractAll(loaderDirectory.getAbsolutePath());
            logInfo("Extraction completed - Files extracted to: " + loaderDirectory.getAbsolutePath());

            setPermissions(loaderDirectory);
            logDebug("Permissions set - Applied to all files in: " + loaderDirectory.getAbsolutePath());

            if (pathOutput.exists()) {
                boolean deleted = pathOutput.delete();
                logDebug("Cleaned up ZIP file - Deleted: " + deleted);
            }

            SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(PREF_VERSION_KEY, serverVersion).apply();
            logInfo("Version saved to preferences: " + serverVersion);

            updateDialogMessage("✨ Extraction successful!");
            return null;

        } catch (Exception e) {
            logError("Critical failure handling update payload", e);
            
            if (pathOutput != null && pathOutput.exists()) {
                logDebug("Partial download exists - Size: " + pathOutput.length() + " bytes");
            }
            
            return "Download failed: " + e.getMessage();
        }
    }
    
    private void updateSpeedAndETA(int downloaded, int totalSize) {
        mainHandler.post(() -> {
            try {
                if (progressDialog == null) return;
                
                TextView speedText = progressDialog.findViewById(R.id.speedText);
                TextView etaText = progressDialog.findViewById(R.id.etaText);
                
                if (speedText != null && etaText != null && startTime > 0) {
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    if (elapsed > 0) {
                        int speed = (downloaded - lastDownloaded) / 1024; // KB/s
                        lastDownloaded = downloaded;
                        
                        int remaining = totalSize - downloaded;
                        int etaSeconds = speed > 0 ? remaining / (speed * 1024) : 99;
                        
                        if (speed >= 1024) {
                            speedText.setText(String.format(Locale.US, "⚡ %.1f MB/s", speed / 1024.0));
                        } else {
                            speedText.setText(String.format(Locale.US, "⚡ %d KB/s", speed));
                        }
                        
                        if (etaSeconds > 0 && etaSeconds < 3600) {
                            etaText.setText(String.format(Locale.US, "⏱️ ETA: %ds", etaSeconds));
                        } else {
                            etaText.setText("⏱️ ETA: --s");
                        }
                    }
                }
            } catch (Exception e) {
                logDebug("Speed indicator update failed: " + e.getMessage());
            }
        });
    }

    private void setPermissions(File directory) {
        if (directory == null) {
            logWarning("setPermissions called with null directory");
            return;
        }
        
        int fileCount = 0;
        if (directory.isDirectory()) {
            directory.setReadable(true, false);
            directory.setWritable(true, false);
            directory.setExecutable(true, false);
            
            File[] files = directory.listFiles();
            if (files != null) {
                fileCount = files.length;
                for (File file : files) {
                    setPermissions(file);
                }
            }
        } else {
            boolean success = true;
            success &= directory.setReadable(true, false);
            success &= directory.setWritable(true, false);
            success &= directory.setExecutable(true, false);
            
            if (!success) {
                logWarning("Failed to set all permissions for file: " + directory.getAbsolutePath());
            }
        }
        
        if (fileCount > 0) {
            logDebug("Permissions set - Processed " + fileCount + " files in directory: " + directory.getAbsolutePath());
        }
    }

    private void showOrUpdateDialog(String message, boolean isFinal) {
        if (activity == null || activity.isFinishing() || 
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
            logDebug("Activity is finishing or destroyed, skipping dialog update");
            return;
        }
        
        mainHandler.post(() -> {
            try {
                if (!dialogShowing) {
                    logDebug("Creating progress dialog - " + (isFinal ? "Final mode" : "Progress mode"));
                    progressDialog = new Dialog(activity);
                    progressDialog.setContentView(R.layout.dialog_update);
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    
                    if (progressDialog.getWindow() != null) {
                        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        progressDialog.getWindow().setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        );
                    }

                    // Initialize all UI components
                    dialogMessageView = progressDialog.findViewById(R.id.alertMessage);
                    progressFill = progressDialog.findViewById(R.id.progressFill);
                    progressPercent = progressDialog.findViewById(R.id.progressPercent);
                    
                    TextView titleView = progressDialog.findViewById(R.id.alertTitle);
                    TextView timestampView = progressDialog.findViewById(R.id.timeStamp);
                    View scanLine = progressDialog.findViewById(R.id.scanLine);
                    View borderGlow = progressDialog.findViewById(R.id.borderGlow);
                    
                    Button getKeyBtn = progressDialog.findViewById(R.id.getKeyBtn);
                    Button actionBtn = progressDialog.findViewById(R.id.alertButton);

                    // Set timestamp
                    if (timestampView != null) {
                        timestampView.setText("[" + getCurrentTimestamp().substring(11, 19) + "]");
                    }
                    
                    // Start animations
                    startGlowAnimations(titleView, progressPercent, scanLine, borderGlow);
                    
                    if (!isFinal) {
                        if (actionBtn != null) {
                            actionBtn.setText("✖ CANCEL");
                            actionBtn.setOnClickListener(v -> {
                                logInfo("User cancelled download");
                                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(60)
                                    .withEndAction(() -> {
                                        stopAllAnimations();
                                        if (progressDialog != null) progressDialog.dismiss();
                                        dialogShowing = false;
                                        executorService.shutdownNow();
                                    }).start();
                            });
                        }
                        
                        if (getKeyBtn != null) {
                            getKeyBtn.setVisibility(View.VISIBLE);
                            getKeyBtn.setOnClickListener(v -> {
                                logInfo("Get Key button clicked");
                                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(60)
                                    .withEndAction(() -> {
                                        v.animate().scaleX(1f).scaleY(1f).setDuration(60).start();
                                        downloadKeyFile();
                                    }).start();
                            });
                        }
                    } else {
                        if (actionBtn != null) {
                            actionBtn.setText("✖ CLOSE");
                            if (getKeyBtn != null) getKeyBtn.setVisibility(View.GONE);
                            actionBtn.setOnClickListener(v -> {
                                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(60)
                                    .withEndAction(() -> {
                                        stopAllAnimations();
                                        if (progressDialog != null) progressDialog.dismiss();
                                        dialogShowing = false;
                                        logDebug("Dialog closed");
                                    }).start();
                            });
                        }
                    }

                    progressDialog.show();
                    dialogShowing = true;
                    updateDialogMessage(message);
                    
                } else {
                    updateDialogMessage(message);
                    
                    if (isFinal) {
                        Button actionBtn = progressDialog.findViewById(R.id.alertButton);
                        View getKeyBtn = progressDialog.findViewById(R.id.getKeyBtn);
                        if (actionBtn != null) {
                            actionBtn.setText("✖ CLOSE");
                            if (getKeyBtn != null) getKeyBtn.setVisibility(View.GONE);
                        }
                    }
                }
            } catch (Exception e) {
                logError("Failed to show/update dialog", e);
            }
        });
    }
    
    private void startGlowAnimations(TextView title, TextView percent, View scanLine, View borderGlow) {
        // Title pulsing glow using setShadowLayer (compatible with all API levels)
        if (title != null) {
            titleGlowAnimator = ValueAnimator.ofFloat(5f, 25f, 5f);
            titleGlowAnimator.setDuration(2000);
            titleGlowAnimator.setRepeatCount(ValueAnimator.INFINITE);
            titleGlowAnimator.addUpdateListener(animation -> {
                float glow = (float) animation.getAnimatedValue();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    title.setShadowLayer(glow, 0, 0, Color.parseColor("#00FFCC"));
                    if (percent != null) percent.setShadowLayer(glow / 2, 0, 0, Color.parseColor("#00FFCC"));
                }
            });
            titleGlowAnimator.start();
        }
        
        // Border glow pulse
        if (borderGlow != null) {
            ValueAnimator borderPulse = ValueAnimator.ofFloat(0.3f, 1f, 0.3f);
            borderPulse.setDuration(1500);
            borderPulse.setRepeatCount(ValueAnimator.INFINITE);
            borderPulse.addUpdateListener(animation -> {
                float alpha = (float) animation.getAnimatedValue();
                borderGlow.setAlpha(alpha);
            });
            borderPulse.start();
        }
        
        // Scan line animation
        if (scanLine != null) {
            scanAnimator = ValueAnimator.ofFloat(0f, 1f);
            scanAnimator.setDuration(2500);
            scanAnimator.setRepeatCount(ValueAnimator.INFINITE);
            scanAnimator.setInterpolator(new DecelerateInterpolator());
            scanAnimator.addUpdateListener(animation -> {
                float fraction = (float) animation.getAnimatedValue();
                if (scanLine.getParent() instanceof FrameLayout) {
                    FrameLayout container = (FrameLayout) scanLine.getParent();
                    scanLine.setTranslationY(fraction * container.getHeight());
                }
            });
            scanAnimator.start();
        }
    }
    
    private void updateBloodProgress(int percent) {
        if (progressFill == null || progressPercent == null) return;
        
        mainHandler.post(() -> {
            try {
                progressPercent.setText(percent + "%");
                
                if (progressFill.getParent() instanceof FrameLayout) {
                    FrameLayout parent = (FrameLayout) progressFill.getParent();
                    if (parent.getWidth() > 0) {
                        int parentWidth = parent.getWidth();
                        int targetWidth = (parentWidth * percent / 100);
                        
                        ValueAnimator widthAnim = ValueAnimator.ofInt(
                            progressFill.getLayoutParams().width,
                            targetWidth
                        );
                        widthAnim.setDuration(300);
                        widthAnim.setInterpolator(new DecelerateInterpolator());
                        widthAnim.addUpdateListener(anim -> {
                            int val = (int) anim.getAnimatedValue();
                            ViewGroup.LayoutParams params = progressFill.getLayoutParams();
                            params.width = val;
                            progressFill.setLayoutParams(params);
                        });
                        widthAnim.start();
                    }
                }
                
                // Pulse animation at milestones
                if (percent == 100) {
                    progressPercent.animate()
                        .scaleX(1.2f).scaleY(1.2f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            progressPercent.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(200)
                                .start();
                        }).start();
                } else if (percent % 25 == 0 && percent > 0) {
                    progressFill.animate()
                        .alpha(0.7f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            progressFill.animate().alpha(1f).setDuration(100).start();
                        }).start();
                }
                
            } catch (Exception e) {
                logError("Error updating blood progress", e);
            }
        });
    }
    
    private void updateDialogMessage(String message) {
        if (dialogMessageView == null) return;
        
        mainHandler.post(() -> {
            try {
                String timestamp = "[" + getCurrentTimestamp().substring(11, 19) + "]";
                String formattedMsg = "> " + timestamp + " " + message;
                
                CharSequence oldText = dialogMessageView.getText();
                String newText = (oldText.length() > 0 ? oldText + "\n" : "") + formattedMsg;
                
                // Keep only last 20 lines
                String[] lines = newText.split("\n");
                if (lines.length > 20) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = lines.length - 20; i < lines.length; i++) {
                        sb.append(lines[i]).append("\n");
                    }
                    newText = sb.toString();
                }
                
                dialogMessageView.setText(newText);
                
                // Auto-scroll to bottom
                if (dialogMessageView.getParent() instanceof ScrollView) {
                    ScrollView scrollView = (ScrollView) dialogMessageView.getParent();
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                }
            } catch (Exception e) {
                logError("Failed to update dialog message", e);
            }
        });
    }
    
    private void downloadKeyFile() {
    logInfo("Downloading key file...");
    updateDialogMessage("🔐 Fetching encryption key...");
    
    executorService.execute(() -> {
        HttpURLConnection conn = null;
        try {
            String keyUrl = Link();
            URL url = new URL(keyUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.connect();
            
            final int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                Scanner scanner = new Scanner(is);
                String key = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
                scanner.close();
                is.close();
                
                SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                prefs.edit().putString("encryption_key", key).apply();
                
                mainHandler.post(() -> {
                    updateDialogMessage("✅ Key retrieved successfully!");
                    if (progressPercent != null) {
                        progressPercent.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300)
                            .withEndAction(() -> {
                                progressPercent.animate().scaleX(1f).scaleY(1f).start();
                            }).start();
                    }
                });
                logInfo("Key saved successfully");
            } else {
                final int code = responseCode;
                mainHandler.post(() -> updateDialogMessage("❌ Failed to retrieve key (HTTP " + code + ")"));
            }
        } catch (Exception e) {
            logError("Key download failed", e);
            mainHandler.post(() -> updateDialogMessage("❌ Key download error: " + e.getMessage()));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    });
}
    
    private void stopAllAnimations() {
        if (titleGlowAnimator != null) {
            titleGlowAnimator.cancel();
            titleGlowAnimator = null;
        }
        if (scanAnimator != null) {
            scanAnimator.cancel();
            scanAnimator = null;
        }
    }
}