package com.example.ecowaste.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecowaste.R;
import com.example.ecowaste.models.AwarenessArticle;

import java.util.List;

public class AwarenessAdapter extends RecyclerView.Adapter<AwarenessAdapter.ViewHolder> {

    private final Context context;
    private final List<AwarenessArticle> articleList;
    private final OnArticleClickListener onArticleClickListener;

    public interface OnArticleClickListener {
        void onArticleClick(AwarenessArticle article);
    }

    public AwarenessAdapter(Context context, List<AwarenessArticle> articleList, OnArticleClickListener onArticleClickListener) {
        this.context = context;
        this.articleList = articleList;
        this.onArticleClickListener = onArticleClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_awareness, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AwarenessArticle article = articleList.get(position);
        holder.bind(article, onArticleClickListener);
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivArticleImage;
        private final TextView tvArticleTitle, tvArticleContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArticleImage = itemView.findViewById(R.id.ivArticleImage);
            tvArticleTitle = itemView.findViewById(R.id.tvArticleTitle);
            tvArticleContent = itemView.findViewById(R.id.tvArticleContent);
        }

        public void bind(final AwarenessArticle article, final OnArticleClickListener listener) {
            tvArticleTitle.setText(article.getTitle());
            tvArticleContent.setText(article.getContent());

            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                Glide.with(context).load(article.getImageUrl()).into(ivArticleImage);
            } else {
                // Set a placeholder if no image is available
                ivArticleImage.setImageResource(R.drawable.placeholder);
            }

            itemView.setOnClickListener(v -> listener.onArticleClick(article));
        }
    }
}
