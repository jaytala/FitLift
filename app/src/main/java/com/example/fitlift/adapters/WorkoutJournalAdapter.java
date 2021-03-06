package com.example.fitlift.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlift.R;
import com.example.fitlift.WorkoutJournal;
import com.example.fitlift.databinding.ItemWorkoutJournalBinding;
import com.example.fitlift.fragments.WorkoutDetailsFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WorkoutJournalAdapter extends RecyclerView.Adapter<WorkoutJournalAdapter.ViewHolder> {

    private Context context;
    private List<WorkoutJournal> workoutJournals;

    public WorkoutJournalAdapter(Context context, List<WorkoutJournal> workoutJournals) {
        this.context = context;
        this.workoutJournals = workoutJournals;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkoutJournalBinding binding = ItemWorkoutJournalBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutJournal workoutJournal = workoutJournals.get(position);
        holder.bind(workoutJournal);
    }

    @Override
    public int getItemCount() {
        return workoutJournals.size();
    }

    public void clear() {
        workoutJournals.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<WorkoutJournal> newWorkouts) {
        workoutJournals.addAll(newWorkouts);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemWorkoutJournalBinding binding;

        public ViewHolder(@NonNull ItemWorkoutJournalBinding b) {
            super(b.getRoot());
            this.binding = b;
            // needed to enable onClickListener on views
            binding.getRoot().setOnClickListener(this);
        }

        public void bind(WorkoutJournal workoutJournal) {
            // bind the workout data to the view elements
            // EEE, MMM d, yy pattern to add year
            String date = new SimpleDateFormat("EEE, MMM d").format(workoutJournal.getCreatedAt());

            binding.etTitle.setText(workoutJournal.getTitle());
            binding.tvDate.setText(date);
        }

        @Override
        public void onClick(View view) {
            // must cast context to FragmentActivity to call getSupportFragmentManager
            FragmentTransaction fts = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
            //Toast.makeText(context, "You clicked", Toast.LENGTH_SHORT).show();
            int position = getAdapterPosition();
            // make sure the position is valid - exists in the view
            if (position != RecyclerView.NO_POSITION) {
                WorkoutJournal workoutJournal = workoutJournals.get(position);
                // bundle needed to pass data to other fragments
                Bundle bundle = new Bundle();
                bundle.putString("workoutId", workoutJournal.getWorkout().getObjectId());
                bundle.putString("workoutJournalId", workoutJournal.getObjectId());
//                bundle.putInt("Adapter position", position);
//                bundle.putString("Title", workoutJournal.getTitle());
//                bundle.putString("Date", workoutJournal.getCreatedAt().toString());
//                // TODO: WILL HAVE TO UPDATE TO ARRAY OF EXERCISES, WEIGHTS, AND REPS

                Fragment fragment = new WorkoutDetailsFragment();

                fts.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom);
                // attach bundle to fragment
                fragment.setArguments(bundle);
                // navigate to new fragment
                fts.replace(R.id.flContainer, fragment).commit();
            }
        }
    }
}
