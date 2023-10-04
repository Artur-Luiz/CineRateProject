package app.cinerate.internal.movie;

import info.movito.themoviedbapi.TmdbApi;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MovieAPIProvider {

    private static final String API_KEY = "90912b17d629a689803603b12d631c08";

    private TmdbApi tmdb = new TmdbApi(API_KEY);

    @Bean
    public TmdbApi provideTmdb() {
        return tmdb;
    }

}
