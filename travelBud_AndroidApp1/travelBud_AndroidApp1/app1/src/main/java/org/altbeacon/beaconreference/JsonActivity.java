package org.altbeacon.beaconreference;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kishaku on 31/05/2015.
 */
public class JsonActivity extends ListActivity {
    private ProgressDialog pDialog;

    // URL to get contacts JSON
    private static String url = "http://172.16.3.178:8888/beacon?uuid=1E9E354C-F44D-4B08-ABBF-40014FE9CC26";

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
    JSONObject uuid=null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> busList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.json_activity);

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
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(JsonActivity.this);

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

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    uuid = jsonObj.getJSONObject(TAG_UUID);

                    //Getting JSON Object node
                    JSONObject safety = uuid.getJSONObject(TAG_SAFETY);
                    JSONObject station = uuid.getJSONObject(TAG_STATION);
                    String station_time = station.getString(TAG_STATION_TIME);

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
                    JsonActivity.this, busList,
                    R.layout.list_item, new String[] { TAG_ROUTE, TAG_DEPARTURE_TIME,
                    TAG_DIRECTION }, new int[] { R.id.name,
                    R.id.email, R.id.mobile });

            setListAdapter(adapter);
        }

    }
}
