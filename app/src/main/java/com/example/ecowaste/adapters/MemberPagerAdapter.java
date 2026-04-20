package com.example.ecowaste.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ecowaste.fragments.AcceptedPickupsFragment;
import com.example.ecowaste.fragments.AvailablePickupsFragment;
import com.example.ecowaste.fragments.BulkSalesFragment;
import com.example.ecowaste.fragments.InventoryFragment;

public class MemberPagerAdapter extends FragmentStateAdapter {

    public MemberPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AvailablePickupsFragment(); // New Requests
            case 1:
                return new AcceptedPickupsFragment();  // In Progress
            case 2:
                return new BulkSalesFragment();       // Bulk Sales
            case 3:
                return new InventoryFragment();       // Inventory/Collected
            default:
                return new AvailablePickupsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
