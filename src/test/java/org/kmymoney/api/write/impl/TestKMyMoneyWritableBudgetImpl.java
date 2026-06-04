package org.kmymoney.api.write.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Locale;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyBudgetImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.write.KMyMoneyWritableBudget;
import org.kmymoney.api.write.aux.KMMWritableBudgetAccount;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyWritableBudgetImpl {
	public static final KMMBdgtID BDGT_1_ID = TestKMyMoneyBudgetImpl.BDGT_1_ID;
	// public static final KMMBdgtID BDGT_2_ID = TestKMyMoneyBudgetImpl.BDGT_2_ID;

	public static final KMMAcctID ACCT_1_ID = TestKMyMoneyBudgetImpl.ACCT_1_ID;
	public static final KMMAcctID ACCT_2_ID = TestKMyMoneyBudgetImpl.ACCT_2_ID;
	public static final KMMAcctID ACCT_3_ID = TestKMyMoneyBudgetImpl.ACCT_3_ID;
	public static final KMMAcctID ACCT_4_ID = TestKMyMoneyBudgetImpl.ACCT_4_ID;
	public static final KMMAcctID ACCT_5_ID = TestKMyMoneyBudgetImpl.ACCT_5_ID;

	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

	private KMMFileStats kmmInFileStats = null;
	private KMMFileStats kmmOutFileStats = null;

	private KMMBdgtID newBdgtID = null;

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
		return new JUnit4TestAdapter(TestKMyMoneyWritableBudgetImpl.class);
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
	// Cf. TestKMyMoneyBudget.test01/02
	//
	// Check whether the KMyMoneyWritableBudget objects returned by
	// KMyMoneyWritableFileImpl.getWritableBudgetByID() are actually
	// complete (as complete as returned be KMyMoneyFileImpl.getBudgetByID().

	@Test
	public void test01() throws Exception {
		KMyMoneyWritableBudget bdgt = kmmInFile.getWritableBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);

		assertEquals(BDGT_1_ID, bdgt.getID());
		assertEquals("Budget 2026", bdgt.getName());
		assertEquals(LocalDate.of(2026, 1, 1), bdgt.getStart());
		
		assertEquals(5, bdgt.getAccounts().size());
		assertEquals(ACCT_1_ID, bdgt.getAccounts().get(0).getAcctID());
		assertEquals(ACCT_2_ID, bdgt.getAccounts().get(1).getAcctID());
		assertEquals(ACCT_3_ID, bdgt.getAccounts().get(2).getAcctID());
		assertEquals(ACCT_4_ID, bdgt.getAccounts().get(3).getAcctID());
		assertEquals(ACCT_5_ID, bdgt.getAccounts().get(4).getAcctID());

		assertEquals(2, bdgt.getPeriods(ACCT_1_ID).size());
		assertEquals(1, bdgt.getPeriods(ACCT_2_ID).size());
		assertEquals(1, bdgt.getPeriods(ACCT_3_ID).size());
		assertEquals(1, bdgt.getPeriods(ACCT_4_ID).size());
		assertEquals(1, bdgt.getPeriods(ACCT_5_ID).size());

		KMMBudgetAccount bdgtAcct = bdgt.getAccounts().get(0);
		assertEquals(bdgt, bdgtAcct.getParent());
		assertEquals(KMMBudgetAccount.Level.MONTH_BY_MONTH, bdgtAcct.getLevel());
		assertEquals(2, bdgtAcct.getPeriods().size());
		
		bdgtAcct = bdgt.getAccounts().get(1);
		assertEquals(bdgt, bdgtAcct.getParent());
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel());
		assertEquals(1, bdgtAcct.getPeriods().size());
		
		bdgtAcct = bdgt.getAccounts().get(2);
		assertEquals(KMMBudgetAccount.Level.MONTHLY, bdgtAcct.getLevel());
		assertEquals(1, bdgtAcct.getPeriods().size());
		
		bdgtAcct = bdgt.getAccounts().get(3);
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel());
		assertEquals(1, bdgtAcct.getPeriods().size());
		
		bdgtAcct = bdgt.getAccounts().get(4);
		assertEquals(KMMBudgetAccount.Level.MONTHLY, bdgtAcct.getLevel());
		assertEquals(1, bdgtAcct.getPeriods().size());
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritableBudget objects returned by
	// can actually be modified -- both in memory and persisted in file.

	@Test
	public void test02_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_BDGT, kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_BDGT, kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.CACHE));

		KMyMoneyWritableBudget bdgt = kmmInFile.getWritableBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);

		assertEquals(BDGT_1_ID, bdgt.getID());

		// ----------------------------
		// Modify the object

		bdgt.setName("Improved Budget 3000");
		bdgt.setStart(LocalDate.of(2122, 4, 7));

		// ::TODO modify entries

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(bdgt);

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

