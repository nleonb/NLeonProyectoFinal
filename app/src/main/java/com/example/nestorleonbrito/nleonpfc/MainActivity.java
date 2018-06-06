package com.example.nestorleonbrito.nleonpfc;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import static org.opencv.imgproc.Imgproc.CV_MAX_SOBEL_KSIZE;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "DaltonicApp::Activity";

    static {
        if(!OpenCVLoader.initDebug()){
            Log.d("NLeonApp", "OenCV no cargado");
        }
        else{
            Log.d("NLeonApp", "OpenCV cargado");
        }
    }

    private int alto;
    private int ancho;
    private String filtro;

    //Color Naranja
    //min
    int iLowH = 110;
    int iLowS = 50;
    int iLowV = 50;

    //max
    int iHighH = 130;
    int iHighS = 255;
    int iHighV = 255;

    Scalar sc1;
    Scalar sc2;

    private  Mat imgHSV;
    private Mat imgThresholded;

    //Cámara
    private JavaCameraView  camara;

    private Button btnSaveImg;

     private Uri file;



    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");

        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Intento de Fullscreen, OpenCV No quiere en galaxy nexus, sí en galaxy 4 :(
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //Screen ON Permanente

        //Brillo máximo permanente
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;


        setContentView(R.layout.activity_main);

        sc1 = new Scalar(iLowH,iLowS,iLowV);
        sc2 = new Scalar(iHighH,iHighS,iHighV);


        filtro = getIntent().getExtras().getString("filtro");


        //connect the camera
        camara = (JavaCameraView)findViewById(R.id.camara_java);
        camara.getVisibility();
        camara.setCameraIndex(0);
        camara.setVisibility(CameraBridgeViewBase.VISIBLE);

        //set callback function
        camara.setCvCameraViewListener(this);

        camara.enableView();

        btnSaveImg = (Button) findViewById(R.id.btnSaveImage);

        btnSaveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePicture();
            }
        });


    }

    private void savePicture() {

        Log.d("NLeonApp", "Entro en Save");
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(imgThresholded.cols(), imgThresholded.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgThresholded, bmp);
            Log.d(TAG, "Cambio a bitmat");
        } catch (CvException e) {
            Log.d(TAG, e.getMessage());
        }

        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        File directory = wrapper.getDir("Images",MODE_APPEND);

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imgTittle = "IMG_"+timestamp+".jpg";

        File file = new File(directory, imgTittle);

        try{
            OutputStream os = null;
            os = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG,100,os);
            os.flush();
            os.close();

        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }

        String savedImageURL = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                bmp,
                imgTittle,
                "NLeonAPP"
        );

        Uri savedImageURI = Uri.parse(savedImageURL);
        // Display saved image uri to TextView
        Log.d (TAG,"Image saved in internal storage.\n" + savedImageURI);

        Toast.makeText (getApplicationContext() , "Imagen Guardada", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camara != null)
            camara.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
        if (camara != null)
            camara.disableView();
    }


