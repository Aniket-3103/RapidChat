package com.mishraaniket.project.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.mishraaniket.project.R;
import com.mishraaniket.project.databinding.ActivityVideoCallingBinding;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.util.Collections;

public class VideoCallingActivity extends AppCompatActivity {

    ZegoSendCallInvitationButton callbtn;
    ActivityVideoCallingBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityVideoCallingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String caller=getIntent().getStringExtra("Username");
        callbtn=findViewById(R.id.callBtn);

        binding.UserName.setText("You are: " + caller);

        binding.targetUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                startVideoCall(binding.targetUser.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZegoUIKitPrebuiltCallInvitationService.unInit();
                finish();
            }
        });

    }

    public void startVideoCall(String targetUserId){
        callbtn.setIsVideoCall(true);
        callbtn.setResourceID("zego_uikit_call");
        callbtn.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetUserId, targetUserId)));
    }

}