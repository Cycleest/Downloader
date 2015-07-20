package cycleest.downloader;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class DownloaderFragment extends Fragment implements LoaderManager.LoaderCallbacks, View.OnClickListener{

    private final int LOADER_ID = 0;

    private int progress = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //Fragment f;
        //View rootView = null;
        //if (getFragmentManager() != null) {
            /*if ((f = getFragmentManager().findFragmentByTag("DownloaderFragment")) != null) {
                return f.getView();
            }*/
        //        rootView = f.getView();
        //        return rootView;
        //    }
        //}

        View rootView = inflater.inflate(R.layout.fragment_downloader, container, false);
        ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);


        Button button = (Button) rootView.findViewById(R.id.button);
        button.setOnClickListener((View.OnClickListener) getActivity());
        ImageView img = (ImageView) rootView.findViewById(R.id.imageView);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader(getActivity()) {

            @Override
            public Object loadInBackground() {
                try {
                    URL url = new URL(getResources().getString(R.string.URL2));
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    int fileLength = connection.getContentLength();
                    InputStream input = new BufferedInputStream(connection.getInputStream());
                    File mydir = getContext().getFilesDir();
                    File fileWithinMyDir = new File(mydir, "testimage.jpg");
                    Log.d("uri", fileWithinMyDir.getPath());
                    FileOutputStream output = new FileOutputStream(fileWithinMyDir);
                    byte data[] = new byte[fileLength > 0 ? fileLength / 100 : 1024];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        ((ProgressBar) getView().findViewById(R.id.progressBar)).setProgress((int) (total * 100 / fileLength));
                        //Thread.sleep(100);
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
        ((TextView) getView().findViewById(R.id.statusLabel)).setText(getResources().getString(R.string.downloaded));
        ((Button) getView().findViewById(R.id.button)).setText(getResources().getString(R.string.open));
        getView().findViewById(R.id.button).setEnabled(true);
        File mydir = getActivity().getFilesDir();
        File fileWithinMyDir = new File(mydir, "testimage.jpg");
        ((ImageView) getView().findViewById(R.id.imageView)).setImageDrawable(Drawable.createFromPath(fileWithinMyDir.getPath()));


    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onClick(View v) {
        if (getLoaderManager().getLoader(LOADER_ID) != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("image/*");
            List<ResolveInfo> allHandlers = getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            String name = allHandlers.get(0).activityInfo.name;
            String pack = allHandlers.get(0).activityInfo.packageName;
            intent.setClassName(pack, name);
            intent.setAction(Intent.ACTION_VIEW);
            File mydir = getActivity().getFilesDir();
            File fileWithinMyDir = new File(mydir, "testimage.jpg");
            Uri contentUri = FileProvider.getUriForFile(getActivity(), "cycleest.downloader", fileWithinMyDir);
            intent.setDataAndType(contentUri, "image/*");
            intent.setFlags(intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else {
            getView().findViewById(R.id.button).setEnabled(false);
            getLoaderManager().initLoader(LOADER_ID, null, this);
            getLoaderManager().getLoader(LOADER_ID).forceLoad();
            getView().findViewById(R.id.progressBar).setVisibility(ProgressBar.VISIBLE);
        }
    }
}
