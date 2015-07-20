package cycleest.downloader;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.File;
import java.util.List;

public class DownloaderFragment extends Fragment implements LoaderManager.LoaderCallbacks, View.OnClickListener{

    private final int LOADER_ID = 0;

    private int progress = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_downloader, container, false);
        ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);


        Button button = (Button) rootView.findViewById(R.id.button);
        button.setOnClickListener((View.OnClickListener) this);
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
        return new ImageLoader(getActivity());
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
