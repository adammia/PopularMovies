package com.example.kleocida.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.kleocida.popularmovies.data.FavoritesContract.*;

public class FavoritesContentProvider extends ContentProvider {

    private FavoritesDbHelper dbHelper;
    private static final int CODE_MOVIES = 100;
    private static final int CODE_MOVIES_WITH_ID = 101;
    private static final int CODE_TRAILERS = 200;
    private static final int CODE_TRAILERS_ID_MOVIE = 201;
    private static final int CODE_REVIEWS = 300;
    private static final int CODE_REVIEWS_ID_MOVIE = 301;


    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoritesContract.AUTHORITY;

        matcher.addURI(authority, FavoritesContract.PATH_MOVIES, CODE_MOVIES);
        matcher.addURI(authority, FavoritesContract.PATH_MOVIES + "/#", CODE_MOVIES_WITH_ID);
        matcher.addURI(authority, FavoritesContract.PATH_TRAILERS + "/#", CODE_TRAILERS_ID_MOVIE);
        matcher.addURI(authority, FavoritesContract.PATH_REVIEWS + "/#", CODE_REVIEWS_ID_MOVIE);
        matcher.addURI(authority, FavoritesContract.PATH_TRAILERS, CODE_TRAILERS);
        matcher.addURI(authority, FavoritesContract.PATH_REVIEWS, CODE_REVIEWS);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new FavoritesDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                cursor = dbHelper.getReadableDatabase().query(
                        MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_MOVIES_WITH_ID:
                String select = MoviesEntry.COLUMN_MOVIE_ID + " = ?";
                String[] args = new String[]{uri.getLastPathSegment()};

                cursor = dbHelper.getReadableDatabase().query(
                        MoviesEntry.TABLE_NAME,
                        projection,
                        select,
                        args,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_TRAILERS_ID_MOVIE:
                String trailerSelect = TrailersEntry.COLUMN_MOVIE_ID + " = ?";
                String[] trailerArgs = new String[]{uri.getLastPathSegment()};

                cursor = dbHelper.getReadableDatabase().query(
                        TrailersEntry.TABLE_NAME,
                        projection,
                        trailerSelect,
                        trailerArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_REVIEWS_ID_MOVIE:
                String reviewSelect = ReviewsEntry.COLUMN_MOVIE_ID + " = ?";
                String[] reviewArgs = new String[]{uri.getLastPathSegment()};

                cursor = dbHelper.getReadableDatabase().query(
                        ReviewsEntry.TABLE_NAME,
                        projection,
                        reviewSelect,
                        reviewArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        Uri returnUri;
        long id;

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                id = dbHelper.getWritableDatabase().insert(
                        MoviesEntry.TABLE_NAME,
                        null,
                        contentValues);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(MoviesEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Impossible to insert the row into: " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        int deleted = 0;
        String id = "";
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES_WITH_ID:
                id = uri.getLastPathSegment();
                deleted = dbHelper.getWritableDatabase().delete(
                        MoviesEntry.TABLE_NAME,
                        MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{id});
                break;
            case CODE_TRAILERS_ID_MOVIE:
                id = uri.getLastPathSegment();
                deleted = dbHelper.getWritableDatabase().delete(
                        TrailersEntry.TABLE_NAME,
                        TrailersEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{id});
                break;
            case CODE_REVIEWS_ID_MOVIE:
                id = uri.getLastPathSegment();
                deleted = dbHelper.getWritableDatabase().delete(
                        ReviewsEntry.TABLE_NAME,
                        ReviewsEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (deleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        int rows = 0;

        switch (sUriMatcher.match(uri)) {
            case CODE_TRAILERS:
                sqLiteDatabase.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long id = sqLiteDatabase.insert(
                                TrailersEntry.TABLE_NAME,
                                null,
                                value);
                        if (id != -1) {
                            rows++;
                        }
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                } finally {
                    sqLiteDatabase.endTransaction();
                }
                if (rows > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rows;

            case CODE_REVIEWS:
                sqLiteDatabase.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long id = sqLiteDatabase.insert(
                                ReviewsEntry.TABLE_NAME,
                                null,
                                value);

                        if (id != -1) {
                            rows++;
                        }
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                } finally {
                    sqLiteDatabase.endTransaction();
                }
                if (rows > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rows;

            default:
                return super.bulkInsert(uri, values);
        }
    }
}
