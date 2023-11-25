package com.mishraaniket.project.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mishraaniket.project.databinding.ActivityOtpactivityBinding;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    ActivityOtpactivityBinding binding;
    ProgressDialog dialog;                              //this shows the loading type of screen after the send code button has been pressed.
    String phoneNumber;
    String OTPid;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityOtpactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());                          //by binding the activity, we are free to not use findViewById method and still use every elements

        getSupportActionBar().hide();

        dialog =  new ProgressDialog(this);                         //creating the dialog for loading type of screen
        dialog.setMessage("Sending OTP...");                               //setting the message to be displayed
        dialog.setCancelable(false);                                       //now user can't cancel the dialog box
        dialog.show();

        //when this activity is started, it switches the keyboard on.
        //For this, in androidManifest file, add windowSoftInputMode=StateVisible in the activity
        binding.otpView.requestFocus();

        phoneNumber=getIntent().getStringExtra("phoneNumber");
        binding.phoneLbl.setText("Verify " + phoneNumber);                      //setting the phone number entered during verification on screen

        mAuth=FirebaseAuth.getInstance();

        initiateOtp();

       binding.continueBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(binding.otpView.getText().toString().isEmpty()){
                   Toast.makeText(getApplicationContext(), "Blank field cannot be processed", Toast.LENGTH_SHORT).show();
               }
               else if(binding.otpView.getText().toString().length()!=6){
                   Toast.makeText(getApplicationContext(), "Invalid OTP", Toast.LENGTH_SHORT).show();
               }
               else{
                   PhoneAuthCredential credential=PhoneAuthProvider.getCredential(OTPid, binding.otpView.getText().toString());
                   signInWithPhoneAuthCredential(credential);
               }
           }
       });

    }

    private void initiateOtp() {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,                                            //Phone number to verify
                60,                                                     //Timeout seconds
                TimeUnit.SECONDS,                                       //Unit of timeout
                this,                                                   //Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    //this method will be executed when sim is not on same device
                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        dialog.dismiss();                                               //dismissing the loading screen after the OTP has been sent.
                        OTPid=s;

                    }

                    //this method will be executed when sim is in the same device
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(OTPActivity.this, "Log in failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent=new Intent(OTPActivity.this, SetupProfileActivity.class);
                            startActivity(intent);
                            finishAffinity();                                                               //closes all the activities that were open before
                        } else {
                            Toast.makeText(OTPActivity.this, "Log in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}