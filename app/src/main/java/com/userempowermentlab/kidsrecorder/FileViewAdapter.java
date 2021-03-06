package com.userempowermentlab.kidsrecorder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.userempowermentlab.aasrecorder.Data.DataManager;
import com.userempowermentlab.aasrecorder.Data.RecordItem;
import com.userempowermentlab.kidsrecorder.Listener.FileVIewMultiselectedListener;
import com.userempowermentlab.kidsrecorder.UI.PlaybackFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * FileExplorer Adapter class, manage between local file information and the UI class
 * Code reference from SoundRecorder: https://github.com/dkim0419/SoundRecorder
 */
public class FileViewAdapter extends RecyclerView.Adapter<FileViewAdapter.RecordingsViewHolder>  {
    Context mContext;
    FileVIewMultiselectedListener mlistener = null;
    RecordItem item;
    DataManager dataManager;
    boolean multiSelectionEnabled = false;
    ArrayList<RecordItem> selectedRecords = new ArrayList<RecordItem>(); //the list storaging the current multi-selected files

    public FileViewAdapter(Context context) {
        super();
        mContext = context;
        dataManager = DataManager.getInstance();
    }

    public void setFileViewMultiselectedListener(FileVIewMultiselectedListener listener){
        mlistener = listener;
    }

    /**
     * The class of the viewholder
     */
    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vLength;
        protected TextView vDateAdded;
        protected TextView vUploaded;
        protected View cardView;

        public RecordingsViewHolder(View v) {
            super(v);
            vName = v.findViewById(R.id.file_name_text);
            vLength = v.findViewById(R.id.file_length_text);
            vDateAdded = v.findViewById(R.id.file_date_added_text);
            vUploaded = v.findViewById(R.id.upload_label);
            cardView = v.findViewById(R.id.recordingcardview);
        }
    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.recordingcardview, parent, false);

        mContext = parent.getContext();

        return new RecordingsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, int position) {
        //get the corresponding item at the location
        item = dataManager.getItemAtPos(position);
        int itemDuration = item.duration;

        //display related information
        long minutes = TimeUnit.SECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.SECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);

        holder.vName.setText(item.filename);
        holder.vLength.setText(String.format("%02d:%02d", minutes, seconds));
        holder.vDateAdded.setText(item.createDate);
        if (!item.uploaded){
            holder.vUploaded.setVisibility(View.INVISIBLE);
        } else {
            holder.vUploaded.setVisibility(View.VISIBLE);
        }

        //check whether the file is multiselected
        if (multiSelectionEnabled && selectedRecords.contains(item)){
            holder.cardView.setBackgroundResource(R.color.selectGray);
        } else {
            holder.cardView.setBackgroundColor(Color.WHITE);
        }

        // define an on click listener to open PlaybackFragment
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (multiSelectionEnabled) {
                    if (selectItemAtPosition(holder.getLayoutPosition())){
                        holder.cardView.setBackgroundResource(R.color.selectGray);
                    } else {
                        holder.cardView.setBackgroundColor(Color.WHITE);
                    }
                } else {
                    try {
                        PlaybackFragment playbackFragment =
                                new PlaybackFragment().newInstance(dataManager.getItemAtPos(holder.getLayoutPosition()));

                        FragmentTransaction transaction = ((FragmentActivity) mContext)
                                .getSupportFragmentManager()
                                .beginTransaction();

                        playbackFragment.show(transaction, "dialog_playback");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //enable multiselect mode when long press
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (multiSelectionEnabled){
                    if (selectItemAtPosition(holder.getLayoutPosition())){
                        holder.cardView.setBackgroundResource(R.color.selectGray);
                    } else {
                        holder.cardView.setBackgroundColor(Color.WHITE);
                    }
                } else {
                    multiSelectionEnabled = true;
                    selectedRecords.add(dataManager.getItemAtPos(holder.getLayoutPosition()));
                    holder.cardView.setBackgroundResource(R.color.selectGray);
                    if (mlistener != null) {
                        mlistener.onMultiselectEnabled(true);
                    }
                }
                return true;
            }
        });
    }

    /**
     * In multi-select mode, select the item in certain position
     */
    private boolean selectItemAtPosition(int position) {
        if (selectedRecords.contains(dataManager.getItemAtPos(position))){
            selectedRecords.remove(dataManager.getItemAtPos(position));
            if (selectedRecords.size() == 0){
                multiSelectionEnabled = false;
                if (mlistener != null) {
                    mlistener.onMultiselectEnabled(false);
                }
            }
            return false;
        } else {
            selectedRecords.add(dataManager.getItemAtPos(position));
            return true;
        }
    }

    /**
     * Deselect all selections and quit multi-select mode
     */
    public void deSelectAll() {
        selectedRecords.clear();
        multiSelectionEnabled = false;
        if (mlistener != null) {
            mlistener.onMultiselectEnabled(false);
        }
        notifyDataSetChanged();
    }

    public boolean isMultiSelectionEnabled() {
        return multiSelectionEnabled;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataManager.getItemCount();
    }

    //UI & interaction stuff
    public void ShowDeleteFileDialog () {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //remove item from database, recyclerview, and storage
                            removeSelectedFiles();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.cancel();
                    }
                });
        confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

    /**
     * upload selected files
     */
    public void UploadSelectedFiles() {
        if (selectedRecords.size() > 0) {
            for (RecordItem item : selectedRecords){
                if (!item.uploaded)
                    dataManager.uploadFile(item.path);
            }
        }
        selectedRecords.clear();
        multiSelectionEnabled = false;
        notifyDataSetChanged();
    }

    /**
     * delete selected files
     */
    private void removeSelectedFiles() {
        //remove item from db, recyclerview and storage
        if (selectedRecords.size() > 0) {
            for (RecordItem item : selectedRecords){
                dataManager.deleteFile(item);
            }
            Toast.makeText(
                    mContext,
                    String.format(
                            mContext.getString(R.string.toast_file_delete)
                    ),
                    Toast.LENGTH_SHORT
            ).show();
        }
        selectedRecords.clear();
        multiSelectionEnabled = false;
        notifyDataSetChanged();
    }

    /**
     * Share selected files
     */
    public void ShowShareFileDialog() {
        if (selectedRecords.size() == 0) return;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
        intent.setType("audio/wav");

        ArrayList<Uri> files = new ArrayList<Uri>();

        for(RecordItem item : selectedRecords /* List of the files you want to send */) {
            File file = new File(item.path);
            Uri uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".fileprovider", file);

            files.add(uri);
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        mContext.startActivity(intent);
    }
}
