package com.example.android.camera2video;

/**
 * Created by kashy on 07/12/17.
 */

public class Constants {

    /*
     * You should replace these values with your own. See the README for details
     * on what to fill in.
     */
    public static final String COGNITO_POOL_ID = "ap-south-1:da1756b6-6c6a-4089-99c3-ec29a64bbe8c";

    /*
     * Region of your Cognito identity pool ID.
     */
    public static final String COGNITO_POOL_REGION = "CHANGE_ME";

    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static final String BUCKET_NAME = "mybucketu";

    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "CHANGE_ME";
}