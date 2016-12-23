package flights.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
public class Flight {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonProperty("from")
    @NotNull
    private String origin;

    @JsonProperty("to")
    @NotNull
    private String destination;

    @JsonFormat(shape=JsonFormat.Shape.STRING,
            pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    @NotNull
    private LocalDateTime date;

    private Flight() {}

    public Flight(Long id, String origin, String destination, LocalDateTime date) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.date = date;
    }

    public Flight(String origin, String destination, LocalDateTime date) {
        this.origin = origin;
        this.destination = destination;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "id=" + id +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", date=" + date +
                '}';
    }
}
