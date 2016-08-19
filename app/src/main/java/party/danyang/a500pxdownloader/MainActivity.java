package party.danyang.a500pxdownloader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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
        getUrl(originUrl.replace(ORIGIN_URL_PREFIX, ""));
    }

    private void getUrl(final String code) {
        Observable.just(null)
                .compose(RxPermissions.getInstance(this).ensure(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .flatMap(new Func1<Boolean, Observable<String>>() {
                    @Override
                    public Observable<String> call(Boolean aBoolean) {
                        if (aBoolean) {
                            return Api.loadHtml(code);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.no_storage_permission, Toast.LENGTH_LONG).show();
                            finish();
                            return null;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        finish();
                        unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                        unsubscribe();
                    }

                    @Override
                    public void onNext(String s) {
                        if (TextUtils.isEmpty(s)) {
                            onError(new Exception(getString(R.string.get_html_null)));
                        }
                        String url = ContenParser.parser(s);
                        if (TextUtils.isEmpty(url)) {
                            onError(new Exception(getString(R.string.parser_html_null)));
                        }
                        Intent intent = new Intent(MainActivity.this, DownloadService.class);
                        intent.putExtra(DownloadService.INTENT_NAME, code);
                        intent.putExtra(DownloadService.INTENT_URL, url);
                        startService(intent);
                    }
                });
    }
}
