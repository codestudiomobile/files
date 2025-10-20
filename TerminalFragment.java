package com.codestudio.mobile.app;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.codestudio.mobile.R;
import com.codestudio.mobile.termux.view.TerminalView;

public class TerminalFragment extends Fragment {

    private TerminalView terminalView;
    private ConsoleInputListener listener;
    private Uri launchUri;

    public static TerminalFragment newInstance(Uri uri) {
        TerminalFragment fragment = new TerminalFragment();
        Bundle args = new Bundle();
        args.putParcelable("uri", uri);
        fragment.setArguments(args);
        Log.d("TerminalFragment", "newInstance: created");
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof ConsoleInputListener) {
            listener = (ConsoleInputListener) getActivity();
        } else {
            throw new RuntimeException(getActivity().toString() + " must implement ConsoleInputListener");
        }

        if (getArguments() != null) {
            launchUri = getArguments().getParcelable("uri");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_terminal_code_studio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        terminalView = view.findViewById(R.id.terminalView);

        // Delay layout until session is attached
        terminalView.post(() -> {
            TermuxSessionManager.startSession(requireContext(), terminalView, output -> {
                if (listener != null) listener.onOutputReceived(output);
            });
        });
    }

    public void runCommand(String command) {
        TermuxSessionManager.sendCommand(command);
        if (listener != null) listener.onUserInputSubmitted(command);
    }

    public void setAwaitingInput(boolean isWaiting) {
        // Optional: update UI state or show a loading indicator
    }

    public void appendOutput(String output) {
        TermuxSessionManager.sendCommand("echo \"" + output.replace("\"", "\\\"") + "\"");
    }

    public interface ConsoleInputListener {
        void onUserInputSubmitted(String input);

        void onOutputReceived(String output);
    }
}
