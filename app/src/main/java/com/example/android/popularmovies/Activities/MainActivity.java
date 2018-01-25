package com.example.android.popularmovies.Activities;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.Adapters.MovieAdapter;
import com.example.android.popularmovies.Data.MovieContract;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.Utils.Constants;
import com.example.android.popularmovies.Utils.Movie;
import com.example.android.popularmovies.Utils.MovieJsonUtils;
import com.example.android.popularmovies.Utils.NetworkUtils;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/*
 * We create 4 tables in the local SQL database:
 * - 1 for holding most popular movies as retrieved from TMDB called here "original most popular";
 * - 1 for holding top rated movies as retrieved from TMDB called here "original top rated";
 * - 1 for holding information about movies that have been favorized by the user called here "favorized";
 * this information will never be refreshed;
 * - and 1 working table for holding the current dataset; on this table the cursorLoader is working and
 * so cursorLoader is refreshing the UI based on the contents of this table. This table is called here "working copy".
 *
 * When the user wants to see the most popular movies, that data is retrieved from TMDB if not yet done,
 * and after that it is copied from "original most popular" to "working copy" if not yet done.
 * Likewise for top rated movies.
 *
 * The user can refresh, and only then TMDB will be reconsulted for updated movie data and the originals will
 * be refreshed.
 *
 * If the user favorizes or unfavorizes a movie via the detailed movie screen, the "favorized" table is updated.
 * The user can return to the main screen by pressing the "<-" button, or by swiping to the right.
 *
 * When the user wants to see a grid of his favorized movies, a complex SQL query is executed against the
 * "original most popular", "original top rated" and the "favorized" tables. The result is copied to the
 * "working copy" and CursorLoader takes care of updating the UI. Quite some things can go wrong so there are
 * a number of error messages and Toast messages, depending on the specific error condition. The aim is to bother
 * the user with red error fields to the minimum.
 */
