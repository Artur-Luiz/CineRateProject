package app.cinerate.internal.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Esta classe armazena as informações de um filme avaliado pelo usuário.
 */
public final class Rating implements Serializable {

    @Serial
    private static final long serialVersionUID = -6674888521953146880L;

    private final int movieId;
    private String commentary;
    private int rating;
    private boolean favorite;
    private Status status;

    public Rating(
            int movieId,
            @Nullable String commentary,
            int rating,
            boolean favorite,
            @NotNull Status status
    ) {
        this.movieId = movieId;
        this.commentary = commentary;
        this.rating = rating;
        this.favorite = favorite;
        this.status = status;
    }


    public int getMovieId() {
        return movieId;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating1 = (Rating) o;
        return movieId == rating1.movieId && favorite == rating1.favorite && Objects.equals(commentary, rating1.commentary) && Objects.equals(rating, rating1.rating) && status == rating1.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, commentary, rating, favorite, status);
    }

    @Override
    public String toString() {
        return "Rating{" +
               "movieId=" + movieId +
               ", commentary='" + commentary + '\'' +
               ", rating=" + rating +
               ", favorite=" + favorite +
               ", status=" + status +
               '}';
    }

    public enum Status {

        PENDING,
        PLAN_TO_WATCH,
        COMPLETED

    }

}