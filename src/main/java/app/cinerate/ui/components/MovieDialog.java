package app.cinerate.ui.components;

import app.cinerate.internal.movie.util.MovieImageHelper;
import app.cinerate.internal.user.Rating;
import app.cinerate.internal.user.User;
import app.cinerate.internal.user.UserDAO;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import info.movito.themoviedbapi.model.MovieDb;

import java.util.Objects;
import java.util.function.Consumer;

@StyleSheet("https://fonts.googleapis.com/css2?family=Aldrich&family=Open+Sans:ital,wght@0,300;0,400;0,500;0,600;0,700;0,800;1,300;1,400;1,500;1,600;1,700;1,800&display=swap")
public class MovieDialog extends Dialog {

    private final transient UserDAO userDAO;
    private final transient User user;
    private final transient MovieDb movie;

    private final transient Rating rating;

    private transient Runnable successCallback;

    public MovieDialog(UserDAO userDAO, User user, MovieDb movie, Rating rating) {
        this.userDAO = userDAO;
        this.user = user;
        this.movie = movie;
        this.rating = rating;

        renderMovieInformation();
    }

    public void renderMovieInformation() {
        removeAll();

        VerticalLayout movieInformation = new VerticalLayout();
        movieInformation.addClassName("movie-dialog-content");

        Image movieImage = new Image();
        if (movie.getPosterPath() == null)
            movieImage.setSrc("https://www.publicdomainpictures.net/pictures/280000/velka/not-found-image-15383864787lu.jpg");
        else
            movieImage.setSrc(MovieImageHelper.getAsW500URL(movie.getPosterPath()));
        movieImage.addClassName("movie-dialog-image");

        Paragraph overview = new Paragraph(movie.getOverview());

        Paragraph review = new Paragraph("Review: " + rating.getCommentary());
        Span title = new Span(movie.getTitle());
        title.addClassName("movie-dialog-title");
        VerticalLayout info = new VerticalLayout(new Span(title), overview);
        info.setAlignItems(FlexComponent.Alignment.CENTER);
        info.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        HorizontalLayout header = new HorizontalLayout(movieImage, info);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        movieInformation.add(header);

        if (rating.getStatus() == Rating.Status.COMPLETED) {
            VerticalLayout reviewTitleLayout = new VerticalLayout(new H2("REVIEW"),createRatingLayout());
            reviewTitleLayout.addClassName("movie-dialog-review-title");
            VerticalLayout reviewLayout = new VerticalLayout(reviewTitleLayout);
            reviewLayout.addClassName("movie-dialog-review");
            if (rating.getCommentary() != null && !rating.getCommentary().isBlank()){
                reviewLayout.add(review);
            }
            movieInformation.add(reviewLayout);
        }

        add(movieInformation, ratingForm());
        addClassName("movie-dialog");
    }

    private FormLayout ratingForm() {

        var formLayout = new FormLayout();

        var binder = new Binder<Rating>();

        var ratingField = new NumberField("Avaliação");
        ratingField.setValue(Math.max(0.0, rating.getRating()));
        ratingField.setMin(0);
        ratingField.setMax(10);
        ratingField.setStep(1.0);

        var statusField = new ComboBox<>("Status do Filme", Rating.Status.values());
        statusField.setValue(rating.getStatus());
        statusField.setItemLabelGenerator(item -> switch (item) {
            case PENDING -> "Nunca Assistido";
            case PLAN_TO_WATCH -> "Planejo Assistir";
            case COMPLETED -> "Assistido";
        });


        binder.forField(ratingField)
                .withConverter(new Converter<Double, Integer>() {
                    @Override
                    public Result<Integer> convertToModel(Double fieldValue, ValueContext context) {
                        if (fieldValue == null) {
                            return Result.ok(null);
                        }
                        int intValue = fieldValue.intValue();
                        if (intValue < 0 || intValue > 10) {
                            return Result.error("Avaliação deve estar entre 0 e 10");
                        }
                        return Result.ok(intValue);
                    }

                    @Override
                    public Double convertToPresentation(Integer modelValue, ValueContext context) {
                        return modelValue == null ? null : modelValue.doubleValue();
                    }
                })
                .withValidator(value -> value != null && value >= 0 && value <= 10, "Avaliação deve estar entre 0 e 10")
                .bind(Rating::getRating, (r, value) -> r.setRating(Objects.requireNonNullElse(value, -1)));


        var commentaryField = new TextArea("Review");
        commentaryField.setClearButtonVisible(true);
        commentaryField.setValue(rating.getCommentary() == null ? "" : rating.getCommentary());
        binder.forField(commentaryField)
                .withValidator(commentary -> commentary.length() <= 500, "Review deve ter no máximo 500 caracteres")
                .bind(Rating::getCommentary, Rating::setCommentary);


        var favoriteField = new Checkbox("Favorito");
        favoriteField.setValue(rating.isFavorite());
        binder.forField(favoriteField)
                .bind(Rating::isFavorite, Rating::setFavorite);

        binder.forField(statusField)
                .bind(Rating::getStatus, Rating::setStatus);

        Consumer<Boolean> disableFields = (disable) -> {
            ratingField.setEnabled(!disable);
            commentaryField.setEnabled(!disable);
            favoriteField.setEnabled(!disable);
        };

        disableFields.accept(rating.getStatus() != Rating.Status.COMPLETED);

        statusField.addValueChangeListener(listener -> {
            if (Objects.requireNonNull(listener.getValue()) != Rating.Status.COMPLETED) {
                disableFields.accept(true);
            } else {
                disableFields.accept(false);
            }
        });

        var submitButton = new Button("Salvar", VaadinIcon.CHECK.create());
        submitButton.addClickListener(listener -> {

            if (statusField.getValue() != Rating.Status.COMPLETED) {

                rating.setRating(-1);
                rating.setFavorite(false);
                rating.setCommentary(null);
                rating.setStatus(statusField.getValue());

                userDAO.update(user);

                if (successCallback != null)
                    successCallback.run();

                Notification notification = Notification
                        .show("As informações foram alteradas com sucesso!");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                renderMovieInformation();
                return;
            }

            if (!binder.isValid()) {
                Notification notification = Notification
                        .show("Por favor, preencha os campos corretamente!");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            binder.writeBeanIfValid(rating);

            userDAO.update(user);

            if (successCallback != null)
                successCallback.run();

            Notification notification = Notification
                    .show("As informações foram alteradas com sucesso!");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            renderMovieInformation();
        });

        formLayout.add(ratingField, statusField, commentaryField, favoriteField, submitButton);

        return formLayout;
    }

    public void onSuccessfulSave(Runnable successCallback) {
        this.successCallback = successCallback;
    }

    private HorizontalLayout createRatingLayout() {
        var rateLayout = new HorizontalLayout();
        var ratingValue = Math.max(0.0, rating.getRating()) / 2.0;
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
        rateLayout.getStyle().set("padding", "0");
        return rateLayout;
    }

}
