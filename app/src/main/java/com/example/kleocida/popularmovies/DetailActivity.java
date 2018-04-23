package com.example.kleocida.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.example.kleocida.popularmovies.data.FavoritesContract.*;
import com.example.kleocida.popularmovies.data.FavoritesDbHelper;
import com.example.kleocida.popularmovies.databinding.ActivityDetailBinding;
import com.example.kleocida.popularmovies.model.Movie;
import com.example.kleocida.popularmovies.model.Review;
import com.example.kleocida.popularmovies.utility.JsonUtility;
import com.example.kleocida.popularmovies.utility.NetworkUtility;
import com.example.kleocida.popularmovies.adapter.ReviewAdapter;
import com.example.kleocida.popularmovies.adapter.TrailerAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks,
        ReviewAdapter.ItemClickListener, TrailerAdapter.ItemClickListener {

    private static final String BACKDROP_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String BACKDROP_WIDTH_URL = "w500";
    private static final int TRAILER_LOADER = 1565;
    private static final int REVIEWS_LOADER = 1352;
    private static final int CURSOR_LOADER = 1265;
    private static final int CURSOR_TRAILER_LOADER = 1854;
    private static final int CURSOR_REVIEW_LOADER = 1584;
    public static final int UPDATED_OBJECT = 158;
    private static final int DEFAULT_POSITION_VALUE = -1;

    private String movieDbApiKey;
    private int idMovie = -1;
    private int position;
    private int numberOfLoaderFinished = 0;
    private boolean movieFavorite = false;

    private ActivityDetailBinding mBinding;

    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;

    private Movie movie = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        int requestCode;
        SQLiteDatabase sqLiteDatabase;

        if (savedInstanceState != null) {
            numberOfLoaderFinished = savedInstanceState.getInt("number");
            movie = savedInstanceState.getParcelable("Movie");
            position = savedInstanceState.getInt("MoviePosition");
        }

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        LinearLayoutManager layoutManagerTrailers = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvTrailers.setLayoutManager(layoutManagerTrailers);

        LinearLayoutManager layoutManagerReviews = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvReviews.setLayoutManager(layoutManagerReviews);

        mReviewAdapter = new ReviewAdapter(this);
        mBinding.rvReviews.setAdapter(mReviewAdapter);

        mTrailerAdapter = new TrailerAdapter(this);
        mBinding.rvTrailers.setAdapter(mTrailerAdapter);

        movieDbApiKey = BuildConfig.THE_MOVIE_DB_API_KEY;


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }

        movie = intent.getParcelableExtra("Movie");
        position = intent.getIntExtra("MoviePosition", DEFAULT_POSITION_VALUE);
        movieFavorite = intent.getBooleanExtra("isFavorite", false);

        if (movie == null || position == -1) {
            finish();
        }

        getSupportActionBar().setTitle(movie.getOriginalTitle());


        if (movie.getTrailerKey().isEmpty()) {
            idMovie = movie.getIdMovie();
            requestCode = NetworkUtility.REQUEST_TRAILERS;
            updateTrailers(requestCode);
        } else {
            mTrailerAdapter.setTrailers(movie.getTrailerKey());
        }


        if (movie.getReviews().isEmpty()) {
            idMovie = movie.getIdMovie();
            requestCode = NetworkUtility.REQUEST_REVIEWS;
            updateReviews(requestCode);
        } else {
            mReviewAdapter.setReviews(movie.getReviews());
            mReviewAdapter.setTitle(movie.getOriginalTitle());
        }

        populateUI(movie);

        FavoritesDbHelper dbHelper = new FavoritesDbHelper(this);
        sqLiteDatabase = dbHelper.getWritableDatabase();


        if (!movieFavorite) {

            try {
                if (getSupportLoaderManager().getLoader(CURSOR_LOADER).isStarted())
                    getSupportLoaderManager().restartLoader(CURSOR_LOADER, null, this);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                getSupportLoaderManager().initLoader(CURSOR_LOADER, null, this);
            }
        } else {
            mBinding.fabFavorites.setImageResource(R.drawable.ic_favorite_black_24dp);
        }

        mBinding.fabFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (movieFavorite) {
                    mBinding.fabFavorites.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    movieFavorite = false;
                    deleteMovieFromFavorites();
                } else {
                    mBinding.fabFavorites.setImageResource(R.drawable.ic_favorite_black_24dp);
                    movieFavorite = true;
                    addMovieToFavorites();
                    addTrailers();
                    addReview();
                }
            }
        });

    }

    private void deleteMovieFromFavorites() {
        Uri MovieUri = MoviesEntry.CONTENT_URI;
        Uri TrailerUri = TrailersEntry.CONTENT_URI;
        Uri ReviewUri = ReviewsEntry.CONTENT_URI;
        MovieUri = MovieUri.buildUpon().appendPath(Integer.toString(movie.getIdMovie())).build();
        TrailerUri = TrailerUri.buildUpon().appendPath(Integer.toString(movie.getIdMovie())).build();
        ReviewUri = ReviewUri.buildUpon().appendPath(Integer.toString(movie.getIdMovie())).build();

        getContentResolver().delete(MovieUri, null, null);
        getContentResolver().delete(TrailerUri, null, null);
        getContentResolver().delete(ReviewUri, null, null);
    }

    private void addReview() {
        ContentValues[] bulkToInsert;
        List<ContentValues> contentValues = new ArrayList<>();

        for (int i = 0; i < movie.getReviews().size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put(ReviewsEntry.COLUMN_MOVIE_ID, movie.getIdMovie());
            cv.put(ReviewsEntry.COLUMN_REVIEW, movie.getReviews().get(i).getComment());
            cv.put(ReviewsEntry.COLUMN_AUTHOR, movie.getReviews().get(i).getAuthor());

            contentValues.add(cv);
        }
        bulkToInsert = new ContentValues[contentValues.size()];
        contentValues.toArray(bulkToInsert);

        getContentResolver().bulkInsert(ReviewsEntry.CONTENT_URI, bulkToInsert);
    }

    private void addTrailers() {
        ContentValues[] bulkToInsert;
        List<ContentValues> contentValues = new ArrayList<>();


        for (int i = 0; i < movie.getTrailerKey().size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put(TrailersEntry.COLUMN_MOVIE_ID, movie.getIdMovie());
            cv.put(TrailersEntry.COLUMN_TRAILER_KEY, movie.getTrailerKey().get(i));
            contentValues.add(cv);
        }
        bulkToInsert = new ContentValues[contentValues.size()];
        contentValues.toArray(bulkToInsert);
        getContentResolver().bulkInsert(TrailersEntry.CONTENT_URI, bulkToInsert);
    }

    private void addMovieToFavorites() {
        ContentValues cv = new ContentValues();
        cv.put(MoviesEntry.COLUMN_MOVIE_ID, movie.getIdMovie());
        cv.put(MoviesEntry.COLUMN_MOVIE_TITLE, movie.getOriginalTitle());
        cv.put(MoviesEntry.COLUMN_MOVIE_POSTER_PATH, movie.getPosterPath());
        cv.put(MoviesEntry.COLUMN_MOVIE_OVERVIEW, movie.getMovieOverview());
        cv.put(MoviesEntry.COLUMN_MOVIE_RATING, movie.getUsersRating());
        cv.put(MoviesEntry.COLUMN_MOVIE_RELEASE_DATE, movie.getReleaseDate());
        cv.put(MoviesEntry.COLUMN_MOVIE_BACKDROP_PATH, movie.getBackdropPath());

        getContentResolver().insert(MoviesEntry.CONTENT_URI, cv);
    }

    private void updateReviews(int request) {
        Bundle bundle = new Bundle();
        bundle.putInt("requestCode", request);

        if (movieFavorite) {
            try {
                if (getSupportLoaderManager().getLoader(CURSOR_REVIEW_LOADER).isStarted())
                    getSupportLoaderManager().restartLoader(CURSOR_REVIEW_LOADER, bundle, this);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                getSupportLoaderManager().initLoader(CURSOR_REVIEW_LOADER, bundle, this);
            }
        } else {
            boolean isConnected = NetworkUtility.checkInternetConnection(this);

            if (isConnected) {
                try {
                    if (getSupportLoaderManager().getLoader(REVIEWS_LOADER).isStarted())
                        getSupportLoaderManager().restartLoader(REVIEWS_LOADER, bundle, this);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    getSupportLoaderManager().initLoader(REVIEWS_LOADER, bundle, this);
                }
            }
        }

    }

    private void updateTrailers(int request) {
        Bundle bundle = new Bundle();
        bundle.putInt("requestCode", request);

        if (movieFavorite) {
            try {
                if (getSupportLoaderManager().getLoader(CURSOR_TRAILER_LOADER).isStarted())
                    getSupportLoaderManager().restartLoader(CURSOR_TRAILER_LOADER, bundle, this);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                getSupportLoaderManager().initLoader(CURSOR_TRAILER_LOADER, bundle, this);
            }
        } else {
            boolean isConnected = NetworkUtility.checkInternetConnection(this);

            if (isConnected) {
                try {
                    if (getSupportLoaderManager().getLoader(TRAILER_LOADER).isStarted())
                        getSupportLoaderManager().restartLoader(TRAILER_LOADER, bundle, this);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    getSupportLoaderManager().initLoader(TRAILER_LOADER, bundle, this);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateUI(Movie movie) {
        String backdropPath = movie.getBackdropPath();

        String backdropUrl = BACKDROP_BASE_URL + BACKDROP_WIDTH_URL + backdropPath;

        Picasso.with(this)
                .load(backdropUrl)
                .into(mBinding.ivBackdrop);

        mBinding.titleTv.setText(movie.getOriginalTitle());
        mBinding.overviewTv.setText(movie.getMovieOverview());
        mBinding.ratingRb.setRating((float) (movie.getUsersRating() / 2));
        mBinding.releaseDateTv.setText(String.valueOf(movie.getReleaseDate()));

    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable final Bundle args) {
        if (id == CURSOR_LOADER || id == CURSOR_TRAILER_LOADER || id == CURSOR_REVIEW_LOADER) {
            return new AsyncTaskLoader<Cursor>(this) {
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    forceLoad();
                }

                @Nullable
                @Override
                public Cursor loadInBackground() {

                    int request;
                    Uri uri;
                    String[] projection;
                    if (args != null) {
                        request = args.getInt("requestCode");
                        if (request == NetworkUtility.REQUEST_TRAILERS) {
                            uri = TrailersEntry.CONTENT_URI;
                            projection = new String[]{TrailersEntry.COLUMN_TRAILER_KEY};
                        } else {
                            uri = ReviewsEntry.CONTENT_URI;
                            projection = new String[]{ReviewsEntry.COLUMN_REVIEW, ReviewsEntry.COLUMN_AUTHOR};
                        }
                    } else {
                        uri = MoviesEntry.CONTENT_URI;
                        projection = new String[]{MoviesEntry._ID};
                    }
                    uri = uri.buildUpon().appendPath(Integer.toString(movie.getIdMovie())).build();

                    try {
                        return getContentResolver().query(uri,
                                projection,
                                null,
                                null,
                                null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
        } else {
            return new AsyncTaskLoader<String>(this) {
                @Nullable
                @Override
                public String loadInBackground() {
                    if (movieDbApiKey == null || TextUtils.isEmpty(movieDbApiKey))
                        return null;
                    try {
                        int request = args.getInt("requestCode");
                        return NetworkUtility.getContentFromHttp(NetworkUtility.buildUrl(movieDbApiKey, request, idMovie));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    forceLoad();
                }
            };
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {

        if (loader.getId() == CURSOR_LOADER) {
            Cursor cursor = (Cursor) data;
            int total;
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getCount();
                cursor.close();
            } else {
                total = 0;
            }

            switch (total) {
                case 0:
                    movieFavorite = false;
                    break;
                default:
                    movieFavorite = true;
                    mBinding.fabFavorites.setImageResource(R.drawable.ic_favorite_black_24dp);
                    break;
            }

            getSupportLoaderManager().destroyLoader(CURSOR_LOADER);

        } else if (loader.getId() == CURSOR_TRAILER_LOADER) {
            Cursor cursor = (Cursor) data;
            List<String> trailersAsList = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    trailersAsList.add(cursor.getString(cursor.getColumnIndex(TrailersEntry.COLUMN_TRAILER_KEY)));
                    cursor.moveToNext();
                }
                mTrailerAdapter.setTrailers(trailersAsList);

            }
            getSupportLoaderManager().destroyLoader(CURSOR_TRAILER_LOADER);
        } else if (loader.getId() == CURSOR_REVIEW_LOADER) {
            Cursor cursor = (Cursor) data;
            List<Review> reviewAsList = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    Review review = new Review();
                    review.setComment(cursor.getString(cursor.getColumnIndex(ReviewsEntry.COLUMN_REVIEW)));
                    review.setAuthor(cursor.getString(cursor.getColumnIndex(ReviewsEntry.COLUMN_AUTHOR)));
                    reviewAsList.add(review);
                    cursor.moveToNext();
                }
                mReviewAdapter.setTitle(movie.getOriginalTitle());
                mReviewAdapter.setReviews(reviewAsList);
            }
            getSupportLoaderManager().destroyLoader(CURSOR_REVIEW_LOADER);
        } else if (loader.getId() == TRAILER_LOADER) {
            numberOfLoaderFinished++;
            List<String> trailersAsList = new ArrayList<>();
            try {
                trailersAsList = JsonUtility.parseTrailerJson(data.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (trailersAsList == null || trailersAsList.isEmpty()) {
                return;
            }

            mTrailerAdapter.setTrailers(trailersAsList);

            movie.setTrailerKey(trailersAsList);

            getSupportLoaderManager().destroyLoader(TRAILER_LOADER);

        } else if (loader.getId() == REVIEWS_LOADER) {
            numberOfLoaderFinished++;
            List<Review> reviewAsList = new ArrayList<>();
            try {
                reviewAsList = JsonUtility.parseReviewJson(data.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (reviewAsList == null || reviewAsList.isEmpty()) {
                return;
            }

            mReviewAdapter.setReviews(reviewAsList);
            mReviewAdapter.setTitle(movie.getOriginalTitle());
            movie.setReviews(reviewAsList);
            getSupportLoaderManager().destroyLoader(REVIEWS_LOADER);
        }


        if (numberOfLoaderFinished == 2) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("Movie", movie);
            bundle.putInt("MoviePosition", position);
            Intent intent = new Intent();
            intent.putExtras(bundle);
            setResult(UPDATED_OBJECT, intent);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("number", numberOfLoaderFinished);
        outState.putParcelable("Movie", movie);
        outState.putInt("MoviePosition", position);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

    }

    @Override
    public void onTrailerClick(int clickItemPosition) {

    }

    @Override
    public void onReviewClick(int clickItemPosition) {

    }
}
