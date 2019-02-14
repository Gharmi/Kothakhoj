package com.project.kothakhoj;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.project.Util.Util.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener {


    private View fragmentView,progressBar;
    private EditText nameET,mailET,passET,contactNoET;
    private RadioGroup radioGroup;

    private RadioButton rent,seek;

    private LoginFragment loginFragment;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;



    private boolean isRenter;
    private String name,mail,pass,contact,networkState;


    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_register, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        loginFragment = new LoginFragment();
        syncWithXml();

        networkState = getNetworkState(getContext());
        if(!networkState.equals(NETWORK_OK)){
            toast(networkState);
        }

        return fragmentView;
    }

    private void syncWithXml() {
        nameET = fragmentView.findViewById(R.id.name_et_reg);
        mailET = fragmentView.findViewById(R.id.email_et_reg);
        passET = fragmentView.findViewById(R.id.pass_et_reg);
        progressBar = fragmentView.findViewById(R.id.progress_register);
        contactNoET = fragmentView.findViewById(R.id.contact_et_reg);
        radioGroup = fragmentView.findViewById(R.id.radio_profile_filter);
        rent = fragmentView.findViewById(R.id.radio_renter);
        seek = fragmentView.findViewById(R.id.radio_seeker);
        fragmentView.findViewById(R.id.register_btn_reg).setOnClickListener(this);
        fragmentView.findViewById(R.id.back_arrow_register).setOnClickListener(this);
        fragmentView.findViewById(R.id.register_option).setOnClickListener(this);
        rent.setOnClickListener(this);
        seek.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register_btn_reg:
                if(networkState.equals(NETWORK_OK)){
                    registerUsingFirebase();
                }else{
                    toast(networkState);
                }
                break;
            case R.id.back_arrow_register:
                changeFragment(getActivity().getSupportFragmentManager(),parentFrameLayout,loginFragment,null);
                break;
            case R.id.register_option:
                changeFragment(getActivity().getSupportFragmentManager(),parentFrameLayout,loginFragment,null);
                break;
            case R.id.radio_seeker:
                rent.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                seek.setCompoundDrawablesWithIntrinsicBounds(0,0,0,R.drawable.ic_radio_checked);
                break;
            case R.id.radio_renter:
                seek.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                rent.setCompoundDrawablesWithIntrinsicBounds(0,0,0,R.drawable.ic_radio_checked);
                break;
        }

    }

    private void registerUsingFirebase() {
        name = nameET.getText().toString();
        mail = mailET.getText().toString();
        pass = passET.getText().toString();
        contact = contactNoET.getText().toString();

        if(isValid(name,mail,pass,contact)){
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(mail,pass)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                storeUserDetails(FirebaseAuth.getInstance().getCurrentUser());
                            }else{
                                if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                    toast("User Already Registered");
                                }else{
                                    toast(task.getException().getMessage());
                                }
                            }
                        }
                    });
        }
    }

    private void storeUserDetails(final FirebaseUser user) {
        int selectedId = radioGroup.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        RadioButton radioButton = fragmentView.findViewById(selectedId);

        String userType = radioButton.getText().toString();

        if(userType.equals(RENTER_KEY)){
            isRenter = true;
        }else if(userType.equals(SEEKER_KEY)){
            isRenter = false;
        }



        Map< String, Object > userDetails = new HashMap< >();

        userDetails.put(USER_NAME_KEY, name);

        userDetails.put(USER_EMAIL_KEY, mail);

        userDetails.put(USER_CONTACT_KEY, contact);

        userDetails.put(USER_TYPE_KEY,isRenter);

        Log.d("USERDETAILSDEBUGGER",userDetails.toString());


        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(userDetails)

                .addOnSuccessListener(new OnSuccessListener< Void >() {

                    @Override

                    public void onSuccess(Void aVoid) {
                        toast("User Successfully Registered");
                        Bundle args = new Bundle();
                        args.putString("mail",mail);
                        args.putString("pass",pass);
                        loginFragment.setArguments(args);
                        firebaseAuth.signOut();
                        progressBar.setVisibility(View.GONE);
                        changeFragment(getActivity().getSupportFragmentManager(),parentFrameLayout,loginFragment,null);
                    }

                })
                .addOnFailureListener(new OnFailureListener() {

                    @Override

                    public void onFailure(@NonNull Exception e) {
                        toast("Some Error Occured");
                        progressBar.setVisibility(View.GONE);
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseAuth.getInstance().signOut();
                                            Log.d("FAILUREDATABASE", "User account deleted.");
                                        }else{
                                            Log.d("FAILUREDATABASE", task.getException().getMessage());
                                        }
                                    }
                                });

                    }

                });
    }

    private void toast(String message){
        Toast.makeText(getActivity().getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private boolean isValid(String name,String mail,String pass,String contact){


        if(name.isEmpty()){
            nameET.setError("Full Name Required");
            nameET.requestFocus();
            return false;
        }

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


        if(contact.isEmpty()){
            contactNoET.setError("Contact is Required");
            contactNoET.requestFocus();
            return false;
        }


        if(contact.length() != 10){
            contactNoET.setError("Please Enter Valid Mobile Number");
            contactNoET.requestFocus();
            return  false;
        }



//        if (radioGroup.getCheckedRadioButtonId() == -1) {
//            // no radio buttons are checked
//            toast("Please select usage type");
//            radioGroup.requestFocus();
//            return false;
//        }

        return true;
    }
}
