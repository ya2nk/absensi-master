package com.waroengweb.absensi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.tapadoo.alerter.Alerter;
import com.waroengweb.absensi.app.AppController;
import com.waroengweb.absensi.helpers.Session;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    AwesomeValidation awesomeValidation;
    EditText username,password;
    Button buttonLogin;
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        buttonLogin = (Button)findViewById(R.id.btn_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLogin();
            }
        });

        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");

        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(LoginActivity.this,R.id.username, RegexTemplate.NOT_EMPTY,R.string.required);
        awesomeValidation.addValidation(LoginActivity.this,R.id.password, RegexTemplate.NOT_EMPTY,R.string.required);
    }

    private void doLogin()
    {
        if (awesomeValidation.validate()) {
            pd.show();
            String url = "https://erk.kerincikab.go.id/";
            StringRequest postRequest = new StringRequest(Request.Method.POST, url+"api/absen-login",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                           pd.dismiss();
                            // response
                            Log.d("Response", response);
                            if (response.equals("success")){
                                Session.setLoginStatus(LoginActivity.this, true);
                                Session.setNip(LoginActivity.this,username.getText().toString());
                                Intent intent = new Intent(LoginActivity.this, PesanActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Alerter.create(LoginActivity.this)
                                        .setTitle("ERROR")
                                        .setText(response)
                                        .setBackgroundColorInt(Color.RED).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            pd.dismiss();
                            Log.d("Error.Response", error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("username",username.getText().toString());
                    params.put("password",password.getText().toString());
                    return params;
                }
            };
            AppController.getInstance().addToRequestQueue(postRequest);
        }

    }
}
