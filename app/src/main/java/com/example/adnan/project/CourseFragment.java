package com.example.adnan.project;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class CourseFragment extends Fragment {

    private HashMap<String, HashMap<String, String>> courses = null;
    private CourseListAdapter courseListAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View course_view = inflater.inflate(R.layout.fragment_course, container, false);

        SharedPreferences sp = this.getActivity().getSharedPreferences(getResources().getString(R.string.user_shared_preference), Context.MODE_PRIVATE);
        try {
//            Log.e("raw", sp.getString("user_detail_json", ""));

            JSONObject user_detail = new JSONObject(sp.getString("user_detail_json", ""));

            ListView listView = (ListView) course_view.findViewById(R.id.courses_listview);

            courseListAdapter = new CourseListAdapter(getContext(), new ArrayList<Pair<String, String>>());

            listView.setAdapter(courseListAdapter);

            new FetchUserCoursesTask().execute(sp.getString("token", ""), user_detail.get("userid").toString());

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String course_code = (String) ((TextView)view.findViewById(R.id.course_code)).getText().toString();
                    String course_id = courses.get(course_code).get("id").toString();

                    Intent intent = new Intent(CourseFragment.this.getActivity(), CourseAccess.class);
                    intent.putExtra("course_id",course_id);
                    startActivity(intent);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Inflate the layout for this fragment
        return course_view;
    }


    public class FetchUserCoursesTask extends AsyncTask<String, Void, String> {

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
                        .appendQueryParameter("userid", params[1])
                        .appendQueryParameter("wsfunction", "core_enrol_get_users_courses")
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

                    JSONArray array = new JSONArray(response);

                    courses = new HashMap<String, HashMap<String, String>>();

                    for(int i=0;i<array.length();i++) {
                        JSONObject obj = array.getJSONObject(i);

                        HashMap<String, String> course_detail = new HashMap<String, String>();

                        course_detail.put("title", obj.get("fullname").toString());
                        course_detail.put("course_desc", obj.get("summary").toString());
                        course_detail.put("language", obj.get("lang").toString());
                        course_detail.put("id",obj.get("id").toString());

                        courses.put(obj.get("idnumber").toString(), course_detail);
                    }

                    ArrayList<Pair<String, String>> courseList = new ArrayList<Pair<String, String>>();

                    for(Map.Entry<String, HashMap<String, String>> course : courses.entrySet()){
                        courseList.add(new Pair<String, String>(course.getValue().get("title").toString(), course.getKey()));
                    }

//                    courseListAdapter = new CourseListAdapter(courseList);
                    for(Pair<String, String> item : courseList){
                        courseListAdapter.addItem(item);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class CourseListAdapter extends BaseAdapter {

        private ArrayList<Pair<String, String>> data;
        private LayoutInflater inflater;

        CourseListAdapter(Context ctx, ArrayList<Pair<String, String>> items){
            data = items;
            inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            notifyDataSetChanged();
        }

        public void addItem(Pair<String, String> item){
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
            View vi = convertView;
            CourseHolder holder;

            if(vi==null){
                vi = inflater.inflate(R.layout.course_list_item, null);

                holder = new CourseHolder();
                holder.courseTitle = (TextView) vi.findViewById(R.id.course_title);
                holder.courseCode = (TextView) vi.findViewById(R.id.course_code);

                vi.setTag(holder);
            }else{
                holder = (CourseHolder) vi.getTag();
            }

            holder.courseTitle.setText(data.get(position).first);
            holder.courseCode.setText(data.get(position).second);

            return vi;
        }

        public class CourseHolder {
            public TextView courseTitle, courseCode;
        }
    }

}
