package nadia.epfl.com.ancontrol;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import static nadia.epfl.com.ancontrol.R.id.button_patient1;

public class GraphActivity extends AppCompatActivity implements View.OnClickListener {

    private static Context context;
    private Intent intentForPopActivity;
    private int nrPatientFromPopActivity;
    private static TextView namePatient;

    private static Button connectDevicePatient1;
    private static Button startMeasurementPatient1;
    private RelativeLayout relativeLayoutPatient1;
    private RelativeLayout relativeLayoutPatient1_2;
    private static Button stopMeasurementPatient1;
    private static TextView connectionStatusPatient1;

    private static Button connectDevicePatient2;
    private static Button startMeasurementPatient2;
    private RelativeLayout relativeLayoutPatient2;
    private RelativeLayout relativeLayoutPatient2_2;
    private static Button stopMeasurementPatient2;
    private static TextView connectionStatusPatient2;

    private static Button patientOne;
    private static Button patientTwo;
    public Button buttonStopService;
    private int numberOfPatients;
    private int nr_patient;

    //info about patient
    private static ArrayList<Patients> patientsArrayList;

    private String deviceNameForConnection = null;
    private String deviceIPForConnection = null;
    private int devicePortForConnection = 0;
    private Boolean connectionRequest = false;
    public static LineChart chart1;
    public static LineChart chart2;
    public static LineChart chart3;
    public static LineChart chart4;
    public static LineChart chart5;
    public static LineChart chart6;

