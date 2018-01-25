package com.example.android.popularmovies.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.popularmovies.Utils.Constants;

/**
 * Created by twelh on 10/01/2018.
 */

/**
 * This class serves as the ContentProvider for all of PopularMmovie's data. This class allows us to
 * bulkInsert data, query data, delete data and update data.
 * <p>
 * ContentProvider implementation requires the implementation of additional methods to
 * perform single inserts, and the ability to get the type of the data from a URI.
 * However, here, they are not implemented since they are not needed in this app.
 */
public class MovieProvider extends ContentProvider {

    /*
     * These constants will be used to match URIs with the data they are looking for. We will take
     * advantage of the UriMatcher class to make that matching MUCH easier than doing something
     * ourselves, such as using regular expressions.
     */
    private static final int CODE_MOVIE = 100;
    private static final int CODE_MOVIE_ID = 110;
    private static final int CODE_FAVORITE_MOVIE = 200;
    private static final int CODE_SELECT_FAVORITE_MOVIES = 300;
    private static final int CODE_MOST_POPULAR_MOVIES = 400;
    private static final int CODE_MOST_POPULAR_MOVIES_ID = 410;
    private static final int CODE_TOP_RATED_MOVIES = 500;
    private static final int CODE_TOP_RATED_MOVIES_ID = 510;


    /*
     * The URI Matcher used by this content provider. The leading "s" in this variable name
     * signifies that this UriMatcher is a static member variable of MovieProvider and is a
     * common convention in Android programming.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Declare, but don't instantiate a MovieDbHelper object called mOpenHelper
    private MovieDbHelper mOpenHelper;

    /**
     * Creates the UriMatcher that will match each URI to the CODE_MOVIE and
     * CODE_FAVORITE_MOVIE constants defined above.
     * <p>
     * It's possible you might be thinking, "Why create a UriMatcher when you can use regular
     * expressions instead? After all, we really just need to match some patterns, and we can
     * use regular expressions to do that right?" Because you're not crazy, that's why.
     * <p>
     * UriMatcher does all the hard work for you. You just have to tell it which code to match
     * with which URI, and it does the rest automagically. Remember, the best programmers try
     * to never reinvent the wheel. If there is a solution for a problem that exists and has
     * been tested and proven, you should almost always use it unless there is a compelling
     * reason not to.
     *
     * @return A UriMatcher that correctly matches the constants for CODE_MOVIE and CODE_FAVORITE_MOVIE
     */
    private static UriMatcher buildUriMatcher() {

        /*
         * All paths added to the UriMatcher have a corresponding code to return when a match is
         * found. The code passed into the constructor of UriMatcher here represents the code to
         * return for the root URI. It's common to use NO_MATCH as the code for this case.
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        /*
         * For each type of URI you want to add, create a corresponding code. Preferably, these are
         * constant fields in your class so that you can use them throughout the class and you no
         * they aren't going to change. In PopularMovies, we use CODE_MOVIE or CODE_FAVORITE_MOVIE.
         */

        /* This URI is content://com.example.android.popularmovie/movie/ */
        matcher.addURI(authority, MovieContract.PATH_MOVIE, CODE_MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", CODE_MOVIE_ID);

        /* This URI is content://com.example.android.popularmovie/favorite_movie/ */
        matcher.addURI(authority, MovieContract.PATH_FAVORITE_MOVIE, CODE_FAVORITE_MOVIE);

        /* This URI is content://com.example.android.popularmovie/join/ */
        matcher.addURI(authority, MovieContract.PATH_SELECT_FAVORITE_MOVIES, CODE_SELECT_FAVORITE_MOVIES);

        /* This URI is content://com.example.android.popularmovie/most_popular_movies/ */
        matcher.addURI(authority, MovieContract.PATH_MOST_POPULAR_MOVIE, CODE_MOST_POPULAR_MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOST_POPULAR_MOVIE + "/#", CODE_MOST_POPULAR_MOVIES_ID);

        /* This URI is content://com.example.android.popularmovie/top_rated_movies/ */
        matcher.addURI(authority, MovieContract.PATH_TOP_RATED_MOVIE, CODE_TOP_RATED_MOVIES);
        matcher.addURI(authority, MovieContract.PATH_TOP_RATED_MOVIE + "/#", CODE_TOP_RATED_MOVIES_ID);


        return matcher;
    }

    /**
     * In onCreate, we initialize our content provider on startup. This method is called for all
     * registered content providers on the application main thread at application launch time.
     * It must not perform lengthy operations, or application startup will be delayed.
     *
     * Nontrivial initialization (such as opening, upgrading, and scanning
     * databases) should be deferred until the content provider is used (via {@link #query},
     * {@link #bulkInsert(Uri, ContentValues[])}, etc).
     *
     * Deferred initialization keeps application startup fast, avoids unnecessary work if the
     * provider turns out not to be needed, and stops database errors (such as a full disk) from
     * halting application launch.
     *
     * @return true if the provider was successfully loaded, false otherwise
     */
    @Override
    public boolean onCreate() {
        /*
         * As noted in the comment above, onCreate is run on the main thread, so performing any
         * lengthy operations will cause lag in your app. Since MovieDbHelper's constructor is
         * very lightweight, we are safe to perform that initialization here.
         */
        mOpenHelper = new MovieDbHelper(getContext());

        // Return true from onCreate to signify success performing setup
        return true;
    }

    /**
     * Handles query requests from clients. We will use this method in Sunshine to query for all
     * of our weather data as well as to query for the weather on a particular day.
     *
     * @param uri           The URI to query
     * @param projection    The list of columns to put into the cursor. If null, all columns are
     *                      included.
     * @param selection     A selection criteria to apply when filtering rows. If null, then all
     *                      rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     *                      the values from selectionArgs, in order that they appear in the
     *                      selection.
     * @param sortOrder     How the rows in the cursor should be sorted.
     * @return A Cursor containing the results of the query. In our implementation,
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        Cursor cursor;

        /*
         * Here's the switch statement that, given a URI, will determine what kind of request is
         * being made and query the database accordingly.
         */
        switch (sUriMatcher.match(uri)) {

            /*
             * When sUriMatcher's match method is called with a URI that looks something like this
             *
             *      content://com.example.android.popularmovies/movie
             *
             * sUriMatcher's match method will return the code that indicates to us that we need
             * to return the the movies.
             *
             * In this case, we want to return a cursor that contains all the row of the movies as
             * returned by TMDB.
             */
            case CODE_MOVIE: {

                cursor = mOpenHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                //Update the UI
                //noinspection ConstantConditions
                cursor.setNotificationUri(getContext().getContentResolver(), uri);

                break;
            }

            case CODE_MOST_POPULAR_MOVIES: {

                cursor = mOpenHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                        MovieContract.MostPopularMovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                //Update the UI
                //noinspection ConstantConditions
                cursor.setNotificationUri(getContext().getContentResolver(), uri);

                break;

            }

                case CODE_TOP_RATED_MOVIES: {

                    cursor = mOpenHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                            MovieContract.TopRatedMovieEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);

                    //Update the UI
                    //noinspection ConstantConditions
                    cursor.setNotificationUri(getContext().getContentResolver(), uri);

                    break;
            }

            case CODE_FAVORITE_MOVIE: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.FavoriteMovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                //Update the UI
                //noinspection ConstantConditions
                cursor.setNotificationUri(getContext().getContentResolver(), uri);

                break;
            }

