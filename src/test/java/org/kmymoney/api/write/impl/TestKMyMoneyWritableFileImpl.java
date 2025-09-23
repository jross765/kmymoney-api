package org.kmymoney.api.write.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyAccountImpl;
import org.kmymoney.api.read.impl.TestKMyMoneySecurityImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyTransactionImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyWritableFileImpl {
	
	private static final KMMComplAcctID ACCT_1_ID = TestKMyMoneyAccountImpl.ACCT_1_ID;
	private static final KMMTrxID TRX_1_ID        = TestKMyMoneyTransactionImpl.TRX_1_ID;
	private static final KMMSecID SEC_2_ID        = TestKMyMoneySecurityImpl.SEC_2_ID;
	
	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile  = null;
	private KMyMoneyWritableFileImpl kmmOutFile = null;
	private KMyMoneyFileImpl         kmmROFile = null;

	private KMMFileStats kmmInFileStats  = null;
	private KMMFileStats kmmOutFileStats = null;

	// https://stackoverflow.com/questions/11884141/deleting-file-and-directory-in-junit
	@SuppressWarnings("exports")
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyWritableFileImpl.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.KMM_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		InputStream kmmInFileStream = null;
		try {
			kmmInFileStream = classLoader.getResourceAsStream(ConstTest.KMM_FILENAME_IN);
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			throw exc;
		}

		try {
			kmmInFile = new KMyMoneyWritableFileImpl(kmmInFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney in-file");
			throw exc;
		}

		kmmInFileStats = new KMMFileStats(kmmInFile);

		try {
			InputStream kmmInFileStream2 = classLoader.getResourceAsStream(ConstTest.KMM_FILENAME_IN);
			kmmROFile = new KMyMoneyFileImpl(kmmInFileStream2);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney read-only file");
			throw exc;
		}
	}

	// -----------------------------------------------------------------
	// PART 1: Read existing objects as modifiable ones
	// (and see whether they are fully symmetrical to their read-only
	// counterparts)
	// -----------------------------------------------------------------
	// Cf. TestKMyMoneyFile.test01/02
	//
	// Check whether the KMyMoneyWritableFile objects returned by
	// KMyMoneyWritableFileImpl.getWritableFileByID() are actually
	// complete (as complete as returned be KMyMoneyFileImpl.getFileByID().

	@Test
	public void test01_01() throws Exception {
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));
	}

	@Test
	public void test01_02() throws Exception {
		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));
	}

	@Test
	public void test01_03() throws Exception {
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT,
				kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW));
		// This one is an exception:
		// assertEquals(ConstTest.Stats.NOF_TRX_SPLT,
		// kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.COUNTER));
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT,
				kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.CACHE));
	}

	// ------------------------------
	@Test
	public void test01_04() throws Exception {
		assertEquals(ConstTest.Stats.NOF_PYE, kmmInFileStats.getNofEntriesPayees(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PYE, kmmInFileStats.getNofEntriesPayees(KMMFileStats.Type.CACHE));
	}

	// ------------------------------

	@Test
	public void test01_05() throws Exception {
		assertEquals(ConstTest.Stats.NOF_SEC, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_SEC, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.CACHE));
	}

	@Test
	public void test01_06() throws Exception {
		assertEquals(ConstTest.Stats.NOF_PRCPR, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRCPR, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.CACHE));
	}

	@Test
	public void test01_07() throws Exception {
		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		// n/a:
		// assertEquals(ConstTest.Stats.NOF_PRC,
		// kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.COUNTER));
		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritableFile objects returned by
	// can actually be modified -- both in memory and persisted in file.

	// ::TODO

	// -----------------------------------------------------------------
	// PART 3: Create new objects
	// -----------------------------------------------------------------

	// ::TODO

	// -----------------------------------------------------------------
	// PART 4: Idempotency
	// 
	// Check that a KMyMoney file which has been loaded by the lib and
	// written into another file without having changed anything produces
	// exactly the same output (i.e., can be loaded into another KMyMoney file
	// object, and both produce the same objects). "Equal" or "the same",
	// in this specific context, does not necessarily means "low-level-equal",
	// i.e. both files are the same byte-for-byte, but rather "high-level-equal",
	// i.e. they can be parsed into another structure in memory, and both
	// have identical contents.
	// 
	// And no, this test is not trivial, absolutely not.
	// -----------------------------------------------------------------

	@Test
	public void test04_1() throws Exception {
		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		kmmOutFile = new KMyMoneyWritableFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(true, outFile.exists());
		assertEquals(false, isGZipped(outFile));

		test04_1_check_1_xmllint(outFile);
		test04_1_check_2();
		test04_1_check_3();
	}

	// CAUTION: Not platform-independent!
	// Tool "xmllint" must be installed and in path
	private void test04_1_check_1_xmllint(File outFile) throws Exception {
		// Check if generated document is valid
		ProcessBuilder bld = new ProcessBuilder("xmllint", outFile.getAbsolutePath());
		Process prc = bld.start();

		if ( prc.waitFor() == 0 ) {
			assertEquals(0, 0);
		} else {
			assertEquals(0, 1);
		}
	}

	private void test04_1_check_2() {
		// Does not work:
		// assertEquals(kmmFileStats, kmmFileStats2);
		// Works:
		assertEquals(true, kmmInFileStats.equals(kmmOutFileStats));
	}

	private void test04_1_check_3() {
		assertEquals(kmmInFile.getAccounts().toString(), kmmOutFile.getAccounts().toString());
		assertEquals(kmmInFile.getTransactions().toString(), kmmOutFile.getTransactions().toString());
		assertEquals(kmmInFile.getTransactionSplits().toString(), kmmOutFile.getTransactionSplits().toString());
		assertEquals(kmmInFile.getPayees().toString(), kmmOutFile.getPayees().toString());
		assertEquals(kmmInFile.getSecurities().toString(), kmmOutFile.getSecurities().toString());
		assertEquals(kmmInFile.getCurrencies().toString(), kmmOutFile.getCurrencies().toString());
		assertEquals(kmmInFile.getPricePairs().toString(), kmmOutFile.getPricePairs().toString());
		assertEquals(kmmInFile.getPrices().toString(), kmmOutFile.getPrices().toString());
	}

	// Same as test04_1, but with compressed file
	@Test
	public void test04_2() throws Exception {
		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile, KMyMoneyWritableFile.CompressMode.COMPRESS);

		kmmOutFile = new KMyMoneyWritableFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(true, outFile.exists());
		assertEquals(true, isGZipped(outFile));

		test04_1_check_1_xmllint(outFile);
		test04_1_check_2();
		test04_1_check_3();
	}

	// -----------------------------------------------------------------
	// PART 5: Symmetry of read-only objects gotten from a) GnucashFile
	// and b) KMyMoneyWritableFile (esp. sub-objects)
	
	@Test
	public void test05_1() throws Exception {
		// CAUTION: This test case is not trivial! It checks for a subtle
		// bug that long went unnoticed. 
		// Notice that the first line calls the *read-only*-method of the *writable* 
		// file object.
		// Cf. comments in org.gnucash.api.*write*.FileAccountManager.createAccount()
		KMyMoneyAccount acct11 = kmmInFile.getAccountByID(ACCT_1_ID);
		KMyMoneyAccount acct12 = kmmROFile.getAccountByID(ACCT_1_ID);
		assertNotEquals(null, acct11);
		assertNotEquals(null, acct12);
		
		// The first comparison is not problematic, it just ensures that the
		// two account objects really belong to the same account. 
		// The following ones are the real test: They check the correct handling 
		// of transactions and trx-splits in KMyMoney*Writable*Account.
		assertEquals(acct11.getQualifiedName(), acct12.getQualifiedName());
		assertTrue(acct11.getTransactions().size() > 0);
		assertEquals(acct11.getTransactions().size(), acct12.getTransactions().size());
		assertTrue(acct11.getBalance().getBigDecimal().doubleValue() > 0);
		assertEquals(acct11.getBalance(), acct12.getBalance());
	}

	@Test
	public void test05_2() throws Exception {
		// Analogous to test05_1, but with transactions
		KMyMoneyTransaction trx11 = kmmInFile.getTransactionByID(TRX_1_ID);
		KMyMoneyTransaction trx12 = kmmROFile.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx11);
		assertNotEquals(null, trx12);
		
		// splits
		assertEquals(trx11.getID(), trx12.getID());
		assertTrue(trx11.getSplits().size() > 0);
		assertEquals(trx11.getSplits().size(), trx12.getSplits().size());
		assertEquals(trx11.getBalance(), trx12.getBalance());
		assertEquals(trx11.getSplits().get(0).getValue(), trx12.getSplits().get(0).getValue());
		assertEquals(trx11.getSplits().get(1).getValue(), trx12.getSplits().get(1).getValue());
	}

	@Test
	public void test05_4() throws Exception {
		// Analogous to test05_1, but with commodities
		KMyMoneySecurity cmdty11 = kmmInFile.getSecurityByID(SEC_2_ID);
		KMyMoneySecurity cmdty12 = kmmROFile.getSecurityByID(SEC_2_ID);
		assertNotEquals(null, cmdty11);
		assertNotEquals(null, cmdty12);
		
		// quotes
		assertEquals(cmdty11.getName(), cmdty12.getName());
		assertTrue(cmdty11.getQuotes().size() > 0);
		assertEquals(cmdty11.getQuotes().size(), cmdty12.getQuotes().size());
	}

	// ---------------------------------------------------------------
	
	// https://stackoverflow.com/questions/30507653/how-to-check-whether-file-is-gzip-or-not-in-java
	public boolean isGZipped(File f) {
		int magic = 0;
		try {
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
			raf.close();
		} catch (Throwable e) {
			e.printStackTrace(System.err);
		}
		return magic == GZIPInputStream.GZIP_MAGIC;
	}
}
