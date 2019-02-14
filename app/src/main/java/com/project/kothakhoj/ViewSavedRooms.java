package com.project.kothakhoj;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.adapters.CustomAdapter;
import com.project.models.Room;

import java.util.ArrayList;

import static com.project.Util.Util.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewSavedRooms extends Fragment{


    private View mView;
    private ArrayList<Room> rooms;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    public ViewSavedRooms() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_view_saved_rooms, container, false);
        recyclerView = mView.findViewById(R.id.savedRoomRecycler);
        progressBar = mView.findViewById(R.id.progressViewSavedRooms);
        rooms = new ArrayList<>();
        adapter = new CustomAdapter(getActivity(),rooms);
        return mView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new CustomAdapter());
        initRecyclerView();
    }

    private void initRecyclerView() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("SavedRooms")
                .document(firebaseAuth.getCurrentUser().getUid())
                .collection("interested").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        rooms.clear();
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot d:task.getResult()){

//roomType, address,  price, date, preference, floor, contact, cooking, rent, owner,docrefId
                                rooms.add(new Room(d.getString(ROOM_TYPE),d.getString(ROOM_ADDRESS),d
                                        .getLong(ROOM_PRICE),d.getString(ROOM_DATE),d
                                        .getString(ROOM_PREFERENCE),d.getString(ROOM_FLOOR),d
                                        .getString(ROOM_CONTACT),d.getString(ROOM_COOKING),d
                                        .getString(ROOM_RENT),d.getString(ROOM_OWNER),d
                                        .getString(ROOM_ID)));
                            }
                            recyclerView.setAdapter(new CustomAdapter(getActivity(),rooms));
                            progressBar.setVisibility(View.GONE);
                        }else{
                            toast("Unable to Load Data");
                            toast(task.getException().getMessage());
                        }
                    }
                });
    }



    private void toast(String message){
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onResume() {
        super.onResume();
        if(adapter.getItemCount() == 0){
            progressBar.setVisibility(View.VISIBLE);
            initRecyclerView();
        }
    }

}
