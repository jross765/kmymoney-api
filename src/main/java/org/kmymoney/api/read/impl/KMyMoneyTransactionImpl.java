package org.kmymoney.api.read.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.Const;
import org.kmymoney.api.generated.PAIR;
import org.kmymoney.api.generated.SPLIT;
import org.kmymoney.api.generated.TRANSACTION;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.hlp.AmountFormatter_FP;
import org.kmymoney.api.read.impl.hlp.HasUserDefinedAttributesImpl;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Implementation of KMyMoneyTransaction that uses JWSDP.
 */
public class KMyMoneyTransactionImpl extends KMyMoneyObjectImpl 
									 implements KMyMoneyTransaction 
{
    @SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyTransactionImpl.class);

    protected static final DateTimeFormatter DATE_POSTED_FORMAT  = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
    protected static final DateTimeFormatter DATE_ENTERED_FORMAT = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
    
    // ---------------------------------------------------------------

    // the JWSDP-object we are facading.
    protected final TRANSACTION jwsdpPeer;

    protected List<KMyMoneyTransactionSplit> mySplits = null;

    // ---------------------------------------------------------------

    protected LocalDate entryDate;
    protected LocalDate postDate;

    // ---------------------------------------------------------------

    /**
     * Create a new Transaction, facading a JWSDP-transaction.
     *
     * @param peer    the JWSDP-object we are facading.
     * @param kmmFile the file to register under
     */
    @SuppressWarnings("exports")
    public KMyMoneyTransactionImpl(
	    final TRANSACTION peer, 
	    final KMyMoneyFile kmmFile) {
    	super(kmmFile);

    	if ( peer == null ) {
    		throw new IllegalArgumentException("argument <peer> is null");
    	}

    	if ( kmmFile == null ) {
    		throw new IllegalArgumentException("argument <kmmFile> is null");
    	}

    	this.jwsdpPeer = peer;
    }

    // Copy-constructor
    public KMyMoneyTransactionImpl(final KMyMoneyTransaction trx) {
    	super(trx.getKMyMoneyFile());

    	if ( trx.getJwsdpPeer() == null ) {
    		throw new IllegalArgumentException("Transaction not correctly initialized: null jwsdpPeer given");
    	}

    	if ( trx.getKMyMoneyFile() == null ) {
    		throw new IllegalArgumentException("Transaction not correctly initialized: null file given");
    	}

    	jwsdpPeer = trx.getJwsdpPeer();
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("exports")
    public TRANSACTION getJwsdpPeer() {
    	return jwsdpPeer;
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public boolean isBalanced() {
    	return getBalance().equals(new FixedPointNumber());
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean isBalancedRat() {
    	return getBalanceRat().equals(BigFraction.ZERO);
	}

    /**
     * {@inheritDoc}
     */
    public KMMQualifSecCurrID getQualifSecCurrID() {
    	if ( jwsdpPeer.getCommodity() == null )
    		return null;
    	
    	KMMQualifSecCurrID result = null;
    	if ( jwsdpPeer.getCommodity().startsWith(KMMQualifSecCurrID.PREFIX_SECURITY) ) {
    		// is security
    		result = new KMMQualifSecID(jwsdpPeer.getCommodity());
    	} else {
    		// is currency
    		result = new KMMQualifCurrID(jwsdpPeer.getCommodity());
    	}
    	
    	return result;
    }
    
	public Currency getCurrency() {
		if ( getQualifSecCurrID().getType() != KMMQualifSecCurrID.Type.CURRENCY ) {
			throw new IllegalStateException("Transaction security/currency is not of type " + KMMQualifSecCurrID.Type.CURRENCY);
		}

		String kmmCurrID = getQualifSecCurrID().getCode();
		return Currency.getInstance(kmmCurrID);
	}

	/**
     * {@inheritDoc}
     */
    @Deprecated
    public FixedPointNumber getBalance() {
    	FixedPointNumber fp = new FixedPointNumber();

    	for ( KMyMoneyTransactionSplit split : getSplits() ) {
    		fp.add(split.getValue());
    	}

    	return fp;
    }

    /**
     * {@inheritDoc}
     */
    public BigFraction getBalanceRat() {
		BigFraction fp = BigFraction.ZERO;

		for ( KMyMoneyTransactionSplit split : getSplits() ) {
			// CAUTION: BigFraction is immutable
			fp = fp.add(split.getValueRat());
		}

		return fp;
    }

    /**
     * {@inheritDoc}
     */
    public String getBalanceFormatted() {
    	return getBalanceFormatted(Locale.getDefault());
    }

    /**
     * {@inheritDoc}
     */
    public String getBalanceFormatted(final Locale lcl) {
    	return AmountFormatter_FP.formatAmount( getKMyMoneyFile(),
    											getBalance(), getQualifSecCurrID(), lcl );    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public FixedPointNumber getNegatedBalance() {
    	return getBalance().multiply(new FixedPointNumber("-100/100"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigFraction getNegatedBalanceRat() {
    	return getBalanceRat().negate();
    }

    /**
     * {@inheritDoc}
     */
    public String getNegatedBalanceFormatted() {
    	return getNegatedBalanceFormatted(Locale.getDefault());
    }

    /**
     * {@inheritDoc}
     */
    public String getNegatedBalanceFormatted(final Locale lcl) {
    	return AmountFormatter_FP.formatAmount( getKMyMoneyFile(),
    											getBalance(), getQualifSecCurrID(), lcl );
    }

    /**
     * {@inheritDoc}
     */
    public KMMTrxID getID() {
    	return new KMMTrxID( jwsdpPeer.getId() );
    }

    /**
     * {@inheritDoc}
     */
    public String getMemo() {
		if (jwsdpPeer.getMemo() == null)
			return null;
		
		if ( jwsdpPeer.getMemo().isBlank() )
			return null;
		
    	return jwsdpPeer.getMemo();
    }

    // ----------------------------

    /**
     * @param impl the split to add to mySplits
     */
    protected void addSplit(final KMyMoneyTransactionSplitImpl impl) {
    	if ( ! jwsdpPeer.getSPLITS().getSPLIT().contains(impl.getJwsdpPeer()) ) {
    		jwsdpPeer.getSPLITS().getSPLIT().add(impl.getJwsdpPeer());
    	}

    	// ::TODO ::CHECK: The following code is useless, isn't it?
    	Collection<KMyMoneyTransactionSplit> splits = getSplits();
    	if ( ! splits.contains(impl) ) {
    		splits.add(impl);
    	}
    }

    /**
     * {@inheritDoc}
     */
    public int getSplitsCount() {
    	return getSplits().size();
    }

    /**
     * {@inheritDoc}
     */
    public KMyMoneyTransactionSplit getSplitByID(final KMMSpltID spltID) {
		if ( spltID == null ) {
			throw new IllegalArgumentException("argument <splitID> is null");
		}

		if ( ! spltID.isSet() ) {
			throw new IllegalArgumentException("argument <splitID> is not set");
		}

		for ( KMyMoneyTransactionSplit split : getSplits() ) {
			if (split.getID().equals(spltID)) {
				return split;
			}

		}
		
		return null;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public KMyMoneyTransactionSplit getSplitByAccountID(KMMAcctID acctID) {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}

		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

		for ( KMyMoneyTransactionSplit split : getSplits() ) {
			if (split.getAccountID().equals(acctID)) {
				return split;
			}

		}
		
		return null;
	}

    /**
     * {@inheritDoc}
     */
    public List<KMyMoneyTransactionSplit> getSplits() {
    	return getSplits(false, false, false);
    }

    public List<KMyMoneyTransactionSplit> getSplits(
    		final boolean addToAcct, 
    		final boolean addToPye,
    		final boolean addToTags) {
    	if (mySplits == null) {
    		initSplits(addToAcct, addToPye, addToTags);
    	}
    	
    	return mySplits;
    }

    private void initSplits(
    		final boolean addToAcct, 
    		final boolean addToPye,
    		final boolean addToTags) {
	    List<SPLIT> jwsdpSplits = jwsdpPeer.getSPLITS().getSPLIT();

	    mySplits = new ArrayList<KMyMoneyTransactionSplit>();
	    for (SPLIT jwsdpSplt : jwsdpSplits) {
		mySplits.add(createSplit(jwsdpSplt,
								 addToAcct, addToPye, addToTags));
	    }
    }

    /**
     * Create a new split for a split found in the jaxb-data.
     *
     * @param jwsdpSplt the jaxb-data
     * @return the new split-instance
     */
    protected KMyMoneyTransactionSplitImpl createSplit(
    		final SPLIT jwsdpSplt,
    		final boolean addToAcct,
    		final boolean addToPye,
    		final boolean addToTags) {
    	return new KMyMoneyTransactionSplitImpl(jwsdpSplt, this, 
												addToAcct, addToPye, addToTags);
    }

    /**
     * {@inheritDoc}
     */
    public LocalDate getDateEntered() {
    	if ( entryDate == null ) {
    		XMLGregorianCalendar cal = jwsdpPeer.getEntrydate();
    		entryDate = LocalDate.of(cal.getYear(), cal.getMonth(), cal.getDay());
    	}

    	return entryDate;
    }

    /**
     * {@inheritDoc}
     */
    public String getDateEnteredFormatted() {
		try {
	    	DateTimeFormatter fmt = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
			return getDateEntered().format(fmt);
		} catch (Exception e) {
			return getDateEntered().toString();
		}
    }

    /**
     * {@inheritDoc}
     */
    public String getDatePostedFormatted() {
		try {
	    	DateTimeFormatter fmt = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
			return getDatePosted().format(fmt);
		} catch (Exception e) {
			return getDatePosted().toString();
		}
    }

    /**
     * {@inheritDoc}
     */
    public LocalDate getDatePosted() {
    	if ( postDate == null ) {
    		XMLGregorianCalendar cal = jwsdpPeer.getPostdate();
    		postDate = LocalDate.of(cal.getYear(), cal.getMonth(), cal.getDay());
    	}

    	return postDate;
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
	public String getUserDefinedAttribute(final String name) {
		if ( name == null ) {
			throw new IllegalArgumentException("argument <name> is null");
		}

		if ( name.isBlank() ) {
			throw new IllegalArgumentException("argument <name> is blank");
		}

		if ( jwsdpPeer.getKEYVALUEPAIRS() == null) {
			return null;
		}
		
		List<PAIR> kvpList = jwsdpPeer.getKEYVALUEPAIRS().getPAIR();
		return HasUserDefinedAttributesImpl.getUserDefinedAttributeCore(kvpList, name);
	}

    /**
     * {@inheritDoc}
     */
	public List<String> getUserDefinedAttributeKeys() {
		if ( jwsdpPeer.getKEYVALUEPAIRS() == null) {
			return null;
		}
		
		List<PAIR> kvpList = jwsdpPeer.getKEYVALUEPAIRS().getPAIR();
		return HasUserDefinedAttributesImpl.getUserDefinedAttributeKeysCore(kvpList);
	}

	// ---------------------------------------------------------------

    @Override
    public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("KMyMoneyTransactionImpl [");

		buffer.append("id=");
		buffer.append(getID());

		buffer.append(", balance=");
		buffer.append(getBalanceFormatted());

		buffer.append(", memo='");
		buffer.append(getMemo() + "'");

		buffer.append(", #splits=");
		try {
			buffer.append(getSplitsCount());
		} catch (Exception e) {
			buffer.append("ERROR");
		}

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

	@Override
	public String toXMLString() {
		try {
			JAXBContext context = JAXBContext.newInstance(TRANSACTION.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(jwsdpPeer, writer);
			return writer.toString();
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to convert JWSDP peer to XML", e);
		}
	}

	// For test purposes only
	public static KMyMoneyTransactionImpl fromXMLString(final String xmlStr, final KMyMoneyFile kmmFile) {
		TRANSACTION jwdspTrx = null;
		try {
			JAXBContext context = JAXBContext.newInstance(TRANSACTION.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			// unmarshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
			StringReader reader = new StringReader(xmlStr);
			jwdspTrx = (TRANSACTION) unmarshaller.unmarshal(new StreamSource(reader));
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to convert XML to " + TRANSACTION.class.getName(), e);
		}

		return new KMyMoneyTransactionImpl(jwdspTrx, kmmFile);
	}

	/**
	 * sorts primarily on the date the transaction happened and secondarily on the
	 * date it was entered.
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final KMyMoneyTransaction otherTrx) {
		try {
				int compare = otherTrx.getDatePosted().compareTo(getDatePosted());
				if ( compare != 0 ) {
					return compare;
				}
				return otherTrx.getDateEntered().compareTo(getDateEntered());
		} catch ( Exception e ) {
			// ::TODO
			e.printStackTrace();
			return 0;
	   }
	}

}
