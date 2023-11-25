package com.mishraaniket.project.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mishraaniket.project.Adapters.MessagesAdapter;
//import com.mishraaniket.chatapp.Manifest;
import com.mishraaniket.project.Models.Message;
import com.mishraaniket.project.R;
import com.mishraaniket.project.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.CALL_PHONE;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;

    static int PERMISSION_CODE=1;

    String senderUid;
    String receiverUid;
    String senderName;

    ProgressDialog dialog;                                     //for showing the loading screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);                   //since there is already a toolbar available, it will throw an error
                                                                //so we need to create a new theme with no supportActionBar means no toolbar

        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        dialog=new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        messages=new ArrayList<>();

        String name=getIntent().getStringExtra("name");
        String profileImage=getIntent().getStringExtra("image");
        String token=getIntent().getStringExtra("token");

        binding.name.setText(name);                                 //setting the username of user on toolbar
        Glide.with(ChatActivity.this)                        //setting the profile image of user on toolbar's imageView i.e. profile
                .load(profileImage)
                .placeholder(R.drawable.avatar)
                .into(binding.profile);

        //when the back arrow is clicked, finish or destroy the current activity
        binding.imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        receiverUid=getIntent().getStringExtra("uid");
        senderUid= FirebaseAuth.getInstance().getUid();



        //when the receiver is online, we need to keep his/her activity status to online.
        //For that, we see in presence folder in our database that whether the folder with receiverUid name has its value set to online or offline
        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status=snapshot.getValue(String.class);                                  //if snapshot exist, means if receiverUid exists named folder exists
                                                                                                    // then retrieve its status value
                    if(!status.isEmpty()) {
                        if(status.equals("Offline")){                                                   //if status if offline then remove the status
                            binding.status.setVisibility(View.GONE);
                        }
                        else {
                            binding.status.setText(status);                                                 // else setting the retrieved status on chatActivity toolbar
                            binding.status.setVisibility(View.VISIBLE);                                     //setting the visibility to visible
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        senderRoom=senderUid + receiverUid;
        receiverRoom=receiverUid + senderUid;

        adapter=new MessagesAdapter(this, messages, senderRoom, receiverRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1: snapshot.getChildren()){
                            Message message=snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //sending the message and storing it in senderRoom and receiverRoom
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt=binding.messageBox.getText().toString();

                Date date=new Date();
                Message message=new Message(messageTxt, senderUid, date.getTime());
                binding.messageBox.setText("");

                String randomKey=database.getReference().push().getKey();                           //creating a common randomKey for sender and receiver room.

                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());

                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)                              //this randomKey will be same for senderRoom and receiverRoom unlike push which creates a new key everytime
                                                                        //so a common folder will be generated for both the room
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                database.getReference().child("users").child(senderUid).child("name").addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        senderName=snapshot.getValue(String.class);
                                                        sendNotification(senderName, message.getMessage(), token);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        });

                            }
                        });
            }
        });


        binding.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                makePhoneCall();
            }
        });

        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,25);                         //startActivityForResult is used so that we can perform some operation or get some result
                //after the intent work has been completed
            }
        });


        final Handler handler=new Handler();

        //to see whether user is typing or not
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                //setting the value to typing when user types something in messageBox and since sender is the one who sends the message we change senderUid value
                database.getReference().child("presence").child(senderUid).setValue("typing...");

                handler.removeCallbacksAndMessages(null);

                //if the user is not typing for more than 1 second then senderUid value will be set to Online
                handler.postDelayed(userStoppedTyping, 1000);
            }

            Runnable userStoppedTyping= new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };

        });

        getSupportActionBar().setDisplayShowTitleEnabled(false);                                        //now the username that was showing will not be shown
//        getSupportActionBar().setTitle(name);                                                         //setting the username on toolbar
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);                                        //for arrow to go on previous activity
    }

    //for sending the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==25){
            if(data!=null){
                if(data.getData()!=null){
                    Uri selectedImage=data.getData();                       //storing the url of the selected image
                    Calendar calendar=Calendar.getInstance();               //creating the instance of calendar to create a folder of time at which image is send in chats folder

                    //creating a reference object to store the image in firebaseStorage and giving it a path to chats folder and creating a new folder of time as its name in it
                    StorageReference reference=storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");

                    dialog.show();                                      //displaying the uploading screen

                    //putting or storing the file at that location
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            dialog.dismiss();                                       //closing the uploading screen

                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        //if file is stored in the database successfully, get the downloadUrl and store the path in filePath variable and show uploading screen.
                                        //downloadUrl is the url of where the image is stored in FirebaseStorage.
                                        String filePath=uri.toString();

                                        String messageTxt=binding.messageBox.getText().toString();

                                        Date date=new Date();
                                        Message message=new Message(messageTxt, senderUid, date.getTime());
                                        message.setMessage("photo");                                                //setting the message as photo
                                        message.setImageUrl(filePath);                                              //setting the imageUrl that we have in filePath variable
                                        binding.messageBox.setText("");

                                        String randomKey=database.getReference().push().getKey();                           //creating a common randomKey for sender and receiver room.

                                        HashMap<String, Object> lastMsgObj = new HashMap<>();                               //creating a hashmap to store last msg time and last msg
                                        lastMsgObj.put("lastMsg", message.getMessage());
                                        lastMsgObj.put("lastMsgTime", date.getTime());

                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);        //putting the lastMsgObj in chats senderRoom
                                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);      //putting the lastMsgObj in chats receiverRoom

                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)                              //this randomKey will be same for senderRoom and receiverRoom unlike push which creates a new key everytime
                                                //so a common folder will be generated for both the room
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        database.getReference().child("chats")
                                                                .child(receiverRoom)
                                                                .child("messages")
                                                                .child(randomKey)
                                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                    }
                                                                });

                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }



    }

    //when the chatActivity is stopped
    @Override
    protected void onPause() {
        super.onPause();

        //we are collecting the userId of the current user and setting its value to offline so that its activity status can be seen as offline on other devices
        String currentId=FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }

    //to make phone calls
    private void makePhoneCall(){

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        database.getReference().child("users").child(receiverUid).child("phoneNumber").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String number=snapshot.getValue(String.class);

                //number contains +91 so we use substring to get 10 digit number
                String phoneNumber=number.substring(3);
                if (ContextCompat.checkSelfPermission(ChatActivity.this,CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ChatActivity.this,new String[]{CALL_PHONE},PERMISSION_CODE);
                }

                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phoneNumber)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //to send notifications
    void sendNotification(String name, String message, String token)  {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";

            //creating a json object because apis use json format
            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);

            JSONObject notificationData=new JSONObject();
            notificationData.put("notification",data);
            notificationData.put("to",token);

            JsonObjectRequest request=new JsonObjectRequest(url, notificationData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
//                    Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//                    Toast.makeText(ChatActivity.this, "failure", Toast.LENGTH_SHORT).show();

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map=new HashMap<>();
                    String key="Key=AAAApfuRrZU:APA91bEJpobu2ElOsW6ZFM8VJv08c_xFKYS-IFG8LtWV1BxWOpRdG37cz0jdUGQOOpeoH0DET_y7urUKOCKP6PVvb-FzmJ0Guxyx2yjHB3gur8WX2Ga1OWf8eRtXbq95yg9vQOc5zu3I";
                    map.put("Content-Type","application/json");
                    map.put("Authorization", key);

                    return map;
                }
            };

            queue.add(request);

        }
        catch(Exception exception){

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

}