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

public class TestKMMWritableBudgetPeriodImpl {
	private static final KMMBdgtID BDGT_1_ID = TestKMyMoneyWritableBudgetImpl.BDGT_1_ID;

	private static final KMMAcctID ACCT_1_ID = TestKMyMoneyWritableBudgetImpl.ACCT_1_ID;

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
		return new JUnit4TestAdapter(TestKMMWritableBudgetPeriodImpl.class);
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
		assertEquals(bdgt, bdgtAcct.getParent());
		
		assertEquals(2, bdgtAcct.getPeriods().size());
		
		KMMWritableBudgetPeriod bdgtPrd = bdgtAcct.getWritablePeriods().get(0);
		assertNotEquals(null, bdgtPrd);
		assertEquals(bdgt, bdgtAcct.getParent());
		
		assertEquals(LocalDate.of(2026, 3, 1), bdgtPrd.getStart());
		assertEquals("2026-03-01", bdgtPrd.getStartStr());
		assertEquals(750.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("750,00 €", bdgtPrd.getAmountFormatted(Locale.GERMANY));
		assertEquals("€750.00", bdgtPrd.getAmountFormatted(Locale.US));
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
		
		KMMWritableBudgetPeriod bdgtPrd = bdgtAcct.getWritablePeriods().get(0);
		assertNotEquals(null, bdgtPrd);
		assertEquals(bdgtAcct, bdgtPrd.getParent());
		
		// ----------------------------
		// Modify the object

		try {
			bdgtPrd.setStart(LocalDate.of(2020, 1, 1));
			assertEquals(0, 1);
		} catch ( IllegalArgumentException | IllegalStateException exc ) {
			assertEquals(0, 0);
		}
		
		bdgtPrd.setStart(LocalDate.of(2026, 4, 1));

		try {
			bdgtPrd.setAmount(BigFraction.of(-3, 1));
			assertEquals(0, 1);
		} catch ( IllegalArgumentException | IllegalStateException exc ) {
			assertEquals(0, 0);
		}
		
		bdgtPrd.setAmount(BigFraction.of(12345, 100));

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(bdgtPrd);

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

	private void test02_1_check_memory(KMMWritableBudgetPeriod bdgtPrd) throws Exception {
		assertEquals(LocalDate.of(2026, 4, 1), bdgtPrd.getStart()); // changed
		assertEquals(BigFraction.of(12345, 100), bdgtPrd.getAmount()); // changed
		assertEquals("123,45 €", bdgtPrd.getAmountFormatted(Locale.GERMANY)); // changed
		assertEquals("€123.45", bdgtPrd.getAmountFormatted(Locale.US)); // changed
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		
		KMyMoneyBudget bdgt = kmmOutFile.getBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);
		assertEquals(BDGT_1_ID, bdgt.getID());
		
		KMMBudgetAccount bdgtAcct = bdgt.getAccounts().get(0);
		assertNotEquals(null, bdgtAcct);
		assertEquals(ACCT_1_ID, bdgtAcct.getAcctID());
		
		assertEquals(2, bdgtAcct.getPeriods().size()); // unchanged
		KMMBudgetPeriod bdgtPrd = bdgtAcct.getPeriods().get(0);
		assertNotEquals(null, bdgtPrd);
		assertEquals(bdgtAcct, bdgtPrd.getParent()); // unchanged
		
		assertEquals(LocalDate.of(2026, 4, 1), bdgtPrd.getStart()); // changed
		assertEquals(BigFraction.of(12345, 100), bdgtPrd.getAmount()); // changed
		assertEquals("123,45 €", bdgtPrd.getAmountFormatted(Locale.GERMANY)); // changed
		assertEquals("€123.45", bdgtPrd.getAmountFormatted(Locale.US)); // changed
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
		
		KMMWritableBudgetAccount bdgtAcct = bdgt.getWritableAccounts().get(0);
		assertNotEquals(null, bdgtAcct);
		assertEquals(ACCT_1_ID, bdgtAcct.getAcctID());
		
		assertEquals(2, bdgtAcct.getPeriods().size());
		
		// ----------------------------
		// Bare naked object

		KMMWritableBudgetPeriod bdgtPrd = null;
		try {
			bdgtPrd = bdgtAcct.createWritablePeriod(LocalDate.of(2020, 1, 1), BigFraction.of(12345, 100));
			assertEquals(0, 1);
		} catch ( IllegalArgumentException | IllegalStateException exc ) {
			assertEquals(0, 0);
		}

		try {
			bdgtPrd = bdgtAcct.createWritablePeriod(LocalDate.of(2026, 4, 1), BigFraction.of(-3, 1));
			assertEquals(0, 1);
		} catch ( IllegalArgumentException | IllegalStateException exc ) {
			assertEquals(0, 0);
		}

		bdgtPrd = bdgtAcct.createWritablePeriod(LocalDate.of(2026, 4, 1), BigFraction.of(12345, 100));
		assertNotEquals(null, bdgtPrd);
		
		// ----------------------------
		// Check whether the object has actually been modified
		// (in memory, not in the file yet).

		test03_1_check_memory(bdgtAcct, bdgtPrd);

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

	private void test03_1_check_memory(KMMBudgetAccount bdgtAcct, 
									   KMMWritableBudgetPeriod bdgtPrd) throws Exception {
		assertEquals(3, bdgtAcct.getPeriods().size()); // changed
		
		assertEquals(bdgtAcct, bdgtPrd.getParent());
		assertEquals(LocalDate.of(2026, 4, 1), bdgtPrd.getStart());
		assertEquals(BigFraction.of(12345, 100), bdgtPrd.getAmount());
		assertEquals("123,45 €", bdgtPrd.getAmountFormatted(Locale.GERMANY));
		assertEquals("€123.45", bdgtPrd.getAmountFormatted(Locale.US));
	}

	private void test03_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		
		KMyMoneyBudget bdgt = kmmOutFile.getBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);
		assertEquals(BDGT_1_ID, bdgt.getID());
		
