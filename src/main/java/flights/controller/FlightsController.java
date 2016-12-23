package flights.controller;

import flights.exception.FlightNotFoundException;
import flights.model.Flight;
import flights.model.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/flight")
@CrossOrigin(origins = "*")
public class FlightsController {

    //NOTE: The example is so simple that we directly work
    //with the repository form within this controller.
    //Thus there is no service layer in between as this would
    //basically only ever delegate to the repository.
    //Further we directly expose the entities without a
    //DTO mapping.

    @Autowired
    FlightRepository flightRepo;

    @RequestMapping(method = RequestMethod.GET)
    public List<Flight> getAllFlights() {

        return flightRepo.findAll();

    }

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public Flight getFlightById(@PathVariable("id") Long id) {

        Flight flight = flightRepo.findOne(id);
        if(flight==null) {
            throw new FlightNotFoundException("no such flight (id: " + id + ")");
        }

        return flight;

    }

    @RequestMapping(params = {"from"}, method = RequestMethod.GET)
    public List<Flight> getFlightsByOrigin(@RequestParam(value = "from", required = false, defaultValue = "") String from) {

        if(!from.isEmpty()) {
            return flightRepo.findByOrigin(from);
        }

        return flightRepo.findAll();

    }

    @RequestMapping(params = {"to"}, method = RequestMethod.GET)
    public List<Flight> getFlightsByDestination(@RequestParam(value = "to", required = false, defaultValue = "") String to) {

        if(!to.isEmpty()) {
            return flightRepo.findByDestination(to);
        }

        return flightRepo.findAll();

    }

    @RequestMapping(params = {"from", "to"}, method = RequestMethod.GET)
    public List<Flight> getFlightsByOriginAndDestination(@RequestParam(value = "from", required = false, defaultValue = "") String from,
                                     @RequestParam(value = "to", required = false, defaultValue = "") String to) {

        if(!from.isEmpty() && !to.isEmpty()) {
            return flightRepo.findByOriginAndDestination(from,to);
        }

        if(!from.isEmpty()) {
            return flightRepo.findByOrigin(from);
        }

        if(!to.isEmpty()) {
            return flightRepo.findByDestination(to);
        }

        return flightRepo.findAll();

    }

}
