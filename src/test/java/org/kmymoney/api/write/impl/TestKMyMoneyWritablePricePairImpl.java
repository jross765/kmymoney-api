package org.kmymoney.api.write.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyPricePairImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.write.KMyMoneyWritablePricePair;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyWritablePricePairImpl {
	private static final KMMPricePairID PRCPR_1_ID = TestKMyMoneyPricePairImpl.PRCPR_1_ID;
	private static final KMMPricePairID PRCPR_2_ID = TestKMyMoneyPricePairImpl.PRCPR_2_ID;
	private static final KMMPricePairID PRCPR_3_ID = TestKMyMoneyPricePairImpl.PRCPR_3_ID;
	private static final KMMPricePairID PRCPR_4_ID = TestKMyMoneyPricePairImpl.PRCPR_4_ID;
	
	private static final KMMPriceID PRC_1_ID = TestKMyMoneyPricePairImpl.PRC_1_ID;
	private static final KMMPriceID PRC_2_ID = TestKMyMoneyPricePairImpl.PRC_2_ID;
	private static final KMMPriceID PRC_12_ID = TestKMyMoneyPricePairImpl.PRC_12_ID;

	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

	private KMMFileStats kmmInFileStats = null;
	private KMMFileStats kmmOutFileStats = null;

	KMMQualifSecCurrID secID1 = null;
	KMMQualifSecCurrID secID2 = null;

	KMMQualifCurrID currID1 = null;

	private KMMPricePairID newID = null;

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
		return new JUnit4TestAdapter(TestKMyMoneyWritablePricePairImpl.class);
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

		newID = new KMMPricePairID(new KMMQualifCurrID("EUR"), new KMMQualifCurrID("EUR")); // dummy
	}

	// -----------------------------------------------------------------
	// PART 1: Read existing objects as modifiable ones
	// (and see whether they are fully symmetrical to their read-only
	// counterparts)
	// -----------------------------------------------------------------
	// Cf. TestKMyMoneyPricePairImpl.test01_xyz
	//
	// Check whether the KMyMoneyWritablePricePair objects returned by
	// KMyMoneyWritableFileImpl.getWritablePricePairByID() are actually
	// complete (as complete as returned be KMyMoneyFileImpl.getPricePairByID().

	@Test
	public void test01() throws Exception {
		Collection<KMyMoneyWritablePricePair> prcPrColl = kmmInFile.getWritablePricePairs();
		List<KMyMoneyWritablePricePair> prcPrList = new ArrayList<KMyMoneyWritablePricePair>(prcPrColl);
		prcPrList.sort(Comparator.naturalOrder());

		assertEquals(4, prcPrList.size());
		assertEquals(PRCPR_1_ID, prcPrList.get(0).getID());
		assertEquals(PRCPR_2_ID, prcPrList.get(1).getID());
		assertEquals(PRCPR_4_ID, prcPrList.get(2).getID());
		assertEquals(PRCPR_3_ID, prcPrList.get(3).getID());
	}

	@Test
	public void test01_1() throws Exception {
		//		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceByID(TestKMyMoneyPriceImpl.PRC_1_ID);
		//		assertNotEquals(null, prc);
		//		KMyMoneyWritablePricePair prcPr = prc.getWritableParentPricePair();
		KMyMoneyWritablePricePair prcPr = kmmInFile.getWritablePricePairByID(PRCPR_1_ID);
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
	}

	@Test
	public void test01_2() throws Exception {
		//		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceByID(TestKMyMoneyPriceImpl.PRC_2_ID);
		//		assertNotEquals(null, prc);
		//		KMyMoneyWritablePricePair prcPr = prc.getWritableParentPricePair();
		KMyMoneyWritablePricePair prcPr = kmmInFile.getWritablePricePairByID(PRCPR_2_ID);
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
	}

	@Test
	public void test01_3() throws Exception {
		//		KMyMoneyWritablePrice prc = kmmInFile.getWritablePriceByID(TestKMyMoneyPriceImpl.PRC_3_ID);
		//		assertNotEquals(null, prc);
		//		KMyMoneyWritablePricePair prcPr = prc.getWritableParentPricePair();
		KMyMoneyWritablePricePair prcPr = kmmInFile.getWritablePricePairByID(PRCPR_3_ID);
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
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritablePricePair objects returned by
	// can actually be modified -- both in memory and persisted in file.

	@Test
	public void test02_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_PRCPR, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRCPR, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.CACHE));

		KMyMoneyWritablePricePair prcPr = kmmInFile.getWritablePricePairByID(PRCPR_1_ID);
		assertNotEquals(null, prcPr);

		assertEquals(PRCPR_1_ID, prcPr.getID());

		// ----------------------------
		// Modify the object

		// Now, obviously, this is a completely nonsensical action that
		// you virtually never would take in real life (regardless of 
		// what specific currencies you choose). But this is a test case, 
		// so we do it anyway...
		prcPr.setFromCurrencyCode("BRL");
		prcPr.setToCurrencyCode("SGD");

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(prcPr);

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

	private void test02_1_check_memory(KMyMoneyWritablePricePair prcPr) throws Exception {
		assertEquals(ConstTest.Stats.NOF_PRCPR, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRCPR, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.CACHE));

		// CAUTION: The price pair, by its very definition, is a special
		// case: It does not *have* an ID (like "unique" and "separated from its 
		// content"), but rather its content *is* its own ID (a term that,
		// strictly speaking, one actually should not use here, but there
		// is a rationale for it, in the broader context of how to treat the 
		// prices). Anyway: This is why, as opposed to all other entities,
		// the price "ID" changes when its content is changed.
		// assertEquals(PRCPR_1_ID, prcPr.getID()); // unchanged <-- NO!
		assertEquals("BRL", prcPr.getFromCurrencyCode()); // changed
		assertEquals("SGD", prcPr.getToCurrencyCode()); // changed
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_PRCPR, kmmOutFileStats.getNofEntriesPricePairs(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRCPR, kmmOutFileStats.getNofEntriesPricePairs(KMMFileStats.Type.CACHE));

		// CAUTION: As opposed to the symmetrical test case in the other
		// entities, we *cannot* search with the original ID here because
		// it does not exist any more.
		// CAUTION: Cf. comment in test02_1_check_memory()
		// KMyMoneyPricePair prcPr = kmmOutFile.getPricePairByID(PRCPR_1_ID);
		KMyMoneyPricePair prcPr = kmmOutFile.getPricePairByID(PRCPR_1_ID);
		assertEquals(null, prcPr); // sic
		prcPr = kmmOutFile.getPricePairByID(new KMMPricePairID("BRL", "SGD"));
		assertNotEquals(null, prcPr);

		// Cf. comment in test02_1_check_memory()
		// assertEquals(PRCPR_1_ID, prcPr.getID()); // unchanged <-- NO!
		assertEquals("BRL", prcPr.getFromCurrencyCode()); // changed
		assertEquals("SGD", prcPr.getToCurrencyCode()); // changed
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

		assertEquals(ConstTest.Stats.NOF_PRCPR, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.RAW)); 
		assertEquals(ConstTest.Stats.NOF_PRCPR, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.CACHE));

		KMMQualifCurrID fromCurr = new KMMQualifCurrID("CNY");  
		KMMQualifCurrID toCurr = new KMMQualifCurrID("JPY");  
		KMMPricePairID prcPrID = new KMMPricePairID(fromCurr, toCurr);  
		KMyMoneyWritablePricePair prcPr = kmmInFile.createWritablePricePair(prcPrID);
		newID.set(prcPr.getID());

		// Additional check for this class -- no, not as trivial as it seems to be. 
		assertEquals(newID, prcPrID);

		// ----------------------------
		// Check whether the object can has actually be created
		// (in memory, not in the file yet).

		test03_1_1_check_memory(prcPr);

		// ----------------------------
		// Now, check whether the created object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableSecurityImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test03_1_1_check_persisted(outFile);
	}

	private void test03_1_1_check_memory(KMyMoneyWritablePricePair prcPr) throws Exception {
		assertEquals(ConstTest.Stats.NOF_PRCPR + 1, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRCPR + 1, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.CACHE));

		assertEquals(newID.toString(), prcPr.getID().toString());
	}

	private void test03_1_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_PRCPR + 1, kmmOutFileStats.getNofEntriesPricePairs(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRCPR + 1, kmmOutFileStats.getNofEntriesPricePairs(KMMFileStats.Type.CACHE));

		KMyMoneyPricePair sec = kmmOutFile.getPricePairByID(newID);
		assertNotEquals(null, sec);

		assertEquals(newID.toString(), sec.getID().toString());
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

		assertEquals(ConstTest.Stats.NOF_PRCPR, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRCPR, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.CACHE));

		KMyMoneyWritablePricePair prc = kmmInFile.getWritablePricePairByID(PRCPR_1_ID);
		assertNotEquals(null, prc);
		assertEquals(PRCPR_1_ID, prc.getID());

		// ----------------------------
		// Delete the object

		kmmInFile.removePricePair(prc);

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

	private void test04_1_check_memory(KMyMoneyWritablePricePair prcPr) throws Exception {
		assertEquals(ConstTest.Stats.NOF_PRCPR - 1, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRCPR - 1, kmmInFileStats.getNofEntriesPricePairs(KMMFileStats.Type.CACHE));

		// CAUTION / ::TODO
		// Old Object still exists and is unchanged
		// Don't know what to do about this oddity right now,
		// but it needs to be addressed at some point.
		assertEquals(secID1.toString(), prcPr.getFromSecCurrQualifID().toString());
		assertEquals("CURRENCY:EUR", prcPr.getToCurrencyQualifID().toString());
		assertEquals("EUR", prcPr.getToCurrencyCode());
		// etc.
		// CAUTION / ::TODO: Here, as opposed to transactions with splits,
		// there are still all prices in the price pair object.
		assertEquals(3, prcPr.getPrices().size());
		assertEquals(PRC_12_ID, prcPr.getPrices().get(0).getID());
		assertEquals(PRC_1_ID, prcPr.getPrices().get(1).getID());
		assertEquals(PRC_2_ID, prcPr.getPrices().get(2).getID());
		
		// However, the price pair cannot newly be instantiated any more,
		// just as you would expect.
		// CAUTION / ::TODO: As opposed to the other entities / sister test cases,
		// no exception is thrown here, but the method simply returns null.
		KMyMoneyWritablePricePair prcPrNow1 = kmmInFile.getWritablePricePairByID(PRCPR_1_ID);
		assertEquals(null, prcPrNow1);
		// Same for a non non-writable instance. 
		// However, due to design asymmetry, no exception is thrown here,
		// but the method just returns null.
		KMyMoneyPricePair prcPrNow2 = kmmInFile.getPricePairByID(PRCPR_1_ID);
		assertEquals(null, prcPrNow2);
		
		// Same with prices under the price pair.
		KMyMoneyPrice prcNow1 = kmmInFile.getPriceByID(PRC_1_ID);
		assertEquals(null, prcNow1);
	}

	private void test04_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_PRCPR - 1, kmmOutFileStats.getNofEntriesPricePairs(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_PRCPR - 1, kmmOutFileStats.getNofEntriesPricePairs(KMMFileStats.Type.CACHE));

		// The price pair does not exist any more, just as you would expect.
		// However, no exception is thrown, as opposed to test04_1_check_memory()
		KMyMoneyPricePair prcPr = kmmOutFile.getPricePairByID(PRCPR_1_ID);
		assertEquals(null, prcPr); // sic
		
		// Same with prices under the price pair.
		KMyMoneyPrice prcNow1 = kmmInFile.getPriceByID(PRC_1_ID);
		assertEquals(null, prcNow1);
	}

	// ------------------------------
	// PART 4.2: Low-Level
	// ------------------------------
	
	// ::EMPTY

}
