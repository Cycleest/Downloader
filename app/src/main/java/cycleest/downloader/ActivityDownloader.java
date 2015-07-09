package cycleest.downloader;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ActivityDownloader extends Activity implements LoaderManager.LoaderCallbacks{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getLoaderManager().getLoader(0) != null){
                    //open image
                }
                else{
                    getLoaderManager().initLoader(0, null, ActivityDownloader.this);
                }
            }
        });
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new Loader(this){
            public void start(){

            }
        };
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
