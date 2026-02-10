package org.kmymoney.api.read.impl.hlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.impl.TestKMyMoneyPriceImpl;
import org.kmymoney.api.read.impl.TestKMyMoneySecurityImpl;
import org.kmymoney.api.read.impl.hlp.fil.FilePriceManager;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import junit.framework.JUnit4TestAdapter;

public class TestFilePriceManager {

	private static final KMMPriceID PRC_1_ID = TestKMyMoneyPriceImpl.PRC_1_ID;
	private static final KMMPriceID PRC_2_ID = TestKMyMoneyPriceImpl.PRC_2_ID;
	private static final KMMPriceID PRC_3_ID = TestKMyMoneyPriceImpl.PRC_3_ID;
	private static final KMMPriceID PRC_4_ID = TestKMyMoneyPriceImpl.PRC_4_ID;
	private static final KMMPriceID PRC_5_ID = TestKMyMoneyPriceImpl.PRC_5_ID;
	private static final KMMPriceID PRC_6_ID = TestKMyMoneyPriceImpl.PRC_6_ID;

	private static final KMMPriceID PRC_12_ID = TestKMyMoneyPriceImpl.PRC_12_ID;

	private static final KMMPriceID PRC_14_ID = TestKMyMoneyPriceImpl.PRC_14_ID;
	private static final KMMPriceID PRC_15_ID = TestKMyMoneyPriceImpl.PRC_15_ID;
	private static final KMMPriceID PRC_16_ID = TestKMyMoneyPriceImpl.PRC_18_ID;

	private static final KMMPriceID PRC_17_ID = TestKMyMoneyPriceImpl.PRC_17_ID;
	private static final KMMPriceID PRC_18_ID = TestKMyMoneyPriceImpl.PRC_18_ID;
	private static final KMMPriceID PRC_19_ID = TestKMyMoneyPriceImpl.PRC_19_ID;
	
	// ---
	
	private static final KMMSecID SEC_1_ID = TestKMyMoneySecurityImpl.SEC_1_ID;
	private static final KMMQualifSecID SEC_1_QUALIF_ID = new KMMQualifSecID(SEC_1_ID); 
	
	// ---------------------------------------------------------------

	private KMyMoneyFileImplTestHelper kmmFile = null;

