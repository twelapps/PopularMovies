package com.example.android.popularmovies.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.Utils.Constants;
import com.example.android.popularmovies.Utils.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by twelh on 20/12/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    //Local variables
    ArrayList<Movie> movieList; //Datamodel, passed by the Activity in the constructor or in a separate method when updated
    final MovieClickListener mMovieClickListener; //Local instance of the interface.

    //Constructor
    public MovieAdapter(MovieClickListener movieClickListener, ArrayList<Movie> movieList) {
        this.movieList = movieList;
        this.mMovieClickListener = movieClickListener;
    }

    /*************************************
     *Create the inner ViewHolder class
     *************************************/
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder
                                        implements View.OnClickListener {

        //Local variable
        private ImageView mMoviePosterImageView;

        //Constructor
        public MovieAdapterViewHolder(View view) {
            super(view);
            mMoviePosterImageView = view.findViewById(R.id.iv_movie_poster);

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
            Movie movie = movieList.get(clickedPosition);
            mMovieClickListener.onMovieClick(clickedPosition, movie);
        }
    }
    /**********************************************
     *End of "Create the inner ViewHolder class"
     **********************************************/

    /**
     * "MovieAdapterViewHolder onCreateViewHolder" gets called when each new ViewHolder is created.
     * This happens when the RecyclerView is laid out.
     * Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent    The parent ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new MovieAdapterViewHolder that holds the View for each list item
     */
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForGridItem = R.layout.movie_griditem;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForGridItem, parent, shouldAttachToParentImmediately);
        return new MovieAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the movie
     * poster for this particular position, using the "position" argument that is conveniently
     * passed into us. We use the Picasso library for this purpose.
     *
     * @param holder                    The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(MovieAdapter.MovieAdapterViewHolder holder, int position) {

        Context context = holder.mMoviePosterImageView.getContext();
        String url = Constants.TMDB_BASE_URL_IMAGES + Constants.TMDB_IMAGE__SIZE +
                movieList.get(position).getPosterViewPath();
        Picasso.with(context).load(url).into(holder.mMoviePosterImageView);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of movies available in our datamodel
     */
    @Override
    public int getItemCount() {
        if (movieList == null) {return 0;}
        return movieList.size();
    }

    /**
     * This method handles the updated datamodel: the local copy of the datamodel is also updated
     * and the view is refreshed.
     *
     * @param updatedMovieList  Reflects the updated datamodel
     */
    public void setMovieData(ArrayList<Movie> updatedMovieList) {
        this.movieList = updatedMovieList;
        notifyDataSetChanged();
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieClickListener {
        void onMovieClick(int clickedMovieIndex, Movie movie);
    }
}
