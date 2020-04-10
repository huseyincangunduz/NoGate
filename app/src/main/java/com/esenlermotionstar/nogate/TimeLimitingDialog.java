package com.esenlermotionstar.nogate;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import com.esenlermotionstar.nogate.DataManager.NoGateDb;
import com.esenlermotionstar.nogate.Helper.TimeLimitManager;
import org.w3c.dom.Text;

import java.util.zip.Inflater;


public class TimeLimitingDialog extends AlertDialog {
    public static void main(String[] args) {
        System.out.println("Hüseyin 496 can 170204051 gündüz".replaceAll("\\D", ""));
        //yazili = yazili;
    }


    public static final String PREFERENCE_DEFAULT_NAME = "NoGatePrefs";

    static boolean getTLEnabledSetting(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_DEFAULT_NAME, Context.MODE_PRIVATE);
        if (prefs.contains("enable_time_limit")) {
            return prefs.getBoolean("enable_time_limit", false);
        } else
            return false;
    }

    static void setTLEnabledSetting(Context context, Boolean newValue) {
        TimeLimitManager.EnableTimeLimit(context,newValue);
    }

    static int getTLMinutesSetting(Context context) {
        return TimeLimitManager.getTimeLimitMins(context);
    }

    static int setTLMinutesSetting(Context context, int newValue) {
return TimeLimitManager.setTimeLimitMins(context,newValue);

    }

    static void setCustomSettingsVisibility(boolean isChecked, View time_limit_adv_settings) {
        if (isChecked) {
            time_limit_adv_settings.setVisibility(View.VISIBLE);

        } else {

            time_limit_adv_settings.setVisibility(View.INVISIBLE);
        }

    }


    static void setMinutesHourInf(Context ctx, int dk, TextView textView) {
        int saat = dk / 60;
        String saat_ = (saat < 10) ? "0" + saat : String.valueOf(saat);
        int dakika = dk % 60;
        String dakika_ = (dakika < 10) ? "0" + dakika : String.valueOf(dakika);
        textView.setText(saat_ + ctx.getString(R.string.hour_s_and_x) + dakika_ + ctx.getString(R.string.x_minute_s));
    }

    public static AlertDialog createDialog(Context context) {
        TimeLimitingDialog.Builder builder = new TimeLimitingDialog.Builder(context);
        View v = LayoutInflater.from(context).inflate(R.layout.timelimit_control, null);
        LinearLayout time_limit_adv_settings = v.findViewById(R.id.time_limit_adv_settings);
        CheckBox allow_video_to_end_chckbox = v.findViewById(R.id.allow_video_to_end_chckbox);
        TextView clean_time_meaner_label = v.findViewById(R.id.clean_time_meaner_label);
        EditText time_txtbox = v.findViewById(R.id.time_txtbox);

        Boolean isEnabled = getTLEnabledSetting(context);


        int savedDakika = getTLMinutesSetting(context);
        time_txtbox.setText(String.valueOf(savedDakika));
        allow_video_to_end_chckbox.setChecked(TimeLimitManager.getAllowingEndVideo(context));
        allow_video_to_end_chckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TimeLimitManager.setAllowEndVideo(context,isChecked);
            }
        });
        setMinutesHourInf(context, savedDakika, clean_time_meaner_label);
        Switch switchContr = v.findViewById(R.id.enable_time_limit_switch);
        switchContr.setChecked(isEnabled);
        setCustomSettingsVisibility(isEnabled, time_limit_adv_settings);

        switchContr.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setTLEnabledSetting(context, isChecked);
            setCustomSettingsVisibility(isChecked, time_limit_adv_settings);
        });
        time_txtbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String yazili = s.toString();
                yazili = yazili.replaceAll("\\D", "");
                int dk;
                dk = tryParse(yazili, 60);
                dk = setTLMinutesSetting(context, dk);
                setMinutesHourInf(context,dk, clean_time_meaner_label);
            }
        });

        builder.setView(v);
        builder.setTitle(R.string.TimeLimit);
        builder.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                TimeLimitManager.updateTimeLimitToDb(context);
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                TimeLimitManager.updateTimeLimitToDb(context);
            }
        });
        builder.setPositiveButton(R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    private static int tryParse(String s, int defaultVal) {
        try {
            int newx = Integer.valueOf(s);
            return newx;
        } catch (Exception ex) {
            ex.printStackTrace();
            return defaultVal;
        }
    }

    protected TimeLimitingDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    static int mins = 10;
    public static AlertDialog.Builder showExtendTimeLimitDialog(Context ctx)
    {
        int dbTimeLimit = TimeLimitManager.getTodayTimeLimit();
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        View setting_interface = View.inflate(ctx,R.layout.extend_time_dialog_view_layout,null);

        RadioGroup group  = setting_interface.findViewById(R.id.rbgroup);
        EditText editText = setting_interface.findViewById(R.id.extend_min_costum_txtbox);

        RadioButton unlimited = setting_interface.findViewById(R.id.rb_costum);
        unlimited.setOnCheckedChangeListener((buttonView, isChecked) -> editText.setEnabled(isChecked));
        editText.setEnabled(unlimited.isChecked());
        return builder.setTitle(ctx.getString(R.string.ExtendingTime)).setView(setting_interface).setPositiveButton(R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (group.getCheckedRadioButtonId())
                {
                    case R.id.rb_five_mins:
                        mins = 5;
                        break;
                    case R.id.rb_ten_mins:
                        mins = 10;
                        break;
                    case R.id.rb_fifteen_mins:
                        mins = 15;
                        break;
                    case R.id.rb_thirty_mins:
                        mins = 30;
                        break;
                    case R.id.rb_unlimited:
                        mins = 1441;
                        break;
                    case R.id.rb_costum:
                        mins = tryParse(editText.getText().toString(),10);
                        break;
                }

                TimeLimitManager.setDatabaseTimeLimit(dbTimeLimit + mins,ctx);

                dialog.dismiss();
            }
        }).setNegativeButton(R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }
}
