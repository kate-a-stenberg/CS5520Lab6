package com.numad.firesidechat;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.numad.firesidechat.databinding.FragmentDashboardBinding;

import java.util.ArrayList;

public class DashboardFragment extends Fragment implements RecyclerAdapter.OnNoteListener {

    FragmentDashboardBinding binding;
    FirebaseDatabase database;
    DatabaseReference dbRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        RecyclerAdapter adapter;
        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;

        layoutManager = new LinearLayoutManager((this.getContext()));
        binding.recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(new ArrayList<Contact>(), this);
        binding.recyclerView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();

        binding.buttonNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Contact contact = new Contact(binding.editTextName.getText().toString());
                // dbRef.child("users").push().setValue(contact);
                // dbRef.child("messageHistory").push().setValue(contact);
            }
        });

        return binding.getRoot();

    }

    public void onNoteClick(int position) {
        Toast toast = Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT);
        toast.show();
    }

}