package cycleest.downloader;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class DownloaderFragment extends Fragment implements LoaderManager.LoaderCallbacks, View.OnClickListener {

    public final static int STATE_IDLE = 0;
    public final static int STATE_DOWNLOADING = 1;
    public final static int STATE_DOWNLOADED = 2;

    private final static int LOADER_ID = 0;

    private TextView statusLabel;
    private ProgressBar progressBar;
    private ImageView imagePreview;
    private Button button;

    private int currentState;
    private int currentProgress;
    private String imagePathInFilesystem;

    private BroadcastReceiver updateReceiver;
    private BroadcastReceiver notificationsReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onProgressUpdate(intent);
            }
        };
        notificationsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onNotificationReceive(intent);
            }
        };

        File myDir = getActivity().getFilesDir();
        imagePathInFilesystem = new File(myDir, "testimage.jpg").getPath();
        Log.d("TAAG", "onCreate");
        Log.d("TAAG", String.valueOf(currentState));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_downloader, container, false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        Log.d("TAAG", "onCreateView");
        Log.d("TAAG", String.valueOf(currentState));
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        statusLabel = (TextView) view.findViewById(R.id.statusLabel);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        imagePreview = (ImageView) view.findViewById(R.id.imageView);
        button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener((View.OnClickListener) this);

        //initState(currentState);

        Log.d("TAAG", "onCreateView");
        Log.d("TAAG", String.valueOf(currentState));

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LoaderManager loaderManager = getLoaderManager();
        Log.d("LoaderManager", String.valueOf(loaderManager.hashCode()));
        loaderManager.restartLoader(LOADER_ID, null, this);
        ImageLoader loader = (ImageLoader) loaderManager.getLoader(LOADER_ID);
        /*if (loader == null) {
            loaderManager.initLoader(LOADER_ID, null, this);
            loader = (ImageLoader) loaderManager.getLoader(LOADER_ID);
        }*/
        Log.d("loaderIsNull", loader == null ? "null" : "not null");
        if (loader != null) {

            int loaderStatus = loader.getLoaderStatus();
            Log.d("loaderStatus", String.valueOf(loaderStatus));
            switch (loaderStatus) {
                case ImageLoader.STATE_DOWNLOADING:
                    initState(STATE_DOWNLOADING);
                    currentProgress = loader.getCurrentProgress();
                    if (currentProgress == ImageLoader.UNKNOWN_FILE_SIZE) {
                        progressBar.setIndeterminate(true);
                    } else {
                        progressBar.setProgress(currentProgress);
                    }
                    break;
                case ImageLoader.STATE_DOWNLOADED:
                    initState(STATE_DOWNLOADED);
            }

        }
    }

    private void initState(int state) {
        currentState = state;
        switch (state) {
            case STATE_IDLE:
                initIdleState();
                break;
            case STATE_DOWNLOADING:
                initDownloadingState();
                break;
            case STATE_DOWNLOADED:
                initDownloadedState();
                break;
        }
    }

    private void initIdleState() {
        statusLabel.setText(getString(R.string.idle));
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        progressBar.setProgress(0);
        button.setEnabled(true);
        imagePreview.setImageDrawable(null);
    }

    private void initDownloadingState() {
        statusLabel.setText(getString(R.string.downloading));
        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressBar.setProgress(currentProgress);
        button.setEnabled(false);
        imagePreview.setImageDrawable(null);
    }

    private void initDownloadedState() {
        statusLabel.setText(getString(R.string.downloaded));
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(progressBar.getMax());
        button.setText(getString(R.string.open));
        button.setEnabled(true);
        imagePreview.setImageDrawable(Drawable.createFromPath(imagePathInFilesystem));
    }

    private void onProgressUpdate(Intent notification) {
        progressBar.setProgress(notification.getIntExtra("cycleest.downloader.progress_amount", 0));
    }

    private void onNotificationReceive(Intent notification) {
        switch (notification.getAction()) {
            case ImageLoader.UNABLE_TO_TRACK_PROGRESS:
                progressBar.setIndeterminate(true);
                currentProgress = ImageLoader.UNKNOWN_FILE_SIZE;
                break;
            case ImageLoader.FAILED_TO_DOWNLOAD:
                initState(STATE_IDLE);
                Toast.makeText(getActivity(), getString(R.string.failed_to_download), Toast.LENGTH_LONG).show();
                initIdleState();
        }
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.d("TAAG", "onCreateLoader");
        Log.d("TAAG", String.valueOf(currentState));
        //LoaderManager lm = getLoaderManager();
        Loader loader;// = lm.getLoader(LOADER_ID);
        //if(loader == null){
            loader =  new ImageLoader(getActivity(), progressBar.getMax());
        //}
        return loader;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        initState(STATE_DOWNLOADED);
        Log.d("TAAG", "onLoadFinished");
        Log.d("TAAG", String.valueOf(currentState));
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onClick(View v) {

        if (((ImageLoader) getLoaderManager().getLoader(LOADER_ID)).getLoaderStatus() == ImageLoader.STATE_DOWNLOADED) {
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
                //getLoaderManager().initLoader(LOADER_ID, null, this);
                getLoaderManager().getLoader(LOADER_ID).forceLoad();
            initState(STATE_DOWNLOADING);
        }
        Log.d("TAAG", "onClick");
        Log.d("TAAG", String.valueOf(currentState));
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter downloadProgress = new IntentFilter(ImageLoader.DOWNLOAD_PROGRESS_UPDATED);
        IntentFilter notification = new IntentFilter();
        notification.addAction(ImageLoader.UNABLE_TO_TRACK_PROGRESS);
        notification.addAction(ImageLoader.FAILED_TO_DOWNLOAD);
        localBroadcastManager.registerReceiver(updateReceiver, downloadProgress);
        localBroadcastManager.registerReceiver(notificationsReceiver, notification);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        localBroadcastManager.unregisterReceiver(updateReceiver);
        localBroadcastManager.unregisterReceiver(notificationsReceiver);
    }
}
