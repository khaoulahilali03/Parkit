package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import static java.time.temporal.ChronoUnit.MILLIS;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }



        //TODO: Some tests are failing here. Need to check if this logic is correct
        double duration = MILLIS.between(ticket.getInTime().toInstant(), ticket.getOutTime().toInstant());

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration > (30 * 60 * 1000) ? (duration/(60 * 60 * 1000)) * Fare.CAR_RATE_PER_HOUR : 0);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration > (30 * 60 * 1000) ? (duration/(60 * 60 * 1000)) * Fare.BIKE_RATE_PER_HOUR : 0);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}