package cycleest.downloader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class ImageLoader extends AsyncTaskLoader {

    public static final String DOWNLOAD_PROGRESS_UPDATED = "cycleest.downloader.action.DOWNLOAD_PROGRESS_UPDATED";

    public ImageLoader(Context context) {
        super(context);
    }

    @Override
    public Object loadInBackground() {
        try {
            URL url = new URL(super.getContext().getResources().getString(R.string.URL2));
            URLConnection connection = url.openConnection();
            connection.connect();
            int fileLength = connection.getContentLength();

            String filepath = super.getContext().getFilesDir().getPath();
            Log.d("dir", Environment.getDataDirectory().getPath());

            InputStream input = new BufferedInputStream(connection.getInputStream());
            
            OutputStream output = super.getContext().openFileOutput("testimage.jpg", Context.MODE_PRIVATE);

            byte data[] = new byte[fileLength > 0 ? fileLength / 100 : 1024];
            long total = 0;
            int count;
            Intent progressNotifier = new Intent();
            progressNotifier.setAction(DOWNLOAD_PROGRESS_UPDATED);

            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
            while ((count = input.read(data)) != -1) {
                total += count;
                progressNotifier.putExtra("cycleest.downloader.progress_amount", (int) (total * 100 / fileLength));
                broadcastManager.sendBroadcast(progressNotifier);
                Thread.sleep(100);
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
}
