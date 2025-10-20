package com.codestudio.mobile.app;

// ExecutionListener is a functional interface used as the JNI callback
public interface ExecutionListener {
    // Called in real-time by the C thread
    void onOutputLine(String line, boolean isError);

    // Called once the entire process has finished
    void onExecutionComplete(int exitCode);
}
