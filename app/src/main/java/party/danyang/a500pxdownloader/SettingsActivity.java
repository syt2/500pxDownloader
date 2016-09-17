package party.danyang.a500pxdownloader;


import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;

import rx.functions.Action1;

public class SettingsActivity extends PreferenceActivity {

    public static final String PREF_PATH = "key_path_under_21";
    public static final String PREF_DOWNLOAD_FROM_MIRROR = "key_download_from_mirror";

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //确认读写权限
        RxPermissions.getInstance(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (!aBoolean) {//如果没有权限，或者被用户取消权限
                            findPreference(PREF_PATH).setEnabled(false);
                            Toast.makeText(SettingsActivity.this
                                    , R.string.download_in_cache, Toast.LENGTH_LONG).show();
                            prefs.edit().putString(PREF_PATH, getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()).commit();
                        }
                    }
                });
        initPref();
    }

    private void initPref() {
        //初始化存储目录
        if (TextUtils.isEmpty(prefs.getString(PREF_PATH, null))) {
            prefs.edit().putString(PREF_PATH, getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()).commit();
        }
        setSummary(PREF_PATH, prefs.getString(PREF_PATH, ""));
        findPreference(PREF_PATH).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (o == null || TextUtils.isEmpty(o.toString())) {
                    setSummary(PREF_PATH, prefs.getString(PREF_PATH, ""));
                    return false;
                }
                File file = new File(o.toString());
                if (file.exists()) {
                    setSummary(PREF_PATH, o.toString());
                    return true;
                } else {
                    if (file.mkdir()) {
                        setSummary(PREF_PATH, o.toString());
                        return true;
                    } else {
                        setSummary(PREF_PATH, prefs.getString(PREF_PATH, ""));
                        Toast.makeText(SettingsActivity.this, R.string.load_dir_error, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            }
        });
    }

    private void setSummary(String key, String s) {
        findPreference(key).setSummary(s);
    }
}
