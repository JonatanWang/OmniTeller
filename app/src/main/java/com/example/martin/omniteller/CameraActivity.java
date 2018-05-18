package com.example.martin.omniteller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.AsyncTask;

import com.example.martin.omniteller.Models.Recognition;
import com.example.martin.omniteller.Views.CameraPreview;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by Martin on 2017-12-27.
 */

public class CameraActivity implements android.hardware.Camera.PreviewCallback {
    private float time;


    @Override
    public void onPreviewFrame(byte[] bytes, android.hardware.Camera camera) {

    }
}
