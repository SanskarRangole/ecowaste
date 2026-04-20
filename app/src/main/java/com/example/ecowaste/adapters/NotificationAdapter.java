package com.example.ecowaste.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.models.NotificationModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final Context context;
    private final List<NotificationModel> notificationList;
    private final OnNotificationClickListener onNotificationClickListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationModel notification);
    }

    public NotificationAdapter(Context context, List<NotificationModel> notificationList, OnNotificationClickListener onNotificationClickListener) {
        this.context = context;
        this.notificationList = notificationList;
        this.onNotificationClickListener = onNotificationClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);
        holder.bind(notification, onNotificationClickListener);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle, tvMessage, tvTimestamp;
        private final View viewUnreadIndicator;
        private final CardView cardNotification;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
            cardNotification = itemView.findViewById(R.id.cardNotification);
        }

        public void bind(final NotificationModel notification, final OnNotificationClickListener listener) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
            if (notification.getTimestamp() != null) {
                tvTimestamp.setText(sdf.format(notification.getTimestamp()));
            }

            itemView.setOnClickListener(v -> listener.onNotificationClick(notification));

            if (notification.isRead()) {
                // Read style
                viewUnreadIndicator.setVisibility(View.GONE);
                cardNotification.setCardBackgroundColor(Color.WHITE);
                cardNotification.setCardElevation(0f); // Flatter look for read
                itemView.setAlpha(0.7f);
            } else {
                // Unread style
                viewUnreadIndicator.setVisibility(View.VISIBLE);
                cardNotification.setCardBackgroundColor(Color.parseColor("#F5F9F6")); // Very light green tint
                cardNotification.setCardElevation(4f); // Pop out more
                itemView.setAlpha(1.0f);
            }
        }
    }
}
