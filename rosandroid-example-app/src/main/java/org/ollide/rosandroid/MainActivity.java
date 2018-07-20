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

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

public class MainActivity extends RosActivity {

    private SensorManager mSensorManager;

    public MainActivity() {
        super("RosAndroidExample", "RosAndroidExample");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeMain nodeSimple = new SimplePublisherNode();
        NodeConfiguration nodeConfigurationSimple = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfigurationSimple.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(nodeSimple, nodeConfigurationSimple);


        NodeMain nodeImu = new ImuPublisher(mSensorManager);
        NodeConfiguration nodeConfigurationImu = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfigurationImu.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(nodeImu, nodeConfigurationImu);
    }
}