    //Handler cases
    static final int DEVICE_CHOOSEN = 1;
    static final int CONNECTION_SUCCESS = 2;
    static final int CONNECTION_INSUCCESS = 3;
    static final int CHOOSE_DEVICE_FOR_CONNECTION_PATIENT1 = 4;
    static final int STOP_MEASUREMENT = 5;
    static final int ERROR_MESSAGE = 6;
    static final int CHOOSE_DEVICE_FOR_CONNECTION_PATIENT2 = 7;
    static final String TAG = "GraphActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        if (savedInstanceState != null) {
            patientsArrayList = savedInstanceState.getParcelableArrayList("patients");
            Log.d(TAG, "OnsaveInstance: " + "token " + patientsArrayList.get(patientsArrayList.size()).getToken() +"username "+ patientsArrayList.get(patientsArrayList.size()).getUsername());
        }

        context = GraphActivity.this;

        //Patient 1 layout
        connectDevicePatient1 = (Button) findViewById(R.id.connectDevicePatient1);
        startMeasurementPatient1 = (Button) findViewById(R.id.startMeasurement);
        stopMeasurementPatient1 = (Button) findViewById(R.id.stopMeasurement);
        connectionStatusPatient1 = (TextView) findViewById(R.id.textView_ConnectionStatus);
        relativeLayoutPatient1 = (RelativeLayout) findViewById(R.id.relativeLayout);
        relativeLayoutPatient1_2 = (RelativeLayout) findViewById(R.id.relativeLayout2);

        //Patient 2 layout
        connectDevicePatient2 = (Button) findViewById(R.id.connectDevicePatient2);
        startMeasurementPatient2 = (Button) findViewById(R.id.startMeasurementPatient2);
        stopMeasurementPatient2 = (Button) findViewById(R.id.stopMeasurementPatient2);
        connectionStatusPatient2 = (TextView) findViewById(R.id.textView_ConnectionStatusPatient2);
        relativeLayoutPatient2 = (RelativeLayout) findViewById(R.id.relativeLayoutPatient2);
        relativeLayoutPatient2_2 = (RelativeLayout) findViewById(R.id.relativeLayout2Patient2);

        buttonStopService = (Button) findViewById(R.id.button_StopService);
        patientOne = (Button) findViewById(R.id.button_patient1);
        patientTwo = (Button) findViewById(R.id.button_patient2);
        namePatient = (TextView) findViewById(R.id.textView_PatientName);

        chart1 = (LineChart) findViewById(R.id.chart1);
        chart2 = (LineChart) findViewById(R.id.chart2);
        chart3 = (LineChart) findViewById(R.id.chart3);
        chart4 = (LineChart) findViewById(R.id.chart4);
        chart5 = (LineChart) findViewById(R.id.chart5);
        chart6 = (LineChart) findViewById(R.id.chart6);

        patientOne.setEnabled(false);
        patientTwo.setEnabled(false);
        startMeasurementPatient1.setEnabled(false);
        stopMeasurementPatient1.setEnabled(false);
        startMeasurementPatient2.setEnabled(false);
        stopMeasurementPatient2.setEnabled(false);

        connectionStatusPatient1.setText("DISCONNECTED");
        connectionStatusPatient1.setTextColor(Color.RED);

        //Hiding views for Patient2
        patientOne.setVisibility(View.INVISIBLE);
        patientTwo.setVisibility(View.INVISIBLE);
        relativeLayoutPatient2_2.setVisibility(View.INVISIBLE);
        relativeLayoutPatient2.setVisibility(View.INVISIBLE);
        connectionStatusPatient2.setVisibility(View.INVISIBLE);
        connectDevicePatient2.setVisibility(View.INVISIBLE);
        chart4.setVisibility(View.INVISIBLE);
        chart5.setVisibility(View.INVISIBLE);
        chart6.setVisibility(View.INVISIBLE);

        if(getIntent().getStringExtra("FROM").equals("PryvTokenActivity") || getIntent().getStringExtra("FROM").equals("PatientActivity")){
            patientsArrayList = getIntent().getParcelableArrayListExtra("PATIENTINFO");
            numberOfPatients = getIntent().getIntExtra("NUMBER_OF_PATIENTS",1);
            namePatient.setText(patientsArrayList.get(0).getName() + " " + patientsArrayList.get(0).getSurname());
            if(patientsArrayList.get(0).getStatusConnection()) {
                connectionStatusPatient1.setText("CONNECTED");
                connectionStatusPatient1.setTextColor(Color.GREEN);
            }

            if (numberOfPatients == 2) {
                patientOne.setVisibility(View.VISIBLE);
                patientTwo.setVisibility(View.VISIBLE);
                patientOne.setEnabled(false);
                patientTwo.setEnabled(true);
            }

            for (int i=0; i < patientsArrayList.size(); i++) {
                Log.d(TAG, "patients: " + patientsArrayList.size() + " // " + "statusConnection: " + patientsArrayList.get(i).getStatusConnection() + " // " + "name: " + patientsArrayList.get(i).getName() + " // " + "surname: " + patientsArrayList.get(i).getSurname() +
                        " // " + "number: " + patientsArrayList.get(i).getNumber() + " // " + "token" + patientsArrayList.get(i).getToken() + " // " + "username" + patientsArrayList.get(i).getUsername());
            }
        }

        connectDevicePatient1.setOnClickListener(this);
        connectDevicePatient2.setOnClickListener(this);
        buttonStopService.setOnClickListener(this);
        startMeasurementPatient1.setOnClickListener(this);
        stopMeasurementPatient1.setOnClickListener(this);
        startMeasurementPatient2.setOnClickListener(this);
        stopMeasurementPatient2.setOnClickListener(this);
        patientOne.setOnClickListener(this);
        patientTwo.setOnClickListener(this);

        //CHARTS OPTIONS
        ChartOption(chart1);
        ChartOption(chart2);
        ChartOption(chart3);
        ChartOption(chart4);
        ChartOption(chart5);
        ChartOption(chart6);
    }

    public void StartWifiService() {
        Log.d(TAG, "Start wifiservice");
        Intent intentService = new Intent(this, WiFiService.class);
        intentService.setExtrasClassLoader(Patients.class.getClassLoader());
        intentService.putParcelableArrayListExtra("PATIENTINFO", patientsArrayList);
        intentService.putExtra("patientNR", nrPatientFromPopActivity);
        intentService.putExtra("STOP MEASUREMENT", false);
        intentService.putExtra("START MEASUREMENT", false);
        startService(intentService);
    }

    //public void MsgToWifiService(String message, boolean value, int patient_nr) {
        public void MsgToWifiService(String message, boolean value) {
        Log.d(TAG, "Message to Service");
        Intent msgIntentService = new Intent(this, WiFiService.class);
        msgIntentService.putExtra(message, value);
        if(!patientOne.isEnabled())
            nr_patient = 0;
        else if(!patientTwo.isEnabled())
            nr_patient = 1;
        msgIntentService.putExtra("patientNR", nr_patient);
        startService(msgIntentService);
    }

    //
    //------- BUTTONS --------

    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connectDevicePatient1:
                ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0,
                        0, v.getWidth(), v.getHeight());
                intentForPopActivity = new Intent(GraphActivity.this, Pop.class);
                intentForPopActivity.putExtra("PATIENT", 0);
                startActivityForResult(intentForPopActivity, CHOOSE_DEVICE_FOR_CONNECTION_PATIENT1, options.toBundle());
                break;

            case R.id.connectDevicePatient2:
                intentForPopActivity = new Intent(GraphActivity.this, Pop.class);
                intentForPopActivity.putExtra("PATIENT", 1);
                ActivityOptions option = ActivityOptions.makeScaleUpAnimation(v, 0,
                        0, v.getWidth(), v.getHeight());
                startActivityForResult(intentForPopActivity, CHOOSE_DEVICE_FOR_CONNECTION_PATIENT2, option.toBundle());
                break;

            case R.id.button_StopService:
                Log.d(TAG, "service stopped");
                connectDevicePatient1.setEnabled(true);
                startMeasurementPatient1.setEnabled(false);
                stopMeasurementPatient1.setEnabled(false);
                connectDevicePatient2.setEnabled(true);
                startMeasurementPatient2.setEnabled(false);
                stopMeasurementPatient2.setEnabled(false);
                connectionStatusPatient1.setText("DISCONNECTED");
                connectionStatusPatient1.setTextColor(Color.RED);
                Intent in = new Intent(this, WiFiService.class);
                stopService(in);
                break;

            case R.id.stopMeasurementPatient2:
                Log.d(TAG, "stop_measurement_patient2 pressed");
                MsgToWifiService("STOP MEASUREMENT", true);
                break;

            case R.id.stopMeasurement:
                Log.d(TAG, "stop_measurement_patient1 pressed");
                MsgToWifiService("STOP MEASUREMENT", true);
                break;

            case R.id.startMeasurement:
                Log.d(TAG, "start_measurement_patient1 pressed");
                MsgToWifiService("START MEASUREMENT", true);
                break;

            case R.id.startMeasurementPatient2:
                Log.d(TAG, "start_measurement_Patient2 pressed");
                MsgToWifiService("START MEASUREMENT", true);
                break;

            case button_patient1:
                patientTwo.setEnabled(true);
                patientOne.setEnabled(false);

                nr_patient = 0;
                namePatient.setText(patientsArrayList.get(nr_patient).getName() + " " + patientsArrayList.get(nr_patient).getSurname());

                connectionStatusPatient1.setVisibility(View.VISIBLE);
                connectDevicePatient1.setVisibility(View.VISIBLE);
                relativeLayoutPatient1.setVisibility(View.VISIBLE);
                relativeLayoutPatient1_2.setVisibility(View.VISIBLE);
                chart1.setVisibility(View.VISIBLE);
                chart2.setVisibility(View.VISIBLE);
                chart3.setVisibility(View.VISIBLE);

                connectionStatusPatient2.setVisibility(View.INVISIBLE);
                connectDevicePatient2.setVisibility(View.INVISIBLE);
                relativeLayoutPatient2.setVisibility(View.INVISIBLE);
                relativeLayoutPatient2_2.setVisibility(View.INVISIBLE);
                chart4.setVisibility(View.INVISIBLE);
                chart5.setVisibility(View.INVISIBLE);
                chart6.setVisibility(View.INVISIBLE);

                break;

            case R.id.button_patient2:
                patientTwo.setEnabled(false);
                patientOne.setEnabled(true);

                nr_patient = 1;
                namePatient.setText(patientsArrayList.get(nr_patient).getName() + " " + patientsArrayList.get(nr_patient).getSurname());

                connectionStatusPatient1.setVisibility(View.INVISIBLE);
                connectDevicePatient1.setVisibility(View.INVISIBLE);
                relativeLayoutPatient1.setVisibility(View.INVISIBLE);
                relativeLayoutPatient1_2.setVisibility(View.INVISIBLE);
                chart1.setVisibility(View.INVISIBLE);
                chart2.setVisibility(View.INVISIBLE);
                chart3.setVisibility(View.INVISIBLE);

                connectionStatusPatient2.setVisibility(View.VISIBLE);
                connectDevicePatient2.setVisibility(View.VISIBLE);
                relativeLayoutPatient2.setVisibility(View.VISIBLE);
                relativeLayoutPatient2_2.setVisibility(View.VISIBLE);
                chart4.setVisibility(View.VISIBLE);
                chart5.setVisibility(View.VISIBLE);
                chart6.setVisibility(View.VISIBLE);

                break;

            default:
                break;
        }
    }

    //------- MESSAGES -------
    public static class MyHandler extends Handler {

        private double current;
        private double voltage;
        private double time;
        private int threshold;
        private WiFiService.MyResult results;
        private String currentString;
        private String voltageString;

        @Override
        public void handleMessage(Message msg) {

            int nr_patient =-1;

            if (msg != null) {
                switch (msg.what) {
                    case WiFiService.WE0_AND_CA:
                        results = GetData(msg);
                        current = results.getCurrent();
                        time = results.getVoltageOrTime();
                        nr_patient = results.getNrPatient();
                        //threshold = results.getThreshold();

                        if(nr_patient == 0){
                            addEntryChart(chart1, current, time, "CA", 30);
                            //addEntryChart(chart5,50,time,"CA");
                            startMeasurementPatient1.setEnabled(false);
                            stopMeasurementPatient1.setEnabled(true);}

                        else if(nr_patient == 1){
                            addEntryChart(chart5, current, time, "CA", 30);
                            //addEntryChart(chart5,50,time,"CA");
                            startMeasurementPatient2.setEnabled(false);
                            stopMeasurementPatient2.setEnabled(true);}
                        Log.d(TAG, " WE0: CA current " + current + " // " + "CA time " + voltage + " // " + "threshold" + threshold);
                        break;

                    case WiFiService.WE0_AND_CV:
                        results = GetData(msg);
                        current = results.getCurrent();
                        voltage = results.getVoltageOrTime();
                        nr_patient = results.getNrPatient();
                        //threshold = results.getThreshold();

                        if(nr_patient == 0){
                            //addEntryChart(chart1, current, voltage, "CV"); not coming back CV
                            startMeasurementPatient1.setEnabled(false);
                            stopMeasurementPatient1.setEnabled(true);}
                        else if(nr_patient == 1){
                            //addEntryChart(chart5, current, voltage, "CV");
                            startMeasurementPatient2.setEnabled(false);
                            stopMeasurementPatient2.setEnabled(true);}
                        Log.d(TAG, "CV current " + current + " // " + "CV voltage " + voltage);
                        break;

                    case WiFiService.WE0_AND_DPV:
                        results = GetData(msg);
                        current = results.getCurrent();
                        voltage = results.getVoltageOrTime();
                        //threshold = results.getThreshold();

                        if(nr_patient == 0){
                            addEntryChart(chart1, current, voltage, "DPV", threshold);
                            startMeasurementPatient1.setEnabled(false);
                            stopMeasurementPatient1.setEnabled(true);}
                        else if(nr_patient == 1){
                            addEntryChart(chart5, current, voltage, "DPV", threshold);
                            startMeasurementPatient2.setEnabled(false);
                            stopMeasurementPatient2.setEnabled(true);}
                        break;

                    case WiFiService.WE1_AND_CA:
                        results = GetData(msg);
                        current = results.getCurrent();
                        time = results.getVoltageOrTime();
                        nr_patient = results.getNrPatient();
                        //threshold = results.getThreshold();

                        if(nr_patient == 0){
                            addEntryChart(chart2, current, time, "CA", 20);
                            startMeasurementPatient1.setEnabled(false);
                            stopMeasurementPatient1.setEnabled(true);}
                        else if(nr_patient == 1){
                            addEntryChart(chart6, current, time, "CA", 20);
                            startMeasurementPatient2.setEnabled(false);
                            stopMeasurementPatient2.setEnabled(true);}
                        Log.d(TAG, " WE1: CA current " + current + " // " + "CA time " + voltage);
                        break;

                    case WiFiService.WE1_AND_CV:
                        results = GetData(msg);
                        current = results.getCurrent();
                        voltage = results.getVoltageOrTime();
                        nr_patient = results.getNrPatient();
                        //threshold = results.getThreshold();

                        if(nr_patient == 0){
                            addEntryChart(chart2, current, voltage, "CV", threshold);
                            startMeasurementPatient1.setEnabled(false);
                            stopMeasurementPatient1.setEnabled(true);}
                        else if(nr_patient == 1){
                            addEntryChart(chart6, current, voltage, "CV", threshold);
                            startMeasurementPatient2.setEnabled(false);
                            stopMeasurementPatient2.setEnabled(true);}
                        break;

                    case WiFiService.WE1_AND_DPV:
                        results = GetData(msg);
                        current = results.getCurrent();
                        voltage = results.getVoltageOrTime();
                        nr_patient = results.getNrPatient();
                        //threshold = results.getThreshold();

                        if(nr_patient == 0){
                            addEntryChart(chart2, current, voltage, "DPV", threshold);
                            startMeasurementPatient1.setEnabled(false);
                            stopMeasurementPatient1.setEnabled(true);}
                        else if(nr_patient == 1){
                            addEntryChart(chart6, current, voltage, "DPV", threshold);
                            startMeasurementPatient2.setEnabled(false);
                            stopMeasurementPatient2.setEnabled(true);}
                        Log.d(TAG, "DPV current " + current + " // " + "DPV voltage " + voltage);
                        break;

                    case WiFiService.WE2_AND_CA:
                        results = GetData(msg);
                        current = results.getCurrent();
                        time = results.getVoltageOrTime();
                        nr_patient = results.getNrPatient();
                        //threshold = results.getThreshold();

                        if(nr_patient == 0){
                            addEntryChart(chart3, current, time, "CA", 40);
                            startMeasurementPatient1.setEnabled(false);
                            stopMeasurementPatient1.setEnabled(true);}
                        else if(nr_patient == 1){
                            addEntryChart(chart4, current, time, "CA", 40);
                            startMeasurementPatient2.setEnabled(false);
                            stopMeasurementPatient2.setEnabled(true);}
                        Log.d(TAG, "WE2: CA current " + current + " // " + "CA time " + time);
                        break;

                    case WiFiService.WE2_AND_CV:
                        results = GetData(msg);
                        current = results.getCurrent();
                        voltage = results.getVoltageOrTime();
                        nr_patient = results.getNrPatient();
                        //threshold = results.getThreshold();

                        if(nr_patient == 0){
                            addEntryChart(chart3, current, voltage, "CV", threshold);
                            startMeasurementPatient1.setEnabled(false);
                            stopMeasurementPatient1.setEnabled(true);}
                        else if(nr_patient == 1){
                            addEntryChart(chart4, current, voltage, "CV", threshold);
                            startMeasurementPatient2.setEnabled(false);
                            stopMeasurementPatient2.setEnabled(true);}
                        break;

                    case WiFiService.WE2_AND_DPV:
                        results = GetData(msg);
                        current = results.getCurrent();
                        voltage = results.getVoltageOrTime();
                        nr_patient = results.getNrPatient();
                        //threshold = results.getThreshold();

                        if(nr_patient == 0){
                            addEntryChart(chart3, current, voltage, "DPV", threshold);
                            startMeasurementPatient1.setEnabled(false);
                            stopMeasurementPatient1.setEnabled(true);}
                        else if(nr_patient == 1){
                            addEntryChart(chart4, current, voltage, "DPV", threshold);
                            startMeasurementPatient2.setEnabled(false);
                            stopMeasurementPatient2.setEnabled(true);}
                        break;

                    case CONNECTION_SUCCESS:
                        nr_patient=(int) msg.obj;
                        Log.d(TAG,"nr patient connection success " + nr_patient);
                        if(nr_patient == 0){
                            connectDevicePatient1.setEnabled(false);
                            patientsArrayList.get(nr_patient).setStatusConnection(true);
                            connectionStatusPatient1.setText("CONNECTED");
                            connectionStatusPatient1.setTextColor(Color.GREEN);
                            startMeasurementPatient1.setEnabled(true);
                            stopMeasurementPatient1.setEnabled(false);}
                        if(nr_patient == 1){
                            connectDevicePatient2.setEnabled(false);
                            patientsArrayList.get(nr_patient).setStatusConnection(true);
                            connectionStatusPatient2.setText("CONNECTED");
                            connectionStatusPatient2.setTextColor(Color.GREEN);
                            startMeasurementPatient2.setEnabled(true);
                            stopMeasurementPatient2.setEnabled(false);}
                        break;

                    case CONNECTION_INSUCCESS:
                        nr_patient=(int) msg.obj;
                        Log.d(TAG,"patient non succesfully connected :( "+ nr_patient);
                        if(nr_patient == 0){
                            connectionStatusPatient1.setText("DISCONNECTED");
                            connectionStatusPatient1.setTextColor(Color.RED);
                        }
                        if(nr_patient == 1){
                            connectionStatusPatient2.setText("DISCONNECTED");
                            connectionStatusPatient2.setTextColor(Color.RED);
                        }
                        Toast.makeText(context, "Connection failed, check the wifi and the selected device", Toast.LENGTH_LONG).show();
                        break;

                    case STOP_MEASUREMENT:
                        nr_patient=(int) msg.obj;
                        Log.d(TAG, "STOP_MEASUREMENT_HANDLER" + " // " + "nr_patient: " + nr_patient );
                        if(nr_patient==0){
                            startMeasurementPatient1.setEnabled(true);
                            stopMeasurementPatient1.setEnabled(false);
                            Log.d(TAG, "STOP_MEASUREMENT_HANDLER_PATIENT1" + " // " + "nr_patient: " + nr_patient + " // " + "StartmeasurementButton1: " + startMeasurementPatient1.isEnabled() + " // " + "StopmeasurementButton1: " + stopMeasurementPatient1.isEnabled() );
                        }
                        if(nr_patient==1){
                            startMeasurementPatient2.setEnabled(true);
                            stopMeasurementPatient2.setEnabled(false);
                            Log.d(TAG, "STOP_MEASUREMENT_HANDLER_PATIENT2" + " // " + "nr_patient: " + nr_patient + " // " + "StartmeasurementButton2: " + startMeasurementPatient2.isEnabled() + " // " + "StopmeasurementButton2: " + stopMeasurementPatient2.isEnabled() );
                        }
                        Toast.makeText(context, "Stop Receiving Data", Toast.LENGTH_LONG).show();
                        break;

                    case ERROR_MESSAGE:
                        Toast.makeText(context, "You are not receiving something correct. Please check what you are sending..", Toast.LENGTH_LONG).show();
                        break;

                    default:
                        break;
                }

            } else {
                Log.d("HANDLER ERROR", "msg received null");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_patient:
                Intent intentAddPatient = new Intent(this, PatientActivity.class);
                intentAddPatient.putExtra("NUMBER_OF_PATIENTS", (patientsArrayList.size() + 1));
                intentAddPatient.putParcelableArrayListExtra("INFOPATIENTS", patientsArrayList);
                startActivity(intentAddPatient);
                return true;

            default:
                return true;
        }
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("PATIENTS", patientsArrayList);
        Log.d(TAG, "OnsaveInstance: " + "token " + patientsArrayList.get(0).getToken() + "username " + patientsArrayList.get(0).getUsername());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == DEVICE_CHOOSEN) {
            connectionRequest = data.getBooleanExtra("CONNECTION REQUEST", false);
            deviceNameForConnection = data.getStringExtra("DEVICE NAME");
            deviceIPForConnection = data.getStringExtra("DEVICE IP");
            devicePortForConnection = data.getIntExtra("DEVICE PORT", 0);
            nrPatientFromPopActivity = data.getIntExtra("PATIENT NUMBER", -1);
            patientsArrayList.get(nrPatientFromPopActivity).setIPserver(deviceIPForConnection);
            patientsArrayList.get(nrPatientFromPopActivity).setPORTserver(devicePortForConnection);

            Log.d(TAG, "numberOfPatientFromPop: " + nrPatientFromPopActivity + " // " + "connectionRequest: " + connectionRequest);

            if (connectionRequest) {

                for (int i = 0; i < patientsArrayList.size(); i++) {
                    Log.d(TAG, "TokenAndUsername" + "Token added: " + patientsArrayList.get(i).getToken() + " // " + "Username added: " + patientsArrayList.get(i).getUsername() + " // " +
                            "Name: " + patientsArrayList.get(i).getName() + " // " + "Surname: " + patientsArrayList.get(i).getSurname() + " // " + "Number: " + patientsArrayList.get(i).getNumber() + " // " +
                            "PryvChecked" + patientsArrayList.get(i).getPryvChecked());
                }
                StartWifiService();
                //connectionRequest = false;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intentDestroy = new Intent(this, WiFiService.class);
        stopService(intentDestroy);
    }

    public static WiFiService.MyResult GetData(Message msgReceived) {

        if (msgReceived.what == WiFiService.WE0_AND_CV || msgReceived.what == WiFiService.WE0_AND_DPV ||
                msgReceived.what == WiFiService.WE1_AND_CV || msgReceived.what == WiFiService.WE1_AND_DPV ||
                msgReceived.what == WiFiService.WE2_AND_CV || msgReceived.what == WiFiService.WE2_AND_DPV) {

            Bundle bundle = (Bundle) msgReceived.obj;
            double currentReceived = bundle.getDouble("CURRENT"); //uA
            double voltageReceived = bundle.getDouble("VOLTAGE"); //mV
            int nr_patient = bundle.getInt("NR_patient");
            int threshold = bundle.getInt("THRESHOLD");

            if (nr_patient == 0) {
                stopMeasurementPatient1.setEnabled(true);
                startMeasurementPatient1.setEnabled(false);
            } else if (nr_patient == 1) {
                stopMeasurementPatient2.setEnabled(true);
                startMeasurementPatient2.setEnabled(false);
            }
            WiFiService.MyResult resultsReceived = new WiFiService.MyResult(currentReceived, voltageReceived, nr_patient);
            return resultsReceived;

        } else if ((msgReceived.what == WiFiService.WE0_AND_CA || msgReceived.what == WiFiService.WE1_AND_CA || msgReceived.what == WiFiService.WE2_AND_CA )) {

            Bundle bundle = (Bundle) msgReceived.obj;
            double currentReceived = bundle.getDouble("CURRENT"); //uA
            double timeReceived = bundle.getDouble("TIME"); //mV
            int nr_patient = bundle.getInt("NR_patient");
            int threshold = bundle.getInt("THRESHOLD");

            if (nr_patient == 0) {
                stopMeasurementPatient1.setEnabled(true);
                startMeasurementPatient1.setEnabled(false);
            } else if (nr_patient == 1) {
                stopMeasurementPatient2.setEnabled(true);
                startMeasurementPatient2.setEnabled(false);
            }
            WiFiService.MyResult resultsReceived = new WiFiService.MyResult(currentReceived, timeReceived, nr_patient);
            return resultsReceived;
        }
        return null;
    }


    //----  GRAPHS: MPANDROIDCHART ------

    //Method to add entry to the LineChart
    public static void addEntryChart(LineChart chart, double current, double appliedvoltage, String technique, int threshold) {

        LineData data = chart.getData();
        LimitLine ll = new LimitLine(threshold);
        chart.getAxisLeft().addLimitLine(ll);

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                //creation

                set = createSet(technique);
                data.addDataSet(set);
            }

            if (technique.equals("CV") || technique.equals("DPV")) {
                data.addEntry(new Entry((float) appliedvoltage, (float) current), 0);
                data.notifyDataChanged();
                chart.getDescription().setText("Voltage (mV)");

            } else if (technique.equals("CA")){
                data.addEntry(new Entry(data.getEntryCount(), (float) current), 0);
                //data.addEntry(new Entry((float) appliedvoltage, (float) current), 0);
                data.notifyDataChanged();
                chart.getDescription().setText("Time (s)");
            }

            //Notifiy to the chart that data are changed
            chart.notifyDataSetChanged();

            //Limit number of visible entries
            chart.setVisibleXRangeMaximum(300);

            // move to the latest entry
            //chart.moveViewToX(data.getEntryCount());
            chart.moveViewToX((float)appliedvoltage);
        }
    }

    private void ChartOption (LineChart chart) {
        chart.setNoDataText("No data for the moment");
        //enable touch gestures, dragging and scaling. Enable pinch zoom to avoid scaling x and y axis separately
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(true);
        chart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        chart.setData(data);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setDrawAxisLine(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yl = chart.getAxisLeft();
        yl.setTextColor(Color.WHITE);
        yl.setDrawGridLines(true);
        yl.setDrawAxisLine(true);
        yl.setAxisMaximum(50f);
        yl.setAxisMinimum(0f);

        YAxis yr = chart.getAxisRight();
        yr.setEnabled(true);
    }

    private static LineDataSet createSet(String technique) {
        LineDataSet set = new LineDataSet(null, technique);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setFillColor(Color.WHITE);
        set.setValueTextSize(10f);
        return set;
    }
}
