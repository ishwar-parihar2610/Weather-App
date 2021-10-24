package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.adapter.WeatherAdapter;
import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.model.WeatherModel;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private ArrayList<WeatherModel> weatherModelArrayList;
    WeatherAdapter adapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        weatherModelArrayList = new ArrayList<>();
        binding.weatherRecycleView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        adapter = new WeatherAdapter(weatherModelArrayList);
        binding.weatherRecycleView.setAdapter(adapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);

        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName=getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInformation(cityName);
        binding.searchImageView.setOnClickListener(v->{
            String city=binding.cityEdt.getText().toString();
            if (city.isEmpty()){
                Toast.makeText(this, "Please enter City Name", Toast.LENGTH_SHORT).show();
            }else{
                getWeatherInformation(city);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_CODE){
            if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted...", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Please Provide The Permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude) {
        String cityName = "Nor Found";
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 10);
            for (Address adr : addresses) {
                if (adr != null) {
                    String city = adr.getLocality();
                    if (city!=null&&!city.equals("")){
                        cityName=city;
                    }else{
                        Log.d("TAG","CITY NoT FOUND");
                        Toast.makeText(this, "User City Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInformation(String cityName) {

        String url = "https://api.weatherapi.com/v1/forecast.json?key=5bb030b01777492788751524212410&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        binding.cityNameTv.setText(cityName);
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            binding.loadingProgressBar.setVisibility(View.GONE);
            binding.homeLayout.setVisibility(View.VISIBLE);
            weatherModelArrayList.clear();
                try {
                    String temperature=response.getJSONObject("current").getString("temp_c");
                    binding.temperatureTv.setText(temperature+"°C");
                    int isDay=response.getJSONObject("current").getInt("is_day");
                    String condition=response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon=response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    String conditionCode=response.getJSONObject("current").getJSONObject("condition").getString("code");
                    Picasso.get().load("https:".concat(conditionIcon)).into(binding.imageViewIcon);
                    binding.conditionTv.setText(condition);
                    if (isDay==1){
                        Picasso.get().load("https://images.unsplash.com/photo-1548266652-99cf27701ced?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=435&q=80").into(binding.backgroundImageView);
                    }else{
                        Picasso.get().load("https://images.unsplash.com/photo-1436891620584-47fd0e565afb?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=387&q=80").into(binding.backgroundImageView);
                    }
                    JSONObject forCastObj=response.getJSONObject("forecast");
                    JSONObject forCastO=forCastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray=forCastO.getJSONArray("hour");
                    for (int i=0;i<hourArray.length();i++){
                        JSONObject hourObj=hourArray.getJSONObject(i);
                        String time=hourObj.getString("time");
                        String tem=hourObj.getString("temp_c");
                        String img=hourObj.getJSONObject("condition").getString("icon");
                        String windSpeed=hourObj.getString("wind_kph");
                        weatherModelArrayList.add(new WeatherModel(time,tem,img,windSpeed));
                    }
                    adapter.notifyDataSetChanged();





                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please enter Valid City Name", Toast.LENGTH_SHORT).show();
                Log.d("Error",error.getMessage());

            }
        });
        requestQueue.add(jsonObjectRequest);

    }
}