package com.kerker.vlc_test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VLCActivity extends AppCompatActivity implements IVLCVout.OnNewVideoLayoutListener, View.OnClickListener {

        private static final String SAMPLE_URL = "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v";
//    private static final String SAMPLE_URL = "http://www.w3school.com.cn/i/movie.ogg";

    private SurfaceView mSurfaceView;

    private VLCVideoLayout mFrameLayout;
    private Button mFullScreenShotBtn;
    private Button mSurfaceViewBtn;
    private Button mLibVlcBtn;

    LibVLC mLibVLC = null;
    MediaPlayer mMediaPlayer = null;
    IVLCVout mIVLCVout = null;

    View.OnLayoutChangeListener onLayoutChangeListener = null;
    Handler handler = new Handler();

    int mVideoHeight = 0;
    int mVideoWidth = 0;
    int mVideoVisibleHeight = 0;
    int mVideoVisibleWidth = 0;
    int mVideoSarNum = 0;
    int mVideoSarDen = 0;

    static final int REQUEST_MEDIA_PROJECTION = 100;
    static final int SURFACE_BEST_FIT = 0;
    static final int SURFACE_FIT_SCREEN = 1;
    static final int SURFACE_FILL = 2;
    static final int SURFACE_16_9 = 3;
    static final int SURFACE_4_3 = 4;
    static final int SURFACE_ORIGINAL = 5;
    static int CURRENT_SIZE = SURFACE_BEST_FIT;

    int mResultCode;
    ImageReader mImageReader;
    MediaProjectionManager projectionManager;
    MediaProjection mediaProjection;
    String imageName;
    Bitmap bitmap;
    Intent mData;
    int width;
    int height;
    int dpi;

    private static final int SCREEN_SHOT = 150;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.surfaceView);
        mFrameLayout = findViewById(R.id.ll_vlc);
        mFullScreenShotBtn = findViewById(R.id.FullScreenBtn);
        mSurfaceViewBtn = findViewById(R.id.SurfaceViewBtn);
        mLibVlcBtn = findViewById(R.id.LibVlcBtn);

        ArrayList<String> options = new ArrayList<>();
        options.add("--aout=opensles");
        options.add("--audio-time-stretch");
        options.add("-vvv");
        options.add("--vout=android-display");


        mLibVLC = new LibVLC(this, options);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        mIVLCVout = mMediaPlayer.getVLCVout();
        if (mSurfaceView != null) {
            mIVLCVout.setVideoView(mSurfaceView);
            mIVLCVout.setVideoSurface(mSurfaceView.getHolder().getSurface(), mSurfaceView.getHolder());
        }
        mIVLCVout.attachViews(this);
        Log.d("tag", "VLC boolewn1: " + mIVLCVout.areViewsAttached());
        final Media media = new Media(mLibVLC, Uri.parse(SAMPLE_URL));
        media.addOption(":network-caching=100");
        media.addOption(":clock-jitter=0");
        media.addOption(":clock-synchro=0");
        media.addOption(":fullscreen");
        mMediaPlayer.setMedia(media);
        mMediaPlayer.setAspectRatio("16:9");
        mMediaPlayer.setScale(0.8f);
        media.release();
        mMediaPlayer.play();

        if (onLayoutChangeListener == null) {
            onLayoutChangeListener = new View.OnLayoutChangeListener() {
                private final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        updateVideoSurfaces();
                    }
                };

                @Override
                public void onLayoutChange(View view, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                        handler.removeCallbacks(runnable);
                        handler.post(runnable);
                    }
                }
            };
        }
        mFrameLayout.addOnLayoutChangeListener(onLayoutChangeListener);

        //********************ScreenShot****************************//
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        }
        mFullScreenShotBtn.setOnClickListener(this);
        mLibVlcBtn.setOnClickListener(this);
    }

    @Override
    public void onClick (View view){
        switch (view.getId()) {
            case R.id.FullScreenBtn:
                DisplayMetrics metric = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metric);
                width = metric.widthPixels;
                height = metric.heightPixels;
                dpi = metric.densityDpi;
                if (ActivityCompat.checkSelfPermission(VLCActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //未取得權限，向使用者要求允許權限
                    ActivityCompat.requestPermissions(VLCActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_SHOT);
                    }
                }
                break;
            case R.id.SurfaceViewBtn:

                break;
            case R.id.LibVlcBtn:
                String snapshotPath = makeSnapshotPath();
                mMediaPlayer.takeSnapShot(snapshotPath, mSurfaceView.getWidth(), mSurfaceView.getHeight());
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCREEN_SHOT) {
            if (resultCode == RESULT_OK) {
                mResultCode = resultCode;
                mData = data;
                setUpMediaProjection();
                setUpVirtualDisplay();
                startCapture();
            }
        }
    }

    private void startCapture() {
        SystemClock.sleep(1000);
        imageName = System.currentTimeMillis() + ".png";
        Image image = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            image = mImageReader.acquireNextImage();
            if (image == null) {
                Log.e("TAG", "image is null.");
                return;
            }
            int width = 0;
            width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            image.close();

            if (bitmap != null) {
                saveScreenshot(bitmap);
            }
        }
    }

    private void setUpVirtualDisplay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
            mediaProjection.createVirtualDisplay("ScreenShout",
                    width, height, dpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
        }
    }

    private void setUpMediaProjection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaProjection = projectionManager.getMediaProjection(mResultCode, mData);
        }
    }

    private void saveScreenshot(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String mPath = makeSnapshotPath();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File screenshotFile = null;
        if (mPath != null) {
            screenshotFile = new File(mPath);
        }
        try {
            if (screenshotFile != null && screenshotFile.exists()) screenshotFile.delete();
            if (screenshotFile != null && screenshotFile.createNewFile()) {
                FileOutputStream fo = new FileOutputStream(screenshotFile);
                fo.write(bytes.toByteArray());
                fo.close();
                Log.d("Saved", "saveScreenshot: ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String makeSnapshotPath() {
        String imageDirectory = StorageCenter.getImageDirectory();
        if (imageDirectory == null) {
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
        String filename = dateFormat.format(new Date());

        String snapshotPath = String.format(Locale.US, "%s/%s.png", imageDirectory, filename);
        return snapshotPath;
    }

    //***********************ScreenShot******************************//

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        /* Change the video placement using the MediaPlayer API */
        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack vtrack = mMediaPlayer.getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (CURRENT_SIZE == SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;

                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (vtrack.sarNum != vtrack.sarDen)
                        videoW = videoW * vtrack.sarNum / vtrack.sarDen;

                    float ar = videoW / (float) videoH;
                    float dar = displayW / (float) displayH;

                    float scale;
                    if (dar >= ar)
                        scale = displayW / (float) videoW; /* horizontal */
                    else
                        scale = displayH / (float) videoH; /* vertical */
                    mMediaPlayer.setScale(scale);
                    mMediaPlayer.setAspectRatio(null);
                } else {
                    mMediaPlayer.setScale(0);
                    mMediaPlayer.setAspectRatio(!videoSwapped ? "" + displayW + ":" + displayH
                            : "" + displayH + ":" + displayW);
                }
                break;
            }
            case SURFACE_16_9:
                mMediaPlayer.setAspectRatio("16:9");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_4_3:
                mMediaPlayer.setAspectRatio("4:3");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_ORIGINAL:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(1);
                break;
        }
    }

    private void updateVideoSurfaces() {
        int sw = getWindow().getDecorView().getWidth();
        int sh = getWindow().getDecorView().getHeight();

        // sanity check
        if (sw * sh == 0) {
            return;
        }

        mMediaPlayer.getVLCVout().setWindowSize(sw, sh);

        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mSurfaceView.setLayoutParams(lp);
            lp = mFrameLayout.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mFrameLayout.setLayoutParams(lp);
            changeMediaPlayerLayout(sw, sh);
            return;
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            mMediaPlayer.setAspectRatio(null);
            mMediaPlayer.setScale(0);
        }

        double dw = sw, dh = sh;
        final boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mVideoSarNum / mVideoSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_SCREEN:
                if (dar >= ar)
                    dh = dw / ar; /* horizontal */
                else
                    dw = dh * ar; /* vertical */
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        // set display size
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        mSurfaceView.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = mFrameLayout.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        mFrameLayout.setLayoutParams(lp);

        mSurfaceView.invalidate();
    }

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth,
                                 int visibleHeight, int sarNum, int sarDen) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mVideoSarNum = sarNum;
        mVideoSarDen = sarDen;
        updateVideoSurfaces();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (onLayoutChangeListener != null) {
            mFrameLayout.removeOnLayoutChangeListener(onLayoutChangeListener);
            onLayoutChangeListener = null;
        }

        mMediaPlayer.stop();

        mIVLCVout.detachViews();
        Log.d("tag", "VLC boolewn1: " + mIVLCVout.areViewsAttached());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mLibVLC.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaPlayer != null) {
            mMediaPlayer.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

}
