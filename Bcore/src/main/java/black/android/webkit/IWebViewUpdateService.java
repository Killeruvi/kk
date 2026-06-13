package black.android.webkit;


import top.niunaijun.blackreflection.annotation.BClassName;
import top.niunaijun.blackreflection.annotation.BMethod;
import top.niunaijun.blackreflection.annotation.BStaticMethod;

@BClassName("android.webkit.IWebViewUpdateService")
public interface IWebViewUpdateService {
    @BMethod
    String getCurrentWebViewPackageName();

    @BMethod
    Object waitForAndGetProvider();

    @BClassName("android.webkit.IWebViewUpdateService$Stub")
    interface Stub {
        @BStaticMethod
        Object asInterface(android.os.IBinder IBinder0);
    }
}
