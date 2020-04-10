package com.esenlermotionstar.nogate;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.esenlermotionstar.nogate.ContentApproval.ApprovedContent;
import com.esenlermotionstar.nogate.ContentApproval.ApprovedContentManager;
import com.esenlermotionstar.nogate.ContentApproval.ApprovedContentsRecyclerAdapter;
import com.esenlermotionstar.nogate.ContentApproval.ApprovedContentsRecyclerAdapter.OnListChangeEventListenerClass;
import com.esenlermotionstar.nogate.DataManager.NoGateDb;
import com.esenlermotionstar.nogate.Helper.NGYoutubeVideoApproved;
import com.esenlermotionstar.nogate.Helper.RegexProcessing;
import com.esenlermotionstar.nogate.Helper.TimeLimitManager;
import com.esenlermotionstar.nogate.MainActivityHelpers.ApprovedContentItemHelper;
import com.esenlermotionstar.nogate.Services.ContentPlayerService;
import com.esenlermotionstar.nogate.SystemControllers.NetworkController;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class MainscreenBrowserWebClient extends WebViewClient {

    Boolean enableOverrideLinks = false;

    public void setEnableOverrideLinks(Boolean enableOverrideLinks) {
        this.enableOverrideLinks = enableOverrideLinks;
    }

    private final Context AppContext;

    public MainscreenBrowserWebClient(Context appContext) {
        AppContext = appContext;
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url_) {
        if (!enableOverrideLinks) return false;
        else {
            Uri url = Uri.parse(url_);
            Intent i = new Intent(Intent.ACTION_VIEW, url);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            AppContext.startActivity(i);
            return true;
        }

    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

        if (!enableOverrideLinks) {
            return false;
        } else {
            Uri url;
            if (Build.VERSION.SDK_INT < 21) {
                return false;
            } else {
                url = request.getUrl();
                Intent i = new Intent(Intent.ACTION_VIEW, url);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AppContext.startActivity(i);
                return true;
            }
        }
    }


}

public class MainActivity extends AppCompatActivity {

    //KONSTANTLAR
    private static final int NOTIFICATION_ID_LOCKSCRPLY_ENABLED = 0;
    private static final int PERMISSION_STORAGE_FOR_SELECT_VIDEO = 0;
    private static final int BROWSING_VIDEOS_IN_YT = 1;
    private static final String PREFERENCE_DEFAULT_NAME = "NoGatePrefs";

    //KONTROLLER
    FloatingActionButton fab;

    /*Kilit ekranında video gösterebilmek için BroadcastReceiver sınıfını extend
      eden LockScrReceiver sınıfını yaratıp register ediyoruz. Bu sadece tetikleyici,
      Activity değil*/

    ApprovedContentManager icerikYoneticisi;
    ApprovedContentsRecyclerAdapter approvedContentsRecyclerAdapter;
    RecyclerView recyclerView;

    WebView tarayici;
    MainscreenBrowserWebClient webClient;

