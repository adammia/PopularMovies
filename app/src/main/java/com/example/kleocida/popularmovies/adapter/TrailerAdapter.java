package com.example.kleocida.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.kleocida.popularmovies.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {
    private List<String> trailers;
    final private ItemClickListener clickListener;


    private static final String TRAIELR_BASE_URL = "http://img.youtube.com/vi/";
    private static final String TRAILER_RESOLUTION_URL = "/0.jpg";
    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";

    public TrailerAdapter(ItemClickListener listener) {
        clickListener = listener;
    }

    public interface ItemClickListener {
        void onTrailerClick(int clickItemPosition);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutId = R.layout.trailers_items;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutId, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.trailerIv.getContext();
        String youTubeKey = trailers.get(position);

        String thumbUrl = TRAIELR_BASE_URL + youTubeKey + TRAILER_RESOLUTION_URL;

        Picasso.with(context)
                .load(thumbUrl)
                .into(holder.trailerIv);

    }

    @Override
    public int getItemCount() {
        if (null == trailers || trailers.isEmpty())
            return 0;
        else
            return trailers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView trailerIv;

        public ViewHolder(View itemView) {
            super(itemView);

            trailerIv = itemView.findViewById(R.id.iv_trailer);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clicked = getAdapterPosition();
            clickListener.onTrailerClick(clicked);

            openYouTubeApp(clicked, trailerIv.getContext());
        }
    }

    public void setTrailers(List<String> trailers) {
        this.trailers = trailers;
        notifyDataSetChanged();
    }

    private void openYouTubeApp(int position, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(YOUTUBE_BASE_URL + trailers.get(position)));
        context.startActivity(intent);

    }
}