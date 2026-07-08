package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyTransactionImpl {
	public static final KMMTrxID TRX_1_ID = new KMMTrxID("T000000000000000001");
	public static final KMMTrxID TRX_2_ID = new KMMTrxID("T000000000000000002");

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyTransaction trx = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyTransactionImpl.class);
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
		trx = kmmFile.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		assertEquals(TRX_1_ID, trx.getID());

		assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0, trx.getBalanceRat().getNumerator().longValue());
		assertEquals(1, trx.getBalanceRat().getDenominator().longValue());
		assertEquals("0,00 €", trx.getBalanceFormatted()); // ::TODO: locale-specific!
		
		assertEquals("", trx.getMemo());
		assertEquals("2023-01-01", trx.getDatePosted().toString());
		assertEquals("2023-01-01", trx.getDatePostedFormatted());
		assertEquals("2023-11-03", trx.getDateEntered().toString());
		assertEquals("2023-11-03", trx.getDateEnteredFormatted());

		assertEquals(2, trx.getSplitsCount());
		assertEquals("S0001", trx.getSplits().get(0).getID().toString());
		assertEquals("S0002", trx.getSplits().get(1).getID().toString());
	}

	@Test
	public void test02() throws Exception {
		trx = kmmFile.getTransactionByID(TRX_2_ID);
		assertNotEquals(null, trx);

		assertEquals(TRX_2_ID, trx.getID());

		assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0, trx.getBalanceRat().getNumerator().longValue());
		assertEquals(1, trx.getBalanceRat().getDenominator().longValue());
		assertEquals("0,00 €", trx.getBalanceFormatted()); // ::TODO: locale-specific!
		
		assertEquals("", trx.getMemo());
		assertEquals("2023-01-03", trx.getDatePosted().toString());
		assertEquals("2023-01-03", trx.getDatePostedFormatted());
		assertEquals("2023-10-14", trx.getDateEntered().toString());
		assertEquals("2023-10-14", trx.getDateEnteredFormatted());

		assertEquals(2, trx.getSplitsCount());
		assertEquals("S0001", trx.getSplits().get(0).getID().toString());
		assertEquals("S0002", trx.getSplits().get(1).getID().toString());
	}

	@Test
	public void test04_1() throws Exception {
		trx = kmmFile.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		// System.err.println("'" + trx.toXMLString() + "'");
		assertEquals("""
				<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
				<TRANSACTION id="T000000000000000001" commodity="EUR" entrydate="2023-11-03" postdate="2023-01-01" memo="">
				    <SPLITS>
				        <SPLIT account="A000004" action="" bankid="" id="S0001" memo="" number="" payee="P000004" price="1/1" reconciledate="" reconcileflag="0" shares="10000/1" value="10000/1"/>
				        <SPLIT account="A000053" action="" bankid="" id="S0002" memo="" number="" payee="P000004" price="1/1" reconciledate="" reconcileflag="0" shares="-10000/1" value="-10000/1"/>
				    </SPLITS>
				</TRANSACTION>""" + "\n", 
				trx.toXMLString());
	}

	// This test case is not interesting in and of itself -- we don't want to 
	// test the JAXB implementation, after all (and we have covered lots of 
	// the generated code indirectly with all the other tests). 
	// Rather, it must be seen in the context of elaborate tests in module 
	// "API Extensions"; it's a sort of preparatory step for them.
	// Cf. TestTransactionMergerVar3 there. 
	// In short: In effect, We are merely testing our helper method 
	// KMyMoneyTransactionImpl.fromXMLString() here, which is very short
	// and straight-forward, anyway.
	@Test
	public void test05() throws Exception {
		KMyMoneyTransaction refTrx = kmmFile.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, refTrx);
		
		// ---

		// copied from test data file:
		String xmlTrxStr = """
				<TRANSACTION id="T000000000000000001" postdate="2023-01-01" memo="" entrydate="2023-11-03" commodity="EUR">
					<SPLITS>
						<SPLIT id="S0001" payee="P000004" reconciledate="" action="" reconcileflag="0" value="10000/1" shares="10000/1" price="1/1" memo="" account="A000004" number="" bankid=""/>
						<SPLIT id="S0002" payee="P000004" reconciledate="" action="" reconcileflag="0" value="-10000/1" shares="-10000/1" price="1/1" memo="" account="A000053" number="" bankid=""/>
					</SPLITS>
				</TRANSACTION>
				""";
		
		KMyMoneyTransactionImpl trx = KMyMoneyTransactionImpl.fromXMLString(xmlTrxStr, kmmFile);
		
		// ---

		assertEquals(refTrx.toString(), trx.toString());
		assertEquals(refTrx.getID(), trx.getID());
		assertEquals(refTrx.getDatePosted(), trx.getDatePosted());
		assertEquals(refTrx.getDateEntered(), trx.getDateEntered());
		assertEquals(refTrx.getBalanceRat(), trx.getBalanceRat());
		assertEquals(refTrx.getMemo(), trx.getMemo());
		
		assertEquals(refTrx.getSplitsCount(), trx.getSplitsCount());
		
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() )
		{
			KMyMoneyTransactionSplit refSplt = refTrx.getSplitByID(splt.getID()); 
			assertEquals(refSplt.toString(), splt.toString());
			assertEquals(refSplt.getID(), splt.getID());
			assertEquals(refSplt.getNumber(), splt.getNumber());
			assertEquals(refSplt.getAccountID(), splt.getAccountID());
			assertEquals(refSplt.getPayeeID(), splt.getPayeeID());
			assertEquals(refSplt.getValueRat(), splt.getValueRat());
			assertEquals(refSplt.getSharesRat(), splt.getSharesRat());
			assertEquals(refSplt.getMemo(), splt.getMemo());
		}
	}

}