    final int REQUEST_ADD_VIDEO_FILE = 666;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aboutmenu:
                Intent itd_about = new Intent(this, AboutActivity.class);
                startActivity(itd_about);
                return true;
            case R.id.settingsmenu:
                Intent itd = new Intent(this, SettingsActivity.class);
                startActivity(itd);
                return true;
            case R.id.menu_timelimit:
                AlertDialog timeLimitDialgBuild = TimeLimitingDialog.createDialog(this);
                timeLimitDialgBuild.show();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }


    void DisableLockScrPlayerService() {
        stopService(new Intent(this, ContentPlayerService.class));
    }


    void AddLocalVideoFileRequest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || controlStoragePermission()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this).setTitle(getString(R.string.sorage_permissions))
                    .setMessage(R.string.needed_storage_perms).setNeutralButton(R.string.cancel, null).setPositiveButton("Devam", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            askStoragePermission(PERMISSION_STORAGE_FOR_SELECT_VIDEO);
                        }
                    });

            dialogBuilder.show();
        } else {
            callAddVideoIntent();
        }
    }


    void callAddVideoIntent() {
        /*Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT , MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_ADD_VIDEO_FILE);*/
        Intent intent = new Intent(this, FileExplorerActivity.class);
        intent.putExtra(FileExplorerActivity.INTENT_EXTRA_KEY_FILEFILTER, new String[]{
                ".mp4"
        });
        startActivityForResult(intent, REQUEST_ADD_VIDEO_FILE);

    }


    private void showMessage(String baslik, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(baslik);
        builder.setMessage(text);
        builder.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }



    class WatchUpdate extends TimeLimitManager.OnChangeTimeLimits
    {

        @Override
        protected void run(int watched, int dbTodayTimeLimit, int defaultTimeLimit) {
           // if (MainActivity.this.hasWindowFocus())

        }
    }

    private void showAddYTDialog() {
        AlertDialog.Builder alertDialogBuild = new AlertDialog.Builder(this);
        alertDialogBuild.setTitle(getString(R.string.adding_yt_video));

        View v = View.inflate(this, R.layout.yt_add_vid_dialog, null);


        final EditText txt = v.findViewById(R.id.linkTextBox);
        alertDialogBuild.setView(v);

        alertDialogBuild.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String yturl = txt.getText().toString();
                addYoutubeVideo(yturl);
            }
        });
        alertDialogBuild.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogBuild.create();
        alertDialogBuild.show();
    }

    private void addYoutubeVideo(String url) {
        /*String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url); //url is youtube url for which you want to extract the id.
*/
        String YTVideoIDTest = RegexProcessing.getYouTubeVideoID(url);

        if (YTVideoIDTest != null) {
            String ytID = YTVideoIDTest;
            //Serverimizde onaylı içerik olup olmadığını kontrol etmemiz gerek

            if (NGYoutubeVideoApproved.isApprovedYTVideo(ytID)) {
                //https://www.youtube.com/watch?v=QyNghafdU2M
                approvedContentsRecyclerAdapter.addYTubeContent(ytID);
                Toast.makeText(this, R.string.youtube_video_added, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.that_cant_added, Toast.LENGTH_SHORT).show();
            }


        } else {
            showMessage(getString(R.string.error), getString(R.string.link_isnt_yt_link));
        }


    }

    //İzinleri kontrol etme/izin isteme
    void askStoragePermission(final int permissionRequestIntFinal) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, permissionRequestIntFinal);
    }

    boolean controlStoragePermission() {

        return (
                ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        );

    }

    boolean wasLoadedBlankSite = false;

    void loadBlankItemsSite() {
        if (!wasLoadedBlankSite) {
            tarayici.getSettings().setJavaScriptEnabled(true);
            String targetLanguage = Locale.getDefault().toString();
            String targetLink = "http://www.morphosium.com/nogate/documents/" + targetLanguage + "/startup.html";
            tarayici.loadUrl(targetLink);
            webClient = new MainscreenBrowserWebClient(getApplicationContext());
            tarayici.setWebViewClient(webClient);
            tarayici.setWebChromeClient(new WebChromeClient());

            webClient.setEnableOverrideLinks(true);
            tarayici.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    android.util.Log.d("WebView", consoleMessage.message());
                    return true;
                }
            });



            wasLoadedBlankSite = true;
        }
    }

    //Açılış fonksiyonu
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NoGateDb db = NoGateDb.getInstance(getApplicationContext());
        TimeLimitManager.addOnChangeTimeLimitEvent(new WatchUpdate());
        NoGateDb.getTodayWatchingTimeMinutes();
        //Log.i(null, "onCreate DİL: " + Locale.getDefault().toString() + " " + Locale.getDefault().getDisplayName());

        setContentView(R.layout.activity_main);
        tarayici = findViewById(R.id.information_webview);


        startService(new Intent(this, ContentPlayerService.class));
        fab = findViewById(R.id.fab);

        //Kaydetme/Okuma gibi işlemleri yapabilmek için içerikyöneticisi var
        icerikYoneticisi = ApprovedContentManager.getInstance(getApplicationContext());
        recyclerView = findViewById(R.id.recyclerView);





        //icerikYoneticisi.readApprovedContentsFromXML();

        if (ApprovedContentManager.getInitialized() && ApprovedContentManager.getFullList() != null) {
            approvedContentsRecyclerAdapter = new ApprovedContentsRecyclerAdapter(icerikYoneticisi,
                    new ApprovedContentsRecyclerAdapter.OnItemClickedEvent() {

                        @Override
                        public void OnClick(final int index, ApprovedContent item) {
                            Toast tost = Toast.makeText(MainActivity.this, getString(R.string.start_play_when_locked_screen) /*+ " " + (getString(R.string.itibaren).replace("{0}",item.getTitle()))*/, Toast.LENGTH_LONG);
                            tost.setGravity(Gravity.CENTER,0,0);
                            tost.show();
                            int x = (index - 1);
                            Log.i(null,"OnClick: " + x);
                            ApprovedContentManager.setSira(x); //SIRA 1 ARTIRILARAK ÇEKİLDİĞİ İÇİN 1 ÇIKARDIM
                        }
                    });


            approvedContentsRecyclerAdapter.setOnListChangeEventListener(new OnListChangeEventListenerClass() {

                public void callback(int size) {
                    if (size > 0) {
                        Log.i(null, "callback: size 0 dan büyük");
                        recyclerView.setVisibility(View.VISIBLE);
                        tarayici.setVisibility(View.INVISIBLE);
                    } else {
                        Log.i(null, "callback: size 0 a eşit");
                        tarayici.setVisibility(View.VISIBLE);
                        loadBlankItemsSite();
                        recyclerView.setVisibility(View.INVISIBLE);
                    }
                }
            });
            /*BAŞLA*/
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu menu = new PopupMenu(v.getContext(), fab);
                    menu.inflate(R.menu.insert_path_menu);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            if (menuItem.getItemId() == R.id.insert_video_file) {
                                //  Toast.makeText(MainActivity.this, "basma!", Toast.LENGTH_SHORT).show();
                                AddLocalVideoFileRequest();
                                return true;
                            }
                            if (menuItem.getItemId() == R.id.insert_youtube_video) {
                                //  Toast.makeText(MainActivity.this, "basma!", Toast.LENGTH_SHORT).show();
                                showAddYTDialog();
                                return true;
                            }
                            if (menuItem.getItemId() == R.id.browse_youtube_videos) {
                                //  Toast.makeText(MainActivity.this, "basma!", Toast.LENGTH_SHORT).show();
                                Intent itd = new Intent(getApplicationContext(), BrowseYouTube.class);
                                startActivityForResult(itd, BROWSING_VIDEOS_IN_YT);
                                return true;
                            }
                            return false;
                        }
                    });
                    menu.show();
                }
            });


            LinearLayoutManager layoutManager = new LinearLayoutManager(this);

            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(approvedContentsRecyclerAdapter);
            ItemTouchHelper ith = new ItemTouchHelper(ApprovedContentItemHelper.getItemTouchHelperForApprovedContent(getApplicationContext(),
                    new ApprovedContentItemHelper.TouchHelperRemoveEvent() {
                        @Override
                        public void RemoveItemByPosition(int i) {
                            ApprovedContentManager.removeItemByIndex(i);
                            approvedContentsRecyclerAdapter.removedItemNotification(i);
                        }
                    })
            );
            ith.attachToRecyclerView(recyclerView);

            firstOpening();
            this.setSupportActionBar((Toolbar) findViewById(R.id.mainacttoolbar));
            getSupportActionBar().setTitle(R.string.app_name);
            updateWatchedMinutesTxt();



        }

    }

    private void updateWatchedMinutesTxt() {

        updateWatchedMinutesTxt(TimeLimitManager.getTimeLimitingEnabled(this),TimeLimitManager.getTodayWatchedTimes(),TimeLimitManager.getTodayTimeLimit());


    }
    private void updateWatchedMinutesTxt(boolean timelimit_enabled, int nm,int max) {
        try
        {
            ActionBar supportToolbar = getSupportActionBar();
            if (supportToolbar != null)
            {
                String str = "";

                if (timelimit_enabled)  str = getString(R.string.watched_times) + ": "+ nm + "/"  + max;

                supportToolbar.setSubtitle(str);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    //İzin sonuçları
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_STORAGE_FOR_SELECT_VIDEO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.successful, Toast.LENGTH_SHORT).show();
                callAddVideoIntent();
            } else {
                Toast.makeText(this, R.string.permission_failed, Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //Açılan farklı activitylerin sonuçları
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_VIDEO_FILE && resultCode == RESULT_OK) {
            String selectedImagePath = data.getStringExtra(FileExplorerActivity.INTENT_RESULT_SELECTED_PATH);

            approvedContentsRecyclerAdapter.addFileContent(selectedImagePath);
        } else if (requestCode == BROWSING_VIDEOS_IN_YT) {
            approvedContentsRecyclerAdapter.notificationWithEvent();
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        icerikYoneticisi.saveApprovedContentsFromXML();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            updateWatchedMinutesTxt();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);


    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        DisableLockScrPlayerService();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        DisableLockScrPlayerService();
    }


    void firstOpening() {
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(PREFERENCE_DEFAULT_NAME, Context.MODE_PRIVATE);
        if (sharedPrefs.getBoolean("first_opening", true)) {
            Intent itd = new Intent(this, LicenseAgreement.class);
            startActivity(itd);

        }

    }
}
