package nadia.epfl.com.ancontrol;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class AddIpAddress extends AppCompatActivity implements View.OnClickListener {

    private EditText ipName;
    private EditText ipAddress;
    private EditText ipPort;
    private Button ipButton;
    private Button backButton;
    static final String TAG = "AddIpAddress";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ip_address);

        ipName = (EditText) findViewById(R.id.editText_Name);
        ipAddress = (EditText) findViewById(R.id.editText_IpAddress);
        ipPort = (EditText) findViewById(R.id.editText_IpPort);
        ipButton = (Button) findViewById(R.id.button_Ok);
        backButton = (Button) findViewById(R.id.button_Back);

        DisplayMetrics dmIp = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dmIp);
        int width = dmIp.widthPixels;
        int heigth = dmIp.heightPixels;
        getWindow().setLayout((int) (width * .4), (int) (heigth * .4));

        ipButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_Ok:
                String Name = ipName.getText().toString();
                String IpAddress = ipAddress.getText().toString();
                int Port = Integer.parseInt(ipPort.getText().toString());

                if (!(Name.isEmpty() || IpAddress.isEmpty() || ipPort.getText().equals(""))) {
                    Intent in = new Intent();
                    in.putExtra("NAME", Name);
                    in.putExtra("IP", IpAddress);
                    in.putExtra("PORT", Port);
                    setResult(Pop.RESULT_SUCCESS, in);
                    finish();
                } else {
                    Toast.makeText(AddIpAddress.this, "Please fill all the fields!", Toast.LENGTH_LONG).show();
                }

                break;

            case R.id.button_Back:
                Intent in = new Intent();
                setResult(3,in);
                finish();
                break;

            default:
                break;
        }

    }
}
