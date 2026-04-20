package com.example.ecowaste.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.models.CenterModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BulkOfferAdapter extends RecyclerView.Adapter<BulkOfferAdapter.ViewHolder> {

    private Map<String, Double> offers;
    private Map<String, String> statuses;
    private List<String> centerIds;
    private OnOfferDecisionListener listener;
    private String finalCenterId;

    public interface OnOfferDecisionListener {
        void onAccept(String centerId, double amount);
        void onReject(String centerId);
    }

    public BulkOfferAdapter(Map<String, Double> offers, Map<String, String> statuses, String finalCenterId, OnOfferDecisionListener listener) {
        this.offers = offers;
        this.statuses = statuses;
        this.centerIds = new ArrayList<>(offers.keySet());
        this.finalCenterId = finalCenterId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_offer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String centerId = centerIds.get(position);
        Double amount = offers.get(centerId);
        String status = statuses.get(centerId);

        holder.tvOfferedAmount.setText(String.format(Locale.getDefault(), "Offered Amount: ₹%.2f", amount));
        
        // Fetch Center Name
        FirebaseFirestore.getInstance().collection("centers").document(centerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        holder.tvCenterName.setText(documentSnapshot.getString("centerName"));
                    }
                });

        if (finalCenterId != null) {
            holder.layoutButtons.setVisibility(View.GONE);
            holder.tvStatus.setVisibility(View.VISIBLE);
            if (centerId.equals(finalCenterId)) {
                holder.tvStatus.setText("SOLD TO THIS CENTER");
                holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.primary));
            } else {
                holder.tvStatus.setText("NOT SELECTED");
                holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.error));
            }
        } else {
            if ("ACCEPTED".equals(status)) {
                holder.layoutButtons.setVisibility(View.VISIBLE);
                holder.tvStatus.setVisibility(View.GONE);
            } else {
                holder.layoutButtons.setVisibility(View.GONE);
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setText(status);
            }
        }

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(centerId, amount));
        holder.btnReject.setOnClickListener(v -> listener.onReject(centerId));
    }

    @Override
    public int getItemCount() {
        return centerIds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCenterName, tvOfferedAmount, tvStatus;
        LinearLayout layoutButtons;
        MaterialButton btnAccept, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCenterName = itemView.findViewById(R.id.tvCenterName);
            tvOfferedAmount = itemView.findViewById(R.id.tvOfferedAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            layoutButtons = itemView.findViewById(R.id.layoutButtons);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
