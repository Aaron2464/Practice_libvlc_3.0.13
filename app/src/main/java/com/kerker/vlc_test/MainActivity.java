package com.kerker.vlc_test;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    LibVLC mLibVLC;
    MediaPlayer mMediaPlayer;
    SurfaceView mSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurface = findViewById(R.id.surfaceView);
        // Create LibVLC
        ArrayList<String> options = new ArrayList<String>();
        options.add("-vvv"); // verbosity
        options.add("--autoscale");
        mLibVLC = new LibVLC(this, options);

        // Creating media player
        mMediaPlayer = new MediaPlayer(mLibVLC);
//        mMediaPlayer.setAspectRatio("16:9");
        mMediaPlayer.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type){
                    case MediaPlayer.Event.Buffering:
//                        mListener.onProgress(event.getBuffering());
                        break;
                    case MediaPlayer.Event.EncounteredError:
                    case MediaPlayer.Event.EndReached:
                        break;
                }
            }
        });

        // Seting up video output
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.setVideoView(mSurface);
        vout.setWindowSize(1920,1080);
        vout.attachViews();
        Media m = new Media(mLibVLC, Uri.parse("rtsp://18.179.83.233:50013/mcast/12"));
        mMediaPlayer.setMedia(m);
        mMediaPlayer.play();
    }
}
