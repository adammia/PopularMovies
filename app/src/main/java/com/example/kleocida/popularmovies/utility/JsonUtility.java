package com.example.kleocida.popularmovies.utility;

import com.example.kleocida.popularmovies.model.Movie;
import com.example.kleocida.popularmovies.model.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonUtility {

    public static List<Review> parseReviewJson (String json) throws JSONException {
        final String REVIEW_COMMENT = "content";
        final String REVIEW_AUTHOR = "author";
        final String REVIEW_RESULT = "results";

        JSONObject reviewJson = new JSONObject(json);
        if(reviewJson.has(REVIEW_RESULT)){
            JSONArray resultsAsArray = reviewJson.optJSONArray(REVIEW_RESULT);
            List<Review> reviewAsList = new ArrayList<>();

            for(int i=0; i<resultsAsArray.length(); i++){
                JSONObject resultObj = new JSONObject(resultsAsArray.getString(i));
                Review review = new Review();
                review.setAuthor(resultObj.optString(REVIEW_AUTHOR));
                review.setComment(resultObj.optString(REVIEW_COMMENT));

                reviewAsList.add(review);
            }
            return reviewAsList;
        }
        return null;
    }

    public static List<String> parseTrailerJson (String json) throws JSONException {
        final String TRAILER_KEY = "key";
        final String TRAILER_RESULT = "results";

        JSONObject trailerJson = new JSONObject(json);
        if(trailerJson.has(TRAILER_RESULT)){
            JSONArray resultsAsArray = trailerJson.optJSONArray(TRAILER_RESULT);

            List<String> trailersAsList = new ArrayList<>();

            for(int i = 0; i < resultsAsArray.length(); i++){
                JSONObject resultObj = new JSONObject(resultsAsArray.getString(i));
                trailersAsList.add(resultObj.optString(TRAILER_KEY));
            }
            return trailersAsList;
        }
        return null;
    }

    public static List<Movie> parseMovieJson (String json) throws JSONException{
        final String MOVIE_ID = "id";
        final String MOVIE_TITLE = "original_title";
        final String MOVIE_POSTER_PATH = "poster_path";
        final String MOVIE_BACKDROP_PATH = "backdrop_path";
        final String MOVIE_OVERVIEW = "overview";
        final String MOVIE_RATING = "vote_average";
        final String MOVIE_RELEASE_DATE = "release_date";
        final String MOVIE_RESULTS = "results";

        JSONObject movieJson = new JSONObject(json);
        if(movieJson.has(MOVIE_RESULTS)){
            JSONArray resultsAsArray = movieJson.optJSONArray(MOVIE_RESULTS);

            List<Movie> movieAsList = new ArrayList<>();

            for(int i = 0; i < resultsAsArray.length(); i++){
                JSONObject resultsObj = new JSONObject(resultsAsArray.getString(i));
                Movie movie = new Movie();
                movie.setIdMovie(resultsObj.optInt(MOVIE_ID));
                movie.setOriginalTitle(resultsObj.optString(MOVIE_TITLE));
                movie.setPosterPath(resultsObj.optString(MOVIE_POSTER_PATH));
                movie.setBackdropPath(resultsObj.optString(MOVIE_BACKDROP_PATH));
                movie.setMovieOverview(resultsObj.optString(MOVIE_OVERVIEW));
                movie.setUsersRating(resultsObj.optDouble(MOVIE_RATING));
                movie.setReleaseDate(resultsObj.optString(MOVIE_RELEASE_DATE));

                movieAsList.add(movie);
            }

            return movieAsList;
        } else {
            return null;
        }
    }
}

