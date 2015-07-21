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

import java.io.File;
import java.util.List;

public class DownloaderFragment extends Fragment implements LoaderManager.LoaderCallbacks, View.OnClickListener {

    public final static int STATE_IDLE = 0;
    public final static int STATE_DOWNLOADING = 1;
    public final static int STATE_DOWNLOADED = 2;

    private final static int LOADER_ID = 0;

    private ProgressBar progressBar;
    private int currentState;
    private int currentProgress;
    private String imagePathInFilesystem;

    private BroadcastReceiver receiver;

    public static final String DOWNLOAD_PROGRESS_UPDATED = "cycleest.downloader.action.DOWNLOAD_PROGRESS_UPDATED";


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


        Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener((View.OnClickListener) this);
        //ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        TextView statusLabel = (TextView) view.findViewById(R.id.statusLabel);
        switch (currentState) {
            case STATE_IDLE:
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                break;
            case STATE_DOWNLOADING:
                statusLabel.setText(getResources().getString(R.string.downloading));
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(currentState);
                button.setEnabled(false);
                break;
            case STATE_DOWNLOADED:
                statusLabel.setText(getResources().getString(R.string.downloaded));
                ((ImageView) view.findViewById(R.id.imageView)).setImageDrawable(Drawable.createFromPath(imagePathInFilesystem));
                button.setText(getResources().getString(R.string.open));
                break;
        }
        Log.d("TAAG", "onCreateView");
        Log.d("TAAG", String.valueOf(currentState));
        if(getLoaderManager().getLoader(LOADER_ID) != null){
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onProgressUpdate(intent);
            }
        };
        currentState = STATE_IDLE;
        currentProgress = 0;
        File mydir = getActivity().getFilesDir();
        imagePathInFilesystem = new File(mydir, "testimage.jpg").getPath();
        Log.d("TAAG", "onCreate");
        Log.d("TAAG", String.valueOf(currentState));
    }

    private void onProgressUpdate(Intent intent) {
        currentProgress = intent.getIntExtra("cycleest.downloader.progress_amount", 0);
        progressBar.setProgress(currentProgress);
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        currentState = STATE_DOWNLOADING;
        ((TextView) getView().findViewById(R.id.statusLabel)).setText(getResources().getString(R.string.downloading));
        Log.d("TAAG", "onCreateLoader");
        Log.d("TAAG", String.valueOf(currentState));
        return new ImageLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        ((TextView) getView().findViewById(R.id.statusLabel)).setText(getResources().getString(R.string.downloaded));
        ((Button) getView().findViewById(R.id.button)).setText(getResources().getString(R.string.open));
        getView().findViewById(R.id.button).setEnabled(true);
        File mydir = getActivity().getFilesDir();
        File fileWithinMyDir = new File(mydir, "testimage.jpg");
        Log.d("filepath", fileWithinMyDir.getPath());
        ((ImageView) getView().findViewById(R.id.imageView)).setImageDrawable(Drawable.createFromPath(fileWithinMyDir.getPath()));

        currentState = STATE_DOWNLOADED;
        Log.d("TAAG", "onLoadFinished");
        Log.d("TAAG", String.valueOf(currentState));
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
        Log.d("TAAG", "onClick");
        Log.d("TAAG", String.valueOf(currentState));
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,
                new IntentFilter(DOWNLOAD_PROGRESS_UPDATED));
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onPause();
    }
}
