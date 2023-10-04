package app.cinerate.ui;

import app.cinerate.internal.movie.MovieService;
import app.cinerate.internal.user.User;
import app.cinerate.internal.user.UserDAO;
import app.cinerate.ui.components.MovieListScrollerComponent;
import app.cinerate.ui.layout.ToolBarLayout;
import com.vaadin.flow.component.ScrollOptions;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

@Route(value = "/search", layout = ToolBarLayout.class)
@PermitAll
public class SearchView extends VerticalLayout {

    private final transient UserDAO userDAO;
    private final transient AuthenticationContext authenticationContext;

    private final transient MovieService movieService;

    private final transient VerticalLayout searchLayout = new VerticalLayout();

    public SearchView(UserDAO userDAO, AuthenticationContext authenticationContext, MovieService movieService) {
        this.userDAO = userDAO;
        this.authenticationContext = authenticationContext;
        this.movieService = movieService;

        renderPage();
    }

    private void renderPage() {
        TextField searchField;
        setSizeFull();

        setAlignItems(Alignment.CENTER);

        searchField = new TextField("Procurar filme");
        searchField.setClearButtonVisible(true);
        searchField.setPlaceholder("Digite o nome do filme");
        searchField.setWidth("250px");

        searchLayout.setAlignItems(Alignment.CENTER);

        searchField.addValueChangeListener(event -> {
            searchField.scrollIntoView(new ScrollOptions(ScrollOptions.Behavior.SMOOTH));
            setupMovies(event.getValue().trim(), searchField);
        });

        add(searchField, searchLayout);
    }

    private void setupMovies(String keyword, TextField searchField) {
        searchLayout.removeAll();
        if (keyword.isBlank()) {
            notSearchAnyMovie();
            return;
        }

        var movies = movieService.searchMovieByName(keyword, 1);
        if (movies.isEmpty()) {
            notFoundAnyMovieInCurrentPage();
            return;
        }

        var user = authenticationContext.getAuthenticatedUser(User.class).orElseThrow();

        setSizeUndefined();
        var moviesLayout = new MovieListScrollerComponent(userDAO, user, true);

        moviesLayout.updateMovies(movies);

        moviesLayout.onPageChangeListener(newPage -> {
            searchField.scrollIntoView(new ScrollOptions(ScrollOptions.Behavior.SMOOTH));

            var newMovies = movieService.searchMovieByName(keyword, newPage);

            setSizeUndefined();
            moviesLayout.updateMovies(newMovies);
        });

        searchLayout.add(moviesLayout);
    }

    private void notSearchAnyMovie() {
        searchLayout.setSizeFull();

        searchLayout.add(
                new H2("Escreva algo para pesquisar"),
                new Span("Para pesquisar, digite o nome do filme no campo de pesquisa")
        );
    }

    private void notFoundAnyMovieInCurrentPage() {
        searchLayout.setSizeFull();
        searchLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        searchLayout.add(
                new H2("Não encontramos nenhum filme :(")
        );
    }


}
