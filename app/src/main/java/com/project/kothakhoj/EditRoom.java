package com.project.kothakhoj;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.project.models.Room;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import id.zelory.compressor.Compressor;

import static com.project.Util.Util.*;
public class EditRoom extends AppCompatActivity implements View.OnClickListener {


    private static final int IMAGE_CHOOSE_CODE = 1420;
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
    private Room room;
    private String documentId;
    private boolean newPic = false;
    String[] values = null;
    protected GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_room);
        getSupportActionBar().setTitle("Edit My Room");
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.colorActionBar));

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        room = (Room) getIntent().getExtras().get("Room");

        loadSpinners();
        findViewById(R.id.room_update).setOnClickListener(this);
        findViewById(R.id.updloadButtonEdit).setOnClickListener(this);
        roomPrice = findViewById(R.id.roomPriceEdit);
        roomAddress = findViewById(R.id.roomAddressEdit);
        roomDate = findViewById(R.id.roomDateEdit);
        roomCooking = findViewById(R.id.radio_cookingEdit);
        roomRent = findViewById(R.id.radio_negotiateEdit);
        progressBar = findViewById(R.id.progress_editRoom);

        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
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
                new DatePickerDialog(EditRoom.this,date,myCalendar
                        .get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar
                        .get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment2);

        autocompleteFragment.setHint("Address");


        roomAddress.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               roomAddress.setVisibility(View.GONE);
               findViewById(R.id.google_places).setVisibility(View.VISIBLE);
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
               return false;
           }
        });

                loadData();

    }

    private void loadData() {
        roomAddress.setText(room.getAddress());
        roomPrice.setText(String.valueOf(room.getPrice()));

        roomPreference.setSelection(getIndex(roomPreference,room.getPreference()),true);

        roomType.setSelection(getIndex(roomType,room.getRoomType()),true);


        roomFloor.setSelection(getIndex(roomFloor,room.getFloor()),true);

        roomContact.setSelection(getIndex(roomContact,room.getContact()),true);

        roomDate.setText(room.getDate());

        documentId  = room.getDocrefId();



        if(room.getCooking().equals("Allowed")){
            toast(room.getCooking());
            roomCooking.check(R.id.cooking_yesEdit);
        }else{
            roomCooking.check(R.id.cooking_noEdit);
        }


        if(room.getRent().equals("Negotiable")){
            roomRent.check(R.id.radio_negotiate_yesEdit);
        }else{
            roomRent.check(R.id.radio_negotiate_noEdit);
        }
    }


    private void toast(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    private void updateAvailableDate() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        roomDate.setText(sdf.format(myCalendar.getTime()));
        roomDate.requestFocus();
    }


    private void loadSpinners() {

        roomType =  findViewById(R.id.roomTypeEdit);
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(this, R.array.roomType, R.layout.custom_spinner_layout);
        adapterType.setDropDownViewResource(R.layout.custom_spinner_dropdown);

        roomType.setAdapter(
                new NothingSelectedSpinnerAdapter(
                        adapterType,
                        R.layout.roomtype_default_text,
                        this));



        roomFloor =  findViewById(R.id.roomFloorEdit);
        ArrayAdapter<CharSequence> adapterFloor = ArrayAdapter.createFromResource(this, R.array.floors, R.layout.custom_spinner_layout);
        adapterFloor.setDropDownViewResource(R.layout.custom_spinner_dropdown);

        roomFloor.setAdapter(
                new NothingSelectedSpinnerAdapter(
                        adapterFloor,
                        R.layout.floor_default_text,
                        this));


        roomContact =  findViewById(R.id.roomContactEdit);
        ArrayAdapter<CharSequence> adapterContact = ArrayAdapter.createFromResource(this, R.array.contactTiming, R.layout.custom_spinner_layout);
        adapterContact.setDropDownViewResource(R.layout.custom_spinner_dropdown);

        roomContact.setAdapter(
                new NothingSelectedSpinnerAdapter(
                        adapterContact,
                        R.layout.timings_default_text,
                        this));


        roomPreference =  findViewById(R.id.roomPreferenceEdit);
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
            case R.id.room_update:
                updateRoomDetails(documentId);
                break;
            case R.id.updloadButtonEdit:
                selectImage();
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
            textView.setText("photo updated with new onw");
            try {
                compressedFile = new Compressor(this)
                        .compressToFile(new File(getPathFromURI(this,imgUri)));
                uploadImageUri = Uri.fromFile(compressedFile);
                newPic = true;
            } catch (IOException e) {
                e.printStackTrace();
                toast(e.getMessage(),1);
            }
        }
    }

    private void updateRoomDetails(final String refId) {
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

            roomDetails.put("ReferenceId", refId);

            collectionReference.document(refId).set(roomDetails)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                if(newPic) {
                                    uploadImage(refId);
                                }else{
                                    toast("Successfully Updated");
                                    finish();
                                }
                            }else if(task.isCanceled()){
                                progressBar.setVisibility(View.GONE);
                                toast("Updating room details cancelled",0);
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
                            toast("Successfully Updated",0);
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

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return false;
    }


    private int getIndex(Spinner spinner, String myString){

        int items = spinner.getCount() ;

        //        here i = 1 as I have added a custom textview to show the default hint message to the user
        for (int i=1;i<items;i++){
            System.out.println(String.valueOf(i)+"="+myString);
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }

        return 0;
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
}
