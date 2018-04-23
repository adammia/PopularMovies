package com.example.kleocida.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavoritesContract {

    public static final String AUTHORITY = "com.example.kleocida.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIES = "favoriteMovies";
    public static final String PATH_TRAILERS = "favoriteTrailers";
    public static final String PATH_REVIEWS = "favoriteReviews";


    public static final class MoviesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();
        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_MOVIE_ID = "idMovie";
        public static final String COLUMN_MOVIE_TITLE = "title";
        public static final String COLUMN_MOVIE_OVERVIEW = "overview";
        public static final String COLUMN_MOVIE_RATING = "rating";
        public static final String COLUMN_MOVIE_POSTER_PATH = "posterPath";
        public static final String COLUMN_MOVIE_BACKDROP_PATH = "backdropPath";
        public static final String COLUMN_MOVIE_RELEASE_DATE = "releaseDate";

    }

    public static class TrailersEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();
        public static final String TABLE_NAME = "trailers";
        public static final String COLUMN_MOVIE_ID = "movie";
        public static final String COLUMN_TRAILER_KEY = "trailerKey";
    }

    public static class ReviewsEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();
        public static final String TABLE_NAME = "reviews";
        public static final String COLUMN_MOVIE_ID = "movie";
        public static final String COLUMN_REVIEW = "review";
        public static final String COLUMN_AUTHOR = "author";
    }
}