	private FilePriceManager mgr = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestFilePriceManager.class);
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
			kmmFile = new KMyMoneyFileImplTestHelper(kmmInFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney in-file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------
	
	@Test
	public void test01() throws Exception {
		mgr = kmmFile.getPriceManager();
		
		assertEquals(ConstTest.Stats.NOF_PRC, mgr.getNofEntriesPriceMap());
		assertEquals(ConstTest.Stats.NOF_PRC, mgr.getPrices().size());
	}

	@Test
	public void test02() throws Exception {
		mgr = kmmFile.getPriceManager();
		
		Collection<KMyMoneyPrice> prcColl = mgr.getPrices();
		KMMPriceID prcID = new KMMPriceID("E000001", "EUR", "2023-11-03");
		KMyMoneyPrice prc = mgr.getPriceByID(prcID);
		assertTrue(prcColl.contains(prc));
	}

	@Test
	public void test03_0() throws Exception {
		mgr = kmmFile.getPriceManager();
		
		// Normal case: Price (pair) exists
		KMMQualifSecCurrID qualifID = new KMMQualifSecCurrID(KMMQualifSecCurrID.Type.SECURITY, "E000003");
		KMMPricePairID prcPrID = new KMMPricePairID("E000003", "EUR");
		LocalDate date = LocalDate.parse("2023-12-01");
		KMMPriceID prcID = new KMMPriceID("E000003", "EUR", "2023-12-01");
		KMyMoneyPrice prc = mgr.getPriceByQualifSecCurrIDDate(qualifID, date);
		assertNotEquals(null, prc);
		assertEquals(prcID, prc.getID());
		
		// Price pair exists, but not price ==> getPriceByQualifSecCurrIDDate( )returns null:
		KMyMoneyPricePair prcPr = kmmFile.getPricePairByID(prcPrID);
		assertNotEquals(null, prcPr);
		date = LocalDate.parse("2026-01-08");
		prcID = new KMMPriceID("E000003", "EUR", "2026-01-08");
		prc = mgr.getPriceByQualifSecCurrIDDate(qualifID, date);
		assertEquals(null, prc);
		
		// Price pair does not exist ==> getPriceByQualifSecCurrIDDate() returns null:
		qualifID = new KMMQualifSecCurrID(KMMQualifSecCurrID.Type.SECURITY, "E000004");
		prcPrID = new KMMPricePairID("E000004", "EUR");
		prcPr = kmmFile.getPricePairByID(prcPrID);
		assertEquals(null, prcPr);
		date = LocalDate.parse("2026-01-01");
		prcID = new KMMPriceID("E000004", "EUR", "2026-01-01");
		prc = mgr.getPriceByQualifSecCurrIDDate(qualifID, date);
		assertEquals(null, prc);
	}

	@Test
	public void test03_1() throws Exception {
		mgr = kmmFile.getPriceManager();
		
		KMMQualifSecID cmdtyID = new KMMQualifSecID(SEC_1_ID);
		assertEquals(58.25, mgr.getLatestPrice(cmdtyID).doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(233,   mgr.getLatestPriceRat(cmdtyID).getNumerator().intValue());
		assertEquals(4,     mgr.getLatestPriceRat(cmdtyID).getDenominator().intValue());
	}

	@Test
	public void test03_2() throws Exception {
		mgr = kmmFile.getPriceManager();
		
		KMMQualifCurrID currID = new KMMQualifCurrID(Currency.getInstance("EUR"));
		assertEquals(null, mgr.getLatestPrice(currID));    // ::CHECK
		assertEquals(null, mgr.getLatestPriceRat(currID)); // ::CHECK
		
		currID = new KMMQualifCurrID(Currency.getInstance("USD"));
		assertEquals(0.93, mgr.getLatestPrice(currID).doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(93,   mgr.getLatestPriceRat(currID).getNumerator().intValue());
		assertEquals(100,  mgr.getLatestPriceRat(currID).getDenominator().intValue());
	}

	@Test
	public void test03_3() throws Exception {
		mgr = kmmFile.getPriceManager();
		
		assertEquals(58.25, mgr.getLatestPrice(SEC_1_QUALIF_ID).doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(233,   mgr.getLatestPriceRat(SEC_1_QUALIF_ID).getNumerator().intValue());
		assertEquals(4,     mgr.getLatestPriceRat(SEC_1_QUALIF_ID).getDenominator().intValue());

		assertEquals(null, mgr.getLatestPrice(KMMQualifSecCurrID.Type.CURRENCY, "EUR"));    // ::CHECK
		assertEquals(null, mgr.getLatestPriceRat(KMMQualifSecCurrID.Type.CURRENCY, "EUR")); // ::CHECK
		
		assertEquals(0.93, mgr.getLatestPrice(KMMQualifSecCurrID.Type.CURRENCY, "USD").doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(93,   mgr.getLatestPriceRat(KMMQualifSecCurrID.Type.CURRENCY, "USD").getNumerator().intValue());
		assertEquals(100,  mgr.getLatestPriceRat(KMMQualifSecCurrID.Type.CURRENCY, "USD").getDenominator().intValue());
	}

	@Test
	public void test04() throws Exception {
		mgr = kmmFile.getPriceManager();
		
		KMyMoneyPrice prc = mgr.getPriceByID(PRC_18_ID);
		assertEquals("CURRENCY:USD", prc.getFromSecCurrQualifID().toString());
		assertEquals("CURRENCY:EUR", prc.getToCurrencyQualifID().toString());
		assertEquals(0.93, prc.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(93,   prc.getValueRat().getNumerator().intValue());
		assertEquals(100,  prc.getValueRat().getDenominator().intValue());
		
		prc = mgr.getPriceByID(PRC_5_ID);
		assertEquals("SECURITY:E000002", prc.getFromSecCurrQualifID().toString());
		assertEquals("CURRENCY:EUR", prc.getToCurrencyQualifID().toString());
		assertEquals(58.25, prc.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(233,   prc.getValueRat().getNumerator().intValue());
		assertEquals(4,     prc.getValueRat().getDenominator().intValue());
	}

}
