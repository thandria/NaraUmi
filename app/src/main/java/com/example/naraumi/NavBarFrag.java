package com.example.naraumi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NavBarFrag extends Fragment {
    
        
        private static final String LOG_TAG = "NavBarFrag";
    
        
        @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
                           @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
                return inflater.inflate(R.layout.nav_bar, container, false);
    }
    
        @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
                    }
    
        
        public View getNavigationButton(int buttonId) {
        View rootView = getView();
        return (rootView != null) ? rootView.findViewById(buttonId) : null;
    }
    
        public boolean isViewReady() {
        return getView() != null;
    }
}
