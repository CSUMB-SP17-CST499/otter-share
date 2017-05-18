package com.ottershare.ottershare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainTask extends AsyncTask<String, String, Integer> {
    private final String LOG_TAG = MainTask.class.getSimpleName();

    /* Context being passed in from main thread*/
    Context mContext;
    Activity prevActivity;

    /* SharedPreferences to be modified */
    SharedPreferences prefs;

    /* Information needed from server response */
    private MapOSFragment frag;

    ArrayList<ParkingPassInfo> parkingPassInfoArray;
    ArrayList<LatLng> locations;

    PassAdapter passAdapter;
    ListView passList;
    HashMap<Integer, Integer> lotMap;

    public MainTask(Activity activity,MapOSFragment frag) {
        mContext = activity.getApplicationContext();
        prevActivity = activity;
        locations = new ArrayList<LatLng>();
        this.frag = frag;
    }

    @Override
    protected Integer doInBackground(String... params) {
        HttpURLConnection httpURLConnection = null;
        String response = "";

        String api_key = params[0];
        String keyword = params[1];

        try {
            final String BASE_URL = "https://young-plains-98404.herokuapp.com/";
            final String CALL = "activeUsers";
            final String API_KEY_PARAM = "api_key";
            final String KEYWORD_PARAM = "keyword";

            URL url = new URL(BASE_URL + CALL);

            //set up http connection
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            HashMap<String, String> postDataParams = new HashMap<>();

            postDataParams.put(API_KEY_PARAM, api_key);
            postDataParams.put(KEYWORD_PARAM, keyword);

            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            int responseCode = httpURLConnection.getResponseCode();


            Log.d(LOG_TAG, "Response code: " + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                int index = 0;
                while ((line = br.readLine()) != null) {
                    response += line;
                    Log.d(LOG_TAG, response);
                }
            } else {
                /*
                * TODO: find out how to test this for when there really is a bad response code
                * Perhaps test the next step where the result is returned to the Switch in onPostExecute...
                */
                return -1;
            }

            //Put data into a json object format
            JSONObject jsonResponseObject = new JSONObject(response);
	    JSONArray jsonResponse = null;
            if (jsonResponseObject.has("success")) {
                jsonResponse = jsonResponseObject.getJSONArray("success");
            } else if (jsonResponseObject.has("error")) {
                return 4;
            }

            //todo : change if statement to eventually distinguish sucsess and failures.
            if(true) {
                parkingPassInfoArray = new ArrayList<ParkingPassInfo>();
                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject parkingPass = jsonResponse.getJSONObject(i);

                    String gpsLocationString = parkingPass.getString("gpsLocation");

                    //making sure that the latlong for the passes are in the correct format.
                    if ((gpsLocationString.length() - gpsLocationString.replace(",", "").length()) == 1) {
                        String[] gpsLocationStringSplit = gpsLocationString.split(",");
                        try {

                            String id = parkingPass.getString("id");
                            double lat = Double.parseDouble(gpsLocationStringSplit[0]);
                            double lon = Double.parseDouble(gpsLocationStringSplit[1]);
                            LatLng gpsLocation = new LatLng(lat, lon);
                            String notes = parkingPass.getString("notes");
                            Boolean forSale = Boolean.valueOf(parkingPass.getString("forSale"));
                            float price = (float) parkingPass.getDouble("price");
                            int lotLocation = parkingPass.getInt("lotLocation");
                            String email = parkingPass.getString("ownerEmail");
                            float rating = (float) parkingPass.getDouble("avgRating");
                            parkingPassInfoArray.add(new ParkingPassInfo(id, gpsLocation, notes, forSale, price, lotLocation, email , rating));
                            locations.add(gpsLocation);

                        } catch(NumberFormatException e) {
                    }
                }else{
                        Log.i(LOG_TAG, "not a latlon format");
                    }

                }

            }else{
                Log.i(LOG_TAG, "json not successful");
            }

            Log.d(LOG_TAG, api_key);
            /*if (jsonResponse.has("error")) {
                status = getErrorStatus(jsonResponse.getString("error"));
            } else {
                getLoginDataFromJson(jsonResponse);
                status = 2;
            }*/

            //Log.d(LOG_TAG, "\"name\" --> " + name);
            //Log.d(LOG_TAG, "\"email\" --> " + email);
            //Log.d(LOG_TAG, "\"Retrieved user api_key\" --> " + api_key);
            return 2;

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) // Make sure the connection is not null.
                httpURLConnection.disconnect();
        }

        return 3;
    }

    //assuming response 200...
    // 0 -> hasn't verified account
    // 1 -> incorrect email and password combination
    // 2 -> success
    // -1 || any other value -> response code was not ok "200"
    protected void onPostExecute(Integer result) {
        switch (result) {
            case (0):
                Log.d(LOG_TAG, "case = 0: " + result);
                break;
            case (1):
                Log.d(LOG_TAG, "case = 1: " + result);
                break;
            case (2):
                Log.d(LOG_TAG, "case = 2: " + result);
                break;
            case (3):

                parkingPassInfoArray = testData();
                populateBottomPannel();
                populateTopPannel();
                break;
            default:
                Log.d(LOG_TAG, "case = default" + " actual: " + result);
                makeToast(R.string.login_toast_fatal_error, Toast.LENGTH_SHORT);
        }

    }

    protected void onProgressUpdate(String... progress) {
        //probably wont use, but maybe way later...

    }

    //put the data from hash map into POST format "this=this&this=that" since post data has be sent via a string
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }


    private void makeToast(int message, int length) {
        Toast.makeText(mContext, message, length).show();
    }

    private void populateBottomPannel(){
        passAdapter = new PassAdapter(prevActivity,parkingPassInfoArray);
        passList = (ListView) prevActivity.findViewById(R.id.pass_list);
        passList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent nextIntent = new Intent(prevActivity,PassView.class);
                ParkingPassInfo currentpass = parkingPassInfoArray.get(position);
                Bundle b = new Bundle();
                b.putParcelable("pass", currentpass);
                nextIntent.putExtras(b);
                prevActivity.startActivity(nextIntent);
            }
        });
        passList.setAdapter(passAdapter);
        passAdapter.add(parkingPassInfoArray.get(0));
        frag.addHeatMap(locations);

    }

    private void populateTopPannel(){

        //For top part of the list view
        lotMap = new HashMap<>();
        for (int i = 0; i < parkingPassInfoArray.size(); ++i) {
            int currentLot = parkingPassInfoArray.get(i).getLotLocation();
            if (lotMap.containsKey(currentLot)) {
                lotMap.put(currentLot, lotMap.get(currentLot) + 1);
            } else {
                lotMap.put(currentLot, 1);
            }
        }

        //iterate through lotMap keys
        Iterator it = lotMap.entrySet().iterator();
        final ArrayList<String> filterList = new ArrayList<>();
        ArrayList<Integer> filtersListLots = new ArrayList<>();

        filterList.add("Filter by all (" + parkingPassInfoArray.size() + ((parkingPassInfoArray.size() == 1) ? " pass" : " passes") + " in " + lotMap.size() + ((lotMap.size() == 1) ? " lot" : " lots") + ")");
        filtersListLots.add(9999); //filter for all is lot #9999
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            filterList.add("Lot #" + pair.getKey() + ": " + pair.getValue() + ((pair.getValue().toString().equals("1")) ? " pass" : " passes"));
            filtersListLots.add(Integer.parseInt(pair.getKey().toString()));
            it.remove(); // avoids a ConcurrentModificationException
        }

        showFiltersList(filterList, filtersListLots);
    }




    // to set up dummy data remove when needed.
    private ArrayList<ParkingPassInfo> testData(){
        ArrayList<ParkingPassInfo> returnData = new ArrayList<>();
        returnData.add(new ParkingPassInfo("bbb",new LatLng(36.652348, -121.798682),"",true,(float)1.00,12,"bchehraz@csumb.edu",(float) .5));
        returnData.add(new ParkingPassInfo("ccc",new LatLng(36.651795, -121.800519),"",true,(float)2.00,19,"montey@csumb.edu",(float) 1.0));
        returnData.add(new ParkingPassInfo("ddd",new LatLng(36.652477, -121.800111),"",true,(float)3.00,19,"ott21@csumb.edu",(float) 1.2));
        returnData.add(new ParkingPassInfo("eee",new LatLng(36.652129, -121.804482),"",true,(float)5.00,19,"mlopez@csumb.edu",(float) 2.0));
        returnData.add(new ParkingPassInfo("aaa",new LatLng(36.652324, -121.798293),"",true,(float)4.00,508,"rgrab@csumb.edu",(float) 2.5));
        returnData.add(new ParkingPassInfo("bbb",new LatLng(36.652348, -121.798682),"",true,(float)1.00,508,"bbob@csumb.edu",(float) 3.0));
        returnData.add(new ParkingPassInfo("ccc",new LatLng(36.651795, -121.800519),"",true,(float)2.00,508,"abcd@csumb.edu",(float) 3.5));
        returnData.add(new ParkingPassInfo("ddd",new LatLng(36.652477, -121.800111),"",true,(float)3.00,12,"brett@csumb.edu",(float) 4.0));
        returnData.add(new ParkingPassInfo("eee",new LatLng(36.652129, -121.804482),"",true,(float)5.00,508,"montedean@csumb.edu",(float) 4.5));
        returnData.add(new ParkingPassInfo("aaa",new LatLng(36.652324, -121.798293),"",true,(float)4.00,508,"monte@csumb.edu",(float) 5.0));
        returnData.add(new ParkingPassInfo("bbb",new LatLng(36.652348, -121.798682),"",true,(float)1.00,508,"otterxp@csumb.edu",(float) 1.25));
        returnData.add(new ParkingPassInfo("ccc",new LatLng(36.651795, -121.800519),"",true,(float)2.00,19,"jimdean@csumb.edu",(float) 1.75));
        returnData.add(new ParkingPassInfo("ddd",new LatLng(36.652477, -121.800111),"",true,(float)3.00,508,"defg@csumb.edu",(float) 2.5));
        returnData.add(new ParkingPassInfo("eee",new LatLng(36.652129, -121.804482),"",true,(float)5.00,508,"abcd@csumb.edu",(float) 3.5));
        locations.add(new LatLng(36.652324, -121.798293));
        locations.add(new LatLng(36.652348, -121.798682));
        locations.add(new LatLng(36.651795, -121.800519));
        locations.add(new LatLng(36.652477, -121.800111));
        locations.add(new LatLng(36.652129, -121.804482));
        return returnData;
    }

    public void showFiltersList(ArrayList<String> filtersList, final ArrayList<Integer> filtersListLots) {
        final ListView lotFilterList = (ListView) prevActivity.findViewById(R.id.top_pannel_filters);

        FilterAdapter adapter = new FilterAdapter(mContext, filtersList);
        lotFilterList.setAdapter(adapter);
        lotFilterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //do something here about filtering the list.
                int currentFilter = filtersListLots.get(position);
                ArrayList<ParkingPassInfo> list = new ArrayList<>();
                for (int i = 0; i < parkingPassInfoArray.size(); ++i) {
                    if (parkingPassInfoArray.get(i).getLotLocation() == currentFilter) {
                        list.add(parkingPassInfoArray.get(i));
                    } else if (currentFilter == 9999) {
                        list = parkingPassInfoArray;
                        break;
                    }
                }
                passAdapter = new PassAdapter(prevActivity, list);
                passList.setAdapter(passAdapter);
            }
        });
    }
}
