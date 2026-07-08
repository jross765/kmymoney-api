package org.kmymoney.api.write.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;

import org.apache.commons.numbers.fraction.BigFraction;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyAccountImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyTransactionImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyWritableTransactionImpl {

	private static final KMMTrxID TRX_1_ID = TestKMyMoneyTransactionImpl.TRX_1_ID;
	private static final KMMTrxID TRX_2_ID = TestKMyMoneyTransactionImpl.TRX_2_ID;

	public static final KMMQualifSpltID TRX_1_SPLT_1_ID = new KMMQualifSpltID(TRX_1_ID.toString(), "S0001");
	public static final KMMQualifSpltID TRX_1_SPLT_2_ID = new KMMQualifSpltID(TRX_1_ID.toString(), "S0002");
	public static final KMMQualifSpltID TRX_2_SPLT_1_ID = new KMMQualifSpltID(TRX_2_ID.toString(), "S0001");
	public static final KMMQualifSpltID TRX_2_SPLT_2_ID = new KMMQualifSpltID(TRX_2_ID.toString(), "S0002");

	private static final KMMAcctID TRX_1_SPLT_1_ACCT_ID = new KMMAcctID("A000004");
	private static final KMMAcctID TRX_1_SPLT_2_ACCT_ID = new KMMAcctID("A000053");
	private static final KMMAcctID TRX_2_SPLT_1_ACCT_ID = new KMMAcctID("A000004");
	private static final KMMAcctID TRX_2_SPLT_2_ACCT_ID = new KMMAcctID("A000050");

	private static final KMMPyeID TRX_1_SPLT_1_PYE_ID = new KMMPyeID("P000004");
	private static final KMMPyeID TRX_1_SPLT_2_PYE_ID = new KMMPyeID("P000004");
	private static final KMMPyeID TRX_2_SPLT_1_PYE_ID = new KMMPyeID("P000006");
	private static final KMMPyeID TRX_2_SPLT_2_PYE_ID = new KMMPyeID("P000006");

	private static final KMMComplAcctID ACCT_1_ID = TestKMyMoneyAccountImpl.ACCT_1_ID;
	private static final KMMAcctID ACCT_20_ID = new KMMAcctID("A000005"); // Asset::Barvermögen::Spar RaiBa
	
	// ----------------------------
	
	private static final int NOF_SPLT_ACCT11_PRE = 17;
	private static final int NOF_SPLT_ACCT12_PRE = 1;
	private static final int NOF_SPLT_PYE11_PRE  = 2;
	private static final int NOF_SPLT_PYE12_PRE  = 2;

	private static final int NOF_SPLT_ACCT21_PRE = 17;
	private static final int NOF_SPLT_ACCT22_PRE = 6;
	private static final int NOF_SPLT_PYE21_PRE  = 14;
	private static final int NOF_SPLT_PYE22_PRE  = 14;
	
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
		assertEquals(0, trx.getBalanceRat().getNumerator().longValue());
		assertEquals(1, trx.getBalanceRat().getDenominator().longValue());
		assertEquals("0,00 €", trx.getBalanceFormatted()); // ::TODO: locale-specific!
		
		assertEquals("", trx.getMemo());
		assertEquals("2023-01-01", trx.getDatePosted().toString());
		assertEquals("2023-01-01", trx.getDatePostedFormatted());
		assertEquals("2023-11-03", trx.getDateEntered().toString());
		assertEquals("2023-11-03", trx.getDateEnteredFormatted());

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
		assertEquals(0, trx.getBalanceRat().getNumerator().longValue());
		assertEquals(1, trx.getBalanceRat().getDenominator().longValue());
		assertEquals("0,00 €", trx.getBalanceFormatted()); // ::TODO: locale-specific!
		
		assertEquals("", trx.getMemo());
		assertEquals("2023-01-03", trx.getDatePosted().toString());
		assertEquals("2023-01-03", trx.getDatePostedFormatted());
		assertEquals("2023-10-14", trx.getDateEntered().toString());
		assertEquals("2023-10-14", trx.getDateEnteredFormatted());

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
		assertEquals(0,   trx.getBalanceRat().getNumerator().intValue());
		assertEquals(1,   trx.getBalanceRat().getDenominator().intValue());

		assertEquals("Super dividend", trx.getMemo()); // changed
		assertEquals("1970-01-01", trx.getDatePosted().toString()); // changed
		assertEquals("1970-01-01", trx.getDatePostedFormatted());
		assertEquals("2023-11-03", trx.getDateEntered().toString()); // unchanged
		assertEquals("2023-11-03", trx.getDateEnteredFormatted());

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
		assertEquals(0,   trx.getBalanceRat().getNumerator().intValue());
		assertEquals(1,   trx.getBalanceRat().getDenominator().intValue());

		assertEquals("Super dividend", trx.getMemo()); // changed
		assertEquals("1970-01-01", trx.getDatePosted().toString()); // changed
		assertEquals("1970-01-01", trx.getDatePostedFormatted());
		assertEquals("2023-11-03", trx.getDateEntered().toString()); // unchanged
		assertEquals("2023-11-03", trx.getDateEnteredFormatted());

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
		splt1.setShares(BigFraction.of(100).negate());
		splt1.setValue(BigFraction.of(100).negate());
		splt1.setMemo("Generated by TestKMyMoneyWritableTransactionImpl.test03_1 (1)");

		KMyMoneyWritableTransactionSplit splt2 = trx.createWritableSplit(acct2);
		splt2.setAction(KMyMoneyTransactionSplit.Action.DEPOSIT);
		splt2.setShares(BigFraction.of(100));
		splt2.setValue(BigFraction.of(100));
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
		assertEquals("2023-05-20", trx.getDatePosted().toString());
		assertEquals("2023-05-20", trx.getDatePostedFormatted());
		assertEquals("2023-12-11", trx.getDateEntered().toString());
		assertEquals("2023-12-11", trx.getDateEnteredFormatted());

		// ---

		assertEquals(0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0,   trx.getBalanceRat().getNumerator().intValue());
		assertEquals(1,   trx.getBalanceRat().getDenominator().intValue());

		// ---

		assertEquals(2, trx.getSplits().size());
		assertEquals(trx.getSplits().size(), trx.getSplitsCount());

		KMyMoneyTransactionSplit splt1 = (KMyMoneyTransactionSplit) trx.getSplits().toArray()[0];
		KMyMoneyTransactionSplit splt2 = (KMyMoneyTransactionSplit) trx.getSplits().toArray()[1];

		assertEquals(ACCT_1_ID.getStdID(), splt1.getAccountID());
		assertEquals(KMyMoneyTransactionSplit.Action.WITHDRAWAL, splt1.getAction());
		
		assertEquals(-100, splt1.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(-100, splt1.getSharesRat().getNumerator().intValue());
		assertEquals(1,    splt1.getSharesRat().getDenominator().intValue());

		assertEquals(-100, splt1.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(-100, splt1.getValueRat().getNumerator().intValue());
		assertEquals(1,    splt1.getValueRat().getDenominator().intValue());

		assertEquals("Generated by TestKMyMoneyWritableTransactionImpl.test03_1 (1)", splt1.getMemo());

		assertEquals(ACCT_20_ID.toString(), splt2.getAccountID().toString());
		assertEquals(KMyMoneyTransactionSplit.Action.DEPOSIT, splt2.getAction());
		
		assertEquals(100, splt2.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(100, splt2.getSharesRat().getNumerator().intValue());
		assertEquals(1,   splt2.getSharesRat().getDenominator().intValue());
		
		assertEquals(100, splt2.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(100, splt2.getValueRat().getNumerator().intValue());
		assertEquals(1,   splt2.getValueRat().getDenominator().intValue());
		
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
		assertEquals("2023-05-20", trx.getDatePosted().toString());
		assertEquals("2023-05-20", trx.getDatePostedFormatted());
		assertEquals("2023-12-11", trx.getDateEntered().toString());
		assertEquals("2023-12-11", trx.getDateEnteredFormatted());

		// ---

		assertEquals(0.0, trx.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0,   trx.getBalanceRat().getNumerator().intValue());
		assertEquals(1,   trx.getBalanceRat().getDenominator().intValue());

		// ---

		assertEquals(2, trx.getSplits().size());
		assertEquals(trx.getSplits().size(), trx.getSplitsCount());

		KMyMoneyTransactionSplit splt1 = (KMyMoneyTransactionSplit) trx.getSplits().toArray()[0];
		KMyMoneyTransactionSplit splt2 = (KMyMoneyTransactionSplit) trx.getSplits().toArray()[1];

		assertEquals(ACCT_1_ID.getStdID(), splt1.getAccountID());
		assertEquals(KMyMoneyTransactionSplit.Action.WITHDRAWAL, splt1.getAction());
		
		assertEquals(-100, splt1.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(-100, splt1.getSharesRat().getNumerator().intValue());
		assertEquals(1,    splt1.getSharesRat().getDenominator().intValue());

		assertEquals(-100, splt1.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(-100, splt1.getValueRat().getNumerator().intValue());
		assertEquals(1,    splt1.getValueRat().getDenominator().intValue());
		
		assertEquals("Generated by TestKMyMoneyWritableTransactionImpl.test03_1 (1)", splt1.getMemo());

		assertEquals(ACCT_20_ID.toString(), splt2.getAccountID().toString());
		assertEquals(KMyMoneyTransactionSplit.Action.DEPOSIT, splt2.getAction());
		
		assertEquals(100, splt2.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(100, splt2.getSharesRat().getNumerator().intValue());
		assertEquals(1,   splt2.getSharesRat().getDenominator().intValue());

		assertEquals(100, splt2.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(100, splt2.getValueRat().getNumerator().intValue());
		assertEquals(1,   splt2.getValueRat().getDenominator().intValue());
		
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

		KMyMoneyAccount acct11 = kmmInFile.getWritableAccountByID(TRX_1_SPLT_1_ACCT_ID);
		assertNotEquals(null, acct11);
		assertEquals(NOF_SPLT_ACCT11_PRE, acct11.getTransactionSplits().size());

		KMyMoneyAccount acct12 = kmmInFile.getAccountByID(TRX_1_SPLT_2_ACCT_ID);
		assertNotEquals(null, acct12);
		assertEquals(NOF_SPLT_ACCT12_PRE, acct12.getTransactionSplits().size());

		KMyMoneyPayee pye11 = kmmInFile.getPayeeByID(TRX_1_SPLT_1_PYE_ID);
		assertNotEquals(null, pye11);
		assertEquals(NOF_SPLT_PYE11_PRE, pye11.getTransactionSplits().size());
		
		KMyMoneyPayee pye12 = kmmInFile.getPayeeByID(TRX_1_SPLT_2_PYE_ID);
		assertNotEquals(null, pye12);
		assertEquals(NOF_SPLT_PYE12_PRE, pye12.getTransactionSplits().size());
		
		// ---

		KMyMoneyAccount acct21 = kmmInFile.getAccountByID(TRX_2_SPLT_1_ACCT_ID);
		assertNotEquals(null, acct21);
		assertEquals(NOF_SPLT_ACCT21_PRE, acct21.getTransactionSplits().size());

		KMyMoneyAccount acct22 = kmmInFile.getAccountByID(TRX_2_SPLT_2_ACCT_ID);
		assertNotEquals(null, acct22);
		assertEquals(NOF_SPLT_ACCT22_PRE, acct22.getTransactionSplits().size());
		
		KMyMoneyPayee pye21 = kmmInFile.getPayeeByID(TRX_2_SPLT_1_PYE_ID);
		assertNotEquals(null, pye21);
		assertEquals(NOF_SPLT_PYE21_PRE, pye21.getTransactionSplits().size());
		
		KMyMoneyPayee pye22 = kmmInFile.getPayeeByID(TRX_2_SPLT_2_PYE_ID);
		assertNotEquals(null, pye22);
		assertEquals(NOF_SPLT_PYE22_PRE, pye22.getTransactionSplits().size());
		
		// ----------------------------
		// Delete the object

		// Variant 1
		KMyMoneyWritableTransaction trx1 = kmmInFile.getWritableTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx1);
		kmmInFile.removeTransaction(trx1);

		// ---
		
		// Variant 2
		KMyMoneyWritableTransaction trx2 = kmmInFile.getWritableTransactionByID(TRX_2_ID);
		assertNotEquals(null, trx2);
		trx2.remove();

		// ----------------------------
		// Check whether the objects have actually been deleted
		// (in memory, not in the file yet).

		test04_1_check_memory(trx1, trx2,
				              acct11, acct12, acct21, acct22,
				              pye11, pye12, pye21, pye22);

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

	@Test
	public void test04_2() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));

		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.CACHE));

		// ----------------------------

		KMyMoneyAccount acct11 = kmmInFile.getWritableAccountByID(TRX_1_SPLT_1_ACCT_ID);
		assertNotEquals(null, acct11);
		assertEquals(NOF_SPLT_ACCT11_PRE, acct11.getTransactionSplits().size());

		KMyMoneyAccount acct12 = kmmInFile.getAccountByID(TRX_1_SPLT_2_ACCT_ID);
		assertNotEquals(null, acct12);
		assertEquals(NOF_SPLT_ACCT12_PRE, acct12.getTransactionSplits().size());

		KMyMoneyPayee pye11 = kmmInFile.getPayeeByID(TRX_1_SPLT_1_PYE_ID);
		assertNotEquals(null, pye11);
		assertEquals(NOF_SPLT_PYE11_PRE, pye11.getTransactionSplits().size());
		
		KMyMoneyPayee pye12 = kmmInFile.getPayeeByID(TRX_1_SPLT_2_PYE_ID);
		assertNotEquals(null, pye12);
		assertEquals(NOF_SPLT_PYE12_PRE, pye12.getTransactionSplits().size());
		
		// ---

		KMyMoneyAccount acct21 = kmmInFile.getAccountByID(TRX_2_SPLT_1_ACCT_ID);
		assertNotEquals(null, acct21);
		assertEquals(NOF_SPLT_ACCT21_PRE, acct21.getTransactionSplits().size());

		KMyMoneyAccount acct22 = kmmInFile.getAccountByID(TRX_2_SPLT_2_ACCT_ID);
		assertNotEquals(null, acct22);
		assertEquals(NOF_SPLT_ACCT22_PRE, acct22.getTransactionSplits().size());
		
		KMyMoneyPayee pye21 = kmmInFile.getPayeeByID(TRX_2_SPLT_1_PYE_ID);
		assertNotEquals(null, pye21);
		assertEquals(NOF_SPLT_PYE21_PRE, pye21.getTransactionSplits().size());
		
		KMyMoneyPayee pye22 = kmmInFile.getPayeeByID(TRX_2_SPLT_2_PYE_ID);
		assertNotEquals(null, pye22);
		assertEquals(NOF_SPLT_PYE22_PRE, pye22.getTransactionSplits().size());
		
		// ----------------------------
		// Delete the object

		// Variant 1
		KMyMoneyWritableTransaction trx1 = kmmInFile.getWritableTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx1);
		assertEquals(2, trx1.getSplitsCount());
		assertTrue(trx1.isBalanced());
		
		KMyMoneyWritableTransactionSplit splt11 = kmmInFile.getWritableTransactionSplitByID(TRX_1_SPLT_1_ID);
		assertNotEquals(null, splt11);
		kmmInFile.removeTransactionSplit(splt11);
		
		// ---

		// Variant 2
		KMyMoneyWritableTransaction trx2 = kmmInFile.getWritableTransactionByID(TRX_2_ID);
		assertNotEquals(null, trx2);
		assertEquals(2, trx2.getSplitsCount());
		assertTrue(trx2.isBalanced());
		
		KMyMoneyWritableTransactionSplit splt21 = kmmInFile.getWritableTransactionSplitByID(TRX_2_SPLT_1_ID);
		assertNotEquals(null, splt21);
		trx2.removeSplit(splt21);

		// ----------------------------
		// Check whether the objects have actually been deleted
		// (in memory, not in the file yet).

		test04_2_check_memory(trx1, trx2,
							  splt11, splt21,
							  acct11, acct12, acct21, acct22,
							  pye11, pye12, pye21, pye22);

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

		test04_2_check_persisted(outFile);
	}

	// ---------------------------------------------------------------

	private void test04_1_check_memory(
			KMyMoneyWritableTransaction trx1,
			KMyMoneyWritableTransaction trx2,
			KMyMoneyAccount acct11,
			KMyMoneyAccount acct12,
			KMyMoneyAccount acct21,
			KMyMoneyAccount acct22,
			KMyMoneyPayee pye11,
			KMyMoneyPayee pye12,
			KMyMoneyPayee pye21,
			KMyMoneyPayee pye22) throws Exception {
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
		assertEquals(0,   trx1.getBalanceRat().getNumerator().intValue());
		assertEquals(1,   trx1.getBalanceRat().getDenominator().intValue());

		assertEquals("", trx1.getMemo()); // unchanged
		assertEquals("2023-01-01", trx1.getDatePosted().toString()); // unchanged
		assertEquals("2023-01-01", trx1.getDatePostedFormatted());
		assertEquals("2023-11-03", trx1.getDateEntered().toString()); // unchanged
		assertEquals("2023-11-03", trx1.getDateEnteredFormatted());
		assertEquals(0, trx1.getSplitsCount()); // changed
		
		// However, the transaction cannot newly be instantiated any more,
		// just as you would expect.
		try {
			KMyMoneyWritableTransaction trx1Now = kmmInFile.getWritableTransactionByID(TRX_1_ID);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		
		// ::TODO
		// assertEquals(16, acct11.getTransactionSplits().size()); // changed
		// ::TODO
		// assertEquals(1, pye1.getTransactionSplits().size()); // changed
		
		// ---
		// Second transaction, same as above:
		
		// CAUTION / ::TODO
		// Cf. above.
		assertEquals(TRX_2_ID, trx2.getID()); // unchanged

		assertEquals(0.0, trx2.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE); // unchanged
		assertEquals(0,   trx2.getBalanceRat().getNumerator().intValue());
		assertEquals(1,   trx2.getBalanceRat().getDenominator().intValue());
		
		assertEquals("", trx2.getMemo()); // unchanged
		assertEquals("2023-01-03", trx2.getDatePosted().toString()); // unchanged
		assertEquals("2023-01-03", trx2.getDatePostedFormatted());
		assertEquals("2023-10-14", trx2.getDateEntered().toString()); // unchanged
		assertEquals("2023-10-14", trx2.getDateEnteredFormatted());
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

		// ------------------------
		// First transaction:
		
		// The transaction does not exist any more, just as you would expect.
		// However, no exception is thrown, as opposed to test04_1_check_memory()
		KMyMoneyTransaction trx1 = kmmOutFile.getTransactionByID(TRX_1_ID);
		assertEquals(null, trx1); // sic

		// ---

		KMyMoneyAccount acct11 = kmmInFile.getWritableAccountByID(TRX_1_SPLT_1_ACCT_ID);
		assertNotEquals(null, acct11);
		assertEquals(NOF_SPLT_ACCT11_PRE - 2, acct11.getTransactionSplits().size()); // changed

		KMyMoneyAccount acct12 = kmmInFile.getAccountByID(TRX_1_SPLT_2_ACCT_ID);
		assertNotEquals(null, acct12);
		assertEquals(NOF_SPLT_ACCT12_PRE - 1, acct12.getTransactionSplits().size()); // changed

		// ::TODO
//		KMyMoneyPayee pye11 = kmmInFile.getPayeeByID(TRX_1_SPLT_1_PYE_ID);
//		assertNotEquals(null, pye11);
//		assertEquals(NOF_SPLT_PYE11_PRE - 2, pye11.getTransactionSplits().size()); // changed
		
		// ::TODO
//		KMyMoneyPayee pye12 = kmmInFile.getPayeeByID(TRX_1_SPLT_2_PYE_ID);
//		assertNotEquals(null, pye12);
//		assertEquals(NOF_SPLT_PYE12_PRE - 2, pye12.getTransactionSplits().size()); // changed
		
		// ------------------------
		// Second transaction, same as above:
		
		// Cf. above
		KMyMoneyTransaction trx2 = kmmOutFile.getTransactionByID(TRX_2_ID);
		assertEquals(null, trx2); // sic
		
		// ---
		
		KMyMoneyAccount acct21 = kmmInFile.getAccountByID(TRX_2_SPLT_1_ACCT_ID);
		assertNotEquals(null, acct21);
		assertEquals(NOF_SPLT_ACCT21_PRE - 2, acct21.getTransactionSplits().size()); // changed

		KMyMoneyAccount acct22 = kmmInFile.getAccountByID(TRX_2_SPLT_2_ACCT_ID);
		assertNotEquals(null, acct22);
		assertEquals(NOF_SPLT_ACCT22_PRE - 1, acct22.getTransactionSplits().size()); // changed

		// ::TODO
//		KMyMoneyPayee pye21 = kmmInFile.getPayeeByID(TRX_2_SPLT_1_PYE_ID);
//		assertNotEquals(null, pye21);
//		assertEquals(NOF_SPLT_PYE21_PRE - 2, pye21.getTransactionSplits().size());// changed
		
//		KMyMoneyPayee pye22 = kmmInFile.getPayeeByID(TRX_2_SPLT_2_PYE_ID);
//		assertNotEquals(null, pye22);
//		assertEquals(NOF_SPLT_PYE21_PRE - 2, pye22.getTransactionSplits().size());// changed
	}

	// ---------------------------------------------------------------

	private void test04_2_check_memory(
			KMyMoneyWritableTransaction trx1,
			KMyMoneyWritableTransaction trx2, 
			KMyMoneyWritableTransactionSplit splt1,
			KMyMoneyWritableTransactionSplit splt2,
			KMyMoneyAccount acct11,
			KMyMoneyAccount acct12,
			KMyMoneyAccount acct21,
			KMyMoneyAccount acct22,
			KMyMoneyPayee pye11,
			KMyMoneyPayee pye12,
			KMyMoneyPayee pye21,
			KMyMoneyPayee pye22) throws Exception {
		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX, kmmInFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));

		assertEquals(ConstTest.Stats.NOF_TRX_SPLT - 2, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW));
		// ::TODO
		// assertEquals(ConstTest.Stats.NOF_TRX_SPLT - 2, kmmInFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.CACHE));

		// ---
		// First transaction:
		
		// CAUTION / ::TODO
		// Old Object still exists and is unchanged
		// Don't know what to do about this oddity right now,
		// but it needs to be addressed at some point.
		assertEquals(TRX_1_ID, splt1.getTransactionID()); // unchanged
		assertEquals(2, trx1.getSplitsCount()); // unchanged
		assertTrue(trx1.isBalanced()); // unchanged
		
		assertEquals(TRX_1_SPLT_1_ID.getSplitID(), splt1.getID()); // unchanged
		assertEquals(TRX_1_SPLT_1_ACCT_ID, splt1.getAccountID()); // unchanged
		assertEquals(TRX_1_SPLT_1_PYE_ID, splt1.getPayeeID()); // unchanged
		assertEquals(BigFraction.of(10000), splt1.getValueRat()); // changed
		assertEquals(BigFraction.of(10000), splt1.getSharesRat()); // changed
		assertEquals("", splt1.getMemo()); // unchanged
		
		// However, the transaction split cannot newly be instantiated any more,
		// just as you would expect.
		try {
			KMyMoneyWritableTransactionSplit splt1Now = kmmInFile.getWritableTransactionSplitByID(TRX_1_SPLT_1_ID);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		
		// ::TODO
//		KMyMoneyTransactionSplit trx1SpltLeft = trx1.getSplits().get(0);
//		assertEquals(TRX_1_SPLT_2_ID.getSplitID(), trx1SpltLeft.getID()); // unchanged
		
		// ::TODO
		assertEquals(NOF_SPLT_ACCT11_PRE, acct11.getTransactionSplits().size()); // unchanged
		assertEquals(NOF_SPLT_ACCT12_PRE, acct12.getTransactionSplits().size()); // unchanged
		assertEquals(NOF_SPLT_PYE11_PRE - 1, pye11.getTransactionSplits().size()); // changed
		assertEquals(NOF_SPLT_PYE12_PRE - 1, pye12.getTransactionSplits().size()); // changed
		
		// ---
		// Second transaction split, same as above:
		
		// CAUTION / ::TODO
		// Cf. above.
		assertEquals(TRX_2_ID, splt2.getTransactionID()); // unchanged
		assertEquals(1, trx2.getSplitsCount()); // changed
		// ::TODO
		// assertTrue(trx2.isBalanced()); // changed
		
		assertEquals(TRX_2_SPLT_1_ID.getSplitID(), splt2.getID()); // unchanged
		assertEquals(TRX_2_SPLT_1_ACCT_ID, splt2.getAccountID()); // unchanged
		assertEquals(TRX_2_SPLT_1_PYE_ID, splt2.getPayeeID()); // unchanged
		assertEquals(BigFraction.of(1000), splt2.getValueRat()); // changed
		assertEquals(BigFraction.of(1000), splt2.getSharesRat()); // changed
		assertEquals("Unfug und Quatsch GmbH Gehalt", splt2.getMemo()); // unchanged
		
		// Cf. above.
		try {
			KMyMoneyWritableTransactionSplit splt2Now = kmmInFile.getWritableTransactionSplitByID(TRX_2_SPLT_1_ID);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		
		KMyMoneyTransactionSplit trx2SpltLeft = trx2.getSplits().get(0);
		assertEquals(TRX_2_SPLT_2_ID.getSplitID(), trx2SpltLeft.getID()); // unchanged
		
		// ::TODO
		// assertEquals(NOF_SPLT_ACCT21_PRE, acct21.getTransactionSplits().size()); // unchanged  ::TODO
		assertEquals(NOF_SPLT_ACCT22_PRE, acct22.getTransactionSplits().size()); // unchanged
		assertEquals(NOF_SPLT_PYE21_PRE - 1, pye21.getTransactionSplits().size()); // changed
		assertEquals(NOF_SPLT_PYE22_PRE - 1, pye22.getTransactionSplits().size()); // changed
	}

	private void test04_2_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_TRX, kmmOutFileStats.getNofEntriesTransactions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX, kmmOutFileStats.getNofEntriesTransactions(KMMFileStats.Type.CACHE));

		assertEquals(ConstTest.Stats.NOF_TRX_SPLT - 2, kmmOutFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT - 2, kmmOutFileStats.getNofEntriesTransactionSplits(KMMFileStats.Type.CACHE));
		
		// ------------------------
		// First transaction:
		
		// The transaction split does not exist any more, just as you would expect.
		// However, no exception is thrown, as opposed to test04_2_check_memory()
		KMyMoneyTransaction trx1 = kmmOutFile.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx1);
		assertEquals(1, trx1.getSplitsCount()); // changed
		assertFalse(trx1.isBalanced()); // changed
		
		KMyMoneyTransactionSplit splt1 = kmmOutFile.getTransactionSplitByID(TRX_1_SPLT_1_ID);
		assertEquals(null, splt1); // sic
		KMyMoneyTransactionSplit trx1SpltLeft = trx1.getSplits().get(0);
		assertEquals(TRX_1_SPLT_2_ID, trx1SpltLeft.getQualifID()); // unchanged

		KMyMoneyAccount acct11 = kmmInFile.getAccountByID(TRX_1_SPLT_1_ACCT_ID);
		assertNotEquals(null, acct11);
		assertEquals(NOF_SPLT_ACCT11_PRE - 2, acct11.getTransactionSplits().size()); // changed

		KMyMoneyAccount acct12 = kmmInFile.getAccountByID(TRX_1_SPLT_2_ACCT_ID);
		assertNotEquals(null, acct12);
		assertEquals(NOF_SPLT_ACCT12_PRE, acct12.getTransactionSplits().size()); // unchanged

