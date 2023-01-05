package com.zeus.chatme.chatSection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.zeus.chatme.PostDetailActivity;
import com.zeus.chatme.R;
import com.zeus.chatme.databinding.DeleteLayoutBinding;
import com.zeus.chatme.databinding.ReceiverChatDialogBinding;
import com.zeus.chatme.databinding.SenderChatDialogBinding;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Message_Adapter extends RecyclerView.Adapter {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private int tap = 1;
    private Context mContext;
    private List<Chat> mChat;
    FirebaseUser fUser;
    String senderRoom;
    String receiverRoom;

    public Message_Adapter(Context mContext, List<Chat> mChat) {
        this.mContext = mContext;
        this.mChat = mChat;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.sender_chat_dialog, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.receiver_chat_dialog, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        Chat message = mChat.get(position);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        senderRoom = fUser.getUid() + message.getReceiver();
        receiverRoom = message.getReceiver() + fUser.getUid();

        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;

            long messageDate = message.getTimestamp();
            Date message_Date = new Date(messageDate);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
            String strDate = simpleDateFormat.format(message_Date);

            Calendar cal_ = Calendar.getInstance(Locale.ENGLISH);
            cal_.setTimeInMillis(messageDate);
            String fullDate = DateFormat.format("dd/MM/yyyy", cal_).toString();

            try {
                if (message.getType().equals("image")) {
                    viewHolder.binding.videoLayout.setVisibility(View.GONE);
                    viewHolder.binding.audioLayout1.setVisibility(View.GONE);
                    viewHolder.binding.imageLayout.setVisibility(View.VISIBLE);
                    viewHolder.binding.constraint3.setVisibility(View.GONE);

                    Glide.with(mContext).load(message.getMessageURL()).placeholder(R.drawable.placeholder_1).into(viewHolder.binding.chatImage);

                    //If there is no text attached to the image
                    if (message.getMessage().equals("")) {
                        viewHolder.binding.showMessageSender2.setVisibility(View.GONE);
                    } else {
                        viewHolder.binding.showMessageSender2.setVisibility(View.VISIBLE);
                        viewHolder.binding.showMessageSender2.setText(message.getMessage());
                    }

                    viewHolder.binding.senderTime2.setText(strDate.toString());

                    if (message.isIsseen()) {
                        viewHolder.binding.seenStatusIcon2.setImageResource(R.drawable.seen_icon);
                        viewHolder.binding.seenStatusIcon2.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.binding.seenStatusIcon2.setImageResource(R.drawable.unseen_icon);
                        viewHolder.binding.seenStatusIcon2.setVisibility(View.VISIBLE);
                    }
                }
                else if (message.getType().equals("video")) {
                    viewHolder.binding.videoLayout.setVisibility(View.VISIBLE);
                    viewHolder.binding.imageLayout.setVisibility(View.GONE);
                    viewHolder.binding.audioLayout1.setVisibility(View.GONE);
                    viewHolder.binding.constraint3.setVisibility(View.GONE);
                    viewHolder.binding.loadingBar.setVisibility(View.VISIBLE);

                    //If there is no text attached to the video
                    if (message.getMessage().equals("")) {
                        viewHolder.binding.showMessageSender3.setVisibility(View.GONE);
                    } else {
                        viewHolder.binding.showMessageSender3.setVisibility(View.VISIBLE);
                        viewHolder.binding.showMessageSender3.setText(message.getMessage());
                    }

                    viewHolder.binding.chatVideo.setVideoURI(Uri.parse(message.getMessageURL()));
                    viewHolder.binding.senderTime3.setText(strDate.toString());

                    //user seen or unseen message
                    if (message.isIsseen()) {
                        viewHolder.binding.seenStatusIcon3.setImageResource(R.drawable.seen_icon);
                        viewHolder.binding.seenStatusIcon3.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.binding.seenStatusIcon3.setImageResource(R.drawable.unseen_icon);
                        viewHolder.binding.seenStatusIcon3.setVisibility(View.VISIBLE);
                    }

                    viewHolder.binding.chatVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            viewHolder.binding.loadingBar.setVisibility(View.GONE);
                            viewHolder.binding.chatVideo.seekTo(1); //Let it show the first frame of the video
                            viewHolder.binding.playIV.setVisibility(View.VISIBLE);
                        }
                    });

                }
                else if (message.getType().equals("audio")) {
                    viewHolder.binding.videoLayout.setVisibility(View.GONE);
                    viewHolder.binding.imageLayout.setVisibility(View.GONE);
                    viewHolder.binding.audioLayout1.setVisibility(View.VISIBLE);
                    viewHolder.binding.constraint3.setVisibility(View.GONE);

                    viewHolder.binding.audioPlayerView2.setAudio(message.getMessageURL());
                    viewHolder.binding.senderTime4.setText(strDate.toString());

                    //user seen or unseen message
                    if (message.isIsseen()) {
                        viewHolder.binding.seenStatusIcon4.setImageResource(R.drawable.seen_icon);
                        viewHolder.binding.seenStatusIcon4.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.binding.seenStatusIcon4.setImageResource(R.drawable.unseen_icon);
                        viewHolder.binding.seenStatusIcon4.setVisibility(View.VISIBLE);
                    }
                }
                else if (message.getType().equals("text")) {
                    viewHolder.binding.videoLayout.setVisibility(View.GONE);
                    viewHolder.binding.imageLayout.setVisibility(View.GONE);
                    viewHolder.binding.audioLayout1.setVisibility(View.GONE);
                    viewHolder.binding.constraint3.setVisibility(View.VISIBLE);
                    viewHolder.binding.showMessageSender.setText(message.getMessage());

                    viewHolder.binding.senderTime.setText(strDate.toString());

                    //user seen or unseen message
                    if (message.isIsseen()) {
                        viewHolder.binding.seenStatusIcon.setImageResource(R.drawable.seen_icon);
                        viewHolder.binding.seenStatusIcon.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.binding.seenStatusIcon.setImageResource(R.drawable.unseen_icon);
                        viewHolder.binding.seenStatusIcon.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
//                Toast.makeText(mContext, "Error occurred", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            //time set in outside chatbox
            viewHolder.binding.senderDateTime.setText((fullDate + ", " + strDate));

            viewHolder.binding.chatImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext , PostDetailActivity.class)
                            .putExtra("Post_", message.getMessageURL())
                            .putExtra("Type_", "iv"));
                }
            });

            viewHolder.binding.chatVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext , PostDetailActivity.class)
                            .putExtra("Post_", message.getMessageURL())
                            .putExtra("Type_", "vv"));
                }
            });

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tap += 1;
                    if (tap % 2 == 0) {
                        viewHolder.binding.senderDateTime.setVisibility(View.VISIBLE);
                    } else  {
                        viewHolder.binding.senderDateTime.setVisibility(View.GONE);

                    }
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.delete_layout, null);
                    DeleteLayoutBinding binding = DeleteLayoutBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setView(binding.getRoot())
                            .create();

                    //If message has being deleted for everyone before
                    if(message.getMessageURL().equals("null")) {
                        binding.deleteEveryone.setVisibility(View.GONE);
                    } else{
                        binding.deleteEveryone.setVisibility(View.VISIBLE);
                    }

                    binding.deleteEveryone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message was deleted.");
                            message.setType("text");
                            message.setMessageURL("null");
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("Chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);
                            dialog.dismiss();

                            Toast.makeText(mContext, "Message Deleted", Toast.LENGTH_SHORT).show();
                        }
                    });

                    binding.deleteMe.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(null);
                            dialog.dismiss();
                            Toast.makeText(mContext, "Message Deleted", Toast.LENGTH_SHORT).show();
                        }
                    });

                    binding.cancelText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return false;
                }
            });

        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            long messageDate = message.getTimestamp();
            Date message_Date = new Date(messageDate);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
            String strDate = simpleDateFormat.format(message_Date);

            Calendar cal_ = Calendar.getInstance(Locale.ENGLISH);
            cal_.setTimeInMillis(messageDate);
            String fullDate = DateFormat.format("dd/MM/yyyy", cal_).toString();

            try {
                if (message.getType().equals("image")) {
                    viewHolder.binding.videoLayout2.setVisibility(View.GONE);
                    viewHolder.binding.audioLayout2.setVisibility(View.GONE);
                    viewHolder.binding.constraint4.setVisibility(View.GONE);
                    viewHolder.binding.imageLayout2.setVisibility(View.VISIBLE);

                    Glide.with(mContext).load(message.getMessageURL()).placeholder(R.drawable.placeholder_1).into(viewHolder.binding.chatImage);

                    //If there is no text attached to the image
                    if (message.getMessage().equals("")) {
                        viewHolder.binding.showMessageReceiver2.setVisibility(View.GONE);
                    } else {
                        viewHolder.binding.showMessageReceiver2.setVisibility(View.VISIBLE);
                        viewHolder.binding.showMessageReceiver2.setText(message.getMessage());
                    }

                    viewHolder.binding.receiverTime2.setText(strDate.toString());
                }
                else if (message.getType().equals("video")) {
                    viewHolder.binding.imageLayout2.setVisibility(View.GONE);
                    viewHolder.binding.audioLayout2.setVisibility(View.GONE);
                    viewHolder.binding.constraint4.setVisibility(View.GONE);
                    viewHolder.binding.loadingBar.setVisibility(View.VISIBLE);
                    viewHolder.binding.videoLayout2.setVisibility(View.VISIBLE);
                    //If there is no text attached to the image
                    if (message.getMessage().equals("")) {
                        viewHolder.binding.showMessageReceiver3.setVisibility(View.GONE);
                    } else {
                        viewHolder.binding.showMessageReceiver3.setVisibility(View.VISIBLE);
                        viewHolder.binding.showMessageReceiver3.setText(message.getMessage());
                    }

                    viewHolder.binding.chatVideo.setVideoURI(Uri.parse(message.getMessageURL()));
                    viewHolder.binding.receiverTime3.setText(strDate.toString());

                    viewHolder.binding.chatVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            viewHolder.binding.loadingBar.setVisibility(View.GONE);
                            viewHolder.binding.chatVideo.seekTo(1); //Let it show the first frame of the video
                            viewHolder.binding.playIV.setVisibility(View.VISIBLE);
                        }
                    });

