package com.example.fitlift.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fitlift.MealJournal;
import com.example.fitlift.OnSwipeTouchListener;
import com.example.fitlift.R;
import com.example.fitlift.activities.MainActivity;
import com.example.fitlift.databinding.FragmentMealDetailsBinding;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class MealDetailsFragment extends Fragment {

    public static final String TAG = "MealDetailsFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 24;
    private FragmentMealDetailsBinding binding;
    private ParseUser user = ParseUser.getCurrentUser();
    private boolean mealJournalUpdate = false;
    private MealJournal updateObject;
    private File photoFile;
    public String photoFileName = "photo.jpg";
    private MainActivity activity;

    public MealDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentMealDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Bundle bundle = getArguments();

        view.setOnTouchListener(new OnSwipeTouchListener(getContext()) {

            @Override
            public void onSwipeDown() {
            }

            @Override
            public void onSwipeUp() {
            }

            @Override
            public void onSwipeRight() {
                activity.goToWorkout();
            }
        });

        if (bundle != null) {

            mealJournalUpdate = true;
            String objectId = bundle.getString("objectId");

            ParseQuery<MealJournal> query = ParseQuery.getQuery(MealJournal.class);
            query.getInBackground(objectId, new GetCallback<MealJournal>() {
                @Override
                public void done(MealJournal object, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error while fetching existing meal", e);
                        return;
                    }

                    updateObject = object;

                    String title = object.getTitle();
                    String description = object.getMealDescription();
                    List<String> nutrients = object.getNutrients();
                    List<Integer> amounts = object.getAmounts();
                    ParseFile mealImage = object.getImage();

                    String date = new SimpleDateFormat("EEE, MMM d").format(object.getCreatedAt());

                    binding.etTitleMealDetails.setText(title);
                    binding.tvDateMealDetails.setText(date);
                    binding.etMealDescription.setText(description);

                    if (mealImage != null) {
                        Glide.with(getContext()).load(mealImage.getUrl()).into(binding.ivMealImage);
                        binding.ivMealImage.setVisibility(View.VISIBLE);
                    }

                    int nutrientSize = nutrients.size();
                    int amountSize = amounts.size();

                    // only bind nutrient if it exists
                    if(nutrientSize > 0) {
                        binding.etNutrient1.setText(nutrients.get(0));
                        if(nutrientSize > 1) {
                            binding.etNutrient2.setText(nutrients.get(1));
                            if(nutrientSize > 2) {
                                binding.etNutrient3.setText(nutrients.get(2));
                                if(nutrientSize > 3) {
                                    binding.etNutrient4.setText(nutrients.get(3));
                                    if(nutrientSize > 4) {
                                        binding.etNutrient5.setText(nutrients.get(4));
                                    }
                                }
                            }
                        }
                    }

                    // only bind amount if it exists
                    if(amountSize > 0) {
                        binding.etAmount1.setText(String.valueOf(amounts.get(0)));
                        if(amountSize > 1) {
                            binding.etAmount2.setText(String.valueOf(amounts.get(1)));
                            if(amountSize > 2) {
                                binding.etAmount3.setText(String.valueOf(amounts.get(2)));
                                if(amountSize > 3) {
                                    binding.etAmount4.setText(String.valueOf(amounts.get(3)));
                                    if(amountSize > 4) {
                                        binding.etAmount5.setText(String.valueOf(amounts.get(4)));
                                    }
                                }
                            }
                        }
                    }
                }
            });

        } else {
            Date now = new Date();
            String date = new SimpleDateFormat("EEE, MMM d").format(now);

            binding.tvDateMealDetails.setText(date);
        }
        // listener to launch camera
        binding.btnAttachImg.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });
        // listener to save post
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"Save button clicked!");

                String currTitle = binding.etTitleMealDetails.getText().toString();
                if (currTitle.isEmpty()) {
                    Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currMealDescription = binding.etMealDescription.getText().toString();
                if (currMealDescription.isEmpty()) {
                    Toast.makeText(getContext(), "Meal Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> nutrients = new ArrayList<>();

                if (!binding.etNutrient1.getText().toString().isEmpty()) {
                    nutrients.add(binding.etNutrient1.getText().toString());
                }

                if (!binding.etNutrient2.getText().toString().isEmpty()) {
                    nutrients.add(binding.etNutrient2.getText().toString());
                }

                if (!binding.etNutrient3.getText().toString().isEmpty()) {
                    nutrients.add(binding.etNutrient3.getText().toString());
                }

                if (!binding.etNutrient4.getText().toString().isEmpty()) {
                    nutrients.add(binding.etNutrient4.getText().toString());
                }

                if (!binding.etNutrient5.getText().toString().isEmpty()) {
                    nutrients.add(binding.etNutrient5.getText().toString());
                }

                List<Integer> amounts = new ArrayList<>();

                if (!binding.etAmount1.getText().toString().isEmpty()) {
                    amounts.add(Integer.valueOf(binding.etAmount1.getText().toString()));
                }

                if (!binding.etAmount2.getText().toString().isEmpty()) {
                    amounts.add(Integer.valueOf(binding.etAmount2.getText().toString()));
                }

                if (!binding.etAmount3.getText().toString().isEmpty()) {
                    amounts.add(Integer.valueOf(binding.etAmount3.getText().toString()));
                }

                if (!binding.etAmount4.getText().toString().isEmpty()) {
                    amounts.add(Integer.valueOf(binding.etAmount4.getText().toString()));
                }

                if (!binding.etAmount5.getText().toString().isEmpty()) {
                    amounts.add(Integer.valueOf(binding.etAmount5.getText().toString()));
                }

                if (mealJournalUpdate) {
                    savePostUpdate(currTitle, currMealDescription, nutrients, amounts, updateObject, photoFile);
                } else {
                    savePost(currTitle, currMealDescription, nutrients, amounts, photoFile);
                }
            }
        });
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider.FitLift", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                binding.ivMealImage.setImageBitmap(takenImage);
                binding.ivMealImage.setVisibility(View.VISIBLE);
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private void savePostUpdate(String currTitle, String currMealDescription, List<String> nutrients, List<Integer> amounts, MealJournal updateObject, File photoFile) {
        updateObject.setTitle(currTitle);
        updateObject.setMealDescription(currMealDescription);
        updateObject.put("nutrients", nutrients);
        updateObject.put("amounts", amounts);

        if (photoFile != null && binding.ivMealImage.getDrawable() != null) {
            updateObject.setImage(new ParseFile(photoFile));
        }

        updateObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "Post was successful");
                binding.ivMealImage.setImageResource(0);

                FragmentManager fragmentManager = getFragmentManager();
                Fragment fragment = new MealFragment();
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        });
    }

    private void savePost(String currTitle, String currMealDescription, List<String> nutrients, List<Integer> amounts, File photoFile) {
        MealJournal mealJournal = new MealJournal();

        mealJournal.put("user", user);
        mealJournal.setTitle(currTitle);
        mealJournal.setMealDescription(currMealDescription);
        mealJournal.setNutrients(nutrients);
        mealJournal.setAmounts(amounts);

        if (photoFile != null && binding.ivMealImage.getDrawable() != null) {
            mealJournal.setImage(new ParseFile(photoFile));
        }

        mealJournal.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "Post was successful");

                FragmentManager fragmentManager = getFragmentManager();
                Fragment fragment = new MealFragment();
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        });

    }
}