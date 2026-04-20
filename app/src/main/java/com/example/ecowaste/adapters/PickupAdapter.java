package com.example.ecowaste.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.activities.PickupDetailActivity;
import com.example.ecowaste.models.Pickup;
import com.example.ecowaste.utils.StatusUtils;

import java.util.List;

public class PickupAdapter extends RecyclerView.Adapter<PickupAdapter.PickupViewHolder> {

    private Context context;
    private List<Pickup> pickupList;

    public PickupAdapter(Context context, List<Pickup> pickupList) {
        this.context = context;
        this.pickupList = pickupList;
    }

    @NonNull
    @Override
    public PickupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pickup, parent, false);
        return new PickupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PickupViewHolder holder, int position) {
        Pickup pickup = pickupList.get(position);
        holder.tvUserName.setText(pickup.getUserName() != null ? pickup.getUserName() : "User");
        holder.tvType.setText(pickup.getType());
        holder.tvDate.setText(pickup.getPreferredDate());
        holder.tvStatus.setText(StatusUtils.getDisplayStatus(pickup.getStatus()));

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

    public static class PickupViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvType, tvDate, tvStatus;

        public PickupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvType = itemView.findViewById(R.id.tvType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
