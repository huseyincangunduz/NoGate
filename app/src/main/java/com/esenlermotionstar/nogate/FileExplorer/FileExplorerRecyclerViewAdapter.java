package com.esenlermotionstar.nogate.FileExplorer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.esenlermotionstar.nogate.FileExplorerActivity;
import com.esenlermotionstar.nogate.Helper.StorageGettingFilter;
import com.esenlermotionstar.nogate.R;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Array;
import java.util.*;

import static android.media.ThumbnailUtils.createVideoThumbnail;

class ExplorerFileFilter implements FileFilter {

    public ExplorerFileFilter(Set<String> types) {
        this.types = types;
    }

    public ExplorerFileFilter(String... extensions) {
        Set<String> fileExtensions = new HashSet<String>();

        for (int i = 0; i < extensions.length; i++) {
            fileExtensions.add(extensions[i]);
        }
        this.types = fileExtensions;
    }

    Set<String> types;

    @Override
    public boolean accept(File pathname) {
        String fileName = pathname.getName();
        int lastDot = fileName.lastIndexOf('.');

        return pathname.isDirectory() ||
                (types == null || types.isEmpty() ||
                        (lastDot > -1 && types.contains(fileName.substring(lastDot))));


    }
}


class VideoImageGetter extends AsyncTask<File, Void, Bitmap> {
    FileExplorerRecyclerViewAdapter.FileItemViewHolder itemViewHolder;

    public VideoImageGetter(FileExplorerRecyclerViewAdapter.FileItemViewHolder fileItemViewHolder) {
        itemViewHolder = fileItemViewHolder;
    }

    @Override
    protected Bitmap doInBackground(File... files) {
        return createVideoThumbnail(files[0].getPath(),
                MediaStore.Images.Thumbnails.MINI_KIND);

    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        itemViewHolder.setThumbnailImage(bitmap);
    }
}

public class FileExplorerRecyclerViewAdapter extends RecyclerView.Adapter<FileExplorerRecyclerViewAdapter.FileItemViewHolder> {

    public static abstract class ItemOnClickListener {
        public abstract void onClick(File dosya);
    }

    ExplorerFileFilter filter;
    private Context ActContext;
    LayoutInflater inflater;
    ItemOnClickListener onClickListener;
    private ArrayList<File> storageLocations;

    public void setOnClickListener(ItemOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public class FileItemViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView thumbnail;

        public FileItemViewHolder(@NonNull View itemView_) {
            super(itemView_);
            itemView = itemView_;
            thumbnail = itemView.findViewById(R.id.imageView);
        }

        public void setFileItem(File fil) {

            setText(reflect(fil));

            if (fil.isFile()) {

                setTypeText(itemView.getContext().getString(R.string.File));

            } else if (fil.isDirectory()) {
                setTypeText(itemView.getContext().getString(R.string.folder));

            }
            setThumbnail(fil);
        }

        private String reflect(File fil) {
            if (specializedLabelsForFiles != null && specializedLabelsForFiles.containsKey(fil))
            {
                return specializedLabelsForFiles.get(fil);
            }

            return fil.getName();
        }

        public void setThumbnail(File fil) {


            if (fil.exists() && fil.isFile()) {
                thumbnail.setImageResource(R.drawable.file);
                if (fil.getName().endsWith(".mp4")) {

                    try {
                        thumbnail.setColorFilter(null);
                        VideoImageGetter videoImageGetter = new VideoImageGetter(this);
                        videoImageGetter.execute(fil);


                    } catch (Exception ex) {

                        ex.printStackTrace();

                    }

                }
            } else {
                thumbnail.setImageBitmap(null);
            }


        }

        public void setTypeText(String text) {
            TextView TypeTextView = itemView.findViewById(R.id.filetypelabel);
            TypeTextView.setText(text);
        }

        public void setText(String text) {
            TextView textView = itemView.findViewById(R.id.filenametitletxt);
            textView.setText(text);
        }


        public void setThumbnailImage(Bitmap bitmap) {
            thumbnail.setImageBitmap(bitmap);
        }
    }

