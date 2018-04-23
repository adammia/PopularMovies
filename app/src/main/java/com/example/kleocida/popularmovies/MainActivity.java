package com.example.kleocida.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.kleocida.popularmovies.data.FavoritesContract;
import com.example.kleocida.popularmovies.data.FavoritesContract.*;
import com.example.kleocida.popularmovies.databinding.ActivityMainBinding;
import com.example.kleocida.popularmovies.model.Movie;
import com.example.kleocida.popularmovies.utility.JsonUtility;
import com.example.kleocida.popularmovies.utility.NetworkUtility;
import com.example.kleocida.popularmovies.adapter.PosterAdapter;


import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks
        , PosterAdapter.ItemClickListener {

    private String movieDbApiKey;
    private static final int MOVIE_LOADER = 1802;
    private static final int CURSOR_LOADER = 1803;
    public static final int DETAILS_INTENT_REQUEST = 59;
    private int requestCode = NetworkUtility.REQUEST_MOST_POPULAR;
    private int idMovie = -1;
    private Bundle lastSavedInstance = null;


    private ActivityMainBinding mBinding;

    private PosterAdapter mPosterAdapter;

    private GridLayoutManager layoutManager = new GridLayoutManager(this, 2);

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("requestCode", requestCode);
        outState.putParcelable("scrollPosition", layoutManager.onSaveInstanceState());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            requestCode = savedInstanceState.getInt("requestCode");
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable("scrollPosition"));
            lastSavedInstance = savedInstanceState;
        }

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mBinding.ivPosters.setLayoutManager(layoutManager);
        mBinding.ivPosters.setHasFixedSize(true);

        mPosterAdapter = new PosterAdapter(this);
        mBinding.ivPosters.setAdapter(mPosterAdapter);

        configureBottomNav();

        movieDbApiKey = BuildConfig.THE_MOVIE_DB_API_KEY;

        mBinding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateMovieList(requestCode);
            }
        });

        updateMovieList(requestCode);
    }

    private void configureBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_bar);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_favorites:
                        requestCode = NetworkUtility.REQUEST_FAVORITES;
                        break;
                    case R.id.action_top_rated:
                        requestCode = NetworkUtility.REQUEST_TOP_RATED;
                        break;
                    case R.id.action_popular:
                        requestCode = NetworkUtility.REQUEST_MOST_POPULAR;
                    default:
                        break;
                }
                updateMovieList(requestCode);
                return true;
            }
        });
    }

    private void updateMovieList(int request) {
        if (request == NetworkUtility.REQUEST_FAVORITES) {
            try {
                if (getSupportLoaderManager().getLoader(CURSOR_LOADER).isStarted())
                    getSupportLoaderManager().restartLoader(CURSOR_LOADER, null, this);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                getSupportLoaderManager().initLoader(CURSOR_LOADER, null, this);
            }
            mBinding.swipeRefresh.setRefreshing(false);
        } else {
            boolean isConnected;
            isConnected = NetworkUtility.checkInternetConnection(this);

            if (isConnected) {
                try {
                    if (getSupportLoaderManager().getLoader(MOVIE_LOADER).isStarted())
                        getSupportLoaderManager().restartLoader(MOVIE_LOADER, null, this);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    getSupportLoaderManager().initLoader(MOVIE_LOADER, null, this);
                }
                mBinding.swipeRefresh.setRefreshing(false);
            } else {
                Toast.makeText(this, "No internet connection, Swipe to refresh", Toast.LENGTH_LONG).show();
                mBinding.swipeRefresh.setRefreshing(false);
            }
        }
    }

    @Override
    public Loader onCreateLoader(int i, final Bundle bundle) {
        if (i == CURSOR_LOADER) {
            return new AsyncTaskLoader<Cursor>(this) {
                @Nullable
                @Override
                public Cursor loadInBackground() {
                    try {
                        return getContentResolver().query(MoviesEntry.CONTENT_URI,
                                new String[]{MoviesEntry.COLUMN_MOVIE_ID,
                                        MoviesEntry.COLUMN_MOVIE_TITLE,
                                        MoviesEntry.COLUMN_MOVIE_OVERVIEW,
                                        MoviesEntry.COLUMN_MOVIE_RATING,
                                        MoviesEntry.COLUMN_MOVIE_RELEASE_DATE,
                                        MoviesEntry.COLUMN_MOVIE_POSTER_PATH,
                                        MoviesEntry.COLUMN_MOVIE_BACKDROP_PATH},
                                null,
                                null,
                                MoviesEntry.COLUMN_MOVIE_RATING);
                    } catch (Exception e) {
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
        } else {
            return new AsyncTaskLoader<String>(this) {
                @Override
                public String loadInBackground() {
                    if (movieDbApiKey == null || TextUtils.isEmpty(movieDbApiKey))
                        return null;
                    try {
                        return NetworkUtility.getContentFromHttp(NetworkUtility.buildUrl(movieDbApiKey, requestCode, idMovie));
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
            List<Movie> favoriteMovies = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    Movie movie = new Movie();
                    movie.setIdMovie(cursor.getInt(cursor.getColumnIndex(MoviesEntry.COLUMN_MOVIE_ID)));
                    movie.setOriginalTitle(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_MOVIE_TITLE)));
                    movie.setMovieOverview(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_MOVIE_OVERVIEW)));
                    movie.setUsersRating(cursor.getDouble(cursor.getColumnIndex(MoviesEntry.COLUMN_MOVIE_RATING)));
                    movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_MOVIE_RELEASE_DATE)));
                    movie.setPosterPath(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_MOVIE_POSTER_PATH)));
                    movie.setBackdropPath(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_MOVIE_BACKDROP_PATH)));
                    favoriteMovies.add(movie);

                    cursor.moveToNext();
                }
            } else {
                Toast.makeText(this, "You haven't favorite movies", Toast.LENGTH_SHORT).show();
            }

            mPosterAdapter.setPoster(favoriteMovies);
            mPosterAdapter.setIsFavorite(true);
            getSupportLoaderManager().destroyLoader(CURSOR_LOADER);

        } else {
            String httpResult = data.toString();
            List<Movie> moviesAsList = new ArrayList<>();
            try {
                moviesAsList = JsonUtility.parseMovieJson(httpResult);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (moviesAsList == null || moviesAsList.isEmpty()) {
                closeOnError();
                getSupportLoaderManager().destroyLoader(MOVIE_LOADER);
                return;
            }

            mPosterAdapter.setPoster(moviesAsList);
            mPosterAdapter.setIsFavorite(false);
            getSupportLoaderManager().destroyLoader(MOVIE_LOADER);
        }

        if (lastSavedInstance != null) {
            layoutManager.onRestoreInstanceState(lastSavedInstance.getParcelable("scrollPosition"));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int clickItemPosition) {

    }

    @Override
    protected void onActivityResult(int requestIntentCode, int resultCode, Intent data) {
        if (requestIntentCode == DETAILS_INTENT_REQUEST) {
            if (resultCode == DetailActivity.UPDATED_OBJECT) {
                Bundle bundle = data.getExtras();
                Movie newMovie = bundle.getParcelable("Movie");
                int position = bundle.getInt("MoviePosition");
                mPosterAdapter.updateMovieTrailer(position, newMovie);
            }
        }

    }
}