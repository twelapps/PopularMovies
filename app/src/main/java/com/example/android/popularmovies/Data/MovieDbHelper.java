package com.example.android.popularmovies.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.example.android.popularmovies.Data.MovieContract.MovieEntry;
import com.example.android.popularmovies.Data.MovieContract.MostPopularMovieEntry;
import com.example.android.popularmovies.Data.MovieContract.TopRatedMovieEntry;
import com.example.android.popularmovies.Data.MovieContract.FavoriteMovieEntry;

/**
 * Created by twelh on 16/01/2018.
 */

class MovieDbHelper extends SQLiteOpenHelper {

    /*
     * This is the name of our database. Database names should be descriptive and end with the
     * .db extension.
     */
    private static final String DATABASE_NAME = "movie.db";

    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     *
     */
    private static final int DATABASE_VERSION = 1;

    /** Static method that will deliver the sql statement to create a table with specification
     * as in MovieContract.MovieEntry.
     * @param tableName The name of the table to be created, as specified in MovieContract.
     * @return          A string representing the sql query that will be executed.
     */
    @NonNull
    private static final String createSqlQueryToCreateTable (String tableName) {
        StringBuilder sqlCreateMovieTable = new StringBuilder();
        sqlCreateMovieTable.append("CREATE TABLE ");
        sqlCreateMovieTable.append(tableName);
        sqlCreateMovieTable.append(" (");
        /**
         * Movie tables do not explicitly declare a column called "_ID". However,
         * MovieEntry implements the interface, "BaseColumns", which does have a field
         * named "_ID". We use that here to designate our table's primary key.
         */
        sqlCreateMovieTable.append(MovieEntry._ID);
        sqlCreateMovieTable.append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sqlCreateMovieTable.append(MovieEntry.COLUMN_SORT_TYPE);
        sqlCreateMovieTable.append(" INTEGER NOT NULL, ");
        sqlCreateMovieTable.append(MovieEntry.COLUMN_TMDB_MOVIE_ID);
        sqlCreateMovieTable.append(" INTEGER NOT NULL, ");
        sqlCreateMovieTable.append(MovieEntry.COLUMN_MOVIE_TITLE);
        sqlCreateMovieTable.append(" STRING NOT NULL, ");
        sqlCreateMovieTable.append(MovieEntry.COLUMN_MOVIE_POSTER_PATH);
        sqlCreateMovieTable.append(" STRING NOT NULL, ");
        sqlCreateMovieTable.append(MovieEntry.COLUMN_MOVIE_OVERVIEW);
        sqlCreateMovieTable.append(" TEXT NOT NULL, ");
        sqlCreateMovieTable.append(MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE);
        sqlCreateMovieTable.append(" DOUBLE NOT NULL, ");
        sqlCreateMovieTable.append(MovieEntry.COLUMN_MOVIE_RUNTIME);
        sqlCreateMovieTable.append(" INTEGER, ");
        //Let's store 4-digit years.
        sqlCreateMovieTable.append(MovieEntry.COLUMN_MOVIE_RELEASE_YEAR);
        sqlCreateMovieTable.append(" YEAR NOT NULL, ");
        sqlCreateMovieTable.append(MovieEntry.COLUMN_MOVIE_FAVORIZE_FLAG);
        sqlCreateMovieTable.append(" INTEGER, ");
        sqlCreateMovieTable.append(MovieEntry.COLUMN_MOVIE_TRAILERS);
        sqlCreateMovieTable.append(" STRING);");
        return sqlCreateMovieTable.toString();
    }

    /**
     * This string will deliver a simple SQL statement that will create a table that will
     * cache our favorized movie data.
     */
    @NonNull
    private static final String createSqlQueryToCreateFavorizedTable () {
        StringBuilder sqlCreateFavorizedMovieTable = new StringBuilder();
        sqlCreateFavorizedMovieTable.append("CREATE TABLE ");
        sqlCreateFavorizedMovieTable.append(MovieContract.FavoriteMovieEntry.TABLE_NAME);
        sqlCreateFavorizedMovieTable.append(" (");
        /**
         * Movie tables do not explicitly declare a column called "_ID". However,
         * MovieEntry implements the interface, "BaseColumns", which does have a field
         * named "_ID". We use that here to designate our table's primary key.
         */
        sqlCreateFavorizedMovieTable.append(MovieEntry._ID);
        sqlCreateFavorizedMovieTable.append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sqlCreateFavorizedMovieTable.append(MovieEntry.COLUMN_TMDB_MOVIE_ID);
        sqlCreateFavorizedMovieTable.append(" INTEGER NOT NULL);");
        return sqlCreateFavorizedMovieTable.toString();
    }

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the tables should happen.
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /*
         * Create all 4 sql tables that are required by this app.
         */
        sqLiteDatabase.execSQL(createSqlQueryToCreateTable(MovieEntry.TABLE_NAME));
        sqLiteDatabase.execSQL(createSqlQueryToCreateTable(MostPopularMovieEntry.TABLE_NAME));
        sqLiteDatabase.execSQL(createSqlQueryToCreateTable(TopRatedMovieEntry.TABLE_NAME));
        sqLiteDatabase.execSQL(createSqlQueryToCreateFavorizedTable());
    }

    /**
     * This database is only a cache for online data, so its upgrade policy is simply to discard
     * the data and call through to onCreate to recreate the table. Note that this only fires if
     * you change the version number for your database (in our case, DATABASE_VERSION). It does NOT
     * depend on the version number for your application found in your app/build.gradle file. If
     * you want to update the schema without wiping data, commenting out the current body of this
     * method should be your top priority before modifying this method.
     *
     * @param sqLiteDatabase Database that is being upgraded
     * @param oldVersion     The old database version
     * @param newVersion     The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MostPopularMovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TopRatedMovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteMovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
