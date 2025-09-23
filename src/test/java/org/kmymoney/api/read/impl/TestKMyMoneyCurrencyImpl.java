package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyCurrencyImpl {
	public static final String CURR_1_ID     = "EUR";
	public static final String CURR_1_SYMB   = "€";

	public static final String CURR_2_ID     = "USD";
	public static final String CURR_2_SYMB   = "$";

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyCurrency curr = null;

	private KMMQualifCurrID currID1 = null;
	private KMMQualifCurrID currID2 = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyCurrencyImpl.class);
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

		currID1 = new KMMQualifCurrID(CURR_1_ID);
		currID2 = new KMMQualifCurrID(CURR_2_ID);
	}

	// -----------------------------------------------------------------

	@Test
	public void test00() throws Exception {
		// Cf. TestCmdtyCurrID -- let's just double-check
		assertEquals(KMMQualifSecCurrID.Type.CURRENCY.toString() + KMMQualifSecCurrID.SEPARATOR + CURR_1_ID,
				currID1.toString());
		assertEquals(KMMQualifSecCurrID.Type.CURRENCY.toString() + KMMQualifSecCurrID.SEPARATOR + CURR_2_ID,
				currID2.toString());
	}

	// ------------------------------

	@Test
	public void test01_1() throws Exception {
		curr = kmmFile.getCurrencyByQualifID(new KMMQualifCurrID(CURR_1_ID));
		assertNotEquals(null, curr);

		assertEquals(currID1.toString(), curr.getQualifID().toString());
		assertNotEquals(currID1, curr);
		assertEquals("Euro", curr.getName());
		assertEquals(4, curr.getPP().intValue());
		assertEquals(100, curr.getSAF().intValue());
		assertEquals(KMMSecCurr.RoundingMethod.ROUND, curr.getRoundingMethod());
	}

	@Test
	public void test01_2() throws Exception {
		curr = kmmFile.getCurrencyByID(CURR_2_ID);
		assertNotEquals(null, curr);

		assertEquals(currID2.toString(), curr.getQualifID().toString());
		assertEquals(currID2.toString(), curr.getQualifID().toString());
		assertNotEquals(currID2, curr);
		assertEquals(CURR_2_SYMB, curr.getSymbol());
		assertEquals("US-Dollar", curr.getName());
		assertEquals(4, curr.getPP().intValue());
		assertEquals(100, curr.getSAF().intValue());
		assertEquals(KMMSecCurr.RoundingMethod.ROUND, curr.getRoundingMethod());
	}

	//  // ------------------------------
	//
	//  @Test
	//  public void test02_1() throws Exception
	//  {
	//    curr = kmmFile.getCurrencyByQualifID(CURR_3_CURRIDTYPE.toString(), CURR_3_ID);
	//    assertNotEquals(null, curr);
	//    
	//    assertEquals(secCurrID3.toString(), curr.getQualifID().toString());
	//    // *Not* equal because of class
	//    assertNotEquals(secCurrID3, curr.getQualifID());
	//    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
	////    assertEquals(secCurrID1, curr.getQualifID()); // not trivial!
	//    assertEquals(CURR_3_ISIN, curr.getXCode());
	//    assertEquals("AstraZeneca Plc", curr.getName());
	//  }
	//
	//  @Test
	//  public void test02_2() throws Exception
	//  {
	//    curr = kmmFile.getCurrencyByQualifID(secCurrID3.toString());
	//    assertNotEquals(null, curr);
	//    
	//    assertEquals(secCurrID3.toString(), curr.getQualifID().toString());
	//    // *Not* equal because of class
	//    assertNotEquals(secCurrID3, curr.getQualifID());
	//    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
	////    assertEquals(secCurrID1, curr.getQualifID()); // not trivial!
	//    assertEquals(CURR_3_ISIN, curr.getXCode());
	//    assertEquals("AstraZeneca Plc", curr.getName());
	//  }
	//
	//  @Test
	//  public void test02_3() throws Exception
	//  {
	//    curr = kmmFile.getCurrencyBySymbol(CURR_3_ISIN);
	//    assertNotEquals(null, curr);
	//    
	//    assertEquals(secCurrID3.toString(), curr.getQualifID().toString());
	//    // *Not* equal because of class
	//    assertNotEquals(secCurrID3, curr.getQualifID());
	//    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
	////    assertEquals(secCurrID1, curr.getQualifID()); // not trivial!
	//    assertEquals(CURR_3_ISIN, curr.getSymbol());
	//    assertEquals("AstraZeneca Plc", curr.getName());
	//  }
	//
	//  @Test
	//  public void test02_4() throws Exception
	//  {
	//    Collection<KMyMoneyCurrency> currList = kmmFile.getCurruritiesByName("astra");
	//    assertNotEquals(null, currList);
	//    assertEquals(1, currList.size());
	//    
	//    assertEquals(secCurrID3.toString(), 
	//	         ((KMyMoneyCurrency) currList.toArray()[0]).getQualifID().toString());
	//    // *Not* equal because of class
	//    assertNotEquals(secCurrID3, 
	//	            ((KMyMoneyCurrency) currList.toArray()[0]).getQualifID());
	//    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
	////    assertEquals(secCurrID1, 
	////	        ((KMyMoneyCurrency) currList.toArray()[0]).getQualifID()); // not trivial!
	//    assertEquals(CURR_3_ISIN, 
	//	         ((KMyMoneyCurrency) currList.toArray()[0]).getXCode());
	//    assertEquals("AstraZeneca Plc", 
	//	         ((KMyMoneyCurrency) currList.toArray()[0]).getName());
	//
	//    currList = kmmFile.getCurruritiesByName("BENZ");
	//    assertNotEquals(null, currList);
	//    assertEquals(1, currList.size());
	//    // *Not* equal because of class
	//    assertNotEquals(secCurrID3, 
	//	            ((KMyMoneyCurrency) currList.toArray()[0]).getQualifID());
	//    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
	////    assertEquals(secCurrID1, 
	////	         ((KMyMoneyCurrency) currList.toArray()[0]).getQualifID());
	//    
	//    currList = kmmFile.getCurruritiesByName(" aStrAzENeCA  ");
	//    assertNotEquals(null, currList);
	//    assertEquals(1, currList.size());
	//    assertEquals(secCurrID3.toString(), 
	//	         ((KMyMoneyCurrency) currList.toArray()[0]).getQualifID().toString());
	//    // *Not* equal because of class
	//    assertNotEquals(secCurrID3, 
	//	            ((KMyMoneyCurrency) currList.toArray()[0]).getQualifID());
	//    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
	////    assertEquals(secCurrID1, 
	////	         ((KMyMoneyCurrency) currList.toArray()[0]).getQualifID()); // not trivial!
	//  }
}
