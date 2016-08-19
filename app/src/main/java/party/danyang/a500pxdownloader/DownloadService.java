package party.danyang.a500pxdownloader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DownloadService extends Service {
    public static final String INTENT_URL = "party.danyang.url";
    public static final String INTENT_NAME = "party.danyang.name";

    private NotificationManager notificationManager;
    private Notification.Builder builder;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new Notification.Builder(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        if (intent.hasExtra(INTENT_URL)) {
            final String url = intent.getStringExtra(INTENT_URL);
            final String name = intent.getStringExtra(INTENT_NAME);
            save(DownloadService.this, url, name);
        }
        return START_NOT_STICKY;
    }

    private void save(final Context context, final String url, final String name) {
        Observable.just(url)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<String, Observable<Bitmap>>() {
                    @Override
                    public Observable<Bitmap> call(String s) {
                        return getBitmap(s);
                    }
                })
                .flatMap(new Func1<Bitmap, Observable<Uri>>() {
                    @Override
                    public Observable<Uri> call(Bitmap bitmap) {
                        return saveImg(name, bitmap);
                    }
                })
                .subscribe(new Subscriber<Uri>() {
                    @Override
                    public void onCompleted() {
                        stopSelf();
                        unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(DownloadService.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        stopSelf();
                        unsubscribe();
                    }

                    @Override
                    public void onNext(Uri uri) {
                        Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                        context.sendBroadcast(scannerIntent);

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "image/*");
                        PendingIntent pi = PendingIntent.getActivity(DownloadService.this, 1, intent, 0);

                        Toast.makeText(DownloadService.this,R.string.save_img_done,Toast.LENGTH_SHORT).show();
                        builder.setSmallIcon(R.drawable.ic_insert_photo_white_24dp)
                                .setWhen(System.currentTimeMillis())
                                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                .setContentTitle(getString(R.string.save_img_done))
                                .setContentIntent(pi)
                                .setStyle(new Notification.BigPictureStyle()
                                        .bigPicture(BitmapFactory.decodeFile(uri.getPath())))
                                .setAutoCancel(true);
                        notificationManager.notify(0, builder.build());
                    }
                });
    }

    private Observable<Uri> saveImg(final String name, final Bitmap bitmap) {
        return Observable
                .create(new Observable.OnSubscribe<Uri>() {
                    @Override
                    public void call(Subscriber<? super Uri> subscriber) {
                        File dir = new File(Environment.getExternalStorageDirectory(), "500px download");
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        File file = new File(dir, name + ".jpg");
                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();
                        } catch (FileNotFoundException e) {
                            subscriber.onError(e);
                        } catch (IOException e) {
                            subscriber.onError(e);
                        }
                        Uri uri = Uri.fromFile(file);
                        subscriber.onNext(uri);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<Bitmap> getBitmap(final String url) {
        return Observable
                .create(new Observable.OnSubscribe<Bitmap>() {
                    @Override
                    public void call(Subscriber<? super Bitmap> subscriber) {
                        Bitmap bitmap = null;
                        try {
                            bitmap = Picasso.with(DownloadService.this)
                                    .load(url)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .get();
                        } catch (IOException e) {
                            subscriber.onError(e);
                        }
                        if (bitmap == null) {
                            subscriber.onError(new Exception(getString(R.string.cannot_download_img)));
                        } else {
                            subscriber.onNext(bitmap);
                        }
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
