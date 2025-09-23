package org.kmymoney.api.write.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;

import org.kmymoney.api.generated.ACCOUNT;
import org.kmymoney.api.generated.KEYVALUEPAIRS;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.generated.SUBACCOUNT;
import org.kmymoney.api.generated.SUBACCOUNTS;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyAccountImpl;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.hlp.KVPListDoesNotContainKeyException;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.api.write.impl.hlp.HasWritableUserDefinedAttributesImpl;
import org.kmymoney.api.write.impl.hlp.KMyMoneyWritableObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.UnknownAccountTypeException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Extension of KMyMoneyAccountImpl to allow writing instead of
 * read-only access.
 */
public class KMyMoneyWritableAccountImpl extends KMyMoneyAccountImpl 
                                         implements KMyMoneyWritableAccount 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyWritableAccountImpl.class);

    // ---------------------------------------------------------------

    // Our helper to implement the KMyMoneyWritableObject-interface.
    private final KMyMoneyWritableObjectImpl helper = new KMyMoneyWritableObjectImpl(getWritableKMyMoneyFile(), this);

	// Used by ${@link #getBalance()} to cache the result.
	private FixedPointNumber myBalanceCached = null;

	// Used by ${@link #getBalance()} to cache the result.
	private PropertyChangeListener myBalanceCachedInvalidator = null;

    // ---------------------------------------------------------------

	/**
	 * @param jwsdpPeer 
	 * @param file 
	 */
	@SuppressWarnings("exports")
	public KMyMoneyWritableAccountImpl(final ACCOUNT jwsdpPeer, final KMyMoneyFileImpl file) {
		super(jwsdpPeer, file);
	}

	/**
	 * @param file 
	 */
	public KMyMoneyWritableAccountImpl(final KMyMoneyWritableFileImpl file) {
		super(createAccount_int(file, file.getNewAccountID()), file);
	}
	
	public KMyMoneyWritableAccountImpl(final KMyMoneyAccountImpl acct, final boolean addSplits) {
		super(acct.getJwsdpPeer(), acct.getKMyMoneyFile());

		if (addSplits) {
		    for ( KMyMoneyTransactionSplit splt : ((KMyMoneyFileImpl) acct.getKMyMoneyFile()).getTransactionSplits_readAfresh() ) {
		    	if ( ! acct.isRootAccount() &&
		    		 splt.getAccountID().equals(acct.getID()) ) {
		    		super.addTransactionSplit(splt);
			    // NO:
//				    addTransactionSplit(new KMyMoneyTransactionSplitImpl(splt.getJwsdpPeer(), splt.getTransaction(), 
//		                                false, false));
		    	}
		    }
		}
	}

	// ---------------------------------------------------------------

	/**
	 * @param file
	 * @return
	 */
	private static ACCOUNT createAccount_int(
			final KMyMoneyWritableFileImpl file, 
			final KMMAcctID newID) {
		if ( newID == null ) {
			throw new IllegalArgumentException("null ID given");
		}

		if ( ! newID.isSet() ) {
			throw new IllegalArgumentException("unset ID given");
		}

		ACCOUNT jwsdpAcct = file.createAccountType();
		
		jwsdpAcct.setId(newID.toString());
		jwsdpAcct.setType(KMyMoneyAccount.Type.ASSET.getCodeBig());
		jwsdpAcct.setName("UNNAMED");
		jwsdpAcct.setDescription("no description yet");
		jwsdpAcct.setCurrency(file.getDefaultCurrencyID());

		file.getRootElement().getACCOUNTS().getACCOUNT().add(jwsdpAcct);
		file.setModified(true);
		return jwsdpAcct;
	}

	/**
	 * Remove this account from the system.<br/>
	 * Throws IllegalStateException if this account has splits or childres.
	 */
	public void remove() {
		if ( hasTransactions() ) {
			throw new IllegalStateException("Cannot remove account while it contains transaction-splits!");
		}
		
		if ( this.getChildren().size() > 0 ) {
			throw new IllegalStateException("Cannot remove account while it contains child-accounts!");
		}

		getWritableKMyMoneyFile().getRootElement().getACCOUNTS().getACCOUNT().remove(jwsdpPeer);
		getWritableKMyMoneyFile().removeAccount(this);
	}

	// ---------------------------------------------------------------

	/**
	 * @see KMyMoneyAccount#addTransactionSplit(KMyMoneyTransactionSplit)
	 */
	@Override
	public void addTransactionSplit(final KMyMoneyTransactionSplit splt) {
		if ( splt == null ) {
			throw new IllegalArgumentException("null split given");
		}
		
		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Setting name is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Setting name is forbidden for top-level accounts");
		}
		
		super.addTransactionSplit(splt);

		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("transactionSplits", null, getTransactionSplits());
		}
	}

	/**
	 * @param impl the split to remove
	 */
	protected void removeTransactionSplit(final KMyMoneyWritableTransactionSplit splt) {
		if ( splt == null ) {
			throw new IllegalArgumentException("null split given");
		}
		
		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Setting name is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Setting name is forbidden for top-level accounts");
		}
		
		List<KMyMoneyTransactionSplit> transactionSplits = getTransactionSplits();
		// That does not work with writable splits:
		// transactionSplits.remove(splt);
		// Instead:
		for ( int i = 0; i < transactionSplits.size(); i++ ) {
			if ( transactionSplits.get(i).getID().equals(splt.getID())) {
				transactionSplits.remove(i);
				i--;
			}
		}

		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("transactionSplits", null, transactionSplits);
		}
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public void setName(final String name) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Setting name is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Setting name is forbidden for top-level accounts");
		}
		
		String oldName = getName();
		if ( oldName == name ) {
			return; // nothing has changed
		}
		
		this.jwsdpPeer.setName(name);
		setIsModified();
		
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("name", oldName, name);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInstitutionID(final KMMInstID instID) {
		if ( instID == null ) {
			throw new IllegalArgumentException("null institution-ID given!");
		}

		if ( ! instID.isSet() ) {
			throw new IllegalArgumentException("unset institution-ID given!");
		}

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Setting institution ID for is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Setting institution ID is forbidden for top-level accounts");
		}
		
		KMMInstID oldInstID = getInstitutionID();
		// ::TODO: Diese extra-Abfrage auf null ist bei optionalen Feldern noetig
		// (oder besser bei allen) ==> ueberall sonst einbauen 
		if ( oldInstID != null ) {
			if ( oldInstID == instID ||
				 oldInstID.equals(instID) ) {
				return; // nothing has changed
			}
		}
		
		this.jwsdpPeer.setInstitution(instID.toString());
		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("institution", oldInstID, instID);
		}
	}

	// ----------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setQualifSecCurrID(final KMMQualifSecCurrID secCurrID) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("null security/currency ID given!");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("unset security/currency ID given!");
		}

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Setting security/currency ID is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Setting security/currency ID is forbidden for top-level accounts");
		}
		
		KMMQualifSecCurrID oldCurrId = getQualifSecCurrID();
		if ( oldCurrId == secCurrID ) {
			return; // nothing has changed
		}
		
		this.jwsdpPeer.setCurrency(secCurrID.getCode());
		setIsModified();
		
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("currencyID", oldCurrId, secCurrID.getCode());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSecID(final KMMSecID secID) {
		if ( secID == null ) {
			throw new IllegalArgumentException("null security ID given");
		}
		
		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("unset security ID given");
		}
		
		setQualifSecCurrID(new KMMQualifSecID(secID));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrency(final Currency curr) {
		if ( curr == null ) {
			throw new IllegalArgumentException("null curreny given");
		}
		
		setQualifSecCurrID(new KMMQualifCurrID(curr));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrency(final String currCode) {
		if ( currCode == null ) {
			throw new IllegalArgumentException("null currency code given");
		}
		
		if ( currCode.isEmpty() ) {
			throw new IllegalArgumentException("empty currency code given");
		}

		setCurrency(Currency.getInstance(currCode));
	}
	
	// ----------------------------

	protected void setIsModified() {
		KMyMoneyWritableFile writableFile = getWritableKMyMoneyFile();
		writableFile.setModified(true);
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getBalance() {
		if ( myBalanceCached != null ) {
			return myBalanceCached;
		}

		List<KMyMoneyTransactionSplit> after = new ArrayList<KMyMoneyTransactionSplit>();
		FixedPointNumber balance = getBalance(LocalDate.now(), after);

		if ( after.isEmpty() ) {
			myBalanceCached = balance;

			// add a listener to keep the cache up to date
			if ( myBalanceCachedInvalidator != null ) {
				myBalanceCachedInvalidator = new PropertyChangeListener() {
					private final Collection<KMyMoneyTransactionSplit> splitsWeAreAddedTo = new HashSet<KMyMoneyTransactionSplit>();

					public void propertyChange(final PropertyChangeEvent evt) {
						myBalanceCached = null;

						// we don't handle the case of removing an account
						// because that happens seldomly enough

						if ( evt.getPropertyName().equals("account") && 
							 evt.getSource() instanceof KMyMoneyWritableTransactionSplit ) {
							KMyMoneyWritableTransactionSplit splitw = (KMyMoneyWritableTransactionSplit) evt.getSource();
							if ( splitw.getAccount() != KMyMoneyWritableAccountImpl.this ) {
								helper.removePropertyChangeListener("account", this);
								helper.removePropertyChangeListener("shares", this);
								helper.removePropertyChangeListener("datePosted", this);
								splitsWeAreAddedTo.remove(splitw);
							}

						}
						
						if ( evt.getPropertyName().equals("transactionSplits") ) {
							Collection<KMyMoneyTransactionSplit> splits = (Collection<KMyMoneyTransactionSplit>) evt.getNewValue();
							for ( KMyMoneyTransactionSplit split : splits ) {
								if ( ! (split instanceof KMyMoneyWritableTransactionSplit) || 
									 splitsWeAreAddedTo.contains(split) ) {
									continue;
								}
								KMyMoneyWritableTransactionSplit splitw = (KMyMoneyWritableTransactionSplit) split;
								helper.addPropertyChangeListener("account", this);
								helper.addPropertyChangeListener("shares", this);
								helper.addPropertyChangeListener("datePosted", this);
								splitsWeAreAddedTo.add(splitw);
							}
						}
					}
				};
				
				helper.addPropertyChangeListener("currencyID", myBalanceCachedInvalidator);
				helper.addPropertyChangeListener("currencyNameSpace", myBalanceCachedInvalidator);
				helper.addPropertyChangeListener("transactionSplits", myBalanceCachedInvalidator);
			}
		}

		return balance;
	}

	/**
	 * {@inheritDoc}
	 */
	public FixedPointNumber getBalanceChange(final LocalDate from, final LocalDate to) {
		FixedPointNumber retval = new FixedPointNumber();
	
		for ( KMyMoneyTransactionSplit splt : getTransactionSplits() ) {
			LocalDate whenHappened = splt.getTransaction().getDatePosted();
			
			if ( !whenHappened.isBefore(to) ) {
				continue;
			}
			
			if ( whenHappened.isBefore(from) ) {
				continue;
			}
			
			retval = retval.add(splt.getShares());
		}
		
		return retval;
	}
	
	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public void setMemo(final String descr) {
		if ( descr == null ) {
			throw new IllegalArgumentException("null description given");
		}
		
    	// Caution: empty string allowed here
		// if ( descr.isEmpty() ) {
		//   throw new IllegalArgumentException("empty description given");
		// }

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Setting memo for top-level account is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Setting memo is forbidden for top-level accounts");
		}
		
		String oldDescr = getMemo();
		if ( oldDescr == descr ) {
			return; // nothing has changed
		}
		
		jwsdpPeer.setDescription(descr);
		setIsModified();
		
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("description", oldDescr, descr);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setType(final KMyMoneyAccount.Type type) {
		setTypeBigInt(type.getCodeBig());
	}

	/**
     * <b>Using this method is discouraged.</b>
     * Use {@link #setType(org.kmymoney.api.read.KMyMoneyAccount.Type)} whenever possible/applicable instead.
     * 
     * @return
     * 
     * @see #setType(org.kmymoney.api.read.KMyMoneyAccount.Type)
	 */
	public void setTypeBigInt(final BigInteger typeInt) {
		if ( typeInt == null ) {
			throw new IllegalArgumentException("null type given!");
		}

		if ( typeInt.intValue() <= 0 ) {
			throw new IllegalArgumentException("type <= 0 given!");
		}

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Setting type is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Setting type is forbidden for top-level accounts");
		}
		
		BigInteger oldType = getTypeBigInt();
		if ( oldType == typeInt ) {
			return; // nothing has changed
		}
		
		// ::CHECK
		// Not sure whether we should allow this action at all...
		// It does happen that you set an account's type wrong by accident,
		// and it should be possibly to correct that.
		// It does not seem prudent to change an account's type when
		// there are already transactions pointing to/from it.
		if ( hasTransactions() ) {
	    	LOGGER.error("Changing account type is forbidden for accounts that already contain transactions: " + getID());
			throw new UnsupportedOperationException("Changing account type is forbidden for accounts that already contain transactions: " + getID());
		}
    	
		jwsdpPeer.setType(typeInt);
		setIsModified();
		
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("type", oldType, typeInt);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setParentAccountID(final KMMComplAcctID prntAcctID) {
		if ( prntAcctID == null ) {
			setParentAccount(null);
			return;
		}

		if ( ! prntAcctID.isSet() ) {
			throw new IllegalArgumentException("unset account ID given!");
		}

		// check if new parent is a child-account recursively
		KMyMoneyWritableAccount prntAcct = getWritableKMyMoneyFile().getWritableAccountByID(prntAcctID); // sic, writable version, needed second step
		if ( isChildAccountRecursive(prntAcct) ) {
			throw new IllegalArgumentException("An account may not be set as its own (grand-)parent");
		}

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Setting parent-account ID is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Setting parent account ID is forbidden for top-level accounts");
		}
		
		// 1) In child account, set reference to parent account
		KMMComplAcctID oldPrntAcctID = getParentAccountID();
		jwsdpPeer.setParentaccount(prntAcctID.toString());
		setIsModified();

		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("parentAccount", oldPrntAcctID, prntAcctID);
		}
		
		// 2) In parent account, add reference to child account
		//    (This redundancy is a specific feature of the KMyMoney file format.
		//    Things will not work correctly if we do not add this reference)
		// 
		// ::TODO / ::CHECK: I do not really want to have this code public -- on the other
		// hand, it would make the code cleaner, and we could add the property change
		// to the parent account as well. Sleep over it and re-evaluate in a while.
		ACCOUNT jwsdpPrntAcct = ((KMyMoneyWritableAccountImpl) prntAcct).getJwsdpPeer();
		// 2.1) Check if list of sub-accounts already contains the new child
		//      This should not happen, but just in case...
		if ( jwsdpPrntAcct.getSUBACCOUNTS() == null ) {
			LOGGER.debug("setParentAccountID: Parent account " + prntAcctID.toString() + " has not references to child accounts yet.");
			SUBACCOUNTS jwsdpChldAcctWrp = getWritableKMyMoneyFile().getObjectFactory().createSUBACCOUNTS();
			jwsdpPrntAcct.setSUBACCOUNTS(jwsdpChldAcctWrp);
		}
		
		List<SUBACCOUNT> jwsdpChldAcctList = jwsdpPrntAcct.getSUBACCOUNTS().getSUBACCOUNT();
		for ( SUBACCOUNT jwsdpChldAcct : jwsdpChldAcctList ) {
			if ( jwsdpChldAcct.getId().equals( getID().toString() ) ) {
				LOGGER.warn("setParentAccountID: Parent account " + prntAcctID.toString() + " already contains reference to " + getID().toString() + ". Nothing to do.");
				return;
			}
		}
		// 2.2) Add reference to child
		SUBACCOUNT jwsdpChldAcctRef = getWritableKMyMoneyFile().getObjectFactory().createSUBACCOUNT();
		jwsdpChldAcctRef.setId(getID().toString());
		jwsdpPrntAcct.getSUBACCOUNTS().getSUBACCOUNT().add(jwsdpChldAcctRef);
		LOGGER.debug("setParentAccountID: Added reference to child-account " + getID().toString() + " in " + prntAcctID.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setParentAccount(final KMyMoneyAccount prntAcct) {
		if ( prntAcct == null ) {
			this.jwsdpPeer.setParentaccount(null);
			return;
		}

		if ( prntAcct == this ) {
			throw new IllegalArgumentException("I cannot be my own parent!");
		}

		setParentAccountID(prntAcct.getID());
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public KMyMoneyWritableFileImpl getWritableKMyMoneyFile() {
		return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KMyMoneyWritableFileImpl getKMyMoneyFile() {
		return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
	}

	// -------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addUserDefinedAttribute(final String name, final String value) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

		if ( value == null ) {
			throw new IllegalArgumentException("null value given");
		}
		
		if ( value.isEmpty() ) {
			throw new IllegalArgumentException("empty value given");
		}

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Adding user-defined attribute is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Adding user-defined attribute is forbidden for top-level accounts");
		}
		
		if ( jwsdpPeer.getKEYVALUEPAIRS() == null ) {
			ObjectFactory fact = getKMyMoneyFile().getObjectFactory();
			KEYVALUEPAIRS newKVPs = fact.createKEYVALUEPAIRS();
			jwsdpPeer.setKEYVALUEPAIRS(newKVPs);
		}
		
		HasWritableUserDefinedAttributesImpl
			.addUserDefinedAttributeCore(jwsdpPeer.getKEYVALUEPAIRS(), getWritableKMyMoneyFile(), 
			                             name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeUserDefinedAttribute(final String name) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Removing user-defined attribute is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Removing user-defined attribute is forbidden for top-level accounts");
		}
		
		if ( jwsdpPeer.getKEYVALUEPAIRS() == null ) {
			throw new KVPListDoesNotContainKeyException();
		}
		
		HasWritableUserDefinedAttributesImpl
			.removeUserDefinedAttributeCore(jwsdpPeer.getKEYVALUEPAIRS(), getWritableKMyMoneyFile(), 
			                             	name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUserDefinedAttribute(final String name, final String value) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Setting user-defined attribute is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Setting user-defined attribute is forbidden for top-level accounts");
		}
		
		if ( jwsdpPeer.getKEYVALUEPAIRS() == null ) {
			throw new KVPListDoesNotContainKeyException();
		}
		
		HasWritableUserDefinedAttributesImpl
			.setUserDefinedAttributeCore(jwsdpPeer.getKEYVALUEPAIRS(), getWritableKMyMoneyFile(), 
			                             name, value);
	}

    // -----------------------------------------------------------------

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("KMyMoneyWritableAccountImpl [");

		buffer.append("id=");
		buffer.append(getID());

		buffer.append(", type=");
		try {
			buffer.append(getType());
		} catch (UnknownAccountTypeException e) {
			buffer.append("ERROR");
		}

		buffer.append(", institution-id=");
		buffer.append(getInstitutionID());

		buffer.append(", qualif-name='");
		buffer.append(getQualifiedName() + "'");

		buffer.append(", security/currency='");
		try {
			buffer.append(getQualifSecCurrID() + "'");
		} catch (Exception e) {
			buffer.append("ERROR");
		}

		buffer.append("]");

		return buffer.toString();
	}

}
