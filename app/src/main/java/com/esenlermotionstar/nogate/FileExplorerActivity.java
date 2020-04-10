package com.esenlermotionstar.nogate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.esenlermotionstar.nogate.FileExplorer.FileExplorerRecyclerViewAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;



public class FileExplorerActivity extends AppCompatActivity {
    FileExplorerRecyclerViewAdapter fileExpAdapter;
    private TextView mTextMessage;
    RecyclerView recyclerView;
    private static final String ENV_SECONDARY_STORAGE = "SECONDARY_STORAGE";
    public static final String SD_CARD = "sdCard";
    public static final String EXTERNAL_SD_CARD = "externalSdCard";

    public static final String INTENT_RESULT_SELECTED_PATH = "INTENT_RESULT_SELECTED_PATH",
    INTENT_EXTRA_KEY_FILEFILTER = "filter";

    Map<String,String> meanings;

    public Map<String, File> getAllStorageLocations() {
        meanings =  new HashMap<>();
        Map<String, File> storageLocations = new HashMap<>(10);
        File sdCard = Environment.getExternalStorageDirectory();
        storageLocations.put(SD_CARD, sdCard);
        meanings.put(SD_CARD, "SD Kart");
        final String rawSecondaryStorage = System.getenv(ENV_SECONDARY_STORAGE);
        if (!TextUtils.isEmpty(rawSecondaryStorage)) {
            String[] externalCards = rawSecondaryStorage.split(":");
            for (int i = 0; i < externalCards.length; i++) {
                String path = externalCards[i];
                String externalStrgName = EXTERNAL_SD_CARD + String.format(i == 0 ? "" : "_%d", i);
                storageLocations.put(externalStrgName, new File(path));
                meanings.put(externalStrgName, "Depolama" + String.format(" (%d)", i));
            }
        }
        return storageLocations;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigate_back:
                    if (fileExpAdapter != null) fileExpAdapter.goUpperFolder();

                    return false;
                case R.id.navigation_storage_selector:

    fileExpAdapter.showStorages();
                    return false;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);

        recyclerView = findViewById(R.id.recyclerViewFileExplorer);
        Intent gelen = getIntent();


        String[] gelenExts = gelen.getStringArrayExtra(INTENT_EXTRA_KEY_FILEFILTER);
        if (gelenExts == null)
        {
            fileExpAdapter =
                    new FileExplorerRecyclerViewAdapter("/storage/emulated/0",
                            this);
        }
        else
            fileExpAdapter =
                    new FileExplorerRecyclerViewAdapter("/storage/emulated/0",
                            this,gelenExts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);



        fileExpAdapter.setOnClickListener(new FileExplorerRecyclerViewAdapter.ItemOnClickListener() {
            @Override
            public void onClick(File dosya) {
                if (dosya.isDirectory()) fileExpAdapter.changeFolder(dosya);
                else finishForResult(dosya.getAbsolutePath());
            }
        });

        recyclerView.setAdapter(fileExpAdapter);

        //mTextMessage = (TextView) findViewById(R.id.message);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void finishForResult(String absolutePath) {
        Intent retIntent = new Intent();
        retIntent.putExtra(INTENT_RESULT_SELECTED_PATH, absolutePath);


        this.setResult(RESULT_OK, retIntent);
        this.finish();
    }

}
