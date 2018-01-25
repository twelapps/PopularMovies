package com.example.android.popularmovies.Utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;

import com.example.android.popularmovies.Data.MovieContract;

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
    public static ArrayList<Movie> getMovieListFromJson(String movieJsonStr, int sortType)
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
        //TODO: change to all pages with movies ??
        for (int i = 0; i < movieJsonArray.length(); i++) {
            JSONObject movieObj = movieJsonArray.getJSONObject(i);

            //Create a new movie instance
            Movie movie = new Movie();

            //Copy parameters from the movie Json object into the just created movie instance.
            if (movieObj.has(MOVIE_ID)) {
                movie.setTmdbId(movieObj.getInt(MOVIE_ID));
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
            movie.setSortType(sortType);

            //Copy local movie instance to the local movie array but only if the id has been set.
            if (movie.getTmdbId() != Movie.TMDB_MOVIE_ID_NONE) {
                parsedMovieData.add(movie);
            }

        }

        return parsedMovieData;
    }

    //Retrieves the movie's runtime from the input JSON string.
    public static int getRuntimeFromJson(String movieJsonStr)
            throws JSONException {

        //Initiate output
        int runtime = 0;

        /* Holds a Json array of movies. */
        final String MOVIE_RUNTIME = "runtime";

        // Build a JSONObject out of the input JSONString.
        JSONObject movieDetailJsonObj = new JSONObject(movieJsonStr);

        //Copy parameters from the movie Json object into the just created movie instance.
        if (movieDetailJsonObj.has(MOVIE_RUNTIME)) {
            runtime = movieDetailJsonObj.getInt(MOVIE_RUNTIME);
        }

        return runtime;
    }

    //Retrieves the movie's trailers from the input JSON string.
    public static ArrayList<String> getMovieTrailerListFromJson(String movieTrailersJsonString)
            throws JSONException {

        final String MOVIE_TRAILER_KEY = "key";

        /* Holds a Json array of movie trailers. */
        final String MOVIE_TRAILERS_RESULTS = "results";

        /* String array to hold all trailer IDs. */
        ArrayList<String> parsedMovieTrailerIds = new ArrayList<>();

        // Build a JSONObject out of the input JSONString.
        JSONObject movieTrailersJsonObj = new JSONObject(movieTrailersJsonString);

        // Get the movie trailers part of the server response and copy it into a local variable.
        JSONArray movieTrailersJsonArray = movieTrailersJsonObj.getJSONArray(MOVIE_TRAILERS_RESULTS);

        // Walk over the movie trailers Json array and copy all data into a local movie trailers array.
        for (int i = 0; i < movieTrailersJsonArray.length(); i++) {
            JSONObject movieTrailerObject = movieTrailersJsonArray.getJSONObject(i);

            //Copy the trailer key to the output array.
            if (movieTrailerObject.has(MOVIE_TRAILER_KEY)) {
                parsedMovieTrailerIds.add(movieTrailerObject.getString(MOVIE_TRAILER_KEY));
            }
        }

        return parsedMovieTrailerIds;
    }


    // Build an array of ContentValues from an input arrayList, as input for ContentProvider DB BulkInsert
    public static ContentValues[] getMovieContentValues (ArrayList<Movie> movieList) {

        ContentValues[] movieContentValues = new ContentValues[movieList.size()];

        for (int i = 0; i < movieList.size(); i++) {
            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieContract.MovieEntry.COLUMN_SORT_TYPE, movieList.get(i).getSortType());
            movieValues.put(MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID, movieList.get(i).getTmdbId());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movieList.get(i).getTitle());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH, movieList.get(i).getPosterViewPath());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, movieList.get(i).getOverview());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE, movieList.get(i).getVoteAverage());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_YEAR, movieList.get(i).getReleaseDate());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RUNTIME, movieList.get(i).getRuntime());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_FAVORIZE_FLAG, movieList.get(i).getFavorizedFlag());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TRAILERS, movieList.get(i).getTrailersString());

            movieContentValues[i] = movieValues;
        }

        return movieContentValues;
    }

    // Build ContentValues from a single movie
    public static ContentValues getSingleMovieContentValues (Movie movie) {

        ContentValues movieContentValues = new ContentValues();

        movieContentValues.put(MovieContract.MovieEntry.COLUMN_SORT_TYPE, movie.getSortType());
        movieContentValues.put(MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID, movie.getTmdbId());
        movieContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
        movieContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH, movie.getPosterViewPath());
        movieContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, movie.getOverview());
        movieContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE, movie.getVoteAverage());
        movieContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_YEAR, movie.getReleaseDate());
        movieContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RUNTIME, movie.getRuntime());
        movieContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_FAVORIZE_FLAG, movie.getFavorizedFlag());
        movieContentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TRAILERS, movie.getTrailersString());

        return movieContentValues;
    }

    //Build a Movies array from input cursor (corresponding to a table cntining Movie data).
    public static ArrayList<Movie> getMovieListFromTable(Cursor cursor, Boolean query) {
        ArrayList<Movie> movieList = new ArrayList<>();

        //Cursor starts BEFORE the first entry, so the first moveToNext will end up at the first row.
        while (cursor.moveToNext()) {
            Movie movie = new Movie();

            movie.setSortType(cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_SORT_TYPE)));
            if (query) {
                //The column name Tmdb_id is ambiguous for the query we use. Therefore we hard-code its index
                //otherwise we run into an exception.
                movie.setTmdbId(cursor.getInt(1));
            } else {
                movie.setTmdbId(cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID)));
            }
            movie.setTitle(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE)));
            movie.setPosterViewPath(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH)));
            movie.setOverview(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW)));
            movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_YEAR)));
            movie.setRuntime(cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RUNTIME)));
            movie.setVoteAverage(cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE)));
            movie.setFavorizedFlag(cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_FAVORIZE_FLAG)));
            movie.setTrailersStringFromString(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TRAILERS)));
            movieList.add(movie);
        }

        return movieList;
    }

    //Build a single Movie input cursor that has been moved to a certain position.
    public static Movie getMovieFromCursorAtPosition (Cursor cursor) {
        Movie movie = new Movie();

        movie.setSortType(cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_SORT_TYPE)));
        movie.setTmdbId(cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID)));
        movie.setTitle(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE)));
        movie.setPosterViewPath(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH)));
        movie.setOverview(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW)));
        movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_YEAR)));
        movie.setRuntime(cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RUNTIME)));
        movie.setVoteAverage(cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE)));
        movie.setFavorizedFlag(cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_FAVORIZE_FLAG)));
        movie.setTrailersStringFromString(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TRAILERS)));

        return movie;
    }
}
