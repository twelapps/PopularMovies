package com.example.android.popularmovies.Utils;

/**
 * Created by twelh on 20/12/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public final class Constants {
    //The Movie DataBase constants
    //TODO: REMOVE TMDB API KEY
    private static final String YOUR_TMDB_API_KEY_HERE = "Your TMDB API key here";
    private static final String MY_TMDB_API_KEY = YOUR_TMDB_API_KEY_HERE;
    private static final String TMDB_BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String TMDB_URL_TRAILERS_EXTENSION = "/videos";
    private static final String TMDB_URL_REVIEWS_EXTENSION = "/reviews";
    private static final String URL_PARM_SEPARATOR = "?";
    private static final String URL_MULTIPLE_PARM_SEPARATOR = "&";
    private static final String URL_PARM_API_KEY = "api_key=";
    private static final String URL_PARM_PAGE = "page=";
    public static final String TMDB_BASE_URL_IMAGES = "http://image.tmdb.org/t/p/";

    public static String TMDB_BASE_URL_MOST_POPULAR (int pageNo) {
        return TMDB_BASE_URL + "popular" + URL_PARM_SEPARATOR +
                URL_PARM_API_KEY + MY_TMDB_API_KEY + URL_MULTIPLE_PARM_SEPARATOR +
                URL_PARM_PAGE + Integer.toString(pageNo);
    }
    public static String TMDB_BASE_URL_TOP_RATED (int pageNo) {
        return TMDB_BASE_URL + "top_rated" + URL_PARM_SEPARATOR +
                URL_PARM_API_KEY + MY_TMDB_API_KEY + URL_MULTIPLE_PARM_SEPARATOR +
                URL_PARM_PAGE + Integer.toString(pageNo);
    }
    public static String TMDB_RUNTIME_URL (int movieId, int pageNo) {
        return TMDB_BASE_URL + Integer.toString(movieId) + URL_PARM_SEPARATOR +
                URL_PARM_API_KEY + MY_TMDB_API_KEY + URL_MULTIPLE_PARM_SEPARATOR +
                URL_PARM_PAGE + Integer.toString(pageNo);
    }
    public static String TMDB_TRAILERS_URL (int movieId) {
        return TMDB_BASE_URL + Integer.toString(movieId) + TMDB_URL_TRAILERS_EXTENSION + URL_PARM_SEPARATOR +
                URL_PARM_API_KEY + MY_TMDB_API_KEY;
    }
    public static String TMDB_REVIEWS_URL (int movieId, int pageNo) {
        return TMDB_BASE_URL + Integer.toString(movieId) + TMDB_URL_REVIEWS_EXTENSION + URL_PARM_SEPARATOR +
                URL_PARM_API_KEY + MY_TMDB_API_KEY + URL_MULTIPLE_PARM_SEPARATOR +
                URL_PARM_PAGE + Integer.toString(pageNo);
    }

    public static final String TMDB_IMAGE__SIZE = "w185/";

    public static final int DB_TABLE_ROW_INSERT_FAILURE = -1;

    public static final String MOVIE_PARCEL = "movie parcel";

    public static final String NO_TRAILERS_FOUND = "No trailers found.";
    public static final String MOVIE_HAS_NO_TRAILERS = "Movie has no trailers.";

    public static final String NO_REVIEWS_FOUND = "No reviews found.";
    public static final String MOVIE_HAS_NO_REVIEWS = "Movie has no reviews.";
    public static final String SWIPE_LEFT_TO_RETURN = "Swipe left on movie title to remove reviews.";

    //Error messages
    public static final String ERROR_NO_INTERNET = "No connection. Probably Internet is unavailable.";
    public static final String ERROR_NO_INTERNET_CANNOT_REFRESH = "No connection, cannot refresh.";
    public static final String ERROR_NO_INTERNET_NO_REVIEWS = "No connection, cannot display reviews.";
    public static final String ERROR_API_KEY_INVALID = "Error: did you set your TMDB API key?";
    public static final String ERROR_WHILE_RETRIEVING_DATA_FROM_TMBD = "Error while retrieving data from TMDB.";
    public static final String ERROR_NO_FAVORITE_MOVIES_DEFINED = "No favorite movies defined, or not available.";
}
