package com.esenlermotionstar.nogate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.esenlermotionstar.nogate.ContentApproval.ApprovedContent;
import com.esenlermotionstar.nogate.ContentApproval.ApprovedContentManager;
import com.esenlermotionstar.nogate.Helper.RegexProcessing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


abstract class BrowseYTActivityWebClientEvents
{
    abstract void onLoadStart(String url);
}


class BrowseYTActivityWebClient extends WebViewClient {
    private Context AppContext;
    BrowseYTActivityWebClientEvents events;

    public void setEvents(BrowseYTActivityWebClientEvents events) {
        this.events = events;
    }

    public BrowseYTActivityWebClientEvents getEvents() {
        return events;
    }

    public BrowseYTActivityWebClient(Context appContext, BrowseYTActivityWebClientEvents events_) {
        AppContext = appContext;
        setEvents(events_);
    }


        Boolean openInNewBrowserIfIsntYT(Uri urlAsUri) {
        if (isInYoutube(urlAsUri)) {
            return false;
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW, urlAsUri);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            AppContext.startActivity(i);
            return true;
        }
    }

    Boolean isInYoutube(Uri urlAsUri) {


        String pattern = "^http(s*):\\/\\/(m|(\\w*www\\w*))\\.youtube\\.com";//"(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
        return RegexProcessing.contains(pattern, urlAsUri.toString());

        /*String url = urlAsUri.toString();
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url); //url is youtube url for which you want to extract the id.
        if (matcher.find())
        {
            return false;
        }
        else
        {
            Intent i = new Intent(Intent.ACTION_VIEW, urlAsUri);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            AppContext.startActivity(i);
            return true;
        }*/
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return openInNewBrowserIfIsntYT(Uri.parse(url));
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return openInNewBrowserIfIsntYT(request.getUrl());
        } else return false;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (getEvents() != null) getEvents().onLoadStart(url);
        super.onPageFinished(view, url);
    }
}

public class BrowseYouTube extends AppCompatActivity {


    WebView tarayici;
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_you_tube);




        Toolbar tb = (Toolbar) findViewById(R.id.ytbrowser_activity_toolbar);
        this.setSupportActionBar(tb);
        tb.setNavigationIcon(R.drawable.ic_arrow_back);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geriGit();
            }
        });


        if (savedInstanceState != null) {

            tarayici.restoreState(savedInstanceState);

        }
        else
        {
            tarayici = findViewById(R.id.ytb_webview);
            tarayici.setWebViewClient(new BrowseYTActivityWebClient(getApplicationContext(), new BrowseYTActivityWebClientEvents() {
                @Override
                void onLoadStart(String url) {
                    changeAddIcon(R.drawable.ic_add);
                }
            }));
            tarayici.getSettings().setJavaScriptEnabled(true);

            tarayici.loadUrl("https://m.youtube.com");
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        (new MenuInflater(this)).inflate(R.menu.ytba_menu, menu);
        optionsMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ytbam_add: {
                addYtVideosAutomatic();
                return true;
            }
            case R.id.ytbam_refresh: {
                sayfayiYenile();
                return true;
            }
        }


        return super.onOptionsItemSelected(item);
    }

    private void addYtVideosAutomatic() {
        String url = tarayici.getUrl();
        String videoId = RegexProcessing.getYouTubeVideoID(url);
        if (videoId != null) {
            ApprovedContent content = new ApprovedContent();
            content.setType(ApprovedContent.TYPE_ATTR_YOUTUBE);
            content.setDataPath(videoId);
            ApprovedContentManager.insertApprovedContent(content);
            Toast.makeText(this, R.string.youtube_video_added, Toast.LENGTH_SHORT).show();
            changeAddIcon(R.drawable.ic_tick);
        }
        else{
            View infoView = View.inflate(this, R.layout.yt_add_in_browser_dialog, null);
            TextView txtView  = infoView.findViewById(R.id.ytba_addfromvideopage_txt);
            String txt = getText(R.string.ytba_add_from_video_page_info).toString();
            Spanned txtStyled;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                 txtStyled = Html.fromHtml(txt,Html.FROM_HTML_MODE_COMPACT);
            }
            else{
                 txtStyled = Html.fromHtml(txt);
            }
            txtView.setText(txtStyled);

            AlertDialog.Builder dialogBuild = new AlertDialog.Builder(this);
            dialogBuild.
                    setView(infoView)
            .setNeutralButton(R.string.ok, (dialog, which) -> dialog.cancel()).setTitle(R.string.addingvidbybrowseyt).show();
        }
    }


    @Override
    public boolean isFinishing() {

        return super.isFinishing();
    }

    private void sayfayiYenile() {
        if (tarayici != null) {
            tarayici.reload();
        }
    }

    private void geriGit() {
        if (tarayici != null && tarayici.canGoBack()) {
            tarayici.goBack();
        }
        else
            this.finish();
    }

    private void changeAddIcon(int drawable_id)
    {
        optionsMenu.findItem(R.id.ytbam_add).setIcon(drawable_id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        tarayici.saveState(outState);
    }
}
