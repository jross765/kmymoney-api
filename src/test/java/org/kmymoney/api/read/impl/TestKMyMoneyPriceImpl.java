package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPrice.Source;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyPriceImpl {
	public static final KMMPriceID PRC_1_ID = new KMMPriceID("E000001", "EUR", "2023-11-03"); // SAP/EUR
	public static final KMMPriceID PRC_2_ID = new KMMPriceID("E000001", "EUR", "2023-11-07"); // SAP/EUR
	public static final KMMPriceID PRC_3_ID = new KMMPriceID("E000002", "EUR", "2023-10-27"); // MBG/EUR
	public static final KMMPriceID PRC_4_ID = new KMMPriceID("E000002", "EUR", "2023-11-01"); // MBG/EUR
	public static final KMMPriceID PRC_5_ID = new KMMPriceID("E000002", "EUR", "2024-06-01"); // MBG/EUR
	public static final KMMPriceID PRC_6_ID = new KMMPriceID("USD", "EUR", "2023-12-04");

	public static final KMMPriceID PRC_12_ID = new KMMPriceID("E000001", "EUR", "2012-03-05"); // SAP/EUR

	public static final KMMPriceID PRC_14_ID = new KMMPriceID("E000003", "EUR", "2023-04-01"); // BASF/EUR
	public static final KMMPriceID PRC_15_ID = new KMMPriceID("E000003", "EUR", "2023-07-01"); // BASF/EUR
	public static final KMMPriceID PRC_16_ID = new KMMPriceID("E000003", "EUR", "2023-10-01"); // BASF/EUR

	public static final KMMPriceID PRC_17_ID = new KMMPriceID("USD", "EUR", "2023-12-01");
	public static final KMMPriceID PRC_18_ID = new KMMPriceID("USD", "EUR", "2024-01-01");
	public static final KMMPriceID PRC_19_ID = new KMMPriceID("USD", "EUR", "2023-11-01");
	
	// SAP SE
	public static final KMMSecID SEC_2_ID    = TestKMyMoneySecurityImpl.SEC_2_ID;
	public static final String   SEC_2_ISIN  = TestKMyMoneySecurityImpl.SEC_2_ISIN;

	// BASF SE
	public static final KMMSecID SEC_4_ID    = new KMMSecID("E000003");
	public static final String   SEC_4_ISIN  = "DE000BASF111";
	
	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyPrice prc = null;

	KMMQualifSecCurrID secID1 = null;
	KMMQualifSecCurrID secID2 = null;

	KMMQualifCurrID currID1 = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyPriceImpl.class);
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
		Collection<KMyMoneyPrice> prcColl = kmmFile.getPrices();
		List<KMyMoneyPrice> prcList = new ArrayList<KMyMoneyPrice>(prcColl);
		prcList.sort(Comparator.naturalOrder());

		//		System.err.println("=============");
		//		for ( GnuCashPrice prc : prcList ) {
		//			System.err.println(prc.toString());
		//		}
		//		System.err.println("=============");

		assertEquals(ConstTest.Stats.NOF_PRC, prcList.size());
		assertEquals(PRC_12_ID, prcList.get(0).getID());
		assertEquals(PRC_1_ID, prcList.get(1).getID());
		assertEquals(PRC_2_ID, prcList.get(2).getID());
		assertEquals(PRC_3_ID, prcList.get(3).getID());
		assertEquals(PRC_4_ID, prcList.get(4).getID());
		assertEquals(PRC_5_ID, prcList.get(5).getID());
	}

	@Test
	public void test01_1() throws Exception {
		prc = kmmFile.getPriceByID(PRC_1_ID);
		assertNotEquals(null, prc);

		assertEquals(PRC_1_ID, prc.getID());
		assertEquals(secID1.toString(), prc.getFromSecCurrQualifID().toString());
		assertEquals(secID1.toString(), prc.getFromSecurityQualifID().toString());
		assertEquals(secID1.getCode().toString(), prc.getFromSecurityQualifID().getSecID().toString());
		assertNotEquals(secID1.getCode(), prc.getFromSecurityQualifID().getSecID()); // sic
		assertNotEquals(secID1, prc.getFromSecurityQualifID()); // sic
		assertEquals("SAP AG", prc.getFromSecurity().getName());
		assertEquals("CURRENCY:EUR", prc.getToCurrencyQualifID().toString());
		assertEquals("EUR", prc.getToCurrencyCode());
		assertEquals("Transaction", ((KMyMoneyPriceImpl) prc).getSourceStr());
		assertEquals(Source.TRANSACTION, prc.getSource());
		assertEquals("2023-11-03", prc.getDateStr());
		assertEquals(LocalDate.of(2023, 11, 3), prc.getDate());
		assertEquals(120.0, prc.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);

		try {
			KMMQualifCurrID dummy = prc.getFromCurrencyQualifID(); // illegal call in this context
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
			KMyMoneyCurrency dummy = prc.getFromCurrency(); // illegal call in this context
			assertEquals(0, 1);
		} catch (Exception exc) {
			assertEquals(0, 0);
		}
	}

	@Test
	public void test01_2() throws Exception {
		prc = kmmFile.getPriceByID(PRC_4_ID);
		assertNotEquals(null, prc);

		assertEquals(PRC_4_ID, prc.getID());
		assertEquals(secID2.toString(), prc.getFromSecCurrQualifID().toString());
		assertEquals(secID2.toString(), prc.getFromSecurityQualifID().toString());
		assertEquals(secID2.getCode().toString(), prc.getFromSecurityQualifID().getSecID().toString());
		assertNotEquals(secID2.getCode(), prc.getFromSecurityQualifID().getSecID()); // sic
		assertNotEquals(secID2, prc.getFromSecurityQualifID()); // sic
		assertEquals("Mercedes-Benz Group AG", prc.getFromSecurity().getName());
		assertEquals("CURRENCY:EUR", prc.getToCurrencyQualifID().toString());
		assertEquals("EUR", prc.getToCurrencyCode());
		assertEquals("User", ((KMyMoneyPriceImpl) prc).getSourceStr());
		assertEquals(Source.USER, prc.getSource());
		assertEquals("2023-11-01", prc.getDateStr());
		assertEquals(LocalDate.of(2023, 11, 1), prc.getDate());
		assertEquals(116.5, prc.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);

		try {
			KMMQualifCurrID dummy = prc.getFromCurrencyQualifID(); // illegal call in this context
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
			KMyMoneyCurrency dummy = prc.getFromCurrency(); // illegal call in this context
			assertEquals(0, 1);
		} catch (Exception exc) {
			assertEquals(0, 0);
		}
	}

	@Test
	public void test01_3() throws Exception {
		prc = kmmFile.getPriceByID(PRC_6_ID);
		assertNotEquals(null, prc);

		assertEquals(PRC_6_ID, prc.getID());
		assertEquals(currID1.toString(), prc.getFromSecCurrQualifID().toString());
		assertEquals(currID1.toString(), prc.getFromCurrencyQualifID().toString());
		assertEquals("USD", prc.getFromCurrencyCode());
		assertEquals("CURRENCY:EUR", prc.getToCurrencyQualifID().toString());
		assertEquals("EUR", prc.getToCurrencyCode());
		assertEquals(Source.USER, prc.getSource());
		assertEquals("User", ((KMyMoneyPriceImpl) prc).getSourceStr());
		assertEquals("2023-12-04", prc.getDateStr());
		assertEquals(LocalDate.of(2023, 12, 4), prc.getDate());
		assertEquals(0.92, prc.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);

		try {
			KMMQualifSecID dummy = prc.getFromSecurityQualifID(); // illegal call in this context
			assertEquals(0, 1);
		} catch (Exception exc) {
			assertEquals(0, 0);
		}

		try {
			KMyMoneySecurity dummy = prc.getFromSecurity(); // illegal call in this context
			assertEquals(0, 1);
		} catch (Exception exc) {
			assertEquals(0, 0);
		}
	}

	// ---------------------------------------------------------------

	@Test
	public void test03_1() throws Exception {
		KMMQualifSecCurrID sec21ID = new KMMQualifSecCurrID(KMMQualifSecCurrID.Type.SECURITY, SEC_2_ID.toString());
		prc = kmmFile.getPriceByQualifSecCurrIDDate(sec21ID, LocalDate.of(2012, 3, 5));
		assertNotEquals(null, prc);
		assertEquals(PRC_12_ID, prc.getID());
		
		KMMQualifSecID sec22ID = new KMMQualifSecID(SEC_2_ID);
		prc = kmmFile.getPriceByQualifSecIDDate(sec22ID, LocalDate.of(2012, 3, 5));
		assertNotEquals(null, prc);
		assertEquals(PRC_12_ID, prc.getID());

		KMMSecID sec23ID = SEC_2_ID;
		prc = kmmFile.getPriceBySecIDDate(sec23ID, LocalDate.of(2012, 3, 5));
		assertNotEquals(null, prc);
		assertEquals(PRC_12_ID, prc.getID());
	}
	
	@Test
	public void test03_2() throws Exception {
		prc = kmmFile.getPriceBySecIDDate(SEC_4_ID, LocalDate.of(2023, 4, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_14_ID, prc.getID());
		
		prc = kmmFile.getPriceBySecIDDate(SEC_4_ID, LocalDate.of(2023, 7, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_15_ID, prc.getID());
		
		prc = kmmFile.getPriceBySecIDDate(SEC_4_ID, LocalDate.of(2023, 10, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_16_ID, prc.getID());
	}
	
	@Test
	public void test04_1() throws Exception {
		KMMQualifCurrID currID = new KMMQualifCurrID("USD");
		prc = kmmFile.getPriceByQualifCurrIDDate(currID, LocalDate.of(2023, 12, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_17_ID, prc.getID());
		
		Currency curr = Currency.getInstance("USD");
		prc = kmmFile.getPriceByCurrDate(curr, LocalDate.of(2023, 12, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_17_ID, prc.getID());
		
		prc = kmmFile.getPriceByQualifSecCurrIDDate(currID, LocalDate.of(2023, 12, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_17_ID, prc.getID());
	}
	
	@Test
	public void test04_2() throws Exception {
		Currency curr = Currency.getInstance("USD");
		prc = kmmFile.getPriceByCurrDate(curr, LocalDate.of(2024, 1, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_18_ID, prc.getID());
		
		prc = kmmFile.getPriceByCurrDate(curr, LocalDate.of(2023, 11, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_19_ID, prc.getID());
	}
}
