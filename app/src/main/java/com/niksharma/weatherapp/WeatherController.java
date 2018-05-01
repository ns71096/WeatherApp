package com.niksharma.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class WeatherController extends AppCompatActivity {

    // Constants:
    AppCompatActivity context;

    final int REQUEST_CODE = 123;
    private int noRequest = 1;

    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "d1666b66f7d67f7c9286926eaa7150b7";


    // TODO: Set LOCATION_PROVIDER here:


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager manager;
    LocationListener listner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        this.context = this;

        // Linking the elements in the layout to Java code
        mCityLabel =  findViewById(R.id.locationTV);
        mWeatherImage =  findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel =  findViewById(R.id.tempTV);
        ImageButton changeCityButton =  findViewById(R.id.changeCityButton);

        // TODO: Add an OnClickListener to the changeCityButton here:

        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(WeatherController.this,ChangeCityController.class);
                startActivity(intent);
            }
        });



    }


    // TODO: Add onResume() here:

    @Override
    protected void onResume() {
        super.onResume();
        Intent recievedIntent=getIntent();
        String city=recievedIntent.getStringExtra("city");
        if(city!=null)
        {
            getWeatherForNewCity(city);
        }else {
            getWeatherForCurrentLocation();
        }
    }


    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForNewCity(String city)
    {
        RequestParams params=new RequestParams();
        params.put("q",city);
        params.put("appid",APP_ID);
        letsDoSomeNetworking(params);

    }


    // TODO: Add getWeatherForCurrentLocation() here:

    private void getWeatherForCurrentLocation() {
        Log.i("info", "getWeatherForCurrentLocation() called");
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String lattitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());
                Log.i("info", "onLocationChanged() called");
                Log.i("info", "lat :" + lattitude + " lon :" + longitude);

                RequestParams params = new RequestParams();
                params.put("lat", lattitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);

                letsDoSomeNetworking(params);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.i("info", "onStatusChanged() called");
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.i("info", "onProviderEnabled() called");
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.i("info", "onProviderDisabled() called");

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (noRequest == 1) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                noRequest++;
            }
            return;
        }

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1000, listner);
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            String lattitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());
            Log.i("info", "onLocationChanged() called");
            Log.i("info", "lat :" + lattitude + " lon :" + longitude);

            RequestParams params = new RequestParams();
            params.put("lat", lattitude);
            params.put("lon", longitude);
            params.put("appid", APP_ID);

            letsDoSomeNetworking(params);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("info", "permission granted");
                getWeatherForCurrentLocation();
            } else {
                Log.i("info", "permission not granted");
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Sorry");
                alert.setMessage("Permission not granted.");
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        noRequest = 1;
                        finish();

                    }
                });
                alert.setCancelable(false);
                alert.show();

            }

        }

    }

    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private void letsDoSomeNetworking(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("info", "Success :" + response.toString());

                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                if(weatherData!=null)
                updateUI(weatherData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(context, "Request Failed", Toast.LENGTH_LONG).show();
                getWeatherForCurrentLocation();
            }
        });

    }


    // TODO: Add updateUI() here:
    private void updateUI(WeatherDataModel weather) {
        mTemperatureLabel.setText(weather.getmTemprature());
        mCityLabel.setText(weather.getmCity());
        int res = getResources().getIdentifier(weather.getmIconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(res);
    }


    // TODO: Add onPause() here:
    @Override
    protected void onPause() {
        super.onPause();
        if (manager != null) {
            manager.removeUpdates(listner);
        }

    }

}
