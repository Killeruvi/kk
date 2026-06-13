package top.niunaijun.blackbox.utils;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileLogger {
    private static final String FILE_NAME = "redirection_debug.log";

    public static void log(Context context, String message) {
        File logFile = new File(context.getExternalFilesDir(null), FILE_NAME);
        try {
            FileOutputStream fos = new FileOutputStream(logFile, true);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String logEntry = timestamp + " - " + message + "\n";
            fos.write(logEntry.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
