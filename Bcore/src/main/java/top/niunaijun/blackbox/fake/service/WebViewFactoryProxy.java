package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.os.Build;
import java.lang.reflect.Method;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;

/**
 * Modern WebViewFactoryProxy for Android 11, 12, 13, 14.
 * Handles WebView provider redirection and class loading.
 */
public class WebViewFactoryProxy extends ClassInvocationStub {
    public static final String TAG = "WebViewFactoryProxy";

    @Override
    protected Object getWho() {
        try {
            return Class.forName("android.webkit.WebViewFactory");
        } catch (Throwable t) {
            return "android.webkit.WebViewFactory";
        }
    }

    @Override
    protected void inject(Object who, Object origin) {
        // Static hooks via ProxyMethod annotations
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getProvider")
    public static class GetProvider extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            // Force WebView to initialize using host context if needed, 
            // but usually we just want to ensure the system provider is returned.
            try {
                return method.invoke(who, args);
            } catch (Throwable t) {
                Slog.e(TAG, "getProvider failed, attempting fallback", t);
                // Fallback: manually trigger getProvider via reflection if first call failed
                Method getProvider = who.getClass().getDeclaredMethod("getProvider");
                getProvider.setAccessible(true);
                return getProvider.invoke(null);
            }
        }
    }

    @ProxyMethod("getWebViewClassLoader")
    public static class GetWebViewClassLoader extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getProviderClass")
    public static class GetProviderClass extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }
}
