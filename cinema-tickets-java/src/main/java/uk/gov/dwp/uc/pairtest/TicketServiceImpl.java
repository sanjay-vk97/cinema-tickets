package uk.gov.dwp.uc.pairtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.CinemaPass;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.laboratory.TicketLaboratory;

public class TicketServiceImpl implements TicketService {
    private static final int _maximumNumberOfTickets = 20;

    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;
    private TicketLaboratory ticketLaboratory;

    public TicketServiceImpl( TicketPaymentService ticketPaymentService,
            SeatReservationService seatReservationService, TicketLaboratory ticketLaboratory ) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
        this.ticketLaboratory = ticketLaboratory;
    }
    
    /**
     * Should only have private methods other than the one below.
     */

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        // Validate inputs before processing the purchase
        validateInputs(accountId, ticketTypeRequests);

        // Process the purchase and generate a list of CinemaPass objects
        List<CinemaPass> tickets = processPurchase(ticketTypeRequests);

        int totalPrice = 0;
        int numberOfSeats = 0;

        // Calculate the total price and the number of seats
        for (CinemaPass ticket : tickets) {
            totalPrice += ticket.getPrice();
            numberOfSeats += ticket.getNoSeats();
        }

        // Make the payment and reserve the seats
        ticketPaymentService.makePayment(accountId, totalPrice);
        seatReservationService.reserveSeat(accountId, numberOfSeats);
    }
    
    // Private method for input validation
    private void validateInputs(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        validateAccountId(accountId);
        validateTicketRequests(ticketTypeRequests);
        validateNumberOfTickets(ticketTypeRequests);
        validateMaximumTicketCount(ticketTypeRequests);
    }

    // Private method for validating the account ID
    private void validateAccountId(Long accountId) {
        if (accountId <= 0) {
            throw new InvalidPurchaseException("AccountId must be greater than 0");
        }
    }

    // Private method for validating the presence of adult ticket requests
    private void validateTicketRequests(TicketTypeRequest... ticketTypeRequests) {
        boolean hasAdultTicketRequests = Arrays.stream(ticketTypeRequests)
                .anyMatch(request -> request.getTicketType() == Type.ADULT);

        if (!hasAdultTicketRequests) {
            throw new InvalidPurchaseException("Minimum 1 adult ticket is required");
        }
    }

    // Private method for validating the number of tickets
    private void validateNumberOfTickets(TicketTypeRequest... ticketTypeRequests) {
        boolean isInvalidNumberOfTickets = Arrays.stream(ticketTypeRequests)
                .anyMatch(request -> request.getNoOfTickets() < 0);

        if (isInvalidNumberOfTickets) {
            throw new InvalidPurchaseException("Invalid number of tickets");
        }
    }

    // Private method for validating the maximum number of tickets that can be purchased
    private void validateMaximumTicketCount(TicketTypeRequest... ticketTypeRequests) {
        var totalTicketCount = Arrays.stream(ticketTypeRequests)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();

        if (totalTicketCount > _maximumNumberOfTickets) {
            throw new InvalidPurchaseException(
                    String.format("You can't purchase more than %s tickets", _maximumNumberOfTickets));
        }
    }

    // Private method for processing the purchase and generating a list of CinemaPass objects
    private List<CinemaPass> processPurchase(TicketTypeRequest... ticketTypeRequests) {
        List<CinemaPass> tickets = new ArrayList<CinemaPass>();

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            for (int i = 0; i < ticketTypeRequest.getNoOfTickets(); i++) {
                tickets.add(ticketLaboratory.generateCinemaTicket(ticketTypeRequest.getTicketType()));
            }
        }

        return tickets; 
    }
}
