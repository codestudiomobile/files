package com.codestudio.mobile.app; // <--- CORRECTED PACKAGE

import android.content.Context;

import java.io.File;

// Assumes the setup logic to copy assets/bin and assets/lib has been run.
public class EnvironmentSetup {

    // Destination root on the device (e.g., /data/data/your.app.package/files)
    public static File getEnvironmentRoot(Context context) {
        return context.getFilesDir();
    }

    // Path where all binaries are installed
    public static File getBinDir(Context context) {
        return new File(getEnvironmentRoot(context), "bin");
    }

    // NOTE: The full setup function (to copy assets and set executable permissions)
    // should be included here.
}
