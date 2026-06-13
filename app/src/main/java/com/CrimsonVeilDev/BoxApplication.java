package com.CrimsonVeilDev;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.CrimsonVeilDev.utils.Prefs;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.configuration.AppLifecycleCallback;
import top.niunaijun.blackbox.app.configuration.ClientConfiguration;
import top.niunaijun.blackbox.core.system.api.MetaActivationManager;

public class BoxApplication extends Application {

    static {
        System.loadLibrary("CrimsonVeilDev");
    }

    public static native String getSdkKey();

    private static final String TAG = "BoxApplication";
    private static File logFile;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
    
    private final String[] process_names = {
            "com.pubg.krmobile",   // KOREA - 1
            "com.tencent.ig",      // GLOBAL - 2
            "com.rekoo.pubgm",     // TAIWAN - 3
            "com.vng.pubgmobile",  // VIETNAM - 4
            "com.pubg.imobile"     // BGMI - 5
    };

    // Initialize logging to external directory
    private void initLogging(Context context) {
        try {
            File externalDir;
            // Try external storage first
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                externalDir = new File(context.getExternalFilesDir(null), "logs");
            } else {
                // Fallback to internal storage if external not available
                externalDir = new File(context.getFilesDir(), "logs");
            }
            
            if (!externalDir.exists()) {
                externalDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            logFile = new File(externalDir, "app_log_" + timestamp + ".txt");
            
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            
            writeLog("=== Application Started ===");
            writeLog("Log file created at: " + logFile.getAbsolutePath());
            writeLog("External storage state: " + Environment.getExternalStorageState());
            writeLog("Log directory: " + externalDir.getAbsolutePath());
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize logging to external directory", e);
            e.printStackTrace();
        }
    }

    // Write log to file
    private void writeLog(String message) {
        try {
            if (logFile != null && logFile.exists()) {
                FileWriter writer = new FileWriter(logFile, true);
                writer.write(dateFormat.format(new Date()) + " - " + message + "\n");
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to write log to file", e);
        }
    }

    // Write exception with stack trace
    private void writeException(String message, Throwable e) {
        try {
            if (logFile != null && logFile.exists()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String stackTrace = sw.toString();
                
                FileWriter writer = new FileWriter(logFile, true);
                writer.write(dateFormat.format(new Date()) + " - ERROR: " + message + "\n");
                writer.write("Stack trace:\n" + stackTrace + "\n");
                writer.flush();
                writer.close();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Failed to write exception to log file", ex);
        }
    }

    // Bypass activation using reflection - FIXED for your MetaActivationManager
    private void bypassActivation() {
        try {
            writeLog("Attempting to bypass activation via reflection...");
            
            // Directly set licenseActivated to true using reflection
            Field activatedField = MetaActivationManager.class.getDeclaredField("licenseActivated");
            activatedField.setAccessible(true);
            activatedField.setBoolean(null, true);
            writeLog("Successfully set licenseActivated to true via reflection");
            
            // Set license message
            Field messageField = MetaActivationManager.class.getDeclaredField("licenseMessage");
            messageField.setAccessible(true);
            messageField.set(null, "Activation bypassed successfully");
            writeLog("Successfully set licenseMessage via reflection");
            
            writeLog("=== ACTIVATION BYPASSED SUCCESSFULLY ===");
            
            // Verify bypass worked
            boolean isActivated = MetaActivationManager.isLicenseActivated();
            writeLog("Verification - License activated: " + isActivated);
            
        } catch (Exception e) {
            writeLog("Failed to bypass activation: " + e.getMessage());
            writeException("Reflection bypass error", e);
            
            // Alternative method - try to use reflection to call private methods
            tryAlternativeBypass();
        }
    }
    
    // Alternative bypass method if direct field access fails
    private void tryAlternativeBypass() {
        try {
            writeLog("Trying alternative bypass method...");
            
            // Try to find and invoke any verification bypass method
            Method[] methods = MetaActivationManager.class.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().contains("bypass") || 
                    method.getName().contains("force") ||
                    method.getName().contains("setActivated")) {
                    method.setAccessible(true);
                    method.invoke(null);
                    writeLog("Invoked bypass method: " + method.getName());
                }
            }
            
            // Try to set using reflection with different field names
            try {
                Field field = MetaActivationManager.class.getDeclaredField("mActivated");
                field.setAccessible(true);
                field.setBoolean(null, true);
                writeLog("Set mActivated field");
            } catch (NoSuchFieldException e) {
                // Field doesn't exist, continue
            }
            
            try {
                Field field = MetaActivationManager.class.getDeclaredField("isActivated");
                field.setAccessible(true);
                field.setBoolean(null, true);
                writeLog("Set isActivated field");
            } catch (NoSuchFieldException e) {
                // Field doesn't exist, continue
            }
            
        } catch (Exception e) {
            writeLog("Alternative bypass also failed: " + e.getMessage());
        }
    }

