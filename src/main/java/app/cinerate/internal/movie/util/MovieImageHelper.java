package app.cinerate.internal.movie.util;

import org.jetbrains.annotations.NotNull;

public class MovieImageHelper {

    private MovieImageHelper() {
    }

    private static final String DEFAULT_IMAGE_URL = "https://image.tmdb.org/t/p/";

    public static String getAsOriginalURL(@NotNull String image) {
//        https://image.tmdb.org/t/p/original/wwemzKWzjKYJFfCeiB57q3r4Bcm.png
        return DEFAULT_IMAGE_URL + "original" + (image.startsWith("/") ? image : ("/" + image));
    }

    public static String getAsW500URL(@NotNull String image) {
//        https://image.tmdb.org/t/p/w500/wwemzKWzjKYJFfCeiB57q3r4Bcm.png
        return DEFAULT_IMAGE_URL + "w500" + (image.startsWith("/") ? image : ("/" + image));
    }

}
