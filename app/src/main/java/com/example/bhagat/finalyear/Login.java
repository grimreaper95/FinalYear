package com.example.bhagat.finalyear;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    SharedPreferences sp;
    public static Context context;
    TextView signup;
    EditText mobileNo, password;
    Button login;
    RadioButton provider;
    ProgressDialog pDialog;
    boolean loggdedIn = false;
    boolean logInSuccessful = false;
    ArrayList<ListData> arrayOfItems;
    String userType;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        mobileNo = (EditText) findViewById(R.id.mobile_no);
        password = (EditText) findViewById(R.id.password);
        signup = (TextView) findViewById(R.id.signup);
        login = (Button) findViewById(R.id.login);
        provider = (RadioButton) findViewById(R.id.login_provider);

        signup.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        arrayOfItems = new ArrayList<>();
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
            }
        });
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor  = sp.edit();
        loggdedIn = sp.getBoolean("loggedin", false);

        /*This is used to keep the user logged in when the next time the user opens the app*/
        if (loggdedIn) {
            String userType = sp.getString("userType","guest");
            if (userType.equals("provider")) {
                editor.putString("username",UserDetails.getInstance().userName);
                startActivity(new Intent(this, ProviderHome.class));
            } else {
                startActivity(new Intent(this, ConsumerHome.class));
            }
            finish();
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mobileNo.getText().toString().length() == 0 || password.getText().toString().length() == 0) {
                    if (mobileNo.length() == 0) {
                        Toast.makeText(Login.this, "Please enter your mobile no", Toast.LENGTH_LONG).show();
                    } else if (password.length() == 0) {
                        Toast.makeText(Login.this, "Please enter your password", Toast.LENGTH_LONG).show();
                    }
                } else {
                    loginClicked();
                    if (logInSuccessful) {

                    } else {
                        Toast.makeText(Login.this, "Sorry invalid username or password. Please try again.", Toast.LENGTH_LONG);
                    }
                }
            }
        });
    }

    public void loginClicked() {
        String url = "http://192.168.109.41/login.php";
        com.android.volley.RequestQueue queue = Volley.newRequestQueue(this);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Logging in...");
        pDialog.show();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.dismiss();
                        Log.d("login response", response);
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if(jsonArray.length() == 0){
                                Toast.makeText(Login.this, "Incorrect login credentials. Try again.", Toast.LENGTH_LONG).show();
                            }
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jOb = jsonArray.getJSONObject(i);
                                arrayOfItems.add(new ListData(jOb));
                                String user_name = arrayOfItems.get(i).jOb.getString("user_name");
                                String user_id = arrayOfItems.get(i).jOb.getString("user_id");

                                if(provider.isChecked()) {
                                    String isAvailable = arrayOfItems.get(i).jOb.getString("available");
                                    boolean available ;
                                    if(isAvailable.equals("1")){
                                        available = true;
                                    }
                                    else{
                                        available = false;
                                    }
                                    editor.putBoolean("available",available);
                                    editor.commit();
                                }

                                int row_count = arrayOfItems.get(i).jOb.getInt("row_count");
                                if (row_count != 1) {
                                    logInSuccessful = false;
                                    Toast.makeText(Login.this, "Incorrect login credentials. Try again.", Toast.LENGTH_LONG).show();
                                } else {
                                    editor.putString("username",user_name);
                                    editor.commit();
                                    UserDetails.getInstance().userName = user_name;
                                    UserDetails.getInstance().userId = user_id;
                                    UserDetails.getInstance().consumerId = user_id;
                                    UserDetails.getInstance().providerId = user_id;
                                    UserDetails.getInstance().isProvider = true;
                                    if (provider.isChecked()) {
                                        userType = "provider";
                                    } else {
                                        userType = "consumer";
                                    }
                                    logInSuccessful = true;
                                    Toast.makeText(Login.this, "Login credentials correct" + UserDetails.getInstance().consumerId, Toast.LENGTH_LONG).show();
                                    editor.putBoolean("loggedin", true);
                                    editor.commit();
                                    if (userType.equals("provider")) {
                                        editor.putString("userType", "provider");
                                        editor.commit();
                                        startActivity(new Intent(Login.this, ProviderHome.class));
                                    } else {
                                        editor.putString("userType", "consumer");
                                        editor.commit();
                                        startActivity(new Intent(Login.this, ConsumerHome.class));
                                    }
                                    finish();
                                }
                            }
                        } catch (Exception e) {
                            Log.d("login json error", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d("login error", error.getMessage() + " ");
                        pDialog.hide();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_type", (provider.isChecked() ? "provider" : "consumer"));
                params.put("mobile_no", mobileNo.getText().toString());
                params.put("password", password.getText().toString());
                return params;
            }
        };
        queue.add(postRequest);
    }
}