package org.altbeacon.beaconreference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.altbeacon.beacon.AltBeacon;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RangingActivity extends ListActivity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    EditText rangingText;
    TextToSpeech t1;


    private ProgressDialog pDialog;

    // URL to get contacts JSON
    private static String url = "http://172.16.3.179:8888/beacon?uuid=1E9E354C-F44D-4B08-ABBF-40014FE9CC26";

    // JSON Node names
    private static final String TAG_UUID = "uuid";
    private static final String TAG_SAFETY = "safety";
    private static final String TAG_STATION = "station";
    private static final String TAG_DEPARTURE_TIME = "time";
    private static final String TAG_STATION_TIME = "time";
    private static final String TAG_DEPARTURES = "departures";
    private static final String TAG_ROUTE = "route";
    private static final String TAG_DIRECTION = "direction";
    private static final String TAG_PHONE = "phone";
    private static final String TAG_PHONE_MOBILE = "mobile";
    private static final String TAG_PHONE_HOME = "home";
    private static final String TAG_PHONE_OFFICE = "office";

    // contacts JSONArray
    JSONArray departures = null;
    JSONObject uuid = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> busList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.json_activity);
        ///   rangingText =(EditText)findViewById(R.id.rangingText);

        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }


    //speak the user text
    private void speakWords(String speech) {
        //speak straight away
        t1.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                  //  EditText editText = (EditText) RangingActivity.this
                       //     .findViewById(R.id.rangingText);
                    Beacon firstBeacon = beacons.iterator().next();
                    if (firstBeacon != null) {
                        onPause();
                        Log.e("error", "Starting json");
                     //   logToDisplay("The first beacon " + firstBeacon.toString() + "UUID is" + firstBeacon.getId1() + " is about " + firstBeacon.getDistance() + " meters away.");
                        busList = new ArrayList<HashMap<String, String>>();

                        ListView lv = getListView();

                        // Listview on item click listener
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                // getting values from selected ListItem
                                String name = ((TextView) view.findViewById(R.id.name))
                                        .getText().toString();
                                String cost = ((TextView) view.findViewById(R.id.email))
                                        .getText().toString();
                                String description = ((TextView) view.findViewById(R.id.mobile))
                                        .getText().toString();

                                // Starting single contact activity
                                Intent in = new Intent(getApplicationContext(),
                                        SingleJsonActivity.class);
                                in.putExtra(TAG_SAFETY, name);
                                // in.putExtra(TAG_EMAIL, cost);
                                in.putExtra(TAG_PHONE_MOBILE, description);
                                startActivity(in);

                            }
                        });

                        // Calling async task to get json
                        new GetData().execute();

                  /*  rangingText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            logToDisplay("Click");
                            String toSpeak = rangingText.getText().toString();
                            speakWords(toSpeak);
                            //t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                            Intent myIntent = new Intent(getApplicationContext(),RangingActivity.class);
                            startActivity(myIntent);
                            // this.startActivity(myIntent);
                        }
                    });

                    t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if(status != TextToSpeech.ERROR) {
                                t1.setLanguage(Locale.UK);
                            }
                        }
                    }); */


                        //  logToDisplay("The first beacon "+firstBeacon.toString()+"UUID is"+firstBeacon.getId1()+ " is about "+firstBeacon.getDistance()+" meters away.");
                        // Intent myIntent = new Intent(getApplicationContext(),JsonActivity.class);
                        // startActivity(myIntent);
                    }
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText) RangingActivity.this
                        .findViewById(R.id.rangingText);
                editText.append(line);
            }
        });
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(RangingActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance

            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    uuid = jsonObj.getJSONObject(TAG_UUID);

                    //Getting JSON Object node
                    JSONObject safety = uuid.getJSONObject(TAG_SAFETY);
                    Log.e("Safety: ", "> " + jsonStr);
                    JSONObject station = uuid.getJSONObject(TAG_STATION);
                    Log.e("Station: ", "> " + jsonStr);
                    String station_time = station.getString(TAG_STATION_TIME);
                    Log.e("Station_time: ", "> " + jsonStr);
                    // Getting JSON Array node
                    departures = station.getJSONArray(TAG_DEPARTURES);

                    // looping through All Contacts
                    for (int i = 0; i < departures.length(); i++) {
                        JSONObject c = departures.getJSONObject(i);

                        String route = c.getString(TAG_ROUTE);
                        String departure_time = c.getString(TAG_DEPARTURE_TIME);
                        String direction = c.getString(TAG_DIRECTION);
                        // String address = c.getString(TAG_ADDRESS);
                        // String gender = c.getString(TAG_GENDER);

                        // Phone node is JSON Object


                        //  String home = phone.getString(TAG_PHONE_HOME);
                        //  String office = phone.getString(TAG_PHONE_OFFICE);

                        // tmp hashmap for single contact
                        HashMap<String, String> departure = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        departure.put(TAG_ROUTE, route);
                        departure.put(TAG_DEPARTURE_TIME, departure_time);
                        departure.put(TAG_DIRECTION, direction);


                        // adding contact to contact list
                        busList.add(departure);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    RangingActivity.this, busList,
                    R.layout.list_item, new String[]{TAG_ROUTE, TAG_DEPARTURE_TIME,
                    TAG_DIRECTION}, new int[]{R.id.name,
                    R.id.email, R.id.mobile});

            setListAdapter(adapter);
        }

    }

}
