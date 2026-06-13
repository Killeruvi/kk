package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.utils.Slog;

/**
 * Redundant suffix logic removed.
 * BActivityThread handles WebView.setDataDirectorySuffix(processName) consistently.
 */
public class WebViewProxy extends ClassInvocationStub {
    public static final String TAG = "WebViewProxy";

    @Override
    protected Object getWho() {
        try {
            return Class.forName("android.webkit.WebView");
        } catch (Throwable t) {
            return "android.webkit.WebView";
        }
    }

    @Override
    protected void inject(Object who, Object origin) {
        // No-op: Suffix is handled in BActivityThread
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
