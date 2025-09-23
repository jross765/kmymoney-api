package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneySecurityImpl {
	// Mercedes-Benz Group AG
	public static final KMMSecID SEC_1_ID     = new KMMSecID("E000002");
	public static final String   SEC_1_ISIN   = "DE0007100000";
	public static final String   SEC_1_TICKER = "MBG";

	// SAP SE
	public static final KMMSecID SEC_2_ID     = new KMMSecID("E000001");
	public static final String   SEC_2_ISIN   = "DE0007164600";
	public static final String   SEC_2_TICKER = "SAP";

	// Coca Cola
	public static final KMMSecID SEC_4_ID     = new KMMSecID("E000004");
	public static final String   SEC_4_ISIN   = "US1912161007";
	public static final String   SEC_4_TICKER = "KO";

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneySecurity sec = null;

	private KMMQualifSecID secID1 = null;
	private KMMQualifSecID secID2 = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneySecurityImpl.class);
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

		secID1 = new KMMQualifSecID(SEC_1_ID);
		secID2 = new KMMQualifSecID(SEC_2_ID);
	}

	// -----------------------------------------------------------------

	@Test
	public void test00() throws Exception {
		// Cf. TestCmdtyCurrID -- let's just double-check
		assertEquals(KMMQualifSecCurrID.Type.SECURITY.toString() + KMMQualifSecCurrID.SEPARATOR + SEC_1_ID,
				secID1.toString());
		assertEquals(KMMQualifSecCurrID.Type.SECURITY.toString() + KMMQualifSecCurrID.SEPARATOR + SEC_2_ID,
				secID2.toString());
	}

	// ------------------------------

	@Test
	public void test01_1() throws Exception {
		sec = kmmFile.getSecurityByQualifID(new KMMQualifSecID(SEC_1_ID));
		assertNotEquals(null, sec);

		assertEquals(secID1.toString(), sec.getQualifID().toString());
		assertNotEquals(secID1, sec);
		assertEquals(KMMSecCurr.Type.STOCK, sec.getType());
		assertEquals(SEC_1_TICKER, sec.getSymbol());
		assertEquals(SEC_1_ISIN, sec.getCode());
		assertEquals("Mercedes-Benz Group AG", sec.getName());
		assertEquals(2, sec.getPP().intValue());
		assertEquals(100, sec.getSAF().intValue());
		assertEquals(KMMSecCurr.RoundingMethod.ROUND, sec.getRoundingMethod());
		assertEquals(new KMMQualifCurrID("EUR"), sec.getTradingCurrency());
		assertEquals("XETRA", sec.getTradingMarket());
	}

	@Test
	public void test01_2() throws Exception {
		sec = kmmFile.getSecurityByQualifID(secID1.toString());
		assertNotEquals(null, sec);

		assertEquals(secID1.toString(), sec.getQualifID().toString());
		assertNotEquals(secID1, sec);
		assertEquals(KMMSecCurr.Type.STOCK, sec.getType());
		assertEquals(SEC_1_TICKER, sec.getSymbol());
		assertEquals(SEC_1_ISIN, sec.getCode());
		assertEquals("Mercedes-Benz Group AG", sec.getName());
		assertEquals(2, sec.getPP().intValue());
		assertEquals(100, sec.getSAF().intValue());
		assertEquals(KMMSecCurr.RoundingMethod.ROUND, sec.getRoundingMethod());
		assertEquals(new KMMQualifCurrID("EUR"), sec.getTradingCurrency());
		assertEquals("XETRA", sec.getTradingMarket());
	}

	@Test
	public void test01_3() throws Exception {
		sec = kmmFile.getSecurityBySymbol(SEC_1_TICKER);
		assertNotEquals(null, sec);

		assertEquals(secID1.toString(), sec.getQualifID().toString());
		assertNotEquals(secID1, sec);
		assertEquals(KMMSecCurr.Type.STOCK, sec.getType());
		assertEquals(SEC_1_TICKER, sec.getSymbol());
		assertEquals(SEC_1_ISIN, sec.getCode());
		assertEquals("Mercedes-Benz Group AG", sec.getName());
		assertEquals(2, sec.getPP().intValue());
		assertEquals(100, sec.getSAF().intValue());
		assertEquals(KMMSecCurr.RoundingMethod.ROUND, sec.getRoundingMethod());
		assertEquals(new KMMQualifCurrID("EUR"), sec.getTradingCurrency());
		assertEquals("XETRA", sec.getTradingMarket());
	}

	@Test
	public void test01_4() throws Exception {
		sec = kmmFile.getSecurityByCode(SEC_1_ISIN);
		assertNotEquals(null, sec);

		assertEquals(secID1.toString(), sec.getQualifID().toString());
		assertNotEquals(secID1, sec);
		assertEquals(KMMSecCurr.Type.STOCK, sec.getType());
		assertEquals(SEC_1_TICKER, sec.getSymbol());
		assertEquals(SEC_1_ISIN, sec.getCode());
		assertEquals("Mercedes-Benz Group AG", sec.getName());
		assertEquals(2, sec.getPP().intValue());
		assertEquals(100, sec.getSAF().intValue());
		assertEquals(KMMSecCurr.RoundingMethod.ROUND, sec.getRoundingMethod());
		assertEquals(new KMMQualifCurrID("EUR"), sec.getTradingCurrency());
		assertEquals("XETRA", sec.getTradingMarket());
	}

	@Test
	public void test01_5() throws Exception {
		List<KMyMoneySecurity> secList = kmmFile.getSecuritiesByName("mercedes");
		assertNotEquals(null, secList);
		assertEquals(1, secList.size());

		assertEquals(secID1.toString(), secList.get(0).getQualifID().toString());
		assertEquals(secID1, secList.get(0).getQualifID());
		assertEquals(SEC_1_TICKER, secList.get(0).getSymbol());
		assertEquals(SEC_1_ISIN, secList.get(0).getCode());
		assertEquals("Mercedes-Benz Group AG", secList.get(0).getName());

		secList = kmmFile.getSecuritiesByName("BENZ");
		assertNotEquals(null, secList);
		assertEquals(1, secList.size());
		assertEquals(secID1, ((KMyMoneySecurity) secList.toArray()[0]).getQualifID());

		secList = kmmFile.getSecuritiesByName(" MeRceDeS-bEnZ  ");
		assertNotEquals(null, secList);
		assertEquals(1, secList.size());
		assertEquals(secID1.toString(), ((KMyMoneySecurity) secList.toArray()[0]).getQualifID().toString());
		assertEquals(secID1, ((KMyMoneySecurity) secList.toArray()[0]).getQualifID());
	}

	//  // ------------------------------
	//
	//  @Test
	//  public void test02_1() throws Exception
	//  {
	//    sec = kmmFile.getSecurityByQualifID(SEC_3_SECIDTYPE.toString(), SEC_3_ID);
	//    assertNotEquals(null, sec);
	//    
	//    assertEquals(secCurrID3.toString(), sec.getQualifID().toString());
	//    // *Not* equal because of class
	//    assertNotEquals(secCurrID3, sec.getQualifID());
	//    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
	////    assertEquals(secCurrID1, sec.getQualifID()); // not trivial!
	//    assertEquals(SEC_3_ISIN, sec.getXCode());
	//    assertEquals("AstraZeneca Plc", sec.getName());
	//  }
	//
	//  @Test
	//  public void test02_2() throws Exception
	//  {
	//    sec = kmmFile.getSecurityByQualifID(secCurrID3.toString());
	//    assertNotEquals(null, sec);
	//    
	//    assertEquals(secCurrID3.toString(), sec.getQualifID().toString());
	//    // *Not* equal because of class
	//    assertNotEquals(secCurrID3, sec.getQualifID());
	//    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
	////    assertEquals(secCurrID1, sec.getQualifID()); // not trivial!
	//    assertEquals(SEC_3_ISIN, sec.getXCode());
	//    assertEquals("AstraZeneca Plc", sec.getName());
	//  }
	//
	//  @Test
	//  public void test02_3() throws Exception
	//  {
	//    sec = kmmFile.getSecurityBySymbol(SEC_3_ISIN);
	//    assertNotEquals(null, sec);
	//    
	//    assertEquals(secCurrID3.toString(), sec.getQualifID().toString());
	//    // *Not* equal because of class
	//    assertNotEquals(secCurrID3, sec.getQualifID());
	//    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
	////    assertEquals(secCurrID1, sec.getQualifID()); // not trivial!
	//    assertEquals(SEC_3_ISIN, sec.getSymbol());
	//    assertEquals("AstraZeneca Plc", sec.getName());
	//  }
	//
	//  @Test
	//  public void test02_4() throws Exception
	//  {
	//    Collection<KMyMoneySecurity> secList = kmmFile.getSecuritiesByName("astra");
	//    assertNotEquals(null, secList);
	//    assertEquals(1, secList.size());
	//    
	//    assertEquals(secCurrID3.toString(), 
	//	         ((KMyMoneySecurity) secList.toArray()[0]).getQualifID().toString());
	//    // *Not* equal because of class
	//    assertNotEquals(secCurrID3, 
	//	            ((KMyMoneySecurity) secList.toArray()[0]).getQualifID());
	//    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
	////    assertEquals(secCurrID1, 
	////	        ((KMyMoneySecurity) secList.toArray()[0]).getQualifID()); // not trivial!
	//    assertEquals(SEC_3_ISIN, 
	//	         ((KMyMoneySecurity) secList.toArray()[0]).getXCode());
	//    assertEquals("AstraZeneca Plc", 
	//	         ((KMyMoneySecurity) secList.toArray()[0]).getName());
	//
	//    secList = kmmFile.getSecuritiesByName("BENZ");
	//    assertNotEquals(null, secList);
	//    assertEquals(1, secList.size());
	//    // *Not* equal because of class
	//    assertNotEquals(secCurrID3, 
	//	            ((KMyMoneySecurity) secList.toArray()[0]).getQualifID());
	//    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
	////    assertEquals(secCurrID1, 
	////	         ((KMyMoneySecurity) secList.toArray()[0]).getQualifID());
	//    
	//    secList = kmmFile.getSecuritiesByName(" aStrAzENeCA  ");
	//    assertNotEquals(null, secList);
	//    assertEquals(1, secList.size());
	//    assertEquals(secCurrID3.toString(), 
	//	         ((KMyMoneySecurity) secList.toArray()[0]).getQualifID().toString());
	//    // *Not* equal because of class
	//    assertNotEquals(secCurrID3, 
	//	            ((KMyMoneySecurity) secList.toArray()[0]).getQualifID());
	//    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
	////    assertEquals(secCurrID1, 
	////	         ((KMyMoneySecurity) secList.toArray()[0]).getQualifID()); // not trivial!
	//  }
}
