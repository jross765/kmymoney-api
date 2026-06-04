package org.kmymoney.api.read.impl.aux;

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
import org.kmymoney.api.read.aux.KMMBudgetPeriod;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyBudgetImpl;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;

import junit.framework.JUnit4TestAdapter;

public class TestKMMBudgetAccountImpl {
	public static final KMMBdgtID BDGT_1_ID = TestKMyMoneyBudgetImpl.BDGT_1_ID;

	private static final KMMAcctID ACCT_1_ID = TestKMyMoneyBudgetImpl.ACCT_1_ID;

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyBudget bdgt = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMMBudgetAccountImpl.class);
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
	public void test01() throws Exception {
		bdgt = kmmFile.getBudgetByID(BDGT_1_ID);
		assertNotEquals(null, bdgt);
		assertEquals(BDGT_1_ID, bdgt.getID());
		
		KMMBudgetAccount bdgtAcct = bdgt.getAccounts().get(0);
		assertNotEquals(null, bdgtAcct);
		assertEquals(bdgt, bdgtAcct.getParent());
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

}
