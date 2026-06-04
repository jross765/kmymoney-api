package org.kmymoney.api.write.impl.aux;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Locale;

import org.apache.commons.numbers.fraction.BigFraction;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.aux.KMMBudgetPeriod;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.write.KMyMoneyWritableBudget;
import org.kmymoney.api.write.aux.KMMWritableBudgetAccount;
import org.kmymoney.api.write.aux.KMMWritableBudgetPeriod;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.api.write.impl.TestKMyMoneyWritableBudgetImpl;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;

import junit.framework.JUnit4TestAdapter;

public class TestKMMWritableBudgetAccountImpl {
	private static final KMMBdgtID BDGT_1_ID = TestKMyMoneyWritableBudgetImpl.BDGT_1_ID;
	// private static final KMMBdgtID BDGT_2_ID = TestKMyMoneyWritableBudgetImpl.BDGT_2_ID;

	private static final KMMAcctID ACCT_1_ID = TestKMyMoneyWritableBudgetImpl.ACCT_1_ID;
	private static final KMMAcctID ACCT_6_ID = new KMMAcctID("A000031");

	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

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
		return new JUnit4TestAdapter(TestKMMWritableBudgetAccountImpl.class);
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
		
		KMMWritableBudgetAccount bdgtAcct = bdgt.getWritableAccounts().get(0);
		assertNotEquals(null, bdgtAcct);
		
		assertEquals(ACCT_1_ID, bdgtAcct.getAcctID());
		assertEquals(KMMBudgetAccount.Level.MONTH_BY_MONTH, bdgtAcct.getLevel());
		assertEquals(false, bdgtAcct.includesSubAccounts());

		assertEquals(2, bdgtAcct.getPeriods().size());
		
		KMMBudgetPeriod bdgtPrd = bdgtAcct.getPeriods().get(0);
		assertEquals(LocalDate.of(2026, 3, 1), bdgtPrd.getStart());
		assertEquals(750.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("750,00 €", bdgtPrd.getAmountFormatted(Locale.GERMANY));
		assertEquals("€750.00", bdgtPrd.getAmountFormatted(Locale.US));

		bdgtPrd = bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1);
		assertEquals(LocalDate.of(2026, 7, 1), bdgtPrd.getStart());
		assertEquals(1500.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("1.500,00 €", bdgtPrd.getAmountFormatted(Locale.GERMANY));
		assertEquals("€1,500.00", bdgtPrd.getAmountFormatted(Locale.US));
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritableBudget objects returned by
	// can actually be modified -- both in memory and persisted in file.

	@Test
	public void test02_1() throws Exception {
		KMyMoneyWritableBudget bdgt = kmmInFile.getWritableBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);
		assertEquals(BDGT_1_ID, bdgt.getID());
		
		KMMWritableBudgetAccount bdgtAcct = bdgt.getWritableAccounts().get(0);
		assertNotEquals(null, bdgtAcct);
		assertEquals(ACCT_1_ID, bdgtAcct.getAcctID());

		assertEquals(2, bdgtAcct.getPeriods().size()); // unchanged
		
		// ----------------------------
		// Modify the object

		bdgtAcct.setAcctID(ACCT_6_ID);
		bdgtAcct.setLevel(KMMBudgetAccount.Level.MONTHLY);
		bdgtAcct.setIncludesSubAccounts(true);

		// ::TODO modify period entries

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(bdgtAcct);

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

	private void test02_1_check_memory(KMMWritableBudgetAccount bdgtAcct) throws Exception {
		assertEquals(ACCT_6_ID, bdgtAcct.getAcctID()); // changed
		assertEquals(KMMBudgetAccount.Level.MONTHLY, bdgtAcct.getLevel()); // changed
		assertEquals(true, bdgtAcct.includesSubAccounts()); // changed
		
		assertEquals(2, bdgtAcct.getPeriods().size()); // unchanged
		
		KMMBudgetPeriod bdgtPrd = bdgtAcct.getPeriods().get(0);
		assertEquals(LocalDate.of(2026, 3, 1), bdgtPrd.getStart()); // unchanged
		assertEquals(750.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE); // unchanged
		assertEquals("750,00 €", bdgtPrd.getAmountFormatted(Locale.GERMANY)); // unchanged
		assertEquals("€750.00", bdgtPrd.getAmountFormatted(Locale.US)); // unchanged
		// .
		assertEquals(LocalDate.of(2026, 7, 1), bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getStart()); // unchanged
		assertEquals(1500.0, bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE); // unchanged
		assertEquals("1.500,00 €", bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getAmountFormatted(Locale.GERMANY)); // unchanged
		assertEquals("€1,500.00", bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getAmountFormatted(Locale.US)); // unchanged
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		
		KMyMoneyBudget bdgt = kmmOutFile.getBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);
		assertEquals(BDGT_1_ID, bdgt.getID());
		
		KMMBudgetAccount bdgtAcct = bdgt.getAccounts().get(0);
		assertNotEquals(null, bdgtAcct);
		
		assertEquals(ACCT_6_ID, bdgtAcct.getAcctID()); // changed
		assertEquals(KMMBudgetAccount.Level.MONTHLY, bdgtAcct.getLevel()); // changed
		assertEquals(true, bdgtAcct.includesSubAccounts()); // changed
		
		assertEquals(2, bdgtAcct.getPeriods().size()); // unchanged
		
		KMMBudgetPeriod bdgtPrd = bdgtAcct.getPeriods().get(0);
		assertEquals(LocalDate.of(2026, 3, 1), bdgtPrd.getStart()); // unchanged
		assertEquals(750.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE); // unchanged
		assertEquals("750,00 €", bdgtPrd.getAmountFormatted(Locale.GERMANY)); // unchanged
		assertEquals("€750.00", bdgtPrd.getAmountFormatted(Locale.US)); // unchanged
		// .
		assertEquals(LocalDate.of(2026, 7, 1), bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getStart()); // unchanged
		assertEquals(1500.0, bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE); // unchanged
		assertEquals("1.500,00 €", bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getAmountFormatted(Locale.GERMANY)); // unchanged
		assertEquals("€1,500.00", bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getAmountFormatted(Locale.US)); // unchanged
	}

