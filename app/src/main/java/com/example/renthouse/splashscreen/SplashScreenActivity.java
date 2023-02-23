package com.example.renthouse.splashscreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.renthouse.authentication.LoginActivity;
import com.example.namespace.R;

public class SplashScreenActivity extends AppCompatActivity {
     ImageButton nextButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        nextButton=findViewById(R.id.next_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}