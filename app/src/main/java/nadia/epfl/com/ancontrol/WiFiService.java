package nadia.epfl.com.ancontrol;

import android.Manifest;
import android.app.Service;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.database.DBinitCallback;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by tamburra on 14.08.17.
 */

public class WiFiService extends Service {

    private static Context context;
    public static Thread Thread1 = null;
    public static Connection connection;
    public static Intent notificationIntent;
    public static Boolean startMeasurement = false;
    public static Boolean stopMeasurement = false;
    public static int nr_patient;
    public Handler mHandler;
    private static ArrayList<FileOutputStream> fileStream0;
    private static ArrayList<FileOutputStream> fileStream1;
    private static ArrayList<FileOutputStream> fileStream2;

    //Info about Patient
    public static ArrayList<Patients> patients_list;
    private HashMap<Integer, Boolean> startMeasurementHashMap = new HashMap<>();
    private HashMap<Integer, Boolean> stopMeasurementHashMap = new HashMap<>();
    private static HashMap<Integer, Socket> socketHashMap = new HashMap<>();
    private static HashMap<Integer, Connection> connectionHashMap = new HashMap<>();

    public String tokenPryv;
    public String usernamePryv;

    //Constants for float conversion
    final static double ADC_REF = 3300 / 1.6; //Unit is mV.
    final static int ADC_SCALE = 2048;
    final static int ADC_BIT = 12;
    final static int DAC_REF = 3300; //Unit is mV.
    final static int DAC_BIT = 10;
    final static int DAC_SCALE = 1024;
    final static float DAC_OFFSET = DAC_REF / 2;

    //wifi communication constants for server
    static final String CONNECTING_REQ = "Hello"; //"B" Server connection request
    static final String CONNECTING_ANS = "Hello!"; //Server answer to the request
    static final String DATA_REQ = "START"; //Server send data request
    static final String STOP_REQ = "STOP"; //"C" Server send data request
    static final String TAG = "WiFiService";
    static final int WE0_AND_CA = 11;
    static final int WE0_AND_CV = 12;
    static final int WE0_AND_DPV = 13;
    static final int WE1_AND_CA = 21;
    static final int WE1_AND_CV = 22;
    static final int WE1_AND_DPV = 23;
    static final int WE2_AND_CA = 31;
    static final int WE2_AND_CV = 32;
    static final int WE2_AND_DPV = 33;

    private static double time_start0 = -1;
    private static double time_start1 = -1;
    private static double time_start2 = -1;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public WiFiService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        context = WiFiService.this;

       //line to allow the connection and the socket creation to happen in the thread. To avoid android.os.NetworkOnMainThreadException!
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        notificationIntent = intent;
        startMeasurement = notificationIntent.getBooleanExtra("START MEASUREMENT", false);
        stopMeasurement = notificationIntent.getBooleanExtra("STOP MEASUREMENT", false);
        nr_patient = notificationIntent.getIntExtra("patientNR", -1);

        Log.d(TAG, "intent data: " + startMeasurement + " // " + stopMeasurement + " // " + nr_patient + " // " + startMeasurementHashMap.size());

        if (nr_patient != -1) {
            Log.d(TAG, "startMeasurementArrayList0: " + "size: " + startMeasurementHashMap.size() + " // " + "nr_patient: " + nr_patient);
            if (stopMeasurement) {
                startMeasurement = true;
                Log.d(TAG, "startMeasurementArrayList0: " + "stopmeasurement true " + " // " + "nr_patient: " + nr_patient);
            }
            startMeasurementHashMap.put(nr_patient, startMeasurement);
            stopMeasurementHashMap.put(nr_patient, stopMeasurement);
            Log.d(TAG, "startMeasurementArrayList1: " + startMeasurementHashMap.get(nr_patient));
        } else {
            Toast.makeText(this, "Patient number is -1", Toast.LENGTH_LONG).show();
        }

        mHandler = new GraphActivity.MyHandler();

