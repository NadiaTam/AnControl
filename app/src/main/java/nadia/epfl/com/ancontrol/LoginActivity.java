package nadia.epfl.com.ancontrol;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private TextView attempts;
    private Button login_btn;
    private RadioButton radioDoctor;
    private RadioButton radioTechnician;
    private int checkDoctorOrTechnician;

    int attempt_counter = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.editText_username);
        password = (EditText) findViewById(R.id.editText_password);
        attempts = (TextView) findViewById(R.id.attempts_counter);
        login_btn = (Button) findViewById(R.id.button_login);
        //radioDoctor = findViewById(R.id.radioButton_Doctor);
        //radioTechnician = findViewById(R.id.radioButton_Technician);

        LoginButton();
    }

    public void LoginButton() {

        //vogliamo settare il count nell'attempt count box.
        attempts.setText(Integer.toString(attempt_counter));

        //aggiungo l'evento click sul button
        login_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

               /* if (!(radioDoctor.isChecked() || radioTechnician.isChecked())) {  //NOR, se non e' selezionato un radiobutton
                    Toast.makeText(LoginActivity.this, "Fill all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }*/

                //controllo se username e password sono uguali a dei valori
                if(username.getText().toString().equals("user") && password.getText().toString().equals("pass")) {

                    /*A toast provides simple feedback about an operation in a small popup.
                    It only fills the amount of space required for the message and the current
                    activity remains visible and interactive. */
                    Toast.makeText(LoginActivity.this, "User and Password is correct", Toast.LENGTH_SHORT).show();

                    //if (radioDoctor.isChecked()) {
                        //checkDoctorOrTechnician = 1;
                        Intent intent = new Intent(LoginActivity.this, PatientActivity.class);
                        ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0,
                                0, v.getWidth(), v.getHeight());
                        startActivity(intent, options.toBundle());
                    /*} else if (radioTechnician.isChecked()) {
                        checkDoctorOrTechnician = 2;
                        Intent intent = new Intent(LoginActivity.this, PryvLoginActivity.class);
                        intent.putExtra("TECHNICIAN", checkDoctorOrTechnician);
                        ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0,
                                0, v.getWidth(), v.getHeight());
                        startActivity(intent, options.toBundle());
                    }*/

                } else {

                    Toast.makeText(LoginActivity.this, "User and Password is not correct", Toast.LENGTH_SHORT).show();

                    //riduci il numero del counter
                    attempt_counter--;

                    //risetta il valore nel box.
                    attempts.setText(Integer.toString(attempt_counter));

                    if(attempt_counter == 0) {
                        //se il counter è a zero disabilita il bottone
                        login_btn.setEnabled(false);
                    }
                }
            }
        });
    }

}


