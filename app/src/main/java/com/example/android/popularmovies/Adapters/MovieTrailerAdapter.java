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

import java.util.ArrayList;

/**
 * Created by twelh on 18/01/2018.
 */

public class MovieTrailerAdapter extends RecyclerView.Adapter<MovieTrailerAdapter.MovieTrailerAdapterViewHolder> {

    private final ArrayList<String> trailers;
    private final MovieTrailerClickListener mMovieTrailerClickListener;

    //Constructor
    public MovieTrailerAdapter(MovieTrailerClickListener movieTrailerClickListener, ArrayList<String> trailers) {
        this.mMovieTrailerClickListener = movieTrailerClickListener;
        this.trailers = trailers;
    }

    /*************************************
     *Create the inner ViewHolder class
     *************************************/
    public class MovieTrailerAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        //Local variables
        private final TextView mMovieTrailerNumber;
        private final ImageView mMovieTrailerPlaySymbol;

        //Constructor
        public MovieTrailerAdapterViewHolder(View view) {
            super(view);
            mMovieTrailerNumber = view.findViewById(R.id.trailer_tv_trailer_number);
            mMovieTrailerPlaySymbol = view.findViewById(R.id.trailer_iv_play_trailer);

            //Click listener
            view.setOnClickListener(this);
        }

        /**
         * Called whenever a user clicks on an item in the list.
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();

            //And pass it to the click listener interface
            mMovieTrailerClickListener.onMovieTrailerClick(clickedPosition);
        }
    }

    @Override
    public MovieTrailerAdapter.MovieTrailerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_trailer_listitem;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new MovieTrailerAdapter.MovieTrailerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieTrailerAdapter.MovieTrailerAdapterViewHolder holder, int position) {
        if (position == 0 && trailers.size() == 1 && trailers.get(position).equals(Constants.NO_TRAILERS_FOUND)) {
            holder.mMovieTrailerNumber.setText(Constants.NO_TRAILERS_FOUND);
            holder.mMovieTrailerPlaySymbol.setVisibility(View.INVISIBLE);
        } else if (position == 0 && trailers.size() == 1 && trailers.get(position).equals(Constants.MOVIE_HAS_NO_TRAILERS)) {
            holder.mMovieTrailerNumber.setText(Constants.MOVIE_HAS_NO_TRAILERS);
            holder.mMovieTrailerPlaySymbol.setVisibility(View.INVISIBLE);
        } else {
            holder.mMovieTrailerNumber.setText("Trailer " + Integer.toString(position + 1));
        }
    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieTrailerClickListener {
        void onMovieTrailerClick(int clickedPosition);
    }
}
