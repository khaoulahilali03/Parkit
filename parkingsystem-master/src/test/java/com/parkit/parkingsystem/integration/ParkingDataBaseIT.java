package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static java.lang.Math.round;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private Ticket ticket;
    private ParkingSpot parkingSpot;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        ticket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertNotNull(ticket);
        parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        parkingService.processIncomingVehicle();
        Assertions.assertNotEquals(parkingSpot, parkingSpotDAO);

    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        try {
            TimeUnit.SECONDS.sleep(1);
            parkingService.processExitingVehicle();
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //TODO: check that the fare generated and out time are populated correctly in the database
        ticket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertNotNull(ticket.getOutTime());
        double duration = SECONDS.between(ticket.getInTime().toInstant(), ticket.getOutTime().toInstant()) ;
        Assertions.assertEquals( round((duration > (30.0 * 60.0 )) ? (((duration / (60.0 * 60.0 ))) * Fare.CAR_RATE_PER_HOUR) : 0),round(ticket.getPrice()));

    }

    @Test
    public void testParkingReccuringCar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ticket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertFalse(ticketDAO.checkIfReccurent(ticket));
        parkingService.processIncomingVehicle();
        try {
            TimeUnit.SECONDS.sleep(1);
            parkingService.processExitingVehicle();
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ticket = ticketDAO.getTicket("ABCDEF");
        parkingService.processIncomingVehicle();
        Assertions.assertTrue(ticketDAO.checkIfReccurent(ticket), "Welcome back! As a recurring user of our parking lot, you'll benefit from a 5% discount.");
        try {
            TimeUnit.SECONDS.sleep(1);
            parkingService.processExitingVehicle();
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