            case CODE_SELECT_FAVORITE_MOVIES: {

                /* Below an SQL query is prepared and executed.
                 * It retrieves the favorized movies both from most popular and top rated movies,
                 * and removes duplicates (through the UNION operator, which removes duplicates). The SQL query is:
                 *
                 * SELECT tmdb_movie_sort_type, most_popular_movie.tmdb_movie_id, tmdb_movie_title,
                 *        tmdb_movie_poster_path, tmdb_movie_overview, tmdb_movie_release_year,
                 *        tmdb_movie_runtime, tmdb_movie_vote_average, tmdb_movie_favorize_flag,
                 *        tmdb_movie_trailers
                 *   FROM
                 *       most_popular_movie
                 *       INNER JOIN
                 *           favorite_movie
                 *         ON
                 *           most_popular_movie.tmdb_movie_id=favorite_movie.tmdb_movie_id
                 * UNION
                 * SELECT tmdb_movie_sort_type, top_rated_movie.tmdb_movie_id, tmdb_movie_title,
                 *        tmdb_movie_poster_path, tmdb_movie_overview, tmdb_movie_release_year,
                 *        tmdb_movie_runtime, tmdb_movie_vote_average, tmdb_movie_favorize_flag,
                 *        tmdb_movie_trailers
                 *   FROM
                 *       top_rated_movie
                 *       INNER JOIN
                 *           favorite_movie
                 *         ON
                 *           top_rated_movie.tmdb_movie_id=favorite_movie.tmdb_movie_id;
                 */

                String buildSql = "SELECT " +
                        MovieContract.MovieEntry.COLUMN_SORT_TYPE + ", " +
                        MovieContract.MostPopularMovieEntry.TABLE_NAME +
                        "." +
                        MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_TITLE + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_YEAR + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_RUNTIME + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_FAVORIZE_FLAG + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_TRAILERS +
                        " FROM " +
                        MovieContract.MostPopularMovieEntry.TABLE_NAME +
                        " INNER JOIN " +
                        MovieContract.FavoriteMovieEntry.TABLE_NAME +
                        " ON " +
                        MovieContract.MostPopularMovieEntry.TABLE_NAME +
                        "." +
                        MovieContract.MostPopularMovieEntry.COLUMN_TMDB_MOVIE_ID +
                        "=" +
                        MovieContract.FavoriteMovieEntry.TABLE_NAME +
                        "." +
                        MovieContract.FavoriteMovieEntry.COLUMN_TMDB_MOVIE_ID +
                        " UNION " + //Removes duplicate rows
                        "SELECT " +
                        MovieContract.MovieEntry.COLUMN_SORT_TYPE + ", " +
                        MovieContract.TopRatedMovieEntry.TABLE_NAME +
                        "." +
                        MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_TITLE + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_YEAR + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_RUNTIME + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_FAVORIZE_FLAG + ", " +
                        MovieContract.MovieEntry.COLUMN_MOVIE_TRAILERS +
                        " FROM " +
                        MovieContract.TopRatedMovieEntry.TABLE_NAME +
                        " INNER JOIN " +
                        MovieContract.FavoriteMovieEntry.TABLE_NAME +
                        " ON " +
                        MovieContract.TopRatedMovieEntry.TABLE_NAME +
                        "." +
                        MovieContract.TopRatedMovieEntry.COLUMN_TMDB_MOVIE_ID +
                        "=" +
                        MovieContract.FavoriteMovieEntry.TABLE_NAME +
                        "." +
                        MovieContract.FavoriteMovieEntry.COLUMN_TMDB_MOVIE_ID +
                        ";";

                cursor = mOpenHelper.getReadableDatabase().rawQuery(buildSql, null);

                //Update the UI
                //noinspection ConstantConditions
                cursor.setNotificationUri(getContext().getContentResolver(), uri);

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    /**
     * Handles requests to insert a new rows.
     *
     * @param uri           The content:// URI of the insertion request.
     * @param contentValues An column_name/value pair to add to the database.
     *                      This must not be {@code null}.
     *
     * @return              The uri pointing to the newly inserted row.
     */
    @Override
    public Uri insert(@NonNull Uri uri, @NonNull ContentValues contentValues) {

        // Get access to the movie database (to write new data to)
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        Uri returnUri; // URI to be returned

        switch (sUriMatcher.match(uri)) {

            case CODE_FAVORITE_MOVIE:
                // Insert new value into the database
                long id = db.insert(MovieContract.FavoriteMovieEntry.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(MovieContract.FavoriteMovieEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    /**
     * Handles requests to insert a set of new rows. In PopularMovies, we are only going to be
     * inserting multiple rows of data at a time when returned by TMDB. There is no use case
     * for inserting a single row of data into our ContentProvider, and so we are only going to
     * implement bulkInsert. In a normal ContentProvider's implementation, you will probably want
     * to provide proper functionality for the insert method as well.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     *
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int rowsInserted;


        switch (sUriMatcher.match(uri)) {


            case CODE_MOVIE:
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != Constants.DB_TABLE_ROW_INSERT_FAILURE) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    //noinspection ConstantConditions
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            case CODE_MOST_POPULAR_MOVIES:
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(MovieContract.MostPopularMovieEntry.TABLE_NAME, null, value);
                        if (_id != Constants.DB_TABLE_ROW_INSERT_FAILURE) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    //noinspection ConstantConditions
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            case CODE_TOP_RATED_MOVIES:
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(MovieContract.TopRatedMovieEntry.TABLE_NAME, null, value);
                        if (_id != Constants.DB_TABLE_ROW_INSERT_FAILURE) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    //noinspection ConstantConditions
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    /**
     * Deletes data at a given URI with optional arguments for more fine tuned deletions.
     *
     * @param uri           The full URI to query
     * @param selection     An optional restriction to apply to rows when deleting.
     * @param selectionArgs Used in conjunction with the selection statement
     * @return The number of rows deleted
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        /* Users of the delete method will expect the number of rows deleted to be returned. */
        int numRowsDeleted;

        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)) {

            case CODE_MOVIE:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            case CODE_MOST_POPULAR_MOVIES:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.MostPopularMovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            case CODE_TOP_RATED_MOVIES:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.TopRatedMovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            case CODE_FAVORITE_MOVIE:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.FavoriteMovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted != 0) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        //Keep track of if an update occurs
        int moviesUpdated;

        //update a single movie by getting the _id
        String id = uri.getPathSegments().get(1);

        // match code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case CODE_MOVIE_ID:
                //using selections
                moviesUpdated = mOpenHelper.getWritableDatabase().update(
                        MovieContract.MovieEntry.TABLE_NAME,
                        contentValues,
                        MovieContract.MovieEntry._ID + "=?",
                        new String[]{id});
                break;

            case CODE_MOST_POPULAR_MOVIES_ID:
                //using selections
                moviesUpdated = mOpenHelper.getWritableDatabase().update(
                        MovieContract.MostPopularMovieEntry.TABLE_NAME,
                        contentValues,
                        MovieContract.MostPopularMovieEntry._ID + "=?",
                        new String[]{id});
                break;

            case CODE_TOP_RATED_MOVIES_ID:
                //using selections
                moviesUpdated = mOpenHelper.getWritableDatabase().update(
                        MovieContract.TopRatedMovieEntry.TABLE_NAME,
                        contentValues,
                        MovieContract.TopRatedMovieEntry._ID + "=?",
                        new String[]{id});
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (moviesUpdated != 0) {
            //set notifications if a task was updated
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return number of tasks updated
        return moviesUpdated;

    }
}
