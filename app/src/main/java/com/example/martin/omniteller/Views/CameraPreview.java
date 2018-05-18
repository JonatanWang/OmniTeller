package com.example.martin.omniteller.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.example.martin.omniteller.MainActivity;
import com.example.martin.omniteller.Models.Recognition;
import com.example.martin.omniteller.Models.TFClassifier;
import com.example.martin.omniteller.Models.WikiParser;
import com.example.martin.omniteller.Views.ResultView;

import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by Martin on 2017-12-18.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private String TAG = "test";
    private MainActivity mainActivity;

    public CameraPreview(Context context, Camera camera) {
        super(context);

        this.camera = camera;
        this.mainActivity = (MainActivity) context;


        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(mainActivity);
            camera.startPreview();
        }
        catch (Exception e){
            Log.d(TAG, "surfaceCreated: Error in surfacecreation");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        camera.setDisplayOrientation(90);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
