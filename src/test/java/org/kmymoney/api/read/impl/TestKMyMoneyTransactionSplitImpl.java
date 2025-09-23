package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyTransactionSplitImpl {
	public static final KMMComplAcctID ACCT_1_ID = TestKMyMoneyAccountImpl.ACCT_1_ID;
	public static final KMMComplAcctID ACCT_8_ID = TestKMyMoneyAccountImpl.ACCT_8_ID;

	public static final KMMTrxID TRX_01_ID = TestKMyMoneyTransactionImpl.TRX_1_ID;
	public static final KMMTrxID TRX_02_ID = TestKMyMoneyTransactionImpl.TRX_2_ID;
	public static final KMMTrxID TRX_04_ID = new KMMTrxID("T000000000000000004");
	public static final KMMTrxID TRX_06_ID = new KMMTrxID("T000000000000000006");
	public static final KMMTrxID TRX_07_ID = new KMMTrxID("T000000000000000007");
	public static final KMMTrxID TRX_08_ID = new KMMTrxID("T000000000000000008");
	public static final KMMTrxID TRX_10_ID = new KMMTrxID("T000000000000000010");
	public static final KMMTrxID TRX_15_ID = new KMMTrxID("T000000000000000015");
	public static final KMMTrxID TRX_18_ID = new KMMTrxID("T000000000000000018");

	public static final KMMQualifSpltID TRXSPLT_01_ID = new KMMQualifSpltID(TRX_01_ID.toString(), "S0001");
	public static final KMMQualifSpltID TRXSPLT_02_ID = new KMMQualifSpltID(TRX_02_ID.toString(), "S0001");
	public static final KMMQualifSpltID TRXSPLT_04_ID = new KMMQualifSpltID(TRX_04_ID.toString(), "S0001");
	public static final KMMQualifSpltID TRXSPLT_06_ID = new KMMQualifSpltID(TRX_06_ID.toString(), "S0001");
	public static final KMMQualifSpltID TRXSPLT_08_ID = new KMMQualifSpltID(TRX_08_ID.toString(), "S0001");
	public static final KMMQualifSpltID TRXSPLT_10_ID = new KMMQualifSpltID(TRX_10_ID.toString(), "S0001");
	public static final KMMQualifSpltID TRXSPLT_15_ID = new KMMQualifSpltID(TRX_15_ID.toString(), "S0001");
	public static final KMMQualifSpltID TRXSPLT_18_ID = new KMMQualifSpltID(TRX_18_ID.toString(), "S0003");

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyTransactionSplit splt = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyTransactionSplitImpl.class);
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
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_01_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_01_ID, splt.getQualifID());
		assertEquals(TRX_01_ID, splt.getTransactionID());
		assertEquals(ACCT_1_ID, splt.getAccountID());
		assertEquals(null, splt.getAction());
		assertEquals(10000.00, splt.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("10.000,00 €", splt.getValueFormatted()); // ::TODO: locale-specific!
		assertEquals(10000.00, splt.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("10.000,00 €", splt.getSharesFormatted());
		assertEquals("", splt.getMemo());
//		assertEquals(null, splt.getUserDefinedAttributeKeys());
	}

	@Test
	public void test02() throws Exception {
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_18_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_18_ID, splt.getQualifID());
		assertEquals(TRX_18_ID, splt.getTransactionID());
		assertEquals(ACCT_8_ID, splt.getAccountID());
		assertEquals(KMyMoneyTransactionSplit.Action.BUY_SHARES, splt.getAction());
		assertEquals(1800.00, splt.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("1.800,00 €", splt.getValueFormatted()); // ::TODO: locale-specific!
		assertEquals(15.00, splt.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("15 SECURITY:E000001", splt.getSharesFormatted());
		assertEquals("", splt.getMemo());
//		assertEquals(null, splt.getUserDefinedAttributeKeys());
	}

	@Test
	public void test03_1() throws Exception {
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_02_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_02_ID, splt.getQualifID());
		assertEquals("0123", splt.getNumber());
	}

	@Test
	public void test03_2() throws Exception {
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_04_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_04_ID, splt.getQualifID());
		assertEquals("x762d", splt.getNumber());
	}

	@Test
	public void test03_3() throws Exception {
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_06_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_06_ID, splt.getQualifID());
		assertEquals("/$@d", splt.getNumber());
	}

	@Test
	public void test03_4() throws Exception {
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_08_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_08_ID, splt.getQualifID());
		assertEquals("ÄÖÜß", splt.getNumber());
	}

	@Test
	public void test03_5() throws Exception {
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_10_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_10_ID, splt.getQualifID());
		assertEquals("<SPLIT>", splt.getNumber()); // sic, tried injection
	}

	@Test
	public void test03_6() throws Exception {
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_15_ID);
		assertNotEquals(null, splt);

		assertEquals(TRXSPLT_15_ID, splt.getQualifID());
		assertEquals("5298", splt.getNumber()); // sic, tried injection
	}

}
