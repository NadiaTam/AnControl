package nadia.epfl.com.ancontrol;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.ArrayList;

public class PatientActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText name;
    private EditText surname;
    private EditText dateOfBirth;
    private EditText placeOfBirth;
    private EditText number;
    private RadioButton male;
    private RadioButton female;
    private CheckBox pryv;
    private Button buttonNext;
    private ArrayList<Patients> patients_list;
    private Patients patient;
    private int numberOfPatients =1;

    static final String TAG = "PatientActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        name = (EditText) findViewById(R.id.editText_Name);
        surname = (EditText) findViewById(R.id.editText_Surname);
        dateOfBirth = (EditText) findViewById(R.id.editText_Age);
        placeOfBirth = (EditText) findViewById(R.id.editText_PlaceBirth);
        number = (EditText) findViewById(R.id.editText_Number);
        male = (RadioButton) findViewById(R.id.radioButton_M);
        female = (RadioButton) findViewById(R.id.radioButton_F);
        pryv = (CheckBox) findViewById(R.id.checkBox_Pryv);
        buttonNext = (Button) findViewById(R.id.button_Next);

        buttonNext.setOnClickListener(this);
        patients_list = new ArrayList<>();

        numberOfPatients = getIntent().getIntExtra("NUMBER_OF_PATIENTS", 1);
        if (numberOfPatients != 1) {
            patients_list = getIntent().getParcelableArrayListExtra("INFOPATIENTS");
        }
            patient = new Patients();
    }

    @Override
    public void onClick(View v) {

        if (isEmptyField(name)) return;
        if (isEmptyField(surname)) return;
        if (isEmptyField(dateOfBirth)) return;
        if (isEmptyField(placeOfBirth)) return;
        if (isEmptyField(number)) return;
        if (!(male.isChecked() || female.isChecked())) {
            Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        patient.setName(name.getText().toString());
        patient.setSurname(surname.getText().toString());
        patient.setNumber(number.getText().toString());
        patient.setPlaceOfBirth(placeOfBirth.getText().toString());
        patient.setDateOfBirth(dateOfBirth.getText().toString());
        patient.setFirst_timeSocket(true);
        patient.setIPserver("null");
        patient.setPORTserver(0);
        patient.setStatusConnection(false);

        if (male.isChecked()) {
            patient.setGender("M");
        } else if (female.isChecked()) {
            patient.setGender("F");
        }
        if (pryv.isChecked()) {
            patient.setPryvChecked(true);
        } else {
            patient.setPryvChecked(false);
        }
        patients_list.add(patient);

        Log.d(TAG, "PATIENT_ACTIVITY" + " " + "patients: " + patients_list.get(0) + " " + "name: " + patients_list.get(0).getName() + " " + "surname: " + patients_list.get(0).getSurname());

        if (pryv.isChecked()) {
            Intent in = new Intent(this, PryvLoginActivity.class);
            in.putExtra("NUMBER_OF_PATIENTS", patients_list.size());
            in.putParcelableArrayListExtra("PATIENTS", patients_list);
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0,
                    0, v.getWidth(), v.getHeight());
            startActivity(in, options.toBundle());

        } else {
            Intent in = new Intent(this, GraphActivity.class);
            in.putExtra("FROM", "PatientActivity");
            in.putExtra("NUMBER_OF_PATIENTS", patients_list.size());
            in.putParcelableArrayListExtra("PATIENTINFO", patients_list);
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0,
                    0, v.getWidth(), v.getHeight());
            startActivity(in, options.toBundle());
        }
    }

    private boolean isEmptyField (EditText editText) {
        boolean result = editText.getText().toString().length() <= 0;
        if (result)
            Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show();
        return result;
    }
}
