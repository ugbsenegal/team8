package com.example.projetandroid;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

class VillePays{
    public  static String VILLE ;
    public  static String PAYS;

    public   class GeoLocation extends AsyncTask<Location, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Location... locations) {


            String api_key = "geocoding_api_key";
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
            try {
                JSONArray arr = obj.getJSONArray("results").getJSONObject(0).getJSONArray("address_components");
                for (int i = 0; i < arr.length(); i++){
                    JSONArray tmp = arr.getJSONObject(i).getJSONArray("types");
                    if(tmp.getString(0).equals("country")){
                        PAYS = arr.getJSONObject(i).getString("long_name");
                    }

                    if(tmp.getString(0).equals("administrative_area_level_1")){
                        VILLE = arr.getJSONObject(i).getString("long_name");
                    }

                }
                Log.d("test", "formattted address:");
            } catch (JSONException e1) {
                e1.printStackTrace();

            }
        }
    }
}


