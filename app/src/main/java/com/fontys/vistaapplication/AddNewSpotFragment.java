package com.fontys.vistaapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fontys.vistaapplication.API.BackendAPI;
import com.fontys.vistaapplication.API.PropertyConstructor;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for processing the AddNewSpotFragment.
 * Has a submit and call to API.
 */
public class AddNewSpotFragment extends Fragment {


    private ProgressBar pbar;

    private static final String TAG = "ADD_NEW_SPOT_FRAGMENT";
    private Bitmap thumbnail = null;

    ImageButton takePictureButton, deletePictureButton;

    private FloatingActionButton ANSsubmitBtn;

    private EditText textANSName;
    private ChipGroup chipGroup1, chipGroup2;
    private final List<String> selectedCategories = new ArrayList<>();

    ImageView imageView;


    @SuppressLint("MissingPermission")
    ActivityResultLauncher<String[]> permissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                        result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                        result.getOrDefault(Manifest.permission.CAMERA, false);
                    }
            );


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Check the input of the chips and text
     *
     * @return true if all is good, false on issues
     */
    private boolean checkInput() {
        return chipGroup1.getCheckedChipId() != View.NO_ID &&
                chipGroup2.getCheckedChipIds().size() != 0 &&
                !textANSName.getText().equals("") &&
                textANSName.getText() != null;
    }

    /**
     * This will disable/enable all the inputs while loading. So the user cannot change things
     *
     * @param type false = disable all , true = enable all
     */
    private void disableEnableInput(boolean type) {
        ANSsubmitBtn.setClickable(type);
        textANSName.setEnabled(type);

        for (int i = 0; i < chipGroup1.getChildCount(); i++) {
            chipGroup1.getChildAt(i).setEnabled(type);
        }
        for (int i = 0; i < chipGroup2.getChildCount(); i++) {
            chipGroup2.getChildAt(i).setEnabled(type);
        }
    }

    /**
     * Method for when user has clicked the submit button.
     */
    private void processSubmit() {

        Log.d(TAG, "IN THE METHOD");

        pbar.setVisibility(View.VISIBLE);
        disableEnableInput(false);
        SystemClock.sleep(1000);//Not necessary but to show the loading circle, even on fast loads...

        if (!checkInput()) {
            Toast.makeText(getContext(), R.string.problems_with_input, Toast.LENGTH_LONG).show();
            pbar.setVisibility(View.GONE);
            disableEnableInput(true);
            return;
        }
        if (!PermissionUtil.isGPSactive(getContext())) {
            Toast.makeText(getContext(), R.string.problem_with_gps, Toast.LENGTH_LONG).show();
            pbar.setVisibility(View.GONE);
            disableEnableInput(true);
            return;
        }
        Toast.makeText(getContext(), R.string.create_spot_toast, Toast.LENGTH_SHORT).show();

        PermissionUtil.getCurrentLocation(getContext(), this, location -> {
            Log.d(TAG, location.toString());

            getAllSelectedChips();
            Chip first = chipGroup1.findViewById(chipGroup1.getCheckedChipId());
            Log.d(TAG, "Submit button pressed");
            Log.d(TAG, selectedCategories.toString());
            Log.d(TAG, location.getLatitude() + ", " + location.getLongitude());

            BackendAPI.Send(new PropertyConstructor()
                            .addProperty("title", textANSName.getText().toString())
                            .addProperty("coordinates", location.getLatitude() + "," + location.getLongitude())
                            .addProperty("tags", selectedCategories)
                            .addProperty("category", first.getText().toString())
                            .addProperty("range", (double) 0),
                    "spot", "POST"
                    , (data) -> {
                        if (data.has("ERROR")) {
                            try {
                                String keyValue = data.getString("ERROR");
                                int resID = getResources().getIdentifier(keyValue, "string", getContext().getPackageName());
                                Toast.makeText(getContext(), getString(resID), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            disableEnableInput(true);
                            pbar.setVisibility(View.GONE);
                            return;
                        } else {

                            try {
                                Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                                Integer spotid = data.getInt("id");
                                AddNewSpotFragment fragment = new AddNewSpotFragment();

                                if (thumbnail != null) {

                                    Thread thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (Looper.myLooper() == null) Looper.prepare();
                                            BackendAPI.Send(new PropertyConstructor().addProperty("data",
                                                    //"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"
                                                    BackendAPI.ConvertImageToData(thumbnail, "JPEG")
                                            ).addProperty("type", "JPEG"), "spots/image/" + spotid + "", "PUT", System.out::println);
                                        }
                                    });
                                    thread.start();
                                }

                                Helper.replaceFragment(getParentFragmentManager(), fragment);

                                Intent intent = new Intent(getContext(), HubActivity.class);
                                startActivity(intent);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        });
    }

    private void getAllSelectedChips() {
        chipGroup2.getCheckedChipIds().forEach(id -> {
            Chip toAdd = chipGroup2.findViewById(id);
            selectedCategories.add(toAdd.getText().toString());
        });
    }

    private void clearImageView() {

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            getActivity().findViewById(R.id.ANS_deletePicture).setVisibility(View.INVISIBLE);
            getActivity().findViewById(R.id.ANS_takePicture).setVisibility(View.VISIBLE);

            imageView.setImageResource(android.R.color.transparent);
        }, 250);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtil.CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PermissionUtil.CAMERA_REQUEST_CODE);
            } else {
                Toast.makeText(getContext(), "Whoopsie, no permission granted :(", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PermissionUtil.CAMERA_REQUEST_CODE) {
                getActivity().findViewById(R.id.ANS_deletePicture).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.ANS_takePicture).setVisibility(View.INVISIBLE);

                thumbnail = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(thumbnail);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_new_spot, container, false);
        PermissionUtil.initPermission(this);


        takePictureButton = view.findViewById(R.id.ANS_takePicture);
        deletePictureButton = view.findViewById(R.id.ANS_deletePicture);


        imageView = view.findViewById(R.id.imageView);


        ANSsubmitBtn = view.findViewById(R.id.ANSsubmitBtn);
        textANSName = view.findViewById(R.id.textANSName);
        chipGroup1 = view.findViewById(R.id.ANSchipGroup);
        chipGroup2 = view.findViewById(R.id.ANSchipGroup2);
        pbar = view.findViewById(R.id.ANSProgress);


        ANSsubmitBtn.setOnClickListener(v -> processSubmit());
        takePictureButton.setOnClickListener(v -> PermissionUtil.openCamera(getContext(), this));
        deletePictureButton.setOnClickListener(v -> clearImageView());


        pbar.setVisibility(View.GONE);
        disableEnableInput(true);

        //this is a method that is active throughout tha app once called...
        BottomNavigationView navBar = getActivity().findViewById(R.id.bottomNavigationView);
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = view.getRootView().getHeight() - view.getHeight();

            if (heightDiff > 400) { // Value is questionable...
                //Log.e("MyActivity", "keyboard opened");
                navBar.setVisibility(View.INVISIBLE);
            } else {
                // Log.e("MyActivity", "keyboard closed");
                navBar.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }
}