	// -----------------------------------------------------------------
	// PART 3: Create new objects
	// -----------------------------------------------------------------

	// ------------------------------
	// PART 3.1: High-Level
	// ------------------------------

	@Test
	public void test03_1() throws Exception {
		KMyMoneyWritableBudget bdgt = kmmInFile.getWritableBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);
		assertEquals(BDGT_1_ID, bdgt.getID());
		
		assertEquals(5, bdgt.getAccounts().size());
		
		// ----------------------------
		// Bare naked object

		KMMWritableBudgetAccount bdgtAcct = bdgt.createWritableAccount(ACCT_6_ID, KMMBudgetAccount.Level.YEARLY);
		assertNotEquals(null, bdgtAcct);
		
		assertEquals(ACCT_6_ID, bdgtAcct.getAcctID());
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel());

		// ----------------------------
		// Modify the object
		
		bdgtAcct.createWritablePeriod(LocalDate.of(2027, 3, 1), BigFraction.of(100));
		bdgtAcct.createWritablePeriod(LocalDate.of(2027, 4, 1), BigFraction.of(200));
		bdgtAcct.createWritablePeriod(LocalDate.of(2027, 5, 1), BigFraction.of(300));

		// ----------------------------
		// Check whether the object has actually been modified
		// (in memory, not in the file yet).

		test03_1_check_memory(bdgt, bdgtAcct);

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

	private void test03_1_check_memory(KMyMoneyWritableBudget bdgt, 
									   KMMWritableBudgetAccount bdgtAcct) throws Exception {
		assertEquals(6, bdgt.getAccounts().size()); // changed
		
		assertEquals(ACCT_6_ID, bdgtAcct.getAcctID());
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel());
		assertEquals(false, bdgtAcct.includesSubAccounts());
		
		assertEquals(3, bdgtAcct.getPeriods().size());

		KMMWritableBudgetPeriod bdgtPrd = bdgtAcct.getWritablePeriods().get(0);
		assertEquals(LocalDate.of(2027, 3, 1), bdgtPrd.getStart());
		assertEquals(100.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);

		bdgtPrd = bdgtAcct.getWritablePeriods().get(1);
		assertEquals(LocalDate.of(2027, 4, 1), bdgtPrd.getStart());
		assertEquals(200.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);

		bdgtPrd = bdgtAcct.getWritablePeriods().get(2);
		assertEquals(LocalDate.of(2027, 5, 1), bdgtPrd.getStart());
		assertEquals(300.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
	}

	private void test03_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		
		KMyMoneyBudget bdgt = kmmOutFile.getBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);
		assertEquals(BDGT_1_ID, bdgt.getID());
		
		assertEquals(6, bdgt.getAccounts().size()); // changed
		
		KMMBudgetAccount bdgtAcct = bdgt.getAccounts().get( bdgt.getAccounts().size() - 1 );
		assertNotEquals(null, bdgtAcct);
		
		// ---
		
		assertEquals(ACCT_6_ID, bdgtAcct.getAcctID());
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel());
		assertEquals(false, bdgtAcct.includesSubAccounts());
		
		assertEquals(3, bdgtAcct.getPeriods().size());

		KMMBudgetPeriod bdgtPrd = bdgtAcct.getPeriods().get(0);
		assertEquals(LocalDate.of(2027, 3, 1), bdgtPrd.getStart());
		assertEquals(100.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);

		bdgtPrd = bdgtAcct.getPeriods().get(1);
		assertEquals(LocalDate.of(2027, 4, 1), bdgtPrd.getStart());
		assertEquals(200.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);

		bdgtPrd = bdgtAcct.getPeriods().get(2);
		assertEquals(LocalDate.of(2027, 5, 1), bdgtPrd.getStart());
		assertEquals(300.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
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
		KMyMoneyWritableBudget bdgt = kmmInFile.getWritableBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);
		assertEquals(BDGT_1_ID, bdgt.getID());
		
		assertEquals(5, bdgt.getAccounts().size());
		
		// ----------------------------
		// Delete the object

		KMMWritableBudgetAccount bdgtAcct = bdgt.getWritableAccount(ACCT_1_ID);
		assertNotEquals(null, bdgtAcct);
		bdgt.removeAccount(bdgtAcct);

		// ----------------------------
		// Check whether the objects have actually been deleted
		// (in memory, not in the file yet).

		test04_1_check_memory(bdgt, bdgtAcct);

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

	private void test04_1_check_memory(KMyMoneyWritableBudget bdgt,
									   KMMWritableBudgetAccount bdgtAcct) throws Exception {
		assertEquals(4, bdgt.getAccounts().size()); // changed
		
		// CAUTION / ::TODO
		// Old Object still exists and is unchanged
		assertEquals(ACCT_1_ID, bdgtAcct.getAcctID()); // unchanged
		assertEquals(KMMBudgetAccount.Level.MONTH_BY_MONTH, bdgtAcct.getLevel()); // unchanged
		assertEquals(false, bdgtAcct.includesSubAccounts()); // unchanged
		
		assertEquals(2, bdgtAcct.getPeriods().size()); // unchanged
		
		// However, the budget account cannot newly be instantiated any more,
		// just as you would expect.
		try {
			KMMWritableBudgetAccount bdgtAcctNow = bdgt.getWritableAccount(ACCT_1_ID);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
	}

	private void test04_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		// The budget account does not exist any more, just as you would expect.
		// However, no exception is thrown, as opposed to test04_1_check_memory()
		KMyMoneyWritableBudget bdgt = kmmInFile.getWritableBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);
		assertEquals(BDGT_1_ID, bdgt.getID());
		
		assertEquals(4, bdgt.getAccounts().size()); // changed
		
		KMMBudgetAccount bdgtAcct = bdgt.getAccount(ACCT_1_ID);
		assertEquals(null, bdgtAcct); // sic
	}

	// ------------------------------
	// PART 4.2: Low-Level
	// ------------------------------

	// ::TODO

}
