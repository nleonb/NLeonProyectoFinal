package com.example.nestorleonbrito.nleonpfc;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
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

    private JavaCameraView cameraView = null;
    private static boolean initOpenCV = false;

    private Button bt_hacerfoto;



    Mat mRgba, hsv_scale;

    //callback loader
    BaseLoaderCallback mCallBackLoader = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {


            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    cameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
    public static String TAG="VideoRecordingActivity";
    public static String TAG2="Video";

    static { initOpenCV = OpenCVLoader.initDebug(); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //connect the camera
        cameraView = (JavaCameraView)findViewById(R.id.cameraview);

        //set visibility
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setMaxFrameSize(1024,768);
        cameraView.clearFocus();

        //set callback function
        cameraView.setCvCameraViewListener(this);

        bt_hacerfoto = (Button) this.findViewById(R.id.button1);
        //Añadimos el Listener Boton
        bt_hacerfoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePicture();
            }});
    }

    private void savePicture() {


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()){
            Log.d(TAG, "Connected");

            //display when the activity resumed,, callback loader
            mCallBackLoader.onManagerConnected(LoaderCallbackInterface.SUCCESS);


        }else{
            Log.d(TAG, "Not connected");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mCallBackLoader);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Release the camera.
        if (cameraView != null) {
            cameraView.disableView();
            cameraView = null;
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        //4 channel
        mRgba = new  Mat(width, height, CvType.CV_8UC4);
        hsv_scale = new Mat(width, height, CvType.CV_8UC3);
    }

    @Override
    public void onCameraViewStopped() {
        //release
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        Mat src = inputFrame.gray(); // convertir a escala de grises
//        Mat cannyEdges = new Mat();  // objeto para almacenar el resultado
//
//        // aplicar el algoritmo canny para detectar los bordes
//        Imgproc.Canny(src, cannyEdges, 10, 100);
//
//        // devolver el objeto Mat procesado
//        return cannyEdges;

        //get each frame from camera

        mRgba = inputFrame.rgba();


        /**********HSV conversion**************/
        //convert mat rgb to mat hsv
        Imgproc.cvtColor(mRgba, hsv_scale, Imgproc.COLOR_RGB2HSV);

        //find scalar sum of hsv
        Scalar mColorHsv = Core.sumElems(hsv_scale);

        int pointCount = 320*240;


        //convert each pixel
        for (int i = 0; i < mColorHsv.val.length; i++) {
            mColorHsv.val[i] /= pointCount;
        }

        //convert hsv scalar to rgb scalar
        Scalar mColorRgb = convertScalarHsv2Rgba(mColorHsv);

        Core.inRange(mRgba,mColorHsv,mColorRgb,hsv_scale);

        Size ksize =new Size(15,15);

        Mat element; // = getStructuringElement(MORPH_RECT, Size(15, 15));
        element = getStructuringElement(MORPH_RECT,ksize);
        Imgproc.erode(mRgba,hsv_scale,element);
        Imgproc.dilate(mRgba,hsv_scale,element);



    /*Log.d("intensity", "Color: #" + String.format("%02X", (int)mColorHsv.val[0])
            + String.format("%02X", (int)mColorHsv.val[1])
            + String.format("%02X", (int)mColorHsv.val[2]) );*/
        //print scalar value
        Log.d("intensity", "R:"+ String.valueOf(mColorRgb.val[0])+" G:"+String.valueOf(mColorRgb.val[1])+" B:"+String.valueOf(mColorRgb.val[2]));


        /*Convert to YUV*/

        int R = (int) mColorRgb.val[0];
        int G = (int) mColorRgb.val[1];
        int B = (int) mColorRgb.val[2];

        int Y = (int) (R *  .299000 + G *  .587000 + B *  .114000);
        int U = (int) (R * -.168736 + G * -.331264 + B *  .500000 + 128);
        int V = (int) (R *  .500000 + G * -.418688 + B * -.081312 + 128);

        //int I = (R+G+B)/3;


        //Log.d("intensity", "I: "+String.valueOf(I));
        Log.d("intensity", "Y:"+ String.valueOf(Y)+" U:"+String.valueOf(U)+" V:"+String.valueOf(V));

        /*calibration*/



        return mRgba;
    }

    //convert Mat hsv to scalar
    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB);

        return new Scalar(pointMatRgba.get(0, 0));
    }


}
