package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyPricePairImpl {
	public static final KMMPricePairID PRCPR_1_ID = new KMMPricePairID("E000001", "EUR"); // SAP/EUR
	public static final KMMPricePairID PRCPR_2_ID = new KMMPricePairID("E000002", "EUR"); // MBG/EUR
	public static final KMMPricePairID PRCPR_3_ID = new KMMPricePairID("USD", "EUR");
	public static final KMMPricePairID PRCPR_4_ID = new KMMPricePairID("E000003", "EUR"); // BASF/EUR

	public static final KMMPriceID PRC_1_ID = TestKMyMoneyPriceImpl.PRC_1_ID;
	public static final KMMPriceID PRC_2_ID = TestKMyMoneyPriceImpl.PRC_2_ID;
	public static final KMMPriceID PRC_3_ID = TestKMyMoneyPriceImpl.PRC_3_ID;
	public static final KMMPriceID PRC_4_ID = TestKMyMoneyPriceImpl.PRC_4_ID;
	public static final KMMPriceID PRC_5_ID = TestKMyMoneyPriceImpl.PRC_5_ID;
	public static final KMMPriceID PRC_6_ID = TestKMyMoneyPriceImpl.PRC_6_ID;
	public static final KMMPriceID PRC_12_ID = TestKMyMoneyPriceImpl.PRC_12_ID;
	public static final KMMPriceID PRC_17_ID = TestKMyMoneyPriceImpl.PRC_17_ID;
	public static final KMMPriceID PRC_18_ID = TestKMyMoneyPriceImpl.PRC_18_ID;
	public static final KMMPriceID PRC_19_ID = TestKMyMoneyPriceImpl.PRC_19_ID;

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyPrice prc = null;
	private KMyMoneyPricePair prcPr = null;

	KMMQualifSecCurrID secID1 = null;
	KMMQualifSecCurrID secID2 = null;

	KMMQualifCurrID currID1 = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyPricePairImpl.class);
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

		// ---

		secID1 = new KMMQualifSecCurrID(KMMQualifSecCurrID.Type.SECURITY, "E000001");
		secID2 = new KMMQualifSecCurrID(KMMQualifSecCurrID.Type.SECURITY, "E000002");

		currID1 = new KMMQualifCurrID("USD");
	}

	// -----------------------------------------------------------------

	@Test
	public void test01() throws Exception {
		Collection<KMyMoneyPricePair> prcPrColl = kmmFile.getPricePairs();
		List<KMyMoneyPricePair> prcPrList = new ArrayList<KMyMoneyPricePair>(prcPrColl);
		prcPrList.sort(Comparator.naturalOrder());

		assertEquals(4, prcPrList.size());
		assertEquals(PRCPR_1_ID, prcPrList.get(0).getID());
		assertEquals(PRCPR_2_ID, prcPrList.get(1).getID());
		assertEquals(PRCPR_4_ID, prcPrList.get(2).getID());
		assertEquals(PRCPR_3_ID, prcPrList.get(3).getID());
	}

	@Test
	public void test01_1() throws Exception {
		prc = kmmFile.getPriceByID(TestKMyMoneyPriceImpl.PRC_1_ID);
		assertNotEquals(null, prc);
		prcPr = prc.getParentPricePair();
		assertNotEquals(null, prcPr);

		assertEquals(PRCPR_1_ID, prcPr.getID());
		assertEquals(secID1.toString(), prcPr.getFromSecCurrQualifID().toString());
		assertEquals(secID1.toString(), prcPr.getFromSecurityQualifID().toString());
		assertEquals(secID1.getCode().toString(), prcPr.getFromSecurityQualifID().getSecID().toString());
		assertNotEquals(secID1.getCode(), prcPr.getFromSecurityQualifID().getSecID()); // sic
		assertNotEquals(secID1, prcPr.getFromSecurityQualifID()); // sic
		assertEquals("SAP AG", prcPr.getFromSecurity().getName());
		assertEquals("CURRENCY:EUR", prcPr.getToCurrencyQualifID().toString());
		assertEquals("EUR", prcPr.getToCurrencyCode());

		try {
			KMMQualifCurrID dummy = prcPr.getFromCurrencyQualifID(); // illegal call in this context
			assertEquals(0, 1);
		} catch (Exception exc) {
			assertEquals(0, 0);
		}

		try {
			String dummy = prc.getFromCurrencyCode(); // illegal call in this context
			assertEquals(0, 1);
		} catch (Exception exc) {
			assertEquals(0, 0);
		}

		try {
			KMyMoneyCurrency dummy = prcPr.getFromCurrency(); // illegal call in this context
			assertEquals(0, 1);
		} catch (Exception exc) {
			assertEquals(0, 0);
		}
		
		assertEquals(3, prcPr.getPrices().size());
		assertEquals(PRC_12_ID, prcPr.getPrices().get(0).getID());
		assertEquals(PRC_1_ID, prcPr.getPrices().get(1).getID());
		assertEquals(PRC_2_ID, prcPr.getPrices().get(2).getID());
	}

	@Test
	public void test01_2() throws Exception {
		prc = kmmFile.getPriceByID(TestKMyMoneyPriceImpl.PRC_3_ID);
		assertNotEquals(null, prc);
		prcPr = prc.getParentPricePair();
		assertNotEquals(null, prcPr);

		assertEquals(PRCPR_2_ID, prcPr.getID());
		assertEquals(secID2.toString(), prcPr.getFromSecCurrQualifID().toString());
		assertEquals(secID2.toString(), prcPr.getFromSecurityQualifID().toString());
		assertEquals(secID2.getCode().toString(), prcPr.getFromSecurityQualifID().getSecID().toString());
		assertNotEquals(secID2.getCode(), prcPr.getFromSecurityQualifID().getSecID()); // sic
		assertNotEquals(secID2, prcPr.getFromSecurityQualifID()); // sic
		assertEquals("Mercedes-Benz Group AG", prcPr.getFromSecurity().getName());
		assertEquals("CURRENCY:EUR", prcPr.getToCurrencyQualifID().toString());
		assertEquals("EUR", prcPr.getToCurrencyCode());

		try {
			KMMQualifCurrID dummy = prcPr.getFromCurrencyQualifID(); // illegal call in this context
			assertEquals(0, 1);
		} catch (Exception exc) {
			assertEquals(0, 0);
		}

		try {
			String dummy = prcPr.getFromCurrencyCode(); // illegal call in this context
			assertEquals(0, 1);
		} catch (Exception exc) {
			assertEquals(0, 0);
		}

		try {
			KMyMoneyCurrency dummy = prcPr.getFromCurrency(); // illegal call in this context
			assertEquals(0, 1);
		} catch (Exception exc) {
			assertEquals(0, 0);
		}
		
		assertEquals(3, prcPr.getPrices().size());
		assertEquals(PRC_3_ID, prcPr.getPrices().get(0).getID());
		assertEquals(PRC_4_ID, prcPr.getPrices().get(1).getID());
		assertEquals(PRC_5_ID, prcPr.getPrices().get(2).getID());
	}

	@Test
	public void test01_3() throws Exception {
		prc = kmmFile.getPriceByID(TestKMyMoneyPriceImpl.PRC_6_ID);
		assertNotEquals(null, prc);
		prcPr = prc.getParentPricePair();
		assertNotEquals(null, prcPr);

		assertEquals(PRCPR_3_ID, prcPr.getID());
		assertEquals(currID1.toString(), prcPr.getFromSecCurrQualifID().toString());
		assertEquals(currID1.toString(), prcPr.getFromCurrencyQualifID().toString());
		assertEquals("USD", prcPr.getFromCurrencyCode());
		assertEquals("CURRENCY:EUR", prcPr.getToCurrencyQualifID().toString());
		assertEquals("EUR", prcPr.getToCurrencyCode());

		try {
			KMMQualifSecID dummy = prcPr.getFromSecurityQualifID(); // illegal call in this context
			assertEquals(0, 1);
		} catch (Exception exc) {
			assertEquals(0, 0);
		}

		try {
			KMyMoneySecurity dummy = prcPr.getFromSecurity(); // illegal call in this context
			assertEquals(0, 1);
		} catch (Exception exc) {
			assertEquals(0, 0);
		}
		
		assertEquals(4, prcPr.getPrices().size());
		assertEquals(PRC_19_ID, prcPr.getPrices().get(0).getID());
		assertEquals(PRC_17_ID, prcPr.getPrices().get(1).getID());
		assertEquals(PRC_6_ID, prcPr.getPrices().get(2).getID());
		assertEquals(PRC_18_ID, prcPr.getPrices().get(3).getID());
	}

}
