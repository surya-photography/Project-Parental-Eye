package com.maemresen.infsec.keyloggerParent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class HomeAnimation extends AppCompatActivity {
    @Override
    protected void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.home_animation_splashscreen );
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        
        @SuppressLint( { "MissingInflatedId", "LocalSuppress" })
        ImageView parentalEye = findViewById( R.id.ParentalEye );
        
        Glide.with( this ).load( R.drawable.parental_eye_rise ).into( parentalEye );
        
        new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {
                
                SharedPreferences preference = getSharedPreferences( "UserLoginActivity",
                        MODE_PRIVATE );
                boolean checkUserLogin = preference.getBoolean( "login", false );
                
                
                Intent SignUpActivity;
                if (checkUserLogin) {
                    SignUpActivity = new Intent( HomeAnimation.this, MainActivity.class );
                } else {
                    SignUpActivity = new Intent( HomeAnimation.this, SignInUpActivity.class );
                }
                startActivity( SignUpActivity );
                finish();
            }
        }, 2600 );
    }
}