package com.example.android.popularmovies.Utils;

/**
 * Created by twelh on 20/12/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public final class Constants {
    //The Movie DataBase constants
    public static final String MY_TMDB_API_KEY = "Copy_your_TMDB_API_key_here";
    public static final String TMDB_BASE_URL_MOST_POPULAR = "http://api.themoviedb.org/3/movie/popular?api_key=" + MY_TMDB_API_KEY;
    public static final String TMDB_BASE_URL_BEST_RATED = "http://api.themoviedb.org/3/movie/top_rated?api_key=" + MY_TMDB_API_KEY;
    public static final String TMDB_BASE_URL_IMAGES = "http://image.tmdb.org/t/p/";
    public static final String TMDB_IMAGE__SIZE = "w185/";

    public static final int NO_MOVIE_ID = -1;

    public static final String MOVIE_PARCEL = "movie parcel";

    //Error messages
    public static final String ERROR_NO_INTERNET = "No connection. Probably Internet is unavailable.";
    public static final String ERROR_API_KEY_INVALID = "Error: did you set your TMDB API key?";
    public static final String ERROR_WHILE_RETRIEVING_DATA_FROM_TMBD = "Error while retrieving data from TMDB.";
}
