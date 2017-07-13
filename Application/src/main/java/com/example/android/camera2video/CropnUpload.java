package com.example.android.camera2video;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.example.android.camera2video.customComps.Fontasm;
import com.example.android.camera2video.customComps.RangeSeekBar;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

public class CropnUpload extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private Fontasm camera;
    private Fontasm bell;
    private VideoView videoView;
    private RangeSeekBar rangeSeekBar;
    private TextView tvLeft;
    private TextView tvRight;
    private TextView tvInstructions;

    private Runnable r;
    private ProgressDialog progressDialog;
    private int duration;
    File uploadToS3;
    private FFmpeg ffmpeg;
    private String filePath;
    private static final String FILEPATH = "filepath";
    AmazonS3 s3Client;
    String bucket = "mybucketu";
    String mNextVideoAbsolutePatha ="/storage/emulated/0/Android/data/com.example.android.camera2video/files/remove.mp4";

    File uploadToS = new File(mNextVideoAbsolutePatha);

    TransferUtility transferUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        uploadToS3 = new File(getIntent().getExtras().getString("path"));
        initView();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    private void initView() {
        camera = (Fontasm) findViewById(R.id.camera);
        bell = (Fontasm) findViewById(R.id.bell);
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setMinimumHeight(videoView.getWidth());
        rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        tvLeft = (TextView) findViewById(R.id.tvLeft);
        tvRight = (TextView) findViewById(R.id.tvRight);
        tvInstructions = (TextView) findViewById(R.id.tvInstructions);

        camera.setOnClickListener(this);
        bell.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);
        rangeSeekBar.setEnabled(false);
        getVideo(uploadToS3);
        s3credentialsProvider();

        // callback method to call the setTransferUtility method
        setTransferUtility();
        loadFFMpegBinary();
    }

    private void getVideo(File uploadToS3) {

        videoView.setVideoURI( Uri.fromFile(uploadToS3));
        videoView.start();


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO Auto-generated method stub
                duration = mp.getDuration() / 1000;
                tvLeft.setText("00:00:00");

                tvRight.setText(getTime(mp.getDuration() / 1000));
                mp.setLooping(true);
                rangeSeekBar.setRangeValues(0, duration);
                rangeSeekBar.setSelectedMinValue(0);
                rangeSeekBar.setSelectedMaxValue(duration);
                rangeSeekBar.setEnabled(true);

                rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                        videoView.seekTo((int) minValue * 1000);

                        tvLeft.setText(getTime((int) bar.getSelectedMinValue()));

                        tvRight.setText(getTime((int) bar.getSelectedMaxValue()));

                    }
                });

                final Handler handler = new Handler();
                handler.postDelayed(r = new Runnable() {
                    @Override
                    public void run() {

                        if (videoView.getCurrentPosition() >= rangeSeekBar.getSelectedMaxValue().intValue() * 1000)
                            videoView.seekTo(rangeSeekBar.getSelectedMinValue().intValue() * 1000);
                        handler.postDelayed(r, 1000);
                    }
                }, 1000);

            }
        });
    }

    private String getTime(int seconds) {
        int hr = seconds / 3600;
        int rem = seconds % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera:
              Intent intent=new Intent(getApplicationContext(),CameraActivity.class);
                startActivity(intent);
                finish();
            case R.id.bell:
//                Log.e("data",":q  "+rangeSeekBar.getSelectedMaxValue()+":q  "+rangeSeekBar.getAbsoluteMaxValue());
//                Log.e("data",":q  "+rangeSeekBar.getSelectedMinValue()+":q  "+rangeSeekBar.getAbsoluteMinValue());
                if (rangeSeekBar.getSelectedMaxValue() == rangeSeekBar.getAbsoluteMaxValue()&&rangeSeekBar.getSelectedMinValue()==rangeSeekBar.getAbsoluteMinValue()){
                    TransferObserver transferObserver = transferUtility.upload(
                            bucket,     /* The bucket to upload to */
                            "MyApp"+ System.currentTimeMillis()+".mp4",    /* The key for the uploaded object */
                            new File(getIntent().getExtras().getString("path"))       /* The file where the data to upload exists */
                    );

                    transferObserverListener(transferObserver);
                }else{
                    executeCutVideoCommand(rangeSeekBar.getSelectedMinValue().intValue() * 1000, rangeSeekBar.getSelectedMaxValue().intValue() * 1000);

                }

                break;
        }
    }


    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                ffmpeg = FFmpeg.getInstance(this);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }

                @Override
                public void onSuccess() {
                    Log.d("Success", "ffmpeg : correct Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        } catch (Exception e) {
            Log.d("Failed", ": " + e);
        }
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(CropnUpload.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CropnUpload.this.finish();
                    }
                })
                .create()
                .show();

    }

    /**
     * Command for cutting video
     */
    private void executeCutVideoCommand(int startMs, int endMs) {
        File moviesDir = new File("/storage/emulated/0/Android/data/com.example.android.camera2video/files/");

        String filePrefix = "cut_video";
        String fileExtn = ".mp4";
        String yourRealPath =  getIntent().getExtras().getString("path");
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        Log.d("1", "startTrim: src: " + yourRealPath);
        Log.d("1", "startTrim: dest: " + dest.getAbsolutePath());
        Log.d("1", "startTrim: startMs: " + startMs);
        Log.d("1", "startTrim: endMs: " + endMs);
        filePath = dest.getAbsolutePath();
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        String[] complexCommand = {"-ss", "" + startMs / 1000, "-y", "-i", yourRealPath, "-t", "" + (endMs - startMs) / 1000, "-s", "640x640","-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};
        Log.e("filePath",filePath);
        execFFmpegBinary(complexCommand);

    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d("Failed", "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.d("SUCCESS", "SUCCESS with output : " + s);

                            getVideo(new File(filePath));
                    TransferObserver transferObserver = transferUtility.upload(
                            bucket,     /* The bucket to upload to */
                            "MyApp"+ System.currentTimeMillis()+".mp4",    /* The key for the uploaded object */
                            uploadToS       /* The file where the data to upload exists */
                    );

                    transferObserverListener(transferObserver);

                }

                @Override
                public void onProgress(String s) {
                    Log.d("TAGonProgress", "Started command : ffmpeg " + command);
                        progressDialog.setMessage("progress : " + s);
                    Log.d("onProgress", "progress : " + s);
                }

                @Override
                public void onStart() {
                    Log.d("onProgress", "Started command : ffmpeg " + command);
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d("onProgress", "Finished command : ffmpeg " + command);

                        progressDialog.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
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

        transferUtility = new TransferUtility(s3Client, getApplicationContext());
    }

    public void transferObserverListener(TransferObserver transferObserver){

        transferObserver.setTransferListener(new TransferListener(){

            @Override
            public void onStateChanged(int id, TransferState state) {
//                Toast.makeText(getApplicationContext(), "State Change"
//                        + state, Toast.LENGTH_SHORT).show();
                progressDialog.setMessage("State Change");
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent/bytesTotal * 100);
//                Toast.makeText(getApplicationContext(), "Progress in %"
//                        + percentage, Toast.LENGTH_SHORT).show();
                progressDialog.setMessage("Progress in "+percentage+"%");
                progressDialog.show();
                if(percentage==100){
                    try {
                        File file = new File(filePath);
                        boolean deleted = file.delete();
                        Log.e("okk",deleted+"");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        File file1 = new File(getIntent().getExtras().getString("path"));
                        boolean deleted1 = file1.delete();
                        Log.e("okk",deleted1+"");
                        if (deleted1) {
                            Intent intent=new Intent(getApplicationContext(),DownloadActivity.class);
                            startActivity(intent);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    clearApplicationData();
                    progressDialog.dismiss();

                }
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("error","error"+id);
            }


        });
    }

    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG",
                            "**************** File /data/data/APP_PACKAGE/" + s
                                    + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
