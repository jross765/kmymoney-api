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

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.Const;
import org.kmymoney.api.generated.ACCOUNT;
import org.kmymoney.api.generated.KEYVALUEPAIRS;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.generated.SUBACCOUNT;
import org.kmymoney.api.generated.SUBACCOUNTS;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
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
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.UnknownAccountTypeException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Extension of KMyMoneyAccountImpl to allow read-write access instead of
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
	private BigFraction      myBalanceCachedRat = null;

	// Used by ${@link #getBalance()} to cache the result.
	private PropertyChangeListener myBalanceCachedInvalidator = null;

    // ---------------------------------------------------------------

	/**
	 * @param jwsdpPeer 
	 * @param file 
	 * 
	 * @see KMyMoneyAccountImpl#KMyMoneyAccountImpl(ACCOUNT, KMyMoneyFile)
	 */
	@SuppressWarnings("exports")
	public KMyMoneyWritableAccountImpl(final ACCOUNT jwsdpPeer, final KMyMoneyFileImpl file) {
		super(constr_checkPeer(jwsdpPeer), constr_checkROFile(file));
	}

	/**
	 * @param file
	 * 
	 * @see KMyMoneyAccountImpl#KMyMoneyAccountImpl(ACCOUNT, KMyMoneyFile) )
	 */
	public KMyMoneyWritableAccountImpl(final KMyMoneyWritableFileImpl file) {
		super(createAccount_int(file, file.getNewAccountID()), constr_checkRWFile(file));
	}

	public KMyMoneyWritableAccountImpl(final KMyMoneyAccountImpl acct, final boolean addSplits) {
		super(constr_getPeer(acct), constr_getKMMFile(acct));

		if (addSplits) {
			if ( ! acct.isRootAccount() ) {
				for ( KMyMoneyTransactionSplit splt : ((KMyMoneyFileImpl) acct.getKMyMoneyFile()).getTransactionSplits_readAfresh() ) {
					if ( splt.getAccountID().equals(acct.getID()) ) {
						super.addTransactionSplit(splt);
			    // NO:
//				    addTransactionSplit(new KMyMoneyTransactionSplitImpl(splt.getJwsdpPeer(), splt.getTransaction(), 
//		                                false, false));
					}
				} // for splt
		    } // if acct
		}
	}

	// ----------------------------
	// Extra check methods for constructor, as we may do nothing before
	// calling super().
	// Cf.: https://stackoverflow.com/questions/79135581/in-java-avoiding-null-dereferences-when-calling-super-in-constructor
	
	private static ACCOUNT constr_checkPeer(final ACCOUNT jwsdpPeer) {
		if ( jwsdpPeer == null ) {
			throw new IllegalArgumentException("argument <jwsdpPeer> is null");
		}
		
		return jwsdpPeer;
	}

	private static KMyMoneyFileImpl constr_checkROFile(final KMyMoneyFileImpl file) {
		if ( file == null ) {
			throw new IllegalArgumentException("argument <file> is null");
		}
		
		return file;
	}

	private static KMyMoneyWritableFileImpl constr_checkRWFile(final KMyMoneyWritableFileImpl file) {
		if ( file == null ) {
			throw new IllegalArgumentException("argument <file> is null");
		}
		
		return file;
	}

	private static ACCOUNT constr_getPeer(final KMyMoneyAccountImpl acct) {
		if ( acct == null ) {
			throw new IllegalArgumentException("argument <acct> is null");
		}
		
		return acct.getJwsdpPeer();
	}

	private static KMyMoneyFile constr_getKMMFile(final KMyMoneyAccountImpl acct) {
		if ( acct == null ) {
			throw new IllegalArgumentException("argument <acct> is null");
		}
		
		return acct.getKMyMoneyFile();
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
			throw new IllegalArgumentException("argument <mewID> is null");
		}

		if ( ! newID.isSet() ) {
			throw new IllegalArgumentException("argument <newID> is not set");
		}

		ACCOUNT jwsdpAcct = file.createAccountType();
		
		jwsdpAcct.setId(newID.toString());
		jwsdpAcct.setType(KMyMoneyAccount.Type.ASSET.getCodeBig());
		jwsdpAcct.setName("UNNAMED");
		jwsdpAcct.setDescription("no description yet");
		jwsdpAcct.setCurrency(file.getDefaultCurrencyIDStr());

		file.getRootElement().getACCOUNTS().getACCOUNT().add(jwsdpAcct);
		file.setModified(true);

		LOGGER.debug("createAccount_int: Created new account (core): " + jwsdpAcct.getId());

		return jwsdpAcct;
	}

	// ---------------------------------------------------------------

	/**
	 * Remove this account from the system.<br/>
	 * Throws IllegalStateException if this account has splits or children.
	 */
	public void remove() {
		if ( hasTransactions() ) {
			throw new IllegalStateException("Cannot remove account while it contains transaction-splits!");
		}
		
		if ( this.getChildren().size() > 0 ) {
			throw new IllegalStateException("Cannot remove account while it contains child-accounts!");
		}

		getWritableKMyMoneyFile().removeAccount(this);
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

	// ---------------------------------------------------------------

	/**
	 * 
	 */
	@Override
	public KMyMoneyWritableTransactionSplit getWritableTransactionSplitByID(final KMMQualifSpltID spltID) {
		return (KMyMoneyWritableTransactionSplit) super.getTransactionSplitByID(spltID);
	}

	/**
	 * 
	 */
	@Override
	public List<KMyMoneyWritableTransactionSplit> getWritableTransactionSplits() {
		List<KMyMoneyWritableTransactionSplit> result = new ArrayList<KMyMoneyWritableTransactionSplit>();

		for ( KMyMoneyTransactionSplit splt : super.getTransactionSplits() ) {
			KMyMoneyWritableTransactionSplitImpl newSplt = new KMyMoneyWritableTransactionSplitImpl(splt);
			result.add(newSplt);
		}

		return result;
	}

	/**
	 * @param impl the split to add to mySplits
	 */
	protected void addTransactionSplit(final KMyMoneyWritableTransactionSplitImpl impl) {
		super.addTransactionSplit(impl);
		// ((KMyMoneyFileImpl) getKMyMoneyFile()).getAccountManager().addTransactionSplit(impl, false);
	}

	/**
	 * @see KMyMoneyAccount#addTransactionSplit(KMyMoneyTransactionSplit)
	 */
	@Override
	public void addTransactionSplit(final KMyMoneyTransactionSplit splt) {
		if ( splt == null ) {
			throw new IllegalArgumentException("argument <splt> is null");
		}

		if ( ! splt.getAccountID().equals(getID()) ) {
			throw new IllegalArgumentException("split " + splt.getID() + " does not belong to account " + getID());
		}

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Adding transaction split is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Adding transaction split is forbidden for top-level accounts");
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
	 * @param splt the split to remove
	 */
	protected void removeTransactionSplit(final KMyMoneyWritableTransactionSplit splt) {
		if ( splt == null ) {
			throw new IllegalArgumentException("argument <splt> is null");
		}

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Removing transaction split is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Removing transaction split is forbidden for top-level accounts");
		}

		List<KMyMoneyTransactionSplit> transactionSplits = getTransactionSplits();
		// That does not work with writable splits:
		// transactionSplits.remove(splt);
		// Instead:
		for ( int i = 0; i < transactionSplits.size(); i++ ) {
			if ( transactionSplits.get(i).getID().equals(splt.getID()) ) {
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
	@Override
	public void setName(final String name) {
		if ( name == null ) {
			throw new IllegalArgumentException("argument <name> is null");
		}

		if ( name.isBlank() ) {
			throw new IllegalArgumentException("argument <name> is blank");
		}

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Setting name is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Setting name is forbidden for top-level accounts");
		}
		
		String oldName = getName();
		if ( oldName == name ) {
			return; // nothing has changed
		}

		this.getJwsdpPeer().setName(name);
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
			throw new IllegalArgumentException("argument <instID> is null");
		}

		if ( ! instID.isSet() ) {
			throw new IllegalArgumentException("argument <instID> is not set");
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

		this.getJwsdpPeer().setInstitution(instID.toString());
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
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set");
		}

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Setting security/currency ID is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Setting security/currency ID is forbidden for top-level accounts");
		}
		
		KMMQualifSecCurrID oldCurrId = getQualifSecCurrID();
		if ( oldCurrId == secCurrID ) {
			return; // nothing has changed
		}

		this.getJwsdpPeer().setCurrency(secCurrID.getCode());
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
			throw new IllegalArgumentException("argument <secID> is null");
		}
		
		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		setQualifSecCurrID(new KMMQualifSecID(secID));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrency(final Currency curr) {
		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}
		
		setQualifSecCurrID(new KMMQualifCurrID(curr));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrency(final String currCode) {
		if ( currCode == null ) {
			throw new IllegalArgumentException("argument <currCode> is null");
		}

		if ( currCode.isBlank() ) {
			throw new IllegalArgumentException("argument <currCode> is blank");
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
	public void setMemo(final String descr) {
		if ( descr == null ) {
			throw new IllegalArgumentException("argument <descr> is null");
		}

		// Caution: empty string allowed here
		// if ( descr.isBlank() ) {
		//   throw new IllegalArgumentException("argument <descr> is null");
		// }

		if ( getKMyMoneyFile().getTopAccountIDs().contains(getID()) ) {
			LOGGER.error("Setting memo for top-level account is forbidden for top-level accounts");
			throw new UnsupportedOperationException("Setting memo is forbidden for top-level accounts");
		}
		
		String oldDescr = getMemo();
		if ( oldDescr == descr ) {
			return; // nothing has changed
		}
		
		getJwsdpPeer().setDescription(descr);
		setIsModified();
		
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("memo", oldDescr, descr);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setType(final KMyMoneyAccount.Type type) {
		if ( type == null ) {
			throw new IllegalArgumentException("argument <type> is null");
		}

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
			throw new IllegalArgumentException("argument <typeInt> is null");
		}

		if ( typeInt.intValue() <= 0 ) {
			throw new IllegalArgumentException("argument <typeInt> is null <= 0");
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
    	
		getJwsdpPeer().setType(typeInt);
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
	@Override
	public void setParentAccountID(final KMMComplAcctID prntAcctID) {
		if ( prntAcctID == null ) {
			setParentAccount(null);
			return;
		}

		if ( ! prntAcctID.isSet() ) {
			throw new IllegalArgumentException("argument <prntAcctID> is not set");
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
	@Override
	public void setParentAccount(final KMyMoneyAccount prntAcct) {
		if ( prntAcct == null ) {
			this.getJwsdpPeer().setParentaccount(null);
			return;
		}

		if ( prntAcct == this ) {
			throw new IllegalArgumentException("An account cannot be set its own parent");
		}

		setParentAccountID(prntAcct.getID());
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
								if ( ! ( split instanceof KMyMoneyWritableTransactionSplit ) || 
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
	@Override
	public BigFraction getBalanceRat() {
		if ( myBalanceCachedRat != null ) {
			return myBalanceCachedRat;
		}

		List<KMyMoneyTransactionSplit> after = new ArrayList<KMyMoneyTransactionSplit>();
		BigFraction balance = getBalanceRat(LocalDate.now(), after);

		if ( after.isEmpty() ) {
			myBalanceCachedRat = balance;

			// add a listener to keep the cache up to date
			if ( myBalanceCachedInvalidator != null ) {
				myBalanceCachedInvalidator = new PropertyChangeListener() {
					private final Collection<KMyMoneyTransactionSplit> splitsWeAreAddedTo = new HashSet<KMyMoneyTransactionSplit>();

					public void propertyChange(final PropertyChangeEvent evt) {
						myBalanceCachedRat = null;

						// we don't handle the case of removing an account
						// because that happens seldomly enough

						if ( evt.getPropertyName().equals("account") && 
							 evt.getSource() instanceof KMyMoneyWritableTransactionSplit ) {
							KMyMoneyWritableTransactionSplit splitw = (KMyMoneyWritableTransactionSplit) evt.getSource();
							if ( splitw.getAccount() != KMyMoneyWritableAccountImpl.this ) {
								helper.removePropertyChangeListener("account", this);
								helper.removePropertyChangeListener("quantity", this);
								helper.removePropertyChangeListener("datePosted", this);
								splitsWeAreAddedTo.remove(splitw);
							}

						}

						if ( evt.getPropertyName().equals("transactionSplits") ) {
							List<KMyMoneyTransactionSplit> splits = (List<KMyMoneyTransactionSplit>) evt.getNewValue();
							for ( KMyMoneyTransactionSplit splt : splits ) {
								if ( ! ( splt instanceof KMyMoneyWritableTransactionSplit ) ||
									 splitsWeAreAddedTo.contains(splt) ) {
									continue;
								}
								KMyMoneyWritableTransactionSplit splitw = (KMyMoneyWritableTransactionSplit) splt;
								helper.addPropertyChangeListener("account", this);
								helper.addPropertyChangeListener("quantity", this);
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
	
	// ----------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getBalanceChange(final LocalDate from, final LocalDate to) {
		FixedPointNumber retval = new FixedPointNumber();

		for ( KMyMoneyTransactionSplit splt : getTransactionSplits() ) {
			LocalDate whenHappened = splt.getTransaction().getDatePosted();
			
			if ( ! whenHappened.isBefore(to) ) {
				continue;
			}
			
			if ( whenHappened.isBefore(from) ) {
				continue;
			}
			
			retval.add(splt.getShares()); // mutable
		}
		
		return retval;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BigFraction getBalanceChangeRat(final LocalDate from, final LocalDate to) {
		BigFraction retval = BigFraction.ZERO;

		for ( KMyMoneyTransactionSplit splt : getTransactionSplits() ) {
			LocalDate whenHappened = splt.getTransaction().getDatePosted();

			if ( ! whenHappened.isBefore( to ) ) {
				continue;
			}

			if ( whenHappened.isBefore( from ) ) {
				continue;
			}

			retval = retval.add(splt.getSharesRat()); // immutable
		}

		return retval;
	}

	// -------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addUserDefinedAttribute(final String name, final String value) {
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
			throw new IllegalArgumentException("argument <name> is null");
		}

		if ( name.isBlank() ) {
			throw new IllegalArgumentException("argument <name> is blank");
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
			throw new IllegalArgumentException("argument <name> is null");
		}

		if ( name.isBlank() ) {
			throw new IllegalArgumentException("argument <name> is blank");
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

	// ---------------------------------------------------------------
	
	@Override
	public void setClosed()
	{
		if ( isClosed() )
			return;
		
		if ( getUserDefinedAttribute(Const.KVP_KEY_ACCT_CLOSED) == null ) {
			addUserDefinedAttribute(Const.KVP_KEY_ACCT_CLOSED, Const.KVP_VAL_ACCT_CLOSED);
		} else {
			setUserDefinedAttribute(Const.KVP_KEY_ACCT_CLOSED, Const.KVP_VAL_ACCT_CLOSED);
		}
	}

	@Override
	public void unsetClosed()
	{
		if ( ! isClosed() )
			return;
		
		removeUserDefinedAttribute(Const.KVP_KEY_ACCT_CLOSED);
	}

	// ---------------------------------------------------------------

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