    // Call activateBox with timeout to prevent hanging
    private void callActivateBoxWithTimeout(String licenseKey) {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] completed = {false};
        
        // Run activation in background thread
        Thread activationThread = new Thread(() -> {
            try {
                writeLog("Calling MetaActivationManager.activateSdk() in background thread...");
                MetaActivationManager.activateSdk(licenseKey);
                completed[0] = true;
                writeLog("MetaActivationManager.activateSdk() completed");
            } catch (Exception e) {
                writeLog("Exception in activateSdk: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        activationThread.start();
        
        // Wait for max 3 seconds
        try {
            boolean finished = latch.await(3, TimeUnit.SECONDS);
            if (!finished) {
                writeLog("WARNING: activateSdk timed out after 3 seconds!");
                activationThread.interrupt();
            }
        } catch (InterruptedException e) {
            writeLog("Interrupted while waiting for activation");
        }
    }
    
    // Force activation status
    private void forceActivationStatus() {
        try {
            writeLog("Forcing activation status...");
            
            // Directly set the boolean field
            Field activatedField = MetaActivationManager.class.getDeclaredField("licenseActivated");
            activatedField.setAccessible(true);
            activatedField.setBoolean(null, true);
            
            // Also set any other potential fields
            try {
                Field isActivatedField = MetaActivationManager.class.getDeclaredField("isActivated");
                isActivatedField.setAccessible(true);
                isActivatedField.setBoolean(null, true);
            } catch (Exception e) {
                // Ignore
            }
            
            writeLog("Force activation completed");
            
        } catch (Exception e) {
            writeLog("Force activation failed: " + e.getMessage());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        
        // Initialize logging first
        initLogging(base);
        
        writeLog("BoxApplication attachBaseContext started");
        Log.d(TAG, "BoxApplication attachBaseContext started");
        
        Prefs prefs = new Prefs(base);
        try {
            writeLog("Initializing BlackBoxCore...");
            Log.d(TAG, "Initializing BlackBoxCore...");
            
            BlackBoxCore.get().doAttachBaseContext(base, new ClientConfiguration() {
                @Override
                public String getHostPackageName() {
                    String pkgName = base.getPackageName();
                    writeLog("Host package name: " + pkgName);
                    return pkgName;
                }

                @Override
                public boolean isHideRoot() {
                    writeLog("Hide root enabled");
                    return true;
                }

                @Override
                public boolean isHideXposed() {
                    writeLog("Hide Xposed enabled");
                    return true;
                }

                @Override
                public boolean isEnableDaemonService() {
                    writeLog("Daemon service disabled");
                    return false;
                }

                public boolean requestInstallPackage(File file) {
                    writeLog("Package installation requested: " + (file != null ? file.getAbsolutePath() : "null"));
                    return false;
                }
            });
            
            writeLog("BlackBoxCore initialization completed");
            writeLog("App name check completed");
            Log.d(TAG, "BlackBoxCore initialization completed");
            
        } catch (Exception e) {
            String errorMsg = "Error in attachBaseContext: " + e.getMessage();
            writeException(errorMsg, e);
            Log.e(TAG, errorMsg, e);
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        writeLog("onCreate started");
        
        try {
            BlackBoxCore.get().doCreate();
            writeLog("BlackBoxCore doCreate completed");
        } catch (Exception e) {
            writeException("BlackBoxCore doCreate failed", e);
        }

        // Register activity lifecycle callbacks to trigger activation when first activity is created
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            private boolean isActivationChecked = false;

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                writeLog("Activity created: " + activity.getClass().getSimpleName());
                if (!isActivationChecked) {
                    isActivationChecked = true;
                    writeLog("Triggering SDK activation...");
                    
                    // FIRST: Bypass activation immediately using reflection
                    bypassActivation();
                    
                    // Get license key from native method
                    String licenseKey = getSdkKey();
                    writeLog("License key obtained (length: " + (licenseKey != null ? licenseKey.length() : 0) + ")");
                    
                    // SECOND: Call activateBox with timeout (non-blocking)
                    callActivateBoxWithTimeout(licenseKey);
                    
                    // THIRD: Check activation status (will be true from our bypass)
                    boolean isActivated = MetaActivationManager.isLicenseActivated();
                    
                    String activationResult = "Activation result: " + isActivated;
                    writeLog(activationResult);
                    Log.d(TAG, activationResult);
                    
                    // If still not activated, force it
                    if (!isActivated) {
                        writeLog("WARNING: Still not activated after bypass! Forcing activation status...");
                        forceActivationStatus();
                        
                        // Re-check
                        isActivated = MetaActivationManager.isLicenseActivated();
                        writeLog("After force - Activation result: " + isActivated);
                    }
                    
                    // Final verification
                    if (MetaActivationManager.isLicenseActivated()) {
                        writeLog("✓✓✓ ACTIVATION SUCCESSFUL ✓✓✓");
                    } else {
                        writeLog("✗✗✗ ACTIVATION FAILED ✗✗✗");
                    }
                }
            }

            @Override public void onActivityStarted(Activity activity) {}
            @Override public void onActivityResumed(Activity activity) {}
            @Override public void onActivityPaused(Activity activity) {}
            @Override public void onActivityStopped(Activity activity) {}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override public void onActivityDestroyed(Activity activity) {}
        });
        
        writeLog("Activity lifecycle callbacks registered");

        // Keep your existing AppLifecycleCallback for loading native libs
        BlackBoxCore.get().addAppLifecycleCallback(new AppLifecycleCallback() {
            @Override
            public void beforeCreateApplication(String packageName, String processName, Context context, int userId) {
                writeLog("beforeCreateApplication - Package: " + packageName + ", Process: " + processName + ", UserId: " + userId);
            }

            @Override
            public void beforeApplicationOnCreate(String packageName, String processName, Application application, int userId) {
                writeLog("beforeApplicationOnCreate - Package: " + packageName + ", Process: " + processName);
                
                try {
                    for (String pkg : process_names) {
                        if (pkg.equals(packageName) && pkg.equals(processName)) {
                            writeLog("Matched game package: " + pkg);

                            // BGMI loader
                            if (pkg.equals("com.pubg.imobile")) {
                                File p1 = new File(getFilesDir(), "loader/libbgmi.so");
                                writeLog("BGMI loader path: " + p1.getAbsolutePath());
                                if (p1.exists()) {
                                    System.load(p1.getAbsolutePath());
                                    writeLog("Successfully loaded libbgmi.so for BGMI");
                                    Log.d("App", "Loaded libbgmi.so for BGMI");
                                } else {
                                    String error = "libbgmi.so not found at: " + p1.getAbsolutePath();
                                    writeLog(error);
                                    Log.e("App", error);
                                }
                            }

                            // PUBG Global loader
                            if (pkg.equals("com.tencent.ig")) {
                                File p2 = new File(getFilesDir(), "loader/libpubgm.so");
                                writeLog("PUBG Global loader path: " + p2.getAbsolutePath());
                                if (p2.exists()) {
                                    System.load(p2.getAbsolutePath());
                                    writeLog("Successfully loaded libpubgm.so for PUBG Global");
                                    Log.d("App", "Loaded libpubgm.so for PUBG Global");
                                } else {
                                    String error = "libpubgm.so not found at: " + p2.getAbsolutePath();
                                    writeLog(error);
                                    Log.e("App", error);
                                }
                            }

                            break;
                        }
                    }
                } catch (UnsatisfiedLinkError e) {
                    String errorMsg = "Native lib load failed: " + e.getMessage();
                    writeException(errorMsg, e);
                    Log.e("App", errorMsg, e);
                    e.printStackTrace();
                    System.exit(0);
                } catch (Exception e) {
                    String errorMsg = "Error loading game libs: " + e.getMessage();
                    writeException(errorMsg, e);
                    Log.e("App", errorMsg, e);
                    e.printStackTrace();
                    System.exit(0);
                }
            }

            @Override
            public void afterApplicationOnCreate(String packageName, String processName, Application application, int userId) {
                writeLog("afterApplicationOnCreate - Package: " + packageName + ", Process: " + processName);
            }
        });
        
        writeLog("AppLifecycleCallback registered");
        writeLog("=== Application initialization completed ===");
    }
}