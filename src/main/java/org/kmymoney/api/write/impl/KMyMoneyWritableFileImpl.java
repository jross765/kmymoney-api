package org.kmymoney.api.write.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.kmymoney.api.generated.ACCOUNT;
import org.kmymoney.api.generated.CURRENCY;
import org.kmymoney.api.generated.INSTITUTION;
import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.generated.PAYEE;
import org.kmymoney.api.generated.PRICE;
import org.kmymoney.api.generated.PRICEPAIR;
import org.kmymoney.api.generated.SECURITY;
import org.kmymoney.api.generated.SPLIT;
import org.kmymoney.api.generated.SPLITS;
import org.kmymoney.api.generated.TAG;
import org.kmymoney.api.generated.TRANSACTION;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyAccount.Type;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyInstitution;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyAccountImpl;
import org.kmymoney.api.read.impl.KMyMoneyCurrencyImpl;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneyInstitutionImpl;
import org.kmymoney.api.read.impl.KMyMoneyPayeeImpl;
import org.kmymoney.api.read.impl.KMyMoneyPriceImpl;
import org.kmymoney.api.read.impl.KMyMoneyPricePairImpl;
import org.kmymoney.api.read.impl.KMyMoneySecurityImpl;
import org.kmymoney.api.read.impl.KMyMoneyTagImpl;
import org.kmymoney.api.read.impl.KMyMoneyTransactionImpl;
import org.kmymoney.api.read.impl.KMyMoneyTransactionSplitImpl;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.api.write.KMyMoneyWritableCurrency;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritableInstitution;
import org.kmymoney.api.write.KMyMoneyWritablePayee;
import org.kmymoney.api.write.KMyMoneyWritablePrice;
import org.kmymoney.api.write.KMyMoneyWritablePricePair;
import org.kmymoney.api.write.KMyMoneyWritableSecurity;
import org.kmymoney.api.write.KMyMoneyWritableTag;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.api.write.ObjectCascadeException;
import org.kmymoney.api.write.hlp.IDManager;
import org.kmymoney.api.write.impl.hlp.WritingContentHandler;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Implementation of KMyMoneyWritableFile based on KMyMoneyFileImpl.
 * @see KMyMoneyFileImpl
 */
