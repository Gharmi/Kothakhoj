package com.project.kothakhoj;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.project.Util.Util.*;

public class Splash extends Activity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        if (hasInternetConnection(this)) {
        launch();

    }

    public void launch(){
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            final DocumentReference ref = db.collection("Users").document(mAuth.getCurrentUser().getUid());

            ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot doc = task.getResult();
                    try {
                        boolean isRenter = (boolean) doc.get(USER_TYPE_KEY);
                        Intent renterIntent = new Intent(Splash.this, HomeActivity.class);
                        renterIntent.putExtra("isRenter", isRenter);
                        renterIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(renterIntent);
                    }catch (NullPointerException e){
                        FirebaseAuth.getInstance().signOut();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
        else{
            new Thread(){
                @Override
                public void run(){
                    try {
                        synchronized(this){
                            wait(3000);
                        }
                    }
                    catch(InterruptedException ex){
                    }

                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(new Intent(Splash.this, FirstActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                }
            }.start();

        }
    }
}
