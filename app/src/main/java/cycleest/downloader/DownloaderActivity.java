package cycleest.downloader;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class DownloaderActivity extends Activity {


    private final String downloaderFragmentTag = "DownloaderFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_downloader);

        Fragment gui = getFragmentManager().findFragmentByTag(downloaderFragmentTag);

        //Log.d("gui existance", gui == null ? "null" : "not null");
        if (gui == null) {
            gui = new DownloaderFragment();

        }
        /*else{
            gui = getFragmentManager().getFragment(savedInstanceState, "DownloaderFragment");
        }*/
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.activity_downloader, gui, downloaderFragmentTag);
        fragmentTransaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //getFragmentManager().putFragment(outState, downloaderFragmentTag, getFragmentManager().findFragmentByTag(downloaderFragmentTag));
    }

}
