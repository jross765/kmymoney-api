package org.kmymoney.api.read.impl.aux;

import java.time.LocalDate;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.generated.RECONCILIATION;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.aux.KMMAccountReconciliation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class KMMAccountReconciliationImpl implements KMMAccountReconciliation {

	private static final Logger LOGGER = LoggerFactory.getLogger(KMMAccountReconciliationImpl.class);

	// ---------------------------------------------------------------

	protected final RECONCILIATION jwsdpPeer;
	
	protected final KMyMoneyFile myFile;
	
	// ---------------------------------------------------------------

	@SuppressWarnings("exports")
	public KMMAccountReconciliationImpl(
			final RECONCILIATION newPeer,
			final KMyMoneyFile kmmFile) {
		super();
		
		this.jwsdpPeer = newPeer;
		this.myFile = kmmFile;
	}

	// ---------------------------------------------------------------

	@Override
	public KMyMoneyFile getKMyMoneyFile() {
		return myFile;
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public LocalDate getDate() {
		XMLGregorianCalendar cal = jwsdpPeer.getDate();
		try {
		    return LocalDate.of(cal.getYear(), cal.getMonth(), cal.getDay());
		} catch (Exception e) {
		    IllegalStateException ex = new IllegalStateException("unparsable date '" + cal + "' in creation date!");
		    ex.initCause(e);
		    throw ex;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public FixedPointNumber getValue() {
		return new FixedPointNumber ( jwsdpPeer.getValue() );
	}

	public BigFraction getValueRat() {
		return BigFraction.parse( jwsdpPeer.getValue() );
	}

	// ---------------------------------------------------------------
	
	@Override
	public String toString() {
		String result = "KMMAccountReconciliationImpl [";
		result += "date=" + getDate() + ", ";
		result += "value=" + getValue() + "]";
		
		return result;
	}

}
