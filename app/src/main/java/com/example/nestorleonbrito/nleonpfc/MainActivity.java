package com.example.nestorleonbrito.nleonpfc;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Vector;

import static org.opencv.imgproc.Imgproc.CV_MAX_SOBEL_KSIZE;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    static {
        if(!OpenCVLoader.initDebug()){
            Log.d("TAG", "OenCV no cargado");
        }
        else{
            Log.d("TAG", "OpenCV cargado");
        }
    }
    int iLowH = 45;
    int iHighH = 75;

    int iLowS = 20;
    int iHighS = 255;

    int iLowV = 10;
    int iHighV = 255;

    Mat imgHSV;
    Mat imgThresholded;

    Scalar sc1;
    Scalar sc2;

    private JavaCameraView cameraView;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        sc1 = new Scalar(iLowH,iLowS,iLowV);
        sc2 = new Scalar(iHighH,iHighS,iHighV);

        //connect the camera
        cameraView = (JavaCameraView)findViewById(R.id.cameraview);
        cameraView.getVisibility();
        cameraView.setCameraIndex(0);
        cameraView.setVisibility(CameraBridgeViewBase.VISIBLE);

        //set callback function
        cameraView.setCvCameraViewListener(this);

        cameraView.enableView();
    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        imgHSV = new Mat(width,height,CvType.CV_16UC4);
        imgThresholded = new Mat(width,height,CvType.CV_16UC4);

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
         Imgproc.cvtColor(inputFrame.rgba(), imgHSV, Imgproc.COLOR_BGR2HSV);
         Core.inRange(imgHSV,sc1,sc2,imgThresholded);
        Log.d("TAG", "Por aqui ando");

        return imgThresholded;
    }



}
