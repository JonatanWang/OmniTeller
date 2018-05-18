package com.example.martin.omniteller.Models;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by Martin on 2017-12-14.
 */

public class TFClassifier {

    private TensorFlowInferenceInterface inferenceInterface;

    private float THRESHOLD =0.20f;
    private int MAX_RESULTS = 3;

    // Config values.
    private String inputName;
    private String outputName;
    private int inputSize;
    private int imageMean;
    private float imageStd;

    // Pre-allocated buffers.

    private ArrayList<String> labels = new ArrayList<>();
    private int[] intValues;
    private float[] floatValues;
    private float[] outputs;
    private String[] outputNames;

    public TFClassifier(){}

    public static TFClassifier create(
            AssetManager assetManager,
            String modelFilename,
            String labelFilename,
            int inputSize,
            int imageMean,
            float imageStd,
            String inputName,
            String outputName) {

        TFClassifier  tfClassifier = new TFClassifier();
        tfClassifier.inputName = inputName;
        tfClassifier.outputName = outputName;

        readLabels(labelFilename,assetManager,tfClassifier);

        tfClassifier.inferenceInterface = new TensorFlowInferenceInterface(assetManager,modelFilename);
        final Operation operation = tfClassifier.inferenceInterface.graphOperation(outputName);
        final int numClasses = (int) operation.output(0).shape().size(1);

        tfClassifier.inputSize = inputSize;
        tfClassifier.imageMean = imageMean;
        tfClassifier.imageStd = imageStd;

        // Pre-allocate buffers.
        tfClassifier.outputNames = new String[] {outputName};
        tfClassifier.intValues = new int[inputSize * inputSize];
        tfClassifier.floatValues = new float[inputSize * inputSize * 3];
        tfClassifier.outputs = new float[numClasses];

        return tfClassifier;
    }

    private static void readLabels(String fileName, AssetManager assetManager, TFClassifier tfClassifier){
        String actualFilename = fileName.split("file:///android_asset/")[1];
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(assetManager.open(actualFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                tfClassifier.labels.add(line);
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!" , e);
        }
    }

    //TODO: This is mostly based on googles example and should be rewritten
    public List<Recognition> recognizeImage(final Bitmap bitmap) {

        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - imageMean) / imageStd;
            floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - imageMean) / imageStd;
            floatValues[i * 3 + 2] = ((val & 0xFF) - imageMean) / imageStd;
        }

        // Copy the input data into TensorFlow.
        inferenceInterface.feed(inputName, floatValues, 1, inputSize, inputSize, 3);

        // Run the inference call.
        inferenceInterface.run(outputNames, false);

        // Copy the output Tensor back into the output array.
        inferenceInterface.fetch(outputName, outputs);

        // Find the best classifications.
        PriorityQueue<Recognition> pq =
                new PriorityQueue<Recognition>(
                        3,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });
        for (int i = 0; i < outputs.length; ++i) {
            if (outputs[i] > THRESHOLD) {
                pq.add(
                        new Recognition(
                                "" + i, labels.size() > i ? labels.get(i) : "unknown", outputs[i]));
            }
        }
        final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }

        return recognitions;
    }


}
