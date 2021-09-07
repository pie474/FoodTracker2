package com.example.foodtracker.ui.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.example.foodtracker.Food;
import com.example.foodtracker.R;
import com.example.foodtracker.databinding.ActivityMainBinding;
import com.example.foodtracker.ui.addFood.InputFood;
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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    public static final String CURRENT_FOODS_FILE = "food.json";
    public static final String CACHED_FOOD_FILE = "cachedFood.json";

    private ActivityMainBinding binding;

    private NotificationManagerCompat notificationManager;

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
                    addFood(result);
                }
            });

    BroadcastReceiver notificationBroadcastReceiver = new NotificationBroadcastReceiver();


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

        //Log.d("debug", this.getFilesDir());

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

        try {
            items.addAll(readFoodsArray());
        } catch (Exception e) {
            Log.d("debug", e.getMessage());
        }

        notificationManager = NotificationManagerCompat.from(this);


        recyclerView = findViewById(R.id.recycler_view);

        FloatingActionButton button = findViewById(R.id.addButton);
        button.setOnClickListener(view -> {
            addFoodLauncher.launch(0);
        });


        foodAdapter = new FoodAdapter(items, this, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(foodAdapter);

        //registerReceiver(notificationBroadcastReceiver, new IntentFilter(AlarmReceiver.ACTION));

        /*SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);

        if (firstStart) { //CODE TO RUN ON FIRST STARTUP (atm setting up notifs)
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("NotificationText", "some text");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);//PendingIntent.FLAG_UPDATE_CURRENT
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Log.d("debug", "SET");
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), 60000, pendingIntent);
                //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
            }
            //prefs.edit().putBoolean("firstStart", false).apply();
        }*/
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

    private void writeFoods() {
        JSONArray arr = new JSONArray();
        for(Food f : items) {
            try {
                arr.put(f.toJSON());
            } catch (JSONException e) {
                Log.d("debug", e.getMessage());
            }
        }

        try {
            writeJSON(currentFoodsFile, arr);
        } catch(IOException e) {
            Log.d("debug", e.getMessage());
        }
    }

    public ArrayList<Food> getActiveFoodArray() {
        return items;
    }

    ArrayList<Food> readFoodsArray() throws IOException, JSONException, ParseException {
        JSONArray arr = readJSON(currentFoodsFile);
        ArrayList<Food> newFoods = new ArrayList<>();

        for(int i = 0; i<arr.length(); i++) {
            newFoods.add(Food.fromJson((JSONObject)arr.get(i)));
        }
        return newFoods;
    }

    private void addFood(Food f) {
        for(int i = 0; i<items.size()+1; i++) {
            if(i==items.size()) {
                items.add(f);
                foodAdapter.notifyItemInserted(i);
                break;
            }
            if(items.get(i).compareTo(f) > 0) {
                items.add(i, f);
                foodAdapter.notifyItemInserted(i);
                break;
            }

        }

        writeFoods();
    }

    public void removeFood(int index) {
        items.remove(index);
        foodAdapter.notifyItemRemoved(index);
        writeFoods();
    }

    @Override
    protected void onResume() {
        //refresh();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(notificationBroadcastReceiver);
    }

    public static class NotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
//                ArrayList<Food> arr = readFoodsArray();
                ArrayList<Food> arr = new ArrayList<>();
                ArrayList<Food> aboutToExpire = new ArrayList<>();
                ArrayList<Food> expired = new ArrayList<>();
                for(Food f : arr) {
                    @Food.ExpState int state = f.getExpirationState();
                    if(state == Food.EXP_STATE_EXPIRED) {
                        expired.add(f);
                    } else if(state == Food.EXP_STATE_ALMOST) {
                        aboutToExpire.add(f);
                    } else if(state == Food.EXP_STATE_OK){
                        break;
                    }
                }

                NotificationHelper notificationHelper = new NotificationHelper(context);

                if(aboutToExpire.size() > 0){
                    StringBuilder message = new StringBuilder();
                    for(Food f : aboutToExpire) {
                        message.append(f.getType());
                        message.append(", ");
                    }
                    message.setLength(message.length()-2);
                    notificationHelper.createNotification("These foods are about to expire!", message.toString());
                }
                if(expired.size() > 0){
                    StringBuilder message = new StringBuilder();
                    for(Food f : expired) {
                        message.append(f.getType());
                        message.append(", ");
                    }
                    message.setLength(message.length()-2);
                    notificationHelper.createNotification("These foods have expired!", message.toString());
                }
            } catch (Exception e) {
                Log.d("debug", "alarm recieved with exception: " + e.getMessage());
            }
        }
    }
}