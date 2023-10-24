package uk.gov.dwp.uc.pairtest.laboratory;

import uk.gov.dwp.uc.pairtest.domain.AdultPass;
import uk.gov.dwp.uc.pairtest.domain.ChildPass;
import uk.gov.dwp.uc.pairtest.domain.CinemaPass;
import uk.gov.dwp.uc.pairtest.domain.InfantPass;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

public class TicketLaboratoryImpl implements TicketLaboratory {
    
    @Override
    public CinemaPass generateCinemaTicket(Type type) {
        // Generate a CinemaPass based on the specified ticket type
        switch (type) {
            case INFANT:
                return new InfantPass();
            case CHILD:
                return new ChildPass();
            case ADULT:
            default:
                // Default to generating an AdultPass for unrecognized types
                return new AdultPass();
        }
    }
}
