package com.example.android.popularmovies.Activities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.Adapters.MovieTrailerAdapter;
import com.example.android.popularmovies.Data.MovieContract;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.Utils.Constants;
import com.example.android.popularmovies.Utils.Movie;
import com.example.android.popularmovies.Utils.MovieJsonUtils;
import com.example.android.popularmovies.Utils.NetworkUtils;
import com.google.android.youtube.player.YouTubeIntents;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class MovieDetailActivity extends AppCompatActivity
                implements MovieTrailerAdapter.MovieTrailerClickListener {

    //Local variables
    private ImageView moviePosterView;
    private TextView tvDetailTitle;
    private TextView tvVoteAverage;
    private TextView tvReleaseDate;
    private TextView tvOverview;
    private Button btFavOrUnfav;
    private ProgressBar pbFavOrUnfavLoadingIndicator;
    private TextView tvRuntime;
    private ProgressBar pbRuntimeLoadingIndicator;
    private TextView tvErrorMessage;
    private RecyclerView rvTrailers;
    private ProgressBar pbTrailersLoadingIndicator;

    private String errorMsg = "";

    private MovieTrailerAdapter mAdapter;

    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set view according to device orientation. I defined two xml files.
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_movie_detail);
        } else {
            setContentView(R.layout.activity_movie_detail_landscape);
        }

        //Connect to the UI elements.
        moviePosterView = findViewById(R.id.detail_iv_detail_movie_poster);
        tvDetailTitle = findViewById(R.id.detail_tv_detail_title);
        tvVoteAverage = findViewById(R.id.detail_tv_vote_average);
        tvReleaseDate = findViewById(R.id.detail_tv_release_date);
        tvOverview = findViewById(R.id.detail_tv_overview);
        pbFavOrUnfavLoadingIndicator = findViewById(R.id.detail_pb_fav_or_unfav_loading_indicator);
        tvRuntime = findViewById(R.id.detail_tv_runtime);
        pbRuntimeLoadingIndicator = findViewById(R.id.detail_pb_runtime_loading_indicator);
        tvErrorMessage = findViewById(R.id.detail_tv_error_message_display);
        tvErrorMessage.setVisibility(View.INVISIBLE); //Initially
        rvTrailers = findViewById(R.id.detail_rv_trailers);
        pbTrailersLoadingIndicator = findViewById(R.id.detail_pb_trailers_loading_indicator);
        btFavOrUnfav = findViewById(R.id.detail_bt_fav_or_unfav);

        //Define an onClick listener for the favorize/unfavorize button.
        btFavOrUnfav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                tvErrorMessage.setVisibility(View.INVISIBLE); //Reset to invisible

                // Following code executes on the main thread after user presses button.
                // We are performing short database operations. In order not to make the code
                // too complicated this will be done on the main thread as well.
                if (btFavOrUnfav.getText().toString().equals(getString(R.string.favorize))) {

                    //This movie is not yet favorized.
                    //Store the favorized movie information in the database.
                    ContentValues values = new ContentValues();
                    values.put(MovieContract.FavoriteMovieEntry.COLUMN_TMDB_MOVIE_ID, movie.getTmdbId());
                    Uri uri = getContentResolver().insert(MovieContract.FavoriteMovieEntry.CONTENT_URI, values);

                    //If record successfully inserted, change the button text to indicate to unfavorize the movie,
                    //and change the button color.
                    if (! (uri == null) ) {
                        btFavOrUnfav.setText(R.string.unfavorize);
                        btFavOrUnfav.setBackgroundColor(getColor(R.color.colorUnfavorize));
                    }
                } else {
                    //Delete the unfavorized movie information from the favorite movies table.
                    String[] args = {Integer.toString(movie.getTmdbId())};
                    int nrRowsDeleted = getContentResolver().delete(
                            MovieContract.FavoriteMovieEntry.CONTENT_URI,
                            MovieContract.FavoriteMovieEntry.COLUMN_TMDB_MOVIE_ID + "=?",
                            args
                    );

                    //If record successfully deleted, change the button text to indicate to favorize the movie,
                    //and change the button color.
                    if (nrRowsDeleted > 0) {
                        btFavOrUnfav.setText(R.string.favorize);
                        btFavOrUnfav.setBackgroundColor(getColor(R.color.colorFavorize));
                    }
                }
            }
        });

        //Construct the layout manager depending on the device orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
            rvTrailers.setLayoutManager(mLinearLayoutManager);
        } else {
            GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, 3);
            rvTrailers.setLayoutManager(mGridLayoutManager);
        }

        //Use this setting to improve performance: changes in content do not
        //change the child layout size in the RecyclerView
        rvTrailers.setHasFixedSize(true);

        //Get the passed data from the Intent
        Intent intent = getIntent();
        movie = intent.getParcelableExtra(Constants.MOVIE_PARCEL);

        /**
         * In the next part all fields in the UI are filled.
         */

        //Set the title of the page equal to the movie title.
        setTitle(movie.getTitle());

        //Get the thumbnail image: should still be in cache of Picasso, hence no network call required.
        String url = Constants.TMDB_BASE_URL_IMAGES + Constants.TMDB_IMAGE__SIZE + movie.getPosterViewPath();
        Picasso.with(this).load(url).into(moviePosterView);

        //Fill some other fields.
        tvDetailTitle.append(movie.getTitle());
        tvVoteAverage.append(String.valueOf(movie.getVoteAverage()) + "/10");
        tvReleaseDate.append(movie.getReleaseDate());
        tvOverview.append(movie.getOverview());

        //Go get movie runtime.
        if ( movie.getRuntime() == Movie.RUNTIME_NONE ) {
            // Not yet obtained.
            new FetchRuntimeTask().execute(movie.getTmdbId());
        } else {
            // Just put it into the appropriate textfield, in minutes.
            tvRuntime.setText(Integer.toString(movie.getRuntime()) + "min");
        }

        //Find out whether movie has been favorized before. Query it from the database based
        //on the TMDB movie ID. My assumption is that that ID is unique over the entire TMDB.
        String[] movieId = {Integer.toString(movie.getTmdbId())};
        Cursor cursor = getContentResolver().query(MovieContract.FavoriteMovieEntry.CONTENT_URI,
                null,
                MovieContract.FavoriteMovieEntry.COLUMN_TMDB_MOVIE_ID + "=?",
                movieId,
                null,
                null);

        if (cursor != null && cursor.getCount() == 0 /* Movie not found in the favorite movies db, so not favorite */) {
            btFavOrUnfav.setText(R.string.favorize);
            btFavOrUnfav.setBackgroundColor(getColor(R.color.colorFavorize));
        } else {
            btFavOrUnfav.setText(R.string.unfavorize);
            btFavOrUnfav.setBackgroundColor(getColor(R.color.colorUnfavorize));
        }

        //Fetch trailer info
        if ( movie.getTrailersString().equals(Movie.MOVIE_TRAILERS_STRING_NONE) ) {
            //Not yet obtained, get them from TMDB.
            new FetchTrailersTask().execute(movie.getTmdbId());
        } else {
            //Just put the trailer information on the screen.
            //If the movie has no trailers, Movie.getTrailersArrayList() returns an array with appropriate text.
            ArrayList<String> movieTrailerList = movie.getTrailersArrayList();
            //Populate the recyclerview
            mAdapter = new MovieTrailerAdapter(MovieDetailActivity.this, movieTrailerList);
            rvTrailers.setAdapter(mAdapter);
        }

        //Detect swipe: on right swipe move to the main activity.
        LinearLayout mLinearLayout = findViewById(R.id.detail_main_layout);
        mLinearLayout.setOnTouchListener(new OnSwipeTouchListener(MovieDetailActivity.this) {
            public void onSwipeRight() {
                finish();
            }
        });
    }

    /**
     * This method implements an onClick interface of the movie trailers recyclerView:
     * the selected trailer will be played in YouTube.
     * If a movie has no traiers, there is a message on the screen informing the user about that,
     * and there is no clickable list entry.
    */
    @Override
    public void onMovieTrailerClick(int clickedPosition) {

        tvErrorMessage.setVisibility(View.INVISIBLE); //Reset to invisible

        //Only call YouTube if there is a network connection.
        if (NetworkUtils.isOnline()) {

            //Call YouTube in order to play the selected trailer
            ArrayList<String> trailers = movie.getTrailersArrayList();
            Intent intent = YouTubeIntents.
                    createPlayVideoIntentWithOptions(this,
                            trailers.get(clickedPosition),
                            false /*fullscreen*/,
                            true /* finishOnEnd*/);

            startActivity(intent);
        } else {
            tvErrorMessage.setText(Constants.ERROR_NO_INTERNET);
            tvErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This inner class fetches the movie runtime from TMDB
     * on an a-synchronous background thread.
     */
    public class FetchRuntimeTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Make runtime field invisible, and make loading indicator visible
            tvRuntime.setVisibility(View.INVISIBLE);
            pbRuntimeLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Integer... params) {

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

            //Get the TMBD movie ID
            int movieTmdbId = params[0];

            //Initiate the output
            int runtime = 0;

            //Build the URL string based on the TMDB movie ID
            URL TMDBRuntimeRequestUrl = NetworkUtils.buildUrl(Constants.TMDB_RUNTIME_URL(movieTmdbId));

            //Obtain a Jsonstring from the response, and parse it into a movielist.
            if (TMDBRuntimeRequestUrl != null) {

                try {
                    String jsonMovieResponse = NetworkUtils
                            .getResponseFromHttpUrl(TMDBRuntimeRequestUrl);

                    runtime = MovieJsonUtils
                            .getRuntimeFromJson(jsonMovieResponse);

                    movie.setRuntime(runtime);
                    Cursor cursor;

                    //Will hold the _ID value of the row. This is the key column in our SQL tables.
                    //That will be used to update the row.
                    int movie_ID;

                    ContentValues cv = MovieJsonUtils.getSingleMovieContentValues(movie);
                    Uri uri;
                    String[] movieTmdbIdArray = {Integer.toString(movie.getTmdbId())};

                    // Check if movie exists in original most popular table
                    cursor = getContentResolver().query(
                            MovieContract.MostPopularMovieEntry.CONTENT_URI,
                            null,
                            MovieContract.MostPopularMovieEntry.COLUMN_TMDB_MOVIE_ID + "=?",
                            movieTmdbIdArray,
                            null,
                            null);

                    //If it exists, update it with runtime information so that we don't have to retrieve it
                    //from TMDB next time. Movie runtime won't change so that is a valuable optimization.
                    if ( cursor != null && cursor.getCount() == 1 ) {
                        cursor.moveToNext();
                        movie_ID = cursor.getInt(cursor.getColumnIndex(MovieContract.MostPopularMovieEntry._ID));
                        uri = ContentUris.withAppendedId(MovieContract.MostPopularMovieEntry.CONTENT_URI, movie_ID);
                        int nrRowsUpdated = getContentResolver().update(
                                uri,
                                cv,
                                null,
                                null
                        );
                    }

                    // Check if movie exists in original top rated table
                    cursor = getContentResolver().query(
                            MovieContract.TopRatedMovieEntry.CONTENT_URI,
                            null,
                            MovieContract.TopRatedMovieEntry.COLUMN_TMDB_MOVIE_ID + "=?",
                            movieTmdbIdArray,
                            null,
                            null);

                    //If it exists, update it with runtime information so that we don't have to retrieve it
                    //from TMDB next time. Movie runtime won't change so that is a valuable optimization.
                    if ( cursor != null && cursor.getCount() == 1 ) {
                        cursor.moveToNext();
                        movie_ID = cursor.getInt(cursor.getColumnIndex(MovieContract.TopRatedMovieEntry._ID));
                        uri = ContentUris.withAppendedId(MovieContract.TopRatedMovieEntry.CONTENT_URI, movie_ID);
                        int nrRowsUpdated = getContentResolver().update(
                                uri,
                                cv,
                                null,
                                null
                        );
                    }

                    // Check if movie exists in working copy table
                    cursor = getContentResolver().query(
                            MovieContract.MovieEntry.CONTENT_URI,
                            null,
                            MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID + "=?",
                            movieTmdbIdArray,
                            null,
                            null);

                    //If it exists, update it with runtime information.
                    if ( cursor != null && cursor.getCount() == 1 ) {
                        cursor.moveToNext();
                        movie_ID = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry._ID));
                        uri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, movie_ID);
                        int nrRowsUpdated = getContentResolver().update(
                                uri,
                                cv,
                                null,
                                null
                        );
                    }
                } catch (FileNotFoundException e) {
                    errorMsg = Constants.ERROR_API_KEY_INVALID;
                } catch (IOException | JSONException e) {
                    errorMsg = Constants.ERROR_WHILE_RETRIEVING_DATA_FROM_TMBD;
                }
            } else {
                errorMsg = Constants.ERROR_WHILE_RETRIEVING_DATA_FROM_TMBD;
            }
            return runtime;
        }

        //Update the UI.
        @Override
        protected void onPostExecute(Integer runtime) {

            //Make runtime field visible, and make loading indicator invisible
            tvRuntime.setVisibility(View.VISIBLE);
            pbRuntimeLoadingIndicator.setVisibility(View.INVISIBLE);

            if (!Objects.equals(errorMsg, "") || runtime == 0) {
                //Do not display an error message, the user is not helped with that, in this case.
                //Just display a small message on red background color.
                tvRuntime.setBackgroundColor(Color.RED);
                tvRuntime.setText("Runtime unknown");
            } else {
                //Display runtime in minutes
                tvRuntime.setText(Integer.toString(runtime) + "min");
            }
        }
    }

    /**
     * This inner class fetches the movie trailer ID's from TMDB
     * on an a-synchronous background thread. After that YouTube can be used to play these trailers.
     */
    public class FetchTrailersTask extends AsyncTask<Integer, Void, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Make trailers RecyclerView invisible, and make loading indicator visible
            rvTrailers.setVisibility(View.INVISIBLE);
            pbTrailersLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<String> doInBackground(Integer... params) {

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

            //Get the TMBD movie ID
            int movieId = params[0];

            //Initiate the output
            ArrayList<String> trailerIds = new ArrayList<>();

            //Build the URL string based on the TMDB movie ID
            URL TMDBMovieTrailersRequestUrl = NetworkUtils.buildUrl(Constants.TMDB_TRAILERS_URL(movieId));

            //Obtain a Jsonstring from the response, and parse it into a movielist.
            if (TMDBMovieTrailersRequestUrl != null) {

                try {
                    String jsonMovieTrailersResponse = NetworkUtils
                            .getResponseFromHttpUrl(TMDBMovieTrailersRequestUrl);

                    trailerIds = MovieJsonUtils
                            .getMovieTrailerListFromJson(jsonMovieTrailersResponse);

                    //If the array has zero length, Movie.setTrailersStringFtomArrayList(ArrayList<String>)
                    //puts appropriate text in the movie instance.
                    movie.setTrailersStringFromArrayList(trailerIds);

                    Cursor cursor;
                    int movie_ID;
                    ContentValues cv = MovieJsonUtils.getSingleMovieContentValues(movie);
                    Uri uri;
                    String[] movieTmdbIdArray = {Integer.toString(movie.getTmdbId())};

                    // Most popular table
                    cursor = getContentResolver().query(
                            MovieContract.MostPopularMovieEntry.CONTENT_URI,
                            null,
                            MovieContract.MostPopularMovieEntry.COLUMN_TMDB_MOVIE_ID + "=?",
                            movieTmdbIdArray,
                            null,
                            null);
                    if ( cursor != null && cursor.getCount() == 1 ) {
                        cursor.moveToNext();
                        movie_ID = cursor.getInt(cursor.getColumnIndex(MovieContract.MostPopularMovieEntry._ID));
                        uri = ContentUris.withAppendedId(MovieContract.MostPopularMovieEntry.CONTENT_URI, movie_ID);
                        int nrRowsUpdated = getContentResolver().update(
                                uri,
                                cv,
                                null,
                                null
                        );
                    }

                    // Top rated table
                    cursor = getContentResolver().query(
                            MovieContract.TopRatedMovieEntry.CONTENT_URI,
                            null,
                            MovieContract.TopRatedMovieEntry.COLUMN_TMDB_MOVIE_ID + "=?",
                            movieTmdbIdArray,
                            null,
                            null);
                    if ( cursor != null && cursor.getCount() == 1 ) {
                        cursor.moveToNext();
                        movie_ID = cursor.getInt(cursor.getColumnIndex(MovieContract.TopRatedMovieEntry._ID));
                        uri = ContentUris.withAppendedId(MovieContract.TopRatedMovieEntry.CONTENT_URI, movie_ID);
                        int nrRowsUpdated = getContentResolver().update(
                                uri,
                                cv,
                                null,
                                null
                        );
                    }

                    // Working table
                    cursor = getContentResolver().query(
                            MovieContract.MovieEntry.CONTENT_URI,
                            null,
                            MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID + "=?",
                            movieTmdbIdArray,
                            null,
                            null);
                    if ( cursor != null && cursor.getCount() == 1 ) {
                        cursor.moveToNext();
                        movie_ID = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry._ID));
                        uri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, movie_ID);
                        int nrRowsUpdated = getContentResolver().update(
                                uri,
                                cv,
                                null,
                                null
                        );
                    }
                } catch (FileNotFoundException e) {
                    errorMsg = Constants.ERROR_API_KEY_INVALID;
                } catch (IOException | JSONException e) {
                    errorMsg = Constants.ERROR_WHILE_RETRIEVING_DATA_FROM_TMBD;
                }
            } else {
                errorMsg = Constants.ERROR_WHILE_RETRIEVING_DATA_FROM_TMBD;
            }
            return trailerIds;
        }

        @Override
        protected void onPostExecute(ArrayList<String> movieTrailerList) {

            ArrayList<String> mtList = new ArrayList<>();

            //Make trailers RecyclerView visible, and make loading indicator invisible
            rvTrailers.setBackgroundColor(Color.WHITE);
            rvTrailers.setVisibility(View.VISIBLE);
            pbTrailersLoadingIndicator.setVisibility(View.INVISIBLE);

            if (! errorMsg.isEmpty()) {
                rvTrailers.setBackgroundColor(Color.RED);
                mtList.add(Constants.NO_TRAILERS_FOUND);
            } else if (movieTrailerList.size() == 0) {
                mtList.add(Constants.MOVIE_HAS_NO_TRAILERS);
            } else {
                mtList = movieTrailerList;
            }

            //Populate the recyclerview
            mAdapter = new MovieTrailerAdapter(MovieDetailActivity.this, mtList);
            rvTrailers.setAdapter(mAdapter);
        }
    }

    //Inner class to handle swipes.
    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener (Context ctx){
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    }
                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }
    }
}
