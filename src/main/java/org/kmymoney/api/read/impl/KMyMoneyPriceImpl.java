package org.kmymoney.api.read.impl;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;

import javax.xml.datatype.XMLGregorianCalendar;

import org.kmymoney.api.Const;
import org.kmymoney.api.generated.PRICE;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class KMyMoneyPriceImpl extends KMyMoneyObjectImpl 
							   implements KMyMoneyPrice 
{
    @SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyPriceImpl.class);

    protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
    
    // -----------------------------------------------------------

    protected final PRICE jwsdpPeer;

    // -----------------------------------------------------------
    
    protected KMyMoneyPricePair parent = null;

	protected LocalDate date;

	// The currency-format to use for formatting.<br/>
    private NumberFormat currencyFormat = null;

    // -----------------------------------------------------------

    /**
     * @param parent 
     * @param newPeer the JWSDP-object we are wrapping.
     * @param kmmFile 
     */
    @SuppressWarnings("exports")
    public KMyMoneyPriceImpl(
    		final KMyMoneyPricePair parent, 
    		final PRICE newPeer, 
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
    public PRICE getJwsdpPeer() {
    	return jwsdpPeer;
    }

    // -----------------------------------------------------------
    
    @Override
    public KMMPriceID getID() {
    	return new KMMPriceID(parent.getFromSecCurrStr(),
    						  parent.getToCurrStr(),
    						  DATE_FORMAT.format(getDate()));
    }

    @Override
    public KMMPricePairID getParentPricePairID() {
    	return parent.getID();
    }

    @Override
    public KMyMoneyPricePair getParentPricePair() {
    	return parent;
    }

    // ---------------------------------------------------------------
    
    @Override
    public String getFromSecCurrStr() {
    	return parent.getFromSecCurrStr();
    }

    @Override
    public String getToCurrStr() {
    	return parent.getToCurrStr();
    }
    
    // ----------------------------

    @Override
    public KMMQualifSecCurrID getFromSecCurrQualifID() {
    	return parent.getFromSecCurrQualifID();
    }

    @Override
    public KMMQualifSecID getFromSecurityQualifID() {
		KMMQualifSecCurrID secCurrID = getFromSecCurrQualifID();
		if ( secCurrID.getType() != KMMQualifSecCurrID.Type.SECURITY )
			throw new InvalidQualifSecCurrTypeException();

		return new KMMQualifSecID(secCurrID);
    }

    @Override
    public KMMQualifCurrID getFromCurrencyQualifID() {
		KMMQualifSecCurrID secCurrID = getFromSecCurrQualifID();
		if ( secCurrID.getType() != KMMQualifSecCurrID.Type.CURRENCY )
			throw new InvalidQualifSecCurrTypeException();

		return new KMMQualifCurrID(secCurrID);
    }

    @Override
    public KMyMoneySecurity getFromSecurity() {
    	return parent.getFromSecurity();
    }
    
    @Override
    public String getFromCurrencyCode() {
    	return getFromCurrencyQualifID().getCurrency().getCurrencyCode();
    }

    @Override
    public KMyMoneyCurrency getFromCurrency() {
    	return parent.getFromCurrency();
    }
    
    // ----------------------------
    
    @Override
    public KMMQualifCurrID getToCurrencyQualifID() {
    	return parent.getToCurrencyQualifID();
    }

    @Override
    public String getToCurrencyCode() {
    	return getToCurrencyQualifID().getCode();
    }

    @Override
    public KMyMoneyCurrency getToCurrency() {
    	return parent.getToCurrency();
    }

    // ---------------------------------------------------------------
    
    /**
     * @return The currency-format to use for formatting.
     * @throws InvalidQualifSecCurrTypeException 
     * @throws InvalidQualifSecCurrIDException 
     */
    private NumberFormat getCurrencyFormat() {
		if ( currencyFormat == null ) {
			currencyFormat = NumberFormat.getCurrencyInstance();
		}

//		The currency may have changed
//		if ( ! getCurrencyQualifID().getType().equals(SecurityCurrID.Type.CURRENCY) )
//	    	throw new InvalidSecCurrTypeException();

		Currency currency = Currency.getInstance(getToCurrencyCode());
		currencyFormat.setCurrency(currency);

		return currencyFormat;
    }

    @Override
    public LocalDate getDate() {
		if ( jwsdpPeer.getDate() == null )
			return null;

		XMLGregorianCalendar cal = jwsdpPeer.getDate();
		try {
			return LocalDate.of(cal.getYear(), cal.getMonth(), cal.getDay());
		} catch (Exception e) {
			IllegalStateException ex = new IllegalStateException("unparsable date '" + cal + "' in price!");
			ex.initCause(e);
			throw ex;
		}
    }

    @Override
    public String getDateStr() {
		if ( jwsdpPeer.getDate() == null )
			return null;

		return jwsdpPeer.getDate().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Source getSource() {
    	return Source.valueOff(getSourceStr());
    }

    /**
     * <b>Using this method is discouraged.</b>
     * Use {@link #getSource()} whenever possible/applicable instead.
     * 
     * @return
     * 
     * @see #getSource()
     */
    public String getSourceStr() {
    	if ( jwsdpPeer.getSource() == null )
    		return null;
	
    	return jwsdpPeer.getSource();
    }

    @Override
    public FixedPointNumber getValue() {
		if ( jwsdpPeer.getPrice() == null )
			return null;

		return new FixedPointNumber(jwsdpPeer.getPrice());
    }

    @Override
    public String getValueFormatted() {
    	return getCurrencyFormat().format(getValue());
    }

    // -----------------------------------------------------------------

    @Override
	public int compareTo(final KMyMoneyPrice otherPrc) {
		int i = getParentPricePairID().toString().compareTo(otherPrc.getParentPricePairID().toString());
		if ( i != 0 ) {
			return i;
		}

		i = getDate().compareTo(otherPrc.getDate());
		if ( i != 0 ) {
			return i;
		}

		i = getSource().toString().compareTo(otherPrc.getSource().toString()); // sic, not getSourceStr()
		if ( i != 0 ) {
			return i;
		}
		
		return ("" + hashCode()).compareTo("" + otherPrc.hashCode());
	}
	
    // -----------------------------------------------------------------

    @Override
    public String toString() {
		String result = "KMyMoneyPriceImpl [";

		try {
			result += "id='" + getID() + "'";
		} catch (Exception e) {
			result += "id=" + "ERROR";
		}

		try {
			result += ", from-sec-curr-qualif-id='" + getFromSecCurrQualifID() + "'";
		} catch (Exception e) {
			result += ", from-sec-curr-qualif-id=" + "ERROR";
		}

		try {
			result += ", to-curr-qualif-id='" + getToCurrencyQualifID() + "'";
		} catch (Exception e) {
			result += ", to-curr-qualif-id=" + "ERROR";
		}

		result += ", date=" + getDate();
		result += ", source='" + getSource() + "'";

		try {
			result += ", value=" + getValueFormatted() + "]";
		} catch (Exception e) {
			result += ", value=" + "ERROR" + "]";
		}

		return result;
    }

}
