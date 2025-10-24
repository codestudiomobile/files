package com.codestudio.mobile.app;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.codestudio.mobile.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/*
 * RecyclerView.Adapter for displaying files and folders,
 * with support for expanding folders and saving file content.
 */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {
    private static final String TAG = "FilesAdapter";
    public final List<FileItem> fileList;
    private final Context context;
    private final OnFileClickListener onFileClickListener;
    private final SaveAsCallback saveAsCallback;

    // --- Constructor ---

    public FilesAdapter(Context context, List<FileItem> fileList, OnFileClickListener onFileClickListener, SaveAsCallback saveAsCallback) {
        this.context = context;
        this.fileList = fileList;
        this.onFileClickListener = onFileClickListener;
        this.saveAsCallback = saveAsCallback;
    }

    // --- RecyclerView.Adapter Overrides ---

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_list_code_studio, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        FileItem item = this.fileList.get(position);
        holder.bind(item, position);
        // Apply padding based on file/folder depth
        int paddingStart = item.depth * 24;
        ViewCompat.setPaddingRelative(holder.itemView, dpToPx(paddingStart, this.context), holder.itemView.getPaddingTop(), holder.itemView.getPaddingEnd(), holder.itemView.getPaddingBottom());
    }

    @Override
    public int getItemCount() {
        return this.fileList.size();
    }

    // --- Public Adapter Methods ---

    public void refresh() {
        if (this.context instanceof Activity) {
            ((Activity) this.context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clearFileList();
                }
            });
        }
    }

    /* Original: m76lambda$refresh$0$comdiplomaprojectFilesAdapter */
    private void clearFileList() {
        this.fileList.clear();
        notifyDataSetChanged();
    }

    public void saveAllFiles(final List<FileContentItem> filesToSave) {
        if (this.saveAsCallback == null) {
            Log.e(TAG, "SaveAsCallback is null. Cannot save untitled files.");
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    performSaveAllFiles(filesToSave);
                }
            }).start();
        }
    }

    /* Original: m79lambda$saveAllFiles$3$comdiplomaprojectFilesAdapter */
    private void performSaveAllFiles(List<FileContentItem> filesToSave) {
        if (filesToSave == null || filesToSave.isEmpty()) {
            return;
        }
        Handler mainHandler = new Handler(Looper.getMainLooper());
        int savedCount = 0;
        Iterator<FileContentItem> it = filesToSave.iterator();
        while (it.hasNext()) {
            final FileContentItem file = it.next();
            try (OutputStream os = this.context.getContentResolver().openOutputStream(file.getUri(), "rwt")) {
                if (os != null) {
                    os.write(file.getContent());
                    savedCount++;
                }
            } catch (IOException e) {
                Log.e(TAG, "Error saving file " + file.getUri().toString() + ": " + e.getMessage());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showSaveFailureToast(file);
                    }
                });
            }
        }
        final int finalSavedCount = savedCount;
        if (finalSavedCount > 0) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    showSaveSuccessToast(finalSavedCount);
                }
            });
        }
    }

    /* Original: m77lambda$saveAllFiles$1$comdiplomaprojectFilesAdapter */
    private void showSaveFailureToast(FileContentItem file) {
        Toast.makeText(this.context.getApplicationContext(), "Failed to save: " + file.getUri().getLastPathSegment(), Toast.LENGTH_LONG).show();
    }

    /* Original: m78lambda$saveAllFiles$2$comdiplomaprojectFilesAdapter */
    private void showSaveSuccessToast(int finalSavedCount) {
        Toast.makeText(this.context.getApplicationContext(), "Successfully saved " + finalSavedCount + " file(s).", Toast.LENGTH_SHORT).show();
    }

    public void collapseAllFolders() {
        if (this.context instanceof Activity) {
            ((Activity) this.context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    executeCollapseAllFolders();
                }
            });
        }
    }

    /* Original: m75lambda$collapseAllFolders$4$comdiplomaprojectFilesAdapter */
    private void executeCollapseAllFolders() {
        for (int i = this.fileList.size() - 1; i >= 0; i--) {
            FileItem item = this.fileList.get(i);
            // Remove all non-root level items
            if (item.depth > 0) {
                this.fileList.remove(i);
            }
            // Reset folder expansion state
            if (item.isDirectory) {
                item.isExpanded = false;
            }
        }
        notifyDataSetChanged();
    }

    public void expandFolder(int position) {
        FileItem folder = this.fileList.get(position);
        if (folder.isExpanded) {
            return;
        }
        // This line looks like a remnant of decompilation creating a temporary ViewHolder just to call its logic.
        // It's better to call the logic directly or ensure the expandFolderLogic is accessible/implemented correctly.
        // Assuming the FileViewHolder is the correct place for folder logic, we'll instantiate and call it:
        new FileViewHolder(LayoutInflater.from(this.context).inflate(R.layout.item_file_list_code_studio, null)).expandFolderLogic(position);
    }

    // --- Utility Method ---

    private int dpToPx(int dp, Context context) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    // --- Interfaces ---

    public interface OnFileClickListener {
        void onFileClicked(Uri uri, String str);

        void onFileContextMenuRequest(Uri uri, View view);
    }

    public interface SaveAsCallback {
        void requestSaveAs(byte[] bArr);
    }

    // --- Data Classes (Assuming these are defined elsewhere or are internal static classes) ---

    // Note: Assuming FileItem is a class defined elsewhere or within the file's original structure.
    // For compilation, a minimal definition of FileItem is needed, but I'll leave the class structure
    // as it was in the decompiled code and assume the compiler can resolve it.

    /* Assuming FileItem is a public class/record in this package or imported */
    // public static class FileItem { ... }

    public static class FileContentItem {
        private final byte[] content;
        private final Uri uri;

        public FileContentItem(Uri uri, byte[] content) {
            this.uri = uri;
            this.content = content;
        }

        public Uri getUri() {
            return this.uri;
        }

        public byte[] getContent() {
            return this.content;
        }
    }

    // --- ViewHolder Class ---

    class FileViewHolder extends RecyclerView.ViewHolder {
        private final ImageView fileIcon;
        private final TextView fileName;

        public FileViewHolder(View itemView) {
            super(itemView);
            this.fileIcon = itemView.findViewById(R.id.fileIcon);
            this.fileName = itemView.findViewById(R.id.fileName);

            // Using the new onClickListener logic from the decompiled code,
            // which delegates to the `handleItemClick()` method.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleItemClick();
                }
            });
        }

        public void bind(final FileItem item, final int position) {
            this.fileName.setText(item.displayName);

            // Rebinding the listeners here seems redundant with the constructor, but matches the decompiled code's logic.
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onFileViewClick(item, position, view);
                }
            });
            this.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return onFileViewLongClick(item, view);
                }
            });

            // Set file icon based on type and expansion state
            if (item.isDirectory) {
                this.fileIcon.setImageResource(item.isExpanded ? R.drawable.ic_folder_open : R.drawable.ic_folder_closed);
                return;
            }
            String mime = item.mimeType != null ? item.mimeType : "";
            if (mime.startsWith("image/")) {
                this.fileIcon.setImageResource(R.drawable.ic_image_file);
            } else if (mime.startsWith("audio/")) {
                this.fileIcon.setImageResource(R.drawable.ic_audio_file);
            } else if (mime.startsWith("video/")) {
                this.fileIcon.setImageResource(R.drawable.ic_video_file);
            } else if (mime.startsWith("text/") || mime.equals("application/json")) {
                this.fileIcon.setImageResource(R.drawable.ic_text_file);
            } else {
                this.fileIcon.setImageResource(R.drawable.ic_unsupported_file);
            }
        }

        /* Original: m80lambda$bind$1$comdiplomaprojectFilesAdapter$FileViewHolder */
        private void onFileViewClick(FileItem item, int position, View v) {
            if (!item.isDirectory) {
                FilesAdapter.this.onFileClickListener.onFileClicked(item.uri, item.displayName);
            } else if (item.isExpanded) {
                collapseFolder(position);
            } else {
                FilesAdapter.this.expandFolder(position);
            }
        }

        /* Original: m81lambda$bind$2$comdiplomaprojectFilesAdapter$FileViewHolder */
        private boolean onFileViewLongClick(FileItem item, View v) {
            FilesAdapter.this.onFileClickListener.onFileContextMenuRequest(item.uri, v);
            return true;
        }

        private void handleItemClick() {
            int position = getAdapterPosition();
            if (position == -1) {
                return;
            }
            FileItem clickedItem = FilesAdapter.this.fileList.get(position);
            if (!clickedItem.isDirectory) {
                if (FilesAdapter.this.onFileClickListener != null) {
                    // Assuming MainActivity is the context and has this method.
                    // This is an external dependency that might require correction.
                    if (FilesAdapter.this.context instanceof MainActivity) {
                        ((MainActivity) FilesAdapter.this.context).closeLeftNavigation();
                    }
                    FilesAdapter.this.onFileClickListener.onFileClicked(clickedItem.uri, clickedItem.displayName);
                }
            } else if (clickedItem.isExpanded) {
                collapseFolder(position);
            } else {
                expandFolderLogic(position);
            }
        }

        /* package-private */
        void expandFolderLogic(final int position) {
            final FileItem folder = FilesAdapter.this.fileList.get(position);
            folder.isExpanded = true;

            // Update the UI for the expanded folder icon
            ((Activity) FilesAdapter.this.context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateItemIcon(position);
                }
            });

            // Run folder content loading on a background thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadFolderContents(folder, position);
                }
            }).start();
        }

        /* Original: m82x523a7d52 */
        private void updateItemIcon(int position) {
            FilesAdapter.this.notifyItemChanged(position);
        }

        /* Original: m84x5060e556 */
        private void loadFolderContents(FileItem folder, final int position) {
            String documentId = DocumentsContract.getDocumentId(folder.uri);
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(folder.uri, documentId);
            ArrayList<FileItem> directories = new ArrayList<>();
            ArrayList<FileItem> files = new ArrayList<>();

            try {
                // Query columns: DOCUMENT_ID, DISPLAY_NAME, MIME_TYPE
                Cursor cursor = FilesAdapter.this.context.getContentResolver().query(childrenUri, new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            String childDocId = cursor.getString(0);
                            String childName = cursor.getString(1);
                            String mimeType = cursor.getString(2);
                            boolean isDirectory = DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType);
                            Uri childUri = DocumentsContract.buildDocumentUriUsingTree(folder.uri, childDocId);
                            // FileItem constructor arguments: (Context context, Uri uri, String displayName, boolean isDirectory, int depth)
                            FileItem childItem = new FileItem(FilesAdapter.this.context, childUri, childName, isDirectory, folder.depth + 1);

                            if (isDirectory) {
                                directories.add(childItem);
                            } else {
                                files.add(childItem);
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e(FilesAdapter.TAG, "Error querying folder contents: " + e.getMessage());
            }

            // Sort directories and files alphabetically
            Comparator<FileItem> nameComparator = new Comparator<FileItem>() {
                @Override
                public int compare(FileItem item1, FileItem item2) {
                    return item1.displayName.compareToIgnoreCase(item2.displayName);
                }
            };
            Collections.sort(directories, nameComparator);
            Collections.sort(files, nameComparator);

            // Combine and add to the file list (directories first, then files)
            final List<FileItem> newItems = new ArrayList<>();
            newItems.addAll(directories);
            newItems.addAll(files);

            // Update the adapter on the main thread
            ((Activity) FilesAdapter.this.context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    insertNewItems(newItems, position);
                }
            });
        }

        /* Original: m83x50d74b55 */
        private void insertNewItems(List<FileItem> newItems, int position) {
            if (!newItems.isEmpty()) {
                FilesAdapter.this.fileList.addAll(position + 1, newItems);
                FilesAdapter.this.notifyItemRangeInserted(position + 1, newItems.size());
            }
        }

        private void collapseFolder(int position) {
            FileItem folder = FilesAdapter.this.fileList.get(position);
            folder.isExpanded = false;
            FilesAdapter.this.notifyItemChanged(position);

            int removalCount = 0;
            // Iterate backwards from the end of the list to remove all nested items
            for (int i = FilesAdapter.this.fileList.size() - 1; i > position; i--) {
                FileItem currentItem = FilesAdapter.this.fileList.get(i);
                // Items with a greater depth than the collapsed folder's depth are nested items
                if (currentItem.depth > folder.depth) {
                    FilesAdapter.this.fileList.remove(i);
                    removalCount++;
                } else {
                    // Since the list is structure-based, once we hit an item not nested, we can stop.
                    break;
                }
            }

            if (removalCount > 0) {
                int startPosition = position + 1;
                FilesAdapter.this.notifyItemRangeRemoved(startPosition, removalCount);
            }
        }
    }
}
