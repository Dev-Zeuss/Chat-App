package com.zeus.chatme.chatSection;

import android.Manifest;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.zeus.adbucks.NetworkConnectionLiveData;
import com.zeus.chatme.ChatHomeActivity;
import com.zeus.chatme.PostDetailActivity;
import com.zeus.chatme.PrePostActivity;
import com.zeus.chatme.R;
import com.zeus.chatme.databinding.ActivityChatDetailBinding;
import com.zeus.chatme.models.User;
import com.zeus.chatme.notifications.APIService;
import com.zeus.chatme.notifications.Client;
import com.zeus.chatme.notifications.Data;
import com.zeus.chatme.notifications.MyResponse;
import com.zeus.chatme.notifications.Sender;
import com.zeus.chatme.notifications.Token;

import java.io.File;
import java.io.IOException;
import java.security.Permissions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDetailActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView userName, onlineStatus;
    EmojiEditText text_send;
    ImageView btn_send, emojiKeyboardBtn, sendImageBtn;
    Permissions permissions;

    ActivityChatDetailBinding binding;
    FirebaseDatabase reference;
    DatabaseReference dbReference, database;
    FirebaseUser fUser;

    Message_Adapter messageAdapter;
    List<Chat> mChat;
    RecyclerView recyclerView;

    ViewTreeObserver.OnGlobalLayoutListener listener;

    ValueEventListener seenListener;

    MediaRecorder mediaRecorder;
    RecordButton recordBtn;
    RecordView recordView;
    private StorageReference storageReference;

    String userId, receiverImage;

    String audioPath;
    String senderRoom, choice, name;
    String receiverRoom;

    private DatabaseReference connectedReference;
    private DatabaseReference onlineStatusRef;
    private int AUDIO_REQUEST_CODE = 333;
    private int STORAGE_REQUEST_CODE = 777;
    boolean seenvalue;

    int click = 1;
    int tap;
    APIService apiService;

    private NetworkConnectionLiveData networkConnection;
    private ProgressDialog dialog;
    boolean notify = false;
    boolean state = false;

    private Runnable runnable;
    private int delay = 100;
    private Handler handler = new Handler();
    private Handler handler2 = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getIntent().getStringExtra("userId");

        tap = getIntent().getIntExtra("tap", 0);
        String sentMessage = getIntent().getStringExtra("message");
        String sentUri = getIntent().getStringExtra("uri");
        String sentUriType = getIntent().getStringExtra("type");

        if (userId != null) {
            saveReceiverID();
        }
        loadReceiverUid();

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        dialog = new ProgressDialog(this, R.style.MyDialogStyle3);
        dialog.setMessage("Sending");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setIndeterminate(false);
        networkConnection = new NetworkConnectionLiveData(ChatDetailActivity.this);
        profile_image = binding.chatDetailUserDp;
        userName = binding.chatDetailsUserName;
        text_send = binding.enterMessageET;
        btn_send = binding.sendMessageBtn;
        recordBtn = binding.audioBtn;
        recordView = binding.recordView;
        sendImageBtn = binding.sendMessageBtn;
        emojiKeyboardBtn = binding.emojiKeyboardBtn;
        onlineStatus = binding.chatDetailsUserOnlineStatus;
        permissions = new Permissions();

        recyclerView = binding.chatDetailRecyclerView;
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        senderRoom = fUser.getUid() + userId;
        receiverRoom = userId + fUser.getUid();

        dbReference = FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverRoom).child("messages");
        reference = FirebaseDatabase.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        try  {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //set user's details when chat screen is open
            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    name = user.getUsername();
                    userName.setText(user.getUsername());

                    receiverImage = user.getImageURL();

                    if (receiverImage.equals("default")) {
                        profile_image.setImageResource(R.drawable.default_dp_2);
                    } else {
                        Picasso.get().load(receiverImage).into(profile_image);
                    }

                }

                @Override
                public void onCancelled(@NotNull DatabaseError error) {

                }
            });

            readMessages(fUser.getUid(), userId);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //online and typing status
        networkConnection.observe(ChatDetailActivity.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                if (isConnected) {
                    binding.chatDetailsUserOnlineStatus.setTextColor(getResources().getColor(R.color.white));
                    binding.chatDetailsUserOnlineStatus.setVisibility(View.VISIBLE);
                    text_send.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            reference.getReference("Users").child(fUser.getUid()).child("status").setValue("typing...");
                            handler.removeCallbacksAndMessages(null);
                            handler.postDelayed(userStoppedTyping, 1000);
                        }

                        Runnable userStoppedTyping = new Runnable() {
                            @Override
                            public void run() {
                                reference.getReference("Users").child(fUser.getUid()).child("status").setValue("Online");
                            }
                        };

                    });
                } else {
                    binding.chatDetailsUserOnlineStatus.setTextColor(getResources().getColor(R.color.main_blue));
                    binding.chatDetailsUserOnlineStatus.setVisibility(View.GONE);
                }
            }
        });

        onUserDisconnect();

        //Initialize Emoji Popup
        EmojiPopup popup = EmojiPopup.Builder.fromRootView(binding.rootView)
                .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
                .build(text_send);

        emojiKeyboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toggle between text and emoji
                click += 1;

                if (click % 2 == 0) {
                    emojiKeyboardBtn.setImageResource(R.drawable.ic_keyboard);
                } else {
                    emojiKeyboardBtn.setImageResource(R.drawable.ic_emoji3);
                }
                popup.toggle();
            }
        });

        //Check if Keyboard is visible or not to correct emoji icon
        listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rec = new Rect();

                findViewById(R.id.root_view).getRootView().getWindowVisibleDisplayFrame(rec);

                //Finding Screen Height
                int screenHeight = findViewById(R.id.root_view).getRootView().getHeight();

                //Finding Keyboard Height
                int keypadHeight = screenHeight - rec.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    String string = "visible";
                } else {
                    if (click % 2 == 0) {
                        emojiKeyboardBtn.setImageResource(R.drawable.ic_emoji3);
                    }
//                    Toast.makeText(getContext(), "Not Visible", Toast.LENGTH_SHORT).show();
                }
            }
        };

        userName.setSelected(true);

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatDetailActivity.this , PostDetailActivity.class)
                        .putExtra("Post_", receiverImage)
                        .putExtra("Type_", "iv"));

            }
        });


        //Send image or video
        if (tap == 1) {
            //Check for network first
            dialog.show();

            try {
//              set message id
                String randomKey = reference.getReference().push().getKey();

                notify = true;

                Chat chat = new Chat(fUser.getUid(), userId, sentMessage, "");
                chat.setTimestamp(new Date().getTime());
                chat.setMessageURL(sentUri);
                if (sentUriType.equals("image"))
                    chat.setType("image");
                else
                    chat.setType("video");
                chat.setMessageId(randomKey);

                text_send.setText("");

                dialog.dismiss();
                reference.getReference().child("Chats").child(senderRoom).child("messages").child(randomKey).setValue(chat)
                        .addOnSuccessListener(success -> {
                            reference.getReference().child("Chats").child(receiverRoom).child("messages").child(randomKey).setValue(chat);

                            MediaPlayer mediaPlayer = MediaPlayer.create(ChatDetailActivity.this, R.raw.message_send_tone);
                            mediaPlayer.start();
                        });

                //Add users to chat home page fragment of both sender and receiver
                addToChatPage();

                //Handle notifications
                reference.getReference().child("Users").child(fUser.getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange( @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                if (notify) {
                                    sendNotification(userId, user.getUsername(), sentMessage, sentUriType);
                                }
                                notify= false;
                            }

                            @Override
                            public void onCancelled( @org.jetbrains.annotations.NotNull DatabaseError error) {

                            }
                        });

            } catch (Exception e) {
                dialog.dismiss();
                Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        binding.chatDetailsBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatDetailActivity.this, ChatHomeActivity.class));
                finish();
                Animatoo.animateSlideRight(ChatDetailActivity.this);
            }
        });

        binding.imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choice = "image";
                binding.imageBtn.startAnimation(AnimationUtils.loadAnimation(ChatDetailActivity.this, R.anim.click));
                if( ActivityCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission();
                } else {
                    startActivity(new Intent(ChatDetailActivity.this, PrePostActivity.class)
                            .putExtra("__CHOICE_", "image"));
                    finish();
                }
            }
        });

        binding.videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choice = "video";
                binding.videoBtn.startAnimation(AnimationUtils.loadAnimation(ChatDetailActivity.this, R.anim.click));
                if( ActivityCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission();
                } else {
                    startActivity(new Intent(ChatDetailActivity.this, PrePostActivity.class)
                            .putExtra("__CHOICE_", "video"));
                    finish();
                }
            }
        });

        binding.sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(ChatDetailActivity.this, R.anim.click);
                binding.sendMessageBtn.startAnimation(animation);

                if (!binding.enterMessageET.getText().toString().trim().isEmpty()) {
                    try {
                        notify = true;

                        FirebaseDatabase reference2 = FirebaseDatabase.getInstance();

                        String message = binding.enterMessageET.getText().toString();

                        //Set message ID
                        String randomKey = reference2.getReference().push().getKey();

                        Chat chat = new Chat(fUser.getUid(), userId, message, "");
                        chat.setTimestamp(new Date().getTime());
                        chat.setMessageURL("none");
                        chat.setType("text");
                        chat.setMessageId(randomKey);

                        reference2.getReference().child("Chats").child(senderRoom).child("messages").child(randomKey).setValue(chat)
                                .addOnSuccessListener(success -> {
                                    reference2.getReference().child("Chats").child(receiverRoom).child("messages").child(randomKey).setValue(chat);
                                    MediaPlayer mediaPlayer = MediaPlayer.create(ChatDetailActivity.this, R.raw.message_send_tone);
                                    mediaPlayer.start();
                                });


                        text_send.setText("");
                        addToChatPage();

                        //Handle notifications
                        reference.getReference().child("Users").child(fUser.getUid())
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange( @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                                        User user = snapshot.getValue(User.class);
                                        if (notify) {
                                            sendNotification(userId, user.getUsername(), message, "text");
                                        }
                                        notify= false;
                                    }

                                    @Override
                                    public void onCancelled( @org.jetbrains.annotations.NotNull DatabaseError error) {

                                    }
                                });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        seenMessage(userId);

    }

    private void sendNotification(String receiver, String username, String message, String type) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(fUser.getUid(), R.drawable.notification_icon, message, username, userId, type);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            //notification failed
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable throwable) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled( @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }

    private void currentUser(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences("USER_PREF", MODE_PRIVATE).edit();
        editor.putString("currentuser", userId);
        editor.apply();
    }

    private void addToChatPage() {
        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("SenderList").child(fUser.getUid()).child(userId);
        DatabaseReference receiverRef = FirebaseDatabase.getInstance().getReference("SenderList").child(userId).child(fUser.getUid());

        senderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    senderRef.child("id").setValue(userId).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            receiverRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        receiverRef.child("id").setValue(fUser.getUid());
                                    }
                                }

                                @Override
                                public void onCancelled(@NotNull DatabaseError error) {

                                }
                            });

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {

            }
        });

    }

    private void seenMessage(String userid) {
        try {
            seenListener = dbReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        assert chat != null;
                        if (chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userid)) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isseen", true);
                            snapshot.getRef().updateChildren(hashMap);
                        }
                    }
                }

                @Override
                public void onCancelled(@NotNull DatabaseError error) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void readMessages(String myId, String userId) {
        mChat = new ArrayList<>();

        try {
            database = FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("messages");
            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    mChat.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        assert chat != null;
                            mChat.add(chat);
                            //play tone when message is received

                        messageAdapter = new Message_Adapter(ChatDetailActivity.this, mChat);
                        recyclerView.setAdapter(messageAdapter);

                    }

                }

                @Override
                public void onCancelled(@NotNull DatabaseError error) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onUserDisconnect() {
        onlineStatusRef = reference.getReference().child("Users").child(fUser.getUid()).child("status");
        connectedReference = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    onlineStatusRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    onlineStatusRef.setValue("Online");
                }
                else {
                    onlineStatusRef.setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Save the receiver ID and other details so that when sender exits the activity when trying to send image or video , tthe details still remain
    private void saveReceiverID() {
        SharedPreferences.Editor editor = getSharedPreferences("SAVE_R_ID", Context.MODE_PRIVATE).edit();
        editor.putString("SAVE_R_ID_", userId);
        editor.apply();
    }

    private void loadReceiverUid() {
        SharedPreferences sharedPref = getSharedPreferences("SAVE_R_ID", Context.MODE_PRIVATE);
        userId = sharedPref.getString("SAVE_R_ID_", null);
    }

    private void initView() {
        recordBtn.setRecordView(recordView);
        recordBtn.setListenForRecord(false);

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                binding.audioBtn.startAnimation(AnimationUtils.loadAnimation(ChatDetailActivity.this, R.anim.click));
                if( ActivityCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    requestAudioPermission();
                } else {
                    recordBtn.setListenForRecord(true);
                }
            }
        });

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                setUpRecording();

                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                    state = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                binding.chatLayout.setVisibility(View.GONE);
                recordView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancel() {
                try {
                    mediaRecorder.reset();
                    mediaRecorder.release();
                    File file = new File(audioPath);
                    if (file.exists()) {
                        file.delete();
                    }

                    recordView.setVisibility(View.GONE);
                    binding.chatLayout.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //Check if the media recorder is currently running before we stop to avoid app from crashing
            @Override
            public void onFinish(long recordTime) {
                try {
                    if (state) {
                        mediaRecorder.stop();
                        mediaRecorder.release();

                        state = false;
                        recordView.setVisibility(View.GONE);
                        binding.chatLayout.setVisibility(View.VISIBLE);

                        sendRecordingMessage(audioPath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onLessThanSecond() {

                try {
                    mediaRecorder.reset();
                    mediaRecorder.release();

                    File file = new File(audioPath);
                    if (file.exists()) {
                        file.delete();
                    }

                    recordView.setVisibility(View.GONE);
                    binding.chatLayout.setVisibility(View.VISIBLE);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    binding.recordView.setVisibility(View.GONE);
                    binding.chatLayout.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private void setUpRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        //To support android 10 and above
        try {
            File file = new File(this.getFilesDir(), "Chat_Me/Media/Recordings");
            if (!file.exists())
                file.mkdirs();

            audioPath = file.getAbsolutePath() + System.currentTimeMillis() + ".3gp";

        } catch (Exception e) {
            e.printStackTrace();
        }

        mediaRecorder.setOutputFile(audioPath);

    }

    private void sendRecordingMessage(String audioPath) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        storageReference = FirebaseStorage.getInstance().getReference( "/Media/Recordings/" + fUser.getUid() + "/" + System.currentTimeMillis());
        Uri audioFile = Uri.fromFile(new File(audioPath));
        storageReference.putFile(audioFile).addOnSuccessListener(success -> {
            Task<Uri> audioUrl = success.getStorage().getDownloadUrl();

            audioUrl.addOnCompleteListener(path -> {
                if (path.isSuccessful()) {
                    String url = path.getResult().toString();

                    try {
//                        set message id
                        String randomKey = reference.getReference().push().getKey();

                        Chat chat = new Chat(fUser.getUid(), userId, "", "");
                        chat.setTimestamp(new Date().getTime());
                        chat.setMessageURL(url);
                        chat.setType("audio");
                        chat.setMessageId(randomKey);

                        reference.getReference().child("Chats").child(senderRoom).child("messages").child(randomKey).setValue(chat)
                                .addOnSuccessListener(finish -> {
                                    reference.getReference().child("Chats").child(receiverRoom).child("messages").child(randomKey).setValue(chat);

                                    MediaPlayer mediaPlayer = MediaPlayer.create(ChatDetailActivity.this, R.raw.message_send_tone);
                                    mediaPlayer.start();
                                });

                        //Add users to chat home page fragment of both sender and receiver
                        addToChatPage();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
//            }
//        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        currentUser(userId);
        String currentID = FirebaseAuth.getInstance().getUid();
        reference.getReference("Users").child(currentID).child("status").setValue("Online");

        View view = findViewById(R.id.root_view);
        view.getViewTreeObserver().addOnGlobalLayoutListener(listener);

        try {
            handler2.postDelayed(runnable = new Runnable() {
                @Override
                public void run()    {
                    handler2.postDelayed(runnable, delay);

//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
                    reference.getReference("Users").child(userId).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            Object status = dataSnapshot.child("status").getValue();

                            if (status != null) {
                                if (status.equals("Online") || status.equals("typing...")) {
                                    onlineStatus.setText(String.valueOf(status));
//                                                    onlineStatus.setVisibility(View.VISIBLE);
                                } else if (!status.equals("Online") || !status.equals("typing...")) {
                                    //convert timestamp to date
                                    try {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                long messageDate = Long.parseLong(String.valueOf(status));
                                                Date message_Date = new Date(messageDate);
                                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
                                                String strDate = simpleDateFormat.format(message_Date);

                                                Calendar cal_ = Calendar.getInstance(Locale.ENGLISH);
                                                cal_.setTimeInMillis(messageDate);
                                                String fullDate = DateFormat.format("dd/MM/yyyy", cal_).toString();

                                                Calendar cal_2 = Calendar.getInstance(Locale.ENGLISH);
                                                cal_2.setTimeInMillis(System.currentTimeMillis());
                                                String fullDate2 = DateFormat.format("dd/MM/yyyy", cal_2).toString();

                                                if (fullDate.equals(fullDate2))
                                                    onlineStatus.setText("Last seen - " + strDate + " , today");
                                                else
                                                    onlineStatus.setText("Last seen - " + strDate + " , " + fullDate);
                                            }
                                        }, 50);
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }
//                                                    onlineStatus.setVisibility(View.VISIBLE);
                                }
//                                                onlineStatus.setVisibility(View.VISIBLE);
                            }
                        }
                    });
//                                }
//                            });
//
//                            try {
//                                Thread.sleep(500);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();
                }
            }, delay);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        View view = findViewById(R.id.root_view);
        dbReference.removeEventListener(seenListener);
        currentUser("none");
        String currentID = FirebaseAuth.getInstance().getUid();
        reference.getReference("Users").child(currentID).child("status").setValue(String.valueOf(System.currentTimeMillis()));

        try {
            if (dialog != null) {
                dialog.dismiss();
//                dialog = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
    }

    @Override
    protected void onDestroy() {
        networkConnection.removeObservers(this);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (choice.equals("image")) {
                    startActivity(new Intent(ChatDetailActivity.this, PrePostActivity.class)
                            .putExtra("__CHOICE_", "image"));
                    finish();
                } else {
                    startActivity(new Intent(ChatDetailActivity.this, PrePostActivity.class)
                            .putExtra("__CHOICE_", "video"));
                    finish();
                }
            } else {
                requestStoragePermission();
            }
        } else if (requestCode == AUDIO_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.audioBtn.setListenForRecord(true);
            } else {
                requestAudioPermission();
            }
        }
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(ChatDetailActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA}, STORAGE_REQUEST_CODE);
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(ChatDetailActivity.this, new String[] {Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ChatHomeActivity.class));
        finish();
        Animatoo.animateSlideRight(ChatDetailActivity.this);
    }

}
