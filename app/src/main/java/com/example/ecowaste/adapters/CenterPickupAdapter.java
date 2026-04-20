package com.example.ecowaste.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.models.PickupModel;
import com.example.ecowaste.utils.StatusUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CenterPickupAdapter extends RecyclerView.Adapter<CenterPickupAdapter.ViewHolder> {

    private Context context;
    private List<PickupModel> pickupList;
    private OnPickupClickListener listener;

    public interface OnPickupClickListener {
        void onPickupClick(PickupModel pickup);
    }

    public CenterPickupAdapter(Context context, List<PickupModel> pickupList, OnPickupClickListener listener) {
        this.context = context;
        this.pickupList = pickupList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_center_pickup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PickupModel pickup = pickupList.get(position);

        holder.tvUserName.setText(pickup.getUserName() != null ? pickup.getUserName() : "User");
        holder.tvWasteType.setText(pickup.getType());
        
        // Fixed: removed null check for primitive double
        holder.tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg", pickup.getEstimatedWeight()));

        String status = pickup.getStatus();
        holder.tvStatus.setText(StatusUtils.getDisplayStatus(status));

        // Professional dynamic status coloring
        int statusColor;
        int statusBgColor;
        
        if ("COMPLETED".equals(status) || "SOLD".equals(status)) {
            statusColor = ContextCompat.getColor(context, R.color.primary_dark);
            statusBgColor = ContextCompat.getColor(context, R.color.primary_light);
        } else if ("ACCEPTED_BY_COMPANY".equals(status)) {
            statusColor = ContextCompat.getColor(context, R.color.secondary_dark);
            statusBgColor = ContextCompat.getColor(context, R.color.secondary_light);
        } else {
            statusColor = ContextCompat.getColor(context, R.color.accent);
            statusBgColor = ContextCompat.getColor(context, R.color.accent_light);
        }

        holder.tvStatus.setTextColor(statusColor);
        holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(statusBgColor));
        holder.statusIndicator.setBackgroundColor(statusColor);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        if (pickup.getCreatedAt() != null) {
            holder.tvDate.setText(sdf.format(pickup.getCreatedAt()));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPickupClick(pickup);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pickupList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvWasteType, tvStatus, tvDate, tvWeight;
        View statusIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvWasteType = itemView.findViewById(R.id.tvWasteType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }
    }
}
