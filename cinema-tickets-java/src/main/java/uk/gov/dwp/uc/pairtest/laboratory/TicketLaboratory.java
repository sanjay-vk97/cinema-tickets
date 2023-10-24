package uk.gov.dwp.uc.pairtest.laboratory;

import uk.gov.dwp.uc.pairtest.domain.CinemaPass;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

public interface TicketLaboratory {
	
	CinemaPass generateCinemaTicket(Type type);
	
}
