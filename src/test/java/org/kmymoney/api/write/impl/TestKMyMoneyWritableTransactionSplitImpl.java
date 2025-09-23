package org.kmymoney.api.write.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyAccountImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyTransactionSplitImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TestKMyMoneyWritableTransactionSplitImpl {
	public static final KMMComplAcctID ACCT_1_ID = TestKMyMoneyAccountImpl.ACCT_1_ID;
	public static final KMMComplAcctID ACCT_2_ID = TestKMyMoneyAccountImpl.ACCT_2_ID;
	public static final KMMComplAcctID ACCT_4_ID = TestKMyMoneyAccountImpl.ACCT_4_ID;
	public static final KMMComplAcctID ACCT_8_ID = TestKMyMoneyAccountImpl.ACCT_8_ID;

	public static final KMMTrxID TRX_01_ID = TestKMyMoneyTransactionSplitImpl.TRX_01_ID;
	public static final KMMTrxID TRX_02_ID = TestKMyMoneyTransactionSplitImpl.TRX_02_ID;
	public static final KMMTrxID TRX_04_ID = TestKMyMoneyTransactionSplitImpl.TRX_04_ID;
	public static final KMMTrxID TRX_06_ID = TestKMyMoneyTransactionSplitImpl.TRX_06_ID;
	public static final KMMTrxID TRX_07_ID = TestKMyMoneyTransactionSplitImpl.TRX_07_ID;
	public static final KMMTrxID TRX_08_ID = TestKMyMoneyTransactionSplitImpl.TRX_08_ID;
	public static final KMMTrxID TRX_10_ID = TestKMyMoneyTransactionSplitImpl.TRX_10_ID;
	public static final KMMTrxID TRX_15_ID = TestKMyMoneyTransactionSplitImpl.TRX_15_ID;
	public static final KMMTrxID TRX_18_ID = TestKMyMoneyTransactionSplitImpl.TRX_18_ID;

	public static final KMMQualifSpltID TRXSPLT_01_ID = TestKMyMoneyTransactionSplitImpl.TRXSPLT_01_ID;
	public static final KMMQualifSpltID TRXSPLT_02_ID = TestKMyMoneyTransactionSplitImpl.TRXSPLT_02_ID;
	public static final KMMQualifSpltID TRXSPLT_04_ID = TestKMyMoneyTransactionSplitImpl.TRXSPLT_04_ID;
	public static final KMMQualifSpltID TRXSPLT_06_ID = TestKMyMoneyTransactionSplitImpl.TRXSPLT_06_ID;
	public static final KMMQualifSpltID TRXSPLT_08_ID = TestKMyMoneyTransactionSplitImpl.TRXSPLT_08_ID;
	public static final KMMQualifSpltID TRXSPLT_10_ID = TestKMyMoneyTransactionSplitImpl.TRXSPLT_10_ID;
	public static final KMMQualifSpltID TRXSPLT_15_ID = TestKMyMoneyTransactionSplitImpl.TRXSPLT_15_ID;
	public static final KMMQualifSpltID TRXSPLT_18_ID = TestKMyMoneyTransactionSplitImpl.TRXSPLT_18_ID;

	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

	private KMMFileStats kmmInFileStats = null;
	private KMMFileStats kmmOutFileStats = null;

	private KMMID newTrxID = null;

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
		return new JUnit4TestAdapter(TestKMyMoneyWritableTransactionSplitImpl.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.kmm_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		InputStream kmmInFileStream = null;
		try {
			kmmInFileStream = classLoader.getResourceAsStream(ConstTest.KMM_FILENAME_IN);
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmInFile = new KMyMoneyWritableFileImpl(kmmInFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney in-file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------
	// PART 1: Read existing objects as modifiable ones
	// (and see whether they are fully symmetrical to their read-only
	// counterparts)
	// -----------------------------------------------------------------
	// Cf. TestKMyMoneyTransaction.test01/02
	//
	// Check whether the KMyMoneyWritableTransaction objects returned by
	// KMyMoneyWritableFileImpl.getWritableTransactionByID() are actually
	// complete (as complete as returned be KMyMoneyFileImpl.getTransactionByID().

	@Test
	public void test01_1() throws Exception {
		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(TRXSPLT_01_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_01_ID, splt.getQualifID());
		assertEquals(TRX_01_ID, splt.getTransactionID());
		assertEquals(ACCT_1_ID, splt.getAccountID());
		assertEquals(null, splt.getAction());
		assertEquals(10000.00, splt.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("10.000,00 €", splt.getValueFormatted()); // ::TODO: locale-specific!
		assertEquals(10000.00, splt.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		// ::TODO: Here, it works correctly, as opposed to 
		// TestKMyMoneyTransactionSplitImpl
		assertEquals("10.000,00 €", splt.getSharesFormatted());
		assertEquals("", splt.getMemo());
//		assertEquals(null, splt.getUserDefinedAttributeKeys());
	}

	@Test
	public void test01_2() throws Exception {
		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(TRXSPLT_18_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_18_ID, splt.getQualifID());
		assertEquals(TRX_18_ID, splt.getTransactionID());
		assertEquals(ACCT_8_ID, splt.getAccountID());
		assertEquals(KMyMoneyTransactionSplit.Action.BUY_SHARES, splt.getAction());
		assertEquals(1800.00, splt.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("1.800,00 €", splt.getValueFormatted()); // ::TODO: locale-specific!
		assertEquals(15.00, splt.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		// ::TODO: The next two: That's not exactly what we want...
		assertEquals("15 SECURITY:E000001", splt.getSharesFormatted());
		assertEquals("", splt.getMemo());
//		assertEquals(null, splt.getUserDefinedAttributeKeys());
	}

	@Test
	public void test01_3_1() throws Exception {
		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(TRXSPLT_02_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_02_ID, splt.getQualifID());
		assertEquals("0123", splt.getNumber());
	}

	@Test
	public void test01_3_2() throws Exception {
		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(TRXSPLT_04_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_04_ID, splt.getQualifID());
		assertEquals("x762d", splt.getNumber());
	}

	@Test
	public void test01_3_3() throws Exception {
		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(TRXSPLT_06_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_06_ID, splt.getQualifID());
		assertEquals("/$@d", splt.getNumber());
	}

	@Test
	public void test01_3_4() throws Exception {
		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(TRXSPLT_08_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_08_ID, splt.getQualifID());
		assertEquals("ÄÖÜß", splt.getNumber());
	}

	@Test
	public void test01_3_5() throws Exception {
		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(TRXSPLT_10_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_10_ID, splt.getQualifID());
		assertEquals("<SPLIT>", splt.getNumber()); // sic, tried injection
	}

	@Test
	public void test01_3_6() throws Exception {
		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(TRXSPLT_15_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_15_ID, splt.getQualifID());
		assertEquals("5298", splt.getNumber()); // sic, tried injection
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritableTransaction objects returned by
	// can actually be modified -- both in memory and persisted in file.
	
	@Test
	public void test02_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW));
		// assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.COUNTER));
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.CACHE));

		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(TRXSPLT_01_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_01_ID, splt.getQualifID());

		// ----------------------------
		// Modify the object

		splt.setAccountID(ACCT_2_ID);
		splt.setValue(new FixedPointNumber("-123.45"));
		splt.setShares(new FixedPointNumber("-67.8901"));
		splt.setMemo("Alle meine Entchen");
		splt.setNumber("<%$@üb234/xy&>"); // very dirty stuff...

		// ::TODO not possible yet
		// trx.getSplitByID("7abf90fe15124254ac3eb7ec33f798e7").remove()
		// trx.getSplitByID("7abf90fe15124254ac3eb7ec33f798e7").setXYZ()

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(splt);

		// ----------------------------
		// Now, check whether the modified object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test02_1_check_persisted(outFile);
	}

	@Test
	public void test02_2() throws Exception {
		// ::TODO
	}
	
	// ---------------------------------------------------------------

	private void test02_1_check_memory(KMyMoneyWritableTransactionSplit splt) throws Exception {
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW));
		// assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.COUNTER));
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.CACHE));

		assertEquals(TRX_01_ID, splt.getTransactionID()); // unchanged
		assertEquals(ACCT_2_ID, splt.getAccountID()); // changed
		assertEquals(null, splt.getAction()); // unchanged
		assertEquals(-123.45, splt.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE); // changed
		assertEquals(-67.8901, splt.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE); // changed
		assertEquals("Alle meine Entchen", splt.getMemo()); // changed
		assertEquals("<%$@üb234/xy&>", splt.getNumber()); // changed
//		assertEquals(null, splt.getUserDefinedAttributeKeys()); // unchanged
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmOutFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW));
		// assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.COUNTER));
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmOutFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.CACHE));

		KMyMoneyTransactionSplit splt = kmmOutFile.getTransactionSplitByID(TRXSPLT_01_ID);
		assertNotEquals(null, splt);

		assertEquals(TRX_01_ID, splt.getTransactionID()); // unchanged
		assertEquals(ACCT_2_ID, splt.getAccountID()); // changed
		assertEquals(null, splt.getAction()); // unchanged
		assertEquals(-123.45, splt.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE); // changed
		assertEquals(-67.8901, splt.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE); // changed
		assertEquals("Alle meine Entchen", splt.getMemo()); // changed
		assertEquals("<%$@üb234/xy&>", splt.getNumber()); // changed
