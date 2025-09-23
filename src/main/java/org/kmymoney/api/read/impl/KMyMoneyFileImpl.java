package org.kmymoney.api.read.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.kmymoney.api.Const;
import org.kmymoney.api.currency.ComplexPriceTable;
import org.kmymoney.api.generated.KEYVALUEPAIRS;
import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.generated.PAIR;
import org.kmymoney.api.generated.PRICEPAIR;
import org.kmymoney.api.generated.PRICES;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyInstitution;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.aux.KMMFileMetaInfo;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.read.impl.hlp.FileAccountManager;
import org.kmymoney.api.read.impl.hlp.FileCurrencyManager;
import org.kmymoney.api.read.impl.hlp.FileInstitutionManager;
import org.kmymoney.api.read.impl.hlp.FilePayeeManager;
import org.kmymoney.api.read.impl.hlp.FilePriceManager;
import org.kmymoney.api.read.impl.hlp.FileSecurityManager;
import org.kmymoney.api.read.impl.hlp.FileTagManager;
import org.kmymoney.api.read.impl.hlp.FileTransactionManager;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.api.read.impl.hlp.NamespaceRemoverReader;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
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
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Implementation of KMyMoneyFile that can only read but not modify 
 * KMyMoney-Files. <br/>
 * 
 * @see KMyMoneyFile
 */
public class KMyMoneyFileImpl implements KMyMoneyFile
{

