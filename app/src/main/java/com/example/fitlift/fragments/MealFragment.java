package com.example.fitlift.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen;
import com.ethanhua.skeleton.Skeleton;
import com.example.fitlift.MealJournal;
import com.example.fitlift.OnSwipeTouchListener;
import com.example.fitlift.R;
import com.example.fitlift.activities.LoginActivity;
import com.example.fitlift.activities.MainActivity;
import com.example.fitlift.adapters.MealJournalAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class MealFragment extends Fragment {

    public static final String TAG = "MealFragment";
    private ParseUser user;
    private MainActivity activity;
    private RecyclerView rvMealFragments;
    private MealJournalAdapter adapter;
    private List<MealJournal> mealJournals;
    private TextView tvUserNameMealFragment;
    private ImageView ivProfileImgMealFragment;
    private RecyclerViewSkeletonScreen skeletonScreen;
    private SwipeRefreshLayout swipeContainer;

    // TODO IMPLEMENT VIEW BINDING LIBRARY
    public MealFragment() { }         // Required empty public constructor

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = ParseUser.getCurrentUser();
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meal, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar_meal);
        rvMealFragments = view.findViewById(R.id.rvMealFragments);
        tvUserNameMealFragment = view.findViewById(R.id.tvUserNameMealFragment);
        ivProfileImgMealFragment = view.findViewById(R.id.ivProfileImgMealFragment);
        swipeContainer = view.findViewById(R.id.swipeContainerMeal);


        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_add_workout:
                        activity.goToMealDetails();
                        break;
                    case R.id.action_logout:
                    default:
                        ParseUser.logOut();
                        Intent i = new Intent(getActivity(), LoginActivity.class);
                        startActivity(i);
                }
                return true;
            }
        });

        tvUserNameMealFragment.setText(user.getUsername());
        // check that user has profile img
        if (user.getParseFile("profileImg") != null) {
            Glide.with(this).load(user.getParseFile("profileImg").getUrl()).circleCrop().into(ivProfileImgMealFragment);
        }

        mealJournals = new ArrayList<>();
        adapter = new MealJournalAdapter(getContext(), mealJournals);

        rvMealFragments.setAdapter(adapter);

        rvMealFragments.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "Fetching new friend posts");
                queryMeals();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        skeletonScreen = Skeleton.bind(rvMealFragments).adapter(adapter).load(R.layout.item_meal).show();

        queryMeals();

        rvMealFragments.setOnTouchListener(new OnSwipeTouchListener(getContext()) {

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
    }

    private void queryMeals() {
        ParseQuery<MealJournal> query = ParseQuery.getQuery(MealJournal.class);

        query.whereContains("user", user.getObjectId());
        query.setLimit(30);
        query.addDescendingOrder(MealJournal.KEY_CREATED_AT);

        query.findInBackground(new FindCallback<MealJournal>() {
            @Override
            public void done(List<MealJournal> meals, ParseException e) {

                swipeContainer.setRefreshing(false);

                if (e != null) {
                    Log.e(TAG, "Issue with getting meal journals", e);
                }

                adapter.clear();
                adapter.addAll(meals);
                adapter.notifyDataSetChanged();
                skeletonScreen.hide();
            }
        });
    }
}