//    //Creamos el menu y las opciones
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        Log.i(TAG, "called onCreateOptionsMenu");
//        menuTipoCamara = menu.add("Cambiar Camara Nativa/Java");
//        menuBlancoYNegro = menu.add("Blanco y Negro");
//        menuModoReconocimiento = menu.add("Modo Preciso / Rango de Color");
//
//        SubMenu subMenu = menu.addSubMenu(4, 4, 4, "Selecciona una resolución");
//        subMenu.add(1, 10, 1, "Alta Resolución (1280x720)");
//        subMenu.add(1, 11, 2, "Media Resolución (960x720)");
//        subMenu.add(1, 12, 3, "Baja Resolución (800x480)");
//
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        String mensajeToast = new String();
//        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
//
//        //Boton cambia tipo de camara
//        if (item == menuTipoCamara) {
//            camara.setVisibility(SurfaceView.GONE);
//            camara = (JavaCameraView ) findViewById(R.id.camara_java);
//            mensajeToast = "Cámara Java";
//
//
//            camara.setVisibility(SurfaceView.VISIBLE);
//            camara.setCvCameraViewListener(this);
//            camara.enableView();
//            Toast toast = Toast.makeText(this, mensajeToast, Toast.LENGTH_LONG);
//            toast.show();
//        }
//        //Fin Tipo Camara
//
//
//        //Boton pone blanco y negro - Grises
//        if (item == menuBlancoYNegro) {
//            if (modoGrises) {
//                modoGrises = false;
//                Toast toast = Toast.makeText(this, "'Modo Grises' desactivado.\n'Modo Normal' habilitado.", Toast.LENGTH_LONG);
//                toast.show();
//            } else {
//                modoGrises = true;
//                Toast toast = Toast.makeText(this, "'Modo Normal' desactivado.\n'Modo Grises' habilitado.", Toast.LENGTH_LONG);
//                toast.show();
//            }
//        }
//        //Fin Modo Grises
//
//        //Boton Modo Preciso / Modo Tonalidades
//        if (item == menuModoReconocimiento) {
//            if (modoReconocimiento) {
//                modoReconocimiento = false;
//                Toast toast = Toast.makeText(this, "'Modo Preciso' desactivado.\n'Modo Tonalidades' habilitado.", Toast.LENGTH_LONG);
//                toast.show();
//            } else {
//                modoReconocimiento = true;
//                Toast toast = Toast.makeText(this, "'Modo Tonalidades' desactivado.\n'Modo Preciso' habilitado.", Toast.LENGTH_LONG);
//                toast.show();
//            }
//        }
//
//
//        //Submenu para cambiar el tamaño del HUD
//        switch (item.getItemId()) {
//            case 10: //Id del menú, para combrobar que se ha pulsado
//                ancho = 1280;
//                alto = 720;
//                Toast toast = Toast.makeText(this, "Resolución del HUD máxima", Toast.LENGTH_LONG);
//                toast.show();
//                break;
//            case 11:
//                ancho = 960;
//                alto = 720;
//                toast = Toast.makeText(this, "Resolución del HUD media", Toast.LENGTH_LONG);
//                toast.show();
//                break;
//            case 12:
//                ancho = 800;
//                alto = 480;
//                toast = Toast.makeText(this, "Resolución del HUD mínima", Toast.LENGTH_LONG);
//                toast.show();
//                break;
//
//        }
//
//        return true;
//    }


    public void onCameraViewStarted(int width, int height) {
        alto = height;
        ancho = height;
    }

    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {


        switch (filtro){
            case "original":

                imgThresholded = original(inputFrame);
                break;
            case "apuntarColor":

                imgThresholded = apuntarColor(inputFrame);
                break;
            case "bordesBw":

                imgThresholded = bordesBW(inputFrame);
                break;
            case "naranja":

                imgThresholded = naranja(inputFrame);
                break;
            case "grises":

                imgThresholded = grises(inputFrame);
                break;
        }

        if (imgThresholded == null)
            Log.d(TAG,"La imagen es nula");

        return imgThresholded;
    }

    private Mat original(CameraBridgeViewBase.CvCameraViewFrame frame) {
        return frame.rgba();
    }

    private Mat grises(CameraBridgeViewBase.CvCameraViewFrame frame) {
        return frame.gray();
    }

    private Mat naranja(CameraBridgeViewBase.CvCameraViewFrame frame) {
        imgHSV = new Mat(ancho,alto, CvType.CV_16UC4);
        imgThresholded = new Mat(ancho,alto,CvType.CV_16UC4);
        Imgproc.cvtColor(frame.rgba(), imgHSV, Imgproc.COLOR_BGR2HSV);
        Core.inRange(imgHSV,sc1,sc2,imgThresholded);

        Mat imgDilatedMat = new Mat();
        Imgproc.dilate ( imgThresholded, imgDilatedMat, new Mat() );

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        // find contours:
        Imgproc.findContours(imgThresholded, contours, hierarchy, Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Imgproc.drawContours(imgThresholded, contours, contourIdx, new Scalar(130, 255, 255), -1);
        }

        return imgThresholded;
    }

    private Mat bordesBW(CameraBridgeViewBase.CvCameraViewFrame frame) {
        imgThresholded = new Mat(ancho,alto,CvType.CV_16UC4);
        Imgproc.Canny(frame.gray(), imgThresholded,120,180);

        return imgThresholded;
    }

    private Mat apuntarColor(CameraBridgeViewBase.CvCameraViewFrame frame) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int width = metrics.widthPixels; // ancho absoluto en pixels
        int height = metrics.heightPixels; // alto absoluto en pixels

        Log.d(TAG, "Ancho = "+width+" Alto = "+height);
        Mat mat = frame.rgba();

        // PIXEL CENTRAL
        int alto = mat.height() / 2;	//camera.getHeight() / 2;
        int ancho = mat.width() / 2;	//camera.getWidth() / 2;

        //Recuperamos el color del pixel central
        double[] color = mat.get(alto, ancho);

        //Log en consola para ver si saca los colores
        //Log.i(TAG , "COLORES RGB -->"+ color[0] +";"+ color[1] +";"+ color[2] +"");

        //El color inverso, para pintar el crosshair y verlo siempre
        double[] colorInverso = { 255 - color[0], 255 - color[1], 255 - color[2], 255};

        //START CROSSHAIR

        //Lineas Horizontales
        int altoCamara = height;
        int anchoCamara = width;

        Imgproc.line(mat, new Point(0, altoCamara), new Point(anchoCamara - 25, altoCamara), new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1, 1, 1); //Izquierda
        Imgproc.line(mat, new Point(anchoCamara + 25, altoCamara), new Point(anchoCamara + anchoCamara, altoCamara), new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1, 1, 1); //Derecha

        //Lineas Verticales
        Imgproc.line(mat, new Point(anchoCamara, 0), new Point(anchoCamara, altoCamara - 25), new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1, 1, 1); //Top
        Imgproc.line(mat, new Point(anchoCamara, altoCamara + 25), new Point(anchoCamara, altoCamara + altoCamara), new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1, 1, 1); //Bottom

        //Circulo interno
        Imgproc.circle(mat, new Point(ancho, alto), 3, new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), -1);

        //Circulo externo
        Imgproc.circle(mat, new Point(ancho, alto), 50, new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1);
        //FIN CROSSHAIR


        //TEXTO
        //Texto generado en cada frame con el color en BGR (float)
        //Sí, BGR, OpenCV maneja los colores como Blue Green Red, no como Red Green Blue
        String texto = "RGB: " + color[0] + " " + color[1] + " " + color[2];
        //Core.putText(img, text, org, fontFace, fontScale, color);
        Imgproc.putText(mat, texto, new Point(10, 80), 3, 2, new Scalar(255, 255, 255, 255), 2);

        //Texto Color Nombre
        String nombreColor = getColorName(color[0], color[1], color[2]);
        Imgproc.putText(mat, nombreColor, new Point(ancho + 30, 80), 3, 2, new Scalar(255, 255, 255, 255), 2);

        //Rectángulo coloreado del color actual
        //Core.rectangle(img, pt1, pt2, color, thickness);
        // Si thickness < 0, hace un fill del rectángulo (Lo rellena)
        Imgproc.rectangle(mat, new Point( 10 , 110), new Point(anchoCamara - 10, 100), new Scalar(color[0], color[1], color[2], 255), -1); //Al pintar, usamos RGBA


        return mat;
    }


    public String getColorName(double r, double g, double b) {

        String nombreColor = null;
// Calculamos a partir del Hue, en vez del valor... Así tomamos rangos
        // http://en.wikipedia.org/wiki/Hue

        //Rojo
        if(r >= g && g >= b){
            nombreColor = "Tono Rojo";
        }

        //Amarillo
        if(g > r && r >= b){
            nombreColor = "Tono Amarillo";
        }

        //Verde
        if(g >= b && b > r){
            nombreColor = "Tono Verde";
        }

        //Cyan
        if(b > g && g > r){
            nombreColor = "Tono Cyan";
        }

        //Azul
        if(b > r && r >= g){
            nombreColor = "Tono Azul";
        }

        //Magenta
        if(r >= b && b > g){
            nombreColor = "Tono Magenta";
        }

        //Negro
        if(r < 10.0 && g < 10.0 && b < 10.0){
            nombreColor = "Tono Negro";
        }

        //Blanco
        if(r > 140.0 && g > 140.0 && b > 140.0){
            if(r > 200.0 && g > 200.0 && b > 200.0){
                nombreColor = "Blanco Puro";
            }else{
                nombreColor = "Tono Blanco";
            }
        }

        return nombreColor;
    }
}


