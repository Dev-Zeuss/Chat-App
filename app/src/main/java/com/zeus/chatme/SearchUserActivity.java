package com.zeus.chatme;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.zeus.adbucks.NetworkConnectionLiveData;
import com.zeus.chatme.adapters.UserAdapter;
import com.zeus.chatme.databinding.ActivitySearchUserBinding;
import com.zeus.chatme.models.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchUserActivity extends AppCompatActivity {

    private ActivitySearchUserBinding binding;
    private RecyclerView searchRecyclerView;
    private UserAdapter usersAdapter;
    private List<User> users;
    private ImageView backBtn;
    private FirebaseUser firebaseUser;
    private MaterialEditText search_input;
    private FirebaseDatabase reference2;
    private NetworkConnectionLiveData networkConnection;
    private boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        networkConnection = new NetworkConnectionLiveData(this);
        searchRecyclerView = binding.chatSearchRecyclerView;
        users = new ArrayList<>();
        reference2 = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        backBtn = binding.searchBackButton;
        search_input = binding.searchUsersInput;
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchRecyclerView.setHasFixedSize(true);
        usersAdapter = new UserAdapter(SearchUserActivity.this, users);
        searchRecyclerView.setAdapter(usersAdapter);

        networkConnection.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                if (isConnected) {
                    loaded = true;
                    binding.noInternetLayout.setVisibility(View.GONE);
                    binding.chatLayout.setVisibility(View.VISIBLE);
                    readUsers();
                } else {
                    if (!loaded) {
                        binding.chatLayout.setVisibility(View.GONE);
                        binding.noInternetLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchUserActivity.this, ChatHomeActivity.class));
                finish();
                Animatoo.animateSlideRight(SearchUserActivity.this);
            }
        });

        search_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void searchUsers(String s) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {

                        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                                .startAt(s)
                                .endAt(s+"\uf8ff");

                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange( @NotNull DataSnapshot dataSnapshot) {
                                users.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    User user = snapshot.getValue(User.class);

//                                    if (user == null) {
//                                        Toast.makeText(SearchUserActivity.this, "User not found", Toast.LENGTH_LONG).show();
//                                    }

//                                    if (!snapshot.exists()) {
//                                        Toast.makeText(SearchUserActivity.this, "User not found", Toast.LENGTH_LONG).show();
//                                    }

                                    assert user != null;
                                    assert fUser != null;

                                    if (!user.getId().equals(fUser.getUid())) {
                                        users.add(user);
                                    }

                                }
                                usersAdapter.notifyDataSetChanged();
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

    private void readUsers() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//              runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange( @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    users.clear();
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                        User user = snapshot1.getValue(User.class);
                                        assert user != null;
                                        assert firebaseUser != null;
                                        if (!user.getId().equals(firebaseUser.getUid())) {// so you won't chat with ur self

                                            //Hide shimmer layout and show recycler view
                                            users.add(user);
                                        }
                                    }

                                    usersAdapter.notifyDataSetChanged();
                                        }

                            }

                            @Override
                            public void onCancelled( @org.jetbrains.annotations.NotNull DatabaseError error) {

                            }
                        });
//                    }
//                });
//            }
//        }).start();
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
        String currentID = FirebaseAuth.getInstance().getUid();
        reference2.getReference("Users").child(currentID).child("status").setValue(String.valueOf(System.currentTimeMillis()));
    }

    @Override
    protected void onDestroy() {
        networkConnection.removeObservers(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(SearchUserActivity.this, ChatHomeActivity.class));
        finish();
        Animatoo.animateSlideRight(SearchUserActivity.this);
    }
}