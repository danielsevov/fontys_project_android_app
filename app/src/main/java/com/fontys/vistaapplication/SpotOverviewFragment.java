package com.fontys.vistaapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.fontys.vistaapplication.API.BackendAPI;
import com.fontys.vistaapplication.API.PropertyConstructor;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This is a controller for the SpotOverview fragment.
 * The SpotOverview fragment is part of the BottomNavigationalBar, which is used for switching views.
 * It is used to overview the information of a specific spot and to allow for edits, deleted, picture uploads and ratings.
 * The SpotOverview fragment replaces the MapsFragment when a user clicks on a specific spot.
 */
public class SpotOverviewFragment extends Fragment {

    private final int spot_id; //stores the id number of the spot
    private final String TAG = "SPOT_OVERVIEW_FRAGMENT";

    //text fields for the properties of the spot
    private EditText titleText;
    private TextView voteText, dateText, creatorText;

    //buttons with animations for the main actions
    private FloatingActionButton editButton, deleteButton, pictureButton,upvoteButton, downvoteButton, saveChangesButton, goBackButton, cancelChangesButton;

    //chip groups for picking categories and tags
    private ChipGroup categoryGroup, tagsGroup;

    //list of all available categories and tags
    private final List<String> categories, tags;

    //list of all chips on the fragment. Used to activate/deactivate all.
    private final List<Chip> chipList;

    //holders for the last selected values of category and tags
    private String selectedCategory;
    private final List<String> selectedTagsList;

    //the bottom navigation bar used for switching views
    private BottomNavigationView navBar;

    //boolean indicating changes since the edit has started
    private boolean madeChanges = false;

    //timer for a page update on a fixed schedule
    private Timer timer = new Timer("Overview-Updater", false);

    //holder for the old data entries on spot edit, so data is not lost on edit cancel
    private HashMap<String, Object> _changesData = new HashMap<>();


    /**
     * Simple constructor for initializing the spot overview fragment.
     *
     * @param spot_id the id of the current spot, used for fetching the spot data from the Backend API.
     */
    public SpotOverviewFragment(Integer spot_id) {
        this.spot_id = spot_id;
        categories = new ArrayList<>();
        tags = new ArrayList<>();
        chipList = new ArrayList<>();
        selectedTagsList = new ArrayList<>();
    }

    /**
     * This code will be executed after the fragment is attached to an activity but is still not created completely.
     * We use it for initializing the list of available categories and tags, as well as for initializing the permissions needed.
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize the permissions
        PermissionUtil.initPermission(this);

        //list of available categories
        categories.add("Sunrise");
        categories.add("Sunset");
        categories.add("Moonrise");
        categories.add("Moonset");

        //list of available tags
        tags.add("Lake");
        tags.add("Mountains");
        tags.add("Coast");
        tags.add("Nature");
        tags.add("Rural");
        tags.add("Urban");
    }

    /**
     * This code will be executed after the fragment is created completely.
     * We use it for finding views by ID, initializing the buttons and starting the update timer.
     *
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_spot_overview, container, false);

        //hide navigational bar
        navBar = getActivity().findViewById(R.id.bottomNavigationView);
        navBar.setVisibility(View.INVISIBLE);

        //Get important fields to java
        titleText = view.findViewById(R.id.SO_titleText);
        dateText = view.findViewById(R.id.SO_dateText);
        creatorText = view.findViewById(R.id.SO_creatorText);
        voteText = view.findViewById(R.id.SO_ratingText);
        upvoteButton = view.findViewById(R.id.SO_up_button);
        downvoteButton = view.findViewById(R.id.SO_down_button);
        categoryGroup = view.findViewById(R.id.categoryGroup);
        tagsGroup = view.findViewById(R.id.tagsGroup);
        saveChangesButton = view.findViewById(R.id.SO_save_button);
        goBackButton = view.findViewById(R.id.SO_back_button);
        pictureButton = view.findViewById(R.id.FSO_addPicture);
        cancelChangesButton = view.findViewById(R.id.SO_cancelbutton);
        deleteButton = view.findViewById(R.id.SO_delete_button);
        editButton = view.findViewById(R.id.SO_edit_button);

        //Set initial field properties
        titleText.setEnabled(false);
        saveChangesButton.setVisibility(View.INVISIBLE);

        //Assign buttons!
        initializeButtons();

        //Run timer that keeps it updating!
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(Looper.myLooper() == null) Looper.prepare();
                try {
                    getActivity().runOnUiThread(() -> updateData());
                } catch (NullPointerException ex) {
                    //Could not run on ui thread
                }
            }
        }, 0, 60 * 1000);

        return view;
    }

    /**
     * Method used for creating a chip view and adding it to a chip group.
     *
     * @param name of the chip
     * @param group of chips for this chip to be added to
     * @param clickable boolean, indicating if the chip should allow clicking
     */
    private void createChip(String name, ChipGroup group, boolean clickable) {
        Chip chipT = new Chip(getActivity());
        ChipDrawable chipDrawableT = ChipDrawable.createFromAttributes(getActivity(),
                null,
                0,
                R.style.chipChoice);
        chipT.setChipDrawable(chipDrawableT);

        chipT.setText(name);
        chipT.setClickable(clickable); //Make it so they cant edit it!
        group.addView(chipT);
    }

