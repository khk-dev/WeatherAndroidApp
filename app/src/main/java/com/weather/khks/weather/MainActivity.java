package com.weather.khks.weather;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.weather.khks.weather.adapters.CityListAdapter;
import com.weather.khks.weather.models.City;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listViewCity;
    Button buttonAddCity;
    AlertDialog alertDialogAddCity;
    DatabaseReference databaseWeather;

    List<City> cityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // setup require object(s) and control(s)
        setup();
        setupEvent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshCityList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_add_city, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_city:
                // display Add City dialog
                displayAddCityDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setup(){
        // FirebaseDatabase
        databaseWeather = FirebaseDatabase.getInstance().getReference("cities");

        // CityListView
        listViewCity = (ListView) findViewById(R.id.listViewCity);

        cityList = new ArrayList<>();
    }

    private void setupEvent() {
        try {
            // ListViewCity's Item Click Event
            listViewCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    TextView textViewCity = (TextView) view.findViewById(R.id.textViewCity);
                    String selectedCity = textViewCity.getText().toString().trim();

                    Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
                    intent.putExtra("city", selectedCity);
                    startActivity(intent);
                }
            });

            // ListViewCity's Item Long Click Event (To Delete City)
            listViewCity.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    TextView textViewCityID = (TextView) view.findViewById(R.id.textViewCityID);
                    displayDeleteCityConfirmDialog(textViewCityID.getText().toString().trim());
                    return true;
                }
            });
        }
        catch(Exception e) {
            showMessage(e.getMessage().toString());
        }
    }

    private void refreshCityList(){
        databaseWeather.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // clear previous data
                cityList.clear();

                // initialize city list
                for(DataSnapshot citySnapshot:dataSnapshot.getChildren()){
                    City city = citySnapshot.getValue(City.class);
                    cityList.add(city);
                }

                Collections.sort(cityList, new Comparator<City>(){
                    @Override
                    public int compare(City cityFirst, City citySecond) {
                        return cityFirst.getCityName().compareTo(citySecond.getCityName());
                    }
                });

                // create custom adapter
                CityListAdapter cityListAdapter = new CityListAdapter(MainActivity.this, cityList);
                // set adapter
                listViewCity.setAdapter(cityListAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayDeleteCityConfirmDialog(final String cityID) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete");
            builder.setMessage("Are you sure to delete?");

            // "Yes" Button
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Delete Selected City;
                    deleteCity(cityID);
                    showMessage("Successfully deleted");
                    // clear memory
                    dialogInterface.dismiss();
                }
            });

            // "No" Button
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Do Nothing
                    // clear memory
                    dialogInterface.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        catch (Exception e)
        {
            showMessage("Unable to delete.");
        }
    }

    private void displayAddCityDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_city, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextCityName = (EditText) dialogView.findViewById(R.id.editTextCity);
        buttonAddCity = (Button) dialogView.findViewById(R.id.buttonAddCity);
        buttonAddCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAlreadyExistCityName(editTextCityName.getText().toString().trim());
            }
        });

        alertDialogAddCity = dialogBuilder.create();
        alertDialogAddCity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialogAddCity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialogAddCity.show();
    }

    private void isAlreadyExistCityName(String cityName)
    {
        final String checkCityName = cityName;
        Query query = databaseWeather.orderByChild("cityName").equalTo(checkCityName.toUpperCase());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren()) {
                    showMessage("City is already saved!");
                }
                else{
                    addCity(checkCityName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showMessage("Please try again!");
            }
        });
    }

    private void addCity(String cityName) {
        if (!TextUtils.isEmpty(cityName)) {
            // generate new unique id
            String id = databaseWeather.push().getKey();
            // create new City object
            // save city name in capitialize format
            City city = new City(id, cityName.toUpperCase());
            // store the newly created City object under the given ID.
            databaseWeather.child(id).setValue(city);
            // display success message
            showMessage("City is added.");
            alertDialogAddCity.dismiss();
        } else {
            // display error message
            showMessage("Please enter city name!");
        }
    }

    private boolean deleteCity(String cityID) {
        try {
            // return false if there is cityID is empty.
            if(TextUtils.isEmpty(cityID)) return false;

            DatabaseReference databaseReferenceCity = FirebaseDatabase.getInstance().getReference("cities").child(cityID);
            databaseReferenceCity.removeValue();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
