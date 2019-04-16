package com.example.projetandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tmpmin;
    private TextView tmpmax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("");
        tmpmin = (TextView) findViewById(R.id.tmpmin);
        tmpmax = (TextView) findViewById(R.id.tmpmax);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            new GetLocation().execute(location);

                        }
                    }
                });
    }

    private class GetLocation extends AsyncTask<Location, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Location... locations) {

            String api_key = "google_api_key";
            String base_url =
                    "https://maps.googleapis.com/maps/api/geocode/json?language=fr&latlng=";
            base_url = base_url + Double.toString(locations[0].getLatitude()) + "," +
            Double.toString(locations[0].getLongitude()) + "&key=" + api_key;
            URLConnection urlConn = null;
            BufferedReader bufferedReader = null;
            try
            {
                URL url = new URL(base_url);
                urlConn = url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuffer.append(line);
                }

                return new JSONObject(stringBuffer.toString());
            }
            catch(Exception ex)
            {
                Log.e("App", "yourDataTask", ex);
                return null;
            }
            finally
            {
                if(bufferedReader != null)
                {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onPostExecute(JSONObject obj) {
            JSONObject location;
            String location_string = "Meteo App";
            String pays = "";
            String ville = "";
            try {
                //Get JSON Array called "results" and then get the 0th complete object as JSON
                //location = obj.getJSONArray("results").getJSONObject(0);
                // Get the value of the attribute whose name is "formatted_string"
                //location_string = location.getString("formatted_address");
                 JSONArray arr = obj.getJSONArray("results").getJSONObject(0).getJSONArray("address_components");
                for (int i = 0; i < arr.length(); i++){
                    JSONArray tmp = arr.getJSONObject(i).getJSONArray("types");
                    if(tmp.getString(0).equals("country")){
                        pays = arr.getJSONObject(i).getString("long_name");
                    }

                    if(tmp.getString(0).equals("administrative_area_level_1")){
                        ville = arr.getJSONObject(i).getString("long_name");
                    }

                }
                Log.d("test", "formattted address:" + location_string);
            } catch (JSONException e1) {
                e1.printStackTrace();

            }
            location_string = ville + ", " + pays;
            setTitle(location_string);
        }
    }
}
