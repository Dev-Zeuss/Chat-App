package com.zeus.chatme;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.zeus.adbucks.NetworkConnectionLiveData;
import com.zeus.chatme.adapters.ChatHomeAdapter;
import com.zeus.chatme.bluetoothMode.BluetoothMainActivity;
import com.zeus.chatme.databinding.ActivityChatHomeBinding;
import com.zeus.chatme.models.ChatsList;
import com.zeus.chatme.models.User;
import com.zeus.chatme.notifications.Token;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChatHomeActivity extends AppCompatActivity {

    private ActivityChatHomeBinding binding;
    private RecyclerView recyclerView;
    private ChatHomeAdapter userAdapter;
    private List<User> mUsers;

    private NetworkConnectionLiveData networkConnection;
    private List<ChatsList> chatsLists;
    private FirebaseUser fUser;
    private DatabaseReference reference;
    private ProgressBar loadingBar;
    private FirebaseDatabase reference2;
    private RelativeLayout noChatsLayout, noInternetLayout;
    private DatabaseReference connectedReference;
    private DatabaseReference onlineStatusRef;
    private CheckNetwork checkNetwork;
    private boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        networkConnection = new NetworkConnectionLiveData(ChatHomeActivity.this);
        chatsLists = new ArrayList<>();
        mUsers = new ArrayList<>();

        checkNetwork = new CheckNetwork(this);
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        reference2 = FirebaseDatabase.getInstance();

        noInternetLayout = binding.noInternetLayout;
        loadingBar = binding.loadingBar;
        noChatsLayout = binding.noChatsLayout;
        recyclerView = binding.messagesRecyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new ChatHomeAdapter(ChatHomeActivity.this, mUsers);
        recyclerView.setAdapter(userAdapter);

//        if(isNetworkOnline()) {
//            loaded = true;
//            noInternetLayout.setVisibility(View.GONE);
//            loadingBar.setVisibility(View.VISIBLE);
//            showChats();
//        } else {
//            if (!loaded) {
//                noChatsLayout.setVisibility(View.GONE);
//                loadingBar.setVisibility(View.GONE);
//                noInternetLayout.setVisibility(View.VISIBLE);
//            }
//        }

        try  {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        networkConnection.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                if (isConnected) {
                    loaded = true;
                    noInternetLayout.setVisibility(View.GONE);
                    loadingBar.setVisibility(View.VISIBLE);
                    showChats();

                    binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menuLogout:
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(ChatHomeActivity.this, LoginActivity.class));
                                    finish();
                                    Animatoo.animateSlideRight(ChatHomeActivity.this);
                                    break;
                                case R.id.menuSettings:
                                    startActivity(new Intent(ChatHomeActivity.this, SettingsActivity.class));
                                    finish();
                                    Animatoo.animateSlideLeft(ChatHomeActivity.this);
                                    break;
                                case R.id.menuZeusMode:
                                    startActivity(new Intent(ChatHomeActivity.this, BluetoothMainActivity.class));
                                    finish();
                                    Animatoo.animateSlideLeft(ChatHomeActivity.this);
                                    break;
                            }
                            return false;
                        }
                    });
                } else {
                    binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menuLogout:
                                    Toast.makeText(ChatHomeActivity.this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.menuSettings:
                                    startActivity(new Intent(ChatHomeActivity.this, SettingsActivity.class));
                                    finish();
                                    Animatoo.animateSlideLeft(ChatHomeActivity.this);
                                    break;
                                case R.id.menuZeusMode:
                                    startActivity(new Intent(ChatHomeActivity.this, BluetoothMainActivity.class));
                                    finish();
                                    Animatoo.animateSlideLeft(ChatHomeActivity.this);
                                    break;
                            }
                            return false;
                        }
                    });

                    if (!loaded) {
                        noChatsLayout.setVisibility(View.GONE);
                        loadingBar.setVisibility(View.GONE);
                        noInternetLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        onUserDisconnect();

        binding.toolbar.inflateMenu(R.menu.home_menu);

        binding.chooseChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.chooseChatButton.startAnimation(AnimationUtils.loadAnimation(ChatHomeActivity.this, R.anim.click));

                startActivity(new Intent(ChatHomeActivity.this, SearchUserActivity.class));
                finish();
                Animatoo.animateSlideLeft(ChatHomeActivity.this);
            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    private void updateToken(String refreshToken) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Tokens");
        Token token = new Token(refreshToken);
        reference.child(fUser.getUid()).setValue(token);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean isNetworkOnline() {
        boolean isOnline = false;
        try {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
            isOnline = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOnline;
    }

    private void showChats() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
                        //Stop shimmer layout and show recycler view if user has chatted before at least once
                        reference = FirebaseDatabase.getInstance().getReference("SenderList").child(fUser.getUid());
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange( @NotNull DataSnapshot dataSnapshot) {
                                chatsLists.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    ChatsList chatsList = new ChatsList();
                                    chatsList.setId(snapshot.child("id").getValue(String.class));

                                    chatsLists.add(chatsList);
                                }

                                getSenderList();
                            }

                            @Override
                            public void onCancelled( @NotNull DatabaseError error) {

                            }
                        });

//                    }
//                });
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    //Load chats that user has made on home screen page
    private void getSenderList() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
                        reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange( @NotNull DataSnapshot dataSnapshot) {
                                mUsers.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    User user = snapshot.getValue(User.class);
                                    for (ChatsList chatsList : chatsLists) {
                                        assert user != null;

                                        if (user.getId().equals(chatsList.getId())) {
//                                            mUsers.add(0, user);
                                            mUsers.add(user);
                                        }
                                    }
                                }

                                try {
                                    if (userAdapter.getItemCount() == 0) {
                                        noChatsLayout.setVisibility(View.VISIBLE);
                                    } else {
                                        noChatsLayout.setVisibility(View.GONE);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                userAdapter.notifyDataSetChanged();
                                loadingBar.setVisibility(View.GONE);

//                                userAdapter.setOnItemClickListener(new ChatHomeAdapter.onItemClickListener() {
//                                    @Override
//                                    public void onItemClick(int position) {
//                                        User user = mUsers.get(position);
//                                        //Start chatting activity with the details of the person user is chatting with
//                                        startActivity(new Intent(ChatHomeActivity.this, ChattingActivity.class)
//                                                .putExtra("userId", user.getId())
//                                                .putExtra("profilePic", user.getImageURL())
//                                                .putExtra("tap", 0)
//                                                .putExtra("userName", user.getUsername()));
//                                        finish();
//                                        Animatoo.animateSlideLeft(ChatHomeActivity.this);
//                                    }
//                                });
                            }

                            @Override
                            public void onCancelled( @NotNull DatabaseError error) {

                            }
                        });

//                    }
//                });
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    private void onUserDisconnect() {

        onlineStatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).child("status");
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

    @Override
    public void onResume() {
        super.onResume();
        String currentID = FirebaseAuth.getInstance().getUid();
        reference2.getReference("Users").child(currentID).child("status").setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            String currentID = FirebaseAuth.getInstance().getUid();
            reference2.getReference("Users").child(fUser.getUid()).child("status").setValue(String.valueOf(System.currentTimeMillis()));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        networkConnection.removeObservers(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        finishAffinity();
        Animatoo.animateSlideRight(this);
    }

}