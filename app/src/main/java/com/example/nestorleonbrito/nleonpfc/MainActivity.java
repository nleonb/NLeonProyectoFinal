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


    private static final String TAG = "NLeonApp";

    //Cargar librerías de OpenCV
    static {
        if(!OpenCVLoader.initDebug()){
            Log.d("NLeonApp", "OenCV no cargado");
        }
        else{
            Log.d("NLeonApp", "OpenCV cargado");
        }
    }

    //Alto y acncho de la camara con la que se van a crear los objetos Mat
    private int alto;
    private int ancho;

    // Tipo de boton que se ha pulsado en la actividad antrior
    private String filtro;

    //GAma de colores para detectar el color Naranja y/o rojo
    //min
    int iLowH = 110;
    int iLowS = 50;
    int iLowV = 50;

    //max
    int iHighH = 130;
    int iHighS = 255;
    int iHighV = 255;

    //Objetos de tipo Scalar para poder crear con los que se daran los rangos de colores RGB
    Scalar sc1;
    Scalar sc2;

    // objetos de tipo Mat que son los que finalmente se ven en la pantalla
    private  Mat imgHSV;
    private Mat imgThresholded;

    //Cámara de OpenCv
    private JavaCameraView  camara;

    // Boton de salvar la imagen
    private Button btnSaveImg;

    //Objeto Uri para almacenar la imagen
    private Uri file;


    /**
     * Metodo llamado cuando se lanza la actividad.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, " onCreate");

        super.onCreate(savedInstanceState);

        // Orientación. Se fija en landscape por que al girar elmovil la imgen de la camara no gira.
        // y solo giran el boton
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Fullscreen
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //Screen ON Permanente

        //Brillo máximo permanente
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;

        // se infla la activiad
        setContentView(R.layout.activity_main);

        // se crean los objetos Scalar que luego se utilizará
        sc1 = new Scalar(iLowH,iLowS,iLowV);
        sc2 = new Scalar(iHighH,iHighS,iHighV);

        //se obtiene el boton pulsado de la actividad anterior
        filtro = getIntent().getExtras().getString("filtro");


        //Se conecta la camara de OpenCV
        camara = (JavaCameraView)findViewById(R.id.camara_java);
        camara.getVisibility();
        camara.setCameraIndex(0);
        camara.setVisibility(CameraBridgeViewBase.VISIBLE);

        //callback function
        camara.setCvCameraViewListener(this);

        camara.enableView();

        //Inflado de boton save
        btnSaveImg = (Button) findViewById(R.id.btnSaveImage);

        // metodo de guardar imagen en internal storge
        btnSaveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePicture();
            }
        });


    }

    /*
    Metodo para guardar la imagen que se esta viendo en ese momento
     */
    private void savePicture() {

        Bitmap bmp = null;
        //Cambio de imagen Mat a bitmap
        try {
            bmp = Bitmap.createBitmap(imgThresholded.cols(), imgThresholded.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgThresholded, bmp);
            Log.d(TAG, "Cambio a bitmat");
        } catch (CvException e) {
            Log.d(TAG, e.getMessage());
        }

        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        //Directorio donde se almacenarán las imagenes
        File directory = wrapper.getDir("Images",MODE_APPEND);

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        // Nombre unico de la imagen con el timestamp
        String imgTittle = "IMG_"+timestamp+".jpg";

        File file = new File(directory, imgTittle);

        //Flujo se escritura de imagen
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
        // Mensaje de que la imagen ha sido guardada.
        Log.d (TAG,"Image saved in internal storage.\n" + savedImageURI);

        Toast.makeText (getApplicationContext() , "Imagen Guardada", Toast.LENGTH_SHORT).show();
    }

    //Si pasa por onPause elimino la cámara
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


    //Si pasa por onDestroy elimino la cámara
    public void onDestroy() {
        super.onDestroy();
        if (camara != null)
            camara.disableView();
    }

    // Cuando la camara se lanza obtengo el alto y el ancho
    public void onCameraViewStarted(int width, int height) {
        alto = height;
        ancho = height;
    }

    @Override
    public void onCameraViewStopped() {

    }

    /*
    Con este metodo obtengo el frame que se esta obteniedo de la cámara y es aqui donde
    se realizan todas las acciones segun la elección del usuario
     */
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

    /*
    Metodo que devielve a imagen sin aplicar ningn tipo de tratamiento de imagen
     */
    private Mat original(CameraBridgeViewBase.CvCameraViewFrame frame) {
        return frame.rgba();
    }

    /*
    Con este metodo se devuelve un filtro de escala de gises aplicado sobre la imagen
    original
     */
    private Mat grises(CameraBridgeViewBase.CvCameraViewFrame frame) {
        return frame.gray();
    }

    /*
    Con este metodo se regula que solo se obtengan solo los objetos cuyo culor estñe entre los rangos
    establecidos. En este caso será un fondo negro y en blanco se observa los colores establecidos
     */
    private Mat naranja(CameraBridgeViewBase.CvCameraViewFrame frame) {
        imgHSV = new Mat(ancho,alto, CvType.CV_16UC4);
        imgThresholded = new Mat(ancho,alto,CvType.CV_16UC4);
        Imgproc.cvtColor(frame.rgba(), imgHSV, Imgproc.COLOR_BGR2HSV);
        Core.inRange(imgHSV,sc1,sc2,imgThresholded);

        Mat imgDilatedMat = new Mat();
        Imgproc.dilate ( imgThresholded, imgDilatedMat, new Mat() );

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(imgThresholded, contours, hierarchy, Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Imgproc.drawContours(imgThresholded, contours, contourIdx, new Scalar(130, 255, 255), -1);
        }

        return imgThresholded;
    }


    /*
    Con este metodo se obtiene, con una imagen de fondo negro, los bordes que tiene la imagen pintados en blanco.
     */
    private Mat bordesBW(CameraBridgeViewBase.CvCameraViewFrame frame) {
        imgThresholded = new Mat(ancho,alto,CvType.CV_16UC4);
        Imgproc.Canny(frame.gray(), imgThresholded,120,180);

        return imgThresholded;
    }

    /*
    Con este metodo se realiza una mirilla al centro de la imgen y que diga el código rgb
    y el nombre del color que está apuntando
     */
    private Mat apuntarColor(CameraBridgeViewBase.CvCameraViewFrame frame) {
        //se obtiene la imagen original
        Mat mat = frame.rgba();

        // Pixel central
        int alto = mat.height() / 2;
        int ancho = mat.width() / 2;

        //Se recupera el color del pixel central
        double[] color = mat.get(alto, ancho);

        //El color inverso, para pintar la mirilla y verlo siempre
        double[] colorInverso = { 255 - color[0], 255 - color[1], 255 - color[2], 255};

        //Mirilla

        //Obtención de alto y ancho de la pantalla
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int width = metrics.widthPixels; // ancho absoluto en pixels
        int height = metrics.heightPixels; // alto absoluto en pixels

        //Lineas Horizontales
        int altoCamara = height;
        int anchoCamara = width;

        Imgproc.line(mat, new Point(0, altoCamara), new Point(anchoCamara - 25, altoCamara), new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1, 1, 1); //Izquierda
        Imgproc.line(mat, new Point(anchoCamara + 25, altoCamara), new Point(anchoCamara + anchoCamara, altoCamara), new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1, 1, 1); //Derecha

        //Lineas Verticales
        Imgproc.line(mat, new Point(anchoCamara, 0), new Point(anchoCamara, altoCamara - 25), new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1, 1, 1); //Arriba
        Imgproc.line(mat, new Point(anchoCamara, altoCamara + 25), new Point(anchoCamara, altoCamara + altoCamara), new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1, 1, 1); //Abajo

        //Circulo interno
        Imgproc.circle(mat, new Point(ancho, alto), 3, new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), -1);

        //Circulo externo
        Imgproc.circle(mat, new Point(ancho, alto), 50, new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1);
        //FIN CROSSHAIR


        //TEXTO

        //Texto generado en cada frame con el color en BGR
        String texto = "RGB: " + color[0] + " " + color[1] + " " + color[2];
        Imgproc.putText(mat, texto, new Point(10, 80), 3, 2, new Scalar(255, 255, 255, 255), 2);

        //Texto con el nombre del color
        String nombreColor = getColorName(color[0], color[1], color[2]);
        Imgproc.putText(mat, nombreColor, new Point(ancho + 30, 80), 3, 2, new Scalar(255, 255, 255, 255), 2);

        //Rectángulo coloreado del color actual
        Imgproc.rectangle(mat, new Point( 10 , 110), new Point(anchoCamara - 10, 100), new Scalar(color[0], color[1], color[2], 255), -1); //Al pintar, usamos RGBA


        return mat;
    }

    /*
    Metodo que devuelve el nombre del color del punto central del la imagen
     */
    public String getColorName(double r, double g, double b) {

        String nombreColor = null;
        // Se calcula a partir del Hue, jugnado con los rangos
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


