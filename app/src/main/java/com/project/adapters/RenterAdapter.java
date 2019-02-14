package com.project.adapters;

/**
 * Author : nilkamal,
 * Creation Date: 13/8/18.
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.kothakhoj.EditRoom;
import com.project.kothakhoj.R;
import com.project.kothakhoj.RenterFragment;
import com.project.models.Room;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.project.Util.Util.*;

/**
 * Author : nilkamal,
 * Creation Date: 11/8/18.
 */
public class RenterAdapter extends RecyclerView.Adapter<RenterAdapter.RoomViewHolder>{


    Context c;

    private List<Room> rooms;
    private FragmentManager manager;

    public RenterAdapter(List<Room> rooms,FragmentManager manager){
        this.rooms = rooms;
        this.manager = manager;
    }


    public RenterAdapter() {
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
        holder.editButton.setBackgroundResource(R.color.colorGrey50);
        holder.editButton.setImageResource(R.drawable.ic_edit);


        StorageReference load = FirebaseStorage.getInstance().getReference("/Rooms/"+docrefId)
                .child("pics");
        load.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Picasso.get().load(task.getResult().toString()).fit().centerInside().into(holder.imgView);
                }else{
                    Log.d("RenterAdapter",task.getException().getMessage());
                }
            }
        });


//        holder.viewButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Rooms");
//                collectionReference.whereEqualTo("Owner", FirebaseAuth.getInstance().getCurrentUser().getUid())
//                        .get()
//                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                            @Override
//                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                for(QueryDocumentSnapshot qd:queryDocumentSnapshots){
//                                    qd.getReference().delete();
//                                }
//                                toast("SuccessFully Deleted");
//                            }
//                        });
//            }
//        });



        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c,EditRoom.class).addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                i.putExtra("Room",rooms.get(position));
                c.startActivity(i);
            }
        });

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
        private ImageView imgView,editButton;


        RoomViewHolder(View itemView) {
            super(itemView);
            viewButton = itemView.findViewById(R.id.cv);
            roomType = itemView.findViewById(R.id.room_type);
            address = itemView.findViewById(R.id.room_location);
            rent = itemView.findViewById(R.id.room_rent);
            date = itemView.findViewById(R.id.room_date);
            price = itemView.findViewById(R.id.room_price);
            editButton = itemView.findViewById(R.id.room_options);
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
