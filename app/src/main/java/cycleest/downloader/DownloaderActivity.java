package cycleest.downloader;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class DownloaderActivity extends Activity {


    private final String downloaderFragmentTag = "DownloaderFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);
        Fragment gui = getFragmentManager().findFragmentByTag(downloaderFragmentTag);
        if (gui == null) {
            gui = new DownloaderFragment();
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.activity_downloader, gui, downloaderFragmentTag);
        fragmentTransaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
