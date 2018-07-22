package org.ollide.rosandroid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.view.SurfaceView;

import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.videoio.Videoio;
import org.ros.internal.message.MessageBuffers;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.CameraBridgeViewBase;
import org.ros.node.topic.Publisher;

import java.io.ByteArrayOutputStream;

import sensor_msgs.CameraInfo;

public class CameraPublisher implements NodeMain, CameraBridgeViewBase.CvCameraViewListener2 {

    private Activity mainActivity;
    private ConnectedNode node = null;
	private Publisher<CameraInfo> cameraInfoPublisher;
    private Publisher<sensor_msgs.CompressedImage> imagePublisher;
	private sensor_msgs.CameraInfo cameraInfo;
    private Bitmap bmp;
    private ChannelBufferOutputStream stream;

    private static final String TAG = CameraPublisher.class.getSimpleName();

//    private CameraDevice cameraDevice;
//    private String cameraId;

    private CameraBridgeViewBase mOpenCvCameraView;


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
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    Log.w(TAG, "OpenCV loading failed.");
                    super.onManagerConnected(status);
                } break;
            }
        }
    };



//    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(CameraDevice camera) {
//            //This is called when the camera is open
//            Log.e(TAG, "onOpened");
//            cameraDevice = camera;
//        }
//
//        @Override
//        public void onDisconnected(CameraDevice camera) {
//            cameraDevice.close();
//        }
//
//        @Override
//        public void onError(CameraDevice camera, int error) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//    };


//    public void openCamera() {
//        CameraManager manager = (CameraManager) mainActivity.getSystemService(Context.CAMERA_SERVICE);
//        Log.e(TAG, "is camera open");
//        try {
//            cameraId = manager.getCameraIdList()[0];
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            assert map != null;
//
////            // Add permission for camera and let user grant the permission
////            if (mainActivity.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && mainActivity.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
////                mainActivity.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
////                return;
////            }
//            manager.openCamera(cameraId, stateCallback, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        Log.e(TAG, "openCamera X");
//    }
//
//
//    public void closeCamera() {
//        if (cameraDevice != null) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//    }



    public CameraPublisher(Activity mainActivity) {
        this.mainActivity = mainActivity;
        Log.i(TAG, "CameraPublisher");

        mOpenCvCameraView = mainActivity.findViewById(R.id.mainSurfaceView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(1920, 1080);
//        mOpenCvCameraView.SetCaptureFormat(Videoio.CAP_PROP_FORMAT);
//        mOpenCvCameraView.setMaxFrameSize(320, 180);

        stream = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        this.node = connectedNode;

        NameResolver resolver = node.getResolver().newChild("camera");

        cameraInfoPublisher = node.newPublisher(resolver.resolve("camera_info"), sensor_msgs.CameraInfo._TYPE);
        imagePublisher = node.newPublisher(resolver.resolve("image/compressed"), sensor_msgs.CompressedImage._TYPE);

        Log.i(TAG, "onStart");
    }

    public void pause()
    {
        Log.i(TAG, "pause");
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void resume()
    {
        Log.i(TAG, "resume");
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, mainActivity, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.enableView();
    }



    @Override
    public void onShutdown(Node node) {
        Log.i(TAG, "onShutdown");

    }

    @Override
    public void onShutdownComplete(Node node) {
        Log.i(TAG, "onShutdownComplete");

    }

    @Override
    public void onError(Node node, Throwable throwable) {
        Log.i(TAG, "onError");

    }

    public void onCameraViewStarted(int width, int height) {
        Log.i(TAG, "onCameraViewStarted");
    }

    public void onCameraViewStopped() {
        Log.i(TAG, "onCameraViewStopped");
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        Log.i(TAG, "onCameraFrame");

        Time currentTime = node.getCurrentTime();

        int numRows = inputFrame.rgba().rows();
        int numCols = inputFrame.rgba().cols();

		cameraInfo = cameraInfoPublisher.newMessage();
		cameraInfo.getHeader().setFrameId("camera");
		cameraInfo.getHeader().setStamp(currentTime);
		cameraInfo.setWidth(numCols);
		cameraInfo.setHeight(numRows);
		cameraInfoPublisher.publish(cameraInfo);

        if(bmp == null)
            bmp = Bitmap.createBitmap(numCols, numRows, Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(inputFrame.rgba(), bmp);

        sensor_msgs.CompressedImage image = imagePublisher.newMessage();
        if(MainActivity.imageCompression == MainActivity.IMAGE_TRANSPORT_COMPRESSION_PNG)
            image.setFormat("png");
        else if(MainActivity.imageCompression == MainActivity.IMAGE_TRANSPORT_COMPRESSION_JPEG)
            image.setFormat("jpeg");
        image.getHeader().setStamp(currentTime);
        image.getHeader().setFrameId("camera");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if(MainActivity.imageCompression == MainActivity.IMAGE_TRANSPORT_COMPRESSION_PNG)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        else if(MainActivity.imageCompression == MainActivity.IMAGE_TRANSPORT_COMPRESSION_JPEG)
            bmp.compress(Bitmap.CompressFormat.JPEG, MainActivity.imageCompressionQuality, baos);

        stream.buffer().writeBytes(baos.toByteArray());

        image.setData(stream.buffer().copy());

        stream.buffer().clear();
        imagePublisher.publish(image);


        return inputFrame.rgba();
    }

    public GraphName getDefaultNodeName()
    {
        return GraphName.of("android_sensors_driver/cameraPublisher");
    }
}
