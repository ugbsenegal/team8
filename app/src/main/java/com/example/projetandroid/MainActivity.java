package com.example.projetandroid;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.DataFormatException;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_LOCATION = 0;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tmpmin;
    private TextView tmpmax;
    private TextView temperature;

    private TextView date;

    private TextView[] tvjours;
    private TextView[] tvmin;
    private TextView[] tvmax;
    private Button bdetails;

    private Button bprieres;

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        date = (TextView)findViewById(R.id.date);
        date.setText(getCurrentDate());

        tvjours = new TextView[5];
        tvmin = new TextView[5];
        tvmax = new TextView[5];

        tvjours[0] = (TextView) findViewById(R.id.jour1);
        tvjours[1] = (TextView) findViewById(R.id.jour2);
        tvjours[2] = (TextView) findViewById(R.id.jour3);
        tvjours[3] = (TextView) findViewById(R.id.jour4);
        tvjours[4] = (TextView) findViewById(R.id.jour5);

        tvmin[0] = (TextView) findViewById(R.id.minjour1);
        tvmin[1] = (TextView) findViewById(R.id.minjour2);
        tvmin[2] = (TextView) findViewById(R.id.minjour3);
        tvmin[3] = (TextView) findViewById(R.id.minjour4);
        tvmin[4] = (TextView) findViewById(R.id.minjour5);

        tvmax = new TextView[5];
        tvmax[0] = (TextView) findViewById(R.id.maxjour1);
        tvmax[1] = (TextView) findViewById(R.id.maxjour2);
        tvmax[2] = (TextView) findViewById(R.id.maxjour3);
        tvmax[3] = (TextView) findViewById(R.id.maxjour4);
        tvmax[4] = (TextView) findViewById(R.id.maxjour5);
        tmpmin = (TextView) findViewById(R.id.tmpmin);
        tmpmax = (TextView) findViewById(R.id.tmpmax);
        temperature = (TextView) findViewById(R.id.temperature);

        bdetails = (Button) findViewById(R.id.details);

        bprieres = (Button) findViewById(R.id.prieres);

        bdetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Details.class);
                startActivity(i);
            }
        });

        bprieres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, HeuresPrieres.class);
                startActivity(i);
            }
        });


        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(MainActivity.this, "Texte", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_LOCATION);
        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "",
                                        "Loading. Please wait...", true);
                                new GetLocation().execute(location);
                                new MeteoDuJour().execute(location);
                                new MeteoDeLaSemaine().execute(location);
                                dialog.dismiss();
                            }
                        }
                    });

        }

    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        if (requestCode == MY_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "",
                                            "Loading. Please wait...", true);
                                    new GetLocation().execute(location);
                                    new MeteoDuJour().execute(location);
                                    new MeteoDeLaSemaine().execute(location);
                                    dialog.dismiss();
                                }
                            }
                        });
            } else {
                Toast.makeText(this, "Texte", Toast.LENGTH_SHORT).show();
            }
        }

        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

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

    class MeteoDuJour extends AsyncTask<Location, Void, JSONObject> {

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


        @Override
        protected void onPostExecute(JSONObject obj) {
            try{
                JSONObject mainObj = obj.getJSONObject("main");
                tmpmax.setText(mainObj.getString("temp_max") + "\u00B0");
                tmpmin.setText(mainObj.getString("temp_min") + "\u00B0");
                temperature.setText(mainObj.getString("temp") + "\u2103");
            }
            catch (JSONException e1) {
                e1.printStackTrace();

            }

        }
    }



    private class MeteoDeLaSemaine extends AsyncTask<Location, Void, JSONObject> {
        public String theDay(int day){
            String[] dayNames = {"Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    	    return dayNames[day];
        }
        public float min(float[] tab){
            float val = tab[0];
            for(int i = 1; i < tab.length; i++)
                if(tab[i] < val)
                    val = tab[i];
            return  val;
        }

        public float max(float[] tab){
            float val = tab[0];
            for(int i = 1; i < tab.length; i++)
                if(tab[i] > val)
                    val = tab[i];
            return val;
        }

        public int jourDuMois(){
            Calendar calendar = Calendar.getInstance();
            return calendar.get(Calendar.DATE);
        }

        int getDayWeek(String date){
            SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd");
            Date d = null;
            try {
                d = form.parse(date);
            }catch (ParseException e){
                e.printStackTrace();

            }

            Calendar c = Calendar.getInstance();
            c.setTime(d);
            return c.get(Calendar.DAY_OF_WEEK);
        }

        int getDayFromDate(String date) throws ParseException {
            SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd");
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
        protected JSONObject doInBackground(Location... locations) {
            String api_key = "236ce1ea6c02a37a1aafff92045314e6";
            String base_url = "http://api.openweathermap.org/data/2.5/forecast?mode=json&units=metric&lat=";
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


        @Override
        protected void onPostExecute(JSONObject obj) {
            float[] tabMin = new float[8];
            float[] tabMax = new float[8];
            int jourActuel = jourDuMois();
            int n = 0;
            int k = 0;

            try{
                JSONArray liste = obj.getJSONArray("list");
                String date="";
                int jour = 0;
                for (int i = 0; i < liste.length(); i++){
                    date = liste.getJSONObject(i).getString("dt_txt");
                    try {
                        jour = getDayFromDate(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if(jourActuel == jour)
                        continue;
                    tabMin[n] = Float.parseFloat(liste.getJSONObject(i).getJSONObject("main").getString("temp_min"));
                    tabMax[n] = Float.parseFloat(liste.getJSONObject(i).getJSONObject("main").getString("temp_max"));
                    if(n == 7){
                        tvjours[k].setText(theDay(getDayWeek(date) - 1));
                        tvmin[k].setText(Float.toString(min(tabMin))  + "\u00B0");
                        tvmax[k].setText(Float.toString(max(tabMax))  + "\u00B0");
                        n = 0;
                        k++;
                    }
                    n++;

                }
            }
            catch (JSONException e1) {
                e1.printStackTrace();

            }

        }
    }
}
