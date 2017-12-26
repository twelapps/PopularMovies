package com.example.android.popularmovies.Utils;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by twelh on 23/12/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public final class MovieJsonUtils {

    /**
     * This method parses JSON from a web response and returns an array of Movies.
     * <p/>
     *
     * The expected format of the (Json format) web response is:
     *
     * {
     *    "page": "1" (only page 1 will be downloaded)
     *    "total_results": "<some number>"
     *    "total_pages": "<some number>"
     *    "results":
     *    [
     *       {
     *          "id": "<some number>" (needed for subsequent calls)
     *          "vote_average": "<some number>"
     *          "original_title": "<some string>"
     *          "poster_path": "<some string>"
     *          "overview": "<some string>"
     *          "release_date": "<some string>"
     *          .... and a lot of other key values that we will not use
     *       }
     *       {
     *          "id": "<some number>" (needed for subsequent calls)
     *          "vote_average": "<some number>"
     *          "original_title": "<some string>"
     *          "poster_path": "<some string>"
     *          "overview": "<some string>"
     *          "release_date": "<some string>"
     *          .... and a lot of other key values that we will not use
     *       }
     *    ]
     * }
     *
     * @param movieJsonStr JSON response from server
     *
     * @return Array of Movies
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ArrayList<Movie> getMovieListFromJson(String movieJsonStr)
            throws JSONException {

        /* Holds a Json array of movies. */
        final String MOVIE_RESULTS = "results";

        /* Unique id of a movie. */
        final String MOVIE_ID = "id";

        /* Other movie parameters that are needed in the app. */
        final String MOVIE_VOTE_AVERAGE = "vote_average";
        final String MOVIE_ORIGINAL_TITLE = "original_title";
        final String MOVIE_POSTER_PATH = "poster_path";
        final String MOVIE_OVERVIEW = "overview";
        final String MOVIE_RELEASE_DATE = "release_date";

        /* Movie array to hold all movies. */
        ArrayList<Movie> parsedMovieData = new ArrayList<>();

        // Build a JSONObject out of the input JSONString.
        JSONObject moviesJsonObj = new JSONObject(movieJsonStr);

        // Get the movies part of the server response and copy it into a local variable.
        JSONArray movieJsonArray = moviesJsonObj.getJSONArray(MOVIE_RESULTS);

        // Walk over the movies Json array and copy all data into a local movies array.
        for (int i = 0; i < movieJsonArray.length(); i++) {
            JSONObject movieObj = movieJsonArray.getJSONObject(i);

            //Create a new movie instance
            Movie movie = new Movie(
            );

            //Copy parameters from the movie Json object into the just created movie instance.
            if (movieObj.has(MOVIE_ID)) {
                movie.setId(movieObj.getInt(MOVIE_ID));
            }
            if (movieObj.has(MOVIE_VOTE_AVERAGE)) {
                movie.setVoteAverage(movieObj.getDouble(MOVIE_VOTE_AVERAGE));
            }
            if (movieObj.has(MOVIE_ORIGINAL_TITLE)) {
                movie.setTitle(movieObj.getString(MOVIE_ORIGINAL_TITLE));
            }
            if (movieObj.has(MOVIE_POSTER_PATH)) {
                movie.setPosterViewPath(movieObj.getString(MOVIE_POSTER_PATH));
            }
            if (movieObj.has(MOVIE_OVERVIEW)) {
                movie.setOverview(movieObj.getString(MOVIE_OVERVIEW));
            }
            if (movieObj.has(MOVIE_RELEASE_DATE)) { //Get the year only.
                String dtStart = movieObj.getString(MOVIE_RELEASE_DATE);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = format.parse(dtStart);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    movie.setReleaseDate(String.valueOf(cal.get(Calendar.YEAR)));
                } catch (ParseException e) {
                    movie.setReleaseDate(""); //We should not throw an error to the end user if just the relase date could not be parsed!
                }
            }
            //Copy local movie instance to the local movie array but only if the id has been set.
            if (movie.getId() != Constants.NO_MOVIE_ID) parsedMovieData.add(movie);

        }

        return parsedMovieData;
    }
}