//		KMyMoneyPayee pye11 = kmmInFile.getPayeeByID(TRX_1_SPLT_1_PYE_ID);
//		assertNotEquals(null, pye11);
//		assertEquals(NOF_SPLT_PYE11_PRE - 1, pye11.getTransactionSplits().size()); // changed
		
		KMyMoneyPayee pye12 = kmmInFile.getPayeeByID(TRX_1_SPLT_2_PYE_ID);
		assertNotEquals(null, pye12);
		assertEquals(NOF_SPLT_PYE12_PRE - 1, pye12.getTransactionSplits().size()); // changed
		
		// ------------------------
		// Second transaction split, same as above:
		
		// Cf. above
		KMyMoneyTransaction trx2 = kmmOutFile.getTransactionByID(TRX_2_ID);
		assertNotEquals(null, trx2);
		assertEquals(1, trx2.getSplitsCount()); // changed
		assertFalse(trx2.isBalanced()); // changed
		
		KMyMoneyTransactionSplit splt2 = kmmOutFile.getTransactionSplitByID(TRX_2_SPLT_1_ID);
		assertEquals(null, splt2); // sic
		KMyMoneyTransactionSplit trx2SpltLeft = trx2.getSplits().get(0);
		assertEquals(TRX_2_SPLT_2_ID, trx2SpltLeft.getQualifID()); // unchanged
		
		KMyMoneyAccount acct21 = kmmInFile.getAccountByID(TRX_2_SPLT_1_ACCT_ID);
		assertNotEquals(null, acct21);
		assertEquals(NOF_SPLT_ACCT21_PRE - 2, acct21.getTransactionSplits().size()); // changed

		KMyMoneyAccount acct22 = kmmInFile.getAccountByID(TRX_2_SPLT_2_ACCT_ID);
		assertNotEquals(null, acct22);
		assertEquals(NOF_SPLT_ACCT22_PRE, acct22.getTransactionSplits().size()); // unchanged

		KMyMoneyPayee pye21 = kmmInFile.getPayeeByID(TRX_2_SPLT_1_PYE_ID);
		assertNotEquals(null, pye21);
		assertEquals(NOF_SPLT_PYE21_PRE - 1, pye21.getTransactionSplits().size()); // changed
		
		// ::TODO
//		KMyMoneyPayee pye22 = kmmInFile.getPayeeByID(TRX_2_SPLT_2_PYE_ID);
//		assertNotEquals(null, pye22);
//		assertEquals(NOF_SPLT_PYE22_PRE, pye22.getTransactionSplits().size()); // unchanged
	}

	// ------------------------------
	// PART 4.2: Low-Level
	// ------------------------------

	// ::TODO

}
