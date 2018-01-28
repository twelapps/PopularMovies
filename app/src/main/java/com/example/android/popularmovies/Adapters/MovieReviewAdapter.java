package com.example.android.popularmovies.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.Utils.Constants;
import com.example.android.popularmovies.Utils.MovieReview;

import java.util.ArrayList;

/**
 * Created by twelh on 27/01/2018.
 */

public class MovieReviewAdapter extends RecyclerView.Adapter<MovieReviewAdapter.MovieReviewAdapterViewHolder> {

    private final ArrayList<MovieReview> reviews;

    //Constructor
    public MovieReviewAdapter(ArrayList<MovieReview> reviews) {
        this.reviews = reviews;
    }

    /*************************************
     *Create the inner ViewHolder class
     *************************************/
    public class MovieReviewAdapterViewHolder extends RecyclerView.ViewHolder {

        //Local variables
        private final TextView mMovieReview;
        private final TextView mMovieReviewAuthor;

        //Constructor
        public MovieReviewAdapterViewHolder(View view) {
            super(view);
            mMovieReview = view.findViewById(R.id.review_tv_review);
            mMovieReviewAuthor = view.findViewById(R.id.review_tv_review_author);
        }
    }

    @Override
    public MovieReviewAdapter.MovieReviewAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_review_listitem;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new MovieReviewAdapter.MovieReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieReviewAdapter.MovieReviewAdapterViewHolder holder, int position) {
        holder.mMovieReview.setText(reviews.get(position).getReview());
        holder.mMovieReviewAuthor.setText(reviews.get(position).getAuthor());
    }

    @Override
    public int getItemCount() {
        return this.reviews.size();
    }
}
