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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AssignmentFragment extends Fragment {

    private ArrayList<AssignmentForListView> items = null;
    private AssignmentListAdapter assignmentListAdapter;

    public AssignmentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View assign_view = inflater.inflate(R.layout.fragment_assignment, container, false);

        Intent assign_intent = getActivity().getIntent();
        Log.d("id", assign_intent.getStringExtra("course_id"));
        String courseid = assign_intent.getStringExtra("course_id").toString();

        SharedPreferences sp = this.getActivity().getSharedPreferences(getResources().getString(R.string.user_shared_preference), Context.MODE_PRIVATE);

        ListView listView = (ListView) assign_view.findViewById(R.id.assignments_listview);

        assignmentListAdapter = new AssignmentListAdapter(getContext(), new ArrayList<AssignmentForListView>());

        listView.setAdapter(assignmentListAdapter);

        new FetchAssignmentsTask().execute(sp.getString("token", ""), courseid);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        return assign_view;
    }

    public class FetchAssignmentsTask extends AsyncTask<String, Void, String>{

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
                        .appendQueryParameter("courseids[]", params[1])
                        .appendQueryParameter("wsfunction", "mod_assign_get_assignments")
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

                    JSONArray assignments = new JSONObject(response).getJSONArray("courses").getJSONObject(0).getJSONArray("assignments");

                    Log.e("Assignments", assignments.toString());

                    items = new ArrayList<AssignmentForListView>();
//                    SimpleDateFormat sdf = new SimpleDateFormat("");
                    if(assignments.length()>0){
                        for(int i=0;i<assignments.length();i++){
                            JSONObject assign_detail = assignments.getJSONObject(i);
                            AssignmentForListView item = new AssignmentForListView();
                            item.topic = assign_detail.get("name").toString();
                            item.dueDate = Long.parseLong(assign_detail.get("duedate").toString());
                            item.grade = Integer.parseInt(assign_detail.get("grade").toString());

                            items.add(item);
                        }
                    }

//                    assignmentListAdapter = new AssignmentListAdapter(getContext(), new ArrayList<AssignmentForListView>());

                    for(AssignmentForListView item : items){
                        assignmentListAdapter.addItem(item);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class AssignmentForListView{
        public String topic;
        public Long dueDate;
        public Integer grade;
    }

    public static class AssignmentHolder{
        public TextView topic;
        public TextView dueDate;
        public TextView grade;
    }

    public class AssignmentListAdapter extends BaseAdapter{

        private ArrayList<AssignmentForListView> data;
        private LayoutInflater inflater;

        AssignmentListAdapter(Context ctx, ArrayList<AssignmentForListView> items){
            data = items;
            inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            notifyDataSetChanged();
        }

        public void addItem(AssignmentForListView item){
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
            AssignmentHolder holder;

            if(v==null){
                v = inflater.inflate(R.layout.assignment_list_item, null);

                holder = new AssignmentHolder();
                holder.topic = (TextView) v.findViewById(R.id.assignment_topic);
                holder.dueDate = (TextView) v.findViewById(R.id.assignment_duedate);
                holder.grade = (TextView) v.findViewById(R.id.assignment_grade);

                v.setTag(holder);
            }else{
                holder = (AssignmentHolder) v.getTag();
            }

            holder.topic.setText(data.get(position).topic);

            Date date = new Date(data.get(position).dueDate);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
            String strDate = sdf.format(date);

            holder.dueDate.setText(strDate);
            holder.grade.setText(data.get(position).grade+" %");

            return v;
        }
    }

}
