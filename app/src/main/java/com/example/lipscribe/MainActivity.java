
package com.example.lipscribe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    boolean start=false;
    private Bundle savedInstanceState;
    JavaCameraView javaCameraView;
    private int inputsize=150;
    private Mat rgba, gray;
    private FaceDetection faceDetection;
    public void startButton(View Button)
    {
        if(start==false)
        {
            start=true;
        }
        else
            start=false;
    }
    public void stopButton(View Button)
    {
        if(start==true)
        {
            start=false;
        }
        else
            start=true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        int MY_PERMISSIONS_REQUEST_CAMERA = 0;
        // if camera permission is not given it will ask for it on device
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        setContentView(R.layout.activity_main);
        javaCameraView = (JavaCameraView) findViewById(R.id.JavaCamView);
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseCallback);
        } else {
            try {
                baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        javaCameraView.setCvCameraViewListener(this);
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        if(start==true) {
            rgba = new Mat();
            gray = new Mat();
        }

    }

    @Override
    public void onCameraViewStopped() {
        if(start==false) {
            rgba.release();
            gray.release();
        }

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        rgba = inputFrame.rgba();
        gray = inputFrame.gray();
        if(start==true) {
            Log.i("Debug","started on cam");
            faceDetection= new FaceDetection(getAssets(),this,"model68.tflite");
            Log.i("Debug","loaded constructor");
            rgba=faceDetection.mouthExtraction(rgba,gray);
            Log.i("Debug","output");
        }
        return rgba;

    }

    private BaseLoaderCallback baseCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) throws IOException {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    javaCameraView.enableView();
                }
                break;
                default:
                    super.onManagerConnected(status);
            }



        }

    };
}



