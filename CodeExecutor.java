package com.codestudio.mobile.app;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CodeExecutor {

    // Core execution function now receives real-time callbacks
    public static void executeCommand(Context context, String language, String code, ExecutionListener listener) {

        // This execution runs on the thread started by the calling code (e.g., TerminalFragment)
        // The JNI call itself will spawn the command and the pipe-reading thread.

        // Setup environment (Must be called before running any Termux binaries)
        File binDir = EnvironmentSetup.getBinDir(context);
        File workDir = context.getFilesDir();
        File codeFile = new File(workDir, "main.txt");

        try (FileOutputStream fos = new FileOutputStream(codeFile)) {
            fos.write(code.getBytes());
        } catch (IOException e) {
            // Report error via the final callback
            listener.onOutputLine("Failed to write code file: " + e.getMessage(), true);
            listener.onExecutionComplete(-1);
            return;
        }

        // --- Define the full command to execute ---
        // CRUCIAL: Must prepend the correct PATH to find Termux binaries (e.g., python3)
        String embeddedPath = "PATH=" + binDir.getAbsolutePath() + ":$PATH";
        String executionCommand = String.format("%s python3 %s", embeddedPath, codeFile.getAbsolutePath());

        // We use 'sh -c' to ensure environment variables are correctly set before the command
        String fullShellCommand = String.format("sh -c '%s'", executionCommand);

        // --- Run the JNI Streaming Execution ---
        int exitCode = NativeBridge.executeStreamCommand(fullShellCommand, workDir.getAbsolutePath(), listener);

        // Cleanup
        codeFile.delete();

        // JNI function blocks until the command is done, so we notify completion here
        ((Activity) context).runOnUiThread(() -> {
            listener.onExecutionComplete(exitCode);
        });
    }
}
