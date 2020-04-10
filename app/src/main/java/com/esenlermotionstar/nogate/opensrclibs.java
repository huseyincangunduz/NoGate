package com.esenlermotionstar.nogate;


import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class opensrclibs extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opensrclibs);
        EditText txt = (EditText)(findViewById(R.id.textEditLisence));
        txt.setKeyListener(null);
        try {
            InputStream giris = getAssets().open("licenses.txt");
            BufferedInputStream giris_buffered = new BufferedInputStream(giris);
            InputStreamReader reader = new InputStreamReader(giris_buffered, StandardCharsets.UTF_8);

            StringBuilder stringBuilder = new StringBuilder();
            int byt;
            do
            {
                byt = reader.read();
                if (byt>-1)  stringBuilder.append((char) byt);
            } while (byt > -1);
            txt.setText(stringBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
