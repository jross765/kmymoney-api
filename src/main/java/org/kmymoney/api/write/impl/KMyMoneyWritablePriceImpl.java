package org.kmymoney.api.write.impl;

import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.kmymoney.api.basetypes.complex.KMMPriceID; // <-- sic, API.basetypes, not BASE.basetypes
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.generated.PRICE;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.KMyMoneyPriceImpl;
import org.kmymoney.api.read.impl.KMyMoneyPricePairImpl;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritablePrice;
import org.kmymoney.api.write.KMyMoneyWritablePricePair;
import org.kmymoney.api.write.impl.hlp.KMyMoneyWritableObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Extension of KMMPriceImpl to allow read-write access instead of
 * read-only access.
 */
public class KMyMoneyWritablePriceImpl extends KMyMoneyPriceImpl 
                                       implements KMyMoneyWritablePrice 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyWritablePriceImpl.class);

    // ---------------------------------------------------------------

    private KMyMoneyWritablePricePair wrtblParent = null;

    // Our helper to implement the KMyMoneyWritableObject-interface.
    private final KMyMoneyWritableObjectImpl helper = new KMyMoneyWritableObjectImpl(getWritableKMyMoneyFile(), this);

    // ---------------------------------------------------------------

    @SuppressWarnings("exports")
    public KMyMoneyWritablePriceImpl(
    		final KMyMoneyWritablePricePair parent,
    		final PRICE jwsdpPeer,
    		final KMyMoneyWritableFile file) {
    	super(parent, jwsdpPeer, file);
    	this.wrtblParent = parent;
    }

    public KMyMoneyWritablePriceImpl(
    		final KMyMoneyPricePairImpl prcPr,
    		final KMyMoneyWritableFileImpl file) {
    	super(prcPr, createPrice_int(prcPr, file), file);
    	
    	if ( prcPr instanceof KMyMoneyWritablePricePair ) {
    		this.wrtblParent = (KMyMoneyWritablePricePair) prcPr;
    	} else {
        	this.wrtblParent = new KMyMoneyWritablePricePairImpl((KMyMoneyPricePairImpl) prcPr);
    	}
    }

    public KMyMoneyWritablePriceImpl(final KMyMoneyPriceImpl prc) {
    	super(prc.getParentPricePair(), prc.getJwsdpPeer(), prc.getKMyMoneyFile());
    	
    	if ( prc.getParentPricePair() instanceof KMyMoneyWritablePricePair ) {
    		this.wrtblParent = (KMyMoneyWritablePricePair) prc.getParentPricePair();
    	} else {
        	this.wrtblParent = new KMyMoneyWritablePricePairImpl((KMyMoneyPricePairImpl) prc.getParentPricePair());
    	}
    }

    // ---------------------------------------------------------------

	/**
     * The KMyMoney file is the top-level class to contain everything.
     *
     * @return the file we are associated with
     */
    public KMyMoneyWritableFileImpl getWritableKMyMoneyFile() {
    	return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
    }

    /**
     * The KMyMoney file is the top-level class to contain everything.
     *
     * @return the file we are associated with
     */
    @Override
    public KMyMoneyWritableFileImpl getKMyMoneyFile() {
    	return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
    }

    // ---------------------------------------------------------------
    
    private static PRICE createPrice_int(
    		final KMyMoneyPricePairImpl prntPrcPr,
    		final KMyMoneyWritableFileImpl file) {
        ObjectFactory factory = file.getObjectFactory();
        
        PRICE jwsdpPrc = file.createPriceType();
        
        jwsdpPrc.setSource(Source.USER.getCode());
        
        LocalDate dateNow = LocalDate.now();
        
        try {
            // https://stackoverflow.com/questions/835889/java-util-date-to-xmlgregoriancalendar
			// https://stackoverflow.com/questions/49667772/localdate-to-gregoriancalendar-conversion
			// CAUTION: The following two lines with new Date() do not work so well.
//            GregorianCalendar cal = new GregorianCalendar();
//            cal.setTime(new Date());
			GregorianCalendar cal = GregorianCalendar
					.from( dateNow.atStartOfDay().atZone(ZoneId.systemDefault()) );
            XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
            jwsdpPrc.setDate(xmlCal);
        } catch ( DatatypeConfigurationException exc ) {
        	throw new DateMappingException();
        }
        
        jwsdpPrc.setPrice("1");
        
        prntPrcPr.getJwsdpPeer().getPRICE().add(jwsdpPrc); // <-- NOT parent (yet)!
        file.setModified(true);
    
        KMMPriceID prcID = new KMMPriceID(prntPrcPr, DATE_FORMAT.format(dateNow));
        LOGGER.debug("createPrice_int: Created new price (core): " + prcID.toString());
        
        return jwsdpPrc;
    }

    // ---------------------------------------------------------------

	@Override
	public void setFromSecCurrStr(String secCurr) {
		if ( secCurr == null )
			throw new IllegalArgumentException("null security/currency given");

		if ( secCurr.trim().length() == 0 )
			throw new IllegalArgumentException("empty security/currency given");

		setFromCurrencyQualifID(new KMMQualifCurrID(secCurr));
	}

	@Override
	public void setToCurrStr(String curr) {
		if ( curr == null )
			throw new IllegalArgumentException("null currency given");

		if ( curr.trim().length() == 0 )
			throw new IllegalArgumentException("empty currency given");

		setToCurrencyQualifID(new KMMQualifCurrID(curr));
	}
    
    // ----------------------------

    @Override
    public void setFromSecCurrQualifID(final KMMQualifSecCurrID qualifID) {
		if ( qualifID == null ) {
			throw new IllegalArgumentException("null security/currency ID given");
		}

		if ( ! qualifID.isSet() ) {
			throw new IllegalArgumentException("unset security/currency ID given");
		}

    	wrtblParent.setFromSecCurrQualifID(qualifID);
    	getWritableKMyMoneyFile().setModified(true);
    }

    @Override
    public void setFromSecurityQualifID(final KMMQualifSecID qualifID) {
		if ( qualifID == null ) {
			throw new IllegalArgumentException("null security ID given");
		}

		if ( ! qualifID.isSet() ) {
			throw new IllegalArgumentException("unset security ID given");
		}

    	wrtblParent.setFromSecurityQualifID(qualifID);
    	getWritableKMyMoneyFile().setModified(true);
    }

    @Override
    public void setFromCurrencyQualifID(final KMMQualifCurrID qualifID) {
		if ( qualifID == null ) {
			throw new IllegalArgumentException("null currency ID given");
		}

		if ( ! qualifID.isSet() ) {
			throw new IllegalArgumentException("unset currency ID given");
		}

    	wrtblParent.setFromCurrencyQualifID(qualifID);
    	getWritableKMyMoneyFile().setModified(true);
    }

    @Override
    public void setFromSecurity(final KMyMoneySecurity sec) {
    	wrtblParent.setFromSecurity(sec);
    	getWritableKMyMoneyFile().setModified(true);
    }

    @Override
    public void setFromCurrencyCode(final String code) {
    	wrtblParent.setFromCurrencyCode(code);
    	getWritableKMyMoneyFile().setModified(true);
    }

    @Override
    public void setFromCurrency(final KMyMoneyCurrency curr) {
    	wrtblParent.setFromCurrency(curr);
    	getWritableKMyMoneyFile().setModified(true);
    }
    
    // ----------------------------

    @Override
    public void setToCurrencyQualifID(KMMQualifCurrID qualifID) {
    	wrtblParent.setToCurrencyQualifID(qualifID);
    	getWritableKMyMoneyFile().setModified(true);
    }

    @Override
    public void setToCurrencyCode(String code) {
    	wrtblParent.setToCurrencyCode(code);
    	getWritableKMyMoneyFile().setModified(true);
    }

    @Override
    public void setToCurrency(KMyMoneyCurrency curr) {
    	wrtblParent.setToCurrency(curr);
    	getWritableKMyMoneyFile().setModified(true);
    }
    
    // ---------------------------------------------------------------

    @Override
    public KMyMoneyWritablePricePair getWritableParentPricePair() {
    	return wrtblParent;
    }
    
    // ---------------------------------------------------------------

    public void setParentPricePairID(KMMPricePairID prcPrID) {
		if ( prcPrID == null )
			throw new IllegalArgumentException("null price pair ID given");

		if ( ! prcPrID.isSet() )
			throw new IllegalArgumentException("unset price pair ID given");

		KMMPricePairID oldPrcPrID = getParentPricePairID();

		getWritableParentPricePair().setID(prcPrID);
		getWritableKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null ) {
			propertyChangeSupport.firePropertyChange("pricePairID", oldPrcPrID, prcPrID);
		}
    }
	
    public void setParentPricePair(KMyMoneyPricePair prcPr) {
		if ( prcPr == null )
			throw new IllegalArgumentException("null price pair given");

    }
	
    // ---------------------------------------------------------------

    @Override
	public void setDate(LocalDate date) {
		if ( date == null )
			throw new IllegalArgumentException("null date given");

		LocalDate oldDate = getDate();
		
		try {
            // https://stackoverflow.com/questions/835889/java-util-date-to-xmlgregoriancalendar
			// https://stackoverflow.com/questions/49667772/localdate-to-gregoriancalendar-conversion
			// https://stackoverflow.com/questions/14060161/specify-the-date-format-in-xmlgregoriancalendar
			this.date = date;
			// CAUTION: The following two lines with new Date(...) do not work (reliably)
//	        GregorianCalendar cal = new GregorianCalendar();
//	        cal.setTime(new Date(this.date.getYear(),
//	        		             this.date.getMonthValue(),
//	        		             this.date.getDayOfMonth()));
			GregorianCalendar cal = GregorianCalendar 
					.from( date.atStartOfDay(ZoneId.systemDefault()) );
	        XMLGregorianCalendar xmlCal = 
	        		DatatypeFactory.newInstance().newXMLGregorianCalendarDate(
	        				cal.get(Calendar.YEAR), 
	        				cal.get(Calendar.MONTH) + 1, 
	        				cal.get(Calendar.DAY_OF_MONTH), 
	        				DatatypeConstants.FIELD_UNDEFINED);
			jwsdpPeer.setDate(xmlCal);
		} catch ( DatatypeConfigurationException exc ) {
			throw new DateMappingException();
		}
		
		getWritableKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null ) {
			propertyChangeSupport.firePropertyChange("price", oldDate, date);
		}
	}

    @Override
    public void setDateTime(LocalDateTime dateTime) {
		if ( dateTime == null )
			throw new IllegalArgumentException("null code given");

		setDate(dateTime.toLocalDate());
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSource(Source src) {
		setSourceStr(src.getCode());
    }

    /**
     * <b>Using this method is discouraged.</b>
     * Use {@link #setSource(org.kmymoney.api.read.KMyMoneyPrice.Source)} whenever possible/applicable instead.
     * 
     * @return
     * 
     * @see #setSource(org.kmymoney.api.read.KMyMoneyPrice.Source)
     */
    public void setSourceStr(String srcStr) {
		if ( srcStr == null )
			throw new IllegalArgumentException("null source given");

		if ( srcStr.trim().length() == 0 )
			throw new IllegalArgumentException("empty source given");

		String oldSrc = getSourceStr();

		jwsdpPeer.setSource(srcStr);
		getWritableKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null ) {
			propertyChangeSupport.firePropertyChange("price", oldSrc, srcStr);
		}
    }

    @Override
    public void setValue(FixedPointNumber val) {
		if ( val == null )
			throw new IllegalArgumentException("null value given");

		FixedPointNumber oldVal = getValue();

		jwsdpPeer.setPrice(val.toKMyMoneyString());
		getWritableKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null ) {
			propertyChangeSupport.firePropertyChange("price", oldVal, val);
		}
    }

    // ---------------------------------------------------------------
    
    @Override
    public String toString() {
		String result = "KMyMoneyWritablePriceImpl [";

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
