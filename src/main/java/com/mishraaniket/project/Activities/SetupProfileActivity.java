package com.mishraaniket.project.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mishraaniket.project.Models.User;
import com.mishraaniket.project.databinding.ActivitySetupProfileBinding;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;

    FirebaseAuth auth;                                            //to check which user has logged in with which phone number and user id

    FirebaseDatabase database;
    FirebaseStorage storage;

    Uri selectedImage;                                            //to store the selected image temporarily
    ProgressDialog dialog;                                        //to show loading screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());                          //by binding the activity, we are free to not use findViewById method and still use every elements

        getSupportActionBar().hide();

        dialog=new ProgressDialog(this);
        dialog.setMessage("Updating profile");
        dialog.setCancelable(false);


        auth=FirebaseAuth.getInstance();                            //creating the instance of FirebaseAuth to check which user has logged in
        database=FirebaseDatabase.getInstance();                    //creating the instance of FirebaseDatabase to store data about the user
        storage=FirebaseStorage.getInstance();                      //creating the instance of FirebaseStorage to store profile image of the user

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);                    //this intent is used to open an activity to get something for your activity
                intent.setType("image/*");                                      //setting that that 'something' is an image
                startActivityForResult(intent, 45);
            }
        });

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=binding.nameBox.getText().toString();
                if(name.isEmpty()){
                    binding.nameBox.setError("Please type a name");                     //if name is empty, then show the error
                    return;
                }

                dialog.show();
                if(selectedImage!=null){                                    //means image is selected

                    //this creates a folder in our FirebaseStorage with a unique id as its name
                    StorageReference reference=storage.getReference().child("Profiles").child(auth.getUid());

                    //putting the selected image in storage/profiles
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){

                                //if insertion of image is successful, get the downloadUrl which will help to store image uri(path)
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUri=uri.toString();
                                        String uid=auth.getUid();
                                        String phone=auth.getCurrentUser().getPhoneNumber();
                                        String name=binding.nameBox.getText().toString();
                                        User user=new User(uid, name, phone, imageUri);                     //creating a new user with all the details filled till now

                                        database.getReference().child("users")                              //creates a folder named users in FirebaseDatabase
                                                .child(uid)                                                 //creates a folder named with every user's uid to store data in it
                                                .setValue(user)                                             //setting the value in the folder as our user
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {

                                                    //if insertion in FirebaseStorage is successful
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        dialog.dismiss();
                                                        Intent intent= new Intent(SetupProfileActivity.this, MainActivity.class);           //sending user to mainActivity
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                }
                else{
                    String uid=auth.getUid();
                    String phone=auth.getCurrentUser().getPhoneNumber();

                    User user=new User(uid, name, phone,"No Image");                     //creating a new user with all the details entered till now

                    database.getReference().child("users")                              //creates a folder named users in FirebaseDatabase
                            .child(uid)                                                 //creates a folder named with every user's uid to store data in it
                            .setValue(user)                                             //setting the value in the folder as our user
                            .addOnSuccessListener(new OnSuccessListener<Void>() {

                                //if insertion in FirebaseStorage is successful
                                @Override
                                public void onSuccess(Void unused) {
                                    dialog.dismiss();
                                    Intent intent= new Intent(SetupProfileActivity.this, MainActivity.class);           //sending user to mainActivity
                                    startActivity(intent);
                                    finish();
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data!=null){
            if(data.getData()!=null){
                binding.imageView.setImageURI(data.getData());                  //setting the image as the profile pic if an image is selected
                selectedImage=data.getData();                                   //storing the selected image
            }
        }
    }

}