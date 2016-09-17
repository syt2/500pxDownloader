package party.danyang.a500pxdownloader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity {

    public static final String ORIGIN_URL_PREFIX = "https://500px.com/photo/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDatas();
    }

    private void initDatas() {
        String originUrl = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (!originUrl.startsWith(ORIGIN_URL_PREFIX)) {
            Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        getUrl(originUrl.replace(ORIGIN_URL_PREFIX, "").split("/")[0]);
    }

    private void getUrl(final String code) {
        Api.loadHtml(code)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response == null || TextUtils.isEmpty(response.body())) {
                            onFailure(call, new Exception(getString(R.string.get_html_null)));
                            return;
                        }
                        String url = ContenParser.parser(response.body());
                        if (TextUtils.isEmpty(url)) {
                            onFailure(call, new Exception(getString(R.string.parser_html_null)));
                            return;
                        }

                        final String path = PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                                .getString(SettingsActivity.PREF_PATH, "");
                        if (!TextUtils.equals(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath(), path)) {
                            if (RxPermissions.getInstance(MainActivity.this).isRevoked(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                Log.e("TAG", getString(R.string.no_storage_permission));
                                onFailure(call, new Exception(getString(R.string.no_storage_permission)));
                                return;
                            }
                        }
                        SaveImage.saveImg(MainActivity.this, code + ".jpg", path, url);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        String msg;
                        if (t == null || TextUtils.isEmpty(t.getMessage())) {
                            msg = getString(R.string.unknown_error);
                        } else {
                            msg = t.getMessage();
                        }
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
