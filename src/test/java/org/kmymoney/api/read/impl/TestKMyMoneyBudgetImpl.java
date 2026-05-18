package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyBudgetImpl {
	public static final KMMBdgtID BDGT_1_ID = new KMMBdgtID("B000001");
	// public static final KMMBdgtID BDGT_2_ID = new KMMBdgtID("B000002");

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

		assertEquals(BDGT_1_ID, bdgt.getID());
		assertEquals("Budget 2026", bdgt.getName());
		assertEquals(LocalDate.of(2026, 1, 1), bdgt.getStart());
		
		assertEquals(5, bdgt.getAccounts().size());
		assertEquals("A000037", bdgt.getAccounts().get(0).getAcctID().toString());
		assertEquals("A000040", bdgt.getAccounts().get(1).getAcctID().toString());
		assertEquals("A000041", bdgt.getAccounts().get(2).getAcctID().toString());
		assertEquals("A000043", bdgt.getAccounts().get(3).getAcctID().toString());
		assertEquals("A000044", bdgt.getAccounts().get(4).getAcctID().toString());

		KMMBudgetAccount bdgtAcct = bdgt.getAccounts().get(0);
		assertEquals(KMMBudgetAccount.Level.MONTH_BY_MONTH, bdgtAcct.getLevel());
		assertEquals(2, bdgtAcct.getPeriods().size());
		
		bdgtAcct = bdgt.getAccounts().get(1);
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

}