//		assertEquals(null, splt.getUserDefinedAttributeKeys()); // unchanged
	}

	// -----------------------------------------------------------------
	// PART 3: Create new objects
	// -----------------------------------------------------------------

	// ------------------------------
	// PART 3.1: High-Level
	// ------------------------------

	// ::TODO

	// ------------------------------
	// PART 3.2: Low-Level
	// ------------------------------

	// ::TODO

	// -----------------------------------------------------------------
	// PART 4: Create new objects
	// -----------------------------------------------------------------

	// ------------------------------
	// PART 4.1: High-Level
	// ------------------------------
	
	@Test
	public void test04_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW));
		// assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(kmmFileStats.Type.COUNTER));
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.CACHE));

		// Variant 1
		KMyMoneyWritableTransaction trx1 = kmmInFile.getWritableTransactionByID(TRX_01_ID);
		KMyMoneyWritableTransactionSplit splt1 = kmmInFile.getWritableTransactionSplitByID(TRXSPLT_01_ID);
		assertNotEquals(null, trx1);
		assertNotEquals(null, splt1);
		trx1.remove(splt1);

		// Variant 2
		KMyMoneyWritableTransaction trx2 = kmmInFile.getWritableTransactionByID(TRX_18_ID);
		KMyMoneyWritableTransactionSplit splt2 = kmmInFile.getWritableTransactionSplitByID(TRXSPLT_18_ID);
		assertNotEquals(null, trx2);
		assertNotEquals(null, splt2);
		splt2.remove();

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test04_1_check_memory(splt1, splt2, 
							  trx1, trx2);

		// ----------------------------
		// Now, check whether the modified object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test04_1_check_persisted(trx1, outFile);
	}
	
	// ---------------------------------------------------------------

	private void test04_1_check_memory(KMyMoneyWritableTransactionSplit splt1, 
									   KMyMoneyWritableTransactionSplit splt2, 
									   KMyMoneyTransaction trx1,
									   KMyMoneyTransaction trx2) throws Exception {
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT - 2, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW));
		// assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(kmmFileStats.Type.COUNTER));
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT - 2, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.CACHE));

		assertEquals(1, trx1.getSplitsCount());
		assertEquals("S0002", trx1.getSplits().get(0).getID().toString());

		KMyMoneyWritableTransaction trx1Now = kmmInFile.getWritableTransactionByID(TRX_01_ID);
		// CAUTION / ::TODO
		// Don't know what to do about this oddity right now,
		// but it needs to be addressed at some point.
		assertEquals(2, trx2.getSplitsCount()); // sic, 3, because it's not persisted yet
		assertNotEquals(null, trx1Now); // still there
		try {
			KMyMoneyWritableTransactionSplit splt1Now = kmmInFile.getWritableTransactionSplitByID(TRXSPLT_01_ID);
			assertEquals(1, 0);
		} catch ( NullPointerException exc ) {
			assertEquals(0, 0);
		}
	}

	private void test04_1_check_persisted(KMyMoneyTransaction trx, File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_TRX_SPLT - 2, kmmOutFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW));
		// assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(kmmFileStats.Type.COUNTER));
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT - 2, kmmOutFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.CACHE));

		KMyMoneyTransactionSplit splt = kmmOutFile.getTransactionSplitByID(TRXSPLT_01_ID);
		assertEquals(null, splt); // sic

		assertEquals(TRX_01_ID, trx.getID()); // unchanged
		assertEquals(1, trx.getSplitsCount());
		assertEquals("S0002", trx.getSplits().get(0).getID().toString());
	}

	// ------------------------------
	// PART 4.2: Low-Level
	// ------------------------------

	// ::TODO

}
