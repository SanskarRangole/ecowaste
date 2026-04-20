package com.example.ecowaste.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.ecowaste.R;
import com.example.ecowaste.activities.GalleryDetailActivity;
import com.example.ecowaste.models.GalleryImage;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private final Context context;
    private final List<GalleryImage> imageList;

    public GalleryAdapter(Context context, List<GalleryImage> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GalleryImage image = imageList.get(position);
        holder.bind(image);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivImage;
        private final TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }

        public void bind(final GalleryImage image) {
            tvTitle.setText(image.getTitle());
            Glide.with(context).load(image.getImageResId()).into(ivImage);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, GalleryDetailActivity.class);
                intent.putExtra("imageResId", image.getImageResId());
                intent.putExtra("title", image.getTitle());
                context.startActivity(intent);
            });
        }
    }
}