//	@Test
//	public void test02_2() throws Exception {
//		// ::TODO
//	}
	
	// ---------------------------------------------------------------

	private void test02_1_check_memory(KMyMoneyWritableBudget bdgt) throws Exception {
		assertEquals(ConstTest.Stats.NOF_BDGT, kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_BDGT, kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.CACHE));

		assertEquals(BDGT_1_ID, bdgt.getID()); // unchanged
		assertEquals("Improved Budget 3000", bdgt.getName()); // changed
		assertEquals(LocalDate.of(2122, 4, 7), bdgt.getStart()); // changed
		
		assertEquals(5, bdgt.getAccounts().size()); // unchanged
		assertEquals(ACCT_1_ID, bdgt.getAccounts().get(0).getAcctID()); // unchanged
		assertEquals(ACCT_2_ID, bdgt.getAccounts().get(1).getAcctID()); // unchanged
		assertEquals(ACCT_3_ID, bdgt.getAccounts().get(2).getAcctID()); // unchanged
		assertEquals(ACCT_4_ID, bdgt.getAccounts().get(3).getAcctID()); // unchanged
		assertEquals(ACCT_5_ID, bdgt.getAccounts().get(4).getAcctID()); // unchanged

		assertEquals(2, bdgt.getPeriods(ACCT_1_ID).size()); // unchanged
		assertEquals(1, bdgt.getPeriods(ACCT_2_ID).size()); // unchanged
		assertEquals(1, bdgt.getPeriods(ACCT_3_ID).size()); // unchanged
		assertEquals(1, bdgt.getPeriods(ACCT_4_ID).size()); // unchanged
		assertEquals(1, bdgt.getPeriods(ACCT_5_ID).size()); // unchanged

		KMMBudgetAccount bdgtAcct = bdgt.getAccounts().get(0);
		assertEquals(bdgt, bdgtAcct.getParent()); // unchanged
		assertEquals(KMMBudgetAccount.Level.MONTH_BY_MONTH, bdgtAcct.getLevel()); // unchanged
		assertEquals(2, bdgtAcct.getPeriods().size()); // unchanged
		
		bdgtAcct = bdgt.getAccounts().get(1);
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel()); // unchanged
		assertEquals(1, bdgtAcct.getPeriods().size()); // unchanged
		
		bdgtAcct = bdgt.getAccounts().get(2);
		assertEquals(bdgt, bdgtAcct.getParent()); // unchanged
		assertEquals(KMMBudgetAccount.Level.MONTHLY, bdgtAcct.getLevel()); // unchanged
		assertEquals(1, bdgtAcct.getPeriods().size()); // unchanged
		
		bdgtAcct = bdgt.getAccounts().get(3);
		assertEquals(bdgt, bdgtAcct.getParent()); // unchanged
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel()); // unchanged
		assertEquals(1, bdgtAcct.getPeriods().size()); // unchanged
		
		bdgtAcct = bdgt.getAccounts().get(4);
		assertEquals(bdgt, bdgtAcct.getParent()); // unchanged
		assertEquals(KMMBudgetAccount.Level.MONTHLY, bdgtAcct.getLevel()); // unchanged
		assertEquals(1, bdgtAcct.getPeriods().size()); // unchanged
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_BDGT, kmmOutFileStats.getNofEntriesBudgets(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_BDGT, kmmOutFileStats.getNofEntriesBudgets(KMMFileStats.Type.CACHE));

		KMyMoneyBudget bdgt = kmmOutFile.getBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);

		assertEquals(BDGT_1_ID, bdgt.getID()); // unchanged
		assertEquals("Improved Budget 3000", bdgt.getName()); // changed
		assertEquals(LocalDate.of(2122, 4, 7), bdgt.getStart()); // changed
		
		assertEquals(5, bdgt.getAccounts().size()); // unchanged
		assertEquals(ACCT_1_ID, bdgt.getAccounts().get(0).getAcctID()); // unchanged
		assertEquals(ACCT_2_ID, bdgt.getAccounts().get(1).getAcctID()); // unchanged
		assertEquals(ACCT_3_ID, bdgt.getAccounts().get(2).getAcctID()); // unchanged
		assertEquals(ACCT_4_ID, bdgt.getAccounts().get(3).getAcctID()); // unchanged
		assertEquals(ACCT_5_ID, bdgt.getAccounts().get(4).getAcctID()); // unchanged

		assertEquals(2, bdgt.getPeriods(ACCT_1_ID).size()); // unchanged
		assertEquals(1, bdgt.getPeriods(ACCT_2_ID).size()); // unchanged
		assertEquals(1, bdgt.getPeriods(ACCT_3_ID).size()); // unchanged
		assertEquals(1, bdgt.getPeriods(ACCT_4_ID).size()); // unchanged
		assertEquals(1, bdgt.getPeriods(ACCT_5_ID).size()); // unchanged

		KMMBudgetAccount bdgtAcct = bdgt.getAccounts().get(0);
		assertEquals(bdgt, bdgtAcct.getParent()); // unchanged
		assertEquals(KMMBudgetAccount.Level.MONTH_BY_MONTH, bdgtAcct.getLevel()); // unchanged
		assertEquals(2, bdgtAcct.getPeriods().size()); // unchanged
		
		bdgtAcct = bdgt.getAccounts().get(1);
		assertEquals(bdgt, bdgtAcct.getParent()); // unchanged
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel()); // unchanged
		assertEquals(1, bdgtAcct.getPeriods().size()); // unchanged
		
		bdgtAcct = bdgt.getAccounts().get(2);
		assertEquals(bdgt, bdgtAcct.getParent()); // unchanged
		assertEquals(KMMBudgetAccount.Level.MONTHLY, bdgtAcct.getLevel()); // unchanged
		assertEquals(1, bdgtAcct.getPeriods().size()); // unchanged
		
		bdgtAcct = bdgt.getAccounts().get(3);
		assertEquals(bdgt, bdgtAcct.getParent()); // unchanged
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel()); // unchanged
		assertEquals(1, bdgtAcct.getPeriods().size()); // unchanged
		
		bdgtAcct = bdgt.getAccounts().get(4);
		assertEquals(bdgt, bdgtAcct.getParent()); // unchanged
		assertEquals(KMMBudgetAccount.Level.MONTHLY, bdgtAcct.getLevel()); // unchanged
		assertEquals(1, bdgtAcct.getPeriods().size()); // unchanged
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

		assertEquals(ConstTest.Stats.NOF_BDGT, kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_BDGT, kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.CACHE));

		// ----------------------------
		// Bare naked object

		KMyMoneyWritableBudget bdgt = kmmInFile.createWritableBudget("The ultimate budget -- once and for all");
		assertNotEquals(null, bdgt);
		newBdgtID = bdgt.getID();
		assertEquals(true, newBdgtID.isSet());

		// ----------------------------
		// Modify the object

		bdgt.setStart(LocalDate.of(2100, 1, 1));

		// Add accounts (without periods)

		KMMWritableBudgetAccount bdgtAcct1 = bdgt.createWritableAccount(ACCT_1_ID, KMMBudgetAccount.Level.MONTHLY);
		bdgtAcct1.setIncludesSubAccounts(false);

		KMMWritableBudgetAccount bdgtAcct2 = bdgt.createWritableAccount(ACCT_2_ID, KMMBudgetAccount.Level.YEARLY);
		bdgtAcct2.setIncludesSubAccounts(true);

		KMMWritableBudgetAccount bdgtAcct3 = bdgt.createWritableAccount(ACCT_4_ID, KMMBudgetAccount.Level.MONTH_BY_MONTH);
		bdgtAcct3.setIncludesSubAccounts(false);

		// ----------------------------
		// Check whether the object has actually been modified
		// (in memory, not in the file yet).

		test03_1_check_memory(bdgt);

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

	private void test03_1_check_memory(KMyMoneyWritableBudget bdgt) throws Exception {
		assertEquals(ConstTest.Stats.NOF_BDGT + 1, kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.RAW));
		// CAUTION: The counter has not been updated yet.
		// This is on purpose
		// ::TODO
		// assertEquals(ConstTest.Stats.NOF_BDGT,
		// kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.COUNTER));
		assertEquals(ConstTest.Stats.NOF_BDGT + 1, kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.CACHE));

		assertEquals("The ultimate budget -- once and for all", bdgt.getName());
		assertEquals(LocalDate.of(2100, 1, 1), bdgt.getStart());
		
		assertEquals(3, bdgt.getAccounts().size());
		assertEquals(ACCT_1_ID, bdgt.getAccounts().get(0).getAcctID());
		assertEquals(ACCT_2_ID, bdgt.getAccounts().get(1).getAcctID());
		assertEquals(ACCT_4_ID, bdgt.getAccounts().get(2).getAcctID());

		assertEquals(0, bdgt.getPeriods(ACCT_1_ID).size());
		assertEquals(0, bdgt.getPeriods(ACCT_2_ID).size());
		assertEquals(0, bdgt.getPeriods(ACCT_4_ID).size());

		KMMBudgetAccount bdgtAcct = bdgt.getAccounts().get(0);
		assertEquals(bdgt, bdgtAcct.getParent());
		assertEquals(KMMBudgetAccount.Level.MONTHLY, bdgtAcct.getLevel());
		assertEquals(0, bdgtAcct.getPeriods().size());
		
		bdgtAcct = bdgt.getAccounts().get(1);
		assertEquals(bdgt, bdgtAcct.getParent());
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel());
		assertEquals(0, bdgtAcct.getPeriods().size());
		
		bdgtAcct = bdgt.getAccounts().get(2);
		assertEquals(bdgt, bdgtAcct.getParent());
		assertEquals(KMMBudgetAccount.Level.MONTH_BY_MONTH, bdgtAcct.getLevel());
		assertEquals(0, bdgtAcct.getPeriods().size());
	}

	private void test03_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		// Here, all 3 stats variants must have been updated
		assertEquals(ConstTest.Stats.NOF_BDGT + 1, kmmOutFileStats.getNofEntriesBudgets(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_BDGT + 1, kmmOutFileStats.getNofEntriesBudgets(KMMFileStats.Type.CACHE));

		KMyMoneyBudget bdgt = kmmOutFile.getBudgetByID(newBdgtID);
		assertNotEquals(null, bdgt);

		assertEquals("The ultimate budget -- once and for all", bdgt.getName());
		assertEquals(LocalDate.of(2100, 1, 1), bdgt.getStart());
		
		assertEquals(3, bdgt.getAccounts().size());
		assertEquals(ACCT_1_ID, bdgt.getAccounts().get(0).getAcctID());
		assertEquals(ACCT_2_ID, bdgt.getAccounts().get(1).getAcctID());
		assertEquals(ACCT_4_ID, bdgt.getAccounts().get(2).getAcctID());

		assertEquals(0, bdgt.getPeriods(ACCT_1_ID).size());
		assertEquals(0, bdgt.getPeriods(ACCT_2_ID).size());
		assertEquals(0, bdgt.getPeriods(ACCT_4_ID).size());

		KMMBudgetAccount bdgtAcct = bdgt.getAccounts().get(0);
		assertEquals(bdgt, bdgtAcct.getParent());
		assertEquals(KMMBudgetAccount.Level.MONTHLY, bdgtAcct.getLevel());
		assertEquals(0, bdgtAcct.getPeriods().size());
		
		bdgtAcct = bdgt.getAccounts().get(1);
		assertEquals(bdgt, bdgtAcct.getParent());
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel());
		assertEquals(0, bdgtAcct.getPeriods().size());
		
		bdgtAcct = bdgt.getAccounts().get(2);
		assertEquals(bdgt, bdgtAcct.getParent());
		assertEquals(KMMBudgetAccount.Level.MONTH_BY_MONTH, bdgtAcct.getLevel());
		assertEquals(0, bdgtAcct.getPeriods().size());
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

