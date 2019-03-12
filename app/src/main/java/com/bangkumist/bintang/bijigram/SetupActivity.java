package com.bangkumist.bintang.bijigram;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setImg;
    private Uri mainImage= null;
    private EditText setupName, setupAddres, setupPhone;
    private Button btnSetup;
    private ProgressBar setupProgress;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private String user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getSupportActionBar().setTitle("Account Setting");

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();

        setImg = findViewById(R.id.setup_photo);
        setupName = findViewById(R.id.setup_name);
        setupAddres = findViewById(R.id.setup_addres);
        setupPhone = findViewById(R.id.setup_phone);
        btnSetup = findViewById(R.id.setup_button);
        setupProgress = findViewById(R.id.setup_progress);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){

                    if (task.getResult().exists()){
                        String name = task.getResult().getString("name");
                        String addres = task.getResult().getString("addres");
                        String phone = task.getResult().getString("phone");
                        String image = task.getResult().getString("image");

                        setupName.setText(name);
                        setupAddres.setText(addres);
                        setupPhone.setText(phone);

                        RequestOptions placeHolder = new RequestOptions();
                        placeHolder.placeholder(R.drawable.defaultimage);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeHolder).load(image).into(setImg);
                    }
                }else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore Error" + error,Toast.LENGTH_LONG).show();
                }
            }
        });

        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user_name = setupName.getText().toString();
                final String user_address = setupAddres.getText().toString();
                final String setup_phone = setupPhone.getText().toString();
                if (!TextUtils.isEmpty(user_name) && !TextUtils.isEmpty(user_address) && !TextUtils.isEmpty(setup_phone)
                        && mainImage != null){
                    user_id = firebaseAuth.getCurrentUser().getUid();
                    setupProgress.setVisibility(View.VISIBLE);

                    StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");
                    image_path.putFile(mainImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()){
                                Task<Uri> download_uri = storageReference.getDownloadUrl();

                                Map<String, String> userMap = new HashMap<>();
                                userMap.put("name", user_name);
                                userMap.put("addres", user_address);
                                userMap.put("phone", setup_phone);
                                userMap.put("image", download_uri.toString());

                                firebaseFirestore.collection("Users").document(user_id).set(userMap).
                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            Toast.makeText(SetupActivity.this, "Succes!" ,Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }else {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "Firestore Error" +
                                                    error,Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                                Toast.makeText(SetupActivity.this, "Succes",Toast.LENGTH_LONG).show();
                            }else {
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "Image Error" + error,Toast.LENGTH_LONG).show();
                            }

                            setupProgress.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

        setImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(SetupActivity.this ,Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }else {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(SetupActivity.this);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImage = result.getUri();

                setImg.setImageURI(mainImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
