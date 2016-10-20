package party.danyang.a500pxdownloader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity {

    public static final String ORIGIN_URL_PREFIX = "https://500px.com/photo/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);

        initDatas();
    }

    private void initDatas() {
        String originUrl = getIntent().getStringExtra(Intent.EXTRA_TEXT);

        if (TextUtils.isEmpty(originUrl) || !originUrl.startsWith(ORIGIN_URL_PREFIX)) {
            Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        getUrl(originUrl.replace(ORIGIN_URL_PREFIX, "").split("/")[0]);
    }

    private void getUrl(final String code) {
        Api.loadHtml(code)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        unsubscribe();
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        String msg;
                        if (e == null || TextUtils.isEmpty(e.getMessage())) {
                            msg = getString(R.string.unknown_error);
                        } else {
                            msg = e.getMessage();
                        }
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                        unsubscribe();
                        finish();
                    }

                    @Override
                    public void onNext(String html) {
                        if (TextUtils.isEmpty(html)) {
                            onError(new Exception(getString(R.string.get_html_null)));
                        }
                        String url = ContentParser.parser(html);
                        if (TextUtils.isEmpty(url)) {
                            onError(new Exception(getString(R.string.parser_html_null)));
                        }

                        if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                                .getBoolean(SettingsActivity.PREF_DOWNLOAD_FROM_MIRROR, false)) {
                            url = url.replace("https://drscdn.500px.org/photo/", "http://odn6f51j0.qnssl.com/");
                        }
                        final String path = PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                                .getString(SettingsActivity.PREF_PATH, "");
                        if (!TextUtils.equals(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath(), path)) {
                            if (RxPermissions.getInstance(MainActivity.this).isRevoked(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                onError(new Exception(getString(R.string.no_storage_permission)));
                            }
                        }
                        if (DownloadMangerResolver.resolve(MainActivity.this)) {
                            SaveImage.saveImg(MainActivity.this, code + ".jpg", path, url);
                        }
                    }
                });
    }
}
