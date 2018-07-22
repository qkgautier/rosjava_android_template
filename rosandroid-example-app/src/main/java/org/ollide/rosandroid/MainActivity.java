/*
 * Copyright (C) 2014 Oliver Degener.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ollide.rosandroid;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

public class MainActivity extends RosActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int     VIEW_MODE_RGBA  = 0;
    public static final int     VIEW_MODE_GRAY  = 1;
    public static final int     VIEW_MODE_CANNY = 2;

    public static final int     IMAGE_TRANSPORT_COMPRESSION_NONE = 0;
    public static final int     IMAGE_TRANSPORT_COMPRESSION_PNG = 1;
    public static final int     IMAGE_TRANSPORT_COMPRESSION_JPEG = 2;

    public static int           viewMode        = VIEW_MODE_RGBA;
    public static int			  imageCompression = IMAGE_TRANSPORT_COMPRESSION_JPEG;
    public static int	imageCompressionQuality = 80;

    private SensorManager mSensorManager;

    private CameraPublisher nodeCamera;

    public MainActivity() {
        super("RosAndroidExample", "RosAndroidExample");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        if (nodeCamera != null)
        {
//            nodeCamera.openCamera();
            nodeCamera.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

        if (nodeCamera != null) {
//            nodeCamera.closeCamera();
            nodeCamera.pause();
//            nodeCamera.releaseCamera();
        }
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        Log.i(TAG, "init");
        NodeMain nodeSimple = new SimplePublisherNode();
        NodeConfiguration nodeConfigurationSimple = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfigurationSimple.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(nodeSimple, nodeConfigurationSimple);


        NodeMain nodeImu = new ImuPublisher(mSensorManager);
        NodeConfiguration nodeConfigurationImu = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfigurationImu.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(nodeImu, nodeConfigurationImu);

        nodeCamera = new CameraPublisher(this);
        NodeConfiguration nodeConfigurationCamera = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfigurationCamera.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(nodeCamera, nodeConfigurationCamera);
        nodeCamera.resume();
        Log.i(TAG, "end init");
    }
}
