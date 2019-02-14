package com.project.kothakhoj;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.zelory.compressor.Compressor;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;


import static com.project.Util.Util.*;

public class AddRoom extends AppCompatActivity implements View.OnClickListener,EasyPermissions.PermissionCallbacks{


    private static final int IMAGE_CHOOSE_CODE = 142;
    private Spinner roomType,roomFloor,roomPreference,roomContact;
    private EditText roomPrice,roomAddress;
    private TextView roomDate;
    private RadioGroup roomCooking,roomRent;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    final Calendar myCalendar = Calendar.getInstance();
    private Uri uploadImageUri = null;
    private File compressedFile;
    private View progressBar;
    protected GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.colorActionBar));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Add Room </font>"));

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);



        loadSpinners();
        findViewById(R.id.room_upload).setOnClickListener(this);
        findViewById(R.id.updloadButton).setOnClickListener(this);
        roomPrice = findViewById(R.id.roomPrice);
        roomAddress = findViewById(R.id.roomAddress);
        roomDate = findViewById(R.id.roomDate);
        roomCooking = findViewById(R.id.radio_cooking);
        roomRent = findViewById(R.id.radio_negotiate);
        progressBar = findViewById(R.id.progress_addRoom);


        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setHint("Address");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                roomAddress.setText(place.getName());
            }

            @Override
            public void onError(Status status) {
                toast(status.getStatusMessage(),0);
            }
        });

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateAvailableDate();
            }

        };

        roomDate.setKeyListener(null);

        roomDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddRoom.this,date,myCalendar
                        .get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar
                        .get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    private void updateAvailableDate() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        roomDate.setText(sdf.format(myCalendar.getTime()));
        roomDate.requestFocus();
    }


    private void loadSpinners() {
        roomType =  findViewById(R.id.roomType);
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(this, R.array.roomType, R.layout.custom_spinner_layout);
        adapterType.setDropDownViewResource(R.layout.custom_spinner_dropdown);

        roomType.setAdapter(
                new NothingSelectedSpinnerAdapter(
                        adapterType,
                        R.layout.roomtype_default_text,
                        this));



        roomFloor =  findViewById(R.id.roomFloor);
        ArrayAdapter<CharSequence> adapterFloor = ArrayAdapter.createFromResource(this, R.array.floors, R.layout.custom_spinner_layout);
        adapterFloor.setDropDownViewResource(R.layout.custom_spinner_dropdown);

        roomFloor.setAdapter(
                new NothingSelectedSpinnerAdapter(
                        adapterFloor,
                        R.layout.floor_default_text,
                        this));


        roomContact =  findViewById(R.id.roomContact);
        ArrayAdapter<CharSequence> adapterContact = ArrayAdapter.createFromResource(this, R.array.contactTiming, R.layout.custom_spinner_layout);
        adapterContact.setDropDownViewResource(R.layout.custom_spinner_dropdown);

        roomContact.setAdapter(
                new NothingSelectedSpinnerAdapter(
                        adapterContact,
                        R.layout.timings_default_text,
                        this));


        roomPreference =  findViewById(R.id.roomPreference);
        ArrayAdapter<CharSequence> adapterPreferences = ArrayAdapter.createFromResource(this, R.array.preferences, R.layout.custom_spinner_layout);
        adapterPreferences.setDropDownViewResource(R.layout.custom_spinner_dropdown);

        roomPreference.setAdapter(
                new NothingSelectedSpinnerAdapter(
                        adapterPreferences,
                        R.layout.preference_default_text,
                        this));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.room_upload:
                storeRoomDetails();
                break;
            case R.id.updloadButton:
                //start intent to select picture from
                String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
                if (EasyPermissions.hasPermissions(this, perms)) {
                    selectImage();
                    // Already have permission, do the thing
                    // ...
                } else {
                    // Do not have permissions, request them now
                    EasyPermissions.requestPermissions(this, "REQUEST_READ_EXTERNAL_STORAGE",
                            1, perms);
                }

                break;
        }

    }


    private void selectImage(){
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
            TextView textView = findViewById(R.id.photo_status);
            textView.setText("1 photo selected");
            try {
                compressedFile = new Compressor(this)
                        .compressToFile(new File(getPathFromURI(this,imgUri)));
                uploadImageUri = Uri.fromFile(compressedFile);
            } catch (IOException e) {
                e.printStackTrace();
                toast(e.getMessage(),1);
            }
        }
    }

    private void storeRoomDetails() {
        String price = roomPrice.getText().toString().trim();
        String address = roomAddress.getText().toString().trim();
        String date = roomDate.getText().toString().trim();
        if(isValid(price,address,date)) {
            progressBar.setVisibility(View.VISIBLE);
            Map<String, Object> roomDetails = new HashMap<>();

            roomDetails.put(ROOM_PRICE, Integer.parseInt(price));

            roomDetails.put(ROOM_ADDRESS, address);

            roomDetails.put(ROOM_DATE, date);

            roomDetails.put(ROOM_FLOOR, roomFloor.getSelectedItem());

            roomDetails.put(ROOM_PREFERENCE, roomPreference.getSelectedItem());

            roomDetails.put(ROOM_TYPE, roomType.getSelectedItem());

            roomDetails.put(ROOM_CONTACT, roomContact.getSelectedItem());

            roomDetails.put(ROOM_OWNER,firebaseAuth.getCurrentUser().getUid());

            int selectedCooking  = roomCooking.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedCooking);

            roomDetails.put(ROOM_COOKING, radioButton.getText().toString());


            int selectRent  = roomRent.getCheckedRadioButtonId();
            RadioButton radioButton2 = findViewById(selectRent);

            roomDetails.put(ROOM_RENT, radioButton2.getText().toString());


            CollectionReference collectionReference = firebaseFirestore.collection("Rooms");

            final String id = collectionReference.document().getId();
            roomDetails.put("ReferenceId", id);

            collectionReference.document(id).set(roomDetails)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            uploadImage(id);
                        }else if(task.isCanceled()){
                            progressBar.setVisibility(View.GONE);
                            toast("Uploading room details cancelled",0);
                        }else {
                            progressBar.setVisibility(View.GONE);
                            toast("ERROR: "+task.getException().getMessage(),0);
                        }
                    }
                });
        }
    }

    private void uploadImage(final String documentId) {
        FirebaseStorage.getInstance()
                .getReference("/Rooms/"+documentId)
                .child("pics")
                .putFile(uploadImageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            toast("Successfully Uploaded",0);
                            progressBar.setVisibility(View.GONE);
                            finish();
                        }else{
                            toast(task.getException().getMessage(),0);
                        }
                    }
                });
    }


    private void toast(String message,int length){
        Toast.makeText(this,message,length).show();
    }


    private boolean isValid(String price,String location,String date){
        if(price.isEmpty()){
            roomPrice.setError("Required");
            roomPrice.requestFocus();
            return false;
        }

        if(location.isEmpty()){
            roomDate.setError("Required");
            roomDate.requestFocus();
            return false;
        }

        if(roomType.getSelectedItem() == null){
            ((TextView)roomType.getSelectedView()).setError("Required");
            toast("Room Type is Required",1);
            return false;
        }


        if(roomPreference.getSelectedItem() == null){
            ((TextView)roomPreference.getSelectedView()).setError("Required");
            toast("Preferred Tenants is Required",1);
            return false;
        }

        if(date.isEmpty()){
            roomDate.setError("Required");
            roomDate.requestFocus();
            return false;
        }


        if(roomContact.getSelectedItem() == null){
            ((TextView)roomContact.getSelectedView()).setError("Required");
            toast("Contact Timing is Required",1);
            return false;
        }


        if(roomFloor.getSelectedItem() == null){
            ((TextView)roomFloor.getSelectedView()).setError("Required");
            toast("Floor value is required",1);
            return false;
        }

        if(uploadImageUri == null){
            toast("Please Select an Image",0);
            return false;
        }

        return true;
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        selectImage();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
        toast("External storage access not granted",0);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this,HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("isRenter",true);
        finish();
        startActivity(i);
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
