package com.project.kothakhoj;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

import static com.project.Util.Util.*;

public class HomeActivity extends AppCompatActivity
        implements View.OnClickListener {


    private static final int IMAGE_CHOOSE_CODE = 1121;
    private FirebaseAuth firebaseAuth;
    private TextView rooms,changeUserPic;
    private ImageView userPic;
    private boolean isRenter;
    private long backPressedTime = 0;    // used by onBackPressed()
    private File compressedFile;
    private ProgressBar progressBar;

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        firebaseAuth = FirebaseAuth.getInstance();
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);


        loadNavigationComponents();

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if(getSupportFragmentManager().getBackStackEntryCount()>1 ) {
                        toggle.setDrawerIndicatorEnabled(false);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onBackPressed();
                            }
                        });
                }else{
                    toggle.setDrawerIndicatorEnabled(true);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    toggle.syncState();
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            drawer.openDrawer(GravityCompat.START);
                        }
                    });
                }
            }
        });

//        check network state and if no network show error in toast
        String networkState = getNetworkState(this);
        if(networkState.equals(NETWORK_OK)){
            setHomeLayout();
            setUsersValues();
        }else{
            toast(networkState);
        }
    }

    private void setUsersValues() {

        FirebaseFirestore.getInstance().collection("Users")
                .document(firebaseAuth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot d = task.getResult();

                    TextView name = findViewById(R.id.nav_user);
                    name.setText(d.getString("Full Name"));

                    TextView address = findViewById(R.id.nav_type);
                    isRenter = d.getBoolean(USER_TYPE_KEY);
                    if(isRenter){
                        address.setText("Renter");
                    }else {
                        address.setText("Seeker");
                    }
                }else{
                    toast("Failed to get user Data");
                }
            }
        });


        FirebaseStorage.getInstance().getReference("/Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("profile").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Picasso.get().load(task.getResult().toString()).fit().into(userPic);
                }else{
                    Log.d("HomeActivity",task.getException().getMessage());
                }
            }
        });
    }

    private void setHomeLayout() {
        boolean isRenter = getIntent().getExtras().getBoolean("isRenter");
        if(isRenter){
            rooms.setText("Other Rooms");
            changeFragment(getSupportFragmentManager(),homeFrameLayout,new RenterFragment(),"Renter");
        }else{
            rooms.setText("Saved Rooms");

            changeFragment(getSupportFragmentManager(),homeFrameLayout,new SeekerFragment(),"Seeker");
        }
    }

    private void loadNavigationComponents() {
        findViewById(R.id.nav_profile).setOnClickListener(this);
        findViewById(R.id.nav_notifications).setOnClickListener(this);
        findViewById(R.id.nav_settings).setOnClickListener(this);
        findViewById(R.id.nav_help).setOnClickListener(this);
        findViewById(R.id.sign_out).setOnClickListener(this);
        rooms = findViewById(R.id.nav_rooms);
        changeUserPic = findViewById(R.id.update_user_pic);
        userPic = findViewById(R.id.nav_pic);
        rooms.setOnClickListener(this);
        progressBar = findViewById(R.id.user_img_prg);

        changeUserPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPic.setVisibility(View.GONE);
                changeUserPic.setVisibility(View.VISIBLE);

            }
        });
    }

    private void updateProfile() {
        Intent changePic = new Intent();
        changePic.setType("image/*");
        changePic.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(changePic,"Select Profile Picture"),IMAGE_CHOOSE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == IMAGE_CHOOSE_CODE && data !=null && data.getData() != null){
            Uri imgUri = data.getData();
            changeUserPic.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            userPic.setImageURI(imgUri);
            try {
                compressedFile = new Compressor(this)
                        .compressToFile(new File(getPathFromURI(this,imgUri)));
                Uri uploadImageUri = Uri.fromFile(compressedFile);
                uploadImage(uploadImageUri);
            } catch (IOException e) {
                progressBar.setVisibility(View.GONE);
                userPic.setVisibility(View.VISIBLE);
                e.printStackTrace();
                toast(e.getMessage());
            }
        }
    }

    private void uploadImage(final Uri uploadImageUri) {
        FirebaseStorage.getInstance()
                .getReference("/Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("profile")
                .putFile(uploadImageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            toast("Successfully Updated");
                        }else{
                            toast(task.getException().getMessage());
                        }
                        progressBar.setVisibility(View.GONE);
                        userPic.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        int count = getSupportFragmentManager().getBackStackEntryCount();
        long t = System.currentTimeMillis();




        if(!userPic.isShown()){
            userPic.setVisibility(View.VISIBLE);
            changeUserPic.setVisibility(View.GONE);
        }

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else if (count == 1) {
            Fragment fragment =  getSupportFragmentManager().getFragments().get(0);
            if (fragment instanceof SeekerFragment  && fragment != null ) {
                View filterOptions = fragment.getView().findViewById(R.id.filter_options);
                if(filterOptions.isShown()){
                    filterOptions.setVisibility(View.GONE);
                }else if(t - backPressedTime > 2000) {    // 2 secs
                    backPressedTime = t;
                    toast("Press back again to exit");
                } else{
                    this.finishAffinity();
                }
            }

        } else {
            getSupportFragmentManager().popBackStack();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            toast(String.valueOf(item.getTitle()));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nav_profile:
                toast("Profile Update Clicked");
                break;
            case R.id.nav_notifications:
                toast("Notifications Under Development");
                break;
            case R.id.nav_settings:
                toast("Settings to be Developed Soon");
                break;
//            case R.id.nav_tutorial:
//                toast("Tutorial are coming");
//                break;
            case R.id.nav_help:
                toast("Just call @ 9860661101 if any help needed");
                break;
            case R.id.nav_rooms:
                if(isRenter) {
                    changeFragment(getSupportFragmentManager(), homeFrameLayout, new ViewOtherRooms(),"ViewRooms");
                }else{
                    changeFragment(getSupportFragmentManager(),homeFrameLayout,new ViewSavedRooms(),"ViewSavedRooms");
                }
                break;
            case R.id.sign_out:
                firebaseAuth.signOut();
                finish();
                overridePendingTransition(0,0);
                toast("Successfully Signed Out");
                startActivity(new Intent(this,FirstActivity.class));
                break;
            }
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }


}