package com.example.friendly.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.friendly.R;
import com.parse.ParseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private TextView tvUser;
    private ImageView ivProfilePic;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUser = view.findViewById(R.id.tvUser);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);

        ivProfilePic.setVisibility(View.GONE); //TODO make picture visible when there is a profile picture to show

        tvUser.setText(ParseUser.getCurrentUser().getUsername());
    }
}