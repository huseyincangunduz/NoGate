package com.esenlermotionstar.nogate.Helper;

import android.os.AsyncTask;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class YoutubeTitleGetter extends AsyncTask<String,String,String> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... ytID) {
        try
        {
            Document doc = Jsoup.connect("https://www.youtube.com/embed/" + ytID[0]).get();
            String title = doc.title();
            doc = null;
            return title;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return "[" + ex.getMessage() + "]";

        }


    }


}
