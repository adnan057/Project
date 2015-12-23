package com.example.adnan.project;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CourseMaterialFragment extends Fragment {

    private ArrayList<ContentForListView> items = null;
    private ContentListAdapter contentListAdapter;

    public CourseMaterialFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View content_view = inflater.inflate(R.layout.fragment_course_material, container, false);

        Intent assign_intent = getActivity().getIntent();
        Log.d("id", assign_intent.getStringExtra("course_id"));
        String courseid = assign_intent.getStringExtra("course_id").toString();

        SharedPreferences sp = this.getActivity().getSharedPreferences(getResources().getString(R.string.user_shared_preference), Context.MODE_PRIVATE);

        ListView listView = (ListView) content_view.findViewById(R.id.content_list);

        contentListAdapter = new ContentListAdapter(getContext(), new ArrayList<ContentForListView>());

        listView.setAdapter(contentListAdapter);

        new FetchContentTask().execute(sp.getString("token",""),courseid);

        return content_view;
    }


    public class FetchContentTask extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute(){

//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    dialog = new ProgressDialog(getActivity());
            this.dialog.setMessage("Loading...");
            this.dialog.setCancelable(false);
            this.dialog.show();
//                }
//            },3000);

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
                        .appendPath("moodle").appendPath("webservice").appendPath("rest").appendPath("server.php")
                        .appendQueryParameter("wstoken", params[0])
                        .appendQueryParameter("courseid", params[1])
                        .appendQueryParameter("wsfunction", "core_course_get_contents")
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

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            try {
                if (response != null) {

                    Log.e("Response", response);

                    items = new ArrayList<ContentForListView>();

                    JSONArray modules = new JSONArray(response);
                    for(int i=0;i<modules.length();i++){
                        JSONObject mod = modules.getJSONObject(i);
                        if(mod.has("modules") && mod.getJSONArray("modules").length()>0){
                            JSONArray sub_mods = mod.getJSONArray("modules");
                            for(int j=0;j<sub_mods.length();j++){
                                JSONObject obj = sub_mods.getJSONObject(j);
                                if(obj.has("contents") && obj.getJSONArray("contents").length()>0){
                                    JSONArray files = obj.getJSONArray("contents");

                                    for(int k=0;k<files.length();k++){
                                        JSONObject content = files.getJSONObject(k);
                                        ContentForListView item = new ContentForListView();
                                        item.filename = content.getString("filename");
                                        item.fileurl = content.getString("fileurl");

                                        items.add(item);
                                    }
                                }
                            }
                        }
                    }

                    for(ContentForListView item : items){
                        contentListAdapter.addItem(item);
                    }

//                    Log.e("Assignments", assignments.toString());


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class ContentForListView{
        public String filename, fileurl;
    }

    public static class ContentHolder{
        public TextView filename;
    }

    public class ContentListAdapter extends BaseAdapter {

        private ArrayList<ContentForListView> data;
        private LayoutInflater inflater;

        ContentListAdapter(Context ctx, ArrayList<ContentForListView> items){
            data = items;
            inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            notifyDataSetChanged();
        }

        public void addItem(ContentForListView item){
            data.add(item);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ContentHolder holder;

            if(v==null){
                v = inflater.inflate(R.layout.content_list_item, null);

                holder = new ContentHolder();
                holder.filename = (TextView) v.findViewById(R.id.content_name);

                v.setTag(holder);
            }else{
                holder = (ContentHolder) v.getTag();
            }

            holder.filename.setText(data.get(position).filename);

            return v;
        }
    }

}
