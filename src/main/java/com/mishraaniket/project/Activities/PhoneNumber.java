package com.mishraaniket.project.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.mishraaniket.project.databinding.ActivityPhoneNumberBinding;

public class PhoneNumber extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());                          //by binding the activity, we are free to not use findViewById method and still use every elements

        getSupportActionBar().hide();

        //when the app is started, it switches the keyboard on.
        //For this, in androidManifest file, add windowSoftInputMode=StateVisible in the activity
        binding.phoneBox.requestFocus();

        binding.ccp.registerCarrierNumberEditText(binding.phoneBox);                //registering textbox with country code

        FirebaseAuth auth=FirebaseAuth.getInstance();

        //if user has already logged in, then he/she will be directed to MainActivity instead of going through OTP and stuff again.
        if(auth.getCurrentUser()!=null){
            Intent intent=new Intent(PhoneNumber.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PhoneNumber.this, OTPActivity.class);
                intent.putExtra("phoneNumber",binding.ccp.getFullNumberWithPlus().trim());              //sending phone number entered as extra information in intent
                startActivity(intent);
            }
        });

    }
}