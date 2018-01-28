package com.example.android.popularmovies.Data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by twelh on 16/01/2018.
 */

public class MovieContract {
    /*
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * Play Store. In this case, "example" is fine although not acceptable for the Play Store, but this
     * app will not be shipped to the Play Store.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies";

    /*
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider for PopularMovies.
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /*
     * Possible paths that can be appended to BASE_CONTENT_URI to form valid URI's that PopularMovies
     * can handle. For instance,
     *
     *     content://com.example.android.popularmovies/movie/
     *     [           BASE_CONTENT_URI         ][ PATH_MOVIE ]
     *
     * is a valid path for looking at movie data.
     *
     *      content://com.example.android.popularmovies/givemeroot/
     *
     * will fail, as the ContentProvider hasn't been given any information on what to do with
     * "givemeroot".
     */
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_MOST_POPULAR_MOVIE = "most_popular_movie";
    public static final String PATH_TOP_RATED_MOVIE = "top_rated_movie";
    public static final String PATH_FAVORITE_MOVIE = "favorite_movie";
    public static final String PATH_SELECT_FAVORITE_MOVIES = "select_favorite_movies";

    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Movie table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_MOVIE)
                .build();

        /* Used internally as the name of our movie table. */
        public static final String TABLE_NAME = "movie";

        /* The sort type of the movies as assigned by TMDB, stored as integer. */
        public static final String COLUMN_SORT_TYPE = "tmdb_movie_sort_type";

        /* The id of the movie as assigned by TMDB, stored as integer. */
        public static final String COLUMN_TMDB_MOVIE_ID = "tmdb_movie_id";

        /* The movie title. */
        public static final String COLUMN_MOVIE_TITLE = "tmdb_movie_title";

        /* Path to the movie poster. */
        public static final String COLUMN_MOVIE_POSTER_PATH = "tmdb_movie_poster_path";

        /* Short summary of the movie. */
        public static final String COLUMN_MOVIE_OVERVIEW = "tmdb_movie_overview";

        /* Movie vote average, stored as double. */
        public static final String COLUMN_MOVIE_VOTE_AVERAGE = "tmdb_movie_vote_average";

        /* Movie year release, stored as integer. */
        public static final String COLUMN_MOVIE_RELEASE_YEAR = "tmdb_movie_release_year";

        /* Movie runtime in minutes, stored as integer. */
        public static final String COLUMN_MOVIE_RUNTIME = "tmdb_movie_runtime";

        /* Movie favorized or not, stored as integer. */
        public static final String COLUMN_MOVIE_FAVORIZE_FLAG = "tmdb_movie_favorize_flag";

        /* Movie trailers, stored as string. */
        public static final String COLUMN_MOVIE_TRAILERS = "tmdb_movie_trailers";
    }

    /* Inner class that defines the table contents of the most popular movie table */
    public static final class MostPopularMovieEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Movie table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_MOST_POPULAR_MOVIE)
                .build();

        /* Used internally as the name of our movie table. */
        public static final String TABLE_NAME = "most_popular_movie";

        /* The sort type of the movies as assigned by TMDB, stored as integer. */
        public static final String COLUMN_SORT_TYPE = "tmdb_movie_sort_type";

        /* The id of the movie as assigned by TMDB, stored as integer. */
        public static final String COLUMN_TMDB_MOVIE_ID = "tmdb_movie_id";

        /* The movie title. */
        public static final String COLUMN_MOVIE_TITLE = "tmdb_movie_title";

        /* Path to the movie poster. */
        public static final String COLUMN_MOVIE_POSTER_PATH = "tmdb_movie_poster_path";

        /* Short summary of the movie. */
        public static final String COLUMN_MOVIE_OVERVIEW = "tmdb_movie_overview";

        /* Movie vote average, stored as double. */
        public static final String COLUMN_MOVIE_VOTE_AVERAGE = "tmdb_movie_vote_average";

        /* Movie year release, stored as integer. */
        public static final String COLUMN_MOVIE_RELEASE_YEAR = "tmdb_movie_release_year";

        /* Movie runtime in minutes, stored as integer. */
        public static final String COLUMN_MOVIE_RUNTIME = "tmdb_movie_runtime";

        /* Movie favorized or not, stored as integer. */
        public static final String COLUMN_MOVIE_FAVORIZE_FLAG = "tmdb_movie_favorize_flag";

        /* Movie trailers, stored as string. */
        public static final String COLUMN_MOVIE_TRAILERS = "tmdb_movie_trailers";
    }

    /* Inner class that defines the table contents of the most popular movie table */
    public static final class TopRatedMovieEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Movie table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_TOP_RATED_MOVIE)
                .build();

        /* Used internally as the name of our movie table. */
        public static final String TABLE_NAME = "top_rated_movie";

        /* The sort type of the movies as assigned by TMDB, stored as integer. */
        public static final String COLUMN_SORT_TYPE = "tmdb_movie_sort_type";

        /* The id of the movie as assigned by TMDB, stored as integer. */
        public static final String COLUMN_TMDB_MOVIE_ID = "tmdb_movie_id";

        /* The movie title. */
        public static final String COLUMN_MOVIE_TITLE = "tmdb_movie_title";

        /* Path to the movie poster. */
        public static final String COLUMN_MOVIE_POSTER_PATH = "tmdb_movie_poster_path";

        /* Short summary of the movie. */
        public static final String COLUMN_MOVIE_OVERVIEW = "tmdb_movie_overview";

        /* Movie vote average, stored as double. */
        public static final String COLUMN_MOVIE_VOTE_AVERAGE = "tmdb_movie_vote_average";

        /* Movie year release, stored as integer. */
        public static final String COLUMN_MOVIE_RELEASE_YEAR = "tmdb_movie_release_year";

        /* Movie runtime in minutes, stored as integer. */
        public static final String COLUMN_MOVIE_RUNTIME = "tmdb_movie_runtime";

        /* Movie favorized or not, stored as integer. */
        public static final String COLUMN_MOVIE_FAVORIZE_FLAG = "tmdb_movie_favorize_flag";

        /* Movie trailers, stored as string. */
        public static final String COLUMN_MOVIE_TRAILERS = "tmdb_movie_trailers";
    }

    /* Inner class that defines the table contents of the favorite_movie table */
    public static final class FavoriteMovieEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the favorite_movie table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_FAVORITE_MOVIE)
                .build();

        /* Used internally as the name of our movie table. */
        public static final String TABLE_NAME = "favorite_movie";

        /* The id of the movie as assigned by TMDB, stored as integer. */
        public static final String COLUMN_TMDB_MOVIE_ID = "tmdb_movie_id";
    }

    /* Inner class that defines the "table" contents of the join operation on movie table and favorite movie table. */
    public static final class SelectFavoriteMoviesEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Movie table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_SELECT_FAVORITE_MOVIES)
                .build();

        /* No table name, this is the result of an sql operation. */

        /* The sort type of the movies as assigned by TMDB, stored as integer. */
        public static final String COLUMN_SORT_TYPE = "tmdb_movie_sort_type";

        /* The id of the movie as assigned by TMDB, stored as integer. */
        public static final String COLUMN_TMDB_MOVIE_ID = "tmdb_movie_id";

        /* The movie title. */
        public static final String COLUMN_MOVIE_TITLE = "tmdb_movie_title";

        /* Path to the movie poster. */
        public static final String COLUMN_MOVIE_POSTER_PATH = "tmdb_movie_poster_path";

        /* Short summary of the movie. */
        public static final String COLUMN_MOVIE_OVERVIEW = "tmdb_movie_overview";

        /* Movie vote average, stored as double. */
        public static final String COLUMN_MOVIE_VOTE_AVERAGE = "tmdb_movie_vote_average";

        /* Movie year release, stored as integer. */
        public static final String COLUMN_MOVIE_RELEASE_YEAR = "tmdb_movie_release_year";

        /* Movie runtime in minutes, stored as integer. */
        public static final String COLUMN_MOVIE_RUNTIME = "tmdb_movie_runtime";

        /* Movie favorized or not, stored as integer. */
        public static final String COLUMN_MOVIE_FAVORIZE_FLAG = "tmdb_movie_favorize_flag";

        /* Movie trailers, stored as string. */
        public static final String COLUMN_MOVIE_TRAILERS = "tmdb_movie_trailers";

    }
}
