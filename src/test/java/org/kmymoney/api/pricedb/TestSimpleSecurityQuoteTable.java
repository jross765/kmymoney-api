package org.kmymoney.api.pricedb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.apache.commons.numbers.fraction.BigFraction;
import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.pricedb.ComplexPriceTable;
import org.kmymoney.api.pricedb.SimplePriceTable;
import org.kmymoney.api.pricedb.SimpleSecurityQuoteTable;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import junit.framework.JUnit4TestAdapter;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TestSimpleSecurityQuoteTable {
	private KMyMoneyFile kmmFile = null;
	private ComplexPriceTable complPriceTab = null;
	private SimplePriceTable simplPriceTab = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestSimpleSecurityQuoteTable.class);
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
		complPriceTab = kmmFile.getCurrencyTable();
		assertNotEquals(null, complPriceTab);

		simplPriceTab = complPriceTab.getTabByType(KMMQualifSecCurrID.Type.SECURITY);
		assertNotEquals(null, simplPriceTab);
		
		assertEquals(3, simplPriceTab.getCodes().size());

		KMMSecID secID = new KMMSecID("E000001");
		assertEquals(119.50, ((SimpleSecurityQuoteTable) simplPriceTab).getConversionFactor(secID).doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(239,    ((SimpleSecurityQuoteTable) simplPriceTab).getConversionFactorRat(secID).getNumerator().intValue());
		assertEquals(2,      ((SimpleSecurityQuoteTable) simplPriceTab).getConversionFactorRat(secID).getDenominator().intValue());

		secID = new KMMSecID("E000002");
		assertEquals(58.25, ((SimpleSecurityQuoteTable) simplPriceTab).getConversionFactor(secID).doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(233,   ((SimpleSecurityQuoteTable) simplPriceTab).getConversionFactorRat(secID).getNumerator().intValue());
		assertEquals(4,     ((SimpleSecurityQuoteTable) simplPriceTab).getConversionFactorRat(secID).getDenominator().intValue());
	}

	@Test
	public void test02_1() throws Exception {
		complPriceTab = kmmFile.getCurrencyTable();
		assertNotEquals(null, complPriceTab);

		simplPriceTab = complPriceTab.getTabByType(KMMQualifSecCurrID.Type.SECURITY);
		assertNotEquals(null, simplPriceTab);

		KMMSecID secID = new KMMSecID("E000001");
		FixedPointNumber valFP = new FixedPointNumber("101.0");
		BigFraction      valBF = BigFraction.of(101, 1);
		assertEquals(12069.50, ((SimpleSecurityQuoteTable) simplPriceTab).convertToBaseCurrency(valFP, secID).doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(24139,    ((SimpleSecurityQuoteTable) simplPriceTab).convertToBaseCurrencyRat(valBF, secID).getNumerator().intValue());
		assertEquals(2,        ((SimpleSecurityQuoteTable) simplPriceTab).convertToBaseCurrencyRat(valBF, secID).getDenominator().intValue());

		secID = new KMMSecID("E000002");
		valFP = new FixedPointNumber("101.0");
		valBF = BigFraction.of(101, 1);
		assertEquals(5883.25, ((SimpleSecurityQuoteTable) simplPriceTab).convertToBaseCurrency(valFP, secID).doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(23533,   ((SimpleSecurityQuoteTable) simplPriceTab).convertToBaseCurrencyRat(valBF, secID).getNumerator().intValue());
		assertEquals(4,       ((SimpleSecurityQuoteTable) simplPriceTab).convertToBaseCurrencyRat(valBF, secID).getDenominator().intValue());
	}

	@Test
	public void test02_2() throws Exception {
		complPriceTab = kmmFile.getCurrencyTable();
		assertNotEquals(null, complPriceTab);

		simplPriceTab = complPriceTab.getTabByType(KMMQualifSecCurrID.Type.SECURITY);
		assertNotEquals(null, simplPriceTab);

		KMMSecID secID = new KMMSecID("E000001");
		FixedPointNumber valFP = new FixedPointNumber("12069.50");
		BigFraction      valBF = BigFraction.of(1206950, 100);
		assertEquals(101.0, ((SimpleSecurityQuoteTable) simplPriceTab).convertFromBaseCurrency(valFP, secID).doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(101,   ((SimpleSecurityQuoteTable) simplPriceTab).convertFromBaseCurrencyRat(valBF, secID).getNumerator().intValue());
		assertEquals(1,     ((SimpleSecurityQuoteTable) simplPriceTab).convertFromBaseCurrencyRat(valBF, secID).getDenominator().intValue());

		secID = new KMMSecID("E000002");
		valFP = new FixedPointNumber("5883.25");
		valBF = BigFraction.of(588325, 100);
		assertEquals(101.0, ((SimpleSecurityQuoteTable) simplPriceTab).convertFromBaseCurrency(valFP, secID).doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(101,   ((SimpleSecurityQuoteTable) simplPriceTab).convertFromBaseCurrencyRat(valBF, secID).getNumerator().intValue());
		assertEquals(1,     ((SimpleSecurityQuoteTable) simplPriceTab).convertFromBaseCurrencyRat(valBF, secID).getDenominator().intValue());
	}
}
