package org.kmymoney.api.read.impl.aux;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.aux.KMMBudgetPeriod;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyBudgetImpl;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;

import junit.framework.JUnit4TestAdapter;

public class TestKMMBudgetPeriodImpl {
	public static final KMMBdgtID BDGT_1_ID = TestKMyMoneyBudgetImpl.BDGT_1_ID;
	// public static final KMMBdgtID BDGT_2_ID = new KMMBdgtID("xyz");

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyBudget bdgt = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMMBudgetPeriodImpl.class);
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
		
		assertEquals(2, bdgtAcct.getPeriods().size());
		
		KMMBudgetPeriod bdgtPrd = bdgtAcct.getPeriods().get(0);
		assertNotEquals(null, bdgtPrd);
		assertEquals(bdgt, bdgtAcct.getParent());
		
		assertEquals(LocalDate.of(2026, 3, 1), bdgtPrd.getStart());
		assertEquals("2026-03-01", bdgtPrd.getStartStr());
		assertEquals(750.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("750,00 €", bdgtPrd.getAmountFormatted());

		bdgtPrd = bdgtAcct.getPeriods().get(1);
		assertNotEquals(null, bdgtPrd);
		assertEquals(bdgt, bdgtAcct.getParent());
		
		assertEquals(LocalDate.of(2026, 7, 1), bdgtPrd.getStart());
		assertEquals("2026-07-01", bdgtPrd.getStartStr());
		assertEquals(1500.0, bdgtPrd.getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("1.500,00 €", bdgtPrd.getAmountFormatted());
	}

}
