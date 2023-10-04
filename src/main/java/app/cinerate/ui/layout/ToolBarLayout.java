package app.cinerate.ui.layout;

import app.cinerate.ui.GenresView;
import app.cinerate.ui.MyMoviesView;
import app.cinerate.ui.SearchView;
import app.cinerate.ui.SortListView;
import app.cinerate.ui.security.LoginView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public class ToolBarLayout extends AppLayout {

    private transient Tabs tabsField;

    public ToolBarLayout() {
        renderAsTab();
    }

    private void renderAsTab() {

        Image logo = new Image();
        logo.setSrc("https://i.imgur.com/o6u5Z2F.png");
        logo.setAlt("Logo da CineRate");

        logo.setWidth("250px");
        logo.setHeight("50px");

        var tabs = Arrays.stream(PageLocation.values())
                .map(pageLocation -> new Tab(pageLocation.getPageName()))
                .toArray(Tab[]::new);

        tabsField = new Tabs(tabs);
        tabsField.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabsField.addThemeName(LumoUtility.Margin.LARGE);

        tabsField.addSelectedChangeListener(listener -> {
            var goTo = Arrays.stream(PageLocation.values())
                    .filter(pageLocation -> pageLocation.getPageName().equals(tabsField.getSelectedTab().getLabel()))
                    .findAny()
                    .map(PageLocation::getLocation)
                    .orElseThrow(() -> new RuntimeException("Page not found"));

            getUI().ifPresent(ui -> ui.navigate(goTo));
        });

        HorizontalLayout header = new HorizontalLayout(logo, tabsField);
        header.addClassName("header");

        header.setSizeFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setPadding(true);

        addToNavbar(header);
    }

    public void selectCorrectLayoutTab(PageLocation pageLocation) {
        tabsField.setSelectedIndex(pageLocation.ordinal());
    }

    public enum PageLocation {
        MY_MOVIES("Meus Filmes", MyMoviesView.class),
        SEARCH_MOVIES("Buscar Filmes", SearchView.class),
        GENRES("Gêneros", GenresView.class),
        LIST_MOVIES("Lista", SortListView.class);

        private final String pageName;

        private final Class<? extends Component> location;

        PageLocation(String pageName, Class<? extends Component> location) {
            this.pageName = pageName;
            this.location = location;
        }

        public String getPageName() {
            return pageName;
        }

        public Class<? extends Component> getLocation() {
            return location;
        }
    }

}
