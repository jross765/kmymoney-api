package org.kmymoney.api.read.impl.aux;

import java.time.LocalDate;
import java.util.Locale;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.generated.PERIOD;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.aux.KMMBudgetPeriod;
import org.kmymoney.api.read.impl.hlp.AmountFormatter_BF;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMMBudgetPeriodImpl extends KMyMoneyObjectImpl 
							     implements KMMBudgetPeriod 
{
    @SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(KMMBudgetPeriodImpl.class);

    // -----------------------------------------------------------

    protected final PERIOD jwsdpPeer;

    // -----------------------------------------------------------
    
    protected final KMMBudgetAccount parent;

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
	public KMMBudgetAccount getParent() {
		return parent;
	}

    // -----------------------------------------------------------------

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

    // ----------------------------

	@Override
	public BigFraction getAmount() {
		return BigFraction.parse(jwsdpPeer.getAmount());
	}

	@Override
	public String getAmountFormatted() {
		return getAmountFormatted(Locale.getDefault());
	}

	@Override
	public String getAmountFormatted(final Locale lcl) {
		KMyMoneyAccount acct = kmmFile.getAccountByID(parent.getAcctID());
    	return AmountFormatter_BF.formatAmount( getKMyMoneyFile(),
    											getAmount(), acct.getQualifSecCurrID(), lcl );	}

    // -----------------------------------------------------------------

    @Override
    public String toString() {
		String result = "KMMBudgetPeriodImpl [";

		try {
			result += "start-date=" + getStart();
		} catch (Exception e) {
			result += "start-date=" + "ERROR";
		}

		try {
			result += ", amount=" + getAmountFormatted();
		} catch (Exception e) {
			result += ", amount=" + "ERROR";
		}

		result += "]";

		return result;
    }

}
