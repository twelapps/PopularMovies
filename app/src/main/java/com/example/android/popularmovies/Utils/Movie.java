package com.example.android.popularmovies.Utils;

import android.os.Parcel;
import android.os.Parcelable;

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

    //Properties
    private int id;
    private String title;
    private String posterViewPath;
    private String overview;
    private double voteAverage;
    private String releaseDate;

    //Constructor
    public Movie(/*ImageView posterView,*/) {
        this.id = Constants.NO_MOVIE_ID;
        this.title = null;
        this.posterViewPath = null;
        this.overview = null;
        this.voteAverage = (double) 0;
        this.releaseDate = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", posterView_path='" + posterViewPath + '\'' +
                ", overview='" + overview + '\'' +
                ", voteAverage=" + voteAverage +
                ", releaseDate='" + releaseDate + '\'' +
                '}';
    }

    //Parcelling part
    private Movie(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.posterViewPath = in.readString();
        this.overview = in.readString();
        this.voteAverage = in.readDouble();
        this.releaseDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(posterViewPath);
        dest.writeString(overview);
        dest.writeDouble(voteAverage);
        dest.writeString(releaseDate);
    }
}
