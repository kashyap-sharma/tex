/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.camera2video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.android.camera2video.customComps.Fontasm;
import com.example.android.camera2video.util.Static_Catelog;

public class CameraActivity extends Activity implements View.OnClickListener {


    private Fontasm videos;
    private Fontasm camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        try {
            if (!Static_Catelog.getStringProperty(this,"trues").equalsIgnoreCase("ok")) {
                if (null == savedInstanceState) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, Camera2VideoFragment.newInstance())
                            .commit();
                }
            } else {
                initView();
            }
        } catch (Exception e) {
            if (null == savedInstanceState) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, Camera2VideoFragment.newInstance())
                        .commit();
            }
        }

    }

    private void initView() {

        videos = (Fontasm) findViewById(R.id.videos);
        camera = (Fontasm) findViewById(R.id.camera);

        videos.setOnClickListener(this);
        camera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.videos:
                Intent intent =new Intent(this,DownloadActivity.class);
                startActivity(intent);

                break;
            case R.id.camera:

                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, Camera2VideoFragment.newInstance())
                            .commit();

                break;
        }
    }
}
