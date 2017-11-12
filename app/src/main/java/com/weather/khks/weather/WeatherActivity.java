package com.weather.khks.weather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.weather.khks.weather.models.Atmosphere;
import com.weather.khks.weather.models.Channel;
import com.weather.khks.weather.models.Item;
import com.weather.khks.weather.services.WeatherServiceCallback;
import com.weather.khks.weather.services.YahooWeatherService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class WeatherActivity extends AppCompatActivity implements WeatherServiceCallback {

    private LinearLayout linearLayoutWeatherInfo;
    private ImageView imageViewWeatherIcon;
    private TextView textViewTemperature;
    private TextView textViewHumidity;
    private TextView textViewPressure;
    private TextView textViewCondition;
    private TextView textViewLocation;
    private TextView textViewLastUpdate;

    private YahooWeatherService service;
    private ProgressDialog dialog;

    private String selectedCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        loadIntent();
        setup();
        loadWeatherInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_refresh_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh_weather:
                // refresh weather infos
                loadWeatherInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadIntent() {
        Intent intent = getIntent();
        this.selectedCity = intent.getStringExtra("city");
    }

    private void setup() {
        // MainLinearLayout WeatherInfo
        linearLayoutWeatherInfo = (LinearLayout) findViewById(R.id.linearLayoutWeatherInfo);
        linearLayoutWeatherInfo.setVisibility(View.INVISIBLE);
        // Weather Icon
        imageViewWeatherIcon = (ImageView) findViewById(R.id.imageViewWeatherIcon);
        // Temperature
        textViewTemperature = (TextView) findViewById(R.id.textViewTemperature);
        // Humidity
        textViewHumidity = (TextView) findViewById(R.id.textViewHumidity);
        // Pressure
        textViewPressure = (TextView) findViewById(R.id.textViewPressure);
        // Condition such as Sunny, Cloudy, etc.
        textViewCondition = (TextView) findViewById(R.id.textViewCondition);
        // Location ( City )
        textViewLocation = (TextView) findViewById(R.id.textViewLocation);
        // Last Update Time
        textViewLastUpdate = (TextView) findViewById(R.id.textViewLastUpdate);

        service = new YahooWeatherService(this);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
    }

    private void loadWeatherInfo() {
        linearLayoutWeatherInfo.setVisibility(View.INVISIBLE);
        dialog.show();
        //service.refreshWeather("Sydney, Australia");
        //service.refreshWeather("Sydney");
        //service.refreshWeather("Yangon");
        //String test = "https://query.yahooapis.com/v1/public/yql?q=%s&format=json";
        service.refreshWeather(this.selectedCity);
    }

    @Override
    public void serviceSuccess(Channel channel) {
        dialog.hide();

        Atmosphere atmosphere = channel.getAtmosphere();
        Item item = channel.getItem();

        /*
        * load icons directly from app resources locally
        int resourceId = getResources().getIdentifier("drawable/icon_"+ item.getCondition().getCode(), null, getPackageName());

        Drawable weatherIconDrawable = getResources().getDrawable(resourceId);

        imageViewWeatherIcon.setImageDrawable(weatherIconDrawable);
        */

        // dynamically download icon from Firebase Storage
        downloadImages(item.getCondition().getCode());
        // end download


        // Temperature
        // "\u00B0" => unicode degree symbol
        textViewTemperature.setText(item.getCondition().getTemperature() + "\u00B0" + channel.getUnits().getTemperature());
        // Humidity
        textViewHumidity.setText(atmosphere.getHumidity().toString());
        // Pressure
        textViewPressure.setText(atmosphere.getPressure().toString() + " " + channel.getUnits().getPressure());
        // Condition
        textViewCondition.setText(item.getCondition().getDescription());
        // Location
        textViewLocation.setText(service.getLocation());
        // Refresh Last Update Date
        refreshLastUpdateTime();
        // Show Weather Info
        linearLayoutWeatherInfo.setVisibility(View.VISIBLE);
    }

    private void downloadImages(int imageCode){
        try {
            //StorageReference mStorageRef = FirebaseStorage.getInstance().getReference().child("weather_icons/yahoo_logo.png");
            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference().child("weather_icons/icon_" + imageCode + ".png");
            final File localFile = File.createTempFile("images", "png");
            mStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap= BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    imageViewWeatherIcon.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
        catch (Exception e)
        {
            showMessage(e.getMessage().toString());
        }
    }

    private void refreshLastUpdateTime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String date = sdfDate.format(now);
        textViewLastUpdate.setText(date);
    }

    @Override
    public void serviceFailure(Exception exception) {
        dialog.hide();
        linearLayoutWeatherInfo.setVisibility(View.INVISIBLE);
        showMessage(exception.getMessage());
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
