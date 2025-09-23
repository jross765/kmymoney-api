package org.kmymoney.api.read.impl.aux;

import java.time.LocalDate;

import javax.xml.datatype.XMLGregorianCalendar;

import org.kmymoney.api.generated.RECONCILIATION;
import org.kmymoney.api.read.aux.KMMAccountReconciliation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class KMMAccountReconciliationImpl implements KMMAccountReconciliation {

	private static final Logger LOGGER = LoggerFactory.getLogger(KMMAccountReconciliationImpl.class);

	// ---------------------------------------------------------------

	private RECONCILIATION jwsdpPeer = null;
	
	// ---------------------------------------------------------------

	@SuppressWarnings("exports")
	public KMMAccountReconciliationImpl(final RECONCILIATION newPeer) {
		this.jwsdpPeer = newPeer;
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
	public FixedPointNumber getValue() {
		return new FixedPointNumber ( jwsdpPeer.getValue() );
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
