package app.cinerate.ui.security;

import app.cinerate.internal.movie.util.MovieImageHelper;
import app.cinerate.internal.user.Role;
import app.cinerate.internal.user.User;
import app.cinerate.internal.user.UserDAO;
import app.cinerate.ui.MyMoviesView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final transient UserDAO userDAO;
    private final transient BCryptPasswordEncoder passwordEncoder;

    private final transient AuthenticationContext authenticationContext;


    @Autowired
    public LoginView(UserDAO userDAO, BCryptPasswordEncoder passwordEncoder, AuthenticationContext authenticationContext) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.authenticationContext = authenticationContext;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

    }

    private boolean registerMode;

    private void render() {

        removeAll();

        FlexLayout layout = new FlexLayout();
        layout.setSizeFull();
        layout.setAlignItems(Alignment.CENTER);
        var poster = new Image();
        poster.setSrc("https://i.imgur.com/lQ6DcM1.jpeg");
        poster.setAlt("Cinerate Poster");

        if (registerMode) {
            VerticalLayout form = new VerticalLayout(registerComponent());
            form.add(new Button("Já tem conta? Clique Aqui!", event -> {
                registerMode = false;
                render();
            }));
            form.setSizeFull();
            form.setAlignItems(Alignment.CENTER);
            form.setJustifyContentMode(JustifyContentMode.CENTER);
            layout.add(form);
        } else {
            VerticalLayout form = new VerticalLayout(loginComponent());
            form.add(new Button("Não tem conta? Clique Aqui!", event -> {
                registerMode = true;
                render();
            }));
            form.setSizeFull();
            form.setAlignItems(Alignment.CENTER);
            form.setJustifyContentMode(JustifyContentMode.CENTER);
            layout.add(form);
        }
        layout.add(poster);
        poster.setSizeFull();
        add(layout);
    }

    private boolean errorLogin;

    private Component loginComponent() {
        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setTitle("ENTRAR");
        i18nForm.setUsername("Usuário");
        i18nForm.setPassword("Senha");
        i18nForm.setSubmit("Entrar");

        i18n.setForm(i18nForm);

        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle("ERRO");
        i18nErrorMessage.setMessage(
                "Nome de usuário ou senha incorretos. Por favor, tente novamente.");
        i18n.setErrorMessage(i18nErrorMessage);

        // DESCOBRIR COMO MUDAR O PASSWORD REQUIRED E USERNAME REQUIRED QUE APARECEM QUANDO O USUÁRIO NÃO DIGITA NADa

        LoginForm loginForm = new LoginForm();
        loginForm.setI18n(i18n);


        loginForm.setForgotPasswordButtonVisible(false);

        loginForm.setAction("login");

        if (errorLogin) {
            loginForm.setError(true);
        }
        return loginForm;
    }

    private Component registerComponent() {
        var verticalLayout = new VerticalLayout();

        verticalLayout.setMaxWidth("80%");

        var binder = new Binder<FormUserRegister>();

        TextField nickname = new TextField("Nome");

        binder.forField(nickname)
                .asRequired("O nome é obrigatório")
                .withValidator(name -> name.length() >= 5, "O nome deve ter no mínimo 5 caracteres")
                .withValidator(name -> userDAO.findByNickname(name).isEmpty(), "O nome de usuário já está em uso")
                .bind(FormUserRegister::getNickname, FormUserRegister::setNickname);

        PasswordField passwordField = new PasswordField("Senha");
        PasswordField passwordConfirmationField = new PasswordField("Confirmar Senha");

        binder.forField(passwordField)
                .asRequired("A senha é obrigatória")
                .withValidator(password -> password.length() >= 8, "A senha deve ter no mínimo 8 caracteres")
                .bind(FormUserRegister::getPassword, FormUserRegister::setPassword);

        binder.forField(passwordConfirmationField)
                .asRequired("A senha é obrigatória")
                .withValidator(password -> password.equals(passwordField.getValue()), "As senhas não coincidem")
                .bind(FormUserRegister::getPassword, FormUserRegister::setPassword);

        FormLayout formLayout = new FormLayout();
        formLayout.add(nickname, passwordField, passwordConfirmationField);
        formLayout.setColspan(nickname, 2);

        var registerButton = new Button("Registrar");

        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();

        registerButton.addClickListener(listener -> {

            if (binder.validate().hasErrors()) {
                binder.validate().getValidationErrors().forEach(validationError -> {
                    var notification = new Notification(validationError.getErrorMessage(), 3000);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.open();
                });
                return;
            }

            userDAO.insert(new User(
                            null,
                            nickname.getValue(),
                            new ArrayList<>(),
                            passwordEncoder.encode(passwordField.getValue()),
                            Role.USER
                    )
            );

            registerMode = false;
            render();

        });

        H2 title = new H2("REGISTRAR");

        verticalLayout.add(title,formLayout, registerButton);
        verticalLayout.setPadding(true);
        verticalLayout.addClassName("register-form");
        return verticalLayout;
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            errorLogin = true;
        }

        // não precisa de autenticação se já estiver autenticado
        if (authenticationContext.isAuthenticated()) {
            event.forwardTo(MyMoviesView.class);
            return;
        }

        render();
    }

    // O User não tem setNickname e setPassword por serem imutaveis nesse tipo de sistema,
    // então esta classe irá fazer o papel de um User temporário para o cadastro
    private static class FormUserRegister {

        private String nickname;

        private String password;

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}