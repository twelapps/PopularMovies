package com.example.android.popularmovies.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.Adapters.MovieAdapter;
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

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieClickListener {

    //Local constants and variables
    final int GRID_COLUMNS = 2;
    final String MOST_POPULAR = "Most popular movies";
    final String BEST_RATED = "Best rated movies";
    RecyclerView mRecyclerView;
    TextView errorMessageView;
    String errorMsg;
    ProgressBar mProgressbar;
    GridLayoutManager mGridLayoutManager;
    MovieAdapter mMovieAdapter = null;
    ImageView ivTemp;
    Menu optionsMenu; //Save the menu as local variable
    MovieAdapter.MovieClickListener movieClickListener;

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

        //Construct the layout manager
        mGridLayoutManager = new GridLayoutManager(this, GRID_COLUMNS);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        //Use this setting to improve performance: changes in content do not
        //change the child layout size in the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        //Set the title for most popular movies which is the default movie sort order.
        setTitle(MOST_POPULAR);

        //Load the data from TMDB
        this.loadMovieData(MOST_POPULAR); //Default sorting method
    }

    /**
     * This method will get the user's preferred movie sorting method, and then tell a
     * background method to get the movie data in the background.
     *
     * @param sortingOrder: MOST_POPULAR movie first, or BEST_RATED movie first.
     *
     */
    private void loadMovieData(String sortingOrder) {

        new FetchMovieTask().execute(sortingOrder);
    }

    /**
     * This inner class fetches movie data in the required sort order from TMDB
     * on an a-synchronous background thread.
     *
     * How this works in terms of parameters:
     *
     * private class ClassName extends AsyncTask<Type1, Integer, Type2> {
     *
     *    //On background thread
     *    protected Type2 doInBackground(Type1... types) {
     *
     *       type2 totalSize;
     *
     *       return totalSize;
     *    }
     *
     *    //On foreground thread
     *    protected void onProgressUpdate(Integer... progress) {
     *       setProgressPercent(progress[0]);
     *    }
     *
     *    //On foreground thread
     *    protected void onPostExecute(Type2 type2) {
     *       //Do something with type2
     *    }
     *
     * }
     *
     */
    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Make loading indicator visible
            displayLoadingIndicator();
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            //Check whether network active.
            try {
                if (!isOnline()) {

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

            String sortMethod = params[0];
            URL TMDBRequestUrl = null;

            //Build the URL string based on the user's selected sorting method
            if (sortMethod.equals(MOST_POPULAR)) {
                TMDBRequestUrl = NetworkUtils.buildUrl(Constants.TMDB_BASE_URL_MOST_POPULAR);
            } else {
                TMDBRequestUrl = NetworkUtils.buildUrl(Constants.TMDB_BASE_URL_BEST_RATED);
            }

            //Obtain a Jsonstring from the response, and parse it into a movielist.
            if (TMDBRequestUrl != null) {

                try {
                    String jsonMovieResponse = NetworkUtils
                            .getResponseFromHttpUrl(TMDBRequestUrl);

                    movieListFromBackground = MovieJsonUtils
                            .getMovieListFromJson(jsonMovieResponse);

                } catch (FileNotFoundException e) {
                    errorMsg = Constants.ERROR_API_KEY_INVALID;
                } catch (IOException e) {
                    errorMsg = Constants.ERROR_WHILE_RETRIEVING_DATA_FROM_TMBD;
                } catch (JSONException e) {
                    errorMsg = Constants.ERROR_WHILE_RETRIEVING_DATA_FROM_TMBD;
                }
            } else {
                errorMsg = Constants.ERROR_WHILE_RETRIEVING_DATA_FROM_TMBD;
            }
            return movieListFromBackground;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movieListFromBackground) {

            if (errorMsg != "") {
                displayErrorMsg(errorMsg);
            } else {
                //Set loading indicator invisible
                initiateViews();

                //Construct or update the adapter.
                if (mMovieAdapter == null) {
                    mMovieAdapter = new MovieAdapter(movieClickListener, movieListFromBackground);
                    mRecyclerView.setAdapter(mMovieAdapter);
                } else {
                    mMovieAdapter.setMovieData(movieListFromBackground);
                }
            }
        }

        //Checks whether my Internet connection is available.
        //Works only when ping'ing a real server, not by using ConnectivityManager.getActiveNetworkInfo().
        public boolean isOnline() throws Exception { //Pass exception to caller
            Runtime runtime = Runtime.getRuntime();
            try {
                //IP 8.8.8.8 is a Google DNS which should always be reachable.
                Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                int     exitValue = ipProcess.waitFor();
                return (exitValue == 0);
            }
            catch (IOException e)          { /* Error will be thrown in calling method. */ }
            catch (InterruptedException e) { /* Error will be thrown in calling method. */ }

            return false;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.sortorder, menu);
        //Save the menu for later manipulation of (in)visibiity of menu items
        optionsMenu = menu;
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_most_popular) {
            //Make the "best rated" menu item visible
            item.setVisible(false);
            optionsMenu.getItem(1).setVisible(true);

            //Set the title accordingly
            setTitle(MOST_POPULAR);

            //Re-initiate the views.
            initiateViews();

            //Load the data from TMDB, most popular movie first
            this.loadMovieData(MOST_POPULAR);
            return true;
        }

        if (id == R.id.action_best_rated) {
            //Make the "most popular" menu item visible
            item.setVisible(false);
            optionsMenu.getItem(0).setVisible(true);

            //Set the title accordingly
            setTitle(BEST_RATED);

            //Re-initiate the views.
            initiateViews();

            //Load the data from TMDB, best rated movie first
            this.loadMovieData(BEST_RATED);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    public void displayErrorMsg (String message) {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mProgressbar.setVisibility(View.INVISIBLE);
        errorMessageView.setVisibility(View.VISIBLE);
        errorMessageView.setText(message);
    }

    /*
     * This method displays a loading indicator while data is retrieved from TMDB in the background.
     *
     */
    public void displayLoadingIndicator () {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mProgressbar.setVisibility(View.VISIBLE);
        errorMessageView.setVisibility(View.INVISIBLE);
    }

    /*
     * This method initiates the visibility of the views.
     *
     */
    public void initiateViews() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressbar.setVisibility(View.INVISIBLE);
        errorMessageView.setVisibility(View.INVISIBLE);
        errorMsg = "";
    }

    /*
     * This method implements the click listener interface.
     */
    @Override
    public void onMovieClick(int clickedMovieIndex, Movie movie) {

        //Pass data to next activity and start it.
        Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
        intent.putExtra(Constants.MOVIE_PARCEL, movie);
        startActivity(intent);
    }
}
