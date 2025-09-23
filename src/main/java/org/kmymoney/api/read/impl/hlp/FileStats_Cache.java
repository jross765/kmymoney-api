package org.kmymoney.api.read.impl.hlp;

import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStats_Cache implements FileStats {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(FileStats_Cache.class);

	// ---------------------------------------------------------------

	private FileInstitutionManager instMgr = null;
	private FileAccountManager     acctMgr = null;
	private FileTransactionManager trxMgr  = null;
	private FilePayeeManager       pyeMgr  = null;
	private FileTagManager         tagMgr  = null;

	private FileSecurityManager    secMgr  = null;
	private FileCurrencyManager    currMgr = null;
	private FilePriceManager       prcMgr  = null;

	// ---------------------------------------------------------------

	public FileStats_Cache(
			final FileInstitutionManager instMgr,
			final FileAccountManager acctMgr, 
			final FileTransactionManager trxMgr,
			final FilePayeeManager pyeMgr, 
			final FileTagManager tagMgr, 
			final FileSecurityManager secMgr, 
			final FileCurrencyManager currMgr,
			final FilePriceManager prcMgr) {
		this.instMgr = instMgr;
		this.acctMgr = acctMgr;
		this.trxMgr  = trxMgr;
		this.pyeMgr  = pyeMgr;
		this.tagMgr  = tagMgr;
		this.secMgr  = secMgr;
		this.currMgr = currMgr;
		this.prcMgr  = prcMgr;
	}

	public FileStats_Cache(final KMyMoneyFileImpl kmmFile) {
		this.instMgr = kmmFile.getInstMgr();
		this.acctMgr = kmmFile.getAcctMgr();
		this.trxMgr  = kmmFile.getTrxMgr();
		this.pyeMgr  = kmmFile.getPyeMgr();
		this.tagMgr  = kmmFile.getTagMgr();
		this.secMgr  = kmmFile.getSecMgr();
		this.currMgr = kmmFile.getCurrMgr();
		this.prcMgr  = kmmFile.getPrcMgr();
	}

	// ---------------------------------------------------------------

	@Override
	public int getNofEntriesInstitutions() {
		return instMgr.getNofEntriesInstitutionMap();
	}

	@Override
	public int getNofEntriesAccounts() {
		return acctMgr.getNofEntriesAccountMap();
	}

	@Override
	public int getNofEntriesTransactions() {
		return trxMgr.getNofEntriesTransactionMap();
	}

	@Override
	public int getNofEntriesTransactionSplits() {
		return trxMgr.getNofEntriesTransactionSplitMap();
	}

	// ----------------------------

	@Override
	public int getNofEntriesPayees() {
		return pyeMgr.getNofEntriesPayeeMap();
	}

	@Override
	public int getNofEntriesTags() {
		return tagMgr.getNofEntriesTagMap();
	}

	// ----------------------------

	@Override
	public int getNofEntriesSecurities() {
		return secMgr.getNofEntriesSecurityMap();
	}

	@Override
	public int getNofEntriesCurrencies() {
		return currMgr.getNofEntriesCurrencyMap();
	}

	// ----------------------------

	@Override
	public int getNofEntriesPricePairs() {
		return prcMgr.getNofEntriesPricePairMap();
	}

	@Override
	public int getNofEntriesPrices() {
		return prcMgr.getNofEntriesPriceMap();
	}

}
