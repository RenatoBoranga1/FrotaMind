package com.example.arlacontrole.ui.configuracoes;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arlacontrole.R;
import com.example.arlacontrole.databinding.ActivitySingleFragmentBinding;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.utils.AppPreferences;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!UserRole.canAccessSettings(new AppPreferences(this).getSession().role)) {
            finish();
            return;
        }

        ActivitySingleFragmentBinding binding = ActivitySingleFragmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setTitle(R.string.settings_title);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.fragmentHost.getId(), new SettingsFragment())
                .commit();
        }
    }
}
