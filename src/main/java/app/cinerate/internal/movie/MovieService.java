package app.cinerate.internal.movie;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final TmdbApi tmdb;

    @Autowired
    public MovieService(TmdbApi tmdb) {
        this.tmdb = tmdb;
    }

    public List<MovieDb> searchMovieByName(String keywords, int page) {
        return tmdb.getSearch()
                .searchMovie(keywords, null, "pt-BR", false /* family friendly */, page)
                .getResults();
    }

    public List<MovieDb> searchTopRatedMovies(int page) {
        return tmdb.getMovies().getTopRatedMovies("pt-BR", page).getResults();
    }

    public List<MovieDb> searchPopularMovies(int page) {
        return tmdb.getMovies().getPopularMovies("pt-BR", page).getResults();
    }

    public List<MovieDb> searchUpcomingMovies(int page) {
        return tmdb.getMovies().getUpcoming("pt-BR", page, "BR").getResults();
    }

    public List<MovieDb> searchMovieByGenre(Genre genre, int page) {
        return tmdb.getGenre().getGenreMovies(genre.getId(), "pt-BR", page, false).getResults();
    }

    public MovieDb findMovieById(int id) {
        return tmdb.getMovies().getMovie(id, "pt-BR", TmdbMovies.MovieMethod.images, TmdbMovies.MovieMethod.videos);
    }

    public List<Genre> getGenres() {
        return tmdb.getGenre().getGenreList("pt-BR").stream().toList();
    }

}
