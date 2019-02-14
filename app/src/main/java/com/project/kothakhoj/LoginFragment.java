package com.project.kothakhoj;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.project.Util.Util.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {


    private EditText mailET,passET;
    private View mView,progressBar;


    private RegisterFragment registerFragment;

    private FirebaseAuth firebaseAuth;

    String networkState;



    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_login, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        registerFragment = new RegisterFragment();

        syncWithXml();

        networkState = getNetworkState(getContext());


        if(getArguments()!=null && getArguments().getString("mail") != null){
            mailET.setText(getArguments().getString("mail"));
            passET.setText(getArguments().getString("pass"));
        }

        return mView;
    }

    private void syncWithXml() {
        mailET = mView.findViewById(R.id.mail_et_log);
        passET = mView.findViewById(R.id.pass_et_log);
        progressBar = mView.findViewById(R.id.progress_login);
        mView.findViewById(R.id.login_btn_log).setOnClickListener(this);
        mView.findViewById(R.id.register_option).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn_log:
                if(networkState.equals(NETWORK_OK)){
                    authenticateWithFirebase();
                }else{
                    toast(networkState);
                }
                break;
            case R.id.register_option:
                changeFragment(getActivity().getSupportFragmentManager(),parentFrameLayout,registerFragment,null);
                break;
        }

    }

    private void authenticateWithFirebase() {

        String mail = mailET.getText().toString();
        String pass = passET.getText().toString();


        if(isValid(mail,pass)){
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(mail,pass)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                registerUserDetails();
                            }else{
                                if(task.getException() instanceof FirebaseAuthInvalidUserException){
                                    toast("Invalid Login Credentials");
                                }else if(task.getException() instanceof FirebaseAuthInvalidUserException){
                                    toast("User not Registered");
                                }else{
                                    toast(task.getException().getMessage());
                                }
                            }
                        }


                    }).addOnFailureListener(getActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

    }

    private void registerUserDetails() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("Users").document(firebaseAuth.getCurrentUser().getUid());

        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                boolean isRenter = (boolean) doc.get(USER_TYPE_KEY);
                progressBar.setVisibility(View.GONE);

                getActivity().finish();
                Intent renterIntent = new Intent(getActivity().getApplicationContext(),HomeActivity.class);
                renterIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                renterIntent.putExtra("isRenter",isRenter);
                startActivity(renterIntent);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toast("Connection to Firebase Failed");
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private boolean isValid(String mail,String pass){

        if(mail.isEmpty()){
            mailET.setError("Email Required");
            mailET.requestFocus();
            return false;
        }


        if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            mailET.setError("Invalid Email");
            mailET.requestFocus();
            return false;
        }


        if(pass.isEmpty()){
            passET.setError("Email Required");
            passET.requestFocus();
            return false;
        }

        if(pass.length() < 6){
            passET.setError("Required more than 6 characters");
            passET.requestFocus();
            return false;
        }

        return true;
    }



    private void toast(String message){
        Toast.makeText(getActivity().getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
}
