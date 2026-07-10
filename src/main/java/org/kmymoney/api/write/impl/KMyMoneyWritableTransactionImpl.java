package org.kmymoney.api.write.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.kmymoney.api.generated.KEYVALUEPAIRS;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.generated.SPLIT;
import org.kmymoney.api.generated.SPLITS;
import org.kmymoney.api.generated.TRANSACTION;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneyTransactionImpl;
import org.kmymoney.api.read.impl.KMyMoneyTransactionSplitImpl;
import org.kmymoney.api.read.impl.hlp.KVPListDoesNotContainKeyException;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.api.write.impl.hlp.HasWritableUserDefinedAttributesImpl;
import org.kmymoney.api.write.impl.hlp.KMyMoneyWritableObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMIDNotSetException;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of KMyMoneyTransactionImpl to allow read-write access instead of
 * read-only access.
 */
public class KMyMoneyWritableTransactionImpl extends KMyMoneyTransactionImpl 
                                             implements KMyMoneyWritableTransaction 
{

	private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyWritableTransactionImpl.class);

	// ---------------------------------------------------------------

	// Our helper to implement the KMyMoneyWritableObject-interface.
	private final KMyMoneyWritableObjectImpl helper = new KMyMoneyWritableObjectImpl(getWritableKMyMoneyFile(), this);

	// ---------------------------------------------------------------

	/**
	 * @param kmmFile   the file we belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 */
	@SuppressWarnings("exports")
	public KMyMoneyWritableTransactionImpl(final TRANSACTION jwsdpPeer, final KMyMoneyFileImpl kmmFile) {
		super(jwsdpPeer, kmmFile);

		// repair a broken file
		if ( jwsdpPeer.getPostdate() == null ) {
			LOGGER.warn("Repairing broken transaction " + jwsdpPeer.getId() + " with no post-date");
			XMLGregorianCalendar datePosted = jwsdpPeer.getEntrydate();
			jwsdpPeer.setPostdate(datePosted);
		}
	}

	/**
	 * Create a new Transaction and add it to the file.
	 *
	 * @param file the file we belong to
	 */
	public KMyMoneyWritableTransactionImpl(final KMyMoneyWritableFileImpl file) {
		super(createTransaction_int(file, file.getNewTransactionID()), file);
		// file.addTransaction(this); NO! Redundant
	}

	public KMyMoneyWritableTransactionImpl(final KMyMoneyTransaction trx) {
		super(trx.getJwsdpPeer(), trx.getKMyMoneyFile());

		// ::TODO / ::CHECK
//		System.err.println("NOT IMPLEMENTED YET");
//		for ( KMyMoeneyTransactionSplit splt : trx.getSplits() ) 
//	   	{
//			addSplit(new KMyMoneyTransactionSplitImpl(splt.jwsdpPeer, trx));
//	    	}
	}

	// ---------------------------------------------------------------

	/**
	 * The KMyMoney file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	@Override
	public KMyMoneyWritableFileImpl getWritableKMyMoneyFile() {
		return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
	}

//	/**
//	 * The KMyMoney file is the top-level class to contain everything.
//	 *
//	 * @return the file we are associated with
//	 */
//	@Override
//	public KMyMoneyWritableFileImpl getKMyMoneyFile() {
//		return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
//	}

	// -----------------------------------------------------------

	/**
	 * Create a new split for a split found in the jaxb-data.
	 *
	 * @param jwsdpSplt the jaxb-data
	 * @return the new split-instance
	 */
	@Override
	protected KMyMoneyTransactionSplitImpl createSplit(
			final SPLIT jwsdpSplt,
		    final boolean addToAcct,
		    final boolean addToPye,
		    final boolean addToTags) {
		KMyMoneyWritableTransactionSplitImpl splt = 
				new KMyMoneyWritableTransactionSplitImpl(jwsdpSplt, this,
						                                 addToAcct, addToPye, addToTags);
		if ( helper.getPropertyChangeSupport() != null ) {
			 helper.getPropertyChangeSupport().firePropertyChange("splits", null, getWritableSplits());
		}

		return splt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KMyMoneyWritableTransactionSplit createWritableSplit(final KMyMoneyAccount acct) {
		if ( acct == null ) {
			throw new IllegalArgumentException("argument <acct> is null");
		}

		KMyMoneyWritableTransactionSplitImpl splt = new KMyMoneyWritableTransactionSplitImpl(this, acct);
		addSplit(splt);
		if ( helper.getPropertyChangeSupport() != null ) {
			 helper.getPropertyChangeSupport().firePropertyChange("splits", null, getWritableSplits());
		}
		
		return splt;
	}

	/**
	 * @see KMyMoneyWritableTransaction#createWritableSplit(KMyMoneyAccount)
	 */
	public KMyMoneyWritableTransactionSplit createWritableSplit(
			final KMyMoneyAccount acct, 
			final KMyMoneyPayee pye,
			final Collection<KMyMoneyTag> tagList) {
		if ( acct == null ) {
			throw new IllegalArgumentException("argument <acct> is null");
		}

		// Sic, null allowed here
//		if ( pye == null ) {
//			throw new IllegalArgumentException("argument <pye> is null");
//		}
		
		// Sic, null allowed here
//		if ( tagList == null ) {
//			throw new IllegalArgumentException("argument <tagList> is null");
//		}
		
		KMyMoneyWritableTransactionSplitImpl splt = new KMyMoneyWritableTransactionSplitImpl(this, acct, pye, tagList);
		addSplit(splt);
		if ( helper.getPropertyChangeSupport() != null ) {
			helper.getPropertyChangeSupport().firePropertyChange("splits", null, getWritableSplits());
		}
		
		return splt;
	}

	/**
	 * Creates a new transaction and add's it to the given KMyMoney file.
	 * Don't modify the ID of the new transaction!
	 */
	protected static TRANSACTION createTransaction_int(
			final KMyMoneyWritableFileImpl file, 
			final KMMTrxID newID) {
		if ( newID == null ) {
			throw new IllegalArgumentException("argument <newID> is null");
		}

		if ( ! newID.isSet() ) {
			throw new IllegalArgumentException("argument <newID> is not set");
		}

		// ObjectFactory fact = file.getObjectFactory();
		TRANSACTION jwsdpTrx = file.createTransactionType();

		jwsdpTrx.setId(newID.toString());

		try {
	        // https://stackoverflow.com/questions/835889/java-util-date-to-xmlgregoriancalendar
			// https://simplesolution.dev/java-convert-localdate-to-calendar/
	        GregorianCalendar cal = new GregorianCalendar();
	        ZonedDateTime zonedDateTime = ZonedDateTime.now();
	        Instant instant = zonedDateTime.toInstant();
	        Date date = Date.from(instant);
	        cal.setTime(date);
	        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
	        jwsdpTrx.setEntrydate(xmlCal);
		} catch ( DatatypeConfigurationException exc ) {
			throw new DateMappingException();
		}

		try {
			LocalDateTime dateTime = LocalDateTime.now();
	        // https://stackoverflow.com/questions/835889/java-util-date-to-xmlgregoriancalendar
			// https://simplesolution.dev/java-convert-localdate-to-calendar/
	        GregorianCalendar cal = new GregorianCalendar();
	        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
	        Instant instant = zonedDateTime.toInstant();
	        Date date = Date.from(instant);
	        cal.setTime(date);
	        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
			jwsdpTrx.setPostdate(xmlCal);
		} catch ( DatatypeConfigurationException exc ) {
			throw new DateMappingException();
		}

		{
			jwsdpTrx.setCommodity(file.getDefaultCurrencyID().toString());
		}

		{
			SPLITS splits = file.createSplitsType();
			
//			{
//				SPLIT splt = file.createSplitType();
//				splits.getSPLIT().add(splt);
//			}

			jwsdpTrx.setSPLITS(splits);
		}

		jwsdpTrx.setMemo("-");

        file.getRootElement().getTRANSACTIONS().getTRANSACTION().add(jwsdpTrx);
        file.setModified(true);

        LOGGER.debug("createTransaction_int: Created new transaction (core): " + jwsdpTrx.getId());
        
        return jwsdpTrx;
	}

	/**
	 * @param splt the split to remove from this transaction
	 */
	@Override
	public void removeSplit(final KMyMoneyWritableTransactionSplit splt) {
		jwsdpPeer.getSPLITS().getSPLIT()
			.remove(((KMyMoneyWritableTransactionSplitImpl) splt).getJwsdpPeer());
		getWritableKMyMoneyFile().setModified(true);

		if ( mySplits == null ) {
			// important!
			@SuppressWarnings("unused")
			List<KMyMoneyTransactionSplit> dummy = getSplits();
		} else {
			// That does not work with writable splits:
			// mySplits.remove(impl);
			// Instead:
			for ( int i = 0; i < mySplits.size(); i++ ) {
				if ( mySplits.get(i).getID().equals(splt.getID()) ) {
					mySplits.remove(i);
					break;
				}
			}
		}

		// NO, this is now done inside the following function call:
//		KMyMoneyWritableAccountImpl acct = (KMyMoneyWritableAccountImpl) splt.getAccount();
//		if ( acct != null ) {
//			acct.removeTransactionSplit(splt);
//		}

		getWritableKMyMoneyFile().removeTransactionSplit(splt, false); // <-- variant important
		// there is no count for splits up to now
		// getWritableFile().decrementCountDataFor()
		if ( helper.getPropertyChangeSupport() != null ) {
			helper.getPropertyChangeSupport().firePropertyChange("splits", null, getWritableSplits());
		}
	}

	@Override
	@Deprecated
	public void remove(final KMyMoneyWritableTransactionSplit impl) {
		removeSplit(impl);
	}

	@Override
	public void removeSplit(final KMMSpltID spltID) {
		KMMQualifSpltID qualifID = new KMMQualifSpltID(getID(), spltID);
		KMyMoneyWritableTransactionSplit splt = getWritableKMyMoneyFile().getWritableTransactionSplitByID(qualifID);
		removeSplit(splt);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KMyMoneyWritableTransactionSplit getWritableSplitByID(final KMMSpltID spltID) {
		// alt:
		// return (KMyMoneyWritableTransactionSplit) super.getSplitByID(id);
		if ( spltID == null ) {
			throw new IllegalArgumentException("argument <spltID> is null");
		}

		if ( ! spltID.isSet() ) {
			throw new IllegalArgumentException("argument <spltID> is not set");
		}

		KMyMoneyTransactionSplit splt = super.getSplitByID(spltID);
		// ::TODO
		// !!! Diese nicht-triviale Aenderung nochmal ganz genau abtesten !!!
		return new KMyMoneyWritableTransactionSplitImpl((KMyMoneyTransactionSplitImpl) splt, 
														false, false, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KMyMoneyWritableTransactionSplit getWritableSplitByAccountID(KMMAcctID acctID) {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}

		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

		KMyMoneyTransactionSplit splt = super.getSplitByAccountID(acctID);
		// ::TODO
		// !!! Diese nicht-triviale Aenderung nochmal ganz genau abtesten !!!
		return new KMyMoneyWritableTransactionSplitImpl((KMyMoneyTransactionSplitImpl) splt, 
														false, false, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends KMyMoneyWritableTransactionSplit> getWritableSplits() {
		List<KMyMoneyWritableTransactionSplit> result = new ArrayList<KMyMoneyWritableTransactionSplit>();
	
		for ( KMyMoneyTransactionSplit split : super.getSplits() ) {
			KMyMoneyWritableTransactionSplit newSplit = new KMyMoneyWritableTransactionSplitImpl((KMyMoneyTransactionSplitImpl) split, false, false, false);
			result.add(newSplit);
		}

		return result;
	}

	/**
	 * @param impl the split to add to mySplits
	 */
	protected void addSplit(final KMyMoneyWritableTransactionSplitImpl impl) {
		super.addSplit(impl);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		getWritableKMyMoneyFile().removeTransaction(this);
//		Collection<KMyMoneyWritableTransactionSplit> c = new ArrayList<KMyMoneyWritableTransactionSplit>();
//		c.addAll(getWritableSplits());
//		for ( KMyMoneyWritableTransactionSplit element : c ) {
//			element.remove();
//		}
	}

	/**
	 * @param id the new currency
	 * @see {@link KMyMoneyTransaction#getQualifSecCurrID()}
	 */
	public void setCurrencyID(final String id) {
		this.jwsdpPeer.setCommodity(id);
	}

	// ---------------------------------------------------------------

	/**
	 * @see KMyMoneyWritableTransaction#setDateEntered(LocalDate)
	 */
	@Override
	public void setDateEntered(final LocalDate dateEntered) {
		if ( dateEntered == null ) {
			throw new IllegalArgumentException("argument <dateEntered> is null");
		}

		this.entryDate = dateEntered;
		
		try {
	        // https://stackoverflow.com/questions/835889/java-util-date-to-xmlgregoriancalendar
			// https://simplesolution.dev/java-convert-localdate-to-calendar/
	        // https://stackoverflow.com/questions/47564200/xmlgregoriancalendar-in-java-with-no-timezone
	        GregorianCalendar cal = new GregorianCalendar();
	        ZonedDateTime zonedDateTime = dateEntered.atStartOfDay(ZoneId.systemDefault());
	        Instant instant = zonedDateTime.toInstant();
	        Date date = Date.from(instant);
	        cal.setTime(date);
	        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
	        xmlCal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			jwsdpPeer.setEntrydate(xmlCal);
		} catch ( DatatypeConfigurationException exc ) {
			throw new DateMappingException();
		}

		getWritableKMyMoneyFile().setModified(true);
	}

	/**
	 * @see KMyMoneyWritableTransaction#setDatePosted(LocalDate)
	 */
	@Override
	public void setDatePosted(final LocalDate datePosted) {
		if ( datePosted == null ) {
			throw new IllegalArgumentException("argument <datePosted> is null");
		}

		this.postDate = datePosted;
		
		try {
	        // https://stackoverflow.com/questions/835889/java-util-date-to-xmlgregoriancalendar
			// https://simplesolution.dev/java-convert-localdate-to-calendar/
	        // https://stackoverflow.com/questions/47564200/xmlgregoriancalendar-in-java-with-no-timezone
	        GregorianCalendar cal = new GregorianCalendar();
	        ZonedDateTime zonedDateTime = datePosted.atStartOfDay(ZoneId.systemDefault());
	        Instant instant = zonedDateTime.toInstant();
	        Date date = Date.from(instant);
	        cal.setTime(date);
	        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
	        xmlCal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			jwsdpPeer.setPostdate(xmlCal);
		} catch ( DatatypeConfigurationException exc ) {
			throw new DateMappingException();
		}

		getWritableKMyMoneyFile().setModified(true);
	}

	/**
	 * @see KMyMoneyWritableTransaction#setMemo(java.lang.String)
	 */
	public void setMemo(final String desc) {
		if ( desc == null ) {
			throw new IllegalArgumentException("argument <desc> is null. Please use the empty string instead of null for an empty description");
		}

		// Yes, empty descr is valid
//		if ( descr.isBlank() ) {
//			throw new IllegalArgumentException("argument <descr> is blank");
//		}

		String old = jwsdpPeer.getMemo();
		jwsdpPeer.setMemo(desc);
		getWritableKMyMoneyFile().setModified(true);

		if ( old == null || ! old.equals(desc) ) {
			if ( helper.getPropertyChangeSupport() != null ) {
				helper.getPropertyChangeSupport().firePropertyChange("description", old, desc);
			}
		}
	}

	// ---------------------------------------------------------------
	
	KMMSpltID getNewSplitID() {
		
		int maxSpltNo = 0;
		for ( KMyMoneyTransactionSplit splt : getSplits() ) {
			try {
				String coreID = splt.getID().get().substring(1);
				if ( Integer.parseInt(coreID) >= maxSpltNo ) {
					maxSpltNo = Integer.parseInt(coreID);
				}
			} catch ( KMMIDNotSetException exc ) {
				throw new CannotGenerateKMMIDException();
			}
		}
		
		return new KMMSpltID(maxSpltNo + 1);
	}

	// ---------------------------------------------------------------

	@Override
	public void addUserDefinedAttribute(final String name, final String value) {
		if ( name == null ) {
			throw new IllegalArgumentException("argument <name> is null");
		}
		
		if ( name.isBlank() ) {
			throw new IllegalArgumentException("argument <name> is emptys");
		}

		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}
		
		if ( value.isBlank() ) {
			throw new IllegalArgumentException("argument <value> is blank");
		}

		if ( jwsdpPeer.getKEYVALUEPAIRS() == null ) {
			ObjectFactory fact = getWritableKMyMoneyFile().getObjectFactory();
			KEYVALUEPAIRS newKVPs = fact.createKEYVALUEPAIRS();
			jwsdpPeer.setKEYVALUEPAIRS(newKVPs);
		}
		
		HasWritableUserDefinedAttributesImpl
			.addUserDefinedAttributeCore(jwsdpPeer.getKEYVALUEPAIRS(), getWritableKMyMoneyFile(), 
			                             name, value);
	}

	@Override
	public void removeUserDefinedAttribute(final String name) {
		if ( name == null ) {
			throw new IllegalArgumentException("argument <name> is null");
		}
		
		if ( name.isBlank() ) {
			throw new IllegalArgumentException("argument <name> is blank");
		}

		if ( jwsdpPeer.getKEYVALUEPAIRS() == null ) {
			throw new KVPListDoesNotContainKeyException();
		}
		
		HasWritableUserDefinedAttributesImpl
			.removeUserDefinedAttributeCore(jwsdpPeer.getKEYVALUEPAIRS(), getWritableKMyMoneyFile(), 
											name);
	}

	@Override
	public void setUserDefinedAttribute(final String name, final String value) {
		if ( name == null ) {
			throw new IllegalArgumentException("argument <name> is null");
		}
		
		if ( name.isBlank() ) {
			throw new IllegalArgumentException("argument <name> is blank");
		}

		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}
		
		if ( value.isBlank() ) {
			throw new IllegalArgumentException("argument <value> is blank");
		}

		if ( jwsdpPeer.getKEYVALUEPAIRS() == null ) {
			throw new KVPListDoesNotContainKeyException();
		}
		
		HasWritableUserDefinedAttributesImpl
			.setUserDefinedAttributeCore(jwsdpPeer.getKEYVALUEPAIRS(), getWritableKMyMoneyFile(), 
			                             name, value);
	}

	// ---------------------------------------------------------------

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("KMyMoneyWritableTransactionImpl [");

		buffer.append("id=");
		buffer.append(getID());

		buffer.append(", balance=");
		buffer.append(getBalanceFormatted());

		buffer.append(", memo='");
		buffer.append(getMemo() + "'");

		buffer.append(", #splits=");
		buffer.append(getSplitsCount());

		buffer.append(", date-posted=");
		try {
			buffer.append(getDatePosted().format(DATE_POSTED_FORMAT));
		} catch (Exception e) {
			buffer.append(getDatePosted().toString());
		}

		buffer.append(", date-entered=");
		try {
			buffer.append(getDateEntered().format(DATE_ENTERED_FORMAT));
		} catch (Exception e) {
			buffer.append(getDateEntered().toString());
		}

		buffer.append("]");

		return buffer.toString();
	}

}
