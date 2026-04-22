package com.example.arlacontrole.ui.management;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arlacontrole.R;
import com.example.arlacontrole.databinding.ActivityManagementHelpBinding;

public class ManagementHelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityManagementHelpBinding binding = ActivityManagementHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(R.string.management_help_title);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
}
