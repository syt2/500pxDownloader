package party.danyang.a500pxdownloader;

import android.app.Application;

import party.danyang.a500pxdownloader.CrashCatcher.CrashCatcher;

/**
 * Created by dream on 16-9-16.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashCatcher.ready().toCatch(this);
    }
}