public class MainActivity extends AppCompatActivity
        implements MovieAdapter.MovieClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    //Locally used constants
    private final String MOST_POPULAR = "Most popular";
    private final String SHOW_FAVORITES = "Favorites";
    private final String TOP_RATED = "Top rated";
    private final String SILENTLY = "Silently";
    private final String NOT_SILENTLY = "Not silently";
    private final String[] FETCH_PARMS_MOST_POPULAR_AND_NOT_SILENTLY = {MOST_POPULAR, NOT_SILENTLY};
    private final String[] FETCH_PARMS_TOP_RATED_AND_SILENTLY = {TOP_RATED, SILENTLY};
    private final String[] FETCH_PARMS_TOP_RATED_AND_NOT_SILENTLY = {TOP_RATED, NOT_SILENTLY};

    //Local variables
    private RecyclerView mRecyclerView;
    private TextView errorMessageView;
    private String errorMsg = "";
    private ProgressBar mProgressbar;
    private MovieAdapter mAdapter = null;
    private Menu optionsMenu; //Save the menu as local variable
    private MovieAdapter.MovieClickListener movieClickListener;
    private Cursor mCursor;

    //Preferences
    private final String SHARED_PREFERENCES_NAME = "popular_movies_shared_preferences";
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;
    private final String PREF_KEY_SORT_ORDER = "SortOrder";
    private int sortOrderRequested = Movie.SORT_TYPE_NONE; //Default

    /*
     * This ID will be used to identify the Loader responsible for loading the movie main information.
     * In some cases, one Activity can deal with many Loaders. However, in
     * our case, there is only one. We will still use this ID to initialize the loader and create
     * the loader for best practice. Please note that 100 was chosen arbitrarily. You can use
     * whatever number you like, so long as it is unique and consistent.
     */
    private static final int ID_LOADER = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Save place where interface method is implemented, namely in this class instance
        movieClickListener = this;

        //Connect to the UI interface elements
        mRecyclerView = findViewById(R.id.rv_popular_movies);
        errorMessageView = findViewById(R.id.tv_error_message_display);
        mProgressbar = findViewById(R.id.pb_loading_indicator);

        //Initiate visibility of views (including error msg).
        initiateViews();

        //Construct the layout manager depending on the device's orientation.
        int gridColumns = 2; //Default 2 columns, i.e. when the device is in portrait orientation.
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridColumns = 3;
        }
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, gridColumns);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        //Use this setting to improve performance: changes in content do not
        //change the child layout size in the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        /* This connects our Activity into the loader lifecycle. */
        getSupportLoaderManager().initLoader(ID_LOADER, null, this);

        //Determine the sort order. If the user has never set it before, the default is "most popular".
        //After that, obtain the list of movies accordingly sorted.
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        sortOrderRequested = mSharedPreferences.getInt(PREF_KEY_SORT_ORDER,
                Movie.SORT_TYPE_NONE); // Movie.SORT_TYPE_NONE(=-1) is default, if it is not yet set.

        //Determine whether data is already retrieved from TMDB. If not, do so.
        Cursor cursor;
        switch (sortOrderRequested) {
            case Movie.SORT_TYPE_NONE:
                //Sort order never been set before, set it to the default ("most popular") sort type.
                sortOrderRequested = Movie.SORT_TYPE_MOST_POPULAR;
                //Data has never been retrieved from TMDB before, do so now.
                new FetchMovieTask(false).execute(FETCH_PARMS_MOST_POPULAR_AND_NOT_SILENTLY);
                //Also retrieve the top rated movies but do not update the working table and the UI.
                new FetchMovieTask(true).execute(FETCH_PARMS_TOP_RATED_AND_SILENTLY);
                break;
            case Movie.SORT_TYPE_MOST_POPULAR:
                //Query most popular movies
                cursor = getContentResolver().query(MovieContract.MostPopularMovieEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null,
                        null);
                //If no data yet, obtain the data from TMDB
                if (cursor != null && cursor.getCount() == 0) {
                    new FetchMovieTask(false).execute(FETCH_PARMS_MOST_POPULAR_AND_NOT_SILENTLY);
                    //Also retrieve the top rated movies but do not update the working table and the UI.
                    new FetchMovieTask(true).execute(FETCH_PARMS_TOP_RATED_AND_SILENTLY);
                } else {
                    //Check whether data already copied from original table to working table. If not, do so.
                    this.CopyDataFromOriginalToWorking(Movie.SORT_TYPE_MOST_POPULAR_STRING_ARRAY,
                            MovieContract.MostPopularMovieEntry.CONTENT_URI,
                            false);
                    //Set the activity title (on the device screen) accordingly.
                    setTitle(MOST_POPULAR);
                }
                break;
            case Movie.SORT_TYPE_TOP_RATED:
                //Query top rated movies
                cursor = getContentResolver().query(MovieContract.TopRatedMovieEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null,
                        null);
                //If no data yet, obtain the data from TMDB
                if (cursor != null && cursor.getCount() == 0) {
                    new FetchMovieTask(false).execute(FETCH_PARMS_TOP_RATED_AND_NOT_SILENTLY);
                } else {
                    //Check whether data already copied from original table to working table. If not, do so.
                    this.CopyDataFromOriginalToWorking(Movie.SORT_TYPE_TOP_RATED_STRING_ARRAY,
                            MovieContract.TopRatedMovieEntry.CONTENT_URI,
                            false);
                    //Set the activity title (on the device screen) accordingly.
                    setTitle(TOP_RATED);
                }
                break;
            case Movie.SORT_TYPE_FAVORIZED:
                //Load the favorite movies info
                this.loadFavoriteMoviesData();

                if (errorMsg.isEmpty()) {
                    //Set the title accordingly.
                    setTitle(SHOW_FAVORITES);
                } else {
                    displayErrorMsg(errorMsg);
                }
                break;
            default: //Should not happen
        }
    } //End of the onCreate method

    /**
     * This method checks whether data already copied from the original table to the working table. If not, does so.
     *
     * Parameters:
     *
     * @param originalTable A string representing from which original table (most popular or top rated) the
     *                      data should be copied to the working copy.
     *
     * @param originalUri   The URI corresponding to that original table.
     *
     * @param alwaysCopy    A switch that determines whether we first have to check whether data already copied to
     *                      the working copy or not. This is a bit of optimization, it saves db access.
     *
     * @return              N/A, it is a void of type void.
     */
    private void CopyDataFromOriginalToWorking (String[] originalTable, Uri originalUri, Boolean alwaysCopy) {
        Cursor cursor = null;
        if (! alwaysCopy) {
            //Check whether original table already copied to working table.
            cursor = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                    null,
                    MovieContract.MovieEntry.COLUMN_SORT_TYPE + "=?",
                    originalTable,
                    null,
                    null);
        }
        //And delete the working table contents if the alwaysCopy switch is TRUE, or if there are other movies in the
        //working table than in the requested original table (and after that, copy those movies to the working copy).
        if (alwaysCopy || (cursor != null && cursor.getCount() == 0 ) ) {
            //Empty the working table first, whatever is in it
            getContentResolver().delete(
                    MovieContract.MovieEntry.CONTENT_URI,
                    null,
                    null);

            //Get a cursor representing the original table
            cursor = getContentResolver().query(originalUri,
                    null,
                    null,
                    null,
                    null,
                    null);
            //Now get an arrayList of movies from this cursor.
            ArrayList<Movie> movieList = MovieJsonUtils.getMovieListFromTable(cursor, false);
            //And build a contentValues array out of it.
            ContentValues[] movieContentValues = MovieJsonUtils.getMovieContentValues(movieList);
            //Finally bulkInsert into the working table.
            getContentResolver().bulkInsert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    movieContentValues);
        }
    }

    /**
     * This inner class fetches movie data in the required sort order from TMDB
     * on an a-synchronous background thread.
     *
     * Input:
     *
     *                      An array of two Strings.
     *                      The first one represents the sort type (which is "most popular"
     *                      or "top rated"). Each must be retrieved from a different URL at TMDB,
     *                      and each fills a different "original" table.
     *                      The second one indicates whether the UI must be updated after the doInBackGround
     *                      method completes, or not (in case of a "silently" request).
     *
     * @return              N/A, it is a class.
     *
     */
    @SuppressLint("StaticFieldLeak")
    public class FetchMovieTask extends AsyncTask<String[], Void, ArrayList<Movie>> {

        final Boolean mSilently;

        //Constructor
        public FetchMovieTask(Boolean silently) {
            this.mSilently = silently;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //If not silently, make loading indicator visible
            if (! mSilently) {
                displayLoadingIndicator();
            }
        }

        /**
         * This method operates on a background task.
         * The String array described before is input to this method.
         * Its output is a Wrapper helper class instance, filled with the
         * actual output of the network operation (an ArrayList<Movies>)
         * and a switch (or rather a string, a Boolean did not work in this environment)
         * indicating whether the UI must be updated or not.
        */
        @Override
        protected ArrayList<Movie> doInBackground(String[]... params) { //"..." means that more Strings or an array of strings may be passed.

            //Check whether network active.
            try {
                if (!NetworkUtils.isOnline()) {

                    //Set error message
                    errorMsg = Constants.ERROR_NO_INTERNET;

                    return null;
                }
            } catch (Exception e) {

                //Set error message
                errorMsg = Constants.ERROR_NO_INTERNET;

                return null;
            }

            //Initiate the output
            ArrayList<Movie> movieListFromBackground = new ArrayList<>();

            String sortMethod = params[0][0];
            URL TMDBRequestUrl;

            //Build the URL string based on the user's selected sorting method
            if (sortMethod.equals(MOST_POPULAR)) {
                TMDBRequestUrl = NetworkUtils.buildUrl(Constants.TMDB_BASE_URL_MOST_POPULAR);
            } else {
                TMDBRequestUrl = NetworkUtils.buildUrl(Constants.TMDB_BASE_URL_TOP_RATED);
            }

            //Obtain a Jsonstring from the response, and parse it into a movielist.
            if (TMDBRequestUrl != null) {

                try {
                    String jsonMovieResponse = NetworkUtils
                            .getResponseFromHttpUrl(TMDBRequestUrl);

                    if (sortMethod.equals(MOST_POPULAR)) {
                        movieListFromBackground = MovieJsonUtils
                                .getMovieListFromJson(jsonMovieResponse, Movie.SORT_TYPE_MOST_POPULAR);
                    } else if (sortMethod.equals(TOP_RATED)) {
                        movieListFromBackground = MovieJsonUtils
                                .getMovieListFromJson(jsonMovieResponse, Movie.SORT_TYPE_TOP_RATED);
                    }

                    /**
                     * Insert the returned movies into the Content Provider database via BulkInsert;
                     * do this on the current background thread as well since db operations can be time-consuming.
                     */
                    if (movieListFromBackground.size() != 0) {

                        ContentValues[] movieContentValues =
                                MovieJsonUtils.getMovieContentValues(movieListFromBackground);

                        /* Get a handle on the ContentResolver to delete and insert data */
                        ContentResolver movieContentResolver = getContentResolver();


                        if (sortMethod.equals(MOST_POPULAR)) {
                            /* Delete old movie data because we don't need to keep that data. */
                            movieContentResolver.delete(
                                    MovieContract.MostPopularMovieEntry.CONTENT_URI,
                                    null,
                                    null);
                            //Add the newly obtained data from the TMDB into the appropriate database.
                            movieContentResolver.bulkInsert(
                                    MovieContract.MostPopularMovieEntry.CONTENT_URI,
                                    movieContentValues);
                            //And now copy it to the working database.
                            CopyDataFromOriginalToWorking(Movie.SORT_TYPE_MOST_POPULAR_STRING_ARRAY,
                                    MovieContract.MostPopularMovieEntry.CONTENT_URI,
                                    false);
                        } else {
                            /* Delete old movie data because we don't need to keep that data. */
                            movieContentResolver.delete(
                                    MovieContract.TopRatedMovieEntry.CONTENT_URI,
                                    null,
                                    null);
                            //Add the newly obtained data from the TMDB into the appropriate database.
                            movieContentResolver.bulkInsert(
                                    MovieContract.TopRatedMovieEntry.CONTENT_URI,
                                    movieContentValues);
                            //If not silently, now copy it to the working database.
                            if (params[0][1].equals(NOT_SILENTLY)) {
                                CopyDataFromOriginalToWorking(Movie.SORT_TYPE_TOP_RATED_STRING_ARRAY,
                                        MovieContract.TopRatedMovieEntry.CONTENT_URI,
                                        false);
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    errorMsg = Constants.ERROR_API_KEY_INVALID;
                } catch (IOException | JSONException e) {
                    errorMsg = Constants.ERROR_WHILE_RETRIEVING_DATA_FROM_TMBD;
                }
            } else {
                errorMsg = Constants.ERROR_WHILE_RETRIEVING_DATA_FROM_TMBD;
            }

            return movieListFromBackground;
        }

        /**
         * This method updates the UI, unless a "silent" request was done.
         *
         * @param movieList  The list of movies obtained from TMDB.
         *
         */
        @Override
 //       protected void onPostExecute(Wrapper wrapper) {
        protected void onPostExecute(ArrayList<Movie> movieList) {

            //Do not update the user interface when "silently" is requested.
            if (! mSilently) {
                if (! errorMsg.isEmpty() ) {
                  displayErrorMsg(errorMsg);
                } else {
                  //Set loading indicator invisible
                  initiateViews();

                  //Update the preference values.
                  //Determine the sort order. If the user has never set it before, the default is "most popular".
                   //After that, obtain the list of movies accordingly sorted.
                   switch (sortOrderRequested) {
                      case Movie.SORT_TYPE_MOST_POPULAR : {
                           //Save the value
                           mSharedPreferencesEditor = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                           mSharedPreferencesEditor.putInt(PREF_KEY_SORT_ORDER, Movie.SORT_TYPE_MOST_POPULAR);
                           mSharedPreferencesEditor.commit();
                           //Set the title for most popular movies which is the default movie sort order.
                          setTitle(MOST_POPULAR);
                          break;
                      }
                      case Movie.SORT_TYPE_TOP_RATED : {
                            //Save the value
                            mSharedPreferencesEditor = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                            mSharedPreferencesEditor.putInt(PREF_KEY_SORT_ORDER, Movie.SORT_TYPE_TOP_RATED);
                            mSharedPreferencesEditor.commit();
                            //Set the title for best rated movies.
                            setTitle(TOP_RATED);
                            break;
                      }
                       case Movie.SORT_TYPE_FAVORIZED : {
                            //Save the value
                            mSharedPreferencesEditor = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                            mSharedPreferencesEditor.putInt(PREF_KEY_SORT_ORDER, Movie.SORT_TYPE_FAVORIZED);
                            mSharedPreferencesEditor.commit();
                            //Set the title for favorite movies.
                            setTitle(SHOW_FAVORITES);
                            break;
                       }
                       default : //Should not happen, do nothing
                   }
                }
            } else errorMsg = ""; //We did it silently, no error messages in case there are....
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.sortorder, menu);
        //Save the menu for later manipulation of (in)visibility of menu items
        optionsMenu = menu;

        //Determine the sort order. Make the menu options visible/invisible accordingly.
        switch (sortOrderRequested) {
            case Movie.SORT_TYPE_MOST_POPULAR : {
                //Make the "most popular" menu item invisible, and the others visible
                optionsMenu.findItem(R.id.action_most_popular).setVisible(false);
                optionsMenu.findItem(R.id.action_top_rated).setVisible(true);
                optionsMenu.findItem(R.id.action_show_favorites).setVisible(true);
                optionsMenu.findItem(R.id.action_refreh).setVisible(true);
                break;
            }
            case Movie.SORT_TYPE_TOP_RATED : {
                //Make the "best rated" menu item invisible, and the others visible
                optionsMenu.findItem(R.id.action_most_popular).setVisible(true);
                optionsMenu.findItem(R.id.action_top_rated).setVisible(false);
                optionsMenu.findItem(R.id.action_show_favorites).setVisible(true);
                optionsMenu.findItem(R.id.action_refreh).setVisible(true);
                break;
            }
            case Movie.SORT_TYPE_FAVORIZED : {
                //Make the "best rated" and the "most popular" menus visible, and the "show favorites" invisible.
                optionsMenu.findItem(R.id.action_most_popular).setVisible(true);
                optionsMenu.findItem(R.id.action_top_rated).setVisible(true);
                optionsMenu.findItem(R.id.action_show_favorites).setVisible(false);

                //Check whether this is a "recreate": if so set the title to "show favorites"
                if (getTitle().toString().equals(this.getString(this.getApplicationInfo().labelRes))) {
                    setTitle(SHOW_FAVORITES);
                    //Make the "show favorites" menu item invisible, and the other menus visible
                    optionsMenu.findItem(R.id.action_show_favorites).setVisible(false);
                    optionsMenu.findItem(R.id.action_most_popular).setVisible(true);
                    optionsMenu.findItem(R.id.action_top_rated).setVisible(true);

                    //Do not support "refresh" in case favorite movies are shown.
                    optionsMenu.findItem(R.id.action_refreh).setVisible(false);
                }
                break;
            }
            default : //Should not happen, do nothing
        }

        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Save the previous sort order and depending on that, set a copy flag.
        int previousSortOrderRequested = sortOrderRequested;
        Boolean alwaysCopy = false;
        //If the favorized movies are being shown, and the user requests display of another movieset,
        //we always have to renew the working set. Hence the alwaysCopy switch.
        if (previousSortOrderRequested == Movie.SORT_TYPE_FAVORIZED) alwaysCopy = true;

        if (id == R.id.action_most_popular) {
            //Make the "most popular" menu item invisible, and the other menus visible
            item.setVisible(false);
            optionsMenu.findItem(R.id.action_top_rated).setVisible(true);
            optionsMenu.findItem(R.id.action_show_favorites).setVisible(true);
            optionsMenu.findItem(R.id.action_refreh).setVisible(true);

            //Set the title accordingly
            setTitle(MOST_POPULAR);

            sortOrderRequested = Movie.SORT_TYPE_MOST_POPULAR;

            //Re-initiate the views.
            initiateViews();

            //Query most popular movies
            Cursor cursor;
            cursor = getContentResolver().query(MovieContract.MostPopularMovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null,
                    null);
            //If no data yet, obtain the data from TMDB
            if (cursor != null && cursor.getCount() == 0) {
                new FetchMovieTask(false).execute(FETCH_PARMS_MOST_POPULAR_AND_NOT_SILENTLY);
            } else {
                //Check whether data already copied from original table to working table. If not, do so.
                this.CopyDataFromOriginalToWorking(Movie.SORT_TYPE_MOST_POPULAR_STRING_ARRAY,
                        MovieContract.MostPopularMovieEntry.CONTENT_URI,
                        alwaysCopy);
                //Save the search order value.
                mSharedPreferencesEditor = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                mSharedPreferencesEditor.putInt(PREF_KEY_SORT_ORDER, Movie.SORT_TYPE_MOST_POPULAR);
                mSharedPreferencesEditor.commit();
            }

            return true;
        }

        if (id == R.id.action_top_rated) {
            //Make the "best rated" menu item invisible, and the other menus visible
            item.setVisible(false);
            optionsMenu.findItem(R.id.action_most_popular).setVisible(true);
            optionsMenu.findItem(R.id.action_show_favorites).setVisible(true);
            optionsMenu.findItem(R.id.action_refreh).setVisible(true);

            //Set the title accordingly
            setTitle(TOP_RATED);

            sortOrderRequested = Movie.SORT_TYPE_TOP_RATED;

            //Re-initiate the views.
            initiateViews();

            //Query top rated movies
            Cursor cursor;
            cursor = getContentResolver().query(MovieContract.TopRatedMovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null,
                    null);
            //If no data yet, obtain the data from TMDB
            if (cursor != null && cursor.getCount() == 0) {
                new FetchMovieTask(false).execute(/*TOP_RATED*/FETCH_PARMS_TOP_RATED_AND_NOT_SILENTLY);
            } else {
                //Check whether data already copied from original table to working table. If not, do so.
                this.CopyDataFromOriginalToWorking(Movie.SORT_TYPE_TOP_RATED_STRING_ARRAY,
                        MovieContract.TopRatedMovieEntry.CONTENT_URI,
                        alwaysCopy);
                //Save the search order value.
                mSharedPreferencesEditor = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                mSharedPreferencesEditor.putInt(PREF_KEY_SORT_ORDER, Movie.SORT_TYPE_TOP_RATED);
                mSharedPreferencesEditor.commit();
            }

            return true;
        }

        if (id == R.id.action_show_favorites) {

            //Load the data from the local database
            this.loadFavoriteMoviesData();

            if (! errorMsg.isEmpty()) {
                displayErrorMsg(errorMsg);
            } else {
                //Make the "show favorites" menu item invisible, and the other menus visible
                item.setVisible(false);
                optionsMenu.findItem(R.id.action_most_popular).setVisible(true);
                optionsMenu.findItem(R.id.action_top_rated).setVisible(true);

                //Do not support "refresh" in case favorite movies are shown.
                optionsMenu.findItem(R.id.action_refreh).setVisible(false);

                //Set the title accordingly
                setTitle(SHOW_FAVORITES);

                //Save the value
                mSharedPreferencesEditor = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                mSharedPreferencesEditor.putInt(PREF_KEY_SORT_ORDER, Movie.SORT_TYPE_FAVORIZED);
                mSharedPreferencesEditor.commit();
                sortOrderRequested = Movie.SORT_TYPE_FAVORIZED;

                //Re-initiate the views.
                initiateViews();
            }

            return true;
        }

        if (id == R.id.action_refreh) {
            //Empty the original tables and the working table (not the favorite movies table), and recreate.
            if (NetworkUtils.isOnline()) {
                getContentResolver().delete(
                        MovieContract.MovieEntry.CONTENT_URI,
                        null,
                        null);
                getContentResolver().delete(
                        MovieContract.MostPopularMovieEntry.CONTENT_URI,
                        null,
                        null);
                getContentResolver().delete(
                        MovieContract.TopRatedMovieEntry.CONTENT_URI,
                        null,
                        null);

                recreate();

            } else {
                displayErrorMsg(Constants.ERROR_NO_INTERNET_CANNOT_REFRESH);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method requests execution of a complex SQL query (through MovieProvider) in order to obtain all movies
     * that the user favorized. The result is copied to the working copy table.
     */
    private void loadFavoriteMoviesData() {
        //Query the table of favorized movies. Note that this table will never be refreshed even if the user requests
        //a refresh: only the most popular and top rated movie tables will be refreshed. Otherwise the user would
        //loose all favorized movies.
        Cursor cursor = getContentResolver().query(MovieContract.SelectFavoriteMoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null,
                null
        );

        if (! (cursor != null && cursor.getCount() == 0) ) {

            /* Insert our favorized movie data into PopularMovie's ContentProvider. */
            /* Loader ensures that the user interface is updated.             */

            //First build a favorite movies array
            ArrayList<Movie> favorizedMovies = MovieJsonUtils.getMovieListFromTable(cursor, true);

            //Now put the result into ContentValues
            ContentValues[] movieContentValues = MovieJsonUtils.getMovieContentValues(favorizedMovies);

            /* First delete the working table's data. */
            /* The reason being that Loader will do its work in updating the UI.    */
            getContentResolver().delete(
                    MovieContract.MovieEntry.CONTENT_URI,
                    null,
                    null);

            getContentResolver().bulkInsert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    movieContentValues);
        } else {
            //Look in the working db: if there is only one movie left in there, we have been looking at favorites
            //and just unfavorized the last favorite movie. In that case also delete the data from the working table.
            cursor = getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            if (cursor != null && cursor.getCount() == 1) {
                int rowsDeleted = getContentResolver().delete(
                        MovieContract.MovieEntry.CONTENT_URI,
                        null,
                        null
                );
            }

            errorMsg = Constants.ERROR_NO_FAVORITE_MOVIES_DEFINED;
        }
    }

    /**
     * This method will make an error message visible and hide the movie
     * View and the loading indicator.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     *
     * @param message: error message text.
     *
     */
    private void displayErrorMsg(String message) {

        if (message.equals(Constants.ERROR_NO_FAVORITE_MOVIES_DEFINED)     ||
                message.equals(Constants.ERROR_NO_INTERNET_CANNOT_REFRESH)    ) {
            //Do not bother the user with a red error field when he should know that he has not favorized movies.
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressbar.setVisibility(View.INVISIBLE);
            errorMessageView.setVisibility(View.VISIBLE);
            errorMessageView.setText(message);
        }
        this.errorMsg = "";
    }

    /*
     * This method displays a loading indicator while data is retrieved from TMDB in the background.
     *
     */
    private void displayLoadingIndicator() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mProgressbar.setVisibility(View.VISIBLE);
        errorMessageView.setVisibility(View.INVISIBLE);
    }

    /*
     * This method initiates the visibility of the views.
     *
     */
    private void initiateViews() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressbar.setVisibility(View.INVISIBLE);
        errorMessageView.setVisibility(View.INVISIBLE);
        errorMsg = "";
    }

    /*
     * This method implements the click listener interface.
     * The request code when starting the DetailActivity is irrelevant here.
     */
    @Override
    public void onMovieClick(Movie movie) {

        //Pass data to next activity and start it.
        Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
        intent.putExtra(Constants.MOVIE_PARCEL, movie);
        startActivityForResult(intent, 100);
    }

    /**
     * If the user was in the DetailActivity, return to MainActivity will be in this method.
     * All it does is recreating MainActivity, in order to refresh the UI in case the user has
     * unfavorized the movie. The requestCode is irrelevant here.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //In case we were on the favorite movies view, selected a movie and
        //unfavorized it, the favorite movies view must be refreshed upon return here.
        recreate();
    }

    /**
     * These methods implement the CursorLoader callbacks.
     * in onCreate the table (or part of the table) is defined to manage updates and
     * synchronization of the UI.
     *
     * @param loaderId  Defined at the top. Not so relevant since we only have one loader.
     * @param args      Not actively used here.
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        CursorLoader cursorLoader = new CursorLoader(
                this,
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        this.mCursor = cursor;
        if (mAdapter == null) {
            mAdapter = new MovieAdapter(this, mCursor);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setMovieData(mCursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mAdapter.swapCursor(mCursor);
    }
}
