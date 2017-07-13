package com.example.android.camera2video;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    AmazonS3 s3Client;
    String bucket = "mybucketu";
    File uploadToS3 = new File("/storage/sdcard0/Pictures/Screenshots/Screenshot.png");
    File downloadFromS3 = new File("/storage/sdcard0/Pictures/Screenshot.png");
    TransferUtility transferUtility;
    List<String> listing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        s3credentialsProvider();

        // callback method to call the setTransferUtility method
        setTransferUtility();

    }



    public void s3credentialsProvider(){

        // Initialize the AWS Credential
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
               getApplicationContext(),
                "ap-south-1:da1756b6-6c6a-4089-99c3-ec29a64bbe8c", // Identity pool ID
                Regions.AP_SOUTH_1 // Region
        );

        createAmazonS3Client(credentialsProvider);
    }

    public void createAmazonS3Client(CognitoCachingCredentialsProvider
                                             credentialsProvider){

        // Create an S3 client
        s3Client = new AmazonS3Client(credentialsProvider);

        // Set the region of your S3 bucket
        s3Client.setRegion(Region.getRegion(Regions.AP_SOUTH_1));
    }

    public void setTransferUtility(){

        transferUtility = new TransferUtility(s3Client,getApplicationContext());
    }


    public void fetchFileFromS3(View view){

        // Get List of files from S3 Bucket
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    Looper.prepare();
                    listing = getObjectNamesForBucket(bucket, s3Client);

                    for (int i=0; i< listing.size(); i++){
                        Toast.makeText(MainActivity.this, listing.get(i),Toast.LENGTH_LONG).show();
                    }
                    Looper.loop();
                    // Log.e("tag", "listing "+ listing);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("tag", "Exception found while listing "+ e);
                }

            }
        });
        thread.start();
    }

    private List<String> getObjectNamesForBucket(String bucket, AmazonS3 s3Client) {
        ObjectListing objects=s3Client.listObjects(bucket);
        List<String> objectNames=new ArrayList<String>(objects.getObjectSummaries().size());
        Iterator<S3ObjectSummary> iterator=objects.getObjectSummaries().iterator();
        while (iterator.hasNext()) {
            objectNames.add(iterator.next().getKey());
        }
        while (objects.isTruncated()) {
            objects=s3Client.listNextBatchOfObjects(objects);
            iterator=objects.getObjectSummaries().iterator();
            while (iterator.hasNext()) {
                objectNames.add(iterator.next().getKey());
            }
        }
        return objectNames;
    }
}
