package com.example.android.popularmovies.Utils;

/**
 * Created by twelh on 27/01/2018.
 */

public class MovieReview {
    public String author;
    public String review;

    public MovieReview(String author, String review) {
        this.author = author;
        this.review = review;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
