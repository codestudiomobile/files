package com.codestudio.mobile.app;

public class NativeBridge {

    private static final String NATIVE_LIBRARY_NAME = "native_bridge";

    static {
        try {
            System.loadLibrary(NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("FATAL: Could not load native library: " + NATIVE_LIBRARY_NAME + ": " + e.getMessage());
        }
    }

    /**
     * Executes a shell command and streams output line-by-line back to the listener.
     *
     * @param command          The full shell command string (e.g., "python3 main.py").
     * @param workingDirectory The directory to execute the command from.
     * @param listener         The Java object that receives real-time output callbacks.
     * @return The final exit code of the shell command.
     */
    public static native int executeStreamCommand(String command, String workingDirectory, ExecutionListener listener);
}
