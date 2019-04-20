package com.example.projetandroid;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
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
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HeuresPrieres extends AppCompatActivity {

    private TextView hFajr;
    private TextView hDhur;
    private TextView hAsr;
    private TextView hMaghrib;
    private TextView hIsha;
    private FusedLocationProviderClient fusedLocationClient;
    private String ville;
    private String pays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heures_prieres);
        hFajr = (TextView) findViewById(R.id.fadjr);
        hDhur = (TextView) findViewById(R.id.dhur);
        hAsr = (TextView) findViewById(R.id.asr);
        hMaghrib = (TextView) findViewById(R.id.maghrib);
        hIsha = (TextView) findViewById(R.id.isha);


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
                .addOnSuccessListener(HeuresPrieres.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            new GetLocation().execute(location);

                        }
                    }
                });
    }

    void runPrieres(){
        new Prieres().execute();
    }

    private class GetLocation extends AsyncTask<Location, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Location... locations) {

            String api_key = "AIzaSyB_3yekQeEAm1P4lgvovt_iBordFuBr_qk";
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
            //String pays = "";
            //String ville = "";
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
                location_string = ville + ", " + pays;
                setTitle(location_string);
                runPrieres();
                Log.i("ville", ville);
                Log.i("pays", pays);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }


    }


    class Prieres extends AsyncTask<Void, Void, JSONObject> {

        public int jourDuMois(){
            Calendar calendar = Calendar.getInstance();
            return calendar.get(Calendar.DATE);
        }


        int getDayFromDate(String date) throws ParseException {
            SimpleDateFormat form = new SimpleDateFormat("dd-MM-yyyy");
            Date d = null;
            try {
                d = form.parse(date);
            }catch (ParseException e){
                e.printStackTrace();

            }

            Calendar c = Calendar.getInstance();
            c.setTime(d);
            return c.get(Calendar.DAY_OF_MONTH);
        }


        @Override
        protected JSONObject doInBackground(Void... voids) {
            String base_url = "http://api.aladhan.com/v1/calendarByCity?city=";
            base_url = base_url + ville + "&country=" + pays + "&method=2&month=" +
                    Integer.toString(getCurrentMonth()+1)
                    +  "&year=" + Integer.toString(getCurrentYear());
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

        private int getCurrentYear() {
            Calendar calendar = Calendar.getInstance();
            return calendar.get(Calendar.YEAR);
        }

        private int getCurrentMonth() {
            Calendar calendar = Calendar.getInstance();
            return calendar.get(Calendar.MONTH);
        }

        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onPostExecute(JSONObject obj) {
            try{
                String date = "";
                int jour = 0;
                JSONArray data = obj.getJSONArray("data");
                int jourActuelle = jourDuMois();
                for(int i = 0; i < data.length(); i++){
                    date = data.getJSONObject(i).getJSONObject("date").getJSONObject("gregorian").
                            getString("date");
                    try {
                        jour = getDayFromDate(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(jour != jourActuelle)
                        continue;
                    JSONObject timings = data.getJSONObject(i).getJSONObject("timings");
                    hFajr.setText(timings.getString("Fajr").split(" ")[0].replace(":", "H"));
                    hDhur.setText(timings.getString("Dhuhr").split(" ")[0].replace(":", "H"));
                    hAsr.setText(timings.getString("Asr").split(" ")[0].replace(":", "H"));
                    hMaghrib.setText(timings.getString("Maghrib").split(" ")[0].replace(":", "H"));
                    hIsha.setText(timings.getString("Isha").split(" ")[0].replace(":", "H"));
                }
            }
            catch (JSONException e1) {
                e1.printStackTrace();

            }

        }
    }



}
