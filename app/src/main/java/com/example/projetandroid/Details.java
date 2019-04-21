package com.example.projetandroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Details extends AppCompatActivity {

    private TextView tmpmin;
    private TextView tmpmax;
    private TextView temperature;
    private TextView tvlat;
    private TextView tvlon;
    private TextView tvLS;
    private TextView tvCS;
    private  TextView tvvent;
    private TextView tvhum;
    private Button bdetails;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView date;

    public static String getCurrentDate(){
        String[] mois = {"Janvier", "Fevrier","Mars","Avril","Main","Juin","Juillet","Aout","Septembre",
                "Octobre","Novembre","Decembre"};
        String[] jour = {"Dimanche","Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi"};

        Calendar c = Calendar.getInstance();
        int i = c.get(Calendar.MONTH);
        int j = c.get(Calendar.DAY_OF_WEEK);
        int k = c.get(Calendar.YEAR);
        int n = c.get(Calendar.DAY_OF_MONTH);

        String s1 = Integer.toString(j);
        String s2 = Integer.toString(k);

        String chaine = jour[j-1]+","+n+" "+mois[i]+" "+s2;

        return chaine;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);

        date = (TextView)findViewById(R.id.date);
        date.setText(getCurrentDate());

        tmpmin = (TextView) findViewById(R.id.tmpmin);
        tmpmax = (TextView) findViewById(R.id.tmpmax);
        temperature = (TextView) findViewById(R.id.temperature);
        tvlat = (TextView) findViewById(R.id.valLat);
        tvlon = (TextView) findViewById(R.id.valLon);
        tvLS = (TextView) findViewById(R.id.valLS);
        tvCS = (TextView) findViewById(R.id.valCS);
        tvvent = (TextView) findViewById(R.id.valVent);
        tvhum = (TextView) findViewById(R.id.valHum);
        bdetails = (Button) findViewById(R.id.details);


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
                        if (location != null){
                            new GetLocation().execute(location);
                            new MeteoDuJour().execute(location);
                        }
                    }
                });
        bdetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Details.this, HeuresPrieres.class);
                startActivity(i);
            }
        });
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
                location_string = ville + ", " + pays;
                setTitle(location_string);

                Log.i("ville", ville);
                Log.i("pays", pays);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }
    }

    private class MeteoDuJour extends AsyncTask<Location, Void, JSONObject> {

        //@TargetApi(Build.VERSION_CODES.O)
        //@RequiresApi(api = Build.VERSION_CODES.O)
        public  Date secToLocalDateTime(long sec) throws ParseException {
            Date date = new Date(sec*1000L);
            // format of the date
            SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            jdf.setTimeZone(TimeZone.getTimeZone(String.valueOf(TimeZone.getDefault())));
            String java_date = jdf.format(date);
            Date val_date=null;
            try{
                val_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(java_date);
            }catch (ParseException e){
                e.printStackTrace();
            }
            return  val_date;
        }

        @Override
        protected JSONObject doInBackground(Location... locations) {
            String api_key = "236ce1ea6c02a37a1aafff92045314e6";
            String base_url = "http://api.openweathermap.org/data/2.5/weather?units=metric&lat=";
            base_url = base_url + Double.toString(locations[0].getLatitude()) + "&lon=" +
                    Double.toString(locations[0].getLongitude()) + "&appid=" + api_key;

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


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(JSONObject obj) {
            try{
                JSONObject mainObj = obj.getJSONObject("main");
                tmpmax.setText(mainObj.getString("temp_max") + "\u00B0");
                tmpmin.setText(mainObj.getString("temp_min") + "\u00B0");
                temperature.setText(mainObj.getString("temp") + "\u2103");

                JSONObject coord = obj.getJSONObject("coord");
                tvlat.setText(coord.getString("lat"));
                tvlon.setText(coord.getString("lon"));

                tvhum.setText(mainObj.getString("humidity") + "%");
                tvvent.setText(obj.getJSONObject("wind").getString("speed") + "m/s");

                JSONObject sys = obj.getJSONObject("sys");
                Date d1 = secToLocalDateTime(sys.getLong("sunrise"));
                Date d2 = secToLocalDateTime(sys.getLong("sunset"));
                tvLS.setText(Long.toString(d1.getHours()) + "H" + Long.toString(d1.getMinutes()));
                tvCS.setText(Long.toString(d2.getHours()) + "H" + Long.toString(d2.getMinutes()));

            }
            catch (JSONException e1) {
                e1.printStackTrace();

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }
}
