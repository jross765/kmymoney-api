package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyBudgetImpl {
	public static final KMMBdgtID BDGT_1_ID = new KMMBdgtID("B000001");
	// public static final KMMBdgtID BDGT_2_ID = new KMMBdgtID("B000002");

	public static final KMMAcctID ACCT_1_ID = new KMMAcctID("A000037");
	public static final KMMAcctID ACCT_2_ID = new KMMAcctID("A000040");
	public static final KMMAcctID ACCT_3_ID = new KMMAcctID("A000041");
	public static final KMMAcctID ACCT_4_ID = new KMMAcctID("A000043");
	public static final KMMAcctID ACCT_5_ID = new KMMAcctID("A000044");

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyBudget bdgt = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyBudgetImpl.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.KMM_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		InputStream kmmFileStream = null;
		try {
			kmmFileStream = classLoader.getResourceAsStream(ConstTest.KMM_FILENAME);
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmFile = new KMyMoneyFileImpl(kmmFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------

	@Test
	public void test01_1() throws Exception {
		bdgt = kmmFile.getBudgetByID(BDGT_1_ID);
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
		assertEquals(KMMBudgetAccount.Level.MONTH_BY_MONTH, bdgtAcct.getLevel());
		// .
		assertEquals(2, bdgtAcct.getPeriods().size());
		assertEquals(LocalDate.of(2026, 3, 1), bdgtAcct.getPeriods().get(0).getStart());
		assertEquals(750.0, bdgtAcct.getPeriods().get(0).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("750,00 €", bdgtAcct.getPeriods().get(0).getAmountFormatted(Locale.GERMANY));
		assertEquals("€750.00", bdgtAcct.getPeriods().get(0).getAmountFormatted(Locale.US));
		// .
		assertEquals(LocalDate.of(2026, 7, 1), bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getStart());
		assertEquals(1500.0, bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("1.500,00 €", bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getAmountFormatted(Locale.GERMANY));
		assertEquals("€1,500.00", bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getAmountFormatted(Locale.US));
		
		bdgtAcct = bdgt.getAccounts().get(1);
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel());
		// .
		assertEquals(1, bdgtAcct.getPeriods().size());
		assertEquals(LocalDate.of(2026, 1, 1), bdgtAcct.getPeriods().get(0).getStart());
		assertEquals(100.0, bdgtAcct.getPeriods().get(0).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("100,00 €", bdgtAcct.getPeriods().get(0).getAmountFormatted(Locale.GERMANY));
		assertEquals("€100.00", bdgtAcct.getPeriods().get(0).getAmountFormatted(Locale.US));
		// .
		// The rest is redundant, because there is only one period for this account:
//		assertEquals(LocalDate.of(2026, 1, 1), bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getStart());
//		assertEquals(100.0, bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
//		assertEquals("100,00 €", bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getAmountFormatted(Locale.GERMANY));
//		assertEquals("€100.00", bdgtAcct.getPeriods().get(bdgtAcct.getPeriods().size() - 1).getAmountFormatted(Locale.US));
		
		bdgtAcct = bdgt.getAccounts().get(2);
		assertEquals(KMMBudgetAccount.Level.MONTHLY, bdgtAcct.getLevel());
		// .
		assertEquals(1, bdgtAcct.getPeriods().size());
		assertEquals(LocalDate.of(2026, 1, 1), bdgtAcct.getPeriods().get(0).getStart());
		assertEquals(800.0, bdgtAcct.getPeriods().get(0).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("800,00 €", bdgtAcct.getPeriods().get(0).getAmountFormatted(Locale.GERMANY));
		assertEquals("€800.00", bdgtAcct.getPeriods().get(0).getAmountFormatted(Locale.US));
		// .
		// The rest is redundant, cf. above
		
		bdgtAcct = bdgt.getAccounts().get(3);
		assertEquals(KMMBudgetAccount.Level.YEARLY, bdgtAcct.getLevel());
		// .
		assertEquals(1, bdgtAcct.getPeriods().size());
		assertEquals(LocalDate.of(2026, 1, 1), bdgtAcct.getPeriods().get(0).getStart());
		assertEquals(12000.0, bdgtAcct.getPeriods().get(0).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("12.000,00 €", bdgtAcct.getPeriods().get(0).getAmountFormatted(Locale.GERMANY));
		assertEquals("€12,000.00", bdgtAcct.getPeriods().get(0).getAmountFormatted(Locale.US));
		// .
		// The rest is redundant, cf. above
		
		bdgtAcct = bdgt.getAccounts().get(4);
		assertEquals(KMMBudgetAccount.Level.MONTHLY, bdgtAcct.getLevel());
		// .
		assertEquals(1, bdgtAcct.getPeriods().size());
		assertEquals(LocalDate.of(2026, 1, 1), bdgtAcct.getPeriods().get(0).getStart());
		assertEquals(200.0, bdgtAcct.getPeriods().get(0).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("200,00 €", bdgtAcct.getPeriods().get(0).getAmountFormatted(Locale.GERMANY));
		assertEquals("€200.00", bdgtAcct.getPeriods().get(0).getAmountFormatted(Locale.US));
		// .
		// The rest is redundant, cf. above
	}

}
