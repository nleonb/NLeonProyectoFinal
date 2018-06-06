package com.example.nestorleonbrito.nleonpfc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class Menu extends Activity{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        //Brillo máximo permanente
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ImageView imgOpenCV = (ImageView) findViewById(R.id.imgOpenCV);
        imgOpenCV.setImageResource(R.drawable.opencv_logo);
        ImageView imgAndroid = (ImageView) findViewById(R.id.imgAndroid);
        imgAndroid.setImageResource(R.drawable.android_logo);

        Button botonImagenOriginal = (Button)findViewById(R.id.imgOriginal);
        Button botonSaberColor = (Button)findViewById(R.id.saberColor);
        Button botonBordesBW = (Button)findViewById(R.id.bordesBW);
        Button botonNaranja = (Button)findViewById(R.id.naranja);
        Button botonGrises = (Button)findViewById(R.id.grises);

        //Accion del boton de imagen original
        botonImagenOriginal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intentIniciar = new Intent(Menu.this, MainActivity.class);
                intentIniciar.putExtra("filtro", "original");
                startActivity(intentIniciar);
            }
        });

        //Accion del boton de saberColor
        botonSaberColor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intentIniciar = new Intent(Menu.this, MainActivity.class);
                intentIniciar.putExtra("filtro", "apuntarColor");
                startActivity(intentIniciar);
            }
        });

        //Accion del boton de bordesBW
        botonBordesBW.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intentIniciar = new Intent(Menu.this, MainActivity.class);
                intentIniciar.putExtra("filtro", "bordesBw");
                startActivity(intentIniciar);
            }
        });

        //Accion del boton de Naranja
        botonNaranja.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intentIniciar = new Intent(Menu.this, MainActivity.class);
                intentIniciar.putExtra("filtro", "naranja");
                startActivity(intentIniciar);
            }
        });

        //Accion del boton de Grises
        botonGrises.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intentIniciar = new Intent(Menu.this, MainActivity.class);
                intentIniciar.putExtra("filtro", "grises");
                startActivity(intentIniciar);
            }
        });

    }
}
