package com.project.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.kothakhoj.R;
import com.project.kothakhoj.ViewRoom;
import com.project.models.Room;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.project.Util.Util.*;
/**
 * Author : nilkamal,
 * Creation Date: 11/8/18.
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.RoomViewHolder>{


    Context c,activity;
    private int blank_heart = R.drawable.ic_heart;
    private int full_heart = R.drawable.ic_heart_filled;

    private List<Room> rooms;

    public CustomAdapter(Context activity,List<Room> rooms){
        this.rooms = rooms;
        this.activity = activity;
    }


    public CustomAdapter() {
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_card_view_others, parent, false);
        RoomViewHolder roomViewHolder = new RoomViewHolder(v);
        c = v.getContext();
        return roomViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RoomViewHolder holder, final int position) {
        holder.rent.setText(rooms.get(position).rent);
        holder.price.setText(String.valueOf(rooms.get(position).price));
        holder.date.setText(rooms.get(position).date);
        holder.roomType.setText(rooms.get(position).roomType);
        holder.address.setText(rooms.get(position).address);
        final String docrefId = rooms.get(position).docrefId;
        holder.saveButton.setImageResource(blank_heart);
        holder.saveButton.setBackgroundResource(R.color.colorGrey50);




        StorageReference load = FirebaseStorage.getInstance().getReference("/Rooms/"+docrefId)
                .child("pics");
        load.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Picasso.get().load(task.getResult().toString()).fit().into(holder.imgView);
                }else{
                    Log.d("CustomAdapter",task.getException().getMessage());
                }
            }
        });



        holder.viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewRoom(position);
            }
        });




        holder.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRoom(holder,docrefId,position);
            }
        });
    }

    private void saveRoom(final RoomViewHolder holder, final String docrefId, final int position) {
        final CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("SavedRooms")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("interested");
        collectionReference.whereEqualTo(docrefId,ROOM_ID).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        boolean isSaved = false;
                        for(QueryDocumentSnapshot q: task.getResult()){
                            if(q.getString(ROOM_ID)!=null){
                                toast("Already Saved");
                                isSaved = true;
                                if((Integer)holder.saveButton.getTag() == full_heart){
                                    holder.saveButton.setImageResource(blank_heart);
                                }
                            }
                            break;
                        }
                        if(!isSaved){
                            Map<String,Object> roomDetails = new HashMap<>();
                            roomDetails.put(ROOM_ID,docrefId);
                            roomDetails.put(ROOM_DATE,rooms.get(position).date);
                            roomDetails.put(ROOM_ADDRESS,rooms.get(position).address);
                            roomDetails.put(ROOM_PRICE,rooms.get(position).price);
                            roomDetails.put(ROOM_RENT,rooms.get(position).address);
                            roomDetails.put(ROOM_TYPE,rooms.get(position).roomType);


                            collectionReference
                                    .add(roomDetails)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if(task.isSuccessful()){
                                                toast("Successfully added to saved room");
                                                holder.saveButton.setBackgroundResource(full_heart);
                                            }else {
                                                toast("ERROR: "+task.getException().getMessage());
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void viewRoom(final int position) {
        ViewRoom v = new ViewRoom();
        Bundle args = new Bundle();
        args.putSerializable("Room", (Serializable) rooms.get(position));
        v.setArguments(args);
        changeFragment(((AppCompatActivity) activity).getSupportFragmentManager(),homeFrameLayout,v,"ViewRoom");
    }



    @Override
    public int getItemCount() {
        int data;
        if(rooms != null && !rooms.isEmpty()){
            data = rooms.size();
        }else{
            data = 0;
        }
        return data;
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        private View viewButton;
        private TextView roomType;
        private  TextView address;
        private  TextView price;
        private  TextView date;
        private  TextView rent;
        private ImageView imgView,saveButton;

//            private  TextView preference;
//            private  TextView floor;
//            private  TextView contact;
//            private  TextView cooking;
//            private  TextView owner;

        RoomViewHolder(View itemView) {
            super(itemView);
            viewButton = itemView.findViewById(R.id.cv);
            roomType = itemView.findViewById(R.id.room_type);
            address = itemView.findViewById(R.id.room_location);
            rent = itemView.findViewById(R.id.room_rent);
            date = itemView.findViewById(R.id.room_date);
            price = itemView.findViewById(R.id.room_price);
            saveButton = itemView.findViewById(R.id.room_options);
            imgView = itemView.findViewById(R.id.room_pics);
        }
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void toast(String message){
        Toast.makeText(c,message,Toast.LENGTH_SHORT).show();
    }

}