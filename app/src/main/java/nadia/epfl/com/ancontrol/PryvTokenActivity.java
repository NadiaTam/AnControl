package nadia.epfl.com.ancontrol;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.database.DBinitCallback;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PryvTokenActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView eventList;
    public MyPryvAdapter myPryvAdapter;
    private ArrayList<Patients> patients_list;
    private ArrayList<Patients> events;
    private Button getToken;
    private Intent intentFromPryvLogin;
    private int numberOfPatients;

    static final String TAG = "PryvTokenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pryv_token);

        eventList = (RecyclerView) findViewById(R.id.RecyclerView_token);
        getToken = (Button) findViewById(R.id.button_getToken);

        events = new ArrayList<>();
        intentFromPryvLogin = getIntent();
        numberOfPatients = intentFromPryvLogin.getIntExtra("NUMBER_OF_PATIENTS", 1);
        patients_list = intentFromPryvLogin.getParcelableArrayListExtra("INFOPATIENT");

        Log.d(TAG, "PRYV_TOKEN_ACTIVITY" + " " + "patients: " + patients_list.size() + " " + "name: " + patients_list.get(0).getName() + " " + "surname: " + patients_list.get(0).getSurname() +
                " " + "number: " + patients_list.get(0).getNumber() + " " + "token: " + patients_list.get(0).getToken() + " " + "username: " + patients_list.get(0).getUsername());

        myPryvAdapter = new MyPryvAdapter(this, events, patients_list);
        eventList.setAdapter(myPryvAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        eventList.setLayoutManager(layoutManager);
        eventList.setHasFixedSize(true);

        getToken.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case(R.id.button_getToken):
                retrieveSharings();
                break;
        }
    }

    private void retrieveSharings() {

        Connection connection = new Connection(PryvTokenActivity.this, Credentials.getUsername(), Credentials.getToken(), PryvLoginActivity.DOMAIN, false, new DBinitCallback());
        Stream patientsStream = new Stream("patients", "patients");
        Filter filter = new Filter();
        filter.addStream(patientsStream);
        filter.setLimit(10);

        connection.events.get(filter, new GetEventsCallback() {
            @Override
            public void cacheCallback(final List<Event> list, Map<String, Double> map) {

                PryvTokenActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        for (Event event : list) {
                            String content = event.getContent().toString();
                            try {
                                JSONObject jsonEvent = new JSONObject(content);
                                String username = jsonEvent.getString("username");
                                String token = jsonEvent.getString("token");
                                //sharings += username + " : " + token;

                                Log.d(TAG, "username: " + username + " " + "token" +token);
                                //MyEvents eventDownloaded = new MyEvents(username, token);

                                events.add(new Patients(username, token));
                                Log.d(TAG, "events size cache" + events.size());
                                myPryvAdapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                //sharings += "Badly formatted sharing";
                            }
                        }
                    }
                });
                //eventRetrieved = updateSharingsList(list);
                Log.i("Pryv", list.size() + " sharings retrieved from cache.");
            }

            @Override
            public void onCacheError(String s) {
                //updateStatusText(s);
                Log.e("Pryv", s);
            }

            @Override
            public void apiCallback(final List<Event> list, Map<String, Double> map, Double aDouble) {
                //updateSharingsList(list);
                PryvTokenActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        for (Event event : list) {
                            String content = event.getContent().toString();
                            try {
                                JSONObject jsonEvent = new JSONObject(content);
                                String username = jsonEvent.getString("username");
                                String token = jsonEvent.getString("token");
                                //sharings += username + " : " + token;

                                Log.d(TAG, "username: " + username + " " + "token" +token);
                                events.add(new Patients(username, token));
                                Log.d(TAG, "events size api" + events.size());
                                myPryvAdapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                //sharings += "Badly formatted sharing";
                            }
                        }
                    }
                });
                Log.i("Pryv", list.size() + " sharings retrieved from API.");
            }

            @Override
            public void onApiError(String s, Double aDouble) {
                //updateStatusText(s);
                Log.e("Pryv", s);
            }
        });
    }
}