//                    viewHolder.binding.chatVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//                        @Override
//                        public boolean onError(MediaPlayer mp, int what, int extra) {
//                            Toast.makeText(mContext, "Video failed to load. Please refresh page.", Toast.LENGTH_SHORT).show();
//                            return false;
//                        }
//                    });

                }
                else if (message.getType().equals("audio")) {
                    viewHolder.binding.videoLayout2.setVisibility(View.GONE);
                    viewHolder.binding.imageLayout2.setVisibility(View.GONE);
                    viewHolder.binding.constraint4.setVisibility(View.GONE);
                    viewHolder.binding.audioLayout2.setVisibility(View.VISIBLE);

                    viewHolder.binding.audioPlayerView.setAudio(message.getMessageURL());
                    viewHolder.binding.receiverTime4.setText(strDate.toString());

                }
                else if (message.getType().equals("text")) {
                    viewHolder.binding.videoLayout2.setVisibility(View.GONE);
                    viewHolder.binding.audioLayout2.setVisibility(View.GONE);
                    viewHolder.binding.imageLayout2.setVisibility(View.GONE);
                    viewHolder.binding.constraint4.setVisibility(View.VISIBLE);

                    viewHolder.binding.showMessageReceive.setText(message.getMessage());

                    viewHolder.binding.receiverTime.setText(strDate.toString());
                }
            } catch (Exception e) {
//                Toast.makeText(mContext, "Error occurred", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            viewHolder.binding.receiverDateTime.setText((fullDate + ", " + strDate));

            viewHolder.binding.chatImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext , PostDetailActivity.class)
                            .putExtra("Post_", message.getMessageURL())
                            .putExtra("Type_", "iv"));
                }
            });

            viewHolder.binding.chatVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext , PostDetailActivity.class)
                            .putExtra("Post_", message.getMessageURL())
                            .putExtra("Type_", "vv"));
                }
            });

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tap += 1;
                    if (tap % 2 == 0) {
                        viewHolder.binding.receiverDateTime.setVisibility(View.VISIBLE);
                    } else  {
                        viewHolder.binding.receiverDateTime.setVisibility(View.GONE);

                    }
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    View view = LayoutInflater.from(mContext).inflate(R.layout.delete_layout, null);
                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setView(view)
                            .create();

                    TextView deleteMe = view.findViewById(R.id.deleteMe);
                    TextView deleteEveryone = view.findViewById(R.id.deleteEveryone);
                    TextView cancel = view.findViewById(R.id.cancelText);

                    deleteEveryone.setVisibility(View.GONE);

                    deleteMe.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference("Chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(null);
                            dialog.dismiss();
                            Toast.makeText(mContext, "Message Deleted", Toast.LENGTH_LONG).show();
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
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
        return mChat.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        ReceiverChatDialogBinding binding;
        public ReceiverViewHolder(@NotNull View itemView) {
            super(itemView);

            binding = ReceiverChatDialogBinding.bind(itemView);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        SenderChatDialogBinding binding;

        public SenderViewHolder(@NotNull View itemView) {
            super(itemView);
            binding = SenderChatDialogBinding.bind(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

}