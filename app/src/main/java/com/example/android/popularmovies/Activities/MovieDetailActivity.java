package com.example.android.popularmovies.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.Utils.Constants;
import com.example.android.popularmovies.Utils.Movie;
import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    //Local variables.
    ImageView moviePosterView;
    TextView tvDetailTitle;
    TextView tvVoteAverage;
    TextView tvReleaseDate;
    TextView tvOverview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        //Connect to the UI elements.
        moviePosterView = findViewById(R.id.iv_detail_movie_poster);
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvVoteAverage = findViewById(R.id.tv_vote_average);
        tvReleaseDate = findViewById(R.id.tv_release_date);
        tvOverview = findViewById(R.id.tv_overview);

        //Get the passed data from the Intent
        Intent intent = getIntent();
        Movie movie = intent.getParcelableExtra(Constants.MOVIE_PARCEL);

        //Set the title of the page equal to the movie title.
        setTitle(movie.getTitle());

        //Get the thumbnail image: should still be in cache of Picasso, hence no network call required.
        String url = Constants.TMDB_BASE_URL_IMAGES + Constants.TMDB_IMAGE__SIZE + movie.getPosterViewPath();
        Picasso.with(this).load(url).into(moviePosterView);

        //Fill the other fields.
        tvDetailTitle.append(movie.getTitle());
        tvVoteAverage.append(String.valueOf(movie.getVoteAverage()) + "/10");
        tvReleaseDate.append(movie.getReleaseDate());
        tvOverview.append(movie.getOverview());
    }
}
