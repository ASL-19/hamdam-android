package com.hamdam.hamdam.service.update;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.hamdam.hamdam.Constants;

import java.io.File;

/**
 * AWS Services
 */

public final class AmazonPresenter {

    private static final String ANDROID_APP = "Android App";
    private static AmazonS3Client s3Client;
    private static CognitoCachingCredentialsProvider credentialsProvider;
    private static TransferUtility transferUtility;

    // Don't instantiate this class
    private AmazonPresenter() {
    }

    /*
         * @param appContext: ApplicationContext
         */
    private static CognitoCachingCredentialsProvider getCredentialProvider(Context appContext) {
        if (credentialsProvider == null) {
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    appContext.getApplicationContext(),
                    Constants.COGNITO_POOL_ID,
                    Regions.US_EAST_1);
        }

        return credentialsProvider;
    }

    /*
     * Instance of S3 Client
     * @param appContext: ApplicationContext
     */
    public static AmazonS3Client getS3Client(Context appContext) {
        if (s3Client == null) {
            s3Client = new AmazonS3Client(getCredentialProvider(appContext));
            s3Client.setRegion(Region.getRegion(Regions.US_EAST_1));
        }

        return s3Client;
    }

    /*
     * Upload and download files from S3.
     * @param appContext: ApplicationContext
     */
    public static TransferUtility getTransferUtility(Context appContext) {
        if (transferUtility == null) {
            transferUtility = new TransferUtility(getS3Client(appContext),
                    appContext.getApplicationContext());
        }

        return transferUtility;
    }

    public static String getCognitoId(Context context) {
        CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = getCredentialProvider(context);
        return cognitoCachingCredentialsProvider.getIdentityId();
    }

    public static TransferObserver getDownloadObserver(Context appContext, String whichKey, File targetFile) {
        return getTransferUtility(appContext).download(
                Constants.BUCKET,     /* The bucket to download from */
                whichKey,    /* The key for the object to download */
                targetFile        /* The file to download the object to */
        );
    }

}
