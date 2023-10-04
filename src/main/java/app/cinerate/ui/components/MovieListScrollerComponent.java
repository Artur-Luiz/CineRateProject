package app.cinerate.ui.components;

import app.cinerate.internal.user.User;
import app.cinerate.internal.user.UserDAO;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import info.movito.themoviedbapi.model.MovieDb;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MovieListScrollerComponent extends VerticalLayout {

    private final transient FlexLayout flexLayout;

    private final transient UserDAO userDAO;

    private final transient User user;

    private transient PaginatorComponent paginator = null;

    private transient Consumer<Integer> consumer = null;

    private List<MovieDb> movies = new ArrayList<>();


    public MovieListScrollerComponent(UserDAO userDAO, User user, boolean paginated) {
        this.userDAO = userDAO;
        this.user = user;

        var verticalLayout = new VerticalLayout();
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);


        flexLayout = new FlexLayout();
        flexLayout.setFlexDirection(FlexLayout.FlexDirection.ROW);
        flexLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        flexLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        flexLayout.setAlignItems(FlexComponent.Alignment.CENTER);


        if (paginated) {
            paginator = new PaginatorComponent();
            paginator.onPageChangeListener(newPage -> {
                if (consumer != null)
                    consumer.accept(newPage);
            });
            paginator.setMargin(true);


            verticalLayout.add(flexLayout, paginator);
        } else {
            verticalLayout.add(flexLayout);
        }

        add(verticalLayout);
    }

    public void updateMovies(List<MovieDb> movies) {
        this.movies = movies;


        flexLayout.removeAll();

        if (movies.isEmpty()) {
            var span = new Span("Nenhum filme foi encontrado nesta página :(");
            flexLayout.add(span);
            return;
        }

        movies.forEach(movie -> {
                    var card = new MovieCardComponent(movie, user);
                    card.addClickListener(event -> showMovieDialog(movie));
                    card.setMargin(true);
                    flexLayout.add(card);
                }
        );
    }

    private void showMovieDialog(MovieDb movie) {
        MovieDialog dialog = new MovieDialog(userDAO, user, movie, user.getOrCreateRating(movie));

        dialog.onSuccessfulSave(() -> updateMovies(movies));

        dialog.open();
    }


    public void onPageChangeListener(Consumer<Integer> consumer) {
        this.consumer = consumer;
    }


}
