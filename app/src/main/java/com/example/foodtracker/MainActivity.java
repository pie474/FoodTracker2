package com.example.foodtracker;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.foodtracker.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import java.util.Collections;
import java.util.Date;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.foodtracker.Notif.NOTIFICATION_ID;

public class MainActivity extends AppCompatActivity {
    public static final String CURRENT_FOODS_FILE = "food.json";
    public static final String CACHED_FOOD_FILE = "cachedFood.json";

    private ActivityMainBinding binding;

    private NotificationManagerCompat notificationManager;
    ArrayList<Food> almostExpired;

    public static final int EXPIRED_DAYS_THRESHOLD = 4;

    public static File currentFoodsFile, cachedFoodFile;

    ArrayList<Food> items = new ArrayList<>();
    RecyclerView recyclerView;
    FoodAdapter foodAdapter;


    ActivityResultLauncher<Integer> addFoodLauncher = registerForActivityResult(new InputFood(),
            new ActivityResultCallback<Food>() {
                @Override
                public void onActivityResult(Food result) {
                    if(result == null) {
                        Log.d("debug", "null");
                        return;
                    }
                    try {
                        addFood(result);
                    } catch (Exception e) {
                        Log.d("debug", e.getMessage());
                    }
                    //Log.d("debug", "date: "+result);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)//, R.id.navigation_notifications
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);*/


        currentFoodsFile = new File(this.getFilesDir(), CURRENT_FOODS_FILE);
        if (!currentFoodsFile.exists()) {
            try {
                currentFoodsFile.createNewFile();
                FileWriter fileWriter = new FileWriter(currentFoodsFile.getAbsoluteFile());
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write("[]");

                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cachedFoodFile = new File(this.getFilesDir(), CACHED_FOOD_FILE);
        if (!cachedFoodFile.exists()) {
            try {
                cachedFoodFile.createNewFile();
                FileWriter fileWriter = new FileWriter(cachedFoodFile.getAbsoluteFile());
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write("[]");

                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        notificationManager = NotificationManagerCompat.from(this);


        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DAY_OF_MONTH, EXPIRED_DAYS_THRESHOLD);

        almostExpired = new ArrayList<>();
        try {
            for(Food a:getFoodsArray()){
                if(c.after(a.getExpDate())) {
                    almostExpired.add(a);
                    sendNotification(a);
                }
            }
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }

        recyclerView = findViewById(R.id.recycler_view);

        FloatingActionButton button = findViewById(R.id.addButton);
        button.setOnClickListener(view -> {
            addFoodLauncher.launch(0);
        });



        foodAdapter = new FoodAdapter(items, this, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(foodAdapter);

        refresh();
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


    public static JSONArray readJSON(File file) throws IOException, JSONException {
        StringBuilder output = new StringBuilder();

        FileReader fileReader = new FileReader(file.getAbsolutePath());

        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line = "";

        while ((line = bufferedReader.readLine()) != null) {
            output.append(line).append("\n");
        }
        String response = output.toString();

        bufferedReader.close();

        JSONArray messageDetails = new JSONArray(response);
        return messageDetails;
    }

    public static void writeJSON(File file, JSONArray messageDetails) throws IOException {
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), false);
        BufferedWriter bw = new BufferedWriter(fileWriter);
        bw.write(messageDetails.toString());
        bw.close();
    }

    private void writeFoods(JSONArray json) throws IOException{
        writeJSON(currentFoodsFile, json);
        refresh();
    }

    private void writeFoods() {

    }

    private ArrayList<Food> getFoodsArray() throws IOException, JSONException, ParseException {
        JSONArray arr = readJSON(currentFoodsFile);
        Log.d("debug", arr.toString());
        ArrayList<Food> newFoods = new ArrayList<>();

        for(int i = 0; i<arr.length(); i++) {
            newFoods.add(Food.fromJson((JSONObject)arr.get(i)));
        }
        return newFoods;
    }

    private void addFood(Food f) throws IOException, JSONException {
        JSONArray messageDetails = readJSON(currentFoodsFile);
        messageDetails.put(f.toJSON());
        writeFoods(messageDetails);
    }

    public void removeFood(int index) throws IOException, JSONException {
        JSONArray messageDetails = readJSON(currentFoodsFile);
        messageDetails.remove(index);
        writeFoods(messageDetails);
    }

    public void refresh() {
        try {
            ArrayList<Food> newList = getFoodsArray();
            items.clear();
            items.addAll(newList);
            Collections.sort(items);
            foodAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        refresh();
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


}