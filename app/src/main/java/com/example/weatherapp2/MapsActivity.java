package com.example.weatherapp2;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    final String googleAddress = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    final String googleAPI = "&key=AIzaSyD3DW3j_jVs2xxjIR_zaDP11hgS87Af8Bc";
    final String darkSkyAddress = "https://api.darksky.net/forecast/032506224743818bcc049e15146e5062/";
    final String testURL = "https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=AIzaSyD3DW3j_jVs2xxjIR_zaDP11hgS87Af8Bc";
    private Marker currMarker;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onSubmit(View view) {
       String inpt = returnAddress();
        Log.i("tag", "Passed get address");
        String combined = googleAddress+inpt+googleAPI;
        searchGoogle(combined);
    }

    public String returnAddress(){
        EditText input = (EditText) findViewById(R.id.editText);
        String sendAdd = input.getText().toString();
        sendAdd = sendAdd.replace(" ", "+");
        return sendAdd;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng utAustin = new LatLng(30.23, -97.54);
        currMarker = mMap.addMarker(new MarkerOptions().position(utAustin).title("Marker in UT AUSTIN"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(utAustin));
    }
    public void searchGoogle(String address){

        //  Intent intent = new Intent(this,MapsActivity.class);
        // startActivity(intent);
        final TextView mTextView = (TextView) findViewById(R.id.textView);
// ...

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // String test = "https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=AIzaSyD3DW3j_jVs2xxjIR_zaDP11hgS87Af8Bc";

// Request a string response from the provided URL.
        StringRequest getRequest = new StringRequest(Request.Method.GET, address,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // display response
                        // Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                        getCoords(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
// add it to the RequestQueue
        queue.add(getRequest);
    }

    public void getCoords(String rawCoords){
        int indexLat = rawCoords.lastIndexOf("\"lat\" : ");
        int indexC = rawCoords.indexOf(",", indexLat);
        int indexLng = rawCoords.lastIndexOf("\"lng\" : ");
        int indexNL = rawCoords.indexOf("\n", indexLng);

        String latString = rawCoords.substring(indexLat+8,indexC);
        String lngString = rawCoords.substring(indexLng+8,indexNL);

        double latCoord = Double.parseDouble(latString);
        double lngCoord = Double.parseDouble(lngString);
        moveMap(latCoord,lngCoord);
        searchDarkSky(latString,lngString);
    }

    public void moveMap(double latCoord, double lngCoord){
        LatLng newLocation = new LatLng(latCoord, lngCoord);
        currMarker.remove();
        currMarker = mMap.addMarker(new MarkerOptions().position(newLocation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));

    }


    public void searchDarkSky(String latCoord, String lngCoord){
        String address = darkSkyAddress + latCoord + "," +lngCoord;
        //String test = "https://api.darksky.net/forecast/032506224743818bcc049e15146e5062/29.7243512197085,-95.7820352302915";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest getRequest = new StringRequest(Request.Method.GET, address,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // display response
                        // Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                        getWeather(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
// add it to the RequestQueue
        queue.add(getRequest);
    }

    public void getWeather(String rawWeather){
        int indexTemp = rawWeather.indexOf("\"temperature\":");
        int indexTempEnd = rawWeather.indexOf(",", indexTemp);
        int indexHumid = rawWeather.indexOf("\"humidity\":");
        int indexHumidEnd = rawWeather.indexOf(",", indexHumid);
        int indexWind = rawWeather.indexOf("\"windSpeed\":");
        int indexWindEnd = rawWeather.indexOf(",", indexWind);
        int indexPrecip= rawWeather.indexOf("\"precipProbability\":");
        int indexPrecipEnd = rawWeather.indexOf(",", indexPrecip);

        String tempS = rawWeather.substring(indexTemp+14,indexTempEnd);
        String humidS = rawWeather.substring(indexHumid+11,indexHumidEnd);
        String windS = rawWeather.substring(indexWind+12,indexWindEnd);
        String precipS = rawWeather.substring(indexPrecip+20,indexPrecipEnd);

        double temp = Double.parseDouble(tempS);
        double humid = Double.parseDouble(humidS);
        double wind = Double.parseDouble(windS);
        double precip = Double.parseDouble(precipS);

        String typeS = "none";
        if(precip>0){
            int indexType = rawWeather.indexOf("\"precipType\":");
            int indexTypeEnd = rawWeather.indexOf(",", indexType);

            typeS = rawWeather.substring(indexType+14,indexTypeEnd-1);

           // Double type = Double.parseDouble(typeS);
        }
    }
}
