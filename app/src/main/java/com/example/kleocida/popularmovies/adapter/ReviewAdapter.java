package com.example.kleocida.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kleocida.popularmovies.R;
import com.example.kleocida.popularmovies.ReviewActivity;
import com.example.kleocida.popularmovies.model.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> reviews;
    private String title;
    final private ItemClickListener clickListener;

    public ReviewAdapter(ItemClickListener listener) {
        clickListener = listener;
    }

    public interface ItemClickListener {
        void onReviewClick(int clickItemPosition);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutId = R.layout.reviews_items;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String author = reviews.get(position).getAuthor();
        String comment = reviews.get(position).getComment();

        holder.authorTv.setText(author);
        holder.commentTv.setText(comment);
        if (position == 0) {
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
            layoutParams.setMarginStart(0);
            holder.cardView.requestLayout();
        }
    }

    @Override
    public int getItemCount() {
        if (reviews == null || reviews.isEmpty()) {
            return 0;
        } else {
            return reviews.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView authorTv;
        private TextView commentTv;
        private CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);

            authorTv = itemView.findViewById(R.id.tv_author);
            commentTv = itemView.findViewById(R.id.tv_comment);
            cardView = itemView.findViewById(R.id.card_reviews);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clicked = getAdapterPosition();
            clickListener.onReviewClick(clicked);

            launchDetailComment(clicked, authorTv.getContext());
        }
    }

    private void launchDetailComment(int position, Context context) {
        Intent intent = new Intent(context, ReviewActivity.class);
        intent.putExtra("author", reviews.get(position).getAuthor());
        intent.putExtra("comment", reviews.get(position).getComment());
        intent.putExtra("title", title);
        context.startActivity(intent);
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
