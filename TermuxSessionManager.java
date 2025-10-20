package com.codestudio.mobile.app;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codestudio.mobile.termux.terminal.TerminalSession;
import com.codestudio.mobile.termux.terminal.TerminalSessionClient;
import com.codestudio.mobile.termux.view.TerminalView;

public class TermuxSessionManager {

    private static TerminalSession terminalSession;

    public static void startSession(Context context, TerminalView terminalView, OutputCallback callback) {

        // --- STEP 1: Define Paths using EnvironmentSetup ---
        String rootDir = EnvironmentSetup.getEnvironmentRoot(context).getAbsolutePath();
        String binPath = EnvironmentSetup.getBinDir(context).getAbsolutePath();
        String homePath = rootDir + "/home";

        String executablePath = binPath + "/sh";
        String workingDirectory = homePath;

        // --- STEP 2: Define Environment Variables (The 60% Linkage) ---
        String[] environment = new String[]{
                // CRUCIAL: Set PATH to include your embedded binaries and the system PATH
                "PATH=" + binPath + ":" + System.getenv("PATH"),
                "HOME=" + homePath,
                "TERM=xterm-256color"
        };

        String[] exportedVariables = environment;
        int sessionId = 0;

        terminalSession = new TerminalSession(
                executablePath,
                workingDirectory,
                environment,
                exportedVariables,
                sessionId,
                new TerminalSessionClient() {
                    // All client methods remain the same...
                    @Override
                    public void onTextChanged(TerminalSession session) {
                        if (callback != null) {
                            String output = session.getEmulator().getScreen().getTranscriptText();
                            callback.onOutput(output);
                        }
                    }

                    @Override
                    public void logStackTrace(String tag, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onTitleChanged(TerminalSession session) {
                    }

                    @Override
                    public void onSessionFinished(TerminalSession session) {
                    }

                    @Override
                    public void onCopyTextToClipboard(@NonNull TerminalSession session, String text) {
                    }

                    @Override
                    public void onPasteTextFromClipboard(@Nullable TerminalSession session) {
                    }

                    @Override
                    public void onClipboardText(TerminalSession session, String text) {
                    }

                    @Override
                    public void onBell(TerminalSession session) {
                    }

                    @Override
                    public void onColorsChanged(TerminalSession session) {
                    }

                    @Override
                    public void onTerminalCursorStateChange(boolean state) {
                    }

                    @Override
                    public void setTerminalShellPid(@NonNull TerminalSession session, int pid) {
                    }

                    @Override
                    public Integer getTerminalCursorStyle() {
                        return 0;
                    }

                    @Override
                    public void logError(String tag, String message) {
                    }

                    @Override
                    public void logWarn(String tag, String message) {
                    }

                    @Override
                    public void logInfo(String tag, String message) {
                    }

                    @Override
                    public void logDebug(String tag, String message) {
                    }

                    @Override
                    public void logVerbose(String tag, String message) {
                    }

                    @Override
                    public void logStackTraceWithMessage(String tag, String message, Exception e) {
                    }
                }
        );
        terminalSession.initializeEmulator(80, 24, 0, 0);
        terminalView.attachSession(terminalSession);
    }

    public static void sendCommand(String command) {
        if (terminalSession != null) {
            terminalSession.write(command + "\n");
        }
    }

    public interface OutputCallback {
        void onOutput(String output);
    }
}
