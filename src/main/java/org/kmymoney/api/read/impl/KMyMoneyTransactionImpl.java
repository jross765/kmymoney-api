package org.kmymoney.api.read.impl;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javax.xml.datatype.XMLGregorianCalendar;

import org.kmymoney.api.Const;
import org.kmymoney.api.generated.PAIR;
import org.kmymoney.api.generated.SPLIT;
import org.kmymoney.api.generated.TRANSACTION;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.hlp.HasUserDefinedAttributesImpl;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.TransactionSplitNotFoundException;
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

    // ---------------------------------------------------------------

    /**
     * @see KMyMoneyTransaction#getDatePosted()
     */
    protected LocalDate postDate;

    /**
     * @see KMyMoneyTransaction#getDateEntered()
     */
    protected LocalDate entryDate;

    /**
     * The Currency-Format to use if no locale is given.
     */
    protected NumberFormat currencyFormat;

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

    	this.jwsdpPeer = peer;
    }

    // Copy-constructor
    public KMyMoneyTransactionImpl(final KMyMoneyTransaction trx) {
    	super(trx.getKMyMoneyFile());

    	jwsdpPeer = trx.getJwsdpPeer();
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

    /**
	 * The Currency-Format to use if no locale is given.
	 *
	 * @return default currency-format with the transaction's currency set
	 */
	protected NumberFormat getCurrencyFormat() {
		if (currencyFormat == null) {
			currencyFormat = NumberFormat.getCurrencyInstance();
			if ( getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY ) { 
				currencyFormat.setCurrency(Currency.getInstance(getQualifSecCurrID().getCode()));
			} else {
				currencyFormat = NumberFormat.getInstance();
			}
		}
		
		return currencyFormat;
	}

    /**
     * {@inheritDoc}
     */
    public FixedPointNumber getBalance() {
    	FixedPointNumber fp = new FixedPointNumber();

    	for (KMyMoneyTransactionSplit split : getSplits()) {
    		fp.add(split.getValue());
    	}

    	return fp;
    }

    /**
     * {@inheritDoc}
     */
    public String getBalanceFormatted() {
    	return getCurrencyFormat().format(getBalance());
    }

    /**
     * {@inheritDoc}
     */
    public String getBalanceFormatted(final Locale loc) {

	NumberFormat cf = NumberFormat.getInstance(loc);
		if ( getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			cf.setCurrency(Currency.getInstance(getQualifSecCurrID().getCode()));
		} else {
			cf.setCurrency(null);
		}

		return cf.format(getBalance());
    }

    /**
     * {@inheritDoc}
     */
    public FixedPointNumber getNegatedBalance() {
    	return getBalance().multiply(new FixedPointNumber("-100/100"));
    }

    /**
     * {@inheritDoc}
     */
    public String getNegatedBalanceFormatted() {
    	return getCurrencyFormat().format(getNegatedBalance());
    }

    /**
     * {@inheritDoc}
     */
    public String getNegatedBalanceFormatted(final Locale loc) {
    	NumberFormat nf = NumberFormat.getInstance(loc);
    	if ( getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
    		nf.setCurrency(Currency.getInstance(getQualifSecCurrID().getCode()));
    	} else {
    		nf.setCurrency(null);
    	}

    	return nf.format(getNegatedBalance());
    }

    /**
     * {@inheritDoc}
     */
    public KMMTrxID getID() {
    	return new KMMTrxID(jwsdpPeer.getId());
    }

    /**
     * {@inheritDoc}
     */
    public String getMemo() {
    	return jwsdpPeer.getMemo();
    }

    // ----------------------------

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("exports")
    public TRANSACTION getJwsdpPeer() {
    	return jwsdpPeer;
    }

    // ----------------------------

    /**
     * @see #getSplits()
     */
    protected List<KMyMoneyTransactionSplit> mySplits = null;

    /**
     * @param impl the split to add to mySplits
     */
    protected void addSplit(final KMyMoneyTransactionSplitImpl impl) {
    	if (!jwsdpPeer.getSPLITS().getSPLIT().contains(impl.getJwsdpPeer())) {
    		jwsdpPeer.getSPLITS().getSPLIT().add(impl.getJwsdpPeer());
    	}

    	Collection<KMyMoneyTransactionSplit> splits = getSplits();
    	if (!splits.contains(impl)) {
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
			throw new IllegalArgumentException("split-ID is null");
		}

		if ( ! spltID.isSet() ) {
			throw new IllegalArgumentException("split-ID is not set");
		}

		for (KMyMoneyTransactionSplit split : getSplits()) {
			if (split.getID().equals(spltID)) {
				return split;
			}

		}
		
		return null;
    }

    /**
     * {@inheritDoc}
     */
    public KMyMoneyTransactionSplit getFirstSplit() throws TransactionSplitNotFoundException {
    	if ( getSplits().size() == 0 )
    		throw new TransactionSplitNotFoundException();
	
    	return getSplits().get(0);
    }

    /**
     * {@inheritDoc}
     */
    public KMyMoneyTransactionSplit getSecondSplit() throws TransactionSplitNotFoundException {
    	if ( getSplits().size() <= 1 )
    		throw new TransactionSplitNotFoundException();
	
    	return getSplits().get(1);
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
    	if (entryDate == null) {
    		String dateStr = jwsdpPeer.getEntrydate();
    		entryDate = LocalDate.parse(dateStr);
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
    	if (postDate == null) {
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
			throw new IllegalArgumentException("null name given");
		}

		if ( name.trim().equals("") ) {
			throw new IllegalArgumentException("empty name given");
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

    /**
     * sorts primarily on the date the transaction happened and secondarily on the
     * date it was entered.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final KMyMoneyTransaction otherTrx) {
    	try {
    		int compare = otherTrx.getDatePosted().compareTo(getDatePosted());
    		if (compare != 0) {
    			return compare;
    		}

    		return otherTrx.getDateEntered().compareTo(getDateEntered());
    	} catch (Exception e) {
    		e.printStackTrace();
    		return 0;
    	}
    }

}
