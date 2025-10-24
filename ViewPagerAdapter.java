package com.codestudio.mobile.app;

import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public static final Uri WELCOME_URI = Uri.parse("app://com.codestudio.mobile/welcome");
    public static final Uri UNTITLED_FILE_URI = Uri.parse("app://com.codestudio.mobile/untitled");
    public final List<String> fileNames;
    public final List<Uri> fileUris;
    private final FragmentActivity activity;

    public ViewPagerAdapter(FragmentActivity fragmentActivity, List<Uri> fileUris) {
        super(fragmentActivity);
        this.activity = fragmentActivity;
        this.fileUris = fileUris;
        this.fileNames = new ArrayList();
        if (fileUris.isEmpty()) {
            SharedPreferences prefs = this.activity.getSharedPreferences(EditorActivity.PREFS_NAME, 0);
            boolean editorStartup = prefs.getBoolean(EditorActivity.KEY_EDITOR_STARTUP, false);
            boolean welcomeStartup = prefs.getBoolean(EditorActivity.KEY_WELCOME_STARTUP, true);
            if (welcomeStartup) {
                fileUris.add(WELCOME_URI);
                this.fileNames.add("Welcome");
            }
            if (editorStartup) {
                fileUris.add(UNTITLED_FILE_URI);
                this.fileNames.add("Untitled");
                return;
            }
            return;
        }
        for (Uri uri : fileUris) {
            String name = uri.getLastPathSegment();
            this.fileNames.add(name != null ? name : "Unknown File");
        }
    }

    public void closeTab(Uri fileUri) {
        int position = this.fileUris.indexOf(fileUri);
        if (position != -1) {
            long itemId = getItemId(position);
            String fragmentTag = "f" + itemId;
            Fragment fragment = this.activity.getSupportFragmentManager().findFragmentByTag(fragmentTag);
            if (fragment != null) {
                if (getItemCount() == 1) {
                    this.fileUris.clear();
                    this.fileNames.clear();
                    SharedPreferences prefs = this.activity.getSharedPreferences(EditorActivity.PREFS_NAME, 0);
                    boolean welcomeStartup = prefs.getBoolean(EditorActivity.KEY_WELCOME_STARTUP, true);
                    if (welcomeStartup) {
                        this.fileUris.add(WELCOME_URI);
                        this.fileNames.add("Welcome");
                    } else {
                        this.fileUris.add(UNTITLED_FILE_URI);
                        this.fileNames.add("Untitled");
                    }
                    notifyDataSetChanged();
                    return;
                }
                this.fileUris.remove(position);
                this.fileNames.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    public void renameTab(Uri oldUri, Uri newUri, String newDisplayName) {
        int position = this.fileUris.indexOf(oldUri);
        if (position != -1) {
            this.fileUris.set(position, newUri);
            this.fileNames.set(position, newDisplayName);
            long itemId = getItemId(position);
            String fragmentTag = "f" + itemId;
            Fragment fragment = this.activity.getSupportFragmentManager().findFragmentByTag(fragmentTag);
            if (fragment instanceof TextFragment) {
                ((TextFragment) fragment).setFileUri(newUri);
            }
            notifyItemChanged(position);
        }
    }

    public Fragment getFragment(int position) {
        if (position < 0 || position >= getItemCount()) {
            return null;
        }
        long itemId = getItemId(position);
        String fragmentTag = "f" + itemId;
        return this.activity.getSupportFragmentManager().findFragmentByTag(fragmentTag);
    }

    @NonNull
    @Override // androidx.viewpager2.adapter.FragmentStateAdapter
    public Fragment createFragment(int position) {
        Uri fileUri = this.fileUris.get(position);
        String fileName = this.fileNames.get(position);
        if (this.activity instanceof MainActivity) {
            ((MainActivity) this.activity).currentFileUri = fileUri;
            ((MainActivity) this.activity).currentMimeType = ((MainActivity) this.activity).getMimeType(fileUri);
        }
        /*if (fileName.startsWith("Run: ")) {
            Fragment fragment = new TerminalFragment();
            return fragment;
        }*/
        if (fileUri.equals(WELCOME_URI)) {
            return new WelcomeFragment();
        }
        if (fileUri.equals(UNTITLED_FILE_URI)) {
            return TextFragment.newInstance(UNTITLED_FILE_URI);
        }
        try {
            return TextFragment.newInstance(fileUri);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.activity, "Error opening file for editing.", Toast.LENGTH_SHORT).show();
            return new WelcomeFragment();
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.fileUris.size();
    }

    public List<Uri> getFileUris() {
        return this.fileUris;
    }

    public List<String> getFileNames() {
        return this.fileNames;
    }

    public int addTab(Uri uri, String fileName) {
        for (int i = 0; i < this.fileUris.size(); i++) {
            if (this.fileUris.get(i).equals(uri) && !fileName.startsWith("Run:")) {
                return i;
            }
        }
        this.fileUris.add(uri);
        this.fileNames.add(fileName);
        notifyItemInserted(this.fileUris.size() - 1);
        return this.fileUris.size() - 1;
    }

    public int findTabPositionByName(String name) {
        for (int i = 0; i < this.fileNames.size(); i++) {
            if (this.fileNames.get(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public void removeTab(int position) {
        if (position >= 0 && position < this.fileUris.size()) {
            this.fileUris.remove(position);
            this.fileNames.remove(position);
            notifyDataSetChanged();
            if (this.fileUris.isEmpty()) {
                SharedPreferences prefs = this.activity.getSharedPreferences(EditorActivity.PREFS_NAME, 0);
                boolean editorStartup = prefs.getBoolean(EditorActivity.KEY_EDITOR_STARTUP, false);
                boolean welcomeStartup = prefs.getBoolean(EditorActivity.KEY_WELCOME_STARTUP, true);
                if (welcomeStartup) {
                    this.fileUris.add(WELCOME_URI);
                    this.fileNames.add("Welcome");
                }
                if (editorStartup) {
                    this.fileUris.add(UNTITLED_FILE_URI);
                    this.fileNames.add("Untitled");
                }
            }
        }
    }

    @Override
    // androidx.viewpager2.adapter.FragmentStateAdapter, androidx.recyclerview.widget.RecyclerView.Adapter
    public long getItemId(int position) {
        return this.fileUris.get(position).toString().hashCode();
    }

    @Override // androidx.viewpager2.adapter.FragmentStateAdapter
    public boolean containsItem(long itemId) {
        for (Uri uri : this.fileUris) {
            if (uri.hashCode() == itemId) {
                return true;
            }
        }
        return false;
    }

    public void removeAllTabs() {
        this.fileUris.clear();
        this.fileNames.clear();
        if (this.fileUris.isEmpty()) {
            SharedPreferences prefs = this.activity.getSharedPreferences(EditorActivity.PREFS_NAME, 0);
            boolean editorStartup = prefs.getBoolean(EditorActivity.KEY_EDITOR_STARTUP, false);
            boolean welcomeStartup = prefs.getBoolean(EditorActivity.KEY_WELCOME_STARTUP, true);
            if (welcomeStartup) {
                this.fileUris.add(WELCOME_URI);
                this.fileNames.add("Welcome");
            }
            if (editorStartup) {
                this.fileUris.add(UNTITLED_FILE_URI);
                this.fileNames.add("Untitled");
            }
        }
        notifyDataSetChanged();
    }

    public void removeOtherTabs(int currentPosition) {
        if (currentPosition < 0 || currentPosition >= this.fileUris.size()) {
            return;
        }
        Uri currentUri = this.fileUris.get(currentPosition);
        String currentName = this.fileNames.get(currentPosition);
        this.fileUris.clear();
        this.fileNames.clear();
        this.fileUris.add(currentUri);
        this.fileNames.add(currentName);
        notifyDataSetChanged();
    }

    public List<FilesAdapter.FileContentItem> getOpenFilesContent() {
        byte[] content;
        List<FilesAdapter.FileContentItem> filesToSave = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            Uri uri = this.fileUris.get(i);
            if (!uri.equals(WELCOME_URI) && !uri.equals(UNTITLED_FILE_URI) && !this.fileNames.get(i).startsWith("Run:")) {
                long itemId = getItemId(i);
                String fragmentTag = "f" + itemId;
                Fragment fragment = this.activity.getSupportFragmentManager().findFragmentByTag(fragmentTag);
                if (fragment instanceof TextFragment) {
                    TextFragment textFragment = (TextFragment) fragment;
                    if (!textFragment.isSaved() && (content = textFragment.getContents()) != null) {
                        filesToSave.add(new FilesAdapter.FileContentItem(uri, content));
                    }
                }
            }
        }
        return filesToSave;
    }
}
