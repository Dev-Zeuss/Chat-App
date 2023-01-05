package com.zeus.chatme;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.vanniktech.emoji.EmojiEditText;
import com.zeus.adbucks.NetworkConnectionLiveData;
import com.zeus.chatme.chatSection.ChatDetailActivity;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {

    private String myUri = "";
    private StorageTask uploadTask;
    private FirebaseStorage storageReference;
    private NetworkConnectionLiveData networkConnection;

    private String chosenImage;
    private String chosenType;
    private ImageView closeBtn, image_added, sendFileBtn;
    private VideoView videoView;
    private ProgressDialog progressDialog;
    private DatabaseReference reference;
    private FirebaseUser fUser;
    private int IMAGE_INSERT_REQUEST = 766;
    private Uri selectedUri;
    private String type;
    private RelativeLayout constraintLayout2;
    private RelativeLayout constraintLayout;
    private CircleImageView playIv;
    private EmojiEditText captionET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        playIv = findViewById(R.id.playIV);
        captionET = findViewById(R.id.enterMessageET);
        sendFileBtn = findViewById(R.id.sendMessageBtn);
        constraintLayout = findViewById(R.id.conL);
        constraintLayout2 = findViewById(R.id.cl);
        videoView = findViewById(R.id.video_added);
        closeBtn = findViewById(R.id.postCloseBtn);
        image_added = findViewById(R.id.image_added);
        progressDialog = new ProgressDialog(this, R.style.MyDialogStyle3);

        try {
            chosenImage = getIntent().getStringExtra("_CROP_");
            chosenType = getIntent().getStringExtra("TYPE__");
            if (loadUri() != null) {
                chosenImage = loadUri();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred.", Toast.LENGTH_SHORT).show();
        }

        if (chosenType.equals("image")) {
            type = "iv";
            constraintLayout2.setVisibility(View.VISIBLE);
            constraintLayout.setVisibility(View.GONE);
            try {
                image_added.setImageURI(Uri.parse(chosenImage));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            type = "vv";
            constraintLayout2.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);

            videoView.setVideoURI(Uri.parse(chosenImage));
            videoView.seekTo(1);
            playIv.setVisibility(View.GONE);
            videoView.start();
        }

        networkConnection = new NetworkConnectionLiveData(this);
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Posts");
        storageReference = FirebaseStorage.getInstance();

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this, ChatDetailActivity.class));
                finish();
                Animatoo.animateSlideRight(PostActivity.this);
            }
        });

        networkConnection.observe(PostActivity.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                if (isConnected) {
                    sendFileBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendFileBtn.startAnimation(AnimationUtils.loadAnimation(PostActivity.this, R.anim.click));

                            if (videoView.isPlaying() && videoView.canPause()) {
                                videoView.pause();
                                playIv.setVisibility(View.VISIBLE);
                            }

                            int dataSize = 0;
                            selectedUri = Uri.parse(chosenImage);

                            //Check size of file before uploading it
                            String scheme = selectedUri.getScheme();
                            if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                                try {
                                    InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(selectedUri);
                                    dataSize = inputStream.available();
//                                    Toast.makeText(PostActivity.this, "Scheme Content , Size of file == " + dataSize + " bytes", Toast.LENGTH_LONG).show();
                                }catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (scheme.equals(ContentResolver.SCHEME_FILE)){
                                String path = selectedUri.getPath();
                                try {
                                    File file = new File(path);
                                    dataSize = (int) file.length();
//                                    Toast.makeText(PostActivity.this, "Scheme File , Size of file == " + dataSize + " bytes", Toast.LENGTH_LONG).show();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            //Get length of file in bytes
                            long fileSizeInBytes = (long) dataSize;
                            //convert the bytes to kilobytes(1 KB = 1024 bytes)
                            long fileSizeInKB = fileSizeInBytes / 1024;
                            //convert the kilobytes to megabytes(1 MB = 1024 kilobytes)
                            long fileSizeInMB = fileSizeInKB / 1024;

                            //Media file is more than 25mb
                            if (fileSizeInMB > 25) {
                                Toast.makeText(PostActivity.this, "Cannot post file more than 25mb.", Toast.LENGTH_SHORT).show();
                            } else {
                                uploadImage(captionET);
                            }
                        }
                    });
                } else {
                    sendFileBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendFileBtn.startAnimation(AnimationUtils.loadAnimation(PostActivity.this, R.anim.click));
                            Toast.makeText(PostActivity.this, "No internet connection.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying() && videoView.canPause()) {
                    videoView.pause();
                    playIv.setVisibility(View.VISIBLE);
                } else {
                    videoView.start();
                    playIv.setVisibility(View.GONE);
                }
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.seekTo(1); //Let it show the first frame of the video
                playIv.setVisibility(View.VISIBLE);
            }
        });

    }

    private String loadType() {
        SharedPreferences sharedPref = getSharedPreferences("SAVE_TYPE", Context.MODE_PRIVATE);
        return sharedPref.getString("SAVETYPE",null);
    }

    private String loadUri() {
        SharedPreferences sharedPref = getSharedPreferences("CHOSEN_URI", Context.MODE_PRIVATE);
        return sharedPref.getString("_URI_", null);
    }

    @Override
    protected void onStop() {
        if (type.equals("vv")) {
            videoView.stopPlayback();
            playIv.setVisibility(View.VISIBLE);
            videoView.seekTo(1);
        }
        super.onStop();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(EmojiEditText editText) {
            progressDialog.setMessage("Sending");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            if (selectedUri != null) {
                StorageReference fileReference = storageReference.getReference("Posts").child("chats")
                        .child(fUser.getUid()).child(System.currentTimeMillis() + "." + getFileExtension(selectedUri));

                uploadTask = fileReference.putFile(selectedUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw  task.getException();
                        }
                        return fileReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete( @NotNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    myUri = downloadUri.toString();
                                    Toast.makeText(PostActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();

                                    startActivity(new Intent(PostActivity.this, ChatDetailActivity.class)
                                            .putExtra("tap", 1)
                                            .putExtra("type", chosenType)
                                            .putExtra("message", editText.getText().toString().trim())
                                            .putExtra("uri", myUri));
                                    finish();
                                    Animatoo.animateSlideRight(PostActivity.this);

                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NotNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(PostActivity.this, e.getMessage() + "", Toast.LENGTH_SHORT).show();
                            }
                        });

                networkConnection.observe(PostActivity.this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean isConnected) {
                        if (!isConnected) {
                            progressDialog.dismiss();
                            Toast.makeText(PostActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                progressDialog.dismiss();
                Toast.makeText(PostActivity.this, "No File Selected", Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    public void onBackPressed() {
        networkConnection.removeObservers(this);
            startActivity(new Intent(this, ChatDetailActivity.class));
            finish();
            Animatoo.animateSlideRight(this);
    }

}