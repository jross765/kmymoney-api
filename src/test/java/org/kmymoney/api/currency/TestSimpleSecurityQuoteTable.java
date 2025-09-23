package org.kmymoney.api.currency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;

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

		simplPriceTab = complPriceTab.getByNamespace(KMMQualifSecCurrID.Type.SECURITY);
		assertNotEquals(null, simplPriceTab);

		assertEquals(3, simplPriceTab.getCurrencies().size());
		assertEquals(119.50, simplPriceTab.getConversionFactor("E000001").doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(58.25, simplPriceTab.getConversionFactor("E000002").doubleValue(), ConstTest.DIFF_TOLERANCE);
	}

	@Test
	public void test02_1() throws Exception {
		complPriceTab = kmmFile.getCurrencyTable();
		assertNotEquals(null, complPriceTab);

		simplPriceTab = complPriceTab.getByNamespace(KMMQualifSecCurrID.Type.SECURITY);
		assertNotEquals(null, simplPriceTab);

		FixedPointNumber val = new FixedPointNumber("101.0");
		assertEquals(true, simplPriceTab.convertToBaseCurrency(val, "E000001"));
		assertEquals(12069.50, val.doubleValue(), ConstTest.DIFF_TOLERANCE);

		val = new FixedPointNumber("101.0");
		assertEquals(true, simplPriceTab.convertToBaseCurrency(val, "E000002"));
		assertEquals(5883.25, val.doubleValue(), ConstTest.DIFF_TOLERANCE);
	}

	@Test
	public void test02_2() throws Exception {
		complPriceTab = kmmFile.getCurrencyTable();
		assertNotEquals(null, complPriceTab);

		simplPriceTab = complPriceTab.getByNamespace(KMMQualifSecCurrID.Type.SECURITY);
		assertNotEquals(null, simplPriceTab);

		FixedPointNumber val = new FixedPointNumber("12069.50");
		assertEquals(true, simplPriceTab.convertFromBaseCurrency(val, "E000001"));
		assertEquals(101.0, val.doubleValue(), ConstTest.DIFF_TOLERANCE);

		val = new FixedPointNumber("5883.25");
		assertEquals(true, simplPriceTab.convertFromBaseCurrency(val, "E000002"));
		assertEquals(101.0, val.doubleValue(), ConstTest.DIFF_TOLERANCE);
	}
}