    /**
     * This method is used to initialize the chip groups when the spot is not being edited.
     * It makes sure that only the selected chips for a spot are being displayed.
     *
     * @param spotData the data of the spot in JSON format
     * @throws JSONException exception thrown if the JSON does not contain a searched entry
     */
    private void initializeChips(JSONObject spotData) throws JSONException {
        //set categories string to id hashmap
        if(spotData.has("category")) {
            categoryGroup.removeAllViews();
            createChip(spotData.getString("category"), categoryGroup, false);
        } else System.out.println("Spot contains no found category!!!");

        //set tags string to id hashmap
        if(spotData.has("tags")) {
            tagsGroup.removeAllViews();
            JSONArray foundTags = spotData.getJSONArray("tags");
            for (int i = 0; i < foundTags.length(); i++) {
                createChip(foundTags.getString(i), tagsGroup, false);
            }
        } else System.out.println("No tags found for spot!");

    }

    /**
     * This method is used to initialize all buttons on the fragment.
     * It would set their visibilities, clickabilities and would strap them with onClickListeners
     * so that callback code can be executed in result of a button push.
     */
    private void initializeButtons() {

        //set the go back button to return to MapsFragment
        goBackButton.setOnClickListener((View v) -> {
                if(cancelChangesButton.getVisibility() == View.VISIBLE) onCancelEditButtonClicked(v); //Cancel the changes if edit is active
                navBar.setVisibility(View.VISIBLE); //show nav bar before switching to MapsFragment

                //switch current fragment with MapsFragment
                Intent intent = new Intent(getContext(), HubActivity.class);
                startActivity(intent);
        });

        //make the button for adding pictures open the camera activity when clicked
        pictureButton.setOnClickListener(v -> PermissionUtil.openCamera(getContext(), this));

        //update the rating of the spot on Upvote button click
        upvoteButton.setOnClickListener((view -> {
            BackendAPI.Send(null, "spots/" + spot_id + "/upvote", "PATCH", (data) -> {
                try {
                    if(data.has("ERROR")) Helper.createToast(data.getString("ERROR"), getActivity(), getContext());
                    if(data.has("SUCCESS")) {
                        Helper.createToast(data.getString("SUCCESS"), getActivity(), getContext());

                        //update the rating number text
                        int rate = Integer.parseInt(String.valueOf(voteText.getText())) + 1;
                        voteText.setText(rate + "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }));

        //update the rating of the spot on Downvote button click
        downvoteButton.setOnClickListener((view -> {
            BackendAPI.Send(null, "spots/" + spot_id + "/downvote", "PATCH", (data) -> {
                try {
                    updateRateability();

                    if(data.has("ERROR")) Helper.createToast(data.getString("ERROR"), getActivity(), getContext());
                    if(data.has("SUCCESS")) {
                        Helper.createToast(data.getString("SUCCESS"), getActivity(), getContext());

                        //update the rating number text
                        int rate = Integer.parseInt(String.valueOf(voteText.getText())) - 1;
                        voteText.setText(rate + "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }));

        //make delete button ask for confirmation and then delete the spot
        deleteButton.setOnClickListener(view -> {

            //prepare confirmation dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setCancelable(true);
            builder.setTitle("Delete spot");
            builder.setMessage("Are you sure you want to delete your spot? Once this is done we can not undo this action!");

            //delete spot in the database if confirmed
            builder.setPositiveButton("Confirm", (dialog, which) -> {
                BackendAPI.Send(null, "spots/" + spot_id, "DELETE", (data) -> {
                    goBackButton.callOnClick(); //If success go back
                });
            });
            //don't do anything if not confirmed
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {});

            //create and display confirmation dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        //prepare edit confirmation button
        editButton.setOnClickListener(v -> {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                onEditButtonClicked(v);
            }, 250);
        });

        //prepare edit cancellation button
        cancelChangesButton.setOnClickListener(v -> {
            //only cancel changes if something has been changed already
            if(!isSpotDataEdited()) {
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                    onCancelEditButtonClicked(v);
                }, 250);
                return;
            }

            //prepare cancellation dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setCancelable(true);
            builder.setTitle("Discard changes");
            builder.setMessage("Are you sure you want to discard your changes? Once this is done we can not undo this action!");

            //set button actions
            builder.setPositiveButton("Confirm", (dialog, which) -> {
                onCancelEditButtonClicked(v);
            });
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {});

            //create and display dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        //prepare save changes button
        saveChangesButton.setOnClickListener(v -> {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                onSaveChangesButtonClicked(v);
            }, 250);
        });
    }

    /**
     * Method to check if any value has been already changed during the spot edit.
     * @return boolean showing if anything was changed during spot edit
     */
    private boolean isSpotDataEdited() {
        boolean tagsChanged = false;
        boolean categoryChanged = !((String)_changesData.get("category")).equals(((Chip) categoryGroup.findViewById(categoryGroup.getCheckedChipId())).getText().toString());
        boolean titleChanged = !_changesData.get("title").equals(titleText.getText().toString());

        for(int chipId : tagsGroup.getCheckedChipIds()) {
            Chip chip = tagsGroup.findViewById(chipId);
            if(((List<String>) _changesData.get("tags")).contains(chip.getText().toString())) continue;

            tagsChanged = true;
            break; //Stop the loop one chip is already different!
        }

        System.out.println("Change?? " + titleChanged + " " + tagsChanged + " " + categoryChanged);

        return titleChanged || tagsChanged || categoryChanged;
    }

    /**
     * Method to prepare the view for a spot edit.
     */
    private void onEditButtonClicked(View view) {
        //set button visibilities
        editButton.setVisibility(View.INVISIBLE);
        saveChangesButton.setVisibility(View.VISIBLE);
        cancelChangesButton.setVisibility(View.VISIBLE);

        //back up data
        _changesData.put("title", String.valueOf(titleText.getText()));
        _changesData.put("category", getSelectedCategory());
        _changesData.put("tags", getTags());

        //make text fields editable
        titleText.setEnabled(true);

        //make existing category chip clickable
        ((Chip) categoryGroup.getChildAt(0)).setChecked(true);
        ((Chip) categoryGroup.getChildAt(0)).setClickable(true);

        //create additional category chips
        for (String cat : categories ) {
            if (cat.equals((String) _changesData.get("category"))) continue;
            createChip(cat, categoryGroup, true);
        }

        //make existing tags chips clickable
        for(int i = 0; i < tagsGroup.getChildCount(); i++) {
            ((Chip) tagsGroup.getChildAt(i)).setChecked(true);
            ((Chip) tagsGroup.getChildAt(i)).setClickable(true);
        }

        //create additional tags chips
        for (String tag : tags ) {
            if(((List<String>) _changesData.get("tags")).contains(tag)) continue;
            createChip(tag, tagsGroup, true);
        }
    }

    /**
     * Method to cancel spot editing.
     */
    private void onCancelEditButtonClicked(View view) {
        //return button visibilities to initial state before edit
        editButton.setVisibility(View.VISIBLE);
        saveChangesButton.setVisibility(View.INVISIBLE);
        cancelChangesButton.setVisibility(View.INVISIBLE);

        //disable and update the title text field
        titleText.setEnabled(false);
        titleText.setText((String)_changesData.get("title"));

        //create the selected tags chips
        tagsGroup.removeAllViews();
        for (String tag : (List<String>)_changesData.get("tags") ) {
            createChip(tag, tagsGroup, false);
        }

        //create the selected category chip
        categoryGroup.removeAllViews();
        createChip((String)_changesData.get("category"), categoryGroup, false);

        //empty backup data holder
        _changesData.clear();
    }

    /**
     * Method to persist the changes made to the spot during spot edit.
     */
    private void onSaveChangesButtonClicked(View view) {
        //return button visibilities to initial state before edit
        editButton.setVisibility(View.VISIBLE);
        saveChangesButton.setVisibility(View.INVISIBLE);
        cancelChangesButton.setVisibility(View.INVISIBLE);

        //disable title text field
        titleText.setEnabled(false);

        //collect selected tag chip values
        List<String> tags = new ArrayList<>();
        for(int id : tagsGroup.getCheckedChipIds()) {
            tags.add(((Chip) tagsGroup.findViewById(id)).getText().toString());
        }
        String category = ((Chip)categoryGroup.findViewById(categoryGroup.getCheckedChipId())).getText().toString();

        //display selected tags chips
        tagsGroup.removeAllViews();
        for(String tag: tags) createChip(tag, tagsGroup, false);

        //display selected category chip
        categoryGroup.removeAllViews();
        createChip(category, categoryGroup, false);

        //persist the changes to the database with an API call
        BackendAPI.Send(new PropertyConstructor()
                        .addProperty("id", spot_id)
                .addProperty("title", titleText.getText().toString())
                .addProperty("category", category)
                .addProperty("tags", tags),
                "spots", "PATCH", (data) -> {
            //empty data backup holder
            _changesData.clear();

            try {
                //if update is successful update the latest modification date to match todays date
                if(data.has("SUCCESS")) {
                    Date date = new Date();
                    date.setTime((long) data.getLong("modified_on"));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    dateText.setText(dateFormat.format(date));

                    Helper.createToast(data.getString("SUCCESS"), getActivity(), getContext());
                }
                //otherwise announce error
                if(data.has("ERROR")) Helper.createToast(data.getString("ERROR"), getActivity(), getContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Method used for making edit and delete buttons visible if the user has permissions to modify/delete spots.
     *
     * @param spot data in JSON format
     */
    private void updateEdibility(JSONObject spot) {
        //get the ID of the current user
        BackendAPI.Send(null, "user", "GET", (data) -> {

            try {
                //compare current user id with creator ID and set visibilities
                if(data.getInt("id") == spot.getInt("creator")) {
                    editButton.setVisibility(View.VISIBLE);
                    deleteButton.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });
    }

    /**
     * Method used for making upvote/downvote buttons clickable if the user has permissions to rate the spot.
     */
    private void updateRateability() {
        upvoteButton.setEnabled(false);
        downvoteButton.setEnabled(false);

        //Check rating ability
        BackendAPI.Send(null, "/spots/" + spot_id + "/rateable", "GET", (data) -> {
            try {
                if(data.getBoolean("is_creator")) return; //User is creator so he cant rate his own spot!
                if(data.getBoolean("response")) { //He has not rated the spot yet!

                    upvoteButton.setClickable(true);
                    downvoteButton.setClickable(true);
                    upvoteButton.setEnabled(true);
                    downvoteButton.setEnabled(true);

                    return;
                }

                //if user has already rated the spot then his vote is displayed
                if(data.getBoolean("is_upvote")) {
                    upvoteButton.setEnabled(true); //Make it so he can technically undo his vote
                    upvoteButton.setClickable(false);
                } else {
                    downvoteButton.setEnabled(true); //Make it so he can technically undo his vote
                    downvoteButton.setClickable(false);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Method used for fetching images from the database and displaying them.
     */
    private void updateImages() {
        //fetching the image data
        BackendAPI.Send(null, "/spots/" + spot_id + "/image", "GET", (data) -> {
            try {
                JSONArray spots = data.getJSONArray("data");
                List<Bitmap> images = new ArrayList<>();

                //taking out Bit Arrays and converting them to images
                for (int i = 0; i < spots.length(); i++) {
                    JSONObject spot_image = spots.getJSONObject(i);

                    Bitmap img = BackendAPI.ConvertDataToImage(spot_image.getString("data"));
                    images.add(img);
                }

                //adding images to the view pager adapter recycler view
                ViewPager layout = (ViewPager) getActivity().findViewById(R.id.image_library);
                layout.setAdapter(new ViewPagerAdapter(getActivity(), images));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Method used to fetch spot data from the database and display it.
     */
    private void updateData() {
        //Update images
        updateImages();

        //Check if user is in range of the spot so he can upload pictures to it
        PermissionUtil.getCurrentLocation(getContext(), this, location -> {
            if(location == null) return;

            //check if user is nearby spot
            BackendAPI.Send(new PropertyConstructor()
                    .addProperty("spot_id", spot_id)
                    .addProperty("user_coordinates", location.getLatitude() + "," + location.getLongitude()),
                    "/is_spot_nearby_user", "POST", (data) -> {
                        try {
                            //set picture upload button visibility based on response
                            if(data.getBoolean("response")) pictureButton.setVisibility(View.VISIBLE);
                            else pictureButton.setVisibility(View.INVISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
        });

        //fetch spot data and update the spot overview page
        BackendAPI.Send(null, "/spots/" + spot_id + "", "GET", (data) -> {
            try {
                JSONObject spot = data.getJSONArray("data").getJSONObject(0);

                //initialize selected chips with spot data
                initializeChips(spot);

                //check if user can edit/delete spot and adjust accordingly
                updateEdibility(spot);

                //check if user can rate spot and adjust accordingly
                updateRateability();

                //Set title text
                titleText.setText(spot.getString("title"));

                //Set last modified date text
                Date date = new Date();
                date.setTime((long) spot.getLong("date_modified"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                dateText.setText(dateFormat.format(date));

                //Set creator text
                BackendAPI.Send(null, "/users/" + spot.getInt("creator"), "GET", (creatorData) -> {
                    try {
                        creatorText.setText(creatorData.getString("username"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

                //set rating number text
                voteText.setText((spot.getInt("positive_rating") - spot.getInt("negative_rating")) + "");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Method to get the current category selection string.
     * @return the selected category string
     */
    private String getSelectedCategory() {
        return ((Chip) categoryGroup.getChildAt(0)).getText().toString();
    }

    /**
     * Method to get the current tags selection strings
     * @return the selected tags list of strings
     */
    private List<String> getTags() {
        selectedTagsList.clear();
        for(int i = 0; i < tagsGroup.getChildCount(); i++) {
            selectedTagsList.add(((Chip) tagsGroup.getChildAt(i)).getText().toString());
        }
        return selectedTagsList;
    }

    /**
     * Method to handle the result of the camera activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == PermissionUtil.CAMERA_REQUEST_CODE){

                PermissionUtil.getCurrentLocation(getContext(), this, location -> {

                    //check if user is close enough to spot to upload picture
                    BackendAPI.Send(new PropertyConstructor()
                                    .addProperty("spot_id", spot_id)
                                    .addProperty("user_coordinates", location.getLatitude() + "," + location.getLongitude()),
                            "/is_spot_nearby_user", "POST", (x) -> {
                                try {
                                    //if user is not close enough the picture cannot be uploaded
                                    if(!x.getBoolean("response")) {
                                        Helper.createToast("image_to_far", getActivity(), getContext());
                                        return;
                                    }

                                    //covert image to bit array and persist it to database via API call
                                    BackendAPI.Send(new PropertyConstructor()
                                            .addProperty("data", BackendAPI.ConvertImageToData((Bitmap) data.getExtras().get("data"), "JPEG"))
                                            .addProperty("type", "JPEG"), "/spots/image/" +spot_id + "", "PUT", (d) -> {
                                        updateImages();
                                        try {
                                            Helper.createToast(d.getString("SUCCESS"), getActivity(), getContext());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    });

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                });
            }
        }
    }

}