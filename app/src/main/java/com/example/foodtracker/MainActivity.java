package com.example.foodtracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.widget.EditText;

import com.example.foodtracker.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import static com.example.foodtracker.Notif.NOTIFICATION_ID;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAMERA_PREF = "camera_pref";
    public static final String FOOD_FILE = "food.json";
    private NotificationManagerCompat notificationManager;
    ArrayList<Food> almostExpired;
    Date expCheck = Calendar.getInstance().getTime();

    public static final int EXPIRED_DAYS_THRESHOLD = 4;

    static File foodFile;
    static FileReader fileReader = null;
    static FileWriter fileWriter = null;
    static BufferedReader bufferedReader = null;
    static BufferedWriter bufferedWriter = null;
    static String response = null;

    public ActivityMainBinding getBinding() {
        return binding;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (getFromPref(this, ALLOW_KEY)) {
                showSettingsAlert();
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    showAlert();
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }
            }
        }

        foodFile = new File(this.getFilesDir(), FOOD_FILE);

//        Snackbar.make(binding.getRoot(), ""+foodFile.getAbsoluteFile().getPath(), Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();

        if (!foodFile.exists()) {
            try {
                foodFile.createNewFile();
                fileWriter = new FileWriter(foodFile.getAbsoluteFile());
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write("[]");

                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        notificationManager = NotificationManagerCompat.from(this);

        expCheck.setHours(0);
        expCheck.setSeconds(0);
        expCheck.setMinutes(0);
        expCheck.setDate(expCheck.getDate() + 2);


        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DAY_OF_MONTH, EXPIRED_DAYS_THRESHOLD);

        almostExpired =  new ArrayList<Food>();
        try {
            for(Food a:getFoodsArray()){
                if(c.after(a.getExpDate())) {//expCheck.compareTo(a.getExpDate()) >= 0 ||
                    boolean something = false;
                    for(Food b: almostExpired){
                        if(a.equals(b)){
                            something = true;
                        }
                    }
                    if(!something) {
                        almostExpired.add(a);
                        sendNotification(a);
                    }
                }
            }
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static void saveToPreferences(Context context, String key, Boolean allowed) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.commit();
    }

    public static Boolean getFromPref(Context context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));

    }

    public void sendNotification(Food f){
        String title = "Food Expiring Soon";
        String message = "Hey! Your Item \"" + f.getType() + "\" is about to expire in "+EXPIRED_DAYS_THRESHOLD+" days!";

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_ID)
                .setSmallIcon(R.drawable.ic_foodicon)
                .setContentTitle(title)
                .setContentText(message)
                .build();
        notificationManager.notify(1, notification );
    }

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                });
        alertDialog.show();
    }

    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(MainActivity.this);
                    }
                });

        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean
                                showRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                        this, permission);

                        if (showRationale) {
                            showAlert();
                        } else if (!showRationale) {
                            // user denied flagging NEVER ASK AGAIN
                            // you can either enable some fall back,
                            // disable features of your app
                            // or open another dialog explaining
                            // again the permission and directing to
                            // the app setting
                            saveToPreferences(MainActivity.this, ALLOW_KEY, true);
                        }
                    }
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if( photoURI != null) {

                InputImage image;
                try {
                    image = InputImage.fromFilePath(this, photoURI);
                    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                    Task<Text> result = recognizer.process(image)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text visionText) {
                                    // Task completed successfully
                                    // ...
                                    StringBuilder text = new StringBuilder(" ");
                                    for(Text.TextBlock t : visionText.getTextBlocks()) {
                                        if(t != null) {
                                            text.append(t.getText());
                                        }
                                    }

                                    Date parsedDate;
                                    try {
                                        parsedDate = DateParser.parse(text.toString());
                                    } catch(DateParser.ParseException e) {
                                        Snackbar.make(binding.getRoot(), e.getMessage() + " " + text, Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();

                                        /*AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle("Date scanning failed. Please enter date manually.");

                                        // Set up the input
                                        final EditText input = new EditText(MainActivity.this);
                                        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_);
                                        builder.setView(input);



                                        // Set up the buttons
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    JSONArray messageDetails = getFoods();
                                                    messageDetails.put((new Food(parsedDate, input.getText().toString())).toJSON());
                                                    writeFoods(messageDetails);
                                                } catch(Exception e) {
                                                    Snackbar.make(binding.getRoot(), "_"+e.getMessage(), Snackbar.LENGTH_LONG)
                                                            .setAction("Action", null).show();
                                                }
                                            }
                                        });
                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

                                        builder.show();*/
                                        return;
                                    }

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Enter Food Name");

                                    // Set up the input
                                    final EditText input = new EditText(MainActivity.this);
                                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                                    builder.setView(input);



                                    // Set up the buttons
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                JSONArray messageDetails = getFoods();
                                                messageDetails.put((new Food(parsedDate, input.getText().toString())).toJSON());
                                                writeFoods(messageDetails);
                                            } catch(Exception e) {
                                                Snackbar.make(binding.getRoot(), "_"+e.getMessage(), Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
                                        }
                                    });
                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });

                                    builder.show();


                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                            throw new RuntimeException("recognizer failed");
                                        }
                                    });



                } catch (Exception e) {
                    Snackbar.make(binding.getRoot(), "_"+e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        }
    }

    public static JSONArray getFoods() throws IOException, JSONException {
        StringBuffer output = new StringBuffer();

        fileReader = new FileReader(foodFile.getAbsolutePath());

        bufferedReader = new BufferedReader(fileReader) ;

        String line = "";

        while ((line = bufferedReader.readLine()) != null) {
            output.append(line + "\n");
        }
        response = output.toString();

        bufferedReader.close();

        JSONArray messageDetails = new JSONArray(response);
        return messageDetails;
    }

    public static void writeFoods(JSONArray messageDetails) throws IOException {
        fileWriter = new FileWriter(foodFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fileWriter);
        bw.write(messageDetails.toString());

        bw.close();
    }

    public static Food[] getFoodsArray() throws IOException, JSONException, ParseException {
        JSONArray arr = getFoods();
        Food[] newFoods = new Food[arr.length()];

        for(int i = 0; i<newFoods.length; i++) {
            newFoods[i] = Food.fromJson((JSONObject)arr.get(i));
        }
        return newFoods;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }

        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }


    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivity(intent);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }
    Uri photoURI;
    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "latest";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}