package com.hamdam.hamdam.service.update;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.hamdam.hamdam.Constants.APK;
import static com.hamdam.hamdam.Constants.APP_NEEDS_UPDATE_KEY;
import static com.hamdam.hamdam.Constants.BROADCAST_APP_UPDATE_FAILURE;
import static com.hamdam.hamdam.Constants.BROADCAST_APP_UPDATE_SUCCESS;
import static com.hamdam.hamdam.Constants.INTENT_ACTION_CHECK_VERSION_CODE;
import static com.hamdam.hamdam.Constants.INTENT_ACTION_START_UPDATE;
import static com.hamdam.hamdam.Constants.UPDATE_NOTIFICATION_ID;

/**
 * Check for new updates to Hamdam.
 */

public class UpdateService extends IntentService {
    private static final String TAG = "UpdateService";

    public UpdateService() {
        super(UpdateService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(INTENT_ACTION_CHECK_VERSION_CODE)) {
            performVersionCheck();

        } else if (intent.getAction().equals(INTENT_ACTION_START_UPDATE)) {
            performAppUpdate();
        }

        CustomAmazonReceiver.completeWakefulIntent(intent);
    }

    private String getApkName() {
        try {
            ApplicationInfo appInfo = getPackageManager()
                    .getPackageInfo(getApplicationContext().getPackageName(), 0)
                    .applicationInfo;
            return appInfo.publicSourceDir;
        } catch (PackageManager.NameNotFoundException ex) {
            Log.e(TAG, ex.getMessage());
        }
        return APK;
    }

    void performAppUpdate() {
        final File path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
        final File file = new File(path, getApkName());
        final TransferObserver observer = AmazonPresenter
                .getDownloadObserver(getApplicationContext(),
                        APK, file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {

                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putBoolean(APP_NEEDS_UPDATE_KEY, false)
                            .apply();

                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    installIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            getApplicationContext(), 0, installIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationManager mNotifyManager
                            = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
                    mBuilder.setContentTitle(getString(com.hamdam.hamdam.R.string.download_success))
                            .setContentText(getString(com.hamdam.hamdam.R.string.install_update))
                            .setSmallIcon(com.hamdam.hamdam.R.drawable.baricon_hamdam_white)
                            .setContentIntent(pendingIntent)
                            .setOngoing(true)
                            .setAutoCancel(false);

                    mNotifyManager.notify(UPDATE_NOTIFICATION_ID, mBuilder.build());
                    sendBroadcast(new Intent(BROADCAST_APP_UPDATE_SUCCESS));
                } else if (state == TransferState.FAILED) {
                    Log.d(TAG, "State is " + state.toString());
                    sendBroadcast(new Intent(BROADCAST_APP_UPDATE_FAILURE));
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("UpdateAPKTask", ex.getMessage());
                file.delete();

            }
        });
    }

    private void performVersionCheck() {
        final File tempPath = getFilesDir();
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }
        final File file = new File(tempPath, "version.tmp");
        final TransferObserver observer = AmazonPresenter
                .getDownloadObserver(getApplicationContext(),
                        "versioncode.tmp", file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new FileReader(file));
                        String line = reader.readLine().trim();
                        int version = Integer.parseInt(line);
                        PackageInfo packageInfo = getPackageManager()
                                .getPackageInfo(getPackageName(), 0);
                        if (packageInfo != null
                                && version > packageInfo.versionCode) {
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                    .edit()
                                    .putBoolean(APP_NEEDS_UPDATE_KEY, true)
                                    .apply();
                            Log.i("UpdateTask", "Newer version of app detected");
                        }
                    } catch (IOException | PackageManager.NameNotFoundException e) {
                        onError(id, e);
                    } finally {
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (IOException ex) {
                            onError(id, ex);
                        }
                    }
                } else if (state == TransferState.FAILED) {
                    Log.d(TAG, "State is " + state.toString());
                }
                file.delete();
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("CheckAPKVersion", ex.getMessage());
                file.delete();
            }
        });

    }
}
