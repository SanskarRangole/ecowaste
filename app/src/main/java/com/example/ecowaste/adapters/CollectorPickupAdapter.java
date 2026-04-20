package com.example.ecowaste.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.models.PickupModel;
import com.example.ecowaste.utils.StatusUtils;

import java.util.List;

public class CollectorPickupAdapter extends RecyclerView.Adapter<CollectorPickupAdapter.ViewHolder> {

    private List<PickupModel> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String pickupId);
    }

    public CollectorPickupAdapter(List<PickupModel> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pickup_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PickupModel pickup = list.get(position);
        holder.tvId.setText("ID: " + pickup.getId().substring(0, 8));
        holder.tvUser.setText(pickup.getUserName());
        holder.tvWaste.setText(pickup.getCategory());
        holder.tvStatus.setText(StatusUtils.getDisplayStatus(pickup.getStatus()));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(pickup.getId()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvUser, tvWaste, tvStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvPickupId);
            tvUser = itemView.findViewById(R.id.tvUserName);
            tvWaste = itemView.findViewById(R.id.tvWasteType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}