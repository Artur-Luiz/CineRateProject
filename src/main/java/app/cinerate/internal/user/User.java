package app.cinerate.internal.user;

import info.movito.themoviedbapi.model.MovieDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Esta classe armazena as informações de um usuário.
 * A senha está encriptada com o método BCrypt.
 */
public final class User implements UserDetails {

    @Serial
    private static final long serialVersionUID = 8869267517579242005L;

    private final Long id;
    private final String nickname;
    private final List<Rating> ratings;
    private String encryptedPassword;
    private final Role role;

    public User(
            @Nullable Long id,
            @NotNull String nickname,
            @NotNull List<Rating> ratings,
            @NotNull String encryptedPassword,
            @NotNull Role role
    ) {
        this.id = id;
        this.nickname = nickname;
        this.ratings = ratings;
        this.encryptedPassword = encryptedPassword;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.of(role)
                .map(Enum::name)
                .map(s -> "ROLE_" + s)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }


    public Rating getOrCreateRating(MovieDb movieDb) {
        var rating = ratings.stream()
                .filter(r -> r.getMovieId() == movieDb.getId())
                .findAny()
                .orElse(new Rating(
                        movieDb.getId(),
                        null,
                        -1,
                        false,
                        Rating.Status.PENDING
                ));

        if (!ratings.contains(rating)) {
            ratings.add(rating);
        }

        return rating;
    }

    @Nullable
    public Rating getRating(MovieDb movieDb) {
        return ratings.stream()
                .filter(r -> r.getMovieId() == movieDb.getId())
                .findAny()
                .orElse(null);
    }

    @Override
    public String getPassword() {
        return encryptedPassword;
    }

    @Override
    public String getUsername() {
        return nickname;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public @Nullable Long getId() {
        return id;
    }

    public @NotNull String getNickname() {
        return nickname;
    }

    public @NotNull List<Rating> getRatings() {
        return ratings;
    }

    public @NotNull String getEncryptedPassword() {
        return encryptedPassword;
    }

    public @NotNull Role getRole() {
        return role;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(nickname, user.nickname) && Objects.equals(ratings, user.ratings) && Objects.equals(encryptedPassword, user.encryptedPassword) && role == user.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nickname, ratings, encryptedPassword, role);
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", nickname='" + nickname + '\'' +
               ", ratings=" + ratings +
               ", encryptedPassword='" + encryptedPassword + '\'' +
               ", role=" + role +
               '}';
    }
}