        //if button start is not pressed the wifi connection has to be set up
        if(!startMeasurementHashMap.get(nr_patient)) {

            Log.d(TAG, "startMeasurement: " + startMeasurementHashMap.get(nr_patient));
            patients_list = notificationIntent.getParcelableArrayListExtra("PATIENTINFO");
            Log.d(TAG, "Connection request to: " + patients_list.get(nr_patient).getIPserver() + " // " + patients_list.get(nr_patient).getPORTserver() + " // " + nr_patient);

            //if doctor wants to send data to pryv
            if (patients_list.get(nr_patient).getPryvChecked()) {
                tokenPryv = patients_list.get(nr_patient).getToken();
                usernamePryv = patients_list.get(nr_patient).getUsername();
                Log.d(TAG, "PRYV CONNECTION: " + "connection Pryv: " + tokenPryv + " " + usernamePryv);
                connectionHashMap.put(nr_patient, CreatePryvConnection(usernamePryv, tokenPryv)); //for each patient a new PRYV connection is created and added into an arraylist
                CreatePryvStream("PERSONAL_DATA", "PERSONAL DATA", connectionHashMap.get(nr_patient));
                SendToPryv(0, 0, "PERSONAL_DATA", "PERSONAL_DATA", connectionHashMap.get(nr_patient));
            }

            try {
                //true = first time: we need to create the connection
                Log.d(TAG, "Launching thread1: establishing connection with RPi " + nr_patient);
                Thread1 = new Thread(new Thread1(mHandler, true, startMeasurementHashMap.get(nr_patient)));
                Thread1.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            //if button start is pressed, the data are coming
            Log.d(TAG, "START MEASUREMENTARRAYLIST: " + startMeasurementHashMap.get(nr_patient));
            try {
                Thread1 = new Thread1(mHandler, false, startMeasurement); //false = not first time
                Thread1.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }

    public static class Thread1 extends Thread implements Runnable {
        boolean first_time;
        private DataInputStream serverOnline;
        private Handler handler_thread;
        public static Socket socketThread1 = null;
        private Boolean startMeasurement;
        Thread2 comThread;

        //Constructor
        public Thread1(Handler handler, boolean first_conn, Boolean startMeasurement) throws IOException {
            this.handler_thread = handler;
            first_time = first_conn;
            this.startMeasurement = startMeasurement;
            patients_list.get(nr_patient).setFirst_timeSocket(first_conn);
            Log.d(TAG,"Thread1 first time? "+ patients_list.get(nr_patient).getFirst_timeSocket() + " // " + "nr_patient: " + nr_patient);

            if(first_time){
                socketThread1 = new Socket(patients_list.get(nr_patient).getIPserver(), patients_list.get(nr_patient).getPORTserver());
                Log.d(TAG, "THREAD1: " + "SocketCreated 1 " + socketThread1 + " " + patients_list.get(nr_patient).getPORTserver() + " " + patients_list.get(nr_patient).getIPserver());
                if(!socketHashMap.containsValue(socketThread1)){
                    socketHashMap.put(nr_patient, socketThread1);
                    for (int jj = 0; jj<socketHashMap.size(); jj++) {
                        Log.d(TAG, "THREAD1: " + "SocketArrayList " + socketHashMap.size() + " " + socketHashMap.get(jj) + " " + patients_list.get(jj).getPORTserver() + " " + patients_list.get(jj).getIPserver());
                    }
                }
            }
        }

        public void send2RPiMessage(byte c[], Socket socketThread1) throws IOException {
            OutputStream outputStream1 = socketThread1.getOutputStream();
            //Send the connecting request to the server
            outputStream1.write(c);
            Log.d(TAG, "THREAD1: " + "MessageSent2RPi " + c + " // " + socketThread1);
        }

        public void send2UImessage(Handler handler, int message_what, int nr_patient) {
            Message msg = Message.obtain();
            msg.obj = nr_patient;
            msg.what = message_what;
            handler.sendMessage(msg);
        }

        public void run() {
            try {
                if (first_time) { //if it's the first time create the connection
                    Log.d(TAG, "MESSAGE: " + "socket:" + socketHashMap.get(nr_patient));
                    send2RPiMessage(CONNECTING_REQ.getBytes(), socketHashMap.get(nr_patient));

                    //Reading Server's answer
                    serverOnline = new DataInputStream(socketHashMap.get(nr_patient).getInputStream());
                    byte[] received_bytes = new byte[6];
                    serverOnline.readFully(received_bytes);

                    String answer = new String(received_bytes);
                    Log.d(TAG, "Server's answer: " + answer);

                    if (answer.equals(CONNECTING_ANS)) {
                        send2UImessage(handler_thread, GraphActivity.CONNECTION_SUCCESS, nr_patient);
                    }

                    comThread = new Thread2(socketHashMap.get(nr_patient), handler_thread, first_time, nr_patient);
                    comThread.start();

                } else { //if it's not the first time.

                    Log.d(TAG, "Meas first time? " + first_time + " // " + patients_list.get(nr_patient).getFirst_timeSocket());
                    Log.d(TAG, "Meas start? " + startMeasurement);
                    Log.d(TAG, "Meas stop? " + stopMeasurement);
                    if (startMeasurement) {
                        if (stopMeasurement) {
                            send2RPiMessage(STOP_REQ.getBytes(), socketHashMap.get(nr_patient));
                            Log.d(TAG, "data requested1 " + socketHashMap.get(nr_patient));
                            send2UImessage(handler_thread, GraphActivity.STOP_MEASUREMENT, nr_patient);

                        } else {
                            Log.d(TAG,"data requested " + socketHashMap.get(nr_patient));
                            send2RPiMessage(DATA_REQ.getBytes(), socketHashMap.get(nr_patient));
                            comThread = new Thread2(socketHashMap.get(nr_patient), handler_thread, first_time, nr_patient);
                            comThread.start();
                        }
                    }
                }
            } catch (UnknownHostException e) {
                Log.d(TAG, "patient non succesfully connected :( " + nr_patient);
                send2UImessage(handler_thread, GraphActivity.CONNECTION_INSUCCESS, nr_patient);
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG, "patient non succesfully connected :( " + nr_patient);
                send2UImessage(handler_thread, GraphActivity.CONNECTION_INSUCCESS, nr_patient);
                e.printStackTrace();
            }
        }
    }

    public static class Thread2 extends Thread implements Runnable {

        public final Socket clientSocket;
        public Handler handler_2;
        public DataInputStream in;
        public DataInputStream in2;
        public double measuredCurrentDouble;
        public double appliedVoltageDouble;
        private int resultCheckFirstByte = 0;
        private int errBytes =0;
        public MyResult myResult;
        public String streamId;
        private Boolean firstTime;
        private int patientNumber = -1;
        private double time;
        private int thresholdForCA_Propofol = 20; //50uA
        private int thresholdForCA_Paracetamol = 30; //50uA
        private int thresholdForCA_Midazolam = 30; //50uA
        private int thresholdForDPV = 50; //50uA
        private int thresholdForCV = 50; //50uA

        //private int jj1 = 0;
        //private int jj2 = 0;
        //private int jj3 = 0;

        public Thread2(Socket clientSocket, Handler handler2, Boolean firstTime, int patient_nr) {
            this.clientSocket = clientSocket;
            this.handler_2 = handler2;
            this.firstTime = firstTime;
            this.patientNumber = patient_nr;
            Log.d(TAG,"Thread2");
        }

        public void send2UImessage(Handler handler, int message_what, int nr_patient) {
            Message msg = Message.obtain();
            msg.what = message_what;

            if (message_what == WiFiService.WE0_AND_CV || message_what == WiFiService.WE0_AND_DPV ||
                    message_what == WiFiService.WE1_AND_CV || message_what == WiFiService.WE1_AND_DPV ||
                    message_what == WiFiService.WE2_AND_CV || message_what == WiFiService.WE2_AND_DPV) {

                Bundle bundleMessage = new Bundle();
                bundleMessage.putDouble("CURRENT", measuredCurrentDouble);
                bundleMessage.putDouble("VOLTAGE", appliedVoltageDouble);
                bundleMessage.putInt("NR_patient", nr_patient);
                //bundleMessage.putInt("THRESHOLD", threshold);
                msg.obj = bundleMessage;

            } else if (message_what == WiFiService.WE0_AND_CA || message_what == WiFiService.WE1_AND_CA || message_what == WiFiService.WE2_AND_CA ) {

                Bundle bundleMessage = new Bundle();
                bundleMessage.putDouble("CURRENT", measuredCurrentDouble);
                bundleMessage.putDouble("TIME", time);
                bundleMessage.putInt("NR_patient", nr_patient);
                msg.obj = bundleMessage;
            }

            handler.sendMessage(msg);
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }

                    in = new DataInputStream(this.clientSocket.getInputStream());

                    byte[] received_bytes = new byte[6];
                    in.readFully(received_bytes);

                    if (in != null) {
                        resultCheckFirstByte = 0;
                        errBytes = 0;
                        while (resultCheckFirstByte == 0) {
                            //not recognised start byte
                            resultCheckFirstByte = CheckFirstByte(received_bytes[errBytes], handler_2);
                            Log.d(TAG, "RESULTCHECKFIRSTBYTE: " + resultCheckFirstByte + "nr_patient: " + nr_patient);
                            if(resultCheckFirstByte == 0) errBytes++; //1//2
                        }

                        Log.d(TAG, "RESULTCHECKFIRSTBYTE error: " + errBytes);
                        if(errBytes!=0) {
                            //shift
                            for (int i = 0; i < errBytes; i++) {
                                received_bytes[i] = received_bytes[i + errBytes];
                            }
                            //reading missing bytes
                            in = new DataInputStream(this.clientSocket.getInputStream());
                            byte[] bytes2add = new byte[errBytes];
                            in.readFully(bytes2add);
                            //put them in the end of receivedbytes
                            for (int i = 0; i < errBytes; i++) {
                                received_bytes[5-i]=bytes2add[errBytes -1 - i];
                            }
                            Log.d(TAG, "RESULTCHECKFIRSTBYTE byte: ");
                            for (int i = 0; i < 6; i++) {
                                Log.d(TAG, " " + received_bytes[i]);
                            }
                        }


                        //WE0 and CA
                        if (resultCheckFirstByte == 11) {
                            myResult = SendValues(received_bytes);
                            measuredCurrentDouble = myResult.getCurrent();
                            appliedVoltageDouble = myResult.getVoltageOrTime();

                            //if for 10 time (consequently) current is over threshold send notification to the smartwatch
                            //for each patient
                            if (measuredCurrentDouble > thresholdForCA_Propofol) {
                                int jj1 = patients_list.get(nr_patient).getCounterPropofol();
                                patients_list.get(nr_patient).setCounterPropofol(jj1+1);
                                Log.d(TAG, "if CounterPropofol: " + patients_list.get(nr_patient).getCounterPropofol());
                            } else {
                                //else put the counter at zero
                                patients_list.get(nr_patient).setCounterPropofol(0);
                                Log.d(TAG, "else CounterPropofol: " + patients_list.get(nr_patient).getCounterPropofol());
                            }

                            if (patients_list.get(nr_patient).getCounterPropofol() == 10) {
                                patients_list.get(nr_patient).setCounterPropofol(0);
                                SendToSmartwatch(nr_patient, "Propofol");
                            }

                            if (time_start0 == -1) {
                                time_start0 = System.currentTimeMillis();
                            }

                            time = (System.currentTimeMillis() - time_start0) / 1000; //time in seconds
                            Log.d(TAG, "Time for CA we0: " + time + " // " + "time_start0: " + time_start0);
                            send2UImessage(handler_2, WE0_AND_CA, patientNumber);

                            if (patients_list.get(nr_patient).getPryvChecked()) {
                                streamId = CreatePryvStream("amperometry_propofol", "PROPOFOL", connectionHashMap.get(nr_patient));
                                SendToPryv((measuredCurrentDouble), time, streamId, "CA", connectionHashMap.get(nr_patient));
                            }
                            saveDataToFile(measuredCurrentDouble, appliedVoltageDouble, 0, nr_patient);//WE0 = propofol
                        }


                        //WE0 and CV
                        if (resultCheckFirstByte == 12) {
                            myResult = SendValues(received_bytes);
                            measuredCurrentDouble = myResult.getCurrent();
                            appliedVoltageDouble = myResult.getVoltageOrTime();

                            //if for 10 time (consequently) current is over threshold send notification to the smartwatch
                            //for each patient
                            if (measuredCurrentDouble > thresholdForCV) {
                                int jj1 = patients_list.get(nr_patient).getCounterPropofol();
                                patients_list.get(nr_patient).setCounterPropofol(jj1+1);
                                Log.d(TAG, "if CounterPropofol: " + patients_list.get(nr_patient).getCounterPropofol());
                            } else {
                                //else put the counter at zero
                                patients_list.get(nr_patient).setCounterPropofol(0);
                                Log.d(TAG, "else CounterPropofol: " + patients_list.get(nr_patient).getCounterPropofol());
                            }

                            if (patients_list.get(nr_patient).getCounterPropofol() == 10) {
                                patients_list.get(nr_patient).setCounterPropofol(0);
                                SendToSmartwatch(nr_patient, "Propofol");
                            }
                            send2UImessage(handler_2, WE0_AND_CV, patientNumber);

                            if (patients_list.get(nr_patient).getPryvChecked()) {
                                streamId = CreatePryvStream("cyclic_voltammetry", "PROPOFOL", connectionHashMap.get(nr_patient));
                                SendToPryv(measuredCurrentDouble, appliedVoltageDouble, streamId, "CV", connectionHashMap.get(nr_patient));
                            }
                            saveDataToFile(measuredCurrentDouble, appliedVoltageDouble, 0, nr_patient);//WE0 = propofol
                        }


                        //WE0 and DPV
                        if (resultCheckFirstByte == 13) {
                            myResult = SendValues(received_bytes);
                            measuredCurrentDouble = myResult.getCurrent();
                            appliedVoltageDouble = myResult.getVoltageOrTime();

                            //if for 10 time (consequently) current is over threshold send notification to the smartwatch
                            //for each patient
                            if (measuredCurrentDouble > thresholdForDPV) {
                                int jj1 = patients_list.get(nr_patient).getCounterPropofol();
                                patients_list.get(nr_patient).setCounterPropofol(jj1+1);
                                Log.d(TAG, "if CounterPropofol: " + patients_list.get(nr_patient).getCounterPropofol());
                            } else {
                                //else put the counter at zero
                                patients_list.get(nr_patient).setCounterPropofol(0);
                                Log.d(TAG, "else CounterPropofol: " + patients_list.get(nr_patient).getCounterPropofol());
                            }

                            if (patients_list.get(nr_patient).getCounterPropofol() == 10) {
                                patients_list.get(nr_patient).setCounterPropofol(0);
                                SendToSmartwatch(nr_patient, "Propofol");
                            }

                            send2UImessage(handler_2, WE0_AND_DPV, patientNumber);

                            if (patients_list.get(nr_patient).getPryvChecked()) {
                                streamId = CreatePryvStream("pulse_voltammetry", "PROPOFOL", connectionHashMap.get(nr_patient));
                                SendToPryv(measuredCurrentDouble, appliedVoltageDouble, streamId, "DPV", connectionHashMap.get(nr_patient));
                            }
                            saveDataToFile(measuredCurrentDouble, appliedVoltageDouble, 0, nr_patient);//WE0 = propofol
                        }


                        //WE1 and CA
                        if (resultCheckFirstByte == 21) {
                            myResult = SendValues(received_bytes);
                            measuredCurrentDouble = myResult.getCurrent();
                            appliedVoltageDouble = myResult.getVoltageOrTime();

                            //if for 10 time (consequently) current is over threshold send notification to the smartwatch
                            //for each patient
                            if (measuredCurrentDouble > thresholdForCA_Paracetamol) {
                                int jj2 = patients_list.get(nr_patient).getCounterMidazolam();
                                patients_list.get(nr_patient).setCounterMidazolam(jj2+1);
                                Log.d(TAG, "if CounterParacetamol: " + patients_list.get(nr_patient).getCounterMidazolam());
                            } else {
                                //else put the counter at zero
                               patients_list.get(nr_patient).setCounterMidazolam(0);
                                Log.d(TAG, "else CounterParacetamol: " + patients_list.get(nr_patient).getCounterMidazolam());
                            }

                            if (patients_list.get(nr_patient).getCounterMidazolam() == 10) {
                                patients_list.get(nr_patient).setCounterMidazolam(0);
                                SendToSmartwatch(nr_patient, "Paracetamol");
                            }

                            send2UImessage(handler_2, WE1_AND_CA, patientNumber);

                            if (time_start1 == -1) {
                                time_start1 = System.currentTimeMillis();
                            }

                            time = (System.currentTimeMillis() - time_start1) / 1000; //time in seconds
                            Log.d(TAG, "Time for CA we1: " + time + " // " + "time_start1: " + time_start1);

                            if (patients_list.get(nr_patient).getPryvChecked()) {
                                streamId = CreatePryvStream("amperometry_paracetamol", "PARACETAMOL", connectionHashMap.get(nr_patient));
                                SendToPryv((measuredCurrentDouble), time, streamId, "CA", connectionHashMap.get(nr_patient));
                            }
                            saveDataToFile(measuredCurrentDouble, appliedVoltageDouble, 1, nr_patient);//WE1 = midazolam
                        }


                        //WE1 and CV
                        if (resultCheckFirstByte == 22) {
                            myResult = SendValues(received_bytes);
                            measuredCurrentDouble = myResult.getCurrent();
                            appliedVoltageDouble = myResult.getVoltageOrTime();

                            //if for 10 time (consequently) current is over threshold send notification to the smartwatch
                            //for each patient
                            if (measuredCurrentDouble > thresholdForCV) {
                                int jj2 = patients_list.get(nr_patient).getCounterMidazolam();
                                patients_list.get(nr_patient).setCounterMidazolam(jj2+1);
                                Log.d(TAG, "if CounterParacetamol: " + patients_list.get(nr_patient).getCounterMidazolam());
                            } else {
                                //else put the counter at zero
                                patients_list.get(nr_patient).setCounterMidazolam(0);
                                Log.d(TAG, "else CounterParacetamol: " + patients_list.get(nr_patient).getCounterMidazolam());
                            }

                            if (patients_list.get(nr_patient).getCounterMidazolam() == 10) {
                                patients_list.get(nr_patient).setCounterMidazolam(0);
                                SendToSmartwatch(nr_patient, "Paracetamol");
                            }

                            send2UImessage(handler_2, WE1_AND_CV, patientNumber);

                            if (patients_list.get(nr_patient).getPryvChecked()) {
                                streamId = CreatePryvStream("cyclic_voltammetry", "PARACETAMOL", connectionHashMap.get(nr_patient));
                                SendToPryv(measuredCurrentDouble, appliedVoltageDouble, streamId, "CV", connectionHashMap.get(nr_patient));
                            }
                            saveDataToFile(measuredCurrentDouble, appliedVoltageDouble, 1, nr_patient);
                        }


                        //WE1 and DPV
                        if (resultCheckFirstByte == 23) {
                            myResult = SendValues(received_bytes);
                            measuredCurrentDouble = myResult.getCurrent();
                            appliedVoltageDouble = myResult.getVoltageOrTime();

                            //if for 10 time (consequently) current is over threshold send notification to the smartwatch
                            //for each patient
                            if (measuredCurrentDouble > thresholdForDPV) {
                                int jj2 = patients_list.get(nr_patient).getCounterMidazolam();
                                patients_list.get(nr_patient).setCounterMidazolam(jj2+1);
                                Log.d(TAG, "if CounterParacetamol: " + patients_list.get(nr_patient).getCounterMidazolam());
                            } else {
                                //else put the counter at zero
                                patients_list.get(nr_patient).setCounterMidazolam(0);
                                Log.d(TAG, "else CounterParacetamol: " + patients_list.get(nr_patient).getCounterMidazolam());
                            }


                            if (patients_list.get(nr_patient).getCounterMidazolam() == 10) {
                                patients_list.get(nr_patient).setCounterMidazolam(0);
                                SendToSmartwatch(nr_patient, "Paracetamol");
                            }

                            Log.d(TAG, "measuredCurrentDoubleDPV: " + measuredCurrentDouble + " " + "appliedVoltageDPV: " + appliedVoltageDouble);
                            send2UImessage(handler_2, WE1_AND_DPV, patientNumber);

                            if (patients_list.get(nr_patient).getPryvChecked()) {
                                streamId = CreatePryvStream("pulse_voltammetry", "PARACETAMOL", connectionHashMap.get(nr_patient));
                                SendToPryv(measuredCurrentDouble, appliedVoltageDouble, streamId, "DPV", connectionHashMap.get(nr_patient));
                            }
                            saveDataToFile(measuredCurrentDouble, appliedVoltageDouble, 1, nr_patient);
                        }


                        //WE2 and CA
                        if (resultCheckFirstByte == 31) {
                            myResult = SendValues(received_bytes);
                            measuredCurrentDouble = myResult.getCurrent();
                            appliedVoltageDouble = myResult.getVoltageOrTime();

                            //if for 10 time (consequently) current is over threshold send notification to the smartwatch
                            //for each patient
                            if (measuredCurrentDouble > thresholdForCA_Midazolam) {
                                int jj3 = patients_list.get(nr_patient).getCounterParacetamol();
                                patients_list.get(nr_patient).setCounterParacetamol(jj3+1);
                                Log.d(TAG, "if CounterMidazolam: " + patients_list.get(nr_patient).getCounterParacetamol());
                            } else {
                                //else put the counter at zero
                                patients_list.get(nr_patient).setCounterParacetamol(0);
                                Log.d(TAG, "else CounterMidazolam: " + patients_list.get(nr_patient).getCounterParacetamol());
                            }

                            if (patients_list.get(nr_patient).getCounterParacetamol() == 10) {
                                patients_list.get(nr_patient).setCounterParacetamol(0);
                                SendToSmartwatch(nr_patient, "Midazolam");
                            }

                            if (time_start2 == -1) {
                                time_start2 = System.currentTimeMillis();
                            }

                            time = (System.currentTimeMillis() - time_start2) / 1000; //time in seconds
                            Log.d(TAG, "measuredCurrentDoubleCA: " + measuredCurrentDouble + " " + "time: " + time);
                            send2UImessage(handler_2, WE2_AND_CA, patientNumber);

                            if (patients_list.get(nr_patient).getPryvChecked()) {
                                streamId = CreatePryvStream("amperometry_midazolam", "MIDAZOLAM", connectionHashMap.get(nr_patient));
                                SendToPryv((measuredCurrentDouble), time, streamId, "CA", connectionHashMap.get(nr_patient));
                            }
                            Log.d(TAG, "patient_nr: " + patientNumber);
                            saveDataToFile(measuredCurrentDouble, appliedVoltageDouble, 2, nr_patient);
                        }


                        //WE2 and CV
                        if (resultCheckFirstByte == 32) {
                            myResult = SendValues(received_bytes);
                            measuredCurrentDouble = myResult.getCurrent();
                            appliedVoltageDouble = myResult.getVoltageOrTime();

                            //if for 10 time (consequently) current is over threshold send notification to the smartwatch
                            //for each patient
                            if (measuredCurrentDouble > thresholdForCV) {
                                int jj3 = patients_list.get(nr_patient).getCounterParacetamol();
                                patients_list.get(nr_patient).setCounterParacetamol(jj3+1);
                                Log.d(TAG, "if CounterParacetamol: " + patients_list.get(nr_patient).getCounterParacetamol());
                            } else {
                                //else put the counter at zero
                                patients_list.get(nr_patient).setCounterParacetamol(0);
                                Log.d(TAG, "else CounterParacetamol: " + patients_list.get(nr_patient).getCounterParacetamol());
                            }

                            if (patients_list.get(nr_patient).getCounterParacetamol() == 10) {
                                patients_list.get(nr_patient).setCounterParacetamol(0);
                                SendToSmartwatch(nr_patient, "Midazolam");
                            }

                            send2UImessage(handler_2, WE2_AND_CV, patientNumber);

                            if (patients_list.get(nr_patient).getPryvChecked()) {
                                streamId = CreatePryvStream("cyclic_voltammetry", "MIDAZOLAM", connectionHashMap.get(nr_patient));
                                SendToPryv(measuredCurrentDouble, appliedVoltageDouble, streamId, "CV", connectionHashMap.get(nr_patient));
                            }
                            saveDataToFile(measuredCurrentDouble, appliedVoltageDouble, 2, nr_patient);
                        }


                        //WE2 and DPV
                        if (resultCheckFirstByte == 33) {
                            myResult = SendValues(received_bytes);
                            measuredCurrentDouble = myResult.getCurrent();
                            appliedVoltageDouble = myResult.getVoltageOrTime();

                            //if for 10 time (consequently) current is over threshold send notification to the smartwatch
                            //for each patient
                            if (measuredCurrentDouble > thresholdForDPV) {
                                int jj3 = patients_list.get(nr_patient).getCounterParacetamol();
                                patients_list.get(nr_patient).setCounterParacetamol(jj3+1);
                                Log.d(TAG, "if CounterParacetamol: " + patients_list.get(nr_patient).getCounterParacetamol());
                            } else {
                                //else put the counter at zero
                                patients_list.get(nr_patient).setCounterParacetamol(0);
                                Log.d(TAG, "else CounterParacetamol: " + patients_list.get(nr_patient).getCounterParacetamol());
                            }

                            if (patients_list.get(nr_patient).getCounterParacetamol() == 10) {
                                patients_list.get(nr_patient).setCounterParacetamol(0);
                                SendToSmartwatch(nr_patient, "Paracetamol");
                            }

                            send2UImessage(handler_2, WE2_AND_DPV, patientNumber);

                            if (patients_list.get(nr_patient).getPryvChecked()) {
                                streamId = CreatePryvStream("pulse_voltammetry", "MIDAZOLAM", connectionHashMap.get(nr_patient));
                                SendToPryv(measuredCurrentDouble, appliedVoltageDouble, streamId, "DPV", connectionHashMap.get(nr_patient));
                            }
                            saveDataToFile(measuredCurrentDouble, appliedVoltageDouble, 2, nr_patient);
                        }


                    } else {
                        Log.d(TAG, "in is null");
                        Thread1 = new Thread1(handler_2, false, startMeasurement);
                        Thread1.start();
                        return;
                    }

                } catch (IOException e) {
                    Log.d(TAG, "IOException");
                    e.printStackTrace();
                }
            }
            Looper.loop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            for (int i=0; i < nr_patient; i++) {
                socketHashMap.get(i).close();
                Log.d(TAG, "Socket chiuso, nr_patient: " + i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        GraphActivity.chart1.clearValues();
        GraphActivity.chart2.clearValues();
        GraphActivity.chart3.clearValues();
        Toast.makeText(this, "My Service Destroyed", Toast.LENGTH_LONG).show();
    }


    //Controlling which working electrode (so which drug) is choosen.
    public static int CheckFirstByte(byte data, Handler handler_2) {
        int firstByte = data & 0xFF;

        //WE0
        if ((!getBit(2, firstByte)) && (!getBit(1, firstByte)) && (!getBit(0, firstByte))) { //000 --> WE0
            Log.d("DATA RECEIVED", "WE SELECTED: " + "electrode 0 --> PROPOFOL");

            if ((getBit(7, firstByte)) && (!getBit(6, firstByte)) && (getBit(5, firstByte)) && (!getBit(4, firstByte))) {
                Log.d("DATA RECEIVED", "TECHNIQUE SELECTED: " + "CA");
                return (WE0_AND_CA);

            } else if ((getBit(7, firstByte)) && (getBit(6, firstByte)) && (getBit(5, firstByte)) && (getBit(4, firstByte))) {
                Log.d("DATA RECEIVED", "TECHNIQUE SELECTED: " + "CV");
                return (WE0_AND_CV);

            } else if ((getBit(7, firstByte)) && (getBit(6, firstByte)) && (!getBit(5, firstByte)) && (getBit(4, firstByte))) {
                Log.d("DATA RECEIVED", "TECHNIQUE SELECTED: " + "DPV");
                return (WE0_AND_DPV);
            }
        }


        //WE1
        else if ((!getBit(2, firstByte)) && (!getBit(1, firstByte)) && (getBit(0, firstByte))) { //001 --> WE1
            Log.d("DATA RECEIVED", "WE SELECTED: " + "electrode 1 --> PARACETAMOL");

            if ((getBit(7, firstByte)) && (!getBit(6, firstByte)) && (getBit(5, firstByte)) && (!getBit(4, firstByte))) {
                Log.d("DATA RECEIVED", "TECHNIQUE SELECTED: " + "CA");
                return (WE1_AND_CA);

            } else if ((getBit(7, firstByte)) && (getBit(6, firstByte)) && (getBit(5, firstByte)) && (getBit(4, firstByte))) {
                Log.d("DATA RECEIVED", "TECHNIQUE SELECTED: " + "CV");
                return (WE1_AND_CV);

            } else if ((getBit(7, firstByte)) && (getBit(6, firstByte)) && (!getBit(5, firstByte)) && (getBit(4, firstByte))) {
                Log.d("DATA RECEIVED", "TECHNIQUE SELECTED: " + "DPV");
                return (WE1_AND_DPV);
            }
        }

        //WE2
        else if ((!getBit(2, firstByte)) && (getBit(1, firstByte)) && (!getBit(0, firstByte))) { //010 --> WE2
            Log.d("DATA RECEIVED", "WE SELECTED: " + "electrode 2 --> MIDAZOLAM");

            if ((getBit(7, firstByte)) && (!getBit(6, firstByte)) && (getBit(5, firstByte)) && (!getBit(4, firstByte))) {
                Log.d("DATA RECEIVED", "TECHNIQUE SELECTED: " + "CA");
                return (WE2_AND_CA);
            } else if ((getBit(7, firstByte)) && (getBit(6, firstByte)) && (getBit(5, firstByte)) && (getBit(4, firstByte))) {
                Log.d("DATA RECEIVED", "TECHNIQUE SELECTED: " + "CV");
                return (WE2_AND_CV);
            } else if ((getBit(7, firstByte)) && (getBit(6, firstByte)) && (!getBit(5, firstByte)) && (getBit(4, firstByte))) {
                Log.d("DATA RECEIVED", "TECHNIQUE SELECTED: " + "DPV");
                return (WE2_AND_DPV);
            }

        }
        return 0;
    }

    public static boolean getBit(int position, int data) {
        return (data & (1 << position)) != 0;
    }

    //Method for reading current and voltage data and converting them into double
    public static MyResult SendValues(byte[] byteReceived) {

        //MEASURED CURRENT/VOLTAGE
        byte measured_Voltage1 = byteReceived[1];
        byte measured_Voltage2 = byteReceived[2];
        Log.d(TAG, "bytereceived: " + byteReceived[0] + " " + byteReceived[1]);
        int measuredCurrent = (0xFF & measured_Voltage2) * 256 + (0xFF & measured_Voltage1); //decimal value
        double current = ((measuredCurrent * 1.0 / DAC_SCALE * DAC_REF) - DAC_OFFSET) / 10;

        //APPLIED VOLTAGE
        byte applied_Voltage1 = byteReceived[3];
        byte applied_Voltage2 = byteReceived[4];
        Log.d(TAG, "bytereceived2: " + byteReceived[2] + " " + byteReceived[3]);
        int applied_Voltage = (0xFF & applied_Voltage2) * 256 + (0xFF & applied_Voltage1); //decimal value
        double voltage = (applied_Voltage * 1.0 / DAC_SCALE * DAC_REF) - DAC_OFFSET;

        MyResult myResult = new MyResult(current, voltage, nr_patient);
        return myResult;
    }

    public static class MyResult {
        double current;
        double voltage;
        int nr_patient;
        int threshold;


        public MyResult(double currentDouble, double voltageDouble, int nr_patient) {
            this.current = currentDouble;
            this.voltage = voltageDouble;
            this.nr_patient = nr_patient;
            //this.threshold = threshold;
        }

        public double getCurrent() {
            return this.current;
        }

        public double getVoltageOrTime() {
            return this.voltage;
        }

        public int getNrPatient() {
            return this.nr_patient;
        }

        //public int getThreshold() {return this.threshold; }

    }


    //
    //----------------- PRYV ------------------
    //

    public static Connection CreatePryvConnection(String pryvUsername, String pryvToken) {
        connection = new Connection(context, pryvUsername, pryvToken, "pryv.me", true, new DBinitCallback());
        return connection;
    }

    public static String CreatePryvStream(String streamId, String streamName, Connection connection) {

        //Creating Pryv Stream
        Stream testStream = new Stream(streamId, streamName);
        Filter scope = new Filter();
        scope.addStream(testStream);
        connection.setupCacheScope(scope);
        connection.streams.create(testStream, new StreamsCallback() {

            @Override
            public void onApiSuccess(String successMessage, Stream stream, Double serverTime) {
                Log.i("Pryv", successMessage);
            }

            @Override
            public void onApiError(String errorMessage, Double serverTime) {
                Log.i("Pryv", errorMessage);
            }

            @Override
            public void onCacheSuccess(String successMessage, Stream stream) {
                Log.i("Pryv", successMessage);
            }

            @Override
            public void onCacheError(String errorMessage) {
                Log.i("Pryv", errorMessage);
            }
        });

        return streamId;
    }

    public static void SendToPryv(double currentReceived, double xAxisReceived, String streamIdReceived, String technique, Connection connection) {

        Event events = new Event();
        events.setStreamId(streamIdReceived);
        if (technique.equals("PERSONAL_DATA")) {
            events.setType("note/txt");

            JSONObject objectData = new JSONObject();

            try {
                objectData.put("Name", patients_list.get(nr_patient).getName());
                objectData.put("Surname", patients_list.get(nr_patient).getSurname());
                objectData.put("Gender", patients_list.get(nr_patient).getGender());
                objectData.put("Place of birth", patients_list.get(nr_patient).getPlaceOfBirth());
                objectData.put("Date of birth", patients_list.get(nr_patient).getDateOfBirth());
                objectData.put("Number", patients_list.get(nr_patient).getNumber());

                Log.d(TAG, "SendPersonalDataToPryv: " + "Name: " + objectData.get("Name") + " // " + "Surname: " + objectData.get("Surname") +
                        " // " + "Gender: " + objectData.get("Gender") + " // " + "PlaceOfBirth: " + objectData.get("Place of birth"));

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "SendPersonalDataToPryv_EXCEPTION");
            }
            String Data = objectData.toString();
            events.setContent(Data);

        } else if (technique.equals("CA")) {
            events.setType("electric-current/a");
            //events.setTime(xAxisReceived);
            events.setContent(currentReceived);
            Log.d(TAG, "PRYV CA" + " // " + "nr_patient: " + nr_patient);

        } else if (technique.equals("DPV") || technique.equals("CV")) {
            events.setType("numset/voltammetry");
            Log.d(TAG, "SONO IN DPV CV PRYV");
            JSONObject object = new JSONObject();

            try {
                object.put("current",currentReceived);
                object.put("voltage",xAxisReceived);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String Data = object.toString();
            events.setContent(Data);
        }

        connection.events.create(events, new EventsCallback() {

                @Override
            public void onApiSuccess(String successMessage, Event event, String stoppedId, Double serverTime) {
                Log.i("Pryv", successMessage);
            }

            @Override
            public void onApiError(String errorMessage, Double serverTime) {
                Log.i("Pryv", errorMessage);
            }

            @Override
            public void onCacheSuccess(String successMessage, Event event) {
                Log.i("Pryv", successMessage);
            }

            @Override
            public void onCacheError(String errorMessage) {
                Log.i("Pryv", errorMessage);
            }

        });
    }

    //
    // ----------- SEND NOTIFICATION TO THE SMARTWATCH ----------------------
    //
    public static void SendToSmartwatch (int nr_patient, String drug) {
        int notificationId = 001;
        String patientName = patients_list.get(nr_patient).getName() + " " + patients_list.get(nr_patient).getSurname();
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Alarm")
                        .setContentText("out of range " + " " + patientName + " " + "On " + drug);

        notificationBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000});

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }




    //
    // ----------- SAVE DATA INTO THE EXTERNAL STORAGE ----------------------
    //

    public static void saveDataToFile(Double current, Double voltage, int WE, int patient_nr) { //cambiare nome WE con anestetico

        //at the first time all the file are opened to save data from 3 electrodes for each patient
        switch(patient_nr){
            case 0:
                if (fileStream0 == null) {
                    for (int i = 0; i < 3; i++) {
                        Log.d(TAG, "creating files_Patient0");
                        fileStream0 = saveFileStart(i, fileStream0, patient_nr);
                    }

                } else {
                    addLine2File(fileStream0, current, voltage, WE);
                   // Log.d(TAG, "addLine2file_Patient0");
                }
                break;

            case 1:
                if (fileStream1 == null) {
                    for (int i = 0; i < 3; i++) {
                        Log.d(TAG, "creating files_Patient1");
                        fileStream1 = saveFileStart(i, fileStream1, patient_nr);
                    }
                } else {
                    addLine2File(fileStream1, current, voltage, WE);
                   // Log.d(TAG, "addLine2file_Patient1");
                }
                break;

            case 2:
                if (fileStream2 == null) {
                    for (int i = 0; i < 3; i++) {
                        Log.d(TAG, "creating files_Patient2");
                        fileStream2 = saveFileStart(i, fileStream2, patient_nr);
                    }
                } else {
                    addLine2File(fileStream2, current, voltage, WE);
                    //Log.d(TAG, "addLine2file_Patient2");
                }
                break;
        }
    }

    public static void addLine2File(ArrayList<FileOutputStream> fileStream, Double current, Double voltage, int WE){
        String newRow; // Row to be written into the file
        //Log.d(TAG,"add a line");
        newRow = String.format(Locale.ROOT, "%5.7f\t%5.7f\n", voltage, current);

        try {
            fileStream.get(WE).write(newRow.getBytes());
        } catch (IOException e) {
            Log.d(TAG, "EXCEPTION: " + "Impossible to write recording");
            e.printStackTrace();
        }
    }

    public static ArrayList<FileOutputStream> saveFileStart(int nrOfWe, ArrayList<FileOutputStream> fileStream_vect, int patient_nr) {

        String fileName;
        String nameOfWe = null;
        File file;
        ArrayList<FileOutputStream> fileStream = fileStream_vect;
        if(fileStream==null) {
            fileStream = new ArrayList<FileOutputStream>();
        }

        //Log.d(TAG, "savefilestart");

        // Checking media availability.
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d(TAG, "EXCEPTION: " + "External Storage not available");
            return null;
        }

        String now = new SimpleDateFormat("dd_MM_yy").format(new Date());
        Log.d(TAG,"data " + now);
        File AppFolderInt = new File(Environment.getExternalStorageDirectory().toString(), "/AnControl/" + patients_list.get(patient_nr).getName() + "_" + patients_list.get(patient_nr).getSurname() + "/" + now);
        if (!AppFolderInt.exists()) {
            AppFolderInt.mkdirs();
            Log.d(TAG, "FOLDER_int CREATED " + AppFolderInt);
        }

        // Create File.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        switch(nrOfWe){
            case 0: nameOfWe="Propofol"; break;
            case 1: nameOfWe="Paracetamol"; break;
            case 2: nameOfWe="Midazolam"; break;
        }

        if(nameOfWe != null) {
            fileName = nameOfWe + "_" + sdf.format(new Date()) + ".csv";
            file = new File(AppFolderInt, fileName); // Create a file inside the root of application's file directory.
            Log.d(TAG, "FILE CREATED: " + fileName);

            //Open the file stream
            try {
                FileOutputStream fileToAdd = new FileOutputStream(file);
                if (!fileStream.contains(fileToAdd)) {
                    //Log.d(TAG, "file added to filestreamArray " + fileToAdd);
                    fileStream.add(fileToAdd); // Output stream ready to write the file.
                }

            } catch (IOException e) {
                //Log.d(TAG,"file added to filestreamArray ERROR");
            }
        }
        return fileStream;
    }
}

