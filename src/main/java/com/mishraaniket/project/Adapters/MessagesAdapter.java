package com.mishraaniket.project.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.mishraaniket.project.Models.Message;
import com.mishraaniket.project.R;
import com.mishraaniket.project.databinding.DeleteDialogBinding;
import com.mishraaniket.project.databinding.ItemReceivedBinding;
import com.mishraaniket.project.databinding.ItemSentBinding;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter
{

    Context context;
    ArrayList<Message> messages;

    final int ITEM_SENT=1;
    final int ITEM_RECEIVE=2;

    String senderRoom;
    String receiverRoom;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom){
        this.messages=messages;
        this.context=context;
        this.senderRoom=senderRoom;
        //System.out.println(senderRoom);
        this.receiverRoom=receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==ITEM_SENT){
            View view= LayoutInflater.from(context).inflate(R.layout.item_sent,parent, false);
            return new SentViewHolder(view);
        }
        else{
            View view= LayoutInflater.from(context).inflate(R.layout.item_received,parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message=messages.get(position);

        //If user Id matches with message sender's Id, then message is sent.
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return ITEM_SENT;
        }
        else{
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message=messages.get(position);

        int reactions[]=new int[]{                                          //creating the array with all the reactions
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)                            //creating a reactionConfig, passing our array to it and building it.
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {

            if(pos<0){
                return  false;
            }

            //if holder is sender then, typecasting holder to sentViewHolder and setting the image on it from our array at that position
            if(holder.getClass()==SentViewHolder.class){
                SentViewHolder viewHolder=(SentViewHolder)holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);                         //setting the visibility of feeling to visible
            }

            //if holder is receiver then, typecasting holder to receiverViewHolder and setting the image on it from our array at that position
            else{
                ReceiverViewHolder viewHolder=(ReceiverViewHolder)holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);                         //setting the visibility of feeling to visible
            }

            message.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);


            return true;                        // true is closing popup, false is requesting a new selection
        });

        if(holder.getClass()==SentViewHolder.class){
            SentViewHolder viewHolder=(SentViewHolder)holder;

            //if the message that was sent was photo,
            if(message.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);                   //setting the visibility of image to visible, so image can be seen.
                viewHolder.binding.message.setVisibility(View.GONE);                    //setting the visibility of message to gone, so that only image is seen.


                //showing the image that was sent instead of avatar image through Glide which will fetch imageUrl from message.getImageUrl method and show it into imageView on screen
                //Glide is used to load image from any online resource
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);
            }


            viewHolder.binding.message.setText(message.getMessage());

            //reactions on image
            if(message.getFeeling()>=0){
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else{
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            //when the message is touched, pop reactions list
            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

            //when the image is touched, pop reactions list
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();

                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message is removed.");
                            message.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);
                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(null);
                            dialog.dismiss();
                        }
                    });

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return false;
                }
            });

        }
        else{
            ReceiverViewHolder viewHolder=(ReceiverViewHolder)holder;

            //if the message that was received was photo,
            if(message.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);                   //setting the visibility of image to visible, so image can be seen.
                viewHolder.binding.message.setVisibility(View.GONE);                    //setting the visibility of message to gone, so that only image is seen.


                //showing the image that was received instead of avatar image through Glide which will fetch imageUrl from message.getImageUrl method and show it into imageView on screen
                //Glide is used to load image from any online resource
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);
            }

            viewHolder.binding.message.setText(message.getMessage());

            if(message.getFeeling()>=0){
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else{
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            //when the message is  touched, pop reactions list
            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

            //when image is touched, pop reactions list
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();

                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message is removed.");
                            message.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);

                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(null);
                            dialog.dismiss();
                        }
                    });

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return false;
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder{

        ItemSentBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding=ItemSentBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{

        ItemReceivedBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding=ItemReceivedBinding.bind(itemView);
        }
    }

}
