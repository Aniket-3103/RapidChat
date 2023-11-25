package com.mishraaniket.project.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mishraaniket.project.Adapters.TopStatusAdapter;
import com.mishraaniket.project.Adapters.UsersAdapter;
import com.mishraaniket.project.Models.Status;
import com.mishraaniket.project.Models.User;
import com.mishraaniket.project.Models.UserStatus;
import com.mishraaniket.project.R;
import com.mishraaniket.project.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<User> users;
    UsersAdapter usersAdapter;
    TopStatusAdapter statusAdapter;
    ArrayList<UserStatus> userStatuses;
    ProgressDialog dialog;
    User user;
    String username, profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database=FirebaseDatabase.getInstance();

        //Every device has a unique token associated to  it. We are storing users token so as to send notifications.
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        HashMap<String, Object> map= new HashMap<>();                           //Creating a hashmap to store user token in it. This hashmap with token value
                                                                                                //will be uploaded to the users database

                        map.put("token", token);                                                //Adding token to the hashmap

                        database.getReference()
                                .child("users")
                                .child(FirebaseAuth.getInstance().getUid())
                                .updateChildren(map);
                    }
                });

        //remoteConfig is used to control application from backend, customization and update purposes
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)                            //time limit specified is to check after how many seconds we should look for updates
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);


        //whatever changes we fetch through this remoteConfig, we will activate it
        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                String toolbarColor=mFirebaseRemoteConfig.getString("toolbarColor");
//                String backgroundImage=mFirebaseRemoteConfig.getString("backgroundImage");

//                Glide.with(MainActivity.this).load(backgroundImage).into(binding.BackgroundImage);                          //changing the background of MainActivity

                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(toolbarColor)));                    //changing the colour of toolbar

            }
        });



        dialog=new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        users=new ArrayList<>();
        userStatuses=new ArrayList<>();

        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid())           //getting the users from firebaseDatabase/users
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user=snapshot.getValue(User.class);                                         //storing the current user in user variable
                        username=user.getName();
                        profileImage=user.getProfileImage();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        usersAdapter=new UsersAdapter(this, users);                                          //creating userAdapter with users arraylist
        statusAdapter=new TopStatusAdapter(this, userStatuses);                              //creating statusAdapter with userStatuses arraylist

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.statusList.setLayoutManager(layoutManager);

        binding.statusList.setAdapter(statusAdapter);                                               //setting the status adapter in statusList recyclerView
        binding.recylerView.setAdapter(usersAdapter);                                               //setting the user adapter in UserList recyclerView

        //enabling the shimmer until the data is not loaded from firebase
        binding.recylerView.showShimmer();
        binding.statusList.showShimmer();

        //loading the users
        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    user=snapshot1.getValue(User.class);

                    //if user in snapshot is not equal to current user, then only show it on screen. (so user doesn't see his own contact)
                    if(!user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                        users.add(user);
                    }
                }
                binding.recylerView.hideShimmer();
                usersAdapter.notifyDataSetChanged();

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //loading the statuses
        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userStatuses.clear();
                    for(DataSnapshot storySnapshot: snapshot.getChildren()){
                        UserStatus status=new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status> statuses=new ArrayList<>();
                        for(DataSnapshot statusSnapshot: storySnapshot.child("statuses").getChildren()){
                            Status sampleStatus= statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }

                        status.setStatus(statuses);
                        userStatuses.add(status);
                    }
                    binding.statusList.hideShimmer();
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //setting the navigationItemSelectListener
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.status:                                                           //if status is selected, start an intent to get an image for status
                        Intent intent=new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, 75);
                        break;

                    case R.id.videoCalls:                                                           //if status is selected, start an intent to get an image for status
                        Intent i=new Intent(MainActivity.this, VideoCallLoginId.class);
                        i.putExtra("Username",username);
                        startActivity(i);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data!=null){
            if(data.getData()!=null){
                dialog.show();
                FirebaseStorage storage=FirebaseStorage.getInstance();
                Date date=new Date();
                StorageReference reference=storage.getReference().child("status").child(date.getTime() + "");

                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UserStatus userStatus=new UserStatus();
                                    userStatus.setName(username);
                                    userStatus.setProfileImage(profileImage);
                                    userStatus.setLastUpdated(date.getTime());

                                    HashMap<String, Object> obj=new HashMap<>();
                                    obj.put("name", userStatus.getName());
                                    obj.put("profileImage", userStatus.getProfileImage());
                                    obj.put("lastUpdated", userStatus.getLastUpdated());

                                    String imageUrl=uri.toString();
                                    Status status=new Status(imageUrl, userStatus.getLastUpdated());

                                    database.getReference()
                                            .child("stories")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .updateChildren(obj);

                                    database.getReference().child("stories")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .child("statuses")
                                            .push()
                                            .setValue(status);

                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    //to show whether the user is online or not
    @Override
    protected void onResume(){
        super.onResume();

        //we are collecting the userId of the current user and setting its value to online so that its activity status can be seen as online on other devices
        String currentId=FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }


    //when the mainActivity is stopped
    @Override
    protected void onPause() {
        super.onPause();

        //we are collecting the userId of the current user and setting its value to offline so that its activity status can be seen as offline on other devices
        String currentId=FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    //when search button or item icon is pressed
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.group:
                startActivity(new Intent(MainActivity.this, GroupChatActivity.class));
                break;
            case R.id.search:
                Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}