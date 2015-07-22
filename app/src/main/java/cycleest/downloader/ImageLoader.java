package cycleest.downloader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class ImageLoader extends AsyncTaskLoader {

    public static final String DOWNLOAD_PROGRESS_UPDATED = "cycleest.downloader.action.DOWNLOAD_PROGRESS_UPDATED";
    public static final String UNABLE_TO_TRACK_PROGRESS = "cycleest.downloader.action.UNABLE_TO_TRACK_PROGRESS";
    public static final String FAILED_TO_DOWNLOAD = "cycleest.downloader.action.FAILED_TO_DOWNLOAD";
    public static final int KILOBYTE = 1024;
    public static final int UNKNOWN_FILE_SIZE = -1;
    public final static int STATE_IDLE = 0;
    public final static int STATE_DOWNLOADING = 1;
    public final static int STATE_DOWNLOADED = 2;

    private final int PROGRESS_CAPACITY;

    private int currentProgress;
    private int loaderStatus;

    public ImageLoader(Context context, int progressCapacity) {
        super(context);
        PROGRESS_CAPACITY = progressCapacity;
        currentProgress = 0;
        loaderStatus = STATE_IDLE;
    }

    public int getLoaderStatus() {
        Log.d("getLoaderStatus", String.valueOf(loaderStatus));
        return loaderStatus;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    @Override
    public Object loadInBackground() {
        try {
            URL url = new URL(super.getContext().getResources().getString(R.string.URL2));
            URLConnection connection = url.openConnection();
            connection.connect();

            byte dataBuffer[];
            int fileLength = getFileLengthAndBroadcastIfFailed(connection);
            dataBuffer = initializeDataBufferAccordingTo(fileLength);

            InputStream StreamFromURL = new BufferedInputStream(connection.getInputStream());
            OutputStream StreamToFileSystem = super.getContext().openFileOutput("testimage.jpg", Context.MODE_PRIVATE);

            if (isFileLengthRetrieveSucceed(fileLength)) {
                startDownloadWithProgressTrack(StreamFromURL, StreamToFileSystem, dataBuffer, fileLength);
            } else {
                startDownloadWithoutProgressTrack(StreamFromURL, StreamToFileSystem, dataBuffer);
            }
            StreamToFileSystem.flush();
            StreamToFileSystem.close();
            StreamFromURL.close();
            loaderStatus = STATE_DOWNLOADED;

        } catch (Exception e) {
            sendBroadcastNotification(FAILED_TO_DOWNLOAD);
            loaderStatus = STATE_IDLE;
            e.printStackTrace();
        }
        return null;
    }

    private byte[] initializeDataBufferAccordingTo(int fileLength) {
        byte[] dataBuffer;
        if (isFileLengthRetrieveSucceed(fileLength)) {
            dataBuffer = getBufferRelativeToFileLength(fileLength);
        } else {
            dataBuffer = getDefaultSizeBuffer();
        }
        return dataBuffer;
    }

    private byte[] getBufferRelativeToFileLength(int fileLength) {
        byte dataBuffer[];
        if (fileLength <= PROGRESS_CAPACITY) {
            dataBuffer = new byte[1];
        } else {
            dataBuffer = new byte[fileLength / PROGRESS_CAPACITY];
        }
        return dataBuffer;
    }

    private byte[] getDefaultSizeBuffer() {
        return new byte[KILOBYTE];
    }

    private int getFileLengthAndBroadcastIfFailed(URLConnection connection) {
        int fileLength = connection.getContentLength();
        if (fileLength == UNKNOWN_FILE_SIZE) {
            sendBroadcastNotification(UNABLE_TO_TRACK_PROGRESS);
        }
        return fileLength;
    }

    private boolean isFileLengthRetrieveSucceed(int fileLength) {
        return fileLength != UNKNOWN_FILE_SIZE;
    }

    private void startDownloadWithProgressTrack(InputStream input, OutputStream output, byte[] dataBuffer, int fileLength) throws IOException {
        long retrievedBytesCount = 0;
        int inBufferBytesCount;
        Intent progressNotifier = new Intent();
        progressNotifier.setAction(DOWNLOAD_PROGRESS_UPDATED);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
        loaderStatus = STATE_DOWNLOADING;
        while ((inBufferBytesCount = input.read(dataBuffer)) != -1) {
            retrievedBytesCount += inBufferBytesCount;
            currentProgress = (int) (retrievedBytesCount * PROGRESS_CAPACITY / fileLength);
            progressNotifier.putExtra("cycleest.downloader.progress_amount", currentProgress);
            broadcastManager.sendBroadcast(progressNotifier);
            output.write(dataBuffer, 0, inBufferBytesCount);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void startDownloadWithoutProgressTrack(InputStream input, OutputStream output, byte[] dataBuffer) throws IOException {
        int inBufferBytesCount;
        loaderStatus = STATE_DOWNLOADING;
        currentProgress = UNKNOWN_FILE_SIZE;
        while ((inBufferBytesCount = input.read(dataBuffer)) != -1) {
            output.write(dataBuffer, 0, inBufferBytesCount);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendBroadcastNotification(String notification) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
        Intent notifier = new Intent();
        notifier.setAction(notification);
        broadcastManager.sendBroadcast(notifier);
    }
}
