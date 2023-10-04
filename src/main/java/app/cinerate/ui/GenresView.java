package app.cinerate.ui;

import app.cinerate.internal.movie.MovieService;
import app.cinerate.internal.user.User;
import app.cinerate.internal.user.UserDAO;
import app.cinerate.ui.layout.ToolBarLayout;
import app.cinerate.ui.components.MovieListScrollerComponent;
import com.vaadin.flow.component.ScrollOptions;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import info.movito.themoviedbapi.model.Genre;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

@Route(value = "/genres/:page?", layout = ToolBarLayout.class)
@PermitAll
public class GenresView extends VerticalLayout {

    private final transient Map<String, Genre> genres = new HashMap<>();

    private final transient UserDAO userDAO;
    private final transient AuthenticationContext authenticationContext;

    private final transient MovieService movieService;

    public GenresView(UserDAO userDAO, AuthenticationContext authenticationContext, MovieService movieService) {
        this.userDAO = userDAO;
        this.authenticationContext = authenticationContext;
        this.movieService = movieService;

        renderPage();
    }

    private void renderPage() {
        setSizeFull();

        List<Tab> tabList = new ArrayList<>();
        movieService.getGenres().forEach(genre -> {
            genres.put(genre.getName(), genre);
            tabList.add(new Tab(genre.getName()));
        });

        var tabs = new Tabs(tabList.toArray(new Tab[0]));
        tabs.addThemeVariants(TabsVariant.LUMO_CENTERED);
        tabs.setWidthFull();

        var user = authenticationContext.getAuthenticatedUser(User.class).orElseThrow();

        var moviesLayout = new MovieListScrollerComponent(userDAO, user, true);

        IntConsumer updateMovieLayout = page -> {
            tabs.scrollIntoView(new ScrollOptions(ScrollOptions.Behavior.SMOOTH));
            moviesLayout.updateMovies(movieService.searchMovieByGenre(genres.get(tabs.getSelectedTab().getLabel()), page));
        };

        updateMovieLayout.accept(1);

        tabs.addSelectedChangeListener(event -> updateMovieLayout.accept(1));

        moviesLayout.onPageChangeListener(updateMovieLayout::accept);

        add(tabs, moviesLayout);
    }


}
