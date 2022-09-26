package com.fontys.vistaapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.fontys.vistaapplication.API.BackendAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private JSONObject spots_object;
    private Button logout;
    private TextView username;
    private ListView editSpotsAdded, editSpotsVisited;
    private ImageView profilePic;
    private final String TAG = "PROFILE_OVERVIEW_FRAGMENT";
    private List<String> addedList = new ArrayList<String>(), visitList = new ArrayList<String>();
    private ArrayAdapter<String> adapterAdded, adapterVisit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        //fill in profile name and details
        logout = view.findViewById(R.id.buttonLogout);
        username = view.findViewById(R.id.textViewUsername);
        editSpotsAdded = view.findViewById(R.id.editTextSpotsAddedByYou);
        editSpotsVisited = view.findViewById(R.id.editTextSpotsVisited);

        adapterAdded = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, addedList);
        adapterVisit = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, visitList);

        editSpotsVisited.setOnItemClickListener((a, v, p, x) -> {
            String item = (String) a.getItemAtPosition(p);
            int id = Integer.parseInt(item.substring(item.lastIndexOf("(") + 1, item.lastIndexOf(")")));

            System.out.println("Clicked on spot: " + id);
            SpotOverviewFragment fragment = new SpotOverviewFragment(id);
            Helper.replaceFragment(getParentFragmentManager(), fragment);
        });

        editSpotsAdded.setOnItemClickListener((a, v, p, x) -> {
            String item = (String) a.getItemAtPosition(p);
            int id = Integer.parseInt(item.substring(item.lastIndexOf("(") + 1, item.lastIndexOf(")")));

            System.out.println("Clicked on spot: " + id);
            SpotOverviewFragment fragment = new SpotOverviewFragment(id);
            Helper.replaceFragment(getParentFragmentManager(), fragment);
        });

//            @Override
//            public void onItemClick(AdapterView<?>adapter,View v, int position){
//                ItemClicked item = adapter.getItemAtPosition(position);
//
//            }
//        });

        logout.setOnClickListener(v -> {
            Log.d(TAG, "logout pressed");
            BackendAPI.ResetJWT();
            Helper.wipeFile();
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });

        //fetch data and prepare details string
        BackendAPI.Send(null, "visits_history", "GET", (data) -> {
            try {
                //extract the needed information from the response
                JSONArray visits_array = data.getJSONArray("data");

                for (int i = 0; i < visits_array.length(); i++) {
                    JSONObject visited_spot = visits_array.getJSONObject(i);
                    Date date = new Date ();
                    date.setTime((long)visited_spot.getLong("last_visit_date"));

                    BackendAPI.Send(null, "/spots/"+visited_spot.getInt("spot_id"), "GET", (spotData) -> {
                        try {
                            JSONObject spot = spotData.getJSONArray("data").getJSONObject(0);
                            StringBuilder sb = new StringBuilder();
                            sb.append(spot.getString("title") + " (" + spot.getString("id") + ")\n");
                            sb.append("Visited on " + date.toLocaleString() + "");

                            System.out.println(sb.toString());
                            visitList.add(sb.toString());
                            adapterVisit.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                if(visits_array.length() == 0) visitList.add("You have not visited any spots");
                adapterVisit.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        BackendAPI.Send(null, "user", "GET", (data) -> {
            try {
                username.setText(data.getString("username"));
                username.setSingleLine(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        addedList.clear();
        BackendAPI.Send(null, "created_spots_history", "GET", (data) -> {
            try {
                JSONArray visits_array = data.getJSONArray("data");
                for(int i = 0; i < visits_array.length(); i++) {
                    JSONObject spot = visits_array.getJSONObject(i);
                    Date date = new Date ();
                    date.setTime((long)spot.getLong("date_created"));

                    StringBuilder sb = new StringBuilder();
                    sb.append(spot.getString("title") + " (" + spot.getString("id") + ")\n");
                    sb.append("Created on " + date.toLocaleString() + "");

                    addedList.add(sb.toString());
                }

                if (visits_array.length() == 0) addedList.add("You have not added any spots");
                adapterAdded.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        editSpotsAdded.setAdapter(adapterAdded);
        editSpotsVisited.setAdapter(adapterVisit);

        return view;
    }
}