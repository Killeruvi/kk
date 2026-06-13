package top.niunaijun.blackbox.fake.service;

import android.os.IBinder;

import black.android.os.BRServiceManager;
import black.android.webkit.BRIWebViewUpdateServiceStub;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import java.lang.reflect.Method;

public class IWebViewUpdateServiceProxy extends BinderInvocationStub {
    public static final String TAG = "IWebViewUpdateServiceProxy";

    public IWebViewUpdateServiceProxy() {
        super(BRServiceManager.get().getService("webviewupdate"));
    }

    @Override
    protected Object getWho() {
        IBinder service = BRServiceManager.get().getService("webviewupdate");
        if (service != null) {
            return BRIWebViewUpdateServiceStub.get().asInterface(service);
        }
        return null;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("webviewupdate");
    }

    @Override
    public boolean isBadEnv() {
        return BRServiceManager.get().getService("webviewupdate") != this;
    }

    @ProxyMethod("getCurrentWebViewPackageName")
    public static class GetCurrentWebViewPackageName extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("waitForAndGetProvider")
    public static class WaitForAndGetProvider extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }
}
