package com.example.kleocida.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.kleocida.popularmovies.data.FavoritesContract.*;

public class FavoritesDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favoriteMovies.db";
    private static final int DATABASE_VERSION = 2;

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + " ( " +
                MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_OVERVIEW + " TEXT, " +
                MoviesEntry.COLUMN_MOVIE_POSTER_PATH + " TEXT, " +
                MoviesEntry.COLUMN_MOVIE_BACKDROP_PATH + " TEXT, " +
                MoviesEntry.COLUMN_MOVIE_RATING + " REAL, " +
                MoviesEntry.COLUMN_MOVIE_RELEASE_DATE + " DATETIME " +
                "); ";

        final String CREATE_TRAILERS_TABLE = "CREATE TABLE " + TrailersEntry.TABLE_NAME + " (" +
                TrailersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TrailersEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                TrailersEntry.COLUMN_TRAILER_KEY + " TEXT NOT NULL " +
                ");";

        final String CREATE_REVIEWS_TABLE = "CREATE TABLE " + ReviewsEntry.TABLE_NAME + " (" +
                ReviewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ReviewsEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                ReviewsEntry.COLUMN_REVIEW + " TEXT NOT NULL, " +
                ReviewsEntry.COLUMN_AUTHOR + " TEXT NOT NULL " +
                "); ";

        sqLiteDatabase.execSQL(CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(CREATE_TRAILERS_TABLE);
        sqLiteDatabase.execSQL(CREATE_REVIEWS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.delete(MoviesEntry.TABLE_NAME, null, null);
        sqLiteDatabase.delete(ReviewsEntry.TABLE_NAME, null, null);
        sqLiteDatabase.delete(TrailersEntry.TABLE_NAME, null, null);
        onCreate(sqLiteDatabase);
    }
}