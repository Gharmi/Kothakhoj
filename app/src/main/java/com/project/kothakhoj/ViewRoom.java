package com.project.kothakhoj;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.models.Room;
import com.squareup.picasso.Picasso;

import static com.project.Util.Util.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewRoom extends Fragment implements View.OnClickListener {


    private View mView;
    private ViewGroup mainLayout,contactLayout;
    private Button contactBtn;
    private TextView location,price,type,floor,preference,cooking,rent,date,name,timing,back;
    private ImageView roompic,owner;
    private String ownerContact,ownerName;
    private Room room;
    public ViewRoom() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_view_room, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        room = (Room) getArguments().getSerializable("Room");

         syncWithXML();

         getDataFromFirebase();
         return mView;
    }


    private void syncWithXML() {
        contactBtn = mView.findViewById(R.id.contact_owner);
        location = mView.findViewById(R.id.view_location);
        price = mView.findViewById(R.id.view_price);
        type = mView.findViewById(R.id.view_type);
        floor = mView.findViewById(R.id.view_floor);
        preference = mView.findViewById(R.id.view_preference);
        cooking = mView.findViewById(R.id.view_cooking);
        rent = mView.findViewById(R.id.view_rent);
        date = mView.findViewById(R.id.view_date);
        name = mView.findViewById(R.id.view_name);
        timing = mView.findViewById(R.id.view_timing);
        back = mView.findViewById(R.id.view_back);
        roompic = mView.findViewById(R.id.view_room);
        owner = mView.findViewById(R.id.view_owner);
        mainLayout = mView.findViewById(R.id.room_layout);
        contactLayout = mView.findViewById(R.id.contact_layout);

        back.setOnClickListener(this);
        contactBtn.setOnClickListener(this);
        location.setOnClickListener(this);

    }


    private void getDataFromFirebase() {

//        as the data is already called in the previous fragment we will use the same data to load the view
//        location,price,type,floor,preference,cooking,rent,date,name,timing;
        location.setText(room.getAddress());
        price.setText("Rs: "+String.valueOf(room.getPrice()));
        type.setText(room.getRoomType());
        floor.setText("Floor: "+room.getFloor());
        preference.setText("Preference: "+room.getPreference());
        cooking.setText("Cooking: "+room.getCooking());
        rent.setText("Rent: "+room.getRent());
        date.setText("Available From:"+room.getDate());
        timing.setText("Contact Timing: "+room.getContact());




        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage pics = FirebaseStorage.getInstance();

        CollectionReference ownerref = db.collection("/Users/");

        StorageReference roompics = pics.getReference("/Rooms/"+room.getDocrefId()).child("pics");
        StorageReference ownerpics = pics.getReference("/Users/"+room.getOwner())
                .child("profile");

        ownerref.document(room.getOwner()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot d = task.getResult();
                    ownerName = d.getString(USER_NAME_KEY);
                    ownerContact = d.getString(USER_CONTACT_KEY);

                    name.setText("Name: " + ownerName);
                }else{
                    toast(task.getException().getMessage());
                }
            }
        });

        roompics.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Picasso.get().load(task.getResult().toString()).fit().into(roompic);
                }else{
                    Log.d("ViewRoom",task.getException().getMessage());
                }
            }
        });

        ownerpics.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Picasso.get().load(task.getResult().toString()).fit().into(owner);
                }else{
                    Log.d("ViewRoom",task.getException().getMessage());
                }
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.view_back:
                getActivity().onBackPressed();
                break;
            case R.id.contact_owner:
                //contact owner
                contactOwner();
                break;
            case R.id.view_location:
                //show in map
                toast("Under Development");
                break;
        }
    }

    private void contactOwner() {
        contactBtn.setEnabled(false);
        location.setEnabled(false);

        contactLayout.setVisibility(View.VISIBLE);

        ImageView ownerBig = mView.findViewById(R.id.contact_owner_pic);
        ownerBig.setImageDrawable(owner.getDrawable());

        mView.findViewById(R.id.call_owner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+ownerContact));
                startActivity(intent);
            }
        });

        mView.findViewById(R.id.message_owner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("sms:"+ownerContact));
                intent.putExtra("sms_body","Hey, "+ownerName+", "+DEFAULT_MESSAGE);
                startActivity(intent);
            }
        });



        contactLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    contactLayout.setVisibility(View.GONE);

                    Handler h = new Handler();

                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    contactBtn.setEnabled(true);
                                    location.setEnabled(true);
                                }
                            });
                        }
                    };

                    h.postDelayed(r,100);


                }
                return false;
            }
        });
    }




    private void toast(String msg){
        Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
    }
}
