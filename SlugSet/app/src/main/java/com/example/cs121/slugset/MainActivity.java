package com.example.cs121.slugset;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.media.IMediaBrowserServiceCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    MediaPlayer backgroundMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backgroundMusic = MediaPlayer.create(this, R.raw.music);
    }

    public void soloButtonPressed(View v) {
        Intent intent = new Intent(this, SoloMode.class);
        startActivity(intent);
    }

    public void multiplayerButtonPressed(View v) {
        Intent intent = new Intent(this, MultiplayerMode.class);
        startActivity(intent);
    }

    @Override
    protected void onPause(){
        super.onPause();

        if (backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }

    }

    // Volume button pressed
    public void volumeControl(View view){
        if (backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        } else {
            backgroundMusic.start();
        }
    }

}
