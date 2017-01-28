# FlightsWebAPI
Dieses Repository beinhaltet das beispielhafte WebAPI-Projekt für meinen gemeinsamen Artikel mit [Manfred Steyer](https://www.softwarearchitekt.at/) und zeigt, wie mittels OIDC/OAuth2 und auf Basis der Identity & Access Management Lösung [Keycloak](http://www.keycloak.org/) die Absicherung einer WebAPI erfolgen kann.

#### Spring Boot WebAPI 
Ausgangspunkt für die Absicherung des Backends mittels OIDC/Oauth2 stellt eine sehr simpel gestrickte, auf [Spring Boot](https://projects.spring.io/spring-boot/) basierende WebAPI dar, welche es konsumierenden Clients ermöglichen soll, nach Flügen zu suchen. Über Maven werden zunächst die folgenden Abhängigkeiten im *pom.xml* konfiguriert:

```xml
    <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>1.4.3.RELEASE</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
        </dependency>
    </dependencies>
```


Das Domänenmodell der WebAPI besteht lediglich aus einer einzigen Entitätsklasse namens *Flight*, welche Attribute von Flügen kapselt. Zu Demostrationszwecken und weil es sich der Angular-Client so erwartet, werden die beiden String Attribute origin sowie destination im Rahmen der JSON Serialisierung mittels *@JsonProperty* Annotationen auf die Key-Namen from bzw. to gemappt. Ebenso wird für den in Java 8 eingeführten LocalDateTime Typ ein vom Client präferiertes Datumsformat angegeben.

```java
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

    //Konstruktor, Getter, toString Methoden entfernt

}
```
Für die korrekte Unterstützung der Java 8 spezifischen Time-API Typen werden entsprechende Maven Abhängigkeiten für den Serializer (hier Jackson FasterXML) konfiguriert:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.module</groupId>
    <artifactId>jackson-module-parameter-names</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jdk8</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

Als Repository für die *Flight* Entitäten fungiert ein [Spring Data](http://projects.spring.io/spring-data/) CrudRepository. Wir deklarieren dafür lediglich ein paar verschiedene *find**-Methoden, um Flüge basierend auf unterschiedlichen Suchangaben finden zu können – Spring Data kümmert sich um die restlichen Details.

```java
public interface FlightRepository extends CrudRepository<Flight, Long> {
    @Override
    List<Flight> findAll();
    List<Flight> findByOrigin(String origin);
    List<Flight> findByDestination(String destination);
    List<Flight> findByOriginAndDestination(String origin, String destination);
}
```

Nachdem sich in den Maven Abhängigenkeiten des Projekts bereits Spring Boot Starter Data JPA sowie [H2](http://www.h2database.com/) als Datenbank befinden, ist die einfache in-memory Persistenz für *Flight* Entitäten damit bereits verwendbar. Was noch fehlt sind ein paar Demodaten für die WebAPI, welche je nach Präferenz sehr schnell auf zwei verschiedene Arten generiert werden können. Eine Möglichkeit ist es, per Konvention ein SQL-Skript mit INSERT Befehlen in eine Datei namens *data.sql* unter *src/main/resources/* abzulegen. Wer sich mehr Flexibilität wünscht, kann sich im Code eine Liste mit Flight Entitäten generieren und diese mittels eines *CommandLineRunner Beans* im Flight Repository ablegen. Letztere Variante findet sich im Code Snippet ganz unten.

Client-Anwendungen benötigen entsprechende HTTP Endpunkte, um die Flights WebAPI zu konsumieren. Diese werden über einen Spring Controller, genauer gesagt *@RestController* bereitgestellt, welcher das *Flight* Repository verwendet, um Suchanfragen zu Flügen beantworten zu können. Damit die Client-Anwendung unabhängig von einem anderen Host/Port ausgeliefert werden kann, muss mit *@CrossOrigin* die nötige [CORS](https://www.w3.org/TR/cors/) Einstellung erfolgen. Mit * als Wildcard werden Anfragen von beliebigen Client Domains erlaubt. In realen Projekten sollte dies möglichst restriktiv auf Host/Port Mappings beschränkt sein, von denen Client-Anfragen gewährt werden dürfen.

```java
@RestController
@RequestMapping("api/flight")
@CrossOrigin(origins = "*")
public class FlightsController {

    @Autowired
    FlightRepository flightRepo;

    @RequestMapping(method = RequestMethod.GET)
    public List<Flight> getAllFlights() {
        return flightRepo.findAll();
    }

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public Flight getFlightById(@PathVariable("id") Long id) {
        //Methoden Code entfernt
    }

    @RequestMapping(params = {"from"}, method = RequestMethod.GET)
    public List<Flight> getFlightsByOrigin(@RequestParam(value = "from", required = false, defaultValue = "") String from) {
        //Methoden Code entfernt
    }

    //weitere HTTP GET-Methoden entfernt
}
```

Die Autoren verzichten der Einfachheit halber bewußt auf einen Service Layer, als auch auf ein explizites Entity <-> DTO Mapping. Beides wären nützliche Erweiterungen im Rahmen von realen WebAPI Projekten.

Schließlich zeigt das folgende Snippet noch die startbare Hauptklasse das Backends und damit den finalen Teil der Spring Boot Flights WebAPI. Zu diesem Zeitpunkt gibt es noch keinerlei Absicherungmaßnahmen, weshalb das Backend von beliebigen Clients verwendbar ist.

```java
@SpringBootApplication
public class FlightsApp {

private static final List<Flight> FLIGHTS;

    static {
        FLIGHTS = new ArrayList<>();
        //Code zum Hinzufügen von Flight Entitäten entfernt
    }

    @Bean
    CommandLineRunner init(FlightRepository flightRepo) {
        return (args) -> FLIGHTS.forEach(flightRepo::save);
    }

    public static void main(String[] args) {
        SpringApplication.run(FlightsApp.class, args);
    }

}
```
#### Absicherung mittels Keycloak Adapter
Für eine Bearer Token-basierte Absicherung der HTTP-Endpunkte des @RestController mittels Keycloak werden zwei Maven Dependencies eingefügt.

```xml
    <dependency>
        <groupId>org.keycloak</groupId>
        <artifactId>keycloak-spring-boot-adapter</artifactId>
        <version>2.5.0.Final</version>
    </dependency>
    <dependency>
        <groupId>org.keycloak</groupId>
        <artifactId>keycloak-tomcat8-adapter</artifactId>
        <version>2.5.0.Final</version>
    </dependency>
```

Die Konfiguration erfolgt in der *application.properties* von Spring Boot wie folgt:

```properties
keycloak.cors = true
keycloak.realm = angular-spring
keycloak.auth-server-url = http://localhost:8080/auth
keycloak.bearer-only = true
keycloak.resource = spring-webapi

keycloak.securityConstraints[0].securityCollections[0].name = secured controller
keycloak.securityConstraints[0].securityCollections[0].authRoles[0] = flightapi_user
keycloak.securityConstraints[0].securityCollections[0].patterns[0] = /api/flight/*
```
Damit ist sichergestellt, dass sämtliche *@RestController* Zugriffe auf _/api/flight/*_ abgesichert sind und der aufrufende Client ein gültiges *Bearer Token* vorweisen muss, welches den *flightapi_user* Claim beinhaltet, um Zugriff zu bekommen.  

Weitere Informationen sowie Details zur Konfiguration von Keycloak selbst sind näher im Artikel beschrieben. 

#### Angular SPA als Client
Eine beispielhafte Client Implementierung, welche diese WebAPI konsumiert, findet sich in Form einer Angular SPA in folgendem [GitHub Repository von Manfred Steyer](https://github.com/manfredsteyer/angular-oauth2-oidc-sample).
