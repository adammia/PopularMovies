package com.example.kleocida.popularmovies.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.kleocida.popularmovies.DetailActivity;
import com.example.kleocida.popularmovies.MainActivity;
import com.example.kleocida.popularmovies.R;
import com.example.kleocida.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PosterAdapter extends RecyclerView.Adapter<PosterAdapter.ViewHolder> {

    private List<Movie> movies;
    final private ItemClickListener clickListener;
    private boolean isFavorite = false;

    private static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String POSTER_WIDTH_URL = "w185";

    public PosterAdapter(ItemClickListener listener) {
        clickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutId = R.layout.posters_items;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutId, parent, false);

        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView posterImageView;

        private ViewHolder(View itemView) {
            super(itemView);

            posterImageView = itemView.findViewById(R.id.iv_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clicked = getAdapterPosition();
            clickListener.onItemClick(clicked);

            launchDetailActivity(clicked, posterImageView.getContext());
        }
    }

    private void launchDetailActivity(int position, Context context) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("Movie", movies.get(position));
        intent.putExtra("MoviePosition", position);
        intent.putExtra("isFavorite", isFavorite);
        ((Activity) context).startActivityForResult(intent, MainActivity.DETAILS_INTENT_REQUEST);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context context = holder.posterImageView.getContext();
        Movie movieObj = movies.get(position);
        String posterPath = movieObj.getPosterPath();

        String posterUrl = POSTER_BASE_URL + POSTER_WIDTH_URL + posterPath;

        Picasso.with(context)
                .load(posterUrl)
                .into(holder.posterImageView);
    }

    @Override
    public int getItemCount() {
        if (null == movies || movies.isEmpty())
            return 0;
        else
            return movies.size();
    }

    public interface ItemClickListener {
        void onItemClick(int clickItemPosition);
    }

    public void setPoster(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public List<Movie> getMovies() {
        return movies;
    }


    public void updateMovieTrailer(int position, Movie newMovie) {
        if (this.movies != null) {
            this.movies.set(position, newMovie);
            notifyItemChanged(position);
        }
    }
}