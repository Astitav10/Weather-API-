package com.example.bmi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIVSearch);
        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);

        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(), location.getLatitude());
        getWeatherInfo(cityName);
        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdt.getText().toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter city name ", Toast.LENGTH_SHORT).show();
                } else {
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please provide permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude) {
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
            for (Address adr : addresses) {
                if (adr != null) {
                    String city = adr.getLocality();
                    if (city != null && city.equals("")) {
                        cityName = city;
                    } else {
                        Log.d("TAG", "City not Found");
                        Toast.makeText(this, "User city not found",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(String CityName) {
        String url = "http://api.weatherapi.com/v1/forecast.json?key=5701b603d31c4994a0d174527232202&q=" + CityName + "&days=1&aqi=no&alerts=no";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();
                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if (isDay==1) {
                        Picasso.get().load("data:image/jpeg;base64,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").into(backIV);
                    } else {
                        Picasso.get().load("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBw0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ8NDQ0NFREWFhURFRUYHSggGBolGxMVITEtJSkrLi4uFx8/ODMsQygxLi4BCgoKDg0NFQ8PFSsZFR0rKy8rKysrKzcrKystLS0rKzcrKysrLS0rNy4sLTA4NysuKzc3Ky0tKy0tKyw3Kys0K//AABEIAQMAwgMBIgACEQEDEQH/xAAbAAEBAAMBAQEAAAAAAAAAAAAAAQIDBAUGB//EACsQAQACAgEDBAECBwEAAAAAAAABAgMRBBIhMQUTUWFBBqEHIjKBkbHRFP/EABcBAQEBAQAAAAAAAAAAAAAAAAABAgP/xAAdEQEBAQACAwEBAAAAAAAAAAAAEQECEgMhQTFx/9oADAMBAAIRAxEAPwD8WF0NogoCCgICggAAACKAgoCCiogqIoigJBIqDEUBsNKigAoIoIgoKgoCC6AQVAEUEQUBAUEARUFkBBQGYoogoCCgIKCIAAACCgIKAgoDEUBBQVNIyRBAAbRRRBQE0aVBAAE0aUBBQEFARNMkBBQEF0AgAqEqAxF0A2igiCiiIyAY6NMtAMTTIBiLoBBU0ggoCCgIKgqCgIjJAQUQbTS6FENKAgujQIaXQCC6NAxNMtAMRdAJpNLoBNCgMRQERQEFQEUEG5FCqChRDShRBkaSrGOhloCMRlpNBERkgRNGlAjHQoEYioJEFQEFQEABvGu+TTTXP3Sq6lY1spRQEqxQErUACkEJtBE7KABSIhMtNs8QqNyS5rcn4a755kSumckLFolwbWl5gSu9GNbdl2VRBFqKJsBzWyTLCEER1YburbzqW1LffL2RrG2/IiDHyIlwzOyJDs9O2SNOeeV3cvXPyxIbyenjyxMMrW08yt5hsvmmY0RezLPl+F4+SXMyxzqRm+3pbNtPvRELOWEdaztPZ5+TzLrjJEuS/mVxjkxAVgABlF5hfclgA3VzfLKMsOcB0+5A5gAABdoAAzx45t4BgNtcEzPT+WzPw7Y43PeEuDnmHV6X6bn5mauDj45yZLbnUdoisebTP4hzb32fefww5+LgZs2XLFerJjrSLT21j3uYi34ncVn70zz5deN+r/HyHrHo/I4OSMXIp0WmN1mJ3W0fUuB9x/ED1aPVeXScFdYsNJpjnX82S0/1Xn67RH9vt8dk4t6zqY7pw53jl/SNJ1S23414jeuz2f0l/wCeMmT3+jr6Y9r3JiK/nq1M9t+P3b3fQ8GJR63rWLHflTGDp6bdMWtXvSLz5mJjt8fu1+o+n1xV3EzMxre07YjzQWYmPMS0IAAAAAAAAAAAAzpfTABs963V1b7tmbl2vXpnw5xILXzD3OLmmuPW/wDunhQ9HiTNo1tjyZca4z66qZ7UmbfiY8R+HDyuXNrROtRH+XbbFPhzcjjTE92eMz9WXK6eHyIyzixW7RMxWfuX0H6j/TmDj8T3Kf1Vrvz5fITE01Mdpidw7OV6zmz44x3tM17b7+Vnv0ZmTa4sfJ1WPmPpqy5731FrTMR4jc6dFMVGOXFSI7NXGY5qTqYnzqYb+TkrMdv9OdZns0jEBQAAAAAAAAAAAAAAb+Nm6ZaBNyj1I5mu7Xk5XVLg2bZ6Y132T46sk7c81lIvJNpXMiU6pJtLEaRYh146Y+jxufz9ONdpuV08fPOG2V24aY+ie0TO538uK0d5142gmZPq+TyZy48c6yADTktZJlAWgAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKgAAAAAqACgIAAAAAAAAAAAAKAAAAAAAAAAKACCKgAAAAAAAoAjIAQAAAAAAAAAABR//9k=").into(backIV);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastO.getJSONArray("hour");
                    for (int i=0; i <hourArray.length(); i++) {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherRVModelArrayList.add(new WeatherRVModel(time, temper, img, wind));

                    }
                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please Enter valid city name...", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}