package com.project.kothakhoj;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import static com.project.Util.Util.*;

public class FirstActivity extends AppCompatActivity implements View.OnClickListener {


    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;
    private FirebaseAuth mAuth;
    private Fragment currentFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_first);

//        initializing firebase auth


//        initializing the fragments
        loginFragment = new LoginFragment();
        registerFragment = new RegisterFragment();


//        registering button click listeners
        findViewById(R.id.login_btn).setOnClickListener(this);
        findViewById(R.id.register_btn).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_btn:
                setCurrentFragment(loginFragment);
                changeFragment(getSupportFragmentManager(),parentFrameLayout,loginFragment,null);
                break;
            case R.id.register_btn:
                setCurrentFragment(registerFragment);
                changeFragment(getSupportFragmentManager(),parentFrameLayout,registerFragment,null);

        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        if(currentFragment!=null){
            changeFragment(getSupportFragmentManager(),parentFrameLayout,currentFragment,null);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(currentFragment!=null){
            changeFragment(getSupportFragmentManager(),parentFrameLayout,currentFragment,null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setCurrentFragment(Fragment fragment){
        this.currentFragment = fragment;
    }

}