	protected static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyFileImpl.class);

    // ---------------------------------------------------------------
    // ::MAGIC
	
	// Cf. https://en.wikipedia.org/wiki/List_of_file_signatures
	private static final int GZIP_HEADER_BYTE_1 = 31;
    private static final int GZIP_HEADER_BYTE_2 = -117;
    
    protected static final String FILE_EXT_ZIPPED_1 = ".gz";
    protected static final String FILE_EXT_ZIPPED_2 = ".kmy";

    // ---------------------------------------------------------------

    private File file;
    
    // ----------------------------

    private KMYMONEYFILE rootElement;
    private KMyMoneyObjectImpl myKMyMoneyObject;

    // ----------------------------

    private volatile ObjectFactory myJAXBFactory;
    private volatile JAXBContext myJAXBContext;

    // ----------------------------
    
    protected FileInstitutionManager instMgr = null;
    protected FileAccountManager     acctMgr = null;
    protected FileTransactionManager trxMgr  = null;
    protected FilePayeeManager       pyeMgr  = null;
    protected FileTagManager         tagMgr  = null;
    protected FileSecurityManager    secMgr  = null;
    protected FileCurrencyManager    currMgr = null;
    
    // ----------------------------

    private final ComplexPriceTable  currencyTable = new ComplexPriceTable();
    protected FilePriceManager       prcMgr        = null;

    // ---------------------------------------------------------------

    /**
     * @param pFile the file to load and initialize from
     * @throws IOException on low level reading-errors (FileNotFoundException if not
     *                     found)
     * @see #loadFile(File)
     */
    public KMyMoneyFileImpl(final File pFile) throws IOException {
    	super();
    	loadFile(pFile);
    }

    /**
     * @param is the input stream to load and initialize from
     * @throws IOException on low level reading-errors (FileNotFoundException if not
     *                     found)
     * @see #loadInputStream(InputStream)
     */
    public KMyMoneyFileImpl(final InputStream is) throws IOException {
    	super();
    	loadInputStream(is);
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile() {
    	return file;
    }

    /**
     * Internal method, just sets this.file .
     *
     * @param pFile the file loaded
     */
    protected void setFile(final File pFile) {
    	if (pFile == null) {
    		throw new IllegalArgumentException("argument <pFile> is null");
    	}
    	
    	file = pFile;
    }

    // ----------------------------

    /**
     * loads the file and calls setRootElement.
     *
     * @param pFile the file to read
     * @throws IOException on low level reading-errors (FileNotFoundException if not found)
     * @see #setRootElement(KMYMONEYFILE)
     */
    protected void loadFile(final File pFile) throws IOException{
		long start = System.currentTimeMillis();

		if ( pFile == null ) {
			throw new IllegalArgumentException("argument <pFile> is null");
		}

		if ( !pFile.exists() ) {
			throw new IllegalArgumentException("File '" + pFile.getAbsolutePath() + "' does not exist");
		}

		setFile(pFile);

		InputStream in = new FileInputStream(pFile);
		if ( pFile.getName().endsWith(FILE_EXT_ZIPPED_1) ||
			 pFile.getName().endsWith(FILE_EXT_ZIPPED_2) ) {
			in = new BufferedInputStream(in);
			in = new GZIPInputStream(in);
		} else {
			// determine if it's gzipped by the magic bytes
			byte[] magic = new byte[2];
			in.read(magic);
			in.close();

			in = new FileInputStream(pFile);
			in = new BufferedInputStream(in);
			if ( magic[0] == GZIP_HEADER_BYTE_1 && 
				 magic[1] == GZIP_HEADER_BYTE_2 ) {
				in = new GZIPInputStream(in);
			}
		}

		loadInputStream(in);

		long end = System.currentTimeMillis();
		LOGGER.info("loadFile: Took " + (end - start) + " ms (total) ");
    }

    protected void loadInputStream(InputStream in) throws UnsupportedEncodingException, IOException{
		long start = System.currentTimeMillis();

		NamespaceRemoverReader reader = new NamespaceRemoverReader(new InputStreamReader(in, "utf-8"));
		try {

			JAXBContext myContext = getJAXBContext();
			if ( myContext == null ) {
				LOGGER.error("loadInputStream: JAXB context cannot be found/generated");
				throw new IOException("JAXB context cannot be found/generated");
			}
			Unmarshaller unmarshaller = myContext.createUnmarshaller();

			KMYMONEYFILE obj = (KMYMONEYFILE) unmarshaller.unmarshal(new InputSource(new BufferedReader(reader)));
			long start2 = System.currentTimeMillis();
			setRootElement(obj);
			long end = System.currentTimeMillis();
			LOGGER.info("loadInputStream: Took " + (end - start) + " ms (total), " + (start2 - start)
					+ " ms (jaxb-loading), " + (end - start2) + " ms (building facades)");

		} catch (JAXBException e) {
			LOGGER.error("loadInputStream: " + e.getMessage(), e);
			throw new IllegalStateException(e);
		} finally {
			reader.close();
		}
    }

    // ---------------------------------------------------------------

    /**
     * @return Returns the currencyTable.
     */
    public ComplexPriceTable getCurrencyTable() {
    	return currencyTable;
    }

    /**
     * Use a heuristic to determine the defaultcurrency-id. If we cannot find one,
     * we default to EUR.<br/>
     * Comodity-stace is fixed as "ISO4217" .
     *
     * @return the default-currency to use.
     */
    public String getDefaultCurrencyID() {
		KMYMONEYFILE root = getRootElement();
		if ( root == null ) {
			return Const.DEFAULT_CURRENCY;
		}

		KEYVALUEPAIRS kvpList = root.getKEYVALUEPAIRS();
		if ( kvpList == null ) {
			return Const.DEFAULT_CURRENCY;
		}

		for ( PAIR kvp : kvpList.getPAIR() ) {
			if ( kvp.getKey().equals("kmm-baseCurrency") ) { // ::MAGIC
				return kvp.getValue();
			}
		}

		// not found
		return Const.DEFAULT_CURRENCY;
    }

    // ---------------------------------------------------------------

    @Override
    public KMyMoneyInstitution getInstitutionByID(final KMMInstID instID) {
		if ( instID == null ) {
			throw new IllegalArgumentException("argument <instID> is null");
		}

		if ( ! instID.isSet() ) {
			throw new IllegalArgumentException("argument <instID> is not set");
		}

		return instMgr.getInstitutionByID(instID);
    }

    @Override
    public Collection<KMyMoneyInstitution> getInstitutionsByName(String expr) {
    	return instMgr.getInstitutionsByName(expr);
    }

    @Override
    public Collection<KMyMoneyInstitution> getInstitutionsByName(String expr, boolean relaxed) {
    	return instMgr.getInstitutionsByName(expr, relaxed);
    }

    @Override
    public KMyMoneyInstitution getInstitutionByNameUniq(String expr)
	    throws NoEntryFoundException, TooManyEntriesFoundException {
    	return instMgr.getInstitutionsByNameUniq(expr);
    }

    @Override
    public Collection<KMyMoneyInstitution> getInstitutions() {
    	return instMgr.getInstitutions();
    }

    // ---------------------------------------------------------------

    @Override
    public KMyMoneyAccount getAccountByID(final KMMComplAcctID acctID) {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}

		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

		return acctMgr.getAccountByID(acctID);
    }

    @Override
    public KMyMoneyAccount getAccountByID(final KMMAcctID acctID) {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}

		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

		return acctMgr.getAccountByID(acctID);
    }

    /**
     * @param acctID if null, gives all account that have no parent
     * @return the sorted collection of children of that account
     */
    @Override
    public List<KMyMoneyAccount> getAccountsByParentID(final KMMComplAcctID acctID) {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}

		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

        return acctMgr.getAccountsByParentID(acctID);
    }

    @Override
    public List<KMyMoneyAccount> getAccountsByName(final String name) {
		if ( name == null ) {
			throw new IllegalArgumentException("argument <name> is null");
		}

		if ( name.trim().equals("") ) {
			throw new IllegalArgumentException("argument <name> is empty");
		}

		return acctMgr.getAccountsByName(name);
    }
    
    /**
     * @see KMyMoneyFile#getAccountsByName(java.lang.String)
     */
    @Override
    public List<KMyMoneyAccount> getAccountsByName(final String expr, boolean qualif, boolean relaxed) {
		if ( expr == null ) {
			throw new IllegalArgumentException("argument <expr> is null");
		}

		if ( expr.trim().equals("") ) {
			throw new IllegalArgumentException("argument <expr> is empty");
		}

    	return acctMgr.getAccountsByName(expr, qualif, relaxed);
    }

    @Override
    public KMyMoneyAccount getAccountByNameUniq(final String name, final boolean qualif) throws NoEntryFoundException, TooManyEntriesFoundException {
		if ( name == null ) {
			throw new IllegalArgumentException("argument <name> is null");
		}

		if ( name.trim().equals("") ) {
			throw new IllegalArgumentException("argument <name> is empty");
		}

    	return acctMgr.getAccountByNameUniq(name, qualif);
    }
    
    /**
     * warning: this function has to traverse all accounts. If it much faster to try
     * getAccountByID first and only call this method if the returned account does
     * not have the right name.
     *
     * @param nameRegEx the regular expression of the name to look for
     * @return null if not found
     * @throws TooManyEntriesFoundException 
     * @throws NoEntryFoundException 
     */
    @Override
    public KMyMoneyAccount getAccountByNameEx(final String nameRegEx) throws NoEntryFoundException, TooManyEntriesFoundException {
		if ( nameRegEx == null ) {
			throw new IllegalArgumentException("argument <nameRegEx> is null");
		}

		if ( nameRegEx.trim().equals("") ) {
			throw new IllegalArgumentException("argument <nameRegEx> is empty");
		}

		return acctMgr.getAccountByNameEx(nameRegEx);
    }

    /**
     * First try to fetch the account by id, then fall back to traversing all
     * accounts to get if by it's name.
     *
     * @param acctID   the id to look for
     * @param name the name to look for if nothing is found for the id
     * @return null if not found
     * @throws TooManyEntriesFoundException 
     * @throws NoEntryFoundException 
     */
    @Override
    public KMyMoneyAccount getAccountByIDorName(final KMMComplAcctID acctID, final String name) throws NoEntryFoundException, TooManyEntriesFoundException {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}

		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

		if ( name == null ) {
			throw new IllegalArgumentException("argument <name> is null");
		}

		if ( name.trim().equals("") ) {
			throw new IllegalArgumentException("argument <name> is empty");
		}

		return acctMgr.getAccountByIDorName(acctID, name);
    }

    /**
     * First try to fetch the account by id, then fall back to traversing all
     * accounts to get if by it's name.
     *
     * @param acctID   the id to look for
     * @param name the regular expression of the name to look for if nothing is
     *             found for the id
     * @return null if not found
     * @throws TooManyEntriesFoundException 
     * @throws NoEntryFoundException 
     */
    @Override
    public KMyMoneyAccount getAccountByIDorNameEx(final KMMComplAcctID acctID, final String name) throws NoEntryFoundException, TooManyEntriesFoundException {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}

		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

		if ( name == null ) {
			throw new IllegalArgumentException("argument <name> is null");
		}

		if ( name.trim().equals("") ) {
			throw new IllegalArgumentException("argument <name> is empty");
		}

		return acctMgr.getAccountByIDorNameEx(acctID, name);
    }

    @Override
    public List<KMyMoneyAccount> getAccountsByType(KMyMoneyAccount.Type type) {
    	return acctMgr.getAccountsByType(type);
    }

    @Override
    public List<KMyMoneyAccount> getAccountsByTypeAndName(KMyMoneyAccount.Type type, String expr, 
	                                                            boolean qualif, boolean relaxed) {
    	return acctMgr.getAccountsByTypeAndName(type, expr, qualif, relaxed);
    }

    /**
     * @return a read-only collection of all accounts
     */
    @Override
    public List<KMyMoneyAccount> getAccounts() {
        return acctMgr.getAccounts();
    }

    /**
     * @return a read-only collection of all accounts that have no parent (the
     *         result is sorted)
     */
    @Override
    public KMyMoneyAccount getRootAccount() {
    	return null;
    }

    /**
     * @return a read-only collection of all accounts that have no parent (the
     *         result is sorted)
     */
    @Override
    public List<? extends KMyMoneyAccount> getParentlessAccounts() {
    	return acctMgr.getParentlessAccounts();
    }

    @Override
    public List<KMMComplAcctID> getTopAccountIDs() {
    	return acctMgr.getTopAccountIDs();
    }

    @Override
    public List<KMyMoneyAccount> getTopAccounts() {
    	return acctMgr.getTopAccounts();
    }

    // ---------------------------------------------------------------

    @Override
    public KMyMoneyTransaction getTransactionByID(final KMMTrxID trxID) {
		if ( trxID == null ) {
			throw new IllegalArgumentException("argument <trxID> is null");
		}

		if ( ! trxID.isSet() ) {
			throw new IllegalArgumentException("argument <trxID> is not set");
		}

		return trxMgr.getTransactionByID(trxID);
    }

    /**
     * @see KMyMoneyFile#getTransactions()
     */
    @Override
    public List<? extends KMyMoneyTransaction> getTransactions() {
    	return trxMgr.getTransactions();
    }
    
    @Override
    public List<? extends KMyMoneyTransaction> getTransactions(final LocalDate fromDate, final LocalDate toDate) {
		ArrayList<KMyMoneyTransaction> result = new ArrayList<KMyMoneyTransaction>();
		
		for ( KMyMoneyTransaction trx : getTransactions() ) {
			 if ( ( trx.getDatePosted().isEqual( fromDate ) ||
				    trx.getDatePosted().isAfter( fromDate ) ) &&
			      ( trx.getDatePosted().isEqual( toDate ) ||
					trx.getDatePosted().isBefore( toDate ) ) ) {
				 result.add(trx);
			 }
		}
		
		return Collections.unmodifiableList(result);
    }
    
    // ----------------------------
    
    /**
     * @return
     */
    @Override
    public List<KMyMoneyTransactionSplit> getTransactionSplitsBySecID(final KMMSecID secID) {
		return trxMgr.getTransactionSplitsBySecID(secID);
    }

    /**
     * @return
     */
    @Override
    public List<KMyMoneyTransactionSplit> getTransactionSplitsByQualifSecID(final KMMQualifSecID qualifID) {
		return trxMgr.getTransactionSplitsByQualifSecID(qualifID);
    }

    /**
     * @return
     */
    @Override
    public List<KMyMoneyTransactionSplit> getTransactionSplitsByCurr(final Currency curr) {
		return trxMgr.getTransactionSplitsByCurr(curr);
    }

    /**
     * @return
     */
    @Override
    public List<KMyMoneyTransactionSplit> getTransactionSplitsByQualifCurrID(final KMMQualifCurrID qualifID) {
		return trxMgr.getTransactionSplitsByQualifCurrID(qualifID);
    }

    /**
     * @return
     */
    @Override
    public List<KMyMoneyTransactionSplit> getTransactionSplitsByQualifSecCurrID(final KMMQualifSecCurrID secID) {
		return trxMgr.getTransactionSplitsByQualifSecCurrID(secID);
    }

    // ---------------------------------------------------------------
    
    @Override
    public KMyMoneyTransactionSplit getTransactionSplitByID(final KMMQualifSpltID spltID) {
		if ( spltID == null ) {
			throw new IllegalArgumentException("argument <spltID> is null");
		}

		if ( ! spltID.isSet() ) {
			throw new IllegalArgumentException("argument <spltID> is not set");
		}

		return trxMgr.getTransactionSplitByID(spltID);
    }

    @Override
    public List<KMyMoneyTransactionSplit> getTransactionSplits() {
    	return trxMgr.getTransactionSplits();
    }

    public Collection<KMyMoneyTransactionSplitImpl> getTransactionSplits_readAfresh() {
    	return trxMgr.getTransactionSplits_readAfresh();
    }

    public Collection<KMyMoneyTransactionSplitImpl> getTransactionSplits_readAfresh(final KMMTrxID trxID) {
		if ( trxID == null ) {
			throw new IllegalArgumentException("argument <trxID> is null");
		}

		if ( ! trxID.isSet() ) {
			throw new IllegalArgumentException("argument <trxID> is not set");
		}

		return trxMgr.getTransactionSplits_readAfresh(trxID);
    }

    // ---------------------------------------------------------------

    @Override
    public KMyMoneyPayee getPayeeByID(final KMMPyeID pyeID) {
		if ( pyeID == null ) {
			throw new IllegalArgumentException("argument <pyeID> is null");
		}

		if ( ! pyeID.isSet() ) {
			throw new IllegalArgumentException("argument <pyeID> is not set");
		}

		return pyeMgr.getPayeeByID(pyeID);
    }

    @Override
    public Collection<KMyMoneyPayee> getPayeesByName(String expr) {
    	return pyeMgr.getPayeesByName(expr);
    }

    @Override
    public Collection<KMyMoneyPayee> getPayeesByName(String expr, boolean relaxed) {
    	return pyeMgr.getPayeesByName(expr, relaxed);
    }

    @Override
    public KMyMoneyPayee getPayeeByNameUniq(String expr)
	    throws NoEntryFoundException, TooManyEntriesFoundException {
    	return pyeMgr.getPayeesByNameUniq(expr);
    }

    @Override
    public Collection<KMyMoneyPayee> getPayees() {
    	return pyeMgr.getPayees();
    }

    // ---------------------------------------------------------------

	@Override
	public KMyMoneyTag getTagByID(KMMTagID tagID) {
		if ( tagID == null ) {
			throw new IllegalArgumentException("argument <tagID> is null");
		}

		if ( ! tagID.isSet() ) {
			throw new IllegalArgumentException("argument <tagID> is not set");
		}

		return tagMgr.getTagByID(tagID);
	}

	@Override
	public Collection<KMyMoneyTag> getTagsByName(String expr) {
    	return tagMgr.getTagsByName(expr);
	}

	@Override
	public Collection<KMyMoneyTag> getTagsByName(String expr, boolean relaxed) {
    	return tagMgr.getTagsByName(expr, relaxed);
	}

	@Override
	public KMyMoneyTag getTagByNameUniq(String expr) 
			throws NoEntryFoundException, TooManyEntriesFoundException {
    	return tagMgr.getTagsByNameUniq(expr);
	}

	@Override
	public Collection<KMyMoneyTag> getTags() {
    	return tagMgr.getTags();
	}

    // ---------------------------------------------------------------

    @Override
    public KMyMoneySecurity getSecurityByID(final KMMSecID secID) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

		return secMgr.getSecurityByID(secID);
    }

    @Override
    public KMyMoneySecurity getSecurityByID(final String idStr) {
    	return secMgr.getSecurityByID(idStr);
    }

    @Override
    public KMyMoneySecurity getSecurityByQualifID(final KMMQualifSecID secID) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

		return secMgr.getSecurityByQualifID(secID);
    }

    @Override
    public KMyMoneySecurity getSecurityByQualifID(final String qualifIDStr) {
    	return secMgr.getSecurityByQualifID(qualifIDStr);
    }

    @Override
    public KMyMoneySecurity getSecurityBySymbol(final String symb) {
    	return secMgr.getSecurityBySymbol(symb);
    }

    @Override
    public KMyMoneySecurity getSecurityByCode(final String code) {
    	return secMgr.getSecurityByCode(code);
    }

    @Override
    public List<KMyMoneySecurity> getSecuritiesByName(final String expr) {
    	return secMgr.getSecuritiesByName(expr);
    }

    @Override
    public List<KMyMoneySecurity> getSecuritiesByName(final String expr, final boolean relaxed) {
    	return secMgr.getSecuritiesByName(expr, relaxed);
    }

    @Override
    public KMyMoneySecurity getSecurityByNameUniq(final String expr) throws NoEntryFoundException, TooManyEntriesFoundException {
    	return secMgr.getSecurityByNameUniq(expr);
    }
    

	@Override
	public List<KMyMoneySecurity> getSecuritiesByType(KMMSecCurr.Type type) {
    	return secMgr.getSecuritiesByType(type);
	}

	@Override
	public List<KMyMoneySecurity> getSecuritiesByTypeAndName(KMMSecCurr.Type type, String expr,
															 boolean relaxed) {
    	return secMgr.getSecuritiesByTypeAndName(type, expr, relaxed);
	}

    @Override
    public List<KMyMoneySecurity> getSecurities() {
    	return secMgr.getSecurities();
    }

    // ---------------------------------------------------------------

    @Override
    public KMyMoneyCurrency getCurrencyByID(String currID) {
    	return currMgr.getCurrencyByID(currID);
    }

    @Override
    public KMyMoneyCurrency getCurrencyByQualifID(KMMQualifCurrID currID) {
    	return currMgr.getCurrencyByQualifID(currID);
    }

    @Override
    public Collection<KMyMoneyCurrency> getCurrencies() {
    	return currMgr.getCurrencies();
    }

    // ---------------------------------------------------------------
    
    @Override
    public KMyMoneyPricePair getPricePairByID(KMMPricePairID prcPrID) {
    	return prcMgr.getPricePairByID(prcPrID);
    }

    @Override
    public Collection<KMyMoneyPricePair> getPricePairs() {
    	return prcMgr.getPricePairs();
    }

    // ---------------------------------------------------------------
    
    @Override
    public KMyMoneyPrice getPriceByID(KMMPriceID prcID) {
    	return prcMgr.getPriceByID(prcID);
    }

    public KMyMoneyPrice getPriceBySecIDDate(final KMMSecID secID, final LocalDate date) {
    	return prcMgr.getPriceBySecIDDate(secID, date);
    }
	
    public KMyMoneyPrice getPriceByQualifSecIDDate(final KMMQualifSecID secID, final LocalDate date) {
    	return prcMgr.getPriceByQualifSecIDDate(secID, date);
    }
	
    public KMyMoneyPrice getPriceByCurrDate(final Currency curr, final LocalDate date) {
    	return prcMgr.getPriceByCurrDate(curr, date);
    }
	
    public KMyMoneyPrice getPriceByQualifCurrIDDate(final KMMQualifCurrID currID, final LocalDate date) {
    	return prcMgr.getPriceByQualifCurrIDDate(currID, date);
    }
	
    public KMyMoneyPrice getPriceByQualifSecCurrIDDate(final KMMQualifSecCurrID secCurrID, final LocalDate date) {
    	return prcMgr.getPriceByQualifSecCurrIDDate(secCurrID, date);
    }
	
    // ---------------------------------------------------------------
    
    @Override
    public List<KMyMoneyPrice> getPrices() {
    	return prcMgr.getPrices();
    }

	@Override
	public List<KMyMoneyPrice> getPricesBySecID(final KMMSecID secID) {
		return prcMgr.getPricesBySecID(secID);
	}

	@Override
	public List<KMyMoneyPrice> getPricesByQualifSecID(final KMMQualifSecID secID) {
		return prcMgr.getPricesByQualifSecCurrID(secID);
	}

	@Override
	public List<KMyMoneyPrice> getPricesByCurr(final Currency curr) {
		return prcMgr.getPricesByCurr(curr);
	}

	@Override
	public List<KMyMoneyPrice> getPricesByQualifCurrID(final KMMQualifCurrID currID) {
		return prcMgr.getPricesByQualifSecCurrID(currID);
	}

	@Override
	public List<KMyMoneyPrice> getPricesByQualifSecCurrID(final KMMQualifSecCurrID secCurrID) {
		return prcMgr.getPricesByQualifSecCurrID(secCurrID);
	}

    @Override
    public FixedPointNumber getLatestPrice(KMMQualifSecCurrID secCurrID)
	    throws InvalidQualifSecCurrIDException {
    	return prcMgr.getLatestPrice(secCurrID);
    }

    // ---------------------------------------------------------------
    
    /**
     * @return the underlying JAXB-element
     */
    @SuppressWarnings("exports")
    public KMYMONEYFILE getRootElement() {
    	return rootElement;
    }

    /**
     * Set the new root-element and load all accounts, transactions,... from it.
     *
     * @param pRootElement the new root-element
     * @throws InvalidQualifSecCurrTypeException 
     * @throws InvalidQualifSecCurrIDException 
     */
    protected void setRootElement(final KMYMONEYFILE pRootElement) {
    	if (pRootElement == null) {
    		throw new IllegalArgumentException("argument <pRootElement> is null");
    	}
    	rootElement = pRootElement;

    	// fill prices
    	prcMgr  = new FilePriceManager(this);

    	loadPriceDatabase(pRootElement);

    	// fill maps
    	// CAUTION: The order matters
    	acctMgr = new FileAccountManager(this);
    	instMgr = new FileInstitutionManager(this);
    	pyeMgr  = new FilePayeeManager(this);
    	tagMgr  = new FileTagManager(this);
    	trxMgr  = new FileTransactionManager(this);
    	secMgr  = new FileSecurityManager(this);
    	currMgr = new FileCurrencyManager(this);
    }

    // ---------------------------------------------------------------

    /**
     * @param pRootElement the root-element of the KMyMoney-file
     * @throws InvalidQualifSecCurrTypeException 
     * @throws InvalidQualifSecCurrIDException 
     */
    private void loadPriceDatabase(final KMYMONEYFILE pRootElement) {
    	boolean noPriceDB = true;
	
    	PRICES priceDB = pRootElement.getPRICES();
    	if ( priceDB.getPRICEPAIR().size() > 0 )
    		noPriceDB = false;
	
		loadPriceDatabaseCore(priceDB);

		if ( noPriceDB ) {
			// no price DB in file
			getCurrencyTable().clear();
		}
    }

    private void loadPriceDatabaseCore(PRICES priceDB) {
//  	getCurrencyTable().clear();
//  	getCurrencyTable().setConversionFactor(KMMSecCurrID.Type.CURRENCY, 
//  		                               getDefaultCurrencyID(), 
//  		                               new FixedPointNumber(1));
	
		String baseCurrency = getDefaultCurrencyID();
	
		for ( PRICEPAIR pricePair : priceDB.getPRICEPAIR() ) {
			String fromSecCurr = pricePair.getFrom();
			// String toCurr      = pricePair.getTo();
	    
			// ::TODO: Try to implement Security type
			KMMQualifSecCurrID.Type nameSpace = null;
			if ( fromSecCurr.startsWith(KMMQualifSecCurrID.PREFIX_SECURITY) )
				nameSpace = KMMQualifSecCurrID.Type.SECURITY;
			else
				nameSpace = KMMQualifSecCurrID.Type.CURRENCY;

			// Check if we already have a latest price for this security
			// (= currency, fund, ...)
			if ( getCurrencyTable().getConversionFactor(nameSpace, fromSecCurr) != null ) {
				continue;
			}

			if ( fromSecCurr.equals(baseCurrency) ) {
					LOGGER.warn("loadPriceDatabaseCore: Ignoring price-quote for " + baseCurrency 
			    + " because " + baseCurrency + " is our base-currency.");
					continue;
			}

			// get the latest price in the file and insert it into
			// our currency table
			FixedPointNumber factor = getLatestPrice(new KMMQualifSecCurrID(nameSpace, fromSecCurr));

			if ( factor != null ) {
				getCurrencyTable().setConversionFactor(nameSpace, fromSecCurr, factor);
			} else {
				LOGGER.warn("loadPriceDatabaseCore: The KMyMoney file defines a factor for a security '" 
						+ fromSecCurr + "' but has no security for it");
			}
		} // for pricePair
    }

    // ---------------------------------------------------------------

    /**
     * @return the jaxb object-factory used to create new peer-objects to extend
     *         this
     */
    @SuppressWarnings("exports")
    public ObjectFactory getObjectFactory() {
    	if (myJAXBFactory == null) {
    		myJAXBFactory = new ObjectFactory();
    	}
    	
    	return myJAXBFactory;
    }

    /**
     * @return the JAXB-context
     */
    protected JAXBContext getJAXBContext() {
    	if (myJAXBContext == null) {
    		try {
    			myJAXBContext = JAXBContext.newInstance("org.kmymoney.api.generated", this.getClass().getClassLoader());
    		} catch (JAXBException e) {
    			LOGGER.error("getJAXBContext: " + e.getMessage(), e);
    		}
    	}
    	
    	return myJAXBContext;
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public KMyMoneyFile getKMyMoneyFile() {
    	return this;
    }
    
    // ---------------------------------------------------------------
    // Helpers for class FileStats_Cache
    
    @SuppressWarnings("exports")
    public FileInstitutionManager getInstMgr() {
    	return instMgr;
    }
    
    @SuppressWarnings("exports")
    public FileAccountManager getAcctMgr() {
    	return acctMgr;
    }
    
    @SuppressWarnings("exports")
    public FileTransactionManager getTrxMgr() {
    	return trxMgr;
    }
    
    @SuppressWarnings("exports")
    public FilePayeeManager getPyeMgr() {
    	return pyeMgr;
    }
    
    @SuppressWarnings("exports")
    public FileTagManager getTagMgr() {
    	return tagMgr;
    }
    
    @SuppressWarnings("exports")
    public FileSecurityManager getSecMgr() {
    	return secMgr;
    }
    
    @SuppressWarnings("exports")
    public FileCurrencyManager getCurrMgr() {
    	return currMgr;
    }
    
    @SuppressWarnings("exports")
    public FilePriceManager getPrcMgr() {
    	return prcMgr;
    }
    
    // ---------------------------------------------------------------
    
    public void dump() {
    	dump(System.out);
    }
    
    public void dump(PrintStream strm) {
    	strm.println("KMYMONEY FILE");
    	
    	// ------------------------

    	strm.println("");
    	strm.println("META INFO:"); 
    	KMMFileMetaInfo metaInfo;
    	try {
    		metaInfo = new KMMFileMetaInfo(this);

    		strm.println("  Creation date:      " + metaInfo.getCreationDate()); 
    		strm.println("  Last-modified date: " + metaInfo.getLastModifiedDate()); 
    		strm.println("  Version:            " + metaInfo.getVersion()); 
    		strm.println("  Fix version:        " + metaInfo.getFixVersion()); 
    		strm.println("  App version:        " + metaInfo.getAppVersion()); 
    		strm.println("  Base currency:      " + metaInfo.getBaseCurrency()); 
    		strm.println("  File ID:            " + metaInfo.getFileID()); 
    	} catch (Exception e) {
    		strm.println("ERROR"); 
    	}

    	strm.println("");
    	strm.println("Stats (raw):"); 
		KMMFileStats stats;
    	try {
			stats = new KMMFileStats(this);

			strm.println("  No. of accounts:           " + stats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
			strm.println("  No. of transactions:       " + stats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
			strm.println("  No. of transaction splits: " + stats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW));
			strm.println("  No. of payees:             " + stats.getNofEntriesPayees(KMMFileStats.Type.RAW));
			strm.println("  No. of tags:               " + stats.getNofEntriesTags(KMMFileStats.Type.RAW));
			strm.println("  No. of securities:         " + stats.getNofEntriesSecurities(KMMFileStats.Type.RAW));
			strm.println("  No. of currencies:         " + stats.getNofEntriesCurrencies(KMMFileStats.Type.RAW));
			strm.println("  No. of price pairs:        " + stats.getNofEntriesPricePairs(KMMFileStats.Type.RAW));
			strm.println("  No. of prices:             " + stats.getNofEntriesPrices(KMMFileStats.Type.RAW));
    	} catch (Exception e) {
    		strm.println("ERROR"); 
    	}
    	
    	// ------------------------

    	strm.println("");
    	strm.println("CONTENTS:"); 

    	strm.println("");
    	strm.println("Institutions:");
    	for ( KMyMoneyInstitution inst : getInstitutions() ) {
    		strm.println(" - " + inst.toString());
    	}

    	strm.println("");
    	strm.println("Accounts:");
    	for ( KMyMoneyAccount acct : getAccounts() ) {
    		strm.println(" - " + acct.toString());
    	}

    	strm.println("");
    	strm.println("Transactions:");
    	for ( KMyMoneyTransaction trx : getTransactions() ) {
    		strm.println(" - " + trx.toString());
        	for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
        		strm.println("   o " + splt.toString());
        	}
    	}

    	strm.println("");
    	strm.println("Payees:");
    	for ( KMyMoneyPayee vend : getPayees() ) {
    		strm.println(" - " + vend.toString());
    	}

    	strm.println("");
    	strm.println("Tags:");
    	for ( KMyMoneyTag tag : getTags() ) {
    		strm.println(" - " + tag.toString());
    	}

    	strm.println("");
    	strm.println("Securities:");
    	for ( KMyMoneySecurity empl : getSecurities() ) {
    		strm.println(" - " + empl.toString());
    	}

    	strm.println("");
    	strm.println("Currencies:");
    	for ( KMyMoneyCurrency job : getCurrencies() ) {
    		strm.println(" - " + job.toString());
    	}

    	strm.println("");
    	strm.println("Prices:");
    	for ( KMyMoneyPricePair prcPr : getPricePairs() ) {
    		strm.println(" - " + prcPr.toString());
        	for ( KMyMoneyPrice prc : prcPr.getPrices() ) {
        		strm.println("   o " + prc.toString());
        	}
    	}
    }
    // ---------------------------------------------------------------
    
    public String toString() {
    	String result = "KMyMoneyFileImpl: [\n";
	
    	result += "  Meta info:\n"; 
    	KMMFileMetaInfo metaInfo;
    	try {
    		metaInfo = new KMMFileMetaInfo(this);

    		result += "    Creation date:      " + metaInfo.getCreationDate() + "\n"; 
    		result += "    Last-modified date: " + metaInfo.getLastModifiedDate() + "\n"; 
    		result += "    Version:            " + metaInfo.getVersion() + "\n"; 
    		result += "    Fix version:        " + metaInfo.getFixVersion() + "\n"; 
    		result += "    App version:        " + metaInfo.getAppVersion() + "\n"; 
    		result += "    Base currency:      " + metaInfo.getBaseCurrency() + "\n"; 
    		result += "    File ID:            " + metaInfo.getFileID() + "\n"; 
    	} catch (Exception e) {
    		result += "ERROR\n"; 
    	}

    	result += "  Stats (raw):\n"; 
    	KMMFileStats stats;
    	try {
    		stats = new KMMFileStats(this);

    		result += "    No. of institutions:       " + stats.getNofEntriesInstitutions(KMMFileStats.Type.RAW) + "\n"; 
    		result += "    No. of accounts:           " + stats.getNofEntriesAccounts(KMMFileStats.Type.RAW) + "\n"; 
    		result += "    No. of transactions:       " + stats.getNofEntriesTransactions(KMMFileStats.Type.RAW) + "\n"; 
    		result += "    No. of transaction splits: " + stats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW) + "\n"; 
    		result += "    No. of payees:             " + stats.getNofEntriesPayees(KMMFileStats.Type.RAW) + "\n"; 
    		result += "    No. of securities:         " + stats.getNofEntriesSecurities(KMMFileStats.Type.RAW) + "\n"; 
    		result += "    No. of currencies:         " + stats.getNofEntriesCurrencies(KMMFileStats.Type.RAW) + "\n";
    		result += "    No. of price pairs:        " + stats.getNofEntriesPricePairs(KMMFileStats.Type.RAW) + "\n";
    		result += "    No. of prices:             " + stats.getNofEntriesPrices(KMMFileStats.Type.RAW) + "\n";
    	} catch (Exception e) {
    		result += "ERROR\n"; 
    	}
	
    	result += "]";
	
    	return result;
    }

}
