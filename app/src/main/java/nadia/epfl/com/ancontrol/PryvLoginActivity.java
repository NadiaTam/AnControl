package nadia.epfl.com.ancontrol;

import com.pryv.model.Permission;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.pryv.Pryv;
import com.pryv.auth.AuthController;
import com.pryv.auth.AuthView;
import java.util.ArrayList;

public class PryvLoginActivity extends AppCompatActivity {

    private String webViewUrl;
    private WebView webView;
    private Permission creatorPermission = new Permission("*", Permission.Level.manage, "Creator");
    private ArrayList<Permission> permissions;
    private ArrayList<Patients> patients;
    private Patients patient;
    private Credentials credentials;
    public final static String DOMAIN = "pryv.me";
    public final static String APPID = "AnControl";
    private String errorMessage = "Unknown error";
    private Intent intentFromPatientActivity;
    private int numberOfPatients;
    static final String TAG = "PryvLoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pryv_login);

        webView = (WebView) findViewById(R.id.webview);

        //patients = new ArrayList<>();
        intentFromPatientActivity = getIntent();
        patients = intentFromPatientActivity.getParcelableArrayListExtra("PATIENTS");
        numberOfPatients = intentFromPatientActivity.getIntExtra("NUMBER_OF_PATIENTS", 1);

        Log.d(TAG, "PRYV_LOGIN_ACTIVITY" + " " + "patients: " + patients.size() + " " + "name: " + patients.get(0).getName() + " " + "surname: " + patients.get(0).getSurname() +
                " " + "number" + patients.get(0).getNumber() + " " + "token" + patients.get(0).getToken() + " " + "username" + patients.get(0).getUsername());

        credentials = new Credentials(this);
        credentials.resetCredentials();

        Pryv.setDomain(DOMAIN);
        permissions = new ArrayList<>();
        permissions.add(creatorPermission);
        new SigninAsync().execute();
    }

    private class SigninAsync extends AsyncTask<Void,Void,Void> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PryvLoginActivity.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            AuthController authenticator = new AuthController(APPID, permissions, null, null, new CustomAuthView());
            authenticator.signIn();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            if (webViewUrl != null) {
                webView.requestFocus(View.FOCUS_DOWN);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setUseWideViewPort(true);
                webView.loadUrl(webViewUrl);
            }
        }
    }

    private class CustomAuthView implements AuthView {

        @Override
        // Set up the WebView url when we get it from AuthController
        public void displayLoginView(String loginURL) {
            webViewUrl = loginURL;
        }

        @Override
        // Save the credentials if authentication succeeds
        public void onAuthSuccess(String username, String token) {
            credentials.setCredentials(username,token);
            setResult(RESULT_OK, new Intent());
            finish();
            Intent inNextActivity = new Intent(PryvLoginActivity.this,PryvTokenActivity.class);
            inNextActivity.putExtra("NUMBER_OF_PATIENTS", patients.size());
            inNextActivity.putParcelableArrayListExtra("INFOPATIENT", patients);
            startActivity(inNextActivity);
        }

        @Override
        // Set up error messages if authentication fails
        public void onAuthError(String msg) {
            errorMessage = msg;
        }

        @Override
        // Set up error messages if authentication is refused
        public void onAuthRefused(int reasonId, String msg, String detail) {
            errorMessage = msg;
        }
    }
}
