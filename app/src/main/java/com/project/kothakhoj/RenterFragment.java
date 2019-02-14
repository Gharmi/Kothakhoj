package com.project.kothakhoj;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.project.adapters.RenterAdapter;
import com.project.models.Room;

import java.util.ArrayList;

import static com.project.Util.Util.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class RenterFragment extends Fragment implements View.OnClickListener {



    private View mView;
    ArrayList<Room> rooms;
    RenterAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    public RenterFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_renter, container, false);
        mView.findViewById(R.id.addRoom).setOnClickListener(this);
        recyclerView = mView.findViewById(R.id.renterView);
        progressBar = mView.findViewById(R.id.progressBarRenter);
        getActivity().setTitle("My Rooms");
        rooms = new ArrayList<>();
        adapter = new RenterAdapter();
        swipeRefresh = mView.findViewById(R.id.swipeRefreshRenter);
        swipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        swipeRefresh.setRefreshing(true);
                        initRecyclerView();
                        swipeRefresh.setRefreshing(false);
                    }
                }
        );
        return mView;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addRoom:
                startActivity(new Intent(getActivity().getApplicationContext(),AddRoom.class).addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP));
        }

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new RenterAdapter());
        initRecyclerView();
    }

    private void initRecyclerView() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Rooms")
                .whereEqualTo("Owner",FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        rooms.clear();
                        if(task.isSuccessful()){
                            if(task.getResult().getDocuments().size() == 0){
                                mView.findViewById(R.id.renterHomeMsg).setVisibility(View.VISIBLE);
                            }else {
                                mView.findViewById(R.id.renterHomeMsg).setVisibility(View.GONE);
                                for (QueryDocumentSnapshot d : task.getResult()) {
                                    rooms.add(new Room(d.getString(ROOM_TYPE),d.getString(ROOM_ADDRESS),d
                                            .getLong(ROOM_PRICE),d.getString(ROOM_DATE),d
                                            .getString(ROOM_PREFERENCE),d.getString(ROOM_FLOOR),d
                                            .getString(ROOM_CONTACT),d.getString(ROOM_COOKING),d
                                            .getString(ROOM_RENT),d.getString(ROOM_OWNER),d
                                            .getString(ROOM_ID)));
                                }
                                recyclerView.setAdapter(new RenterAdapter(rooms, getActivity().getSupportFragmentManager()));
                            }
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
        if(adapter.getItemCount() ==0){
            progressBar.setVisibility(View.VISIBLE);
            initRecyclerView();
        }
    }

}
