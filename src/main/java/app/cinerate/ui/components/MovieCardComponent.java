package app.cinerate.ui.components;

import app.cinerate.internal.movie.util.MovieImageHelper;
import app.cinerate.internal.user.Rating;
import app.cinerate.internal.user.User;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import info.movito.themoviedbapi.model.MovieDb;

@StyleSheet("https://fonts.googleapis.com/css2?family=Aldrich&family=Open+Sans:ital,wght@0,300;0,400;0,500;0,600;0,700;0,800;1,300;1,400;1,500;1,600;1,700;1,800&display=swap")
public class MovieCardComponent extends VerticalLayout {

    public MovieCardComponent(MovieDb movie, User user) {

        Image poster = new Image();

        if (movie.getPosterPath() == null) {
            poster.setSrc("https://www.publicdomainpictures.net/pictures/280000/velka/not-found-image-15383864787lu.jpg");
        } else {
            poster.setSrc(MovieImageHelper.getAsW500URL(movie.getPosterPath()));
        }

        poster.addClassName("moviecard-poster");
        poster.setHeightFull();

        var status = new HorizontalLayout();
        status.addClassName("moviecard-status");
        status.setWidthFull();
        var verticalLayout = new VerticalLayout();

        Span title = new Span();
        title.addClassName("moviecard-title");
        title.setText(movie.getTitle());

        var ratingUser = user.getRating(movie);

        verticalLayout.add(title);
        verticalLayout.addClassName("moviecard-title-layout");

        var ratingValue = Math.max(0.0, ratingUser != null ? ratingUser.getRating() : 0) / 2.0;

        var rateLayout = new HorizontalLayout();
        for (int i = 0; i < 5; i++) {
            Icon icon;
            if (ratingValue >= i + 1) {
                icon = VaadinIcon.STAR.create();
            } else if (ratingValue >= i + 0.5) {
                icon = VaadinIcon.STAR_HALF_LEFT_O.create();
            } else {
                icon = VaadinIcon.STAR_O.create();
            }

            icon.getStyle().set("margin", "0");

            rateLayout.add(icon);
        }
        rateLayout.addClassName("moviecard-rate");
        verticalLayout.add(rateLayout);

        add(status, poster, title, rateLayout);

        var spanToEditMovie = new Span();

        if (ratingUser != null && ratingUser.isFavorite()) {
            getStyle().set("border", "1px solid #FFD700");
            getStyle().set("box-shadow", "2px 2px 12px #FFD700");
            status.getStyle().set("background-color", "#FFD700");
            status.add(new Span("FAVORITO"));
        } else {
            switch (ratingUser != null ? ratingUser.getStatus() : Rating.Status.PENDING) {
                case COMPLETED:
                    getStyle().set("border", "1px solid green");
                    getStyle().set("box-shadow", "2px 2px 12px green");
                    status.getStyle().set("background-color", "green");
                    status.add(new Span("ASSITIDO"));
                    break;
                case PLAN_TO_WATCH:
                    getStyle().set("border", "1px solid #FFFB0D");
                    getStyle().set("box-shadow", "2px 2px 12px #FFFB0D");
                    status.getStyle().set("background-color", "#FFFB0D");
                    status.add(new Span("PLANEJA ASSISTIR"));
                    break;
                case PENDING:
                    getStyle().set("border", "1px solid gray");
                    getStyle().set("box-shadow", "2px 2px 12px gray");
                    status.getStyle().set("background-color", "gray");
                    status.add(new Span("PENDENTE"));
                    break;
            }
        }

        add(spanToEditMovie);
        addClassName("moviecard");
        setBoxSizing(BoxSizing.BORDER_BOX);
        setSpacing(false);
    }
}
