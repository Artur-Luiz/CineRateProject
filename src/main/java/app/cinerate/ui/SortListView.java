package app.cinerate.ui;

import app.cinerate.internal.movie.MovieService;
import app.cinerate.internal.user.User;
import app.cinerate.internal.user.UserDAO;
import app.cinerate.ui.components.MovieListScrollerComponent;
import app.cinerate.ui.layout.ToolBarLayout;
import com.vaadin.flow.component.ScrollOptions;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "/list", layout = ToolBarLayout.class)
@PermitAll
public class SortListView extends VerticalLayout {

    private transient ComboBox<Sort> sortField;


    private final transient VerticalLayout sortedMoviesLayout = new VerticalLayout();

    private final transient UserDAO userDAO;
    private final transient AuthenticationContext authenticationContext;

    private final transient MovieService movieService;

    public SortListView(UserDAO userDAO, AuthenticationContext authenticationContext, MovieService movieService) {
        this.userDAO = userDAO;
        this.authenticationContext = authenticationContext;
        this.movieService = movieService;
        setSizeFull();

        renderPage();
    }

    private void renderPage() {
        sortField = new ComboBox<>("Organizar Por", List.of(Sort.values()));
        sortField.setWidth("300px");
        sortField.setItemLabelGenerator(Sort::getDisplayName);
        sortField.setPrefixComponent(VaadinIcon.SORT.create());
        sortField.setAllowCustomValue(false);

        setAlignItems(Alignment.CENTER);

        sortField.setValue(Sort.POPULAR);

        updateMovie();

        sortField.addValueChangeListener(event -> updateMovie());

        sortedMoviesLayout.setAlignItems(Alignment.CENTER);


        add(sortField, sortedMoviesLayout);
    }


    private void updateMovie() {

        sortedMoviesLayout.removeAll();

        var user = authenticationContext.getAuthenticatedUser(User.class).orElseThrow();

        var movieScroll = new MovieListScrollerComponent(userDAO, user, true);

        switch (sortField.getValue()) {
            case RELEASE -> {
                movieScroll.updateMovies(movieService.searchUpcomingMovies(1));
                movieScroll.onPageChangeListener(newPage -> {
                    sortField.scrollIntoView(new ScrollOptions(ScrollOptions.Behavior.SMOOTH));
                    var movies = movieService.searchUpcomingMovies(newPage);
                    movieScroll.updateMovies(movies);
                });
            }
            case RATING -> {
                movieScroll.updateMovies(movieService.searchTopRatedMovies(1));
                movieScroll.onPageChangeListener(newPage -> {
                    sortField.scrollIntoView(new ScrollOptions(ScrollOptions.Behavior.SMOOTH));
                    var movies = movieService.searchTopRatedMovies(newPage);
                    movieScroll.updateMovies(movies);
                });
            }
            default -> {
                movieScroll.updateMovies(movieService.searchPopularMovies(1));
                movieScroll.onPageChangeListener(newPage -> {
                    sortField.scrollIntoView(new ScrollOptions(ScrollOptions.Behavior.SMOOTH));
                    var movies = movieService.searchPopularMovies(newPage);
                    movieScroll.updateMovies(movies);
                });
            }
        }
        sortedMoviesLayout.add(movieScroll);

        sortField.scrollIntoView(new ScrollOptions(ScrollOptions.Behavior.SMOOTH));

    }


    private enum Sort {
        POPULAR("Popular"),
        RELEASE("Lançamento"),
        RATING("Avaliação");

        private final String displayName;

        Sort(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }


}
