package org.kmymoney.api.write.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPrice.Source;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneyPriceImpl;
import org.kmymoney.api.read.impl.KMyMoneyPricePairImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyPriceImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyPricePairImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.read.impl.hlp.FileStats;
import org.kmymoney.api.write.KMyMoneyWritablePrice;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import junit.framework.JUnit4TestAdapter;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TestKMyMoneyWritablePriceImpl {
	private static final KMMPriceID PRC_1_ID = TestKMyMoneyPriceImpl.PRC_1_ID;
	private static final KMMPriceID PRC_2_ID = TestKMyMoneyPriceImpl.PRC_2_ID;
	private static final KMMPriceID PRC_3_ID = TestKMyMoneyPriceImpl.PRC_3_ID;
	private static final KMMPriceID PRC_4_ID = TestKMyMoneyPriceImpl.PRC_4_ID;
	private static final KMMPriceID PRC_5_ID = TestKMyMoneyPriceImpl.PRC_5_ID;
	private static final KMMPriceID PRC_6_ID = TestKMyMoneyPriceImpl.PRC_6_ID;

	private static final KMMPriceID PRC_12_ID = TestKMyMoneyPriceImpl.PRC_12_ID;

	private static final KMMPriceID PRC_14_ID = TestKMyMoneyPriceImpl.PRC_14_ID;
	private static final KMMPriceID PRC_15_ID = TestKMyMoneyPriceImpl.PRC_15_ID;
	private static final KMMPriceID PRC_16_ID = TestKMyMoneyPriceImpl.PRC_16_ID;

	private static final KMMPriceID PRC_17_ID = TestKMyMoneyPriceImpl.PRC_17_ID;
	private static final KMMPriceID PRC_18_ID = TestKMyMoneyPriceImpl.PRC_18_ID;
	private static final KMMPriceID PRC_19_ID = TestKMyMoneyPriceImpl.PRC_19_ID;
	
	// SAP SE
	private static final KMMSecID SEC_2_ID    = TestKMyMoneyPriceImpl.SEC_2_ID;
	private static final String   SEC_2_ISIN  = TestKMyMoneyPriceImpl.SEC_2_ISIN;

	// BASF SE
	private static final KMMSecID SEC_4_ID    = TestKMyMoneyPriceImpl.SEC_4_ID;
	private static final String   SEC_4_ISIN  = TestKMyMoneyPriceImpl.SEC_4_ISIN;
	
	
	
	private static final KMMPricePairID PRCPR_1_ID = TestKMyMoneyPricePairImpl.PRCPR_1_ID;
	private static final KMMPricePairID PRCPR_2_ID = TestKMyMoneyPricePairImpl.PRCPR_2_ID;
	private static final KMMPricePairID PRCPR_3_ID = TestKMyMoneyPricePairImpl.PRCPR_3_ID;

	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

	private KMMFileStats kmmInFileStats = null;
	private KMMFileStats kmmOutFileStats = null;

	KMMQualifSecCurrID secID1 = null;
	KMMQualifSecCurrID secID2 = null;

	KMMQualifCurrID currID1 = null;

	private KMMPriceID newID = null;

	// https://stackoverflow.com/questions/11884141/deleting-file-and-directory-in-junit
	@SuppressWarnings("exports")
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyWritablePriceImpl.class);
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
			kmmInFile = new KMyMoneyWritableFileImpl(kmmInFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney in-file");
			exc.printStackTrace();
		}

		// ---

		secID1 = new KMMQualifSecCurrID(KMMQualifSecCurrID.Type.SECURITY, "E000001");
		secID2 = new KMMQualifSecCurrID(KMMQualifSecCurrID.Type.SECURITY, "E000002");

		currID1 = new KMMQualifCurrID("USD");

		newID = new KMMPriceID("EUR", "EUR", "1970-01-01"); // dummy
	}

	// -----------------------------------------------------------------
	// PART 1: Read existing objects as modifiable ones
	// (and see whether they are fully symmetrical to their read-only
	// counterparts)
	// -----------------------------------------------------------------
	// Cf. TestKMyMoneyPriceImpl.test01_xyz
	//
	// Check whether the KMyMoneyWritablePrice objects returned by
	// KMyMoneyWritableFileImpl.getWritablePriceByID() are actually
	// complete (as complete as returned be KMyMoneyFileImpl.getPriceByID().

	@Test
	public void test01_1() throws Exception {
		Collection<KMyMoneyWritablePrice> prcColl = kmmInFile.getWritablePrices();
		List<KMyMoneyWritablePrice> prcList = new ArrayList<KMyMoneyWritablePrice>(prcColl);
		prcList.sort(Comparator.naturalOrder());

		assertEquals(ConstTest.Stats.NOF_PRC, prcList.size());
		assertEquals(PRC_12_ID, prcList.get(0).getID());
		assertEquals(PRC_1_ID, prcList.get(1).getID());
		assertEquals(PRC_2_ID, prcList.get(2).getID());
		assertEquals(PRC_3_ID, prcList.get(3).getID());
		assertEquals(PRC_4_ID, prcList.get(4).getID());
		assertEquals(PRC_5_ID, prcList.get(5).getID());
	}

	@Test
	public void test01_1_1() throws Exception {
		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceByID(PRC_1_ID);
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
		assertEquals(Source.TRANSACTION, prc.getSource()); // unchanged
		assertEquals("Transaction", ((KMyMoneyWritablePriceImpl) prc).getSourceStr()); // unchanged
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
	public void test01_1_2() throws Exception {
		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceByID(PRC_4_ID);
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
		assertEquals(Source.USER, prc.getSource());
		assertEquals("User", ((KMyMoneyWritablePriceImpl) prc).getSourceStr());
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
	public void test01_1_3() throws Exception {
		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceByID(PRC_6_ID);
		assertNotEquals(null, prc);

		assertEquals(PRC_6_ID, prc.getID());
		assertEquals(currID1.toString(), prc.getFromSecCurrQualifID().toString());
		assertEquals(currID1.toString(), prc.getFromCurrencyQualifID().toString());
		assertEquals("USD", prc.getFromCurrencyCode());
		assertEquals("CURRENCY:EUR", prc.getToCurrencyQualifID().toString());
		assertEquals("EUR", prc.getToCurrencyCode());
		assertEquals(Source.USER, prc.getSource());
		assertEquals("User", ((KMyMoneyWritablePriceImpl) prc).getSourceStr());
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
	public void test01_3_1() throws Exception {
		KMMQualifSecCurrID sec21ID = new KMMQualifSecCurrID(KMMQualifSecCurrID.Type.SECURITY, SEC_2_ID.toString());
		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceByQualifSecCurrIDDate(sec21ID, LocalDate.of(2012, 3, 5));
		assertNotEquals(null, prc);
		assertEquals(PRC_12_ID, prc.getID());
		
		KMMQualifSecID sec22ID = new KMMQualifSecID(SEC_2_ID);
		prc = kmmInFile.getWritablePriceByQualifSecIDDate(sec22ID, LocalDate.of(2012, 3, 5));
		assertNotEquals(null, prc);
		assertEquals(PRC_12_ID, prc.getID());

		KMMSecID sec23ID = SEC_2_ID;
		prc = kmmInFile.getWritablePriceBySecIDDate(sec23ID, LocalDate.of(2012, 3, 5));
		assertNotEquals(null, prc);
		assertEquals(PRC_12_ID, prc.getID());
	}
	
	@Test
	public void test01_3_2() throws Exception {
		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceBySecIDDate(SEC_4_ID, LocalDate.of(2023, 4, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_14_ID, prc.getID());
		
		prc = kmmInFile.getWritablePriceBySecIDDate(SEC_4_ID, LocalDate.of(2023, 7, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_15_ID, prc.getID());
		
		prc = kmmInFile.getWritablePriceBySecIDDate(SEC_4_ID, LocalDate.of(2023, 10, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_16_ID, prc.getID());
	}
	
	@Test
	public void test01_4_1() throws Exception {
		KMMQualifCurrID currID = new KMMQualifCurrID("USD");
		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceByQualifCurrIDDate(currID, LocalDate.of(2023, 12, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_17_ID, prc.getID());
		
		Currency curr = Currency.getInstance("USD");
		prc = kmmInFile.getWritablePriceByCurrDate(curr, LocalDate.of(2023, 12, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_17_ID, prc.getID());
		
		prc = kmmInFile.getWritablePriceByQualifSecCurrIDDate(currID, LocalDate.of(2023, 12, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_17_ID, prc.getID());
	}
	
	@Test
	public void test01_4_2() throws Exception {
		Currency curr = Currency.getInstance("USD");
		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceByCurrDate(curr, LocalDate.of(2024, 1, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_18_ID, prc.getID());
		
		prc = kmmInFile.getWritablePriceByCurrDate(curr, LocalDate.of(2023, 11, 1));
		assertNotEquals(null, prc);
		assertEquals(PRC_19_ID, prc.getID());
	}
	
	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritablePrice objects returned by
	// can actually be modified -- both in memory and persisted in file.

	@Test
	public void test02_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));

		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceByID(PRC_1_ID);
		assertNotEquals(null, prc);

		assertEquals(PRC_1_ID, prc.getID());

		// ----------------------------
		// Modify the object

		// CAUTION: No, not date, because that would change the "ID".
		// Cf. TestKMyMoneyWritablePricePair.
		// prc.setDate(LocalDate.of(1977, 7, 12));
		prc.setValue(new FixedPointNumber(123.71));
		prc.setSource(Source.USER);

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(prc);

		// ----------------------------
		// Now, check whether the modified object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test02_1_check_persisted(outFile);
	}

	@Test
	public void test02_2() throws Exception {
		// ::TODO
	}

	@Test
	public void test02_3() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));

		KMMSecID sec31ID = SEC_2_ID;
		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceBySecIDDate(sec31ID, LocalDate.of(2012, 3, 5));
		assertNotEquals(null, prc);

		assertEquals(PRC_12_ID, prc.getID());

		// ----------------------------
		// Modify the object

		// CAUTION: No, not date, because that would change the "ID".
		// Cf. TestKMyMoneyWritablePricePair.
		// prc.setDate(LocalDate.of(1977, 7, 12));
		prc.setValue(new FixedPointNumber(123.71));
		prc.setSource(Source.USER);

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_3_check_memory(prc);

		// ----------------------------
		// Now, check whether the modified object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test02_3_check_persisted(outFile);
	}
	
	// ----------------------------
	
	private void test02_1_check_memory(KMyMoneyWritablePrice prc) throws Exception {
		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));

		assertEquals(PRC_1_ID, prc.getID()); // unchanged
		assertEquals(secID1.toString(), prc.getFromSecCurrQualifID().toString()); // unchanged
		assertEquals(secID1.toString(), prc.getFromSecurityQualifID().toString()); // unchanged
		assertEquals(secID1.getCode().toString(), prc.getFromSecurityQualifID().getSecID().toString()); // unchanged
		assertNotEquals(secID1.getCode(), prc.getFromSecurityQualifID().getSecID()); // unchanged
		assertNotEquals(secID1, prc.getFromSecurityQualifID()); // unchanged
		assertEquals("SAP AG", prc.getFromSecurity().getName()); // unchanged
		assertEquals("CURRENCY:EUR", prc.getToCurrencyQualifID().toString()); // unchanged
		assertEquals("EUR", prc.getToCurrencyCode()); // unchanged
		assertEquals("User", ((KMyMoneyWritablePriceImpl) prc).getSourceStr()); // unchanged
		assertEquals(Source.USER, prc.getSource()); // unchanged
		assertEquals("2023-11-03", prc.getDateStr());
		assertEquals(LocalDate.of(2023, 11, 3), prc.getDate()); // unchanged
		assertEquals(123.71, prc.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE); // changed
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_PRC, kmmOutFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRC, kmmOutFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));

		KMyMoneyPrice prc = kmmOutFile.getPriceByID(PRC_1_ID);
		assertNotEquals(null, prc);

		assertEquals(PRC_1_ID, prc.getID());
		assertEquals(secID1.toString(), prc.getFromSecCurrQualifID().toString()); // unchanged
		assertEquals(secID1.toString(), prc.getFromSecurityQualifID().toString()); // unchanged
		assertEquals(secID1.getCode().toString(), prc.getFromSecurityQualifID().getSecID().toString()); // unchanged
		assertNotEquals(secID1.getCode(), prc.getFromSecurityQualifID().getSecID()); // unchanged
		assertNotEquals(secID1, prc.getFromSecurityQualifID()); // unchanged
		assertEquals("SAP AG", prc.getFromSecurity().getName()); // unchanged
		assertEquals("CURRENCY:EUR", prc.getToCurrencyQualifID().toString()); // unchanged
		assertEquals("EUR", prc.getToCurrencyCode()); // unchanged
		assertEquals("User", ((KMyMoneyPriceImpl) prc).getSourceStr()); // changed
		assertEquals(Source.USER, prc.getSource()); // changed
		assertEquals("2023-11-03", prc.getDateStr());
		assertEquals(LocalDate.of(2023, 11, 3), prc.getDate()); // unchanged
		assertEquals(123.71, prc.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE); // changed
	}

	// ----------------------------

	private void test02_3_check_memory(KMyMoneyWritablePrice prc) throws Exception {
		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));

		assertEquals(PRC_12_ID, prc.getID()); // unchanged
		assertEquals(secID1.toString(), prc.getFromSecCurrQualifID().toString()); // unchanged
		assertEquals(secID1.toString(), prc.getFromSecurityQualifID().toString()); // unchanged
		assertEquals(secID1.getCode().toString(), prc.getFromSecurityQualifID().getSecID().toString()); // unchanged
		assertNotEquals(secID1.getCode(), prc.getFromSecurityQualifID().getSecID()); // unchanged
		assertNotEquals(secID1, prc.getFromSecurityQualifID()); // unchanged
		assertEquals("SAP AG", prc.getFromSecurity().getName()); // unchanged
		assertEquals("CURRENCY:EUR", prc.getToCurrencyQualifID().toString()); // unchanged
		assertEquals("EUR", prc.getToCurrencyCode()); // unchanged
		assertEquals("User", ((KMyMoneyWritablePriceImpl) prc).getSourceStr()); // unchanged
		assertEquals(Source.USER, prc.getSource()); // unchanged
		assertEquals("2012-03-05", prc.getDateStr());
		assertEquals(LocalDate.of(2012, 3, 5), prc.getDate()); // unchanged
		assertEquals(123.71, prc.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE); // changed
	}

	private void test02_3_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_PRC, kmmOutFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRC, kmmOutFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));

		KMyMoneyPrice prc = kmmOutFile.getPriceByID(PRC_12_ID);
		assertNotEquals(null, prc);

		assertEquals(PRC_12_ID, prc.getID());
		assertEquals(secID1.toString(), prc.getFromSecCurrQualifID().toString()); // unchanged
		assertEquals(secID1.toString(), prc.getFromSecurityQualifID().toString()); // unchanged
		assertEquals(secID1.getCode().toString(), prc.getFromSecurityQualifID().getSecID().toString()); // unchanged
		assertNotEquals(secID1.getCode(), prc.getFromSecurityQualifID().getSecID()); // unchanged
		assertNotEquals(secID1, prc.getFromSecurityQualifID()); // unchanged
		assertEquals("SAP AG", prc.getFromSecurity().getName()); // unchanged
		assertEquals("CURRENCY:EUR", prc.getToCurrencyQualifID().toString()); // unchanged
		assertEquals("EUR", prc.getToCurrencyCode()); // unchanged
		assertEquals("User", ((KMyMoneyPriceImpl) prc).getSourceStr()); // changed
		assertEquals(Source.USER, prc.getSource()); // changed
		assertEquals("2012-03-05", prc.getDateStr());
		assertEquals(LocalDate.of(2012, 3, 5), prc.getDate()); // unchanged
		assertEquals(123.71, prc.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE); // changed
	}

	// -----------------------------------------------------------------
	// PART 3: Create new objects
	// -----------------------------------------------------------------

	// ------------------------------
	// PART 3.1: High-Level
	// ------------------------------

	@Test
	public void test03_1_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));

		KMyMoneyPricePair prcPr = kmmInFile.getPricePairByID(PRCPR_1_ID);
		KMyMoneyWritablePrice prc = kmmInFile.createWritablePrice((KMyMoneyPricePairImpl) prcPr, LocalDate.of(1910, 5, 1));
		prc.setValue(new FixedPointNumber(345.21));
		prc.setSource(Source.USER);

		// ----------------------------
		// Check whether the object can has actually be created
		// (in memory, not in the file yet).

		test03_1_1_check_memory(prc);

		// ----------------------------
		// Now, check whether the created object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test03_1_1_check_persisted(outFile);
	}

	private void test03_1_1_check_memory(KMyMoneyWritablePrice prc) throws Exception {
		assertEquals(ConstTest.Stats.NOF_PRC + 1, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRC + 1, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));

		newID.set( prc.getID() );
		assertEquals(PRCPR_1_ID.getFromSecCurr().getCode(), newID.getFromSecCurr());
		assertEquals(PRCPR_1_ID.getToCurr().getCode(), newID.getToCurr());
		assertEquals("1910-05-01", newID.getDateStr());
		assertEquals(LocalDate.of(1910, 5, 1), newID.getDate());

		assertEquals(PRCPR_1_ID.getFromSecCurr(), prc.getFromSecCurrQualifID());
		assertEquals(PRCPR_1_ID.getToCurr(), prc.getToCurrencyQualifID());
		assertEquals("1910-05-01", prc.getDateStr());
		assertEquals(LocalDate.of(1910, 5, 1), prc.getDate());
		assertEquals(345.21, prc.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("User", ((KMyMoneyWritablePriceImpl) prc).getSourceStr());
		assertEquals(Source.USER, prc.getSource());
	}

	private void test03_1_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_PRC + 1, kmmOutFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRC + 1, kmmOutFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));

		KMyMoneyPrice prc = kmmOutFile.getPriceByID(newID);
		assertNotEquals(null, prc);

		assertEquals(newID, prc.getID());
		assertEquals(PRCPR_1_ID.getFromSecCurr().getCode(), newID.getFromSecCurr());
		assertEquals(PRCPR_1_ID.getToCurr().getCode(), newID.getToCurr());
		assertEquals("1910-05-01", newID.getDateStr());
		assertEquals(LocalDate.of(1910, 5, 1), newID.getDate());

		assertEquals(PRCPR_1_ID.getFromSecCurr(), prc.getFromSecCurrQualifID());
		assertEquals(PRCPR_1_ID.getToCurr(), prc.getToCurrencyQualifID());
		assertEquals("1910-05-01", prc.getDateStr());
		assertEquals(LocalDate.of(1910, 5, 1), prc.getDate());
		assertEquals(345.21, prc.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("User", ((KMyMoneyPriceImpl) prc).getSourceStr());
		assertEquals(Source.USER, prc.getSource());
	}

	// ------------------------------
	// PART 3.2: Low-Level
	// ------------------------------

	// ::TODO

	// -----------------------------------------------------------------
	// PART 4: Delete objects
	// -----------------------------------------------------------------

	// ------------------------------
	// PART 4.1: High-Level
	// ------------------------------

	@Test
	public void test04_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRC, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));

		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceByID(PRC_1_ID);
		assertNotEquals(null, prc);
		assertEquals(PRC_1_ID, prc.getID());

		// ----------------------------
		// Delete the object

		kmmInFile.removePrice(prc);

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test04_1_check_memory(prc);

		// ----------------------------
		// Now, check whether the modified object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritablePriceImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test04_1_check_persisted(outFile);
	}
	
	// ---------------------------------------------------------------

	private void test04_1_check_memory(KMyMoneyWritablePrice prc) throws Exception {
		assertEquals(ConstTest.Stats.NOF_PRC - 1, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRC - 1, kmmInFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));

		// CAUTION / ::TODO
		// Old Object still exists and is unchanged
		// Exception: no splits any more
		// Don't know what to do about this oddity right now,
		// but it needs to be addressed at some point.
		assertEquals(secID1.toString(), prc.getFromSecurityQualifID().toString());
		assertEquals("CURRENCY:EUR", prc.getToCurrencyQualifID().toString());
		assertEquals(LocalDate.of(2023, 11, 3), prc.getDate());
		assertEquals(120.0, prc.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		// etc.
		
		// However, the price cannot newly be instantiated any more,
		// just as you would expect.
		try {
			KMyMoneyWritablePrice prcNow1 = kmmInFile.getWritablePriceByID(PRC_1_ID);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		// Same for a non non-writable instance. 
		// However, due to design asymmetry, no exception is thrown here,
		// but the method just returns null.
		KMyMoneyPrice prcNow2 = kmmInFile.getPriceByID(PRC_1_ID);
		assertEquals(null, prcNow2);
	}

	private void test04_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_PRC - 1, kmmOutFileStats.getNofEntriesPrices(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRC - 1, kmmOutFileStats.getNofEntriesPrices(KMMFileStats.Type.CACHE));

		// The price does not exist any more, just as you would expect.
		// However, no exception is thrown, as opposed to test04_1_check_memory()
		KMyMoneyPrice prc = kmmOutFile.getPriceByID(PRC_1_ID);
		assertEquals(null, prc); // sic
	}

	// ------------------------------
	// PART 4.2: Low-Level
	// ------------------------------
	
	// ::EMPTY

}