		KMMBudgetAccount bdgtAcct = bdgt.getAccounts().get(0);
		assertNotEquals(null, bdgtAcct);
		assertEquals(ACCT_1_ID, bdgtAcct.getAcctID());
		
		assertEquals(3, bdgtAcct.getPeriods().size()); // changed
		
		KMMBudgetPeriod bdgtPrd = bdgtAcct.getPeriods().get( bdgtAcct.getPeriods().size() - 1 );
		assertNotEquals(null, bdgtPrd);
		
		// ---
		
		assertEquals(bdgtAcct, bdgtPrd.getParent());
		assertEquals(LocalDate.of(2026, 4, 1), bdgtPrd.getStart());
		assertEquals(BigFraction.of(12345, 100), bdgtPrd.getAmount());
		assertEquals("123,45 €", bdgtPrd.getAmountFormatted(Locale.GERMANY));
		assertEquals("€123.45", bdgtPrd.getAmountFormatted(Locale.US));
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
		
		KMMWritableBudgetAccount bdgtAcct = bdgt.getWritableAccounts().get(0);
		assertNotEquals(null, bdgtAcct);
		assertEquals(ACCT_1_ID, bdgtAcct.getAcctID());
		
		assertEquals(2, bdgtAcct.getPeriods().size());
		
		// ----------------------------
		// Delete the object

		KMMWritableBudgetPeriod bdgtPrd = bdgtAcct.getWritablePeriods().get(0);
		assertNotEquals(null, bdgtPrd);
		bdgtAcct.removePeriod(bdgtPrd);
		
		// ----------------------------
		// Check whether the objects have actually been deleted
		// (in memory, not in the file yet).

		test04_1_check_memory(bdgtAcct, bdgtPrd);

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

	private void test04_1_check_memory(KMMWritableBudgetAccount bdgtAcct,
									   KMMWritableBudgetPeriod bdgtPrd) throws Exception {
		assertEquals(1, bdgtAcct.getPeriods().size());
		
		// CAUTION / ::TODO
		// Old Object still exists and is unchanged
		assertEquals(LocalDate.of(2026, 3, 1), bdgtPrd.getStart()); // unchanged
		assertEquals(BigFraction.of(750), bdgtPrd.getAmount()); // unchanged
		
		// However, the budget period cannot newly be instantiated any more,
		// just as you would expect.
		KMMWritableBudgetPeriod bdgtPrdOther = bdgtAcct.getWritablePeriods().getFirst(); // the only remaining one
		// Iterate through all remaining periods in order to prove
		// that the one we just removed really is not there any more.
		// (Well, in this particular case, it's just one remaining period,
		// anyway, but we'll keep it like that for symmetry w/ the sister
		// project.)
		for ( KMMWritableBudgetPeriod bdgtPrd2 : bdgtAcct.getWritablePeriods() ) {
			assertNotEquals(LocalDate.of(2026, 3, 1), bdgtPrd2.getStart());
		}
		// Rest: Nothing to do
	}

	private void test04_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		
		// The budget period does not exist any more, just as you would expect.
		// However, no exception is thrown, as opposed to test04_1_check_memory()
		KMyMoneyBudget bdgt = kmmInFile.getBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);
		assertEquals(BDGT_1_ID, bdgt.getID());
		
		KMMBudgetAccount bdgtAcct = bdgt.getAccount(ACCT_1_ID);
		assertNotEquals(null, bdgtAcct);
		
		assertEquals(1, bdgtAcct.getPeriods().size()); // changed
		
		// Iterate through all remaining periods in order to prove
		// that the one we just removed really is not there any more.
		// (Well, in this particular case, it's just one remaining period,
		// anyway, but we'll keep it like that for symmetry w/ the sister
		// project.)
		for ( KMMBudgetPeriod bdgtPrd2 : bdgtAcct.getPeriods() ) {
			assertNotEquals(LocalDate.of(2026, 3, 1), bdgtPrd2.getStart());
		}
		// Rest: Nothing to do
	}

	// ------------------------------
	// PART 4.2: Low-Level
	// ------------------------------

	// ::TODO

}
