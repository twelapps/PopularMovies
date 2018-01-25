package com.example.android.popularmovies.Utils;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by twelh on 20/12/2017.
 */

@SuppressWarnings({"ALL", "DefaultFileTemplate"})
public class Movie implements Parcelable {

    //Mandatory CREATOR static for a parcelable object.
    @SuppressWarnings("unused")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    //Constants
    public static final int    SORT_TYPE_NONE                        = -1;
    public static final int    SORT_TYPE_MOST_POPULAR                = 1;
    public static final String[] SORT_TYPE_MOST_POPULAR_STRING_ARRAY = {Integer.toString(SORT_TYPE_MOST_POPULAR)};
    public static final int    SORT_TYPE_TOP_RATED                   = 2;
    public static final String[] SORT_TYPE_TOP_RATED_STRING_ARRAY    = {Integer.toString(SORT_TYPE_TOP_RATED)};
    public static final int    SORT_TYPE_FAVORIZED                   = 3;
    public static final String[] SORT_TYPE_FAVORIZED_STRING_ARRAY    = {Integer.toString(SORT_TYPE_FAVORIZED)};
    public static final int    TMDB_MOVIE_ID_NONE                    = -1;
    public static final double VOTE_AVERAGE_NONE                     = -1;
    public static final int    RUNTIME_NONE                          = -1;
    public static final int    FAVORIZED_FLAG_NONE                   = -1;
    public static final int    FAVORIZED_FLAG_FALSE                  = 1;
    public static final int    FAVORIZED_FLAG_TRUE                   = 2;
    public static final String MOVIE_TRAILERS_STRING_NONE            = "No movie trailers retrieved yet";
    public static final String MOVIE_TRAILERS_NO_TRAILERS            = Constants.MOVIE_HAS_NO_TRAILERS;
    private final String       JSON_KEY                              = "Trailers";

    //Properties
    private int sortType;
    private int tmdbId;
    private String title;
    private String posterViewPath;
    private String overview;
    private double voteAverage;
    private String releaseDate;
    private int runtime;
    private String trailersString;
    private int favorized_flag;

    //Constructor
    public Movie() {
        this.sortType       = SORT_TYPE_NONE;
        this.tmdbId         = TMDB_MOVIE_ID_NONE;
        this.title          = null;
        this.posterViewPath = null;
        this.overview       = null;
        this.voteAverage    = VOTE_AVERAGE_NONE;
        this.releaseDate    = null;
        this.runtime        = RUNTIME_NONE;
        this.favorized_flag = FAVORIZED_FLAG_NONE;

        /* For trailers we use a trick. Originally trailers is an array of strings. But that cannot be stored in
         * an SQL db, neither is it Parcelable.
         * Therefore we transform the String[] to a String via JSON. And when retrieved from the
         * db, it is transformed back to a String[] via JSON.
         */
        this.trailersString = MOVIE_TRAILERS_STRING_NONE;


    }

    public int getSortType() {
        return sortType;
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
    }

    public int getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterViewPath() {
        return posterViewPath;
    }

    public void setPosterViewPath(String posterViewPath) {
        this.posterViewPath = posterViewPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public int getFavorizedFlag() {
        return favorized_flag;
    }

    public void setFavorizedFlag(int favorized_flag) {
        this.favorized_flag = favorized_flag;
    }

    //When a Movie instance must be stored in the database
    public String getTrailersString() {
        return trailersString;
    }

    //When a Movie object must be displayed on the screen
    public ArrayList<String> getTrailersArrayList() {

        //Initiate output.
        ArrayList<String> trailersArrayList = new ArrayList<String>();

        if (! (this.trailersString.equals(this.MOVIE_TRAILERS_NO_TRAILERS))) {
            try {
                JSONObject trailersJsonObject = new JSONObject(this.trailersString);
                JSONArray trailersJsonArray = trailersJsonObject.optJSONArray(JSON_KEY);
                if (trailersJsonArray != null) {
                    for (int i=0;i<trailersJsonArray.length();i++){
                        trailersArrayList.add(trailersJsonArray.getString(i));
                    }
                }
            }
            catch (JSONException e) { e.printStackTrace(); }
        } else {
            trailersArrayList.add(this.MOVIE_TRAILERS_NO_TRAILERS);
        }

        return trailersArrayList;
    }

    //When an array must be stored into the database or must be used for Parceling
    public void setTrailersStringFromArrayList (ArrayList<String> trailersArrayList) {

        if (trailersArrayList.size() > 0) {

            JSONArray trailersJsonArray = new JSONArray();
            for (int i = 0; i < trailersArrayList.size(); i++) {
                trailersJsonArray.put(trailersArrayList.get(i));
            }
            JSONObject trailersJsonObject = new JSONObject();
            try {
                trailersJsonObject.put(JSON_KEY, trailersJsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            this.trailersString = trailersJsonObject.toString();
        } else {
            this.trailersString = this.MOVIE_TRAILERS_NO_TRAILERS;
        }
    }

    //When it is read from the database and must be stored into a Movie instance
    public void setTrailersStringFromString (String trailersString) {
        this.trailersString = trailersString;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "sortType='"          + sortType       + "\'" +
                ", tmdbId='"          + tmdbId         + "\'" +
                ", title='"           + title          + "\'" +
                ", posterView_path='" + posterViewPath + "\'" +
                ", overview='"        + overview       + "\'" +
                ", voteAverage='"     + voteAverage    + "\'" +
                ", releaseDate='"     + releaseDate    + "\'" +
                ", runtime='"         + runtime        + "\'" +
                ", favorite='"        + favorized_flag + "\'" +
                ", trailersString='"  + trailersString + "\'" +
                '}';
    }

    //Parcelling part
    private Movie(Parcel in) {
        this.sortType = in.readInt();
        this.tmdbId = in.readInt();
        this.title = in.readString();
        this.posterViewPath = in.readString();
        this.overview = in.readString();
        this.voteAverage = in.readDouble();
        this.releaseDate = in.readString();
        this.runtime = in.readInt();
        this.favorized_flag = in.readInt();
        this.trailersString = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(this.sortType);
        dest.writeInt(this.tmdbId);
        dest.writeString(this.title);
        dest.writeString(this.posterViewPath);
        dest.writeString(this.overview);
        dest.writeDouble(this.voteAverage);
        dest.writeString(this.releaseDate);
        dest.writeInt(this.runtime);
        dest.writeInt(this.favorized_flag);
        dest.writeString(this.trailersString);
    }
}
