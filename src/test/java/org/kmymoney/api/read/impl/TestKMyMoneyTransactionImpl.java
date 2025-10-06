package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyTransactionImpl {
	public static final KMMTrxID TRX_1_ID = new KMMTrxID("T000000000000000001");
	public static final KMMTrxID TRX_2_ID = new KMMTrxID("T000000000000000002");

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyTransaction trx = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyTransactionImpl.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.GCSH_FILENAME);
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
		trx = kmmFile.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		assertEquals(TRX_1_ID, trx.getID());
		assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE);
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
		trx = kmmFile.getTransactionByID(TRX_2_ID);
		assertNotEquals(null, trx);

		assertEquals(TRX_2_ID, trx.getID());
		assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("", trx.getMemo());
		assertEquals("2023-01-03", trx.getDatePosted().toString());
		assertEquals("2023-01-03", trx.getDatePostedFormatted());
		assertEquals("2023-10-14", trx.getDateEntered().toString());
		assertEquals("2023-10-14", trx.getDateEnteredFormatted());

		assertEquals(2, trx.getSplitsCount());
		assertEquals("S0001", trx.getSplits().get(0).getID().toString());
		assertEquals("S0002", trx.getSplits().get(1).getID().toString());
	}
}
