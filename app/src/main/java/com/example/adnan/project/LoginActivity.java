package com.example.adnan.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

//    public static final String USER_PREF = "user_preference_file";
    private static final int TIME_OUT = 5000;
    private EditText username;
    private EditText password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        getSupportActionBar().hide();

        Button login = (Button) findViewById(R.id.button);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = ((EditText) findViewById(R.id.text_matric)).getText().toString();
                final String password = ((EditText) findViewById(R.id.text_pass)).getText().toString();

                if(username.length()>0 && password.length()>0) {

                    new FetchTokenTask().execute(username, password);

                    SharedPreferences sp = getSharedPreferences(getResources().getString(R.string.user_shared_preference), Context.MODE_PRIVATE);
                    String token = sp.getString("token", "");

                    new FetchUserDetailTask().execute(token);

                    //got to the courses for the student
                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    public class FetchTokenTask extends AsyncTask<String, Void, String>{

        private ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute(){
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            if(params.length<2) return null;
            HttpURLConnection con = null;
            BufferedReader reader = null;

            String response = null;

            try{
                Uri.Builder builder = new Uri.Builder();
                builder .scheme("http")
                        .encodedAuthority(getResources().getString(R.string.server_ip))
                        .appendPath("moodle").appendPath("login").appendPath("token.php")
                        .appendQueryParameter("username", params[0])
                        .appendQueryParameter("password", params[1])
                        .appendQueryParameter("service", "moodle_mobile_app");

                String strURL = builder.build().toString();

                URL url = new URL(strURL);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.connect();

                InputStream inputStream = con.getInputStream();

                if(inputStream==null) return null;

                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer buffer = new StringBuffer();

                String line;
                while((line = reader.readLine())!=null){
                    buffer.append(line+"\n");
                }

                if(buffer.length()==0) return null;

                response = buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response){

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            try {
                if (response != null) {
                    JSONObject obj = new JSONObject(response);
                    SharedPreferences shp = getSharedPreferences(getResources().getString(R.string.user_shared_preference), 0);
                    SharedPreferences.Editor editor = shp.edit();

                    editor.putString("token", obj.get("token").toString());

                    editor.commit();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class FetchUserDetailTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            if(params.length<1) return null;
            HttpURLConnection con = null;
            BufferedReader reader = null;

            String response = null;

            try{
                Uri.Builder builder = new Uri.Builder();
                builder .scheme("http")
                        .encodedAuthority(getResources().getString(R.string.server_ip))
                        .appendPath("moodle").appendPath("webservice").appendPath("rest").appendPath("server.php")
                        .appendQueryParameter("wstoken", params[0])
                        .appendQueryParameter("wsfunction", "core_webservice_get_site_info")
                        .appendQueryParameter("moodlewsrestformat", "json");

                String strURL = builder.build().toString();

                URL url = new URL(strURL);

                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.connect();

                InputStream inputStream = con.getInputStream();

                if(inputStream==null) return null;

                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer buffer = new StringBuffer();

                String line;
                while((line = reader.readLine())!=null){
                    buffer.append(line+"\n");
                }

                if(buffer.length()==0) return null;

                response = buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response){
            try {
                if (response != null) {
                    JSONObject obj = new JSONObject(response);
//                    HashMap<String, String> user = new HashMap<String, String>();

//                    user.put("fullname", obj.get("fullname").toString());
//                    user.put("userid", obj.get("userid").toString());
//                    user.put("profile_pic", obj.get("userpictureurl").toString());

                    SharedPreferences shp = getSharedPreferences(getResources().getString(R.string.user_shared_preference), 0);
                    SharedPreferences.Editor editor = shp.edit();

                    editor.putString("user_detail_json", obj.toString());

                    editor.commit();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
