package com.example.ecowaste.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.models.CenterModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CenterSelectAdapter extends RecyclerView.Adapter<CenterSelectAdapter.ViewHolder> {

    private List<CenterModel> centers;
    private List<String> selectedCenterIds = new ArrayList<>();

    public CenterSelectAdapter(List<CenterModel> centers) {
        this.centers = centers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_center_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CenterModel center = centers.get(position);
        holder.tvCenterName.setText(center.getCenterName());
        holder.tvDynamicRate.setText(String.format(Locale.getDefault(), "Current Rate: ₹%.2f/kg", center.getCurrentRate()));
        holder.tvTerms.setText("Terms: " + (center.getTermsAndConditions() != null ? center.getTermsAndConditions() : "No specific terms."));

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(selectedCenterIds.contains(center.getCenterId()));

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedCenterIds.contains(center.getCenterId())) {
                    selectedCenterIds.add(center.getCenterId());
                }
            } else {
                selectedCenterIds.remove(center.getCenterId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return centers.size();
    }

    public List<String> getSelectedCenterIds() {
        return selectedCenterIds;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCenterName, tvDynamicRate, tvTerms;
        CheckBox cbSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCenterName = itemView.findViewById(R.id.tvCenterName);
            tvDynamicRate = itemView.findViewById(R.id.tvDynamicRate);
            tvTerms = itemView.findViewById(R.id.tvTerms);
            cbSelect = itemView.findViewById(R.id.cbSelect);
        }
    }
}
