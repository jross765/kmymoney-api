package org.kmymoney.api.write.impl.aux;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.generated.PERIOD;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.impl.KMyMoneyBudgetImpl;
import org.kmymoney.api.read.impl.aux.KMMBudgetPeriodImpl;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.aux.KMMWritableBudgetPeriod;
import org.kmymoney.api.write.impl.DateMappingException;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of KMMBudgetPeriodImpl to allow read-write access instead of
 * read-only access.
 */
public class KMMWritableBudgetPeriodImpl extends KMMBudgetPeriodImpl 
                                         implements KMMWritableBudgetPeriod 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KMMWritableBudgetPeriodImpl.class);

    // ---------------------------------------------------------------

    @SuppressWarnings("exports")
    public KMMWritableBudgetPeriodImpl(
    		final KMMBudgetAccount bdgtAcct,
    		final PERIOD jwsdpPeer,
    		final KMyMoneyWritableFile kmmFile) {
    	super(bdgtAcct, jwsdpPeer, kmmFile);
    }

    public KMMWritableBudgetPeriodImpl(final KMMBudgetPeriodImpl bdgtPrd) {
    	super(bdgtPrd.getParent(), bdgtPrd.getJwsdpPeer(), bdgtPrd.getKMyMoneyFile());
    }

	public KMMWritableBudgetPeriodImpl(
			final KMMWritableBudgetAccountImpl bdgtAcct) {
		super(bdgtAcct,
			  createBudgetPeriod_int(bdgtAcct.getWritableKMyMoneyFile(), bdgtAcct), 
			  bdgtAcct.getKMyMoneyFile());

		bdgtAcct.addPeriod(this);
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KMyMoneyWritableFileImpl getWritableKMyMoneyFile() {
		return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public KMyMoneyWritableFileImpl getKMyMoneyFile() {
//		return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
//	}

	// ---------------------------------------------------------------
	
	/**
	 * Creates a new budget account and adds it to the given KMyMoney file.
	 */
	protected static PERIOD createBudgetPeriod_int(
			final KMyMoneyWritableFileImpl file, 
			final KMMWritableBudgetAccountImpl bdgtAcct) {
		if ( file == null ) {
			throw new IllegalArgumentException("argument <file> is null");
		}

		if ( bdgtAcct == null ) {
			throw new IllegalArgumentException("argument <bdgtAcct> is null");
		}
		
		// This is needed because transaction.addAccount() later
		// must have an already built account.
		// Otherwise, it will create it from the JAXB-Data
		// thus 2 instances of this KMyMoneyWritableAddressImpl
		// will exist. One created in getAccounts() from this JAXB-Data
		// the other is this object.
		bdgtAcct.getPeriods();

		ObjectFactory fact = file.getObjectFactory();

		PERIOD jwsdpBdgtPrd = fact.createPERIOD();
		jwsdpBdgtPrd.setAmount("1/100"); // sic
		jwsdpBdgtPrd.setStart( ((KMyMoneyBudgetImpl) bdgtAcct.getParent()).getJwsdpPeer().getStart() ); // sic
		
		// NO, not here but in the calling method:
		// trx.addAccount(new KMyMoneyWritableBudgetAccountImpl(jwsdpBdgtAcct, bdgt.getKMyMoneyFile(), bdgt));
		file.setModified(true);
    
        LOGGER.debug("createBudgetPeriod_int: Created new budget period (core): " + jwsdpBdgtPrd);
		
        return jwsdpBdgtPrd;
	}

    // ---------------------------------------------------------------

	@Override
	public void setStart(final LocalDate startDate) {
		if ( startDate == null ) {
			throw new IllegalArgumentException("argument <date> is null");
		}
		
		if ( startDate.compareTo( getParent().getParent().getStart() ) < 0 ) {
			throw new IllegalStateException("argument <startDate> is < the budget's start date: " + startDate + " < " + getParent().getParent().getStart());
		}
		
		// ::TODO: Check for the upper limit of start date as well
		
		// ::TODO
    	// LocalDate oldStartDate = getStart();
    	
		try {
	        // https://stackoverflow.com/questions/835889/java-util-date-to-xmlgregoriancalendar
			// https://simplesolution.dev/java-convert-localdate-to-calendar/
	        GregorianCalendar cal = new GregorianCalendar();
	        ZonedDateTime zonedDateTime = startDate.atStartOfDay(ZoneId.systemDefault());
	        Instant instant = zonedDateTime.toInstant();
	        Date date = Date.from(instant);
	        cal.setTime(date);
	        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
			jwsdpPeer.setStart(xmlCal);
		} catch ( DatatypeConfigurationException exc ) {
			throw new DateMappingException();
		}

		getWritableKMyMoneyFile().setModified(true);

		// ::TODO
//		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
//    	if ( propertyChangeSupport != null) {
//    		propertyChangeSupport.firePropertyChange("start", oldStartDate, startDate);
//    	}
	}

	@Override
	public void setAmount(final BigFraction amt) {
		if ( amt == null ) {
			throw new IllegalArgumentException("argument <amt> is null");
		}
		
		if ( amt.compareTo(BigFraction.ZERO) <= 0 ) {
			throw new IllegalArgumentException("argument <amt> is <= 0");
		}
		
		// ::TODO
		// BigFraction oldAmt = getAmount();
		
		jwsdpPeer.setAmount( amt.toString().replaceAll(" ", "") );

		getWritableKMyMoneyFile().setModified(true);

		// ::TODO
//		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
//    	if ( propertyChangeSupport != null) {
//    		propertyChangeSupport.firePropertyChange("amount", oldAmt, amt);
//    	}
	}

    // ---------------------------------------------------------------

    @Override
	public String toString() {
		String result = "KMMWritableBudgetPeriodImpl [";

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
