package com.example.ecowaste.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.models.BulkSaleModel;

import java.util.List;
import java.util.Map;

public class BulkSaleRequestAdapter extends RecyclerView.Adapter<BulkSaleRequestAdapter.ViewHolder> {

    private List<BulkSaleModel> bulkSales;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BulkSaleModel bulkSale);
    }

    public BulkSaleRequestAdapter(List<BulkSaleModel> bulkSales, OnItemClickListener listener) {
        this.bulkSales = bulkSales;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_sale_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BulkSaleModel bulkSale = bulkSales.get(position);
        holder.tvMemberName.setText("From: " + (bulkSale.getMemberName() != null ? bulkSale.getMemberName() : "Unknown"));
        
        StringBuilder details = new StringBuilder();
        if (bulkSale.getWasteWeights() != null) {
            for (Map.Entry<String, Double> entry : bulkSale.getWasteWeights().entrySet()) {
                details.append(entry.getKey()).append(": ").append(entry.getValue()).append("kg, ");
            }
            if (details.length() > 2) details.setLength(details.length() - 2);
        } else {
            details.append("No items listed");
        }
        
        holder.tvDetails.setText(details.toString());
        holder.tvStatus.setText(bulkSale.getStatus() != null ? bulkSale.getStatus() : "PENDING");
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(bulkSale));
    }

    @Override
    public int getItemCount() {
        return bulkSales.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvDetails, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvUserName);
            tvDetails = itemView.findViewById(R.id.tvWasteType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
