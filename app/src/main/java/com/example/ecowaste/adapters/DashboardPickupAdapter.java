package com.example.ecowaste.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecowaste.R;
import com.example.ecowaste.models.PickupModel;
import com.example.ecowaste.utils.StatusUtils;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DashboardPickupAdapter extends RecyclerView.Adapter<DashboardPickupAdapter.ViewHolder> {

    private final Context context;
    private final List<PickupModel> pickupList;

    public DashboardPickupAdapter(Context context, List<PickupModel> pickupList) {
        this.context = context;
        this.pickupList = pickupList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_pickup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PickupModel pickup = pickupList.get(position);
        holder.bind(pickup);
    }

    @Override
    public int getItemCount() {
        return pickupList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate, tvStatus, tvAddress, tvPin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPin = itemView.findViewById(R.id.tvPin);
        }

        public void bind(final PickupModel pickup) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            if (pickup.getCreatedAt() != null) {
                tvDate.setText(sdf.format(pickup.getCreatedAt()));
            }
            tvStatus.setText(StatusUtils.getDisplayStatus(pickup.getStatus()));
            
            // Set status color based on status
            String status = pickup.getStatus();
            if ("COMPLETED".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.gradient_button);
            } else if ("REJECTED".equals(status) || "CANCELLED".equals(status)) {
                tvStatus.setBackgroundColor(Color.RED);
            } else if ("ACCEPTED_BY_CENTER".equals(status) || "PICKED_UP".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.gradient_splash);
            } else {
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            }

            tvAddress.setText(pickup.getAddress());
            
            // Requirement: Pin visibility until completed
            if (pickup.getPickupPin() != null && !"COMPLETED".equals(pickup.getStatus())) {
                tvPin.setText("PIN: " + pickup.getPickupPin());
                tvPin.setVisibility(View.VISIBLE);
            } else {
                tvPin.setVisibility(View.GONE);
            }
        }
    }
}
