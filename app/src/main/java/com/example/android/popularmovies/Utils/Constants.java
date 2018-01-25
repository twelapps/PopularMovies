package com.example.android.popularmovies.Utils;

/**
 * Created by twelh on 20/12/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public final class Constants {
    //The Movie DataBase constants
    private static final String MY_TMDB_API_KEY = "YOUR_TMDB_API_KEY_HERE";
    private static final String TMDB_BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String TMDB_URL_TRAILERS_EXTENSION = "/videos";
    private static final String URL_PARM_SEPARATOR = "?";
    private static final String URL_PARM_API_KEY = "api_key=";
    public static final String TMDB_BASE_URL_MOST_POPULAR = TMDB_BASE_URL + "popular" + URL_PARM_SEPARATOR +
            URL_PARM_API_KEY + MY_TMDB_API_KEY;
    public static final String TMDB_BASE_URL_TOP_RATED = TMDB_BASE_URL + "top_rated" + URL_PARM_SEPARATOR +
            URL_PARM_API_KEY + MY_TMDB_API_KEY;
    public static final String TMDB_BASE_URL_IMAGES = "http://image.tmdb.org/t/p/";

    public static String TMDB_RUNTIME_URL (int movieId) {
        return TMDB_BASE_URL + Integer.toString(movieId) + URL_PARM_SEPARATOR +
                URL_PARM_API_KEY + MY_TMDB_API_KEY;
    }
    public static String TMDB_TRAILERS_URL (int movieId) {
        return TMDB_BASE_URL + Integer.toString(movieId) + TMDB_URL_TRAILERS_EXTENSION + URL_PARM_SEPARATOR +
                URL_PARM_API_KEY + MY_TMDB_API_KEY;
    }

    public static final String TMDB_IMAGE__SIZE = "w185/";

    public static final int DB_TABLE_ROW_INSERT_FAILURE = -1;

    public static final String MOVIE_PARCEL = "movie parcel";

    public static final String NO_TRAILERS_FOUND = "No trailers found.";
    public static final String MOVIE_HAS_NO_TRAILERS = "Movie has no trailers.";

    //Error messages
    public static final String ERROR_NO_INTERNET = "No connection. Probably Internet is unavailable.";
    public static final String ERROR_NO_INTERNET_CANNOT_REFRESH = "No connection, cannot refresh.";
    public static final String ERROR_API_KEY_INVALID = "Error: did you set your TMDB API key?";
    public static final String ERROR_WHILE_RETRIEVING_DATA_FROM_TMBD = "Error while retrieving data from TMDB.";
    public static final String ERROR_NO_FAVORITE_MOVIES_DEFINED = "No favorite movies defined, or not available.";
}
