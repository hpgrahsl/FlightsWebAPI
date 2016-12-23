package flights;

import flights.model.Flight;
import flights.model.FlightRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EntityScan(basePackageClasses =
        { FlightsApp.class, Jsr310JpaConverters.class })
@SpringBootApplication
public class FlightsApp {

    private static final Random RND = new Random();
    private static final int DAYS_RANGE = 30;
    private static final int HOURS_RANGE = 24;
    private static final List<Flight> FLIGHTS;

    static {
        LocalDateTime now = LocalDateTime.of(LocalDate.now(),
                                LocalTime.of(LocalTime.now().getHour(),0));
        FLIGHTS = new ArrayList<>();
        FLIGHTS.add(new Flight("Paris", "London", now));
        FLIGHTS.add(new Flight("Graz", "Zürich",
                now.plusDays(RND.nextInt(DAYS_RANGE))
                        .plusHours(RND.nextInt(HOURS_RANGE))));
        FLIGHTS.add(new Flight("Graz", "Hamburg",
                now.plusDays(RND.nextInt(DAYS_RANGE))
                        .plusHours(RND.nextInt(HOURS_RANGE))));
        FLIGHTS.add(new Flight("Wien", "Barcelona",
                now.plusDays(RND.nextInt(DAYS_RANGE))
                        .plusHours(RND.nextInt(HOURS_RANGE))));
        FLIGHTS.add(new Flight("Wien", "Paris",
                now.plusDays(RND.nextInt(DAYS_RANGE))
                        .plusHours(RND.nextInt(HOURS_RANGE))));
    }

    @Bean
    CommandLineRunner init(FlightRepository flightRepo) {
        return (args) -> FLIGHTS.forEach(flightRepo::save);
    }

    public static void main(String[] args) {
        SpringApplication.run(FlightsApp.class, args);
    }

}