    File[] files;
    File startFolderFile;

    public FileExplorerRecyclerViewAdapter(String startFolder, Context context_, String[] extensions) {
        startFolderFile = new File(startFolder);
        filter = new ExplorerFileFilter(extensions);
        files = startFolderFile.listFiles(filter);
        ActContext = context_;
        inflater = LayoutInflater.from(ActContext);

    }

    public FileExplorerRecyclerViewAdapter(String startFolder, Context context_) {
        startFolderFile = new File(startFolder);
        files = startFolderFile.listFiles(filter);
        ActContext = context_;
        inflater = LayoutInflater.from(ActContext);
        filter = new ExplorerFileFilter();
    }

    public void changeFolder(File newfolder) {
        startFolderFile = newfolder;
        files = newfolder.listFiles(filter);
        this.notifyDataSetChanged();
    }

    public void goUpperFolder() {
        File parentFolder = startFolderFile.getParentFile();
        if (parentFolder != null) changeFolder(parentFolder);
    }

    @NonNull
    @Override
    public FileItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_item, viewGroup, false);

        return new FileItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FileItemViewHolder fileItemViewHolder, int i) {

        final File curdosya = files[i];
        Log.i(null, "onBindViewHolder: " + curdosya.getAbsolutePath());
        fileItemViewHolder.setFileItem(curdosya);

        fileItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(curdosya);
            }
        });
    }


    @Override
    public int getItemCount() {
        if (files != null)
            return files.length;
        else return 0;
    }


    public static final String SD_CARD = "sdCard";
    public static final String EXTERNAL_SD_CARD = "externalSdCard";
    private static final String ENV_SECONDARY_STORAGE = "SECONDARY_STORAGE";
    protected Map<File, String> specializedLabelsForFiles;

    public void showStorages() {

        final List<File> allStorage = getAllStorageLocations();
        files = allStorage.toArray(new File[allStorage.size()]);
        this.notifyDataSetChanged();
    }


    void addToTwoBoth(ArrayList<File> files, Map<File,String> labels, String label, File fil)
    {
        files.add(fil);
        labels.put(fil,label);
    }

    void addToTwoBothK( String label, File fil)
    {
        addToTwoBoth(storageLocations,specializedLabelsForFiles,label,fil);
    }

    public ArrayList<File> getAllStorageLocations() {
        //meanings =  new HashMap<>();

        if (specializedLabelsForFiles == null)
        {
            specializedLabelsForFiles = new HashMap<>(10);
        }
        if (storageLocations == null)
        {



            storageLocations = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {

                File StorageFolder = new File("/storage");
                if (StorageFolder.exists() && StorageFolder.canRead() && StorageFolder.isDirectory())
                {
                    File internal = new File("/storage/emulated/0");
                    addToTwoBothK(ActContext.getString(R.string.internal_storage),internal);
                    File[] files = StorageFolder.listFiles(new StorageGettingFilter());
                    for (int i = 0; i < files.length; i++) {

                        File fil = files[i];
                        String externalLabel = ActContext.getString(R.string.external_storage) + " " + String.valueOf(i) + String.format(" (%s)",fil.getName());
                        addToTwoBothK(externalLabel,fil);
                    }

                }



            }
            else{ //4.4

                File sdCard = Environment.getExternalStorageDirectory();
                addToTwoBothK(SD_CARD, sdCard);
                //meanings.put(SD_CARD, "SD Kart");
                final String rawSecondaryStorage = System.getenv(ENV_SECONDARY_STORAGE);
                if (!TextUtils.isEmpty(rawSecondaryStorage)) {
                    String[] externalCards = rawSecondaryStorage.split(":");
                    for (int i = 0; i < externalCards.length; i++) {
                        String path = externalCards[i];
                        String externalStrgName = EXTERNAL_SD_CARD + String.format(i == 0 ? "" : "_%d", i);
                        addToTwoBothK(externalStrgName, new File(path));
                        //meanings.put(externalStrgName, "Depolama" + String.format(" (%d)", i));
                    }
                }

            }
        }

        return storageLocations;
    }


}
