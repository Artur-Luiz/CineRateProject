package app.cinerate.ui;

import app.cinerate.internal.movie.MovieService;
import app.cinerate.internal.user.Rating;
import app.cinerate.internal.user.User;
import app.cinerate.internal.user.UserDAO;
import app.cinerate.ui.components.MovieListScrollerComponent;
import app.cinerate.ui.layout.ToolBarLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.security.AuthenticationContext;
import info.movito.themoviedbapi.model.MovieDb;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Route(value = "/my-movies", layout = ToolBarLayout.class)
@RouteAlias(value = "", layout = ToolBarLayout.class)
@PermitAll
public class MyMoviesView extends VerticalLayout {

    private final transient MovieService movieService;

    private final transient UserDAO userDAO;
    private final transient AuthenticationContext authenticationContext;

    private final transient VerticalLayout sortedMoviesLayout = new VerticalLayout();

    private transient ComboBox<Sort> sortField;

    public MyMoviesView(UserDAO userDAO, AuthenticationContext authenticationContext, MovieService movieService) {
        this.userDAO = userDAO;
        this.authenticationContext = authenticationContext;
        this.movieService = movieService;

        renderPage();
    }

    private void renderPage() {
        setSizeFull();

        sortField = new ComboBox<>("Organizar Por", Sort.values());
        sortField.setWidth("300px");
        sortField.setPrefixComponent(VaadinIcon.SORT.create());
        sortField.setItemLabelGenerator(Sort::getDisplayName);
        sortField.setAllowCustomValue(false);

        setAlignItems(Alignment.CENTER);

        sortField.setValue(Sort.ALL);

        updateMovie();

        sortField.addValueChangeListener(event -> updateMovie());

        sortedMoviesLayout.setAlignItems(Alignment.CENTER);

        add(sortField, sortedMoviesLayout);
    }


    private void updateMovie() {

        sortedMoviesLayout.removeAll();

        var user = authenticationContext.getAuthenticatedUser(User.class).orElseThrow();

        var movieScroll = new MovieListScrollerComponent(userDAO, user, false);

        // primeiro filtro, onde nao queremos os filmes pendentes
        var userMovies = user.getRatings().stream()
                .filter(rating -> rating.getStatus() != Rating.Status.PENDING);

        Stream<Rating> ratingMovies = switch (sortField.getValue()) {
            case FAVORITES -> userMovies.filter(Rating::isFavorite);
            case COMPLETED -> userMovies.filter(rating -> rating.getStatus() == Rating.Status.COMPLETED);
            case PLAN_TO_WATCH -> userMovies.filter(rating -> rating.getStatus() == Rating.Status.PLAN_TO_WATCH);
            default -> userMovies;
        };

        // todos os filmes que o usuario tem
        // a função parallel é usada para que a busca dos filmes seja feita em paralelo
        List<MovieDb> movies = ratingMovies.parallel()
                .map(rating -> movieService.findMovieById(rating.getMovieId()))
                .filter(Objects::nonNull)
                .toList();

        movieScroll.updateMovies(movies);

        sortedMoviesLayout.add(movieScroll);

    }

    private enum Sort {
        ALL("Todos"),
        FAVORITES("Favoritos"),
        COMPLETED("Completos"),
        PLAN_TO_WATCH("Assistir Depois");

        private final String displayName;

        Sort(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

}
