package com.codestudio.mobile.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CodeExecutionManager {

    public static void execute(String command, OutputCallback outputCallback, CompletionCallback completionCallback) {
        new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    outputCallback.onOutput(line);
                }
                while ((line = errorReader.readLine()) != null) {
                    outputCallback.onOutput(line);
                }

                process.waitFor();
                completionCallback.onComplete();

            } catch (Exception e) {
                outputCallback.onOutput("Execution error: " + e.getMessage());
                completionCallback.onComplete();
            }
        }).start();
    }

    public interface OutputCallback {
        void onOutput(String output);
    }

    public interface CompletionCallback {
        void onComplete();
    }
}
