package nadia.epfl.com.ancontrol;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class Pop extends AppCompatActivity implements AdapterView.OnItemClickListener {

    static final int NEW_IP_REQUEST = 1;  // The request code
    static final int RESULT_SUCCESS = 2;
    static final String TAG = "Pop";
    private ArrayList<Device> devices;
    private MyListAdapter myAdapter;
    private ListView ipList;
    private String Name;
    private String IP;
    private int Port;
    private Boolean connectionRequest;
    private int nr_patient;

    //handle multiple Wifi connections
    //ArrayList<Device> CONNECTEDDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);

        ipList = (ListView) findViewById(R.id.listview_Items);
        devices = new ArrayList<>();

        nr_patient = getIntent().getIntExtra("PATIENT", -1);
        Log.d(TAG, "nr_patient: " + nr_patient);

        //adding items in the arrayList
        devices.add(new Device("Raspberry 1", "10.177.86.212", 40000));
        devices.add(new Device("Raspberry 2", "10.177.221.172", 40400));
        devices.add(new Device("Nadia's Phone", "10.177.215.229", 8000));

        if (savedInstanceState != null) {
            devices = savedInstanceState.getParcelableArrayList("Device list");
        }

        myAdapter = new MyListAdapter(this, R.layout.rowlayout, devices);
        ipList.setAdapter(myAdapter);
        ipList.setOnItemClickListener(this);

        //Fixing size of the Popup window
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int heigth = dm.heightPixels;
        getWindow().setLayout((int) (width * .5), (int) (heigth * .5));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_ip:
                startActivityForResult(new Intent(this, AddIpAddress.class), NEW_IP_REQUEST);
                return true;

            default:
                return true;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_SUCCESS) {
            Name = data.getStringExtra("NAME");
            IP = data.getStringExtra("IP");
            Port = data.getIntExtra("PORT",0);
            Log.d("NAME - IP - Port", "Name is " + Name + " " + "IP Address is" + IP + "Port is" + Port);
            devices.add(new Device(Name, IP, Port));
            myAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("Device list", devices);
        super.onSaveInstanceState(outState);
    }


/*  view: The view within the AdapterView that was clicked (this will be a view provided by the adapter)
    position: The position of the view in the adapter.
    id: The row id of the item that was clicked.*/

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Toast.makeText(this, "Trying to connect...", Toast.LENGTH_LONG).show();

        Object deviceName_add = ipList.getItemAtPosition(position);
        Device item_add = (Device) deviceName_add;
        //Device deviceConnected = new Device(item_add.getName(), item_add.getIpAddress(), item_add.getPort());
        String intentName_add = item_add.getName();
        String intentIP_add = item_add.getIpAddress();
        int intentPort_add = item_add.getPort();
        Log.d("PORTA SELEZIONATA", "porta selezionata: " + intentPort_add);

        //CONNECTEDDevices.add(item_add);

        connectionRequest = true;

        Intent intent = new Intent();
        intent.putExtra("FROM", "Pop");
        intent.putExtra("CONNECTION REQUEST", connectionRequest);
        //intent.putExtra("CONNECTED DEVICES", deviceConnected);
        //intent.putParcelableArrayListExtra("DEVICES", CONNECTEDDevices);
        intent.putExtra("DEVICE NAME", intentName_add);
        intent.putExtra("DEVICE IP", intentIP_add);
        intent.putExtra("DEVICE PORT", intentPort_add);
        intent.putExtra("PATIENT NUMBER", nr_patient);
        setResult(GraphActivity.DEVICE_CHOOSEN, intent);
        finish();

    }
}

