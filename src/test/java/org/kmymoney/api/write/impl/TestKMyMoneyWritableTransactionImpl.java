package org.kmymoney.api.write.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyAccountImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyTransactionImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TestKMyMoneyWritableTransactionImpl {
	private static final KMMTrxID TRX_1_ID = TestKMyMoneyTransactionImpl.TRX_1_ID;
	private static final KMMTrxID TRX_2_ID = TestKMyMoneyTransactionImpl.TRX_2_ID;

	private static final KMMComplAcctID ACCT_1_ID = TestKMyMoneyAccountImpl.ACCT_1_ID;
	private static final KMMAcctID ACCT_20_ID = new KMMAcctID("A000005"); // Asset::Barvermögen::Spar RaiBa

	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

	private KMMFileStats kmmInFileStats = null;
	private KMMFileStats kmmOutFileStats = null;

	private KMMTrxID newTrxID = null;

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
		return new JUnit4TestAdapter(TestKMyMoneyWritableTransactionImpl.class);
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
	public void test01() throws Exception {
		KMyMoneyWritableTransaction trx = kmmInFile.getWritableTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		assertEquals(TRX_1_ID, trx.getID());
		assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("", trx.getMemo());
		assertEquals("2023-01-01", trx.getDatePosted().toString());
		assertEquals("2023-11-03", trx.getDateEntered().toString());

		assertEquals(2, trx.getSplitsCount());
		assertEquals("S0001", trx.getSplits().get(0).getID().toString());
		assertEquals("S0002", trx.getSplits().get(1).getID().toString());
	}

	@Test
	public void test02() throws Exception {
		KMyMoneyWritableTransaction trx = kmmInFile.getWritableTransactionByID(TRX_2_ID);
		assertNotEquals(null, trx);

		assertEquals(TRX_2_ID, trx.getID());
		assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("", trx.getMemo());
		assertEquals("2023-01-03", trx.getDatePosted().toString());
		assertEquals("2023-10-14", trx.getDateEntered().toString());

		assertEquals(2, trx.getSplitsCount());
		assertEquals("S0001", trx.getSplits().get(0).getID().toString());
		assertEquals("S0002", trx.getSplits().get(1).getID().toString());
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritableTransaction objects returned by
	// can actually be modified -- both in memory and persisted in file.

	@Test
	public void test02_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));

		KMyMoneyWritableTransaction trx = kmmInFile.getWritableTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		assertEquals(TRX_1_ID, trx.getID());

		// ----------------------------
		// Modify the object

		trx.setMemo("Super dividend");
		trx.setDatePosted(LocalDate.of(1970, 1, 1));

		// ::TODO not possible yet
		// trx.getSplitByID("7abf90fe15124254ac3eb7ec33f798e7").remove()
		// trx.getSplitByID("7abf90fe15124254ac3eb7ec33f798e7").setXYZ()

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(trx);

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

	private void test02_1_check_memory(KMyMoneyWritableTransaction trx) throws Exception {
		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));

		assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE); // unchanged
		assertEquals("Super dividend", trx.getMemo()); // changed
		assertEquals("1970-01-01", trx.getDatePosted().toString()); // changed
		assertEquals("2023-11-03", trx.getDateEntered().toString()); // unchanged

		assertEquals(2, trx.getSplitsCount()); // unchanged
		assertEquals("S0001", trx.getSplits().get(0).getID().toString()); // unchanged
		assertEquals("S0002", trx.getSplits().get(1).getID().toString()); // unchanged
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_TRX, kmmOutFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX, kmmOutFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));

		KMyMoneyTransaction trx = kmmOutFile.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		assertEquals(TRX_1_ID, trx.getID());
		assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE); // unchanged
		assertEquals("Super dividend", trx.getMemo()); // changed
		assertEquals("1970-01-01", trx.getDatePosted().toString()); // changed
		assertEquals("2023-11-03", trx.getDateEntered().toString()); // unchanged

		assertEquals(2, trx.getSplitsCount()); // unchanged
		assertEquals("S0001", trx.getSplits().get(0).getID().toString()); // unchanged
		assertEquals("S0002", trx.getSplits().get(1).getID().toString()); // unchanged
	}

	// -----------------------------------------------------------------
	// PART 3: Create new objects
	// -----------------------------------------------------------------

	// ------------------------------
	// PART 3.1: High-Level
	// ------------------------------

	@Test
	public void test03_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));

		// ----------------------------
		// Bare naked object

		KMyMoneyWritableTransaction trx = kmmInFile.createWritableTransaction();
		assertNotEquals(null, trx);
		newTrxID = trx.getID();
		assertEquals(true, newTrxID.isSet());

		// ----------------------------
		// Modify the object

		// trx.setType(KMyMoneyTransaction.Type.PAYMENT);
		trx.setMemo("Chattanooga Choo-Choo");
		trx.setCurrencyID("EUR");
		trx.setDateEntered(LocalDate.of(2023, 12, 11));
		trx.setDatePosted(LocalDate.of(2023, 5, 20));

		KMyMoneyAccount acct1 = kmmInFile.getAccountByID(ACCT_1_ID);
		KMyMoneyAccount acct2 = kmmInFile.getAccountByID(ACCT_20_ID);

		KMyMoneyWritableTransactionSplit splt1 = trx.createWritableSplit(acct1);
		splt1.setAction(KMyMoneyTransactionSplit.Action.WITHDRAWAL);
		splt1.setShares(new FixedPointNumber(100).copy().negate());
		splt1.setValue(new FixedPointNumber(100).copy().negate());
		splt1.setMemo("Generated by TestKMyMoneyWritableTransactionImpl.test03_1 (1)");

		KMyMoneyWritableTransactionSplit splt2 = trx.createWritableSplit(acct2);
		splt2.setAction(KMyMoneyTransactionSplit.Action.DEPOSIT);
		splt2.setShares(new FixedPointNumber(100));
		splt2.setValue(new FixedPointNumber(100));
		splt2.setMemo("Generated by TestKMyMoneyWritableTransactionImpl.test03_1 (2)");

		// ----------------------------
		// Check whether the object has actually been modified
		// (in memory, not in the file yet).

		test03_1_check_memory(trx);

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

		test03_1_check_persisted(outFile);
	}

	// ---------------------------------------------------------------

	private void test03_1_check_memory(KMyMoneyWritableTransaction trx) throws Exception {
		assertEquals(ConstTest.Stats.NOF_TRX + 1, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		// CAUTION: The counter has not been updated yet.
		// This is on purpose
		// ::TODO
		// assertEquals(ConstTest.Stats.NOF_TRX,
		// kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.COUNTER));
		assertEquals(ConstTest.Stats.NOF_TRX + 1, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));

		// assertEquals(KMyMoneyTransaction.Type.PAYMENT, trx.getType());
		assertEquals("Chattanooga Choo-Choo", trx.getMemo());
		assertEquals("CURRENCY:EUR", trx.getQualifSecCurrID().toString());
		assertEquals("2023-12-11", trx.getDateEntered().toString());
		assertEquals("2023-05-20", trx.getDatePosted().toString());

		// ---

		assertEquals(0, trx.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);

		// ---

		assertEquals(2, trx.getSplits().size());
		assertEquals(trx.getSplits().size(), trx.getSplitsCount());

		KMyMoneyTransactionSplit splt1 = (KMyMoneyTransactionSplit) trx.getSplits().toArray()[0];
		KMyMoneyTransactionSplit splt2 = (KMyMoneyTransactionSplit) trx.getSplits().toArray()[1];

		assertEquals(ACCT_1_ID, splt1.getAccountID());
		assertEquals(KMyMoneyTransactionSplit.Action.WITHDRAWAL, splt1.getAction());
		assertEquals(new FixedPointNumber(100).copy().negate(), splt1.getShares());
		assertEquals(new FixedPointNumber(100).copy().negate(), splt1.getValue());
		assertEquals("Generated by TestKMyMoneyWritableTransactionImpl.test03_1 (1)", splt1.getMemo());

		assertEquals(ACCT_20_ID.toString(), splt2.getAccountID().toString());
		assertEquals(KMyMoneyTransactionSplit.Action.DEPOSIT, splt2.getAction());
		assertEquals(new FixedPointNumber(100), splt2.getShares());
		assertEquals(new FixedPointNumber(100), splt2.getValue());
		assertEquals("Generated by TestKMyMoneyWritableTransactionImpl.test03_1 (2)", splt2.getMemo());
	}

	private void test03_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		// Here, all 3 stats variants must have been updated
		assertEquals(ConstTest.Stats.NOF_TRX + 1, kmmOutFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX + 1, kmmOutFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));

		KMyMoneyTransaction trx = kmmOutFile.getTransactionByID(newTrxID);
		assertNotEquals(null, trx);

		// assertEquals(KMyMoneyTransaction.Type.PAYMENT, trx.getType());
		assertEquals("Chattanooga Choo-Choo", trx.getMemo());
		assertEquals("CURRENCY:EUR", trx.getQualifSecCurrID().toString());
		assertEquals("2023-12-11", trx.getDateEntered().toString());
		assertEquals("2023-05-20", trx.getDatePosted().toString());

		// ---

		assertEquals(0, trx.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);

		// ---

		assertEquals(2, trx.getSplits().size());
		assertEquals(trx.getSplits().size(), trx.getSplitsCount());

		KMyMoneyTransactionSplit splt1 = (KMyMoneyTransactionSplit) trx.getSplits().toArray()[0];
		KMyMoneyTransactionSplit splt2 = (KMyMoneyTransactionSplit) trx.getSplits().toArray()[1];

		assertEquals(ACCT_1_ID, splt1.getAccountID());
		assertEquals(KMyMoneyTransactionSplit.Action.WITHDRAWAL, splt1.getAction());
		assertEquals(new FixedPointNumber(100).copy().negate(), splt1.getShares());
		assertEquals(new FixedPointNumber(100).copy().negate(), splt1.getValue());
		assertEquals("Generated by TestKMyMoneyWritableTransactionImpl.test03_1 (1)", splt1.getMemo());

		assertEquals(ACCT_20_ID.toString(), splt2.getAccountID().toString());
		assertEquals(KMyMoneyTransactionSplit.Action.DEPOSIT, splt2.getAction());
		assertEquals(new FixedPointNumber(100), splt2.getShares());
		assertEquals(new FixedPointNumber(100), splt2.getValue());
		assertEquals("Generated by TestKMyMoneyWritableTransactionImpl.test03_1 (2)", splt2.getMemo());
	}

	// ------------------------------
	// PART 3.2: Low-Level
	// ------------------------------

	// ::TODO

	// -----------------------------------------------------------------
	// PART 4: Delete objects
	// -----------------------------------------------------------------

	// ------------------------------
	// PART 4.1: High-Level
	// ------------------------------

	@Test
	public void test04_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));


		// ----------------------------
		// Delete the object

		// Variant 1
		KMyMoneyWritableTransaction trx1 = kmmInFile.getWritableTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx1);
		kmmInFile.removeTransaction(trx1);

		// Variant 2
		KMyMoneyWritableTransaction trx2 = kmmInFile.getWritableTransactionByID(TRX_2_ID);
		trx2.remove();

		// ----------------------------
		// Check whether the objects have actually been deleted
		// (in memory, not in the file yet).

		test04_1_check_memory(trx1, trx2);

		// ----------------------------
		// Now, check whether the deletions have been written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test04_1_check_persisted(outFile);
	}

	// ---------------------------------------------------------------

	private void test04_1_check_memory(KMyMoneyWritableTransaction trx1,
									   KMyMoneyWritableTransaction trx2) throws Exception {
		assertEquals(ConstTest.Stats.NOF_TRX - 2, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX - 2, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));

		// ---
		// First transaction:
		
		// CAUTION / ::TODO
		// Old Object still exists and is unchanged
		// Exception: no splits any more
		// Don't know what to do about this oddity right now,
		// but it needs to be addressed at some point.
		assertEquals(0.0, trx1.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE); // unchanged
		assertEquals("", trx1.getMemo()); // unchanged
		assertEquals("2023-01-01", trx1.getDatePosted().toString()); // unchanged
		assertEquals("2023-11-03", trx1.getDateEntered().toString()); // unchanged
		assertEquals(0, trx1.getSplitsCount()); // changed
		
		// However, the transaction cannot newly be instantiated any more,
		// just as you would expect.
		try {
			KMyMoneyWritableTransaction trx1Now = kmmInFile.getWritableTransactionByID(TRX_1_ID);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		
		// ---
		// Second transaction, same as above:
		
		// CAUTION / ::TODO
		// Cf. above.
		assertEquals(TRX_2_ID, trx2.getID()); // unchanged
		assertEquals(0.0, trx2.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE); // unchanged
		assertEquals("", trx2.getMemo()); // unchanged
		assertEquals("2023-01-03", trx2.getDatePosted().toString()); // unchanged
		assertEquals("2023-10-14", trx2.getDateEntered().toString()); // unchanged
		assertEquals(0, trx2.getSplitsCount()); // changed
		
		// Cf. above.
		try {
			KMyMoneyWritableTransaction trx2Now = kmmInFile.getWritableTransactionByID(TRX_2_ID);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
	}

	private void test04_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_TRX - 2, kmmOutFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX - 2, kmmOutFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));

		// ---
		// First transaction:
		
		// The transaction does not exist any more, just as you would expect.
		// However, no exception is thrown, as opposed to test04_1_check_memory()
		KMyMoneyTransaction trx1 = kmmOutFile.getTransactionByID(TRX_1_ID);
		assertEquals(null, trx1); // sic

		// ---
		// Second transaction, same as above:
		
		// Cf. above
		KMyMoneyTransaction trx2 = kmmOutFile.getTransactionByID(TRX_2_ID);
		assertEquals(null, trx2); // sic
	}

	// ------------------------------
	// PART 4.2: Low-Level
	// ------------------------------

	// ::TODO

}
