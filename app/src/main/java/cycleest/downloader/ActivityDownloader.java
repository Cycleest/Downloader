package cycleest.downloader;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class ActivityDownloader extends Activity implements LoaderManager.LoaderCallbacks{

    private final int loader = 0;
    private String dir = Environment.getExternalStorageDirectory().getPath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);
        findViewById(R.id.progressBar).setVisibility(ProgressBar.INVISIBLE);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getLoaderManager().getLoader(loader) != null) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + "/storage/emulated/0/testimage.jpg"), "image/*");
                    List<ResolveInfo> allHandlers = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    String name = allHandlers.get(0).activityInfo.name;
                    String pack = allHandlers.get(0).activityInfo.packageName;
                    intent.setClassName(pack, name);
                    startActivity(intent);
                } else {
                    findViewById(R.id.button).setEnabled(false);
                    getLoaderManager().initLoader(loader, null, ActivityDownloader.this);
                    getLoaderManager().getLoader(loader).forceLoad();
                    findViewById(R.id.progressBar).setVisibility(ProgressBar.VISIBLE);
                }
            }
        });
        ImageView img = (ImageView) findViewById(R.id.imageView);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader(this){
            @Override
            public Object loadInBackground() {
                try {
                    URL url = new URL(getResources().getString(R.string.URL2));
                    URLConnection connection = url.openConnection();
                    connection.connect();

                    int fileLength = connection.getContentLength();

                    String filepath = Environment.getExternalStorageDirectory().getPath();
                    Log.d("dir", Environment.getExternalStorageDirectory().getPath());

                    InputStream input = new BufferedInputStream(connection.getInputStream());

                    OutputStream output = new FileOutputStream(filepath + "/" + "testimage.jpg");

                    byte data[] = new byte[fileLength > 0 ? fileLength/100 : 1024];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        ((ProgressBar) findViewById(R.id.progressBar)).setProgress((int) (total * 100 / fileLength));
                        //Thread.sleep(10);
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                } catch (Exception e) {
                    Log.e("happens", "smth");
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        ((TextView) this.findViewById(R.id.statusLabel)).setText(getResources().getString(R.string.downloaded));
        ((Button) this.findViewById(R.id.button)).setText(getResources().getString(R.string.open));
        findViewById(R.id.button).setEnabled(true);
        ((ImageView)this.findViewById(R.id.imageView)).setImageDrawable(Drawable.createFromPath(Environment.getExternalStorageDirectory().getPath() + "/" + "testimage.jpg"));

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
