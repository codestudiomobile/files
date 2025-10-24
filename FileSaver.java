package com.codestudio.mobile.app;

import android.content.Context;
import android.net.Uri;

import java.io.OutputStream;

/* loaded from: classes3.dex */
public class FileSaver {
    public static void saveFile(Context context, Uri uri, byte[] content) throws Exception {
        OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
        try {
            if (outputStream != null) {
                outputStream.write(content);
                if (outputStream != null) {
                    outputStream.close();
                    return;
                }
                return;
            }
            throw new Exception("Could not open output stream for URI: " + uri);
        } catch (Throwable th) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }
}
