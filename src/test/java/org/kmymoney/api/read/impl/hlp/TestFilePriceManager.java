package org.kmymoney.api.read.impl.hlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;

import junit.framework.JUnit4TestAdapter;

public class TestFilePriceManager {

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
	public void test03() throws Exception {
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

}
