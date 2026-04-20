package com.example.ecowaste.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.activities.PickupDetailActivity;
import com.example.ecowaste.models.PickupModel;
import com.example.ecowaste.utils.StatusUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MemberPickupAdapter extends RecyclerView.Adapter<MemberPickupAdapter.ViewHolder> {

    private Context context;
    private List<PickupModel> pickupList;
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public MemberPickupAdapter(Context context, List<PickupModel> pickupList) {
        this.context = context;
        this.pickupList = pickupList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_member_pickup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PickupModel pickup = pickupList.get(position);

        holder.tvUserName.setText(pickup.getUserName() != null ? pickup.getUserName() : "User");
        holder.tvWasteType.setText(pickup.getCategory() + " - " + pickup.getType());
        holder.tvStatusBadge.setText(StatusUtils.getDisplayStatus(pickup.getStatus()).toUpperCase());
        
        if (pickup.getCreatedAt() != null) {
            holder.tvDate.setText(sdf.format(pickup.getCreatedAt()));
        }

        double amount = pickup.getUserAmount();
        if (amount > 0) {
            holder.tvAmountValue.setText("₹" + String.format(Locale.getDefault(), "%.0f", amount));
            holder.tvAmountLabel.setVisibility(View.VISIBLE);
            holder.tvAmountValue.setVisibility(View.VISIBLE);
        } else {
            holder.tvAmountLabel.setVisibility(View.GONE);
            holder.tvAmountValue.setVisibility(View.GONE);
        }

        // Status Badge Color Logic
        int colorRes = R.color.primary;
        String status = pickup.getStatus();
        if ("PENDING_REVIEW".equals(status)) colorRes = R.color.secondary;
        else if ("OFFER_SENT".equals(status)) colorRes = R.color.accent;
        else if ("COLLECTED".equals(status)) colorRes = R.color.primary;
        else if ("REJECTED".equals(status) || "CANCELLED".equals(status)) colorRes = R.color.error;

        holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)));

        // Category Icon Logic
        int iconRes = R.drawable.ic_pickup;
        String cat = pickup.getCategory();
        if ("Mobile".equals(cat)) iconRes = R.drawable.ic_mobile_realistic;
        else if ("Laptop".equals(cat)) iconRes = R.drawable.ic_laptop_realistic;
        
        holder.ivTypeIcon.setImageResource(iconRes);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PickupDetailActivity.class);
            intent.putExtra("pickupId", pickup.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return pickupList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvWasteType, tvStatusBadge, tvDate, tvAmountLabel, tvAmountValue;
        ImageView ivTypeIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvWasteType = itemView.findViewById(R.id.tvWasteType);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmountLabel = itemView.findViewById(R.id.tvAmountLabel);
            tvAmountValue = itemView.findViewById(R.id.tvAmountValue);
            ivTypeIcon = itemView.findViewById(R.id.ivTypeIcon);
        }
    }
}
