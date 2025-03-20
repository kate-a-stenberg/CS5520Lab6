package com.example.lab6dashboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.lab6dashboard.databinding.FragmentChatDashboardBinding;

import java.util.ArrayList;

public class ChatDashboard extends Fragment implements RecyclerAdapter.OnNoteListener {

    FragmentChatDashboardBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentChatDashboardBinding.inflate(inflater, container, false);

        RecyclerAdapter adapter;
        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;

        layoutManager = new LinearLayoutManager((this.getContext()));
        binding.recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(new ArrayList<Person>(), this);
        binding.recyclerView.setAdapter(adapter);

        return binding.getRoot();

    }

    public void onNoteClick(int position) {
        Toast toast = Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT);
        toast.show();
    }

}