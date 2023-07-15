package com.maemresen.infsec.keyloggerParent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.window.SplashScreen;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class HomeAnimation extends AppCompatActivity {
    @Override
    protected void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.home_animation_splashscreen );
    
        @SuppressLint( {"MissingInflatedId","LocalSuppress"} )
        ImageView parentalEye  = findViewById( R.id.ParentalEye);
    
        Glide.with( this ).load(R.drawable.parental_eye_rise).into(parentalEye);
    
        new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {
                Intent home = new Intent( HomeAnimation.this, SignInUpActivity.class);
                startActivity(home);
                finish();
            }
        }, 2600);
    }
}