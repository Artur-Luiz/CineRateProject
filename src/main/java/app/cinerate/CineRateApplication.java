package app.cinerate;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
@PWA(name = "CineRate" , shortName = "CineRate")
@Theme(value = "cine-rate", variant = Lumo.DARK)
public class CineRateApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(CineRateApplication.class, args);
	}

}