//	@Test
//	public void test04_1() throws Exception {
//		kmmInFileStats = new KMMFileStats(kmmInFile);
//
//		assertEquals(ConstTest.Stats.NOF_BDGT, kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.RAW));
//		assertEquals(ConstTest.Stats.NOF_BDGT, kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.CACHE));
//
//
//		// ----------------------------
//		// Delete the object
//
//		// Variant 1
//		KMyMoneyWritableBudget bdgt1 = kmmInFile.getWritableBudgetByID(BDGT_1_ID);
//		assertNotEquals(null, bdgt1);
//		kmmInFile.removeBudget(bdgt1);
//
//		// Variant 2
//		KMyMoneyWritableBudget bdgt2 = kmmInFile.getWritableBudgetByID(BDGT_2_ID);
//		bdgt2.remove();
//
//		// ----------------------------
//		// Check whether the objects have actually been deleted
//		// (in memory, not in the file yet).
//
//		test04_1_check_memory(bdgt1, bdgt2);
//
//		// ----------------------------
//		// Now, check whether the deletions have been written to the
//		// output file, then re-read from it, and whether is is what
//		// we expect it is.
//
//		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
//		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
//		// + outFile.getPath() + "'");
//		outFile.delete(); // sic, the temp. file is already generated (empty),
//		// and the KMyMoney file writer does not like that.
//		kmmInFile.writeFile(outFile);
//
//		test04_1_check_persisted(outFile);
//	}
//
//	// ---------------------------------------------------------------
//
//	private void test04_1_check_memory(KMyMoneyWritableBudget bdgt1,
//									   KMyMoneyWritableBudget bdgt2) throws Exception {
//		assertEquals(ConstTest.Stats.NOF_BDGT - 2, kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.RAW));
//		assertEquals(ConstTest.Stats.NOF_BDGT - 2, kmmInFileStats.getNofEntriesBudgets(KMMFileStats.Type.CACHE));
//
//		// ---
//		// First transaction:
//		
//		// CAUTION / ::TODO
//		// Old Object still exists and is unchanged
//		// Exception: no splits any more
//		// Don't know what to do about this oddity right now,
//		// but it needs to be addressed at some point.
//		assertEquals(0.0, bdgt1.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE); // unchanged
//		assertEquals("", bdgt1.getMemo()); // unchanged
//		assertEquals("2023-01-01", bdgt1.getDatePosted().toString()); // unchanged
//		assertEquals("2023-01-01", bdgt1.getDatePostedFormatted());
//		assertEquals("2023-11-03", bdgt1.getDateEntered().toString()); // unchanged
//		assertEquals("2023-11-03", bdgt1.getDateEnteredFormatted());
//		assertEquals(0, bdgt1.getSplitsCount()); // changed
//		
//		// However, the transaction cannot newly be instantiated any more,
//		// just as you would expect.
//		try {
//			KMyMoneyWritableBudget bdgt1Now = kmmInFile.getWritableBudgetByID(BDGT_1_ID);
//			assertEquals(1, 0);
//		} catch ( Exception exc ) {
//			assertEquals(0, 0);
//		}
//		
//		// ---
//		// Second transaction, same as above:
//		
//		// CAUTION / ::TODO
//		// Cf. above.
//		assertEquals(BDGT_2_ID, bdgt2.getID()); // unchanged
//		assertEquals(0.0, bdgt2.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE); // unchanged
//		assertEquals("", bdgt2.getMemo()); // unchanged
//		assertEquals("2023-01-03", bdgt2.getDatePosted().toString()); // unchanged
//		assertEquals("2023-01-03", bdgt2.getDatePostedFormatted());
//		assertEquals("2023-10-14", bdgt2.getDateEntered().toString()); // unchanged
//		assertEquals("2023-10-14", bdgt2.getDateEnteredFormatted());
//		assertEquals(0, bdgt2.getSplitsCount()); // changed
//		
//		// Cf. above.
//		try {
//			KMyMoneyWritableBudget bdgt2Now = kmmInFile.getWritableBudgetByID(BDGT_2_ID);
//			assertEquals(1, 0);
//		} catch ( Exception exc ) {
//			assertEquals(0, 0);
//		}
//	}
//
//	private void test04_1_check_persisted(File outFile) throws Exception {
//		kmmOutFile = new KMyMoneyFileImpl(outFile);
//		kmmOutFileStats = new KMMFileStats(kmmOutFile);
//
//		assertEquals(ConstTest.Stats.NOF_BDGT - 2, kmmOutFileStats.getNofEntriesBudgets(KMMFileStats.Type.RAW));
//		assertEquals(ConstTest.Stats.NOF_BDGT - 2, kmmOutFileStats.getNofEntriesBudgets(KMMFileStats.Type.CACHE));
//
//		// ---
//		// First transaction:
//		
//		// The transaction does not exist any more, just as you would expect.
//		// However, no exception is thrown, as opposed to test04_1_check_memory()
//		KMyMoneyBudget bdgt1 = kmmOutFile.getBudgetByID(BDGT_1_ID);
//		assertEquals(null, bdgt1); // sic
//
//		// ---
//		// Second transaction, same as above:
//		
//		// Cf. above
//		KMyMoneyBudget bdgt2 = kmmOutFile.getBudgetByID(BDGT_2_ID);
//		assertEquals(null, bdgt2); // sic
//	}

	// ------------------------------
	// PART 4.2: Low-Level
	// ------------------------------

	// ::TODO

}
