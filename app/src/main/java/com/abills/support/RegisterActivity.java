package com.abills.support;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

  MaterialEditText username, email, password;
  Button btn_register;

  FirebaseAuth auth;
  DatabaseReference reference;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    Objects.requireNonNull(getSupportActionBar()).setTitle("Реєстрація");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    username = findViewById(R.id.username);
    email = findViewById(R.id.email);
    password = findViewById(R.id.password);
    btn_register = findViewById(R.id.btn_register);

    auth = FirebaseAuth.getInstance();

    btn_register.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String txt_username = username.getText().toString();
        String txt_email = email.getText().toString();
        String txt_password = password.getText().toString();

        if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
          Toast.makeText(RegisterActivity.this, "Всі поля повинні бути заповнені", Toast.LENGTH_SHORT).show();
        } else if (txt_password.length() < 6) {
          Toast.makeText(RegisterActivity.this, "пароль має містити не менше 6 символів", Toast.LENGTH_SHORT).show();
        } else {
          register(txt_username, txt_email, txt_password);
        }
      }
    });
  }

  private void register(final String username, String email, String password) {

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
              FirebaseUser firebaseUser = auth.getCurrentUser();
              assert firebaseUser != null;
              String userid = firebaseUser.getUid();

              reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

              HashMap<String, String> hashMap = new HashMap<>();
              hashMap.put("id", userid);
              hashMap.put("username", username);
              hashMap.put("imageURL", "default");
              hashMap.put("status", "offline");
              hashMap.put("search", username.toLowerCase());

              reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                  if (task.isSuccessful()) {
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    verifyEmail();
                    Toast.makeText(RegisterActivity.this, "Лист відправлено на пошту, прошу поверніться до логіну", Toast.LENGTH_SHORT).show();
                    finish();
                  }
                }
              });
            } else {
              Toast.makeText(RegisterActivity.this, "Ви не можете зареєструватися за допомогою цієї електронної пошти", Toast.LENGTH_SHORT).show();
            }
          }
        });
  }


  public void verifyEmail() {

    FirebaseUser user = auth.getCurrentUser();
    assert user != null;
    user.sendEmailVerification()
        .addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()) {
              Toast.makeText(RegisterActivity.this, "Підтвердження пошти відправлено вам на пошту", Toast.LENGTH_SHORT).show();
            }
          }
        });
  }
}
