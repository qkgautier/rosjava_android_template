package org.ollide.rosandroid;

import android.app.Activity;
import android.util.Log;

import org.opencv.core.Mat;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.CameraBridgeViewBase;

public class CameraPublisher implements NodeMain, CameraBridgeViewBase.CvCameraViewListener2 {

    private Activity mainActivity;
    private ConnectedNode node = null;
    private static final String TAG = CameraPublisher.class.getSimpleName();
//    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(mainActivity)
    {
        @Override
        public void onManagerConnected(int status)
        {

            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully.");
//                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    Log.w(TAG, "OpenCV loading failed.");
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CameraPublisher(Activity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        this.node = node;

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, mainActivity, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }



    @Override
    public void onShutdown(Node node) {

    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    public GraphName getDefaultNodeName()
    {
        return GraphName.of("android_sensors_driver/cameraPublisher");
    }
}
