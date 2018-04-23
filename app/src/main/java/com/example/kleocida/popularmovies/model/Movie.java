package com.example.kleocida.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Movie implements Parcelable {
    private int idMovie;
    private String originalTitle;
    private String posterPath;
    private String backdropPath;
    private String movieOverview;
    private double usersRating;
    private String releaseDate;
    private List<String> trailerKey = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();

    public Movie() {

    }

    public Movie(int idMovie, String originalTitle, String posterPath, String backdropPath, String movieOverview,
                 double usersRating, String releaseDate, List<String> trailerKey, List<Review> reviews) {
        this.idMovie = idMovie;
        this.originalTitle = originalTitle;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.movieOverview = movieOverview;
        this.usersRating = usersRating;
        this.releaseDate = releaseDate;
        this.trailerKey = trailerKey;
        this.reviews = reviews;
    }

    protected Movie(Parcel in) {
        idMovie = in.readInt();
        originalTitle = in.readString();
        posterPath = in.readString();
        backdropPath = in.readString();
        movieOverview = in.readString();
        usersRating = in.readDouble();
        releaseDate = in.readString();
        in.readList(trailerKey, null);
        in.readTypedList(reviews, Review.CREATOR);
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public int getIdMovie() {
        return idMovie;
    }

    public void setIdMovie(int idMovie) {
        this.idMovie = idMovie;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getMovieOverview() {
        return movieOverview;
    }

    public void setMovieOverview(String movieOverview) {
        this.movieOverview = movieOverview;
    }

    public double getUsersRating() {
        return usersRating;
    }

    public void setUsersRating(double usersRating) {
        this.usersRating = usersRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<String> getTrailerKey() {
        return trailerKey;
    }

    public void setTrailerKey(List<String> trailerKey) {
        this.trailerKey = trailerKey;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(idMovie);
        parcel.writeString(originalTitle);
        parcel.writeString(posterPath);
        parcel.writeString(backdropPath);
        parcel.writeString(movieOverview);
        parcel.writeDouble(usersRating);
        parcel.writeString(releaseDate);
        parcel.writeList(trailerKey);
        parcel.writeTypedList(reviews);
    }
}