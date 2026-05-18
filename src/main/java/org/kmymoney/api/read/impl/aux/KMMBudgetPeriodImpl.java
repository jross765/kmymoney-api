package org.kmymoney.api.read.impl.aux;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.Const;
import org.kmymoney.api.generated.PERIOD;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.aux.KMMBudgetPeriod;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMMBudgetPeriodImpl extends KMyMoneyObjectImpl 
							     implements KMMBudgetPeriod 
{
    @SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(KMMBudgetPeriodImpl.class);

    protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
    
    // -----------------------------------------------------------

    protected final PERIOD jwsdpPeer;

    // -----------------------------------------------------------
    
    protected KMMBudgetAccount parent = null;

    // -----------------------------------------------------------

    /**
     * @param parent 
     * @param newPeer the JWSDP-object we are wrapping.
     * @param kmmFile 
     */
    @SuppressWarnings("exports")
    public KMMBudgetPeriodImpl(
    		final KMMBudgetAccount parent, 
    		final PERIOD newPeer, 
    		final KMyMoneyFile kmmFile) {
    	super(kmmFile);
		
    	this.parent    = parent;
    	this.jwsdpPeer = newPeer;
    }

	// ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public PERIOD getJwsdpPeer() {
    	return jwsdpPeer;
    }

    // -----------------------------------------------------------------

	@Override
	public BigFraction getAmount() {
		return BigFraction.parse(jwsdpPeer.getAmount());
	}

	@Override
    public LocalDate getStart() {
		if ( jwsdpPeer.getStart() == null )
			return null;

		XMLGregorianCalendar cal = jwsdpPeer.getStart();
		try {
			return LocalDate.of(cal.getYear(), cal.getMonth(), cal.getDay());
		} catch (Exception e) {
			IllegalStateException ex = new IllegalStateException("unparsable date '" + cal + "' in price!");
			ex.initCause(e);
			throw ex;
		}
    }

    @Override
    public String getStartStr() {
		if ( jwsdpPeer.getStart() == null )
			return null;

		return jwsdpPeer.getStart().toString();
    }

    // -----------------------------------------------------------------

    @Override
    public String toString() {
		String result = "KMMBudgetPeriodImpl [";

		result += "amount=" + getAmount();

		try {
			result += ", start=" + getStart();
		} catch (Exception e) {
			result += ", start=" + "ERROR";
		}

		result += "]";

		return result;
    }

}
