package com.example.friendly.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendly.R;
import com.example.friendly.adapters.FriendsAdapter;
import com.example.friendly.objects.FriendRemoval;
import com.example.friendly.objects.FriendRequest;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * <p>
 * Fragment that displays profile of logged in user
 */
public class ProfileFragment extends Fragment {
    public static final String TAG = "ProfileFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1046;
    public static final String photoFileName = "photo.jpg";

    private ParseUser curUser;
    private TextView username;
    private ImageView profilePic;
    private Button changeProfilePic;
    private ProgressBar loadingProfilePic;

    private RecyclerView recyclerviewFriends;
    private FriendsAdapter adapter;

    private File photoFile;

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

        username = view.findViewById(R.id.text_username);
        profilePic = view.findViewById(R.id.image_profile_pic);
        changeProfilePic = view.findViewById(R.id.button_change_profile_pic);
        loadingProfilePic = view.findViewById(R.id.loading_bar);

        //add user data to profile page
        curUser = ParseUser.getCurrentUser();
        if (curUser.getParseFile("profilePic") != null) {
            Glide.with(view).load(curUser.getParseFile("profilePic").getUrl()).into(profilePic);
        } else {
            Glide.with(view).load(R.drawable.placeholder).into(profilePic);
            profilePic.setVisibility(View.VISIBLE);
        }
        username.setText(curUser.getUsername());

        //set up recyclerview of friends
        recyclerviewFriends = view.findViewById(R.id.recyclerview_friends);
        adapter = new FriendsAdapter(getContext());
        recyclerviewFriends.setAdapter(adapter);
        recyclerviewFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        queryFriends();

        //take new profile picture with camera
        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });
    }

    private void queryFriends() {  //TODO break up this method into separate methods
        List<ParseUser> newFriends = ParseUser.getCurrentUser().getList("friends");
        // check if any friend requests have been accepted
        ParseQuery<FriendRequest> query = ParseQuery.getQuery(FriendRequest.class);
        query.whereEqualTo(FriendRequest.KEY_FROM_USER, ParseUser.getCurrentUser());
        query.include(FriendRequest.KEY_TO_USER);
        query.findInBackground(new FindCallback<FriendRequest>() {
            @Override
            public void done(List<FriendRequest> friendRequests, ParseException e) {
                if (e != null) {
                    //query unsuccessful
                    Log.e(TAG, "issue getting requests", e);
                    return;
                }
                if (!friendRequests.isEmpty()) {
                    for (int i = 0; i < friendRequests.size(); i++) {
                        // adds the accepted friend to friends list
                        if (friendRequests.get(i).getBoolean(FriendRequest.KEY_ACCEPTED)) {
                            newFriends.add(friendRequests.get(i).getParseUser("toUser"));

                            // remove the user from pending friend requests list for that user
                            List<ParseUser> requests = ParseUser.getCurrentUser().getList("requests");
                            for (int j = 0; j < requests.size(); j++) {
                                if (requests.get(j).getObjectId().equals(friendRequests.get(j).getParseUser("toUser").getObjectId())) {
                                    requests.remove(j);
                                }
                            }
                            requests.remove(friendRequests.get(i).getParseUser("toUser"));
                            ParseUser.getCurrentUser().put("requests", requests);
                            ParseUser.getCurrentUser().saveInBackground();

                            // deleting the friend request
                            try {
                                FriendRequest request = friendRequests.get(i);
                                request.delete();
                                request.saveInBackground();
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    // save the updated list of friends in Parse server
                    ParseUser.getCurrentUser().put("friends", newFriends);
                    ParseUser.getCurrentUser().saveInBackground();
                }
                // after requests are handled, remove all users that have removed friendship
                ParseQuery<FriendRemoval> removalQuery = ParseQuery.getQuery(FriendRemoval.class);
                removalQuery.whereEqualTo(FriendRemoval.KEY_TO_USER, ParseUser.getCurrentUser());
                removalQuery.include(FriendRemoval.KEY_FROM_USER);
                removalQuery.findInBackground(new FindCallback<FriendRemoval>() {
                    @Override
                    public void done(List<FriendRemoval> friendRemovals, ParseException e) {
                        if (e != null) {
                            //query unsuccessful
                            Log.e(TAG, "issue getting removals", e);
                            return;
                        }
                        if (!friendRemovals.isEmpty()) {
                            List<ParseUser> removeFriends = new ArrayList<>();

                            // add users to list of removeFriends and delete the friend removals objects
                            for (int i = 0; i < friendRemovals.size(); i++) {
                                removeFriends.add(friendRemovals.get(i).getFromUser());
                                FriendRemoval friendRemoval = friendRemovals.get(i);
                                try {
                                    friendRemoval.delete();
                                    friendRemoval.saveInBackground();
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            ParseUser.getCurrentUser().removeAll("friends", removeFriends);
                            ParseUser.getCurrentUser().saveInBackground();
                        }
                    }
                });
            }


        });
        //gets all users in friends array
        if (ParseUser.getCurrentUser().getList("friends") != null) {
            List<ParseUser> allFriends = new ArrayList<>();
            allFriends.addAll(ParseUser.getCurrentUser().getList("friends"));
            adapter.clear();
            adapter.updateUsers(allFriends);
        }

    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider2", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                saveImage(photoFile);
            } else { // Result was a failure
                Toast.makeText(getContext(), getString(R.string.picture_not_taken), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImage(File photoFile) {
        loadingProfilePic.setVisibility(View.VISIBLE);
        Glide.with(getContext()).load(R.drawable.grey_square).into(profilePic);
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.put("profilePic", new ParseFile(photoFile));
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                }
                Glide.with(getContext()).load(curUser.getParseFile("profilePic").getUrl()).into(profilePic);
                loadingProfilePic.setVisibility(View.INVISIBLE);
            }
        });
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }
}