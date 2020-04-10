package com.esenlermotionstar.nogate.ContentApproval;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.esenlermotionstar.nogate.Helper.YoutubeTitleGetter;
import com.esenlermotionstar.nogate.R;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.zip.Inflater;

public class ApprovedContentsRecyclerAdapter extends RecyclerView.Adapter<ApprovedContentsRecyclerAdapter.ApprovedItemViewHolder> {

    public abstract static class OnListChangeEventListenerClass {
        public abstract void callback(int size);
    }

    OnListChangeEventListenerClass onListChangeEventListener;

    public OnListChangeEventListenerClass getOnListChangeEventListener() {
        return onListChangeEventListener;
    }

    public void setOnListChangeEventListener(OnListChangeEventListenerClass onListChangeEventListener) {
        this.onListChangeEventListener = onListChangeEventListener;
        removedItemNotification(getApprovedContents().size());
    }

    void runCallbackOnListCghangeEventList(int size) {
        if (onListChangeEventListener != null) onListChangeEventListener.callback(size);
    }

    //private final Context RecyclersContext;

    //Tıklanma olayı
    public abstract static class OnItemClickedEvent {
        public abstract void OnClick(final int index, final ApprovedContent item);
    }

   /* public abstract static class OnItemRemovedEvent {
        public abstract void onRemoveItem(ApprovedContent item, int index);
    }*/

    //OnItemRemovedEvent onRemoveEvent;
    OnItemClickedEvent onItemClickedEvent;

    public void setOnClickedListener(OnItemClickedEvent onClickedListener) {
        this.onClickedListener = onClickedListener;
    }

    public OnItemClickedEvent getOnClickedListener() {
        return onClickedListener;
    }
    /*public void setOnRemoveEvent(OnItemRemovedEvent onRemoveEvent) {
        this.onRemoveEvent = onRemoveEvent;
    }*/

    public void RemoveItem(int i) {
        if (ApprovedContents.size() > i) {
            ApprovedContents.remove(i);
            notifyItemRemoved(i);
            int sizeUpdate = ApprovedContents.size();
            runCallbackOnListCghangeEventList(sizeUpdate);

        }

    }

    public void removedItemNotification(int i) {
        notifyItemRemoved(i);
        int sizeUpdate = ApprovedContents.size();
        runCallbackOnListCghangeEventList(sizeUpdate);
    }

    public void notificationWithEvent() {
        int sizeUpdate = ApprovedContents.size();
        runCallbackOnListCghangeEventList(sizeUpdate);
        this.notifyDataSetChanged();

    }


    //Yer tutucu sınıfı
    public class ApprovedItemViewHolder extends RecyclerView.ViewHolder {
        public ApprovedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            AC_Item = itemView;

        }

        View AC_Item;

        public String getTitleText() {
            if (AC_Item != null) {
                TextView txtV = AC_Item.findViewById(R.id.itemtitleTxt);
                return txtV.getText().toString();
            }
            return null;
        }

        private void setTitleText(String titleText) {
            if (AC_Item != null) {
                TextView txtV = AC_Item.findViewById(R.id.itemtitleTxt);
                txtV.setText(titleText);
            }
        }

        private void setTitleText(int StringResource) {
            if (AC_Item != null) {
                TextView txtV = AC_Item.findViewById(R.id.itemtitleTxt);
                txtV.setText(StringResource);
            }
        }

        public void setItemTexts(ApprovedContent approvedContent) {
            if (approvedContent.isTypeThat(ApprovedContent.TYPE_ATTR_YOUTUBE)) {

                if (approvedContent.getTitle().isEmpty()) {
                    YoutubeTitleGetter titleGetter = new YoutubeTitleGetter() {
                        @Override
                        protected void onPostExecute(String s) {
                            super.onPostExecute(s);
                            approvedContent.setTitle(s);
                            setTitleText(s);
                        }

                    };
                    titleGetter.execute(approvedContent.getDataPath());

                    setTitleText(approvedContent.getDataPath());
                } else setTitleText(approvedContent.getTitle());


            } else if (approvedContent.isTypeThat(ApprovedContent.TYPE_ATTR_FILE)) {
                File fil = new File(approvedContent.getDataPath());
                if (fil.exists()) this.setTitleText(fil.getName());
                else this.setTitleText(R.string.file_is_not_found);
            }
            setTypeText(approvedContent);
        }

        private void setTypeText(ApprovedContent approvedContent) {
            TextView txtView = AC_Item.findViewById(R.id.typeText);
            //txtView.setText(String.format("%s türü", ));
            if (approvedContent.getType().equals(ApprovedContent.TYPE_ATTR_YOUTUBE)) {
                txtView.setText(R.string.yt_video);
            } else {
                txtView.setText(R.string.offline_video);
            }
        }


        public void setViewOnClickListener(View.OnClickListener onClickListener) {
            itemView.setOnClickListener(onClickListener);
        }
    }

    //üstteki sınıfların bir örneği olmalı
    OnItemClickedEvent onClickedListener;
    ArrayList<ApprovedContent> ApprovedContents;
    private ApprovedContentManager approvedContentManager;
    //inflater'ı bir kenarda tutmak


    public ArrayList<ApprovedContent> getApprovedContents() {
        return ApprovedContents;
    }

    public ApprovedContentManager getApprovedContentManager() {
        return approvedContentManager;
    }

    public ApprovedContentsRecyclerAdapter(ApprovedContentManager contentManager, OnItemClickedEvent onClickedListener_) {

        setOnClickedListener(onClickedListener_);
        approvedContentManager = contentManager;
        ApprovedContents = contentManager.getApprovedContents();
    }


    @NonNull
    @Override
    public ApprovedItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {


        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.approved_content_item, viewGroup, false);

        ApprovedItemViewHolder holder = new ApprovedItemViewHolder(v);


        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ApprovedItemViewHolder viewHolder, int i) {

        final ApprovedContent suanki = ApprovedContents.get(i);


        viewHolder.setItemTexts(suanki);
        viewHolder.setViewOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getOnClickedListener().OnClick(i, ApprovedContents.get(i));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return ApprovedContents.size();
    }

    public void addFileContent(String selectedImagePath) {
        ApprovedContent content = new ApprovedContent();
        content.setDataPath(selectedImagePath);
        content.setType(ApprovedContentManager.TYPE_ATTR_FILE);
        ApprovedContentManager.insertApprovedContent(content);
        int sizeUpdate = ApprovedContents.size();
        notifyItemInserted(sizeUpdate - 1);
        runCallbackOnListCghangeEventList(sizeUpdate);
        //notifyDataSetChanged();
    }


    public void addYTubeContent(final String videoID) {
        final ApprovedContent content = new ApprovedContent();
        content.setDataPath(videoID);
        content.setType(ApprovedContentManager.TYPE_ATTR_YOUTUBE);
        ApprovedContentManager.insertApprovedContent(content);
        int sizeUpdate = ApprovedContents.size();
        notifyItemInserted(sizeUpdate - 1);
        runCallbackOnListCghangeEventList(sizeUpdate);


        //notifyDataSetChanged();
    }


}

