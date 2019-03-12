package com.bangkumist.bintang.bijigram;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtEmailReg, edtPassReg, edtConPassReg;
    private Button btnCreate, btnLoginReg;
    private ProgressBar progressBar;

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        edtEmailReg = findViewById(R.id.reg_email);
        edtPassReg = findViewById(R.id.reg_password);
        edtConPassReg = findViewById(R.id.reg_confirm_password);
        btnCreate = findViewById(R.id.btn_reg);
        btnLoginReg  = findViewById(R.id.btn_reg_login);
        progressBar = findViewById(R.id.reg_pb);

        btnLoginReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmailReg.getText().toString();
                String password = edtPassReg.getText().toString();
                String conPass = edtConPassReg.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) & !TextUtils.isEmpty(conPass)){

                    if (password.equals(conPass)){

                        progressBar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    Intent setIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                                    startActivity(setIntent);
                                } else {
                                    String err = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error : " + err
                                            ,Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Confirm Password and Password doesnt match"
                        ,Toast.LENGTH_LONG).show();
                    }


                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
