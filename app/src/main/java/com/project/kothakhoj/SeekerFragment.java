package com.project.kothakhoj;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
public class SeekerFragment extends Fragment {

    private View seekHome,priceFilter,addressFilter;
    private RecyclerView recyclerView;
    private ArrayList<Room> rooms;
    private ProgressBar progressBar;
    private CustomAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private Spinner filter1;
    private EditText maxPrice,minPrice;

    protected GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    public SeekerFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        seekHome = inflater.inflate(R.layout.fragment_seeker, container, false);
        recyclerView = seekHome.findViewById(R.id.recyclerView);
        progressBar = seekHome.findViewById(R.id.progressBar);
        maxPrice = seekHome.findViewById(R.id.filter_max);
        minPrice = seekHome.findViewById(R.id.filter_min);
        priceFilter = seekHome.findViewById(R.id.priceFilter);
        addressFilter = seekHome.findViewById(R.id.addressFilter);

        rooms = new ArrayList<>();
        adapter = new CustomAdapter(getActivity(),rooms);
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        swipeRefresh = seekHome.findViewById(R.id.swipeRefreshSeeker);
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

        loadSpinners();

        return seekHome;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PlaceAutocompleteFragment f = (PlaceAutocompleteFragment) getActivity().getFragmentManager()
                .findFragmentById(R.id.place_autocomplete_fragment1);
        if (f != null)
            getActivity().getFragmentManager().beginTransaction().remove(f).commit();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getActivity(), null);
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment1);

        autocompleteFragment.setHint("Address");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                queryFilterOptions(place.getName().toString(),null,false);
            }

            @Override
            public void onError(Status status) {
                toast(status.getStatusMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_filter) {
            seekHome.findViewById(R.id.filter_options).setVisibility(View.VISIBLE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void queryFilterOptions(String value1,String value2,boolean isPrice){
        ArrayList<Room> filteredRooms = new ArrayList<>();
        if(isPrice){
            if(value1.isEmpty()){
                minPrice.setText("0");
                value1 = "0";
            }

            if(value2.isEmpty()){
                maxPrice.setText("10000");
                value2 = "10000";
            }
            int min = Integer.parseInt(value1);
            int max = Integer.parseInt(value2);
            if(rooms.size() > 0){
                for(Room r:rooms){
                    if(r.getPrice() <= max &&  r.getPrice() >= min){
                        filteredRooms.add(r);
                    }
                }
                recyclerView.setAdapter(new CustomAdapter(getActivity(),filteredRooms));
                rooms = filteredRooms;
            }else{
                queryFirebasePrice(min,max);
            }
        }else{
            if(rooms.size() > 0){
                for(Room r:rooms){
                    if(r.getAddress().contains(value1)){
                        filteredRooms.add(r);
                    }
                }
                recyclerView.setAdapter(new CustomAdapter(getActivity(),filteredRooms));
                rooms = filteredRooms;
            }else{
                queryFirebasePrice(0,0);
            }
        }
    }

    private void queryFirebasePrice(int minPrice, int maxPrice) {
        toast("Need to fire query to firebase. Under Development");
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
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Rooms").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        rooms.clear();
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot d:task.getResult()){
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
        if(adapter.getItemCount() ==0){
            progressBar.setVisibility(View.VISIBLE);
            initRecyclerView();
        }
    }



    private void loadSpinners() {

        String[] filters = {"Address","Price"};

        filter1 =  seekHome.findViewById(R.id.filter1);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(),   R.layout.custom_spinner_layout, filters);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown); // The drop down view
        filter1.setAdapter(spinnerArrayAdapter);

        filter1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(filter1.getSelectedItem()=="Price"){
                    //show price filter value

                    addressFilter.setVisibility(View.GONE);
                    priceFilter.setVisibility(View.VISIBLE);
                    seekHome.findViewById(R.id.filter_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            queryFilterOptions(minPrice.getText().toString(),maxPrice.getText().toString(),true);
                        }
                    });

                }else if(filter1.getSelectedItem() == "Address"){
                    //call google places api and fire query to firebase
                    priceFilter.setVisibility(View.GONE);
                    addressFilter.setVisibility(View.VISIBLE);
                }else{
                    toast("Nothing Selected");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(0);
            }
        });

    }

    //    used for displaying the search icon at top bar
}
