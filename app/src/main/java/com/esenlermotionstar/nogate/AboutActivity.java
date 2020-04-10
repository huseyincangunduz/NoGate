package com.esenlermotionstar.nogate;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;


public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void showSrcLicenses(View view) {
        Intent niyet = new Intent(this,opensrclibs.class);
        startActivity(niyet);
    }

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    public void opennogatesite(View view) {
        goToUrl("http://www.morphosium.com/nogate");
    }
}
