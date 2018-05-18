package com.example.martin.omniteller;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.martin.omniteller.Models.Recognition;
import com.example.martin.omniteller.Models.TFClassifier;
import com.example.martin.omniteller.Models.WikiParser;
import com.example.martin.omniteller.Views.CameraPreview;
import com.example.martin.omniteller.Views.ResultView;

import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Camera.PreviewCallback {


    // These are the settings for the original v1 Inception model. If you want to
    // use a model that's been produced from the TensorFlow for Poets codelab,
    // you'll need to set IMAGE_SIZE = 299, IMAGE_MEAN = 128, IMAGE_STD = 128,
    // INPUT_NAME = "Mul", and OUTPUT_NAME = "final_result".
    // You'll also need to update the MODEL_FILE and LABEL_FILE paths to point to
    // the ones you produced.
    //
    // To use v3 Inception model, strip the DecodeJpeg Op from your retrained
    // model first:
    //
    // python strip_unused.py \
    // --input_graph=<retrained-pb-file> \
    // --output_graph=<your-stripped-pb-file> \
    // --input_node_names="Mul" \
    // --output_node_names="final_result" \
    // --input_binary=true
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";
    private String TAG = "test";

    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/imagenet_comp_graph_label_strings.txt";
    private static final boolean MAINTAIN_ASPECT = true;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private Integer sensorOrientation;
    private static TFClassifier classifier;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private ImageView imageView;
    private ResultView resultView;
    float time = 0;
    RetrieveFeedTask retrieveFeedTask = new RetrieveFeedTask();
    private Handler handler;
    private HandlerThread handlerThread;
    private Button readMore;

    private CameraPreview preview;
    private static Camera camera = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cars);

        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE,false);

        classifier =
                TFClassifier.create(
                        getAssets(),
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        IMAGE_MEAN,
                        IMAGE_STD,
                        INPUT_NAME,
                        OUTPUT_NAME);


        Log.d("test", "onCreate: Recog");
        ArrayList<Recognition> recognitions = (ArrayList<Recognition>) classifier.recognizeImage(bitmap);

        Log.d("test", "size: "+ recognitions.size());
        for (Recognition r: recognitions
             ) {
            Log.d("test", "size: "+ r.getLabel());
        }

        //setupCamera();

        readMore = findViewById(R.id.wikiButton);
        readMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Explicitly declare Intent, start second activity directly
                Intent intent = new Intent(MainActivity.this, WikiActivity.class);
                intent.putExtra("title", resultView.getTitle().toString());
                startActivity(intent);
            }
        });

}

    private void setupCamera(){
        if(!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Log.d(TAG, "setupCamera: no camera ");
            return;
        }

        Log.d(TAG, "setupCamera: Camera found!");

        try {
            camera = Camera.open();
        }
        catch (Exception e){
            Log.d(TAG, "setupCamera: Failed setup", e);
            return;
        }
            preview = new CameraPreview(this, camera);
            FrameLayout prev = (FrameLayout) findViewById(R.id.camera_preview);
            prev.addView(preview);
    }

    @Override
    public void onResume(){
        super.onResume();
        handlerThread = new HandlerThread("detector");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void onStart() {
        super.onStart();
        setupCamera();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putAll(bundle);
    }

    private synchronized void doInBacklground(Runnable runnable){
        if(handler != null)
            handler.post(runnable);
    }


    private boolean processingFrame = false;
    /**
     * When the android camera detects a new preview frame this method is called.

     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {

        //Processing the image in the neural network takes longer time than
        //the time in between frames, therefore some are discarded during processing
        if(processingFrame){
            return;
        }
        time = System.currentTimeMillis();
        processingFrame = true;
        doInBacklground(() -> {
            Bitmap convertedBitmap = convertPreviewToBitmap(data);
            ArrayList<Recognition> recognitions = (ArrayList<Recognition>) classifier.recognizeImage(convertedBitmap);

            if(resultView == null)
                resultView = findViewById(R.id.resultView);

            runOnUiThread(() -> {
                if (!recognitions.isEmpty() && resultView.getTitle().compareTo(recognitions.get(0).getLabel().toUpperCase()) != 0) {
                    resultView.setTitle(recognitions.get(0).getLabel().toUpperCase());
                    resultView.setInfo("Fetching data...");
                    new RetrieveFeedTask().execute(recognitions.get(0).getLabel());
                }
            });
            processingFrame = false;
        });
    }

    /**
     * Convert the preview frame to a bitmap the
     * neural network model can handle
     * @param data Preview frame to be converted
     * @return converted bitmap
     */
    private Bitmap convertPreviewToBitmap(byte[] data){
        int width = camera.getParameters().getPreviewSize().width;
        int height = camera.getParameters().getPreviewSize().height;
        YuvImage yImg = new YuvImage(data, camera.getParameters().getPreviewFormat(), width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yImg.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        byte[] bytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, false);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }
    /**
     * Class for connection to internet in the background:
     * Parses information from wiki page based on name (urls)
     */
    class RetrieveFeedTask extends AsyncTask<String, Void, Elements> {

        protected Elements doInBackground(String... urls) {

            Elements paragraphs = WikiParser.getWikiInfoFromName(urls[0]);

            if(isCancelled())
                return null;
            return paragraphs;
        }

        protected void onPostExecute(Elements newsHeadlines) {

            if(newsHeadlines == null) {
                return;
            }
            resultView.setInfo( newsHeadlines.first().text());
        }
    }
}
