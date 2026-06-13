package com.CrimsonVeilDev;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.CrimsonVeilDev.utils.AppManager;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.entity.pm.InstallResult;

import org.lsposed.lsparanoid.Obfuscate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Obfuscate
public class MAct extends AppCompatActivity {

    static {
        try {
            System.loadLibrary("CrimsonVeilDev");
        } catch (UnsatisfiedLinkError ignored) {}
    }

    public static native String apkcrc();
    public static native String exdate();

    private static final String TAG = "CrimsonMAct";
    
    // Core Application Package Registries
    private static final String PKG_BGMI = "com.pubg.imobile";
    private static final String PKG_PUBG_VNG = "com.vng.pubgmobile";
    private static final String PKG_PUBG_GLOBAL = "com.tencent.ig";
    private static final String PKG_TWITTER = "com.twitter.android";
    private static final String PKG_FACEBOOK = "com.facebook.lite";
    
    private static final int USER_ID = 0;

    private ProgressBar progressBar;
    private TextView progressText;
    private AppManager appManager;
    
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private CrimsonParticleView particleView;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(0xFF050508);
        }

        appManager = new AppManager(this);

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        FrameLayout particleContainer = findViewById(R.id.particleContainer);

        if (particleContainer != null) {
            particleView = new CrimsonParticleView(this);
            particleContainer.addView(particleView);
        }

        // Initialize Game Environment Control Anchors
        setupModularCardControl(R.id.installBtn1, PKG_BGMI, "BGMI", true);
        setupModularCardControl(R.id.installBtn2, PKG_PUBG_VNG, "PUBG VNG", true);
        setupModularCardControl(R.id.installBtn3, PKG_PUBG_GLOBAL, "PUBG Global", true);

        // Initialize Social Tools Environment Control Anchors (Now Sandbox Enabled)
        setupModularCardControl(R.id.launchBtnX, PKG_TWITTER, "X / Twitter", false);
        setupModularCardControl(R.id.launchBtnFB, PKG_FACEBOOK, "Facebook", false);
        
        Button installAllBtn = findViewById(R.id.installAllBtn);
        if (installAllBtn != null) {
            installAllBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.CrimsonVeilDev.ui.AppInstallerActivity.class);
                startActivity(intent);
            });
        }

        initializeSecureTimerLoop();
        executeDashboardEntranceCascade();
        initializeGothicGlowAnimations();
    }

    /**
     * Binds application containers dynamically to verify, track, or initialize internal virtual engines.
     */
    private void setupModularCardControl(int buttonId, final String packageName, final String label, final boolean isGame) {
        Button btn = findViewById(buttonId);
        if (btn == null) return;

        if (BlackBoxCore.get().isInstalled(packageName, USER_ID)) {
            btn.setText("LAUNCH");
        } else {
            btn.setText("INSTALL");
        }

        btn.setOnClickListener(v -> {
            v.animate().scaleX(0.93f).scaleY(0.93f).setDuration(70).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(70).withEndAction(() -> {
                    handleEnvironmentInteractionLifecycle(packageName, label, (Button) v, isGame);
                }).start();
            }).start();
        });
    }

    private void handleEnvironmentInteractionLifecycle(final String packageName, final String label, final Button actionButton, final boolean isGame) {
        if (!BlackBoxCore.get().isInstalled(packageName, USER_ID)) {
            InstallResult result = BlackBoxCore.get().installPackageAsUser(packageName, USER_ID);
            if (result.success) {
                showGothicToast(label + " Sandbox Initialized successfully.");
                actionButton.setText("LAUNCH");
            } else {
                showGothicToast("Sandbox Registration Blocked: " + result.msg);
            }
            return;
        }

        showGothicCastleInterfaceMatrix(packageName, label);
    }

    /**
     * Programmatic custom Gothic Crimson layout generator injection routine.
     * Replaces standard native components entirely with flat obsidian tiles, deep blood headers, and styled custom buttons.
     */
    private void showGothicCastleInterfaceMatrix(final String packageName, final String titleLabel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Root Container Setup
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(40, 45, 40, 45);
        
        // Midnight Onyx Background with sharp Blood Crimson outline boundaries
        GradientDrawable gothicBg = new GradientDrawable();
        gothicBg.setColor(0xFF0A0A0F); 
        gothicBg.setCornerRadius(8f);
        gothicBg.setStroke(3, 0xFF8B0000); 
        rootLayout.setBackground(gothicBg);

        // Header Title Decoration
        TextView headerText = new TextView(this);
        headerText.setText("✦ " + titleLabel.toUpperCase() + " MATRIX ✦");
        headerText.setTextColor(0xFFFF1A1A);
        headerText.setTextSize(17f);
        headerText.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        headerText.setLetterSpacing(0.15f);
        headerText.setGravity(Gravity.CENTER);
        headerText.setShadowLayer(8f, 0, 0, 0xFFCC0000);
        
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerParams.setMargins(0, 0, 0, 40);
        rootLayout.addView(headerText, headerParams);

        // Button Definition Helper Configuration
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 110);
        btnParams.setMargins(0, 12, 0, 12);

        // Action Button One: Engine Deployment execution
        Button launchBtn = new Button(this);
        launchBtn.setText("LAUNCH ENVIRONMENT");
        launchBtn.setTextColor(Color.WHITE);
        launchBtn.setTextSize(12f);
        launchBtn.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        GradientDrawable launchStyle = new GradientDrawable();
        launchStyle.setColor(0xFF5A0000);
        launchStyle.setCornerRadius(4f);
        launchBtn.setBackground(launchStyle);

        // Action Button Two: Cache Wiping deployment
        Button wipeBtn = new Button(this);
        wipeBtn.setText("WIPE CREDENTIALS CACHE");
        wipeBtn.setTextColor(0xFFFFA7A7);
        wipeBtn.setTextSize(12f);
        wipeBtn.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        GradientDrawable wipeStyle = new GradientDrawable();
        wipeStyle.setColor(0xFF1F0505);
        wipeStyle.setCornerRadius(4f);
        wipeStyle.setStroke(2, 0xFF4A0000);
        wipeBtn.setBackground(wipeStyle);

        rootLayout.addView(launchBtn, btnParams);
        rootLayout.addView(wipeBtn, btnParams);

        builder.setView(rootLayout);
        final AlertDialog gothicDialog = builder.create();
        
        if (gothicDialog.getWindow() != null) {
            gothicDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }
        gothicDialog.show();

        // Event Assignments for Custom Elements
        launchBtn.setOnClickListener(v -> {
            gothicDialog.dismiss();
            verifyAndLaunchSequence(packageName);
        });

        wipeBtn.setOnClickListener(v -> {
            gothicDialog.dismiss();
            handleClearHandshake(packageName);
        });
    }

    private void verifyAndLaunchSequence(final String packageName) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(packageName, 0);
            String gameObb = "main." + info.versionCode + "." + info.packageName + ".obb";
            File obbDestFile = new File(BEnvironment.getExternalObbDir(packageName), gameObb);

            if (obbDestFile.exists() || packageName.contains("twitter") || packageName.contains("facebook")) {
                launchGame(packageName);
            } else {
                copyObbFilesPipeline(packageName, gameObb);
            }
        } catch (PackageManager.NameNotFoundException e) {
            launchGame(packageName);
        }
    }

    private void handleClearHandshake(String packageName) {
        try {
            BlackBoxCore.get().stopPackage(packageName, USER_ID);
            BlackBoxCore.get().clearPackage(packageName, USER_ID);
            showGothicToast("Handshake tracking caches purged clean.");
        } catch (Exception e) {
            showGothicToast("Purge tracking failure.");
        }
    }

    private void launchGame(String packageName) {
        try {
            BlackBoxCore.get().launchApk(packageName, USER_ID);
            showGothicToast("Deploying execution shift environment...");
        } catch (Exception e) {
            showGothicToast("Override deployment matrix fault.");
        }
    }

    private void copyObbFilesPipeline(final String packageName, final String obbFileName) {
        final View progressCard = findViewById(R.id.progressCard);
        if (progressCard != null) {
            progressCard.setVisibility(View.VISIBLE);
            progressCard.setAlpha(0f);
            progressCard.animate().alpha(1f).setDuration(250).start();
        }
        
        progressText.setText("Locating storage containers...");
        progressBar.setProgress(0);

        new Thread(() -> {
            try {
                File obbDestDir = BEnvironment.getExternalObbDir(packageName);
                if (!obbDestDir.exists()) obbDestDir.mkdirs();

                File obbSource = null;
                StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
                for (StorageVolume storageVolume : sm.getStorageVolumes()) {
                    File obbDir;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        obbDir = storageVolume.getDirectory();
                    } else {
                        try {
                            Method getPathFile = StorageVolume.class.getMethod("getPathFile");
                            obbDir = (File) getPathFile.invoke(storageVolume);
                        } catch (Exception e) {
                            obbDir = null;
                        }
                    }

                    if (obbDir != null) {
                        File obbSubDir = new File(obbDir, "Android/obb/" + packageName);
                        File obbFile = new File(obbSubDir, obbFileName);
                        if (obbFile.exists() && obbFile.canRead()) {
                            obbSource = obbFile;
                            break;
                        }
                    }
                }

                File obbDestFile = new File(obbDestDir, obbFileName);
                if (obbSource == null || !obbSource.exists()) {
                    runOnUiThread(() -> {
                        showGothicToast("Missing OBB storage dependencies.");
                        if (progressCard != null) progressCard.setVisibility(View.GONE);
                    });
                    return;
                }

                FileInputStream fis = new FileInputStream(obbSource);
                FileOutputStream fos = new FileOutputStream(obbDestFile);

                long total = obbSource.length();
                long copied = 0;
                byte[] buf = new byte[16384]; 
                int len;
                while ((len = fis.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                    copied += len;
                    final int prog = (int) ((copied * 100) / total);
                    runOnUiThread(() -> {
                        progressBar.setProgress(prog);
                        progressText.setText("Syncing Core Matrix Stack: " + prog + "%");
                    });
                }
                fis.close();
                fos.close();

                runOnUiThread(() -> {
                    progressText.setText("Injection protocol synchronized.");
                    launchGame(packageName);
                });

            } catch (Exception e) {
                runOnUiThread(() -> showGothicToast("OBB tracking processing failure."));
            }
        }).start();
    }

    /**
     * Custom styled text layout Toast popup injection logic to avoid native green/gray standard shapes.
     */
    private void showGothicToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        View view = toast.getView();
        if (view != null) {
            GradientDrawable toastStyle = new GradientDrawable();
            toastStyle.setColor(0xFF0F0F14);
            toastStyle.setCornerRadius(6f);
            toastStyle.setStroke(2, 0xFF8B0000);
            view.setBackground(toastStyle);
            
            TextView text = view.findViewById(android.R.id.message);
            if (text != null) {
                text.setTextColor(0xFFFF4D4D);
                text.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
            }
        }
        toast.show();
    }

    private void initializeSecureTimerLoop() {
        final TextView dView = findViewById(R.id.tvD);
        final TextView hView = findViewById(R.id.tvH);
        final TextView mView = findViewById(R.id.tvM);
        final TextView sView = findViewById(R.id.tvS);

        if (dView == null || hView == null || mView == null || sView == null) return;

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String dateStr = exdate();
                    if (dateStr == null || dateStr.isEmpty()) {
                        Log.e(TAG, "Expiry date is null or empty");
                        return;
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    Date expiryDate = dateFormat.parse(dateStr);
                    long now = System.currentTimeMillis();
                    long distance = (expiryDate != null ? expiryDate.getTime() : now) - now;

                    if (distance <= 0) {
                        dView.setText("00"); hView.setText("00"); mView.setText("00"); sView.setText("00");
                        showGothicToast("Security structure configuration outdated.");
                    } else {
                        long d = distance / (24 * 60 * 60 * 1000);
                        long h = (distance / (60 * 60 * 1000)) % 24;
                        long m = (distance / (60 * 1000)) % 60;
                        long s = (distance / 1000) % 60;

                        dView.setText(String.format(Locale.US, "%02d", d));
                        hView.setText(String.format(Locale.US, "%02d", h));
                        mView.setText(String.format(Locale.US, "%02d", m));
                        sView.setText(String.format(Locale.US, "%02d", s));
                        
                        mainHandler.postDelayed(this, 1000);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Timer update verification synchronization failure: ", e);
                }
            }
        };
        mainHandler.post(timerRunnable);
    }

    private void initializeGothicGlowAnimations() {
        final TextView mainTitle = findViewById(R.id.mainTitle);
        final View countdownCard = findViewById(R.id.countdownCard);

        if (mainTitle != null) {
            ValueAnimator titleGlow = ValueAnimator.ofFloat(4f, 20f);
            titleGlow.setDuration(2000);
            titleGlow.setRepeatMode(ValueAnimator.REVERSE);
            titleGlow.setRepeatCount(ValueAnimator.INFINITE);
            titleGlow.addUpdateListener(animation -> {
                float radius = (float) animation.getAnimatedValue();
                mainTitle.setShadowLayer(radius, 0, 0, Color.parseColor("#FF0000"));
                mainTitle.invalidate();
            });
            titleGlow.start();
        }

        if (countdownCard != null) {
            ObjectAnimator pulseAlpha = ObjectAnimator.ofFloat(countdownCard, "alpha", 0.85f, 1.0f);
            pulseAlpha.setDuration(2500);
            pulseAlpha.setRepeatMode(ValueAnimator.REVERSE);
            pulseAlpha.setRepeatCount(ValueAnimator.INFINITE);
            pulseAlpha.setInterpolator(new DecelerateInterpolator());
            pulseAlpha.start();
        }
    }

    private void executeDashboardEntranceCascade() {
        View mainTitle = findViewById(R.id.mainTitle);
        View countdownCard = findViewById(R.id.countdownCard);

        if (mainTitle == null || countdownCard == null) return;

        mainTitle.setAlpha(0f); mainTitle.setTranslationY(-30f);
        countdownCard.setAlpha(0f); countdownCard.setTranslationY(40f);

        mainTitle.animate().alpha(1f).translationY(0f).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
        countdownCard.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(150)
                .setInterpolator(new OvershootInterpolator(1.0f)).start();
    }
    
    @Override
    protected void onDestroy() {
        if (timerRunnable != null) {
            mainHandler.removeCallbacks(timerRunnable);
        }
        super.onDestroy();
    }

    private static class CrimsonParticleView extends View {
        private final List<Particle> particles = new ArrayList<>();
        private final Paint paint = new Paint();
        private final Random random = new Random();
        private final int maxParticles = 45;

        public CrimsonParticleView(Context context) {
            super(context);
            paint.setAntiAlias(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (particles.size() < maxParticles && random.nextFloat() < 0.1f) {
                particles.add(new Particle(getWidth(), getHeight(), random));
            }

            for (int i = particles.size() - 1; i >= 0; i--) {
                Particle p = particles.get(i);
                p.update();

                if (p.alpha <= 0 || p.y < 0) {
                    particles.remove(i);
                } else {
                    paint.setColor(p.color);
                    paint.setAlpha(p.alpha);
                    canvas.drawCircle(p.x, p.y, p.radius, paint);
                }
            }
            invalidate();
        }

        private static class Particle {
            float x, y, radius, speedY, speedX;
            int alpha, color;
            Random rand;

            Particle(int width, int height, Random r) {
                this.rand = r;
                this.x = rand.nextInt(width > 0 ? width : 1080);
                this.y = height + 20;
                this.radius = 3f + rand.nextFloat() * 7f;
                this.speedY = 1.5f + rand.nextFloat() * 3f;
                this.speedX = -0.5f + rand.nextFloat() * 1.0f;
                this.alpha = 150 + rand.nextInt(105);
                
                if (rand.nextBoolean()) {
                    this.color = Color.parseColor("#9E1B1B");
                } else {
                    this.color = Color.parseColor("#FF5722");
                }
            }

            void update() {
                y -= speedY;
                x += speedX;
                alpha -= 2;
                if (alpha < 0) alpha = 0;
            }
        }
    }
}
