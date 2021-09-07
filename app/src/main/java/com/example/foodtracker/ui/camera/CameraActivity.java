package com.example.foodtracker.ui.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.foodtracker.Food;
import com.example.foodtracker.R;
import com.example.foodtracker.parsing.barcode.BarcodeParser;
import com.example.foodtracker.parsing.barcode.OnBarcodeParseFailedListener;
import com.example.foodtracker.parsing.barcode.OnBarcodeParsedListener;
import com.example.foodtracker.parsing.date.DateParser;
import com.example.foodtracker.ui.main.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CameraActivity extends AppCompatActivity {

    private final String[] REQUIRED_PERMISSIONS = new String[] {Manifest.permission.CAMERA};
    private final int REQUEST_CODE_PERMISSIONS = 10;

    private ImageCapture imageCapture = null;
    private File outputDirectory;
    private ExecutorService cameraExecutor;
    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;

    private FloatingActionButton doneButton;

    ArrayList<VisionStatusItem> items = new ArrayList<>();
    private RecyclerView recyclerView;
    private VisionStatusItemAdapter itemAdapter;

    private VisionStatusItem nameItem = new VisionStatusItem("Name");
    private VisionStatusItem expdateItem = new VisionStatusItem("Exp. Date");

    private final String TAG = "Camera";
    private final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";

    private Bundle detectedAttrs = new Bundle();
    private HashMap<String, String> foodCache = new HashMap<>();

    public CameraActivity() {
        super(R.layout.activity_camera);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }


        outputDirectory = getOutputDirectory();

        cameraExecutor = Executors.newSingleThreadExecutor();

        previewView = findViewById(R.id.viewFinder);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        try {
            JSONArray arr = MainActivity.readJSON(MainActivity.cachedFoodFile);
            for(int i = 0; i<arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                foodCache.put(obj.getString("upc"), obj.getString("name"));
            }
        } catch(Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
//
//        if (savedInstanceState == null) {
//            startCamera();
//        }

        doneButton = findViewById(R.id.finishCameraButton);
        doneButton.setOnClickListener(view -> {
            returnResult();
        });
        doneButton.setVisibility(View.GONE);


        items.add(nameItem);
        items.add(expdateItem);

        recyclerView = findViewById(R.id.statusView);

        itemAdapter = new VisionStatusItemAdapter(items, this, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(itemAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        previewView = null;
        graphicOverlay = null;

        JSONArray update = new JSONArray();
        for(String s : foodCache.keySet()) {
            try {
                update.put(new JSONObject().put("upc", s).put("name", foodCache.get(s)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            MainActivity.writeJSON(MainActivity.cachedFoodFile, update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void returnResult() {
        Intent returnIntent = new Intent();
        returnIntent.putExtras(detectedAttrs);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }



    private boolean allPermissionsGranted() {
        boolean check = true;
        for(String s : REQUIRED_PERMISSIONS) {
            check &= (ContextCompat.checkSelfPermission(getBaseContext(), s) == PackageManager.PERMISSION_GRANTED);
        }
        return check;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    private void takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        if(imageCapture == null) return;

        // Create time-stamped output file to hold the image
        File photoFile = new File(outputDirectory, new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg");

        // Create output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onError(@NotNull ImageCaptureException exc) {
                Log.e(TAG, "Photo capture failed: " + exc.getMessage(), exc);
            }
            @Override
            public void onImageSaved(@NotNull ImageCapture.OutputFileResults output) {
                Uri savedUri = Uri.fromFile(photoFile);
                String msg = "Photo capture succeeded: " + savedUri;
                //Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                Log.d(TAG, msg);
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            ProcessCameraProvider cameraProvider = null;
            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                Log.d(TAG, Arrays.toString(e.getStackTrace()));
            }

            // Preview

            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());


            imageCapture = new ImageCapture.Builder().build();

            graphicOverlay.add(new RecognizedObjectOverlay(graphicOverlay));

            ImageAnalysis analysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();

            analysis.setAnalyzer(cameraExecutor, new Analyzer());




            // Select back camera as a default
            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                Camera camera = cameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, preview, imageCapture, analysis);

                CameraControl cameraControl = camera.getCameraControl();

                previewView.setOnTouchListener((v, event) -> {
                    v.performClick();
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;
                        case MotionEvent.ACTION_UP:
                            // Get the MeteringPointFactory from PreviewView
                            MeteringPointFactory factory = previewView.getMeteringPointFactory();

                            // Create a MeteringPoint from the tap coordinates
                            MeteringPoint point = factory.createPoint(event.getX(), event.getY());

                            // Create a MeteringAction from the MeteringPoint, you can configure it to specify the metering mode
                            FocusMeteringAction action = new FocusMeteringAction.Builder(point).build();

                            // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                            // is asynchronous. You can use it get notified when the focus is successful or if it fails.
                            cameraControl.startFocusAndMetering(action);

                            return true;
                        default:
                            return false;
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private File getOutputDirectory() {
        File[] mediaDirs = getExternalMediaDirs();
        File mediaDir = null;
        if(mediaDirs != null && mediaDirs.length>0) {
            mediaDir = new File(mediaDirs[0], getResources().getString(R.string.app_name));
            mediaDir.mkdirs();
        }

        if(mediaDir != null) {
            return mediaDir;
        } else {
            return getFilesDir();
        }
    }

    private boolean allAttrsDetected() {
        for(String attr : Food.ALL_ATTRS) {
            if(detectedAttrs.getString(attr) == null) {
                return false;
            }
        }
        return true;
    }

    private boolean oneAttrDetected() {
        for(String attr : Food.ALL_ATTRS) {
            if(detectedAttrs.getString(attr) != null) {
                return true;
            }
        }
        return false;
    }

 //a    2/2/21
    private class Analyzer implements ImageAnalysis.Analyzer {
        final Object lock = new Object();

        private TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        private final BarcodeScannerOptions barcodeOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E)
                .build();
        private BarcodeScanner barcodeRecognizer = BarcodeScanning.getClient(barcodeOptions);
        private BarcodeParser barcodeParser = null;

        private boolean needUpdateGraphicOverlayImageSourceInfo = true;

        private int runningTasks = 0;

        @androidx.camera.core.ExperimentalGetImage
        @Override
        public void analyze(ImageProxy imageProxy) {
            if(allAttrsDetected()) {
                returnResult();
            }

            if(oneAttrDetected()) {
                Handler mainHandler = new Handler(getBaseContext().getMainLooper());
                mainHandler.post(() -> doneButton.setVisibility(View.VISIBLE));
            }

            if (needUpdateGraphicOverlayImageSourceInfo) {
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                    graphicOverlay.setImageSourceInfo(
                            imageProxy.getWidth(), imageProxy.getHeight(), false);
                } else {
                    graphicOverlay.setImageSourceInfo(
                            imageProxy.getHeight(), imageProxy.getWidth(), false);
                }
                needUpdateGraphicOverlayImageSourceInfo = false;
            }



            Image mediaImage = imageProxy.getImage();

            if (mediaImage != null) {
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                synchronized(lock) {
                    if(detectedAttrs.getString(Food.NAME_TAG) == null && (barcodeParser == null || barcodeParser.getState() == Thread.State.TERMINATED)) {
                        runningTasks++;
                        barcodeRecognizer.process(image)
                                .addOnSuccessListener(barcodes -> {
                                    for (Barcode barcode : barcodes) {
                                        String upc = barcode.getRawValue();
                                        if(upc==null) continue;
                                        if (BarcodeParser.validUPC(upc)) {
                                            detectedBlocks.add(new Recognizable(barcode.getCornerPoints(), Color.BLUE));
                                            Log.d("debug", upc);

                                            if (foodCache.containsKey(upc)) {//barcode has been seen before
                                                detectedAttrs.putString(Food.NAME_TAG, foodCache.get(upc));
                                                nameItem.setComplete(foodCache.get(upc));
                                                itemAdapter.notifyItemChanged(items.indexOf(nameItem));
                                            } else {//need to look it up
                                                nameItem.setLoading();
                                                itemAdapter.notifyItemChanged(items.indexOf(nameItem));

                                                barcodeParser = new BarcodeParser(upc)
                                                        .addParsedListener(new OnBarcodeParsedListener() {
                                                            @Override
                                                            public void onParsed(String result) {
                                                                detectedAttrs.putString(Food.NAME_TAG, result);
                                                                foodCache.put(upc, result);
                                                                nameItem.setComplete(result);
                                                                itemAdapter.notifyItemChanged(items.indexOf(nameItem));
                                                            }
                                                        })
                                                        .addFailureListener(new OnBarcodeParseFailedListener() {
                                                            @Override
                                                            public void onFailure(Exception e) {
                                                                Log.d("debug", ""+e.getMessage());
                                                                nameItem.reset();
                                                                itemAdapter.notifyItemChanged(items.indexOf(nameItem));
                                                            }
                                                        });
                                                barcodeParser.start();

                                            }
                                        } else {
                                            detectedBlocks.add(new Recognizable(barcode.getCornerPoints(), Color.RED));
                                        }
                                    }
                                })
                                .addOnCompleteListener(task -> closeIfLast(imageProxy));
                    }

                    runningTasks++;
                    textRecognizer.process(image)
                            .addOnSuccessListener(visionText -> {
                                List<DateParser.FoundDate> dates = new ArrayList<>();
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    for (Text.Line line : block.getLines()) {
                                        List<DateParser.FoundDate> foundDates = DateParser.parse(line);
                                        for(DateParser.FoundDate d : foundDates) {
                                            if(d.getCornerPoints() != null) detectedBlocks.add(new Recognizable(d.getCornerPoints(), Color.YELLOW));
                                        }
                                        dates.addAll(foundDates);
                                    }
                                }

                                if(dates.size() > 0) {
                                    LocalDate date = dates.get(0).getDate();
                                    detectedAttrs.putInt(Food.EXP_DAY_TAG, date.getDayOfMonth());
                                    detectedAttrs.putInt(Food.EXP_MONTH_TAG, date.getMonthValue());
                                    detectedAttrs.putInt(Food.EXP_YEAR_TAG, date.getYear());
                                    expdateItem.setComplete(date.toString());
                                    itemAdapter.notifyItemChanged(items.indexOf(expdateItem));
                                }
                            })
                            .addOnCompleteListener(task -> closeIfLast(imageProxy));
                }


            }

        }


        private void closeIfLast(ImageProxy proxy) {
            if(--runningTasks == 0) {
                proxy.close();
                if(graphicOverlay != null) graphicOverlay.postInvalidate();//check null in case detection finishes after activity is closed
            }
        }

    }

    private List<Recognizable> detectedBlocks = new ArrayList<>();

    class RecognizedObjectOverlay extends GraphicOverlay.Graphic {

        Paint paint = new Paint();

        public RecognizedObjectOverlay(GraphicOverlay overlay) {
            super(overlay);
            paint.setColor(Color.YELLOW);
            paint.setAlpha(64);
        }


        @Override
        public void draw(Canvas canvas) {
            for(Iterator<Recognizable> i = detectedBlocks.iterator(); i.hasNext();) {
                Recognizable r = i.next();
                r.draw(canvas, graphicOverlay.getWidth() / (float)graphicOverlay.getImageWidth(),
                        graphicOverlay.getHeight() / (float)graphicOverlay.getImageHeight());
                i.remove();
            }
        }
    }

    class Recognizable {
        private final Paint paint;
        private final Point[] cornerPoints;

        Recognizable(Point[] cornerPoints, @ColorInt int color) {
            paint = new Paint();
            paint.setColor(color);
            paint.setAlpha(64);

            this.cornerPoints = cornerPoints;
        }

        void draw(Canvas canvas, float scaleX, float scaleY) {
            Path path = new Path();
            for(int i = 0; i<cornerPoints.length; i++) {
                if(i==0) {
                    path.moveTo(cornerPoints[i].x * scaleX, cornerPoints[i].y * scaleY);
                } else {
                    path.lineTo(cornerPoints[i].x * scaleX, cornerPoints[i].y * scaleY);
                }
            }
            canvas.drawPath(path, paint);
        }

    }

}