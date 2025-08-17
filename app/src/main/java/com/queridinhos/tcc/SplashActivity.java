package com.queridinhos.tcc;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    //Tempo tela splash
    private static final int SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //ImageView
        ImageView imgSplash = findViewById(R.id.imgSplash);

        //Animação
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);

        //Animação de fade in
        imgSplash.startAnimation(fadeIn);

        //Aplica pulse
        new Handler().postDelayed(() -> {
            imgSplash.startAnimation(pulse);
        }, 1000);

        //Aciona Main Activity após anim
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}