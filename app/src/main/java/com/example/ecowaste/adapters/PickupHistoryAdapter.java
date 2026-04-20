package com.example.ecowaste.adapters;

import android.content.Context;
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

public class PickupHistoryAdapter extends RecyclerView.Adapter<PickupHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<PickupModel> pickupList;
    private final OnPickupClickListener onPickupClickListener;

    public interface OnPickupClickListener {
        void onPickupClick(PickupModel pickup);
    }

    public PickupHistoryAdapter(Context context, List<PickupModel> pickupList, OnPickupClickListener onPickupClickListener) {
        this.context = context;
        this.pickupList = pickupList;
        this.onPickupClickListener = onPickupClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pickup_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PickupModel pickup = pickupList.get(position);
        holder.bind(pickup, onPickupClickListener);
    }

    @Override
    public int getItemCount() {
        return pickupList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate, tvStatus, tvAddress, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }

        public void bind(final PickupModel pickup, final OnPickupClickListener listener) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            if (pickup.getCreatedAt() != null) {
                tvDate.setText(sdf.format(pickup.getCreatedAt()));
            }
            tvStatus.setText(StatusUtils.getDisplayStatus(pickup.getStatus()));
            tvAddress.setText(pickup.getAddress());
            tvAmount.setText(String.format(Locale.getDefault(), "₹%.2f", pickup.getUserAmount()));

            itemView.setOnClickListener(v -> listener.onPickupClick(pickup));
        }
    }
}
