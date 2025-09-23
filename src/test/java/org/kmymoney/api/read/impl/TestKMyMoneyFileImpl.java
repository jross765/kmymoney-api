package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.impl.aux.KMMFileStats;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyFileImpl {
	private KMyMoneyFileImpl kmmFile = null;
	private KMyMoneyFileImpl kmmFile2 = null;

	private KMMFileStats kmmFileStats = null;
	private KMMFileStats kmmFileStats2 = null;
	
	// ::MAGIC
	private final String DUMP_OUT_FILE_NAME = "/home/xxx/Programme/finanzen/kmymoney/test/out/dump.txt";
	private final String DUMP_REF_FILE_NAME = "/home/xxx/Programme/finanzen/kmymoney/test/ref/dump.txt";

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyFileImpl.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.GCSH_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		InputStream kmmFileStream = null;
		InputStream kmmFileStream2 = null;
		try {
			kmmFileStream = classLoader.getResourceAsStream(ConstTest.KMM_FILENAME);
			kmmFileStream2 = classLoader.getResourceAsStream(ConstTest.KMM_FILENAME);
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmFile = new KMyMoneyFileImpl(kmmFileStream);
			kmmFile2 = new KMyMoneyFileImpl(kmmFileStream2);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney file");
			exc.printStackTrace();
		}

		kmmFileStats = new KMMFileStats(kmmFile);
		kmmFileStats2 = new KMMFileStats(kmmFile2);
	}

	// -----------------------------------------------------------------

	@Test
	public void test01() throws Exception {
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));
	}

	@Test
	public void test02() throws Exception {
		assertEquals(ConstTest.Stats.NOF_TRX, kmmFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX, kmmFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));
	}

	@Test
	public void test03() throws Exception {
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW));
		// This one is an exception:
		// assertEquals(ConstTest.Stats.NOF_TRX_SPLT,
		// kmmFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.COUNTER));
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT,
				kmmFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.CACHE));
	}

	@Test
	public void test04() throws Exception {
		assertEquals(ConstTest.Stats.NOF_PYE, kmmFileStats.getNofEntriesPayees(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PYE, kmmFileStats.getNofEntriesPayees(KMMFileStats.Type.CACHE));
	}

	@Test
	public void test05() throws Exception {
		assertEquals(ConstTest.Stats.NOF_TAG, kmmFileStats.getNofEntriesTags(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TAG, kmmFileStats.getNofEntriesTags(KMMFileStats.Type.CACHE));
	}

	@Test
	public void test06() throws Exception {
		assertEquals(ConstTest.Stats.NOF_SEC, kmmFileStats.getNofEntriesSecurities(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_SEC, kmmFileStats.getNofEntriesSecurities(KMMFileStats.Type.CACHE));
	}

	@Test
	public void test07() throws Exception {
		assertEquals(ConstTest.Stats.NOF_CURR, kmmFileStats.getNofEntriesCurrencies(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_CURR, kmmFileStats.getNofEntriesCurrencies(KMMFileStats.Type.CACHE));
	}

	@Test
	public void test08() throws Exception {
		assertEquals(ConstTest.Stats.NOF_PRC, kmmFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		// This one is an exception:
		// assertEquals(ConstTest.Stats.NOF_PRC,
		// kmmFileStats.getNofEntriesPrices(KMMFileStats.Type.COUNTER));
		assertEquals(ConstTest.Stats.NOF_PRC, kmmFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));
	}

	// ---------------------------------------------------------------
	// The following test cases seem trivial, obvious, superfluous. 
	// I am not so sure about that. I cannot exactly provide a reason
	// right now, but my gut and my experience tell me that these tests
	// are not that trivial and redundant as they seem to be.

	@Test
	public void test09() throws Exception {
		assertEquals(kmmFile.toString(), kmmFile2.toString());
		// Does not work:
		// assertEquals(kmmFileStats, kmmFileStats2);
		// Works:
		assertEquals(true, kmmFileStats2.equals(kmmFileStats));
	}

	@Test
	public void test24() throws Exception {
		assertEquals(kmmFile.getAccounts().toString(), kmmFile2.getAccounts().toString());
		assertEquals(kmmFile.getTransactions().toString(), kmmFile2.getTransactions().toString());
		assertEquals(kmmFile.getTransactionSplits().toString(), kmmFile2.getTransactionSplits().toString());
		assertEquals(kmmFile.getPayees().toString(), kmmFile2.getPayees().toString());
		assertEquals(kmmFile.getSecurities().toString(), kmmFile2.getSecurities().toString());
		assertEquals(kmmFile.getCurrencies().toString(), kmmFile2.getCurrencies().toString());
		assertEquals(kmmFile.getPricePairs().toString(), kmmFile2.getPricePairs().toString());
		assertEquals(kmmFile.getPrices().toString(), kmmFile2.getPrices().toString());
	}

	// ---------------------------------------------------------------

	/*
	@Test
	public void test30() throws Exception {
		PrintStream dumpOutStream = new PrintStream(DUMP_OUT_FILE_NAME);
		kmmFile.dump(dumpOutStream);
		dumpOutStream.close();
		
		File dumpOutFile = new File(DUMP_OUT_FILE_NAME);
		File dumpRefFile = new File(DUMP_REF_FILE_NAME);
		assertTrue(FileUtils.contentEquals(dumpOutFile, dumpRefFile));
	}
	*/

}