public class KMyMoneyWritableFileImpl extends KMyMoneyFileImpl 
                                      implements KMyMoneyWritableFile,
                                                 IDManager
{
	// ::MAGIC
	private static final int HEX = 16;
	private static final String CODEPAGE = "UTF-8";

	// ---------------------------------------------------------------

	// true if this file has been modified.
	private boolean modified = false;

	private long lastWriteTime = 0;

	// ---------------------------------------------------------------

	/**
	 * @param file the file to load
	 * @throws IOException                   on bsic io-problems such as a
	 *                                       FileNotFoundException
	 */
	public KMyMoneyWritableFileImpl(final File file)
			throws IOException {
		super(file);
		setModified(false);

    	// CAUTION: The order matters
		acctMgr = new org.kmymoney.api.write.impl.hlp.FileAccountManager(this);
		instMgr = new org.kmymoney.api.write.impl.hlp.FileInstitutionManager(this);
		pyeMgr  = new org.kmymoney.api.write.impl.hlp.FilePayeeManager(this);
		tagMgr  = new org.kmymoney.api.write.impl.hlp.FileTagManager(this);
		trxMgr  = new org.kmymoney.api.write.impl.hlp.FileTransactionManager(this);
		secMgr  = new org.kmymoney.api.write.impl.hlp.FileSecurityManager(this);
		currMgr = new org.kmymoney.api.write.impl.hlp.FileCurrencyManager(this);
		prcMgr  = new org.kmymoney.api.write.impl.hlp.FilePriceManager(this);
	}

	public KMyMoneyWritableFileImpl(final InputStream is)
			throws IOException {
		super(is);

    	// CAUTION: The order matters
		acctMgr = new org.kmymoney.api.write.impl.hlp.FileAccountManager(this);
		instMgr = new org.kmymoney.api.write.impl.hlp.FileInstitutionManager(this);
		pyeMgr  = new org.kmymoney.api.write.impl.hlp.FilePayeeManager(this);
		tagMgr  = new org.kmymoney.api.write.impl.hlp.FileTagManager(this);
		trxMgr  = new org.kmymoney.api.write.impl.hlp.FileTransactionManager(this);
		secMgr  = new org.kmymoney.api.write.impl.hlp.FileSecurityManager(this);
		currMgr = new org.kmymoney.api.write.impl.hlp.FileCurrencyManager(this);
		prcMgr  = new org.kmymoney.api.write.impl.hlp.FilePriceManager(this);
	}

	// ---------------------------------------------------------------
	// ::TODO Description
	// ---------------------------------------------------------------
	
	@Override
	public KMyMoneyWritableFile getWritableKMyMoneyFile() {
		return this;
	}
	
	// ---------------------------------------------------------------
	// ::TODO Description
	// ---------------------------------------------------------------

	/**
	 * @param pModified true if this file has been modified false after save, load
	 *                  or undo of changes
	 */
	@Override
	public void setModified(final boolean pModified) {
		// boolean old = this.modified;
		modified = pModified;
		// if (propertyChange != null)
		// propertyChange.firePropertyChange("modified", old, pModified);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isModified() {
		return modified;
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadFile(final File pFile) throws IOException {
		super.loadFile(pFile);
		lastWriteTime = Math.max(pFile.lastModified(), System.currentTimeMillis());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeFile(final File file) throws IOException {
		writeFile(file, CompressMode.GUESS_FROM_FILENAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeFile(File file, CompressMode compMode) throws IOException {
		if ( file == null ) {
			throw new IllegalArgumentException("argument <file> is null");
		}

		if ( file.exists() ) {
			throw new IllegalArgumentException("File '" + file.getAbsolutePath() + "' already exists");
		}

		updateLastModified();

		setFile(file);

		OutputStream out = new FileOutputStream(file);
		out = new BufferedOutputStream(out);
		if ( compMode == CompressMode.COMPRESS ) {
			out = new GZIPOutputStream(out);
		} else if ( compMode == CompressMode.GUESS_FROM_FILENAME ) {
			if ( file.getName().endsWith(FILE_EXT_ZIPPED_1) ||
				 file.getName().endsWith(FILE_EXT_ZIPPED_2) ) {
				out = new GZIPOutputStream(out);
			}
		}

		Writer writer = new OutputStreamWriter(out, CODEPAGE);
		try {
			JAXBContext context = getJAXBContext();
			Marshaller marsh = context.createMarshaller();

			// marsh.marshal(getRootElement(), writer);
			// marsh.marshal(getRootElement(), new PrintWriter( System.out ) );
			marsh.marshal(getRootElement(), new WritingContentHandler(writer));

			setModified(false);
		} catch (JAXBException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			writer.close();
		}

		out.close();

		lastWriteTime = Math.max(file.lastModified(), System.currentTimeMillis());
	}

	/**
	 * @return the time in ms (compatible with File.lastModified) of the last
	 *         write-operation
	 */
	@Override
	public long getLastWriteTime() {
		return lastWriteTime;
	}

	// ---------------------------------------------------------------

	/**
	 * Update the 'last updated' info in the KMM document.
	 */
	private void updateLastModified() {
		try {
            // https://stackoverflow.com/questions/835889/java-util-date-to-xmlgregoriancalendar
			// https://stackoverflow.com/questions/49667772/localdate-to-gregoriancalendar-conversion
			// https://stackoverflow.com/questions/14060161/specify-the-date-format-in-xmlgregoriancalendar
			LocalDate today = LocalDate.now();
			// CAUTION: The following two lines with new Date(...) do not work (reliably)
//	        GregorianCalendar cal = new GregorianCalendar();
//	        cal.setTime(new Date(this.date.getYear(),
//	        		             this.date.getMonthValue(),
//	        		             this.date.getDayOfMonth()));
			GregorianCalendar cal = GregorianCalendar 
					.from( today.atStartOfDay(ZoneId.systemDefault()) );
	        XMLGregorianCalendar xmlCal = 
	        		DatatypeFactory.newInstance().newXMLGregorianCalendarDate(
	        				cal.get(Calendar.YEAR), 
	        				cal.get(Calendar.MONTH) + 1, 
	        				cal.get(Calendar.DAY_OF_MONTH), 
	        				DatatypeConstants.FIELD_UNDEFINED);
	        getRootElement().getFILEINFO().getLASTMODIFIEDDATE().setDate(xmlCal);
		} catch ( DatatypeConfigurationException exc ) {
			throw new DateMappingException();
		}
	}

	// ---------------------------------------------------------------

	/**
	 * Used by KMyMoneyTransactionImpl.createTransaction to add a new Transaction to
	 * this file.
	 *
	 * @see KMyMoneyTransactionImpl#createSplit(GncTransaction.TrnSplits.TrnSplit)
	 */
	protected void addTransaction(final KMyMoneyTransactionImpl trx) {
		getRootElement().getTRANSACTIONS().getTRANSACTION().add(trx.getJwsdpPeer());
		setModified(true);
		((org.kmymoney.api.write.impl.hlp.FileTransactionManager) super.trxMgr)
			.addTransaction(trx);
	}

	/**
	 * @see KMyMoneyFileImpl#setRootElement(GncV2)
	 */
	@SuppressWarnings("exports")
	@Override
	public void setRootElement(final KMYMONEYFILE rootElement) {
		super.setRootElement(rootElement);
	}

	/**
	 * @return the underlying JAXB-element
	 * @see KMyMoneyWritableFile#getRootElement()
	 */
	@SuppressWarnings("exports")
	@Override
	public KMYMONEYFILE getRootElement() {
		return super.getRootElement();
	}
	
	// ---------------------------------------------------------------

	protected INSTITUTION createInstitutionType() {
		INSTITUTION retval = getObjectFactory().createINSTITUTION();
		return retval;
	}
	
	protected ACCOUNT createAccountType() {
		ACCOUNT retval = getObjectFactory().createACCOUNT();
		return retval;
	}

	protected TRANSACTION createTransactionType() {
		TRANSACTION retval = getObjectFactory().createTRANSACTION();
		return retval;
	}

	protected SPLITS createSplitsType() {
		SPLITS retval = getObjectFactory().createSPLITS();
		return retval;
	}

	protected SPLIT createSplitType() {
		SPLIT retval = getObjectFactory().createSPLIT();
		return retval;
	}

	protected PAYEE createPayeeType() {
		PAYEE retval = getObjectFactory().createPAYEE();
		return retval;
	}
	
	protected TAG createTagType() {
		TAG retval = getObjectFactory().createTAG();
		return retval;
	}
	
	protected SECURITY createSecurityType() {
		SECURITY retval = getObjectFactory().createSECURITY();
		return retval;
	}
	
	protected CURRENCY createCurrencyType() {
		CURRENCY retval = getObjectFactory().createCURRENCY();
		return retval;
	}
	
	protected PRICEPAIR createPricePairType() {
		PRICEPAIR retval = getObjectFactory().createPRICEPAIR();
		return retval;
	}
	
	protected PRICE createPriceType() {
		PRICE retval = getObjectFactory().createPRICE();
		return retval;
	}
	
	// ---------------------------------------------------------------

	/**
	 * This overridden method creates the writable version of the returned object.
	 *
	 * @see KMyMoneyFileImpl#createAccount(GncAccount)
	 */
	protected KMyMoneyAccountImpl createAccount(final ACCOUNT jwsdpAcct) {
		KMyMoneyAccountImpl account = new KMyMoneyWritableAccountImpl(jwsdpAcct, this);
		return account;
	}

	/**
	 * This overridden method creates the writable version of the returned object.
	 *
	 * @see KMyMoneyFileImpl#createTransaction(GncTransaction)
	 */
	protected KMyMoneyTransactionImpl createTransaction(final TRANSACTION jwsdpTrx) {
		KMyMoneyTransactionImpl account = new KMyMoneyWritableTransactionImpl(jwsdpTrx, this);
		return account;
	}
	
	// ---------------------------------------------------------------

	@Override
	public KMyMoneyWritableInstitution getWritableInstitutionByID(KMMInstID instID) {
		if ( instID == null ) {
			throw new IllegalArgumentException("argument <instID> is null");
		}

		if ( ! instID.isSet() ) {
			throw new IllegalArgumentException("argument <instID> is not set");
		}

		KMyMoneyInstitution inst = super.getInstitutionByID(instID);
		return new KMyMoneyWritableInstitutionImpl((KMyMoneyInstitutionImpl) inst);
	}

	/**
	 * This overridden method creates the writable version of the returned object.
	 *
	 * @return the new institution
	 * @see KMyMoneyFileImpl#createCustomer(GncV2.GncBook.GncGncInstitution)
	 */
	@Override
	public KMyMoneyWritableInstitution createWritableInstitution(final String name) {
		KMyMoneyWritableInstitutionImpl inst = new KMyMoneyWritableInstitutionImpl(this);
		inst.setName(name);
		((org.kmymoney.api.write.impl.hlp.FileInstitutionManager) super.instMgr)
			.addInstitution(inst);
		
		return inst;
	}

	@Override
	public void removeInstitution(KMyMoneyWritableInstitution inst) {
		((org.kmymoney.api.write.impl.hlp.FileInstitutionManager) super.instMgr)
			.removeInstitution(inst);
		getRootElement().getINSTITUTIONS().getINSTITUTION().remove(((KMyMoneyWritableInstitutionImpl) inst).getJwsdpPeer());
		setModified(true);
	}
	
	// ---------------------------------------------------------------

	/**
	 * @see KMyMoneyWritableFile#getWritableTransactions()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<? extends KMyMoneyWritableTransaction> getWritableTransactions() {
		return (Collection<? extends KMyMoneyWritableTransaction>) getTransactions();
	}

	/**
	 * @param impl what to remove
	 */
	@Override
	public void removeTransaction(final KMyMoneyWritableTransaction trx) {
		Collection<KMyMoneyWritableTransactionSplit> spltList = new LinkedList<KMyMoneyWritableTransactionSplit>();
		spltList.addAll(trx.getWritableSplits());
		for ( KMyMoneyWritableTransactionSplit splt : spltList ) {
			splt.remove();
		}

		getRootElement().getTRANSACTIONS().getTRANSACTION().remove(((KMyMoneyWritableTransactionImpl) trx).getJwsdpPeer());
		setModified(true);		
		((org.kmymoney.api.write.impl.hlp.FileTransactionManager) super.trxMgr)
			.removeTransaction(trx);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KMyMoneyWritableTransaction createWritableTransaction() {
		KMyMoneyWritableTransactionImpl trx = new KMyMoneyWritableTransactionImpl(this);
		((org.kmymoney.api.write.impl.hlp.FileTransactionManager) super.trxMgr)
			.addTransaction(trx);
		
		return trx;
	}

	// ----------------------------

	/**
	 * @param pye what to remove
	 */
	@Override
	public void removePayee(final KMyMoneyWritablePayee pye) {
		if ( pye.hasTransactions() ) {
			throw new IllegalStateException("cannot remove payee while it contains transaction-splits!");
		}

		// 1) Remove avatar in payee manager
		((org.kmymoney.api.write.impl.hlp.FilePayeeManager) super.pyeMgr)
			.removePayee(pye);
		
		// 2) Remove payee
		getRootElement().getPAYEES().getPAYEE().remove(((KMyMoneyWritablePayeeImpl) pye).getJwsdpPeer());
		setModified(true);
	}

	// ----------------------------

	/**
	 * @param pye what to remove
	 */
	@Override
	public void removeTag(final KMyMoneyWritableTag tag) {
		if ( tag.hasTransactions() ) {
			throw new IllegalStateException("cannot remove tag while it contains transaction-splits!");
		}

		// 1) Remove avatar in tag manager
		((org.kmymoney.api.write.impl.hlp.FileTagManager) super.tagMgr)
			.removeTag(tag);
		
		// 2) Remove payee
		getRootElement().getTAGS().getTAG().remove(((KMyMoneyWritableTagImpl) tag).getJwsdpPeer());
		setModified(true);
	}

	// ----------------------------

	/**
	 * @see KMyMoneyWritableFile#createWritableAccount(Type, KMMQualifSecCurrID, KMMComplAcctID, String)
	 */
	@Override
	@Deprecated
	public KMyMoneyWritableAccount createWritableAccount() {
		KMyMoneyWritableAccountImpl acct = new KMyMoneyWritableAccountImpl(this);
		((org.kmymoney.api.write.impl.hlp.FileAccountManager) super.acctMgr)
			.addAccount(acct);
		
		return acct;
	}

	/**
	 * @see KMyMoneyWritableFile#createWritableAccount(Type, KMMQualifSecCurrID, KMMComplAcctID, String)
	 */
	@Override
	public KMyMoneyWritableAccount createWritableAccount(KMyMoneyAccount.Type type, 
			  											 KMMQualifSecCurrID secCurrID,
			  											 KMMComplAcctID parentID,
			  											 String name) {
		KMyMoneyWritableAccountImpl acct = new KMyMoneyWritableAccountImpl(this);
		((org.kmymoney.api.write.impl.hlp.FileAccountManager) super.acctMgr)
			.addAccount(acct);
		
		acct.setType(type);
		acct.setQualifSecCurrID(secCurrID);
		acct.setParentAccountID(parentID);
		acct.setName(name);
		
		return acct;
	}

	/**
	 * @see KMyMoneyWritableFile#createWritableAccount(Type, KMMQualifSecCurrID, KMMComplAcctID, String)
	 */
	@Override
	public KMyMoneyWritableAccount createWritableAccount(KMyMoneyAccount.Type type, 
			  											 KMMQualifSecID secID,
			  											 KMMComplAcctID parentID,
			  											 String name) {
		return createWritableAccount(type, (KMMQualifSecCurrID) secID, parentID, name);
	}

	/**
	 * @see KMyMoneyWritableFile#createWritableAccount(Type, KMMQualifCurrID, KMMComplAcctID, String)
	 */
	@Override
	public KMyMoneyWritableAccount createWritableAccount(KMyMoneyAccount.Type type, 
			  											 KMMQualifCurrID currID,
			  											 KMMComplAcctID parentID,
			  											 String name) {
		return createWritableAccount(type, (KMMQualifSecCurrID) currID, parentID, name);
	}

	/**
	 * @see KMyMoneyWritableFile#createWritableAccount(Type, KMMSecID, KMMComplAcctID, String)
	 */
	@Override
	public KMyMoneyWritableAccount createWritableAccount(KMyMoneyAccount.Type type, 
			  											 KMMSecID secID,
			  											 KMMComplAcctID parentID,
			  											 String name) {
		return createWritableAccount(type, new KMMQualifSecID(secID), parentID, name);
	}

	/**
	 * @see KMyMoneyWritableFile#createWritableAccount(Type, Currency, KMMComplAcctID, String)
	 */
	@Override
	public KMyMoneyWritableAccount createWritableAccount(KMyMoneyAccount.Type type, 
			  											 Currency curr,
			  											 KMMComplAcctID parentID,
			  											 String name) {
		return createWritableAccount(type, new KMMQualifCurrID(curr), parentID, name);
	}

	/**
	 * @param acct what to remove
	 */
	public void removeAccount(final KMyMoneyWritableAccount acct) {
		if ( acct.hasTransactions() ) {
			throw new IllegalStateException("cannot remove account while it contains transaction-splits!");
		}

		// 1) Remove avatar in account manager
		((org.kmymoney.api.write.impl.hlp.FileAccountManager) super.acctMgr)
			.removeAccount(acct);
		
		// 2) remove account
		getRootElement().getACCOUNTS().getACCOUNT().remove(((KMyMoneyWritableAccountImpl) acct).getJwsdpPeer());
		setModified(true);
	}

	/**
	 * @return a read-only collection of all accounts that have no parent
	 */
	@SuppressWarnings("unchecked")
	public Collection<? extends KMyMoneyWritableAccount> getWritableParentlessAccounts() {
		return (Collection<? extends KMyMoneyWritableAccount>) getParentlessAccounts();
	}

	/**
	 * @return a read-only collection of all accounts
	 */
	@Override
	public Collection<KMyMoneyWritableAccount> getWritableAccounts() {
		TreeSet<KMyMoneyWritableAccount> retval = new TreeSet<KMyMoneyWritableAccount>();
		for ( KMyMoneyAccount account : getAccounts() ) {
			retval.add((KMyMoneyWritableAccount) account);
		}
		
		return retval;
	}

	@Override
	public Collection<? extends KMyMoneyWritableAccount> getWritableRootAccounts() {
		List<KMyMoneyWritableAccount> result = new ArrayList<KMyMoneyWritableAccount>();
		
		KMyMoneyWritableAccount acct1 = getWritableAccountByID(KMMComplAcctID.get(KMMComplAcctID.Top.ASSET));
		result.add(acct1);
		
		KMyMoneyWritableAccount acct2 = getWritableAccountByID(KMMComplAcctID.get(KMMComplAcctID.Top.LIABILITY));
		result.add(acct2);
		
		KMyMoneyWritableAccount acct3 = getWritableAccountByID(KMMComplAcctID.get(KMMComplAcctID.Top.INCOME));
		result.add(acct3);
		
		KMyMoneyWritableAccount acct4 = getWritableAccountByID(KMMComplAcctID.get(KMMComplAcctID.Top.EXPENSE));
		result.add(acct4);
		
		KMyMoneyWritableAccount acct5 = getWritableAccountByID(KMMComplAcctID.get(KMMComplAcctID.Top.EQUITY));
		result.add(acct5);
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.kmm.write.jwsdpimpl.KMyMoneyFileImpl#getRootAccounts()
	 */
	@Override
	public List<? extends KMyMoneyAccount> getParentlessAccounts() {
		// TODO Auto-generated method stub
		List<? extends KMyMoneyAccount> rootAccounts = super.getParentlessAccounts();
		if ( rootAccounts.size() > 1 ) {
			KMyMoneyAccount root = null;
			StringBuilder roots = new StringBuilder();
			for ( KMyMoneyAccount kmmAccount : rootAccounts ) {
				if ( kmmAccount == null ) {
					continue;
				}
				if ( kmmAccount.getParentAccountID() == null ) {
					root = kmmAccount;
					continue;
				}
				roots.append(kmmAccount.getID()).append("=\"").append(kmmAccount.getName()).append("\" ");
			}
			LOGGER.warn("File has more then one root-account! Attaching excess accounts to root-account: " + roots.toString());
			LinkedList<KMyMoneyAccount> rootAccounts2 = new LinkedList<KMyMoneyAccount>();
			rootAccounts2.add(root);
			for ( KMyMoneyAccount kmmAcct : rootAccounts ) {
				if ( kmmAcct == null ) {
					continue;
				}
				if ( kmmAcct == root ) {
					continue;
				}
				((KMyMoneyWritableAccount) kmmAcct).setParentAccount(root);

			}
			rootAccounts = rootAccounts2;
		}
		
		return rootAccounts;
	}

	// ---------------------------------------------------------------

	@Override
	public Collection<KMyMoneyWritableAccount> getWritableAccountsByName(String name) {
		Collection<KMyMoneyWritableAccount> result = new ArrayList<KMyMoneyWritableAccount>();
		
		for ( KMyMoneyAccount acct : getAccountsByName(name) ) {
			KMyMoneyWritableAccountImpl newAcct = new KMyMoneyWritableAccountImpl((KMyMoneyAccountImpl) acct, true);
			result.add(newAcct);
		}
		
		return result;
	}

	@Override
	public Collection<KMyMoneyWritableAccount> getWritableAccountsByType(KMyMoneyAccount.Type type) {
		Collection<KMyMoneyWritableAccount> result = new ArrayList<KMyMoneyWritableAccount>();
		
		for ( KMyMoneyAccount acct : getAccountsByType(type) ) {
			KMyMoneyWritableAccountImpl newAcct = new KMyMoneyWritableAccountImpl((KMyMoneyAccountImpl) acct, true);
			result.add(newAcct);
		}
		
		return result;
	}

	@Override
	public Collection<KMyMoneyWritableAccount> getWritableAccountsByTypeAndName(Type type, String expr, 
																				boolean qualif, boolean relaxed) {
		Collection<KMyMoneyWritableAccount> result = new ArrayList<KMyMoneyWritableAccount>();
		
		for ( KMyMoneyAccount acct : getAccountsByTypeAndName(type, expr, qualif, relaxed) ) {
			KMyMoneyWritableAccountImpl newAcct = new KMyMoneyWritableAccountImpl((KMyMoneyAccountImpl) acct, true);
			result.add(newAcct);
		}
		
		return result;
	}

	@Override
	public KMyMoneyWritableAccount getWritableAccountByID(final KMMComplAcctID acctID) {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}

		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

		try {
			KMyMoneyAccount acct = super.getAccountByID(acctID);
			return new KMyMoneyWritableAccountImpl((KMyMoneyAccountImpl) acct, true);
		} catch (Exception exc) {
			LOGGER.error(
					"getWritableAccountByID: Could not instantiate writable account object from read-only account object (ID: "
							+ acctID + ")");
			throw new RuntimeException(
					"Could not instantiate writable account object from read-only account object (ID: " + acctID + ")");
		}
	}

	@Override
	public KMyMoneyWritableAccount getWritableAccountByID(final KMMAcctID acctID) {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}

		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

		try {
			KMyMoneyAccount acct = super.getAccountByID(acctID);
			return new KMyMoneyWritableAccountImpl((KMyMoneyAccountImpl) acct, true);
		} catch (Exception exc) {
			LOGGER.error(
					"getWritableAccountByID: Could not instantiate writable account object from read-only account object (ID: "
							+ acctID + ")");
			throw new RuntimeException(
					"Could not instantiate writable account object from read-only account object (ID: " + acctID + ")");
		}
	}

	@Override
	public KMyMoneyWritableTransaction getWritableTransactionByID(KMMTrxID trxID) {
		if ( trxID == null ) {
			throw new IllegalArgumentException("argument <trxID> is null");
		}

		if ( ! trxID.isSet() ) {
			throw new IllegalArgumentException("argument <trxID> is not set");
		}

		try {
			return new KMyMoneyWritableTransactionImpl((KMyMoneyWritableTransactionImpl) super.getTransactionByID(trxID));
		} catch (Exception exc) {
			LOGGER.error("getWritableTransactionByID: Could not instantiate writable transaction object from read-only transaction object (ID: " + trxID + ")");
			throw new RuntimeException("Could not instantiate writable transaction object from read-only transaction object (ID: " + trxID + ")");
		}
	}

	@Override
	public KMyMoneyWritableTransactionSplit getWritableTransactionSplitByID(KMMQualifSpltID spltID) {
		if ( spltID == null ) {
			throw new IllegalArgumentException("argument <spltID> is null");
		}

		if ( !spltID.isSet() ) {
			throw new IllegalArgumentException("argument <spltID> is not set");
		}

		KMyMoneyTransactionSplit splt = super.getTransactionSplitByID(spltID);
		// ::TODO
		// !!! Diese nicht-triviale Änderung nochmal ganz genau abtesten !!!
		return new KMyMoneyWritableTransactionSplitImpl((KMyMoneyTransactionSplitImpl) splt, 
														false, false, false);
	}
	
	// By purpose, this method has not been defined in the interface
	// ::TODO: Symetry w/ sister project
	// @Override
	public void removeTransactionSplit(final KMyMoneyWritableTransactionSplit splt) {
		// 1) remove avatar in transaction manager
		((org.kmymoney.api.write.impl.hlp.FileTransactionManager) super.trxMgr)
			.removeTransactionSplit(splt);
		
		// 2) remove transaction split
//		KMMTrxID trxID = splt.getTransactionID();
//		String trxIDStr = null;
//		try {
//			trxIDStr = trxID.get();
//		} catch (KMMIDNotSetException e) {
//			throw new IllegalStateException("Transaction-split " + splt + " does not seem to have a correct transaction (ID)");
//		}
		
//		for ( TRANSACTION jwsdpTrx : getRootElement().getTRANSACTIONS().getTRANSACTION() ) {
//			if ( jwsdpTrx.getId().equals(trxIDStr) ) {
//				// CAUTION concurrency ::CHECK
//				jwsdpTrx.getSPLITS().getSPLIT().remove(((KMyMoneyWritableTransactionSplitImpl) splt).getJwsdpPeer());
//				break;
//			}
//		}
		
		((org.kmymoney.api.write.impl.hlp.FileTransactionManager) trxMgr)
			.removeTransactionSplit_raw(splt.getTransactionID(), splt.getID());
		
		// 3) remove transaction, if no splits left
		// ::TODO / ::CHECK
		// uncomment?
		// cf. according code in removePrice()
//		for ( TRANSACTION jwsdpTrx : getRootElement().getTRANSACTIONS().getTRANSACTION() ) {
//			if ( jwsdpTrx.getId().equals(trx.getID().get()) ) {
//				if ( jwsdpTrx.getSPLITS().size() == 0 ) {
//					// CAUTION concurrency ::CHECK
//					getRootElement().getTRANSACTIONS().getTRANSACTION().remove(jwsdpTrx);
//					break;
//				}
//			}
//		}
		
		// 4) set 'modified' flag
		setModified(true);
	}

	// ---------------------------------------------------------------

	@Override
	public KMyMoneyWritablePayee getWritablePayeeByID(KMMPyeID pyeID) {
		if ( pyeID == null ) {
			throw new IllegalArgumentException("argument <pyeID> is null");
		}

		if ( ! pyeID.isSet() ) {
			throw new IllegalArgumentException("argument <pyeID> is not set");
		}

		try {
			KMyMoneyPayee pye = super.getPayeeByID(pyeID);
			return new KMyMoneyWritablePayeeImpl((KMyMoneyPayeeImpl) pye, true);
		} catch (Exception exc) {
			LOGGER.error(
					"getWritablePayeeByID: Could not instantiate writable payee object from read-only account object (ID: "
							+ pyeID + ")");
			throw new RuntimeException(
					"Could not instantiate writable payee object from read-only account object (ID: " + pyeID + ")");
		}
	}

	/**
	 * This overridden method creates the writable version of the returned object.
	 *
	 * @return the new payee
	 * @see KMyMoneyFileImpl#createPayee()
	 */
	@Override
	public KMyMoneyWritablePayee createWritablePayee(final String name) {
		KMyMoneyWritablePayeeImpl pye = new KMyMoneyWritablePayeeImpl(this);
		pye.setName(name);
		((org.kmymoney.api.write.impl.hlp.FilePayeeManager) super.pyeMgr)
			.addPayee(pye);
		
		return pye;
	}

	// ---------------------------------------------------------------

	@Override
	public KMyMoneyWritableTag getWritableTagByID(KMMTagID tagID) {
		if ( tagID == null ) {
			throw new IllegalArgumentException("argument <trxID> is null");
		}

		if ( ! tagID.isSet() ) {
			throw new IllegalArgumentException("argument <trxID> is not set");
		}

		try {
			KMyMoneyTag tag = super.getTagByID(tagID);
			return new KMyMoneyWritableTagImpl((KMyMoneyTagImpl) tag, true);
		} catch (Exception exc) {
			LOGGER.error(
					"getWritableTagByID: Could not instantiate writable tag object from read-only account object (ID: "
							+ tagID + ")");
			throw new RuntimeException(
					"Could not instantiate writable tag object from read-only account object (ID: " + tagID + ")");
		}
	}

	/**
	 * This overridden method creates the writable version of the returned object.
	 *
	 * @return the new tag
	 * @see KMyMoneyFileImpl#createTag()
	 */
	@Override
	public KMyMoneyWritableTag createWritableTag(final String name) {
		KMyMoneyWritableTagImpl tag = new KMyMoneyWritableTagImpl(this);
		tag.setName(name);
		((org.kmymoney.api.write.impl.hlp.FileTagManager) super.tagMgr)
			.addTag(tag);
		
		return tag;
	}

	// ---------------------------------------------------------------
	
	@Override
	public KMyMoneyWritableCurrency getWritableCurrencyByID(String currCode) {
		if ( currCode == null ) {
			throw new IllegalArgumentException("argument <currCode> is null");
		}

		if ( currCode.trim().equals("") ) {
			throw new IllegalArgumentException("argument <currCode> is not set");
		}

		KMyMoneyCurrency curr = super.getCurrencyByID(currCode);
		return new KMyMoneyWritableCurrencyImpl((KMyMoneyCurrencyImpl) curr);
	}

	@Override
	public KMyMoneyWritableCurrency getWritableCurrencyByQualifID(KMMQualifCurrID qualifID) {
		if ( qualifID == null ) {
			throw new IllegalArgumentException("argument <qualifID> is null");
		}

		if ( ! qualifID.isSet() ) {
			throw new IllegalArgumentException("argument <qualifID> is not set");
		}

		KMyMoneyCurrency curr = super.getCurrencyByQualifID(qualifID);
		return new KMyMoneyWritableCurrencyImpl((KMyMoneyCurrencyImpl) curr);
	}

	@Override
	public Collection<KMyMoneyWritableCurrency> getWritableCurrencies() {
		Collection<KMyMoneyWritableCurrency> result = new ArrayList<KMyMoneyWritableCurrency>();

		for ( KMyMoneyCurrency curr : super.getCurrencies() ) {
			KMyMoneyWritableCurrency newCurr = new KMyMoneyWritableCurrencyImpl((KMyMoneyCurrencyImpl) curr);
			result.add(newCurr);
		}

		return result;
	}

	@Override
	public KMyMoneyWritableCurrency createWritableCurrency(String currID, String name) {
		KMyMoneyWritableCurrencyImpl curr = new KMyMoneyWritableCurrencyImpl(this, Currency.getInstance(currID));
		curr.setName(name);
		((org.kmymoney.api.write.impl.hlp.FileCurrencyManager) super.currMgr)
			.addCurrency(curr);
		
		return curr;
	}

	/**
	 * Add a new currency.<br/>
	 * If the currency already exists, add a new price-quote for it.
	 *
	 * @param pCmdtySpace        the name space (e.g. "GOODS" or "CURRENCY")
	 * @param pCmdtyId           the currency-name
	 * @param conversionFactor   the conversion-factor from the base-currency (EUR).
	 * @param pCmdtyNameFraction number of decimal-places after the comma
	 * @param pCmdtyName         common name of the new currency
	 */
	@Override
	public void addCurrency(final String pCmdtySpace, final String pCmdtyId, final FixedPointNumber conversionFactor,
			final int pCmdtyNameFraction, final String pCmdtyName) {

		if ( conversionFactor == null ) {
			throw new IllegalArgumentException("argument <conversionFactor> is null");
		}
		if ( pCmdtySpace == null ) {
			throw new IllegalArgumentException("argument <pCmdtySpace> is null");
		}
		if ( pCmdtyId == null ) {
			throw new IllegalArgumentException("argument <pCmdtyId> is null");
		}
		if ( pCmdtyName == null ) {
			throw new IllegalArgumentException("argument <pCmdtyName> is null");
		}
		
		/*
		 * ::TODO
		if ( getCurrencyTable().getConversionFactor(pCmdtySpace, pCmdtyId) == null ) {

			CURRENCY newCurrency = getObjectFactory().createGncV2GncBookGncCommodity();
			newCurrency.setCmdtyFraction(pCmdtyNameFraction);
			newCurrency.setCmdtySpace(pCmdtySpace);
			newCurrency.setCmdtyId(pCmdtyId);
			newCurrency.setCmdtyName(pCmdtyName);
			newCurrency.setVersion(Const.XML_FORMAT_VERSION);
			getRootElement().getGncBook().getBookElements().add(newCurrency);
			incrementCountDataFor("commodity");
		}
		// add price-quote
		CURRENCY currency = new GncV2.GncBook.GncPricedb.Price.PriceCommodity();
		currency.setCmdtySpace(pCmdtySpace);
		currency.setCmdtyId(pCmdtyId);

		CURRENCY baseCurrency = getObjectFactory()
				.createGncV2GncBookGncPricedbPricePriceCurrency();
		baseCurrency.setCmdtySpace(CurrencyNameSpace.NAMESPACE_CURRENCY);
		baseCurrency.setCmdtyId(getDefaultCurrencyID());

		PRICE newQuote = getObjectFactory().createGncV2GncBookGncPricedbPrice();
		newQuote.setPriceSource("JKMyMoneyLib");
		newQuote.setPriceId(getObjectFactory().createGncV2GncBookGncPricedbPricePriceId());
		newQuote.getPriceId().setType(Const.XML_DATA_TYPE_GUID);
		newQuote.getPriceId().setValue(createGUID());
		newQuote.setPriceCommodity(currency);
		newQuote.setPriceCurrency(baseCurrency);
		newQuote.setPriceTime(getObjectFactory().createGncV2GncBookGncPricedbPricePriceTime());
		newQuote.getPriceTime().setTsDate(PRICE_QUOTE_DATE_FORMAT.format(new Date()));
		newQuote.setPriceType("last");
		newQuote.setPriceValue(conversionFactor.toKMyMoneyString());

		List<Object> bookElements = getRootElement().getBookElements();
		for ( Object element : bookElements ) {
			if ( element instanceof GncV2.GncBook.GncPricedb ) {
				GncV2.GncBook.GncPricedb prices = (GncV2.GncBook.GncPricedb) element;
				prices.getPrice().add(newQuote);
				getCurrencyTable().setConversionFactor(pCmdtySpace, pCmdtyId, conversionFactor);
				return;
			}
		}
		throw new IllegalStateException("No priceDB in Book in KMyMoney file");
		*/
	}

	// ---------------------------------------------------------------

	@Override
	public KMyMoneyWritableSecurity getWritableSecurityByID(KMMSecID secID) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

		KMyMoneySecurity sec = super.getSecurityByID(secID);
		return new KMyMoneyWritableSecurityImpl((KMyMoneySecurityImpl) sec);
	}

	@Override
	public KMyMoneyWritableSecurity getWritableSecurityByQualifID(KMMQualifSecID qualifID) {
		if ( qualifID == null ) {
			throw new IllegalArgumentException("argument <qualifID> is null");
		}

		if ( ! qualifID.isSet() ) {
			throw new IllegalArgumentException("argument <qualifID> is not set");
		}

		KMyMoneySecurity sec = super.getSecurityByQualifID(qualifID);
		return new KMyMoneyWritableSecurityImpl((KMyMoneySecurityImpl) sec);
	}

	@Override
	public KMyMoneyWritableSecurity getWritableSecurityBySymbol(final String symb) {
		KMyMoneySecurity sec = super.getSecurityBySymbol(symb);
		return new KMyMoneyWritableSecurityImpl((KMyMoneySecurityImpl) sec);
	}

	@Override
	public KMyMoneyWritableSecurity getWritableSecurityByCode(final String code) {
		KMyMoneySecurity sec = super.getSecurityByCode(code);
		return new KMyMoneyWritableSecurityImpl((KMyMoneySecurityImpl) sec);
	}

	@Override
	public List<KMyMoneyWritableSecurity> getWritableSecuritiesByName(final String expr) {
		List<KMyMoneyWritableSecurity> result = new ArrayList<KMyMoneyWritableSecurity>();

		for ( KMyMoneySecurity sec : super.getSecuritiesByName(expr) ) {
			KMyMoneyWritableSecurity newSec = new KMyMoneyWritableSecurityImpl((KMyMoneySecurityImpl) sec);
			result.add(newSec);
		}

		return result;
    }

	@Override
	public List<KMyMoneyWritableSecurity> getWritableSecuritiesByName(final String expr, final boolean relaxed) {
		List<KMyMoneyWritableSecurity> result = new ArrayList<KMyMoneyWritableSecurity>();

		for ( KMyMoneySecurity sec : super.getSecuritiesByName(expr, relaxed) ) {
			KMyMoneyWritableSecurity newSec = new KMyMoneyWritableSecurityImpl((KMyMoneySecurityImpl) sec);
			result.add(newSec);
		}

		return result;
	}
    
	@Override
	public KMyMoneyWritableSecurity getWritableSecurityByNameUniq(final String expr) 
			throws NoEntryFoundException, TooManyEntriesFoundException {
		KMyMoneySecurity sec = super.getSecurityByNameUniq(expr);
		return new KMyMoneyWritableSecurityImpl((KMyMoneySecurityImpl) sec);
	}
    
	@Override
	public Collection<KMyMoneyWritableSecurity> getWritableSecuritiesByType(KMMSecCurr.Type type) {
		Collection<KMyMoneyWritableSecurity> result = new ArrayList<KMyMoneyWritableSecurity>();
		
		for ( KMyMoneySecurity sec : getSecuritiesByType(type) ) {
			KMyMoneyWritableSecurityImpl newSec = new KMyMoneyWritableSecurityImpl((KMyMoneySecurityImpl) sec);
			result.add(newSec);
		}
		
		return result;
	}

	@Override
	public Collection<KMyMoneyWritableSecurity> getWritableSecuritiesByTypeAndName(KMMSecCurr.Type type, String expr, 
																				   boolean relaxed) {
		Collection<KMyMoneyWritableSecurity> result = new ArrayList<KMyMoneyWritableSecurity>();
		
		for ( KMyMoneySecurity sec : getSecuritiesByTypeAndName(type, expr, relaxed) ) {
			KMyMoneyWritableSecurityImpl newSec = new KMyMoneyWritableSecurityImpl((KMyMoneySecurityImpl) sec);
			result.add(newSec);
		}
		
		return result;
	}

	@Override
	public Collection<KMyMoneyWritableSecurity> getWritableSecurities() {
		Collection<KMyMoneyWritableSecurity> result = new ArrayList<KMyMoneyWritableSecurity>();

		for ( KMyMoneySecurity sec : super.getSecurities() ) {
			KMyMoneyWritableSecurity newSec = new KMyMoneyWritableSecurityImpl((KMyMoneySecurityImpl) sec);
			result.add(newSec);
		}

		return result;
	}

	@Override
	public KMyMoneyWritableSecurity createWritableSecurity(
			final KMMSecCurr.Type type,
			final String code, // <-- e.g., ISIN
			final String name) {
		KMyMoneyWritableSecurityImpl sec = new KMyMoneyWritableSecurityImpl(this);
		sec.setType(type);
	    sec.setCode(code);
		sec.setName(name);
		((org.kmymoney.api.write.impl.hlp.FileSecurityManager) super.secMgr)
			.addSecurity(sec);
		
		return sec;
	}

	@Override
	public void removeSecurity(KMyMoneyWritableSecurity sec) throws ObjectCascadeException {
		if ( sec == null ) {
			throw new IllegalArgumentException("argument <sec> is null");
		}

		if ( sec.getQuotes().size() > 0 ) {
			LOGGER.error("removeSecurity: Security with ID '" + sec.getID() + "' cannot be removed because "
					+ "there are price objects in the Price DB that depend on it");
			throw new ObjectCascadeException();
		}

		if ( sec.getTransactionSplits().size() > 0 ) {
			LOGGER.error("removeSecurity: Security with ID '" + sec.getID() + "' cannot be removed because "
					+ "there are transactions (splits) that depend on it");
			throw new ObjectCascadeException();
		}

		((org.kmymoney.api.write.impl.hlp.FileSecurityManager) super.secMgr)
			.removeSecurity(sec);
		
		getRootElement().getSECURITIES().getSECURITY().remove(((KMyMoneyWritableSecurityImpl) sec).getJwsdpPeer());
		setModified(true);
	}

	// ---------------------------------------------------------------

	@Override
	public KMyMoneyWritablePricePair getWritablePricePairByID(KMMPricePairID prcPrID) {
		if ( prcPrID == null ) {
			throw new IllegalArgumentException("argument <prcPrID> is null");
		}

		if ( ! prcPrID.isSet() ) {
			throw new IllegalArgumentException("argument <prcPrID> is not set");
		}

		KMyMoneyPricePair prcPr = super.getPricePairByID(prcPrID);
		if ( prcPr == null ) {
			return null;
		}
		
		return new KMyMoneyWritablePricePairImpl((KMyMoneyPricePairImpl) prcPr);
	}

	@Override
	public Collection<KMyMoneyWritablePricePair> getWritablePricePairs() {
		Collection<KMyMoneyWritablePricePair> result = new ArrayList<KMyMoneyWritablePricePair>();

		for ( KMyMoneyPricePair sec : super.getPricePairs() ) {
			KMyMoneyWritablePricePair newSec = new KMyMoneyWritablePricePairImpl((KMyMoneyPricePairImpl) sec);
			result.add(newSec);
		}

		return result;
	}

	// ----------------------------

	@Override
	public KMyMoneyWritablePricePair createWritablePricePair(
			final KMMQualifSecCurrID fromSecCurrID, 
			final KMMQualifCurrID toCurrID) {
		if ( fromSecCurrID == null ) {
			throw new IllegalArgumentException("argument <fromSecCurrID> is null");
		}

		if ( ! fromSecCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <fromSecCurrID> is not set");
		}

		if ( toCurrID == null ) {
			throw new IllegalArgumentException("argument <toCurrID> is null");
		}

		if ( ! toCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <toCurrID> is not set");
		}

		KMyMoneyWritablePricePairImpl prc = new KMyMoneyWritablePricePairImpl(fromSecCurrID, toCurrID, 
																			  this);
		((org.kmymoney.api.write.impl.hlp.FilePriceManager) super.prcMgr)
			.addPricePair(prc);
		
		return prc;
	}

	@Override
	public KMyMoneyWritablePricePair createWritablePricePair(KMMPricePairID prcPrID) {
		KMyMoneyWritablePricePairImpl prc = new KMyMoneyWritablePricePairImpl(prcPrID, this);
		((org.kmymoney.api.write.impl.hlp.FilePriceManager) super.prcMgr)
			.addPricePair(prc);
		
		return prc;
	}
	
	@Override
	public void removePricePair(KMyMoneyWritablePricePair prcPr) {
		// 1) remove avatar in price manager
		((org.kmymoney.api.write.impl.hlp.FilePriceManager) super.prcMgr)
			.removePricePair(prcPr);

		// 2) remove price pair
		((org.kmymoney.api.write.impl.hlp.FilePriceManager) super.prcMgr)
			.removePricePair_raw(prcPr.getID());
	
		// 3) set 'modified' flag
		setModified(true);
	}

	// ---------------------------------------------------------------

	@Override
	public KMyMoneyWritablePrice getWritablePriceByID(KMMPriceID prcID) {
		if ( prcID == null ) {
			throw new IllegalArgumentException("argument <prcID> is null");
		}

		if ( ! prcID.isSet() ) {
			throw new IllegalArgumentException("argument <prcID> is not set");
		}

		KMyMoneyPrice prc = super.getPriceByID(prcID);
		return new KMyMoneyWritablePriceImpl((KMyMoneyPriceImpl) prc);
	}

	public KMyMoneyWritablePrice getWritablePriceBySecIDDate(final KMMSecID secID, final LocalDate date) {
		KMyMoneyPrice prc = prcMgr.getPriceBySecIDDate(secID, date);
		return new KMyMoneyWritablePriceImpl((KMyMoneyPriceImpl) prc);
	}
	
	public KMyMoneyWritablePrice getWritablePriceByQualifSecIDDate(final KMMQualifSecID secID, final LocalDate date) {
		KMyMoneyPrice prc = prcMgr.getPriceByQualifSecIDDate(secID, date);
		return new KMyMoneyWritablePriceImpl((KMyMoneyPriceImpl) prc);
	}
	
	public KMyMoneyWritablePrice getWritablePriceByCurrDate(final Currency curr, final LocalDate date) {
		KMyMoneyPrice prc = prcMgr.getPriceByCurrDate(curr, date);
		return new KMyMoneyWritablePriceImpl((KMyMoneyPriceImpl) prc);
	}
	
	public KMyMoneyWritablePrice getWritablePriceByQualifCurrIDDate(final KMMQualifCurrID currID, final LocalDate date) {
		KMyMoneyPrice prc = prcMgr.getPriceByQualifCurrIDDate(currID, date);
		return new KMyMoneyWritablePriceImpl((KMyMoneyPriceImpl) prc);
	}
	
	public KMyMoneyWritablePrice getWritablePriceByQualifSecCurrIDDate(final KMMQualifSecCurrID secCurrID, final LocalDate date) {
		KMyMoneyPrice prc = prcMgr.getPriceByQualifSecCurrIDDate(secCurrID, date);
		return new KMyMoneyWritablePriceImpl((KMyMoneyPriceImpl) prc);
	}
	
	// ---------------------------------------------------------------
	
	@Override
	public Collection<KMyMoneyWritablePrice> getWritablePrices() {
		Collection<KMyMoneyWritablePrice> result = new ArrayList<KMyMoneyWritablePrice>();

		for ( KMyMoneyPrice sec : super.getPrices() ) {
			KMyMoneyWritablePrice newSec = new KMyMoneyWritablePriceImpl((KMyMoneyPriceImpl) sec);
			result.add(newSec);
		}

		return result;
	}
	
	// ----------------------------

	@Override
	public KMyMoneyWritablePrice createWritablePrice(
			final KMyMoneyPricePairImpl prcPr,
			final LocalDate date) {
		KMyMoneyWritablePriceImpl prc = new KMyMoneyWritablePriceImpl(prcPr, this);
		prc.setDate(date);
		((org.kmymoney.api.write.impl.hlp.FilePriceManager) super.prcMgr)
			.addPrice(prc);
		
		return prc;
	}

	@Override
	public void removePrice(KMyMoneyWritablePrice prc) {
		// 1) remove avatar in price manager
		((org.kmymoney.api.write.impl.hlp.FilePriceManager) super.prcMgr)
			.removePrice(prc);
		
		// 2) remove price
		((org.kmymoney.api.write.impl.hlp.FilePriceManager) super.prcMgr)
			.removePrice_raw(prc.getID());
		
		// 3) set 'modified' flag
		setModified(true);
	}

	// ---------------------------------------------------------------
	
	@Override
	public KMMInstID getNewInstitutionID() {
		int counter = 0;
		
		for ( KMyMoneyInstitution inst : getInstitutions() ) {
			try {
				String coreID = inst.getID().get().substring(1);
				if ( Integer.parseInt(coreID) > counter ) {
					counter = Integer.parseInt(coreID);
				}
			} catch (Exception e) {
				throw new CannotGenerateKMMIDException();
			}
		}
		
		counter++;
		
		return new KMMInstID(counter);
	}

	@Override
	public KMMAcctID getNewAccountID() {
		int counter = 0;
		
		for ( KMyMoneyAccount trx : getAccounts() ) {
			try {
				if ( trx.getID().getType() == KMMComplAcctID.Type.STANDARD ) {
					String coreID = trx.getID().getStdID().get().substring(1);
					if ( Integer.parseInt(coreID) > counter ) {
						counter = Integer.parseInt(coreID);
					}
				}
			} catch (Exception e) {
				throw new CannotGenerateKMMIDException();
			}
		}
		
		counter++;
		
		return new KMMAcctID(counter);
	}

	@Override
	public KMMTrxID getNewTransactionID() {
		int counter = 0;
		
		for ( KMyMoneyTransaction trx : getTransactions() ) {
			try {
				String coreID = trx.getID().get().substring(1);
				if ( Integer.parseInt(coreID) > counter ) {
					counter = Integer.parseInt(coreID);
				}
			} catch (Exception e) {
				throw new CannotGenerateKMMIDException();
			}
		}
		
		counter++;
		
		return new KMMTrxID(counter);
	}

	@Override
	public KMMSpltID getNewSplitID() {
		int counter = 0;
		
		for ( KMyMoneyTransactionSplit splt : getTransactionSplits() ) {
			try {
				String coreID = splt.getID().get().substring(1);
				if ( Integer.parseInt(coreID) > counter ) {
					counter = Integer.parseInt(coreID);
				}
			} catch (Exception e) {
				throw new CannotGenerateKMMIDException();
			}
		}
		
		counter++;
		
		return new KMMSpltID(counter);
	}

	@Override
	public KMMPyeID getNewPayeeID() {
		int counter = 0;
		
		for ( KMyMoneyPayee pye : getPayees() ) {
			try {
				String coreID = pye.getID().get().substring(1);
				if ( Integer.parseInt(coreID) > counter ) {
					counter = Integer.parseInt(coreID);
				}
			} catch (Exception e) {
				throw new CannotGenerateKMMIDException();
			}
		}
		
		counter++;
		
		return new KMMPyeID(counter);
	}

	@Override
	public KMMTagID getNewTagID() {
		int counter = 0;
		
		for ( KMyMoneyTag tag : getTags() ) {
			try {
				String coreID = tag.getID().get().substring(1);
				if ( Integer.parseInt(coreID) > counter ) {
					counter = Integer.parseInt(coreID);
				}
			} catch (Exception e) {
				throw new CannotGenerateKMMIDException();
			}
		}
		
		counter++;
		
		return new KMMTagID(counter);
	}

	@Override
	public KMMSecID getNewSecurityID() {
		int counter = 0;
		
		for ( KMyMoneySecurity sec : getSecurities() ) {
			try {
				String coreID = sec.getID().get().substring(1);
				if ( Integer.parseInt(coreID) > counter ) {
					counter = Integer.parseInt(coreID);
				}
			} catch (Exception e) {
				throw new CannotGenerateKMMIDException();
			}
		}
		
		counter++;
		
		return new KMMSecID(counter);
	}

}
