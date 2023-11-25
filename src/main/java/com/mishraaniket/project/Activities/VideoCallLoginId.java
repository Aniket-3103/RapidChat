package com.mishraaniket.project.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mishraaniket.project.databinding.ActivityVideoCallLoginIdBinding;
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;

public class VideoCallLoginId extends AppCompatActivity {

    ActivityVideoCallLoginIdBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityVideoCallLoginIdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        String Username= getIntent().getStringExtra("Username");
        binding.UserName.setText("Enter the video call room with ID: " + Username);                                  //setting the current UserID

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMyService(Username);
                Intent intent=new Intent(VideoCallLoginId.this, VideoCallingActivity.class);
                intent.putExtra("Username",Username);
                startActivity(intent);
            }
        });
    }

    public void startMyService(String userId){
        Application application=getApplication();
        long appID=781317475;               //268228071
        String appSign="fd4a439b6ed2e8087105fef16f57b3fcb1a3e001ced173555e40e30d41510354";
        //a2d4567ccc8585a9c4c06957fae9fd01095ebbfd3fd64e85d952f6e6218eae38
        String userID=userId;
        String userName=userId;

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig=new ZegoUIKitPrebuiltCallInvitationConfig();
        callInvitationConfig.notifyWhenAppRunningInBackgroundOrQuit=true;
        ZegoNotificationConfig notificationConfig=new ZegoNotificationConfig();
        notificationConfig.sound="zego_uikit_sound_call";
        notificationConfig.channelID="CallInvitation";
        notificationConfig.channelName="CallInvitation";
        ZegoUIKitPrebuiltCallInvitationService.init(application, appID, appSign, userID, userName,callInvitationConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoUIKitPrebuiltCallInvitationService.unInit();
    }
}