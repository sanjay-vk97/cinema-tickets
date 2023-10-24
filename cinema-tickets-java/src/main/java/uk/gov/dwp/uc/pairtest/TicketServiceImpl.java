package uk.gov.dwp.uc.pairtest;

import java.util.Arrays;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
	private static final int _maximumNumberOfTickets = 20;
	private static final int ADULT_TICKET_PRICE = 20;
	private static final int CHILD_TICKET_PRICE = 10;

    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;

    public TicketServiceImpl( TicketPaymentService ticketPaymentService,
            SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }
    /**
     * Should only have private methods other than the one below.
     */

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
    	validateInputs(accountId, ticketTypeRequests);

    	int[] tickets = processPurchase(ticketTypeRequests);

        int numberOfSeats = tickets[0];
        int totalPrice = tickets[1];
        

        ticketPaymentService.makePayment(accountId, totalPrice);
        seatReservationService.reserveSeat(accountId, numberOfSeats);
    }
    
    private void validateInputs(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        validateAccountId(accountId);
        validateTicketRequests(ticketTypeRequests);
        validateNumberOfTickets(ticketTypeRequests);
        validateMaximumTicketCount(ticketTypeRequests);
    }

    private void validateAccountId(Long accountId) {
        if (accountId <= 0) {
            throw new InvalidPurchaseException("AccountId must be greater than 0");
        }
    }

    private void validateTicketRequests(TicketTypeRequest... ticketTypeRequests) {
        boolean hasAdultTicketRequests = Arrays.stream(ticketTypeRequests)
                .anyMatch(request -> request.getTicketType() == Type.ADULT);

        if (!hasAdultTicketRequests) {
            throw new InvalidPurchaseException("Minimum 1 adult ticket is required");
        }
    }

    private void validateNumberOfTickets(TicketTypeRequest... ticketTypeRequests) {
        boolean isInvalidNumberOfTickets = Arrays.stream(ticketTypeRequests)
                .anyMatch(request -> request.getNoOfTickets() < 0);

        if (isInvalidNumberOfTickets) {
            throw new InvalidPurchaseException("Invalid number of tickets");
        }
    }

    private void validateMaximumTicketCount(TicketTypeRequest... ticketTypeRequests) {
        var totalTicketCount = Arrays.stream(ticketTypeRequests)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();

        if (totalTicketCount > _maximumNumberOfTickets) {
        	 throw new InvalidPurchaseException(
                     String.format("You can't purchase more than %s tickets", _maximumNumberOfTickets));
        }
    }


    private int[] processPurchase(TicketTypeRequest... ticketTypeRequests) {
        int adultTicketCount = 0;
        int childTicketCount = 0;
        int totalPrice = 0;

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            switch (ticketTypeRequest.getTicketType()) {
                case INFANT:
                    // Infants do not pay for tickets, so we can ignore them.
                    break;
                case CHILD:
                    childTicketCount += ticketTypeRequest.getNoOfTickets();
                    totalPrice += ticketTypeRequest.getNoOfTickets() * CHILD_TICKET_PRICE;
                    break;
                case ADULT:
                    adultTicketCount += ticketTypeRequest.getNoOfTickets();
                    totalPrice += ticketTypeRequest.getNoOfTickets() * ADULT_TICKET_PRICE;
                    break;
            }
        }

        int[] tickets = new int[]{adultTicketCount + childTicketCount, totalPrice};
        return tickets; 
    }
}
