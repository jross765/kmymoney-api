package org.kmymoney.api.read.impl.hlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;

public class TestHasUserDefinedAttributesImpl {
	public static final KMMAcctID ACCT_1_ID = new KMMAcctID( "A000006" );
	public static final KMMAcctID ACCT_2_ID = new KMMAcctID( "A000062" );
	public static final KMMComplAcctID ACCT_3_ID = KMMComplAcctID.get(KMMComplAcctID.Top.ASSET);

	public static final KMMTrxID TRX_1_ID = new KMMTrxID( "T000000000000000009" );
	// public static final KMMTrxID TRX_2_ID = new KMMTrxID( "T000000000000000000" );
	// public static final KMMTrxID TRX_3_ID = new KMMTrxID( "T000000000000000000" );

	// public static final KMMSecID SEC_1_ID = new KMMSecID( "E000000" );
	public static final KMMSecID SEC_2_ID = new KMMSecID( "E000002" );
	public static final KMMSecID SEC_3_ID = new KMMSecID( "E000001" );

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestHasUserDefinedAttributesImpl.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.KMM_FILENAME);
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
	// Account
	// -----------------------------------------------------------------

	// No kvps
	@Test
	public void test_acct_01() throws Exception {
		KMyMoneyAccount acct = kmmFile.getAccountByID(ACCT_1_ID);
		assertNotEquals(null, acct);

		assertEquals(null, acct.getUserDefinedAttributeKeys());
	}

	// One kvp
	@Test
	public void test_acct_02() throws Exception {
		KMyMoneyAccount acct = kmmFile.getAccountByID(ACCT_2_ID);
		assertNotEquals(null, acct);

		assertNotEquals(null, acct.getUserDefinedAttributeKeys());
		assertEquals(1, acct.getUserDefinedAttributeKeys().size());
		assertEquals(ConstTest.KVP_KEY_ACCT_IBAN, acct.getUserDefinedAttributeKeys().get(0));
		assertEquals("DE01 1234 2345 3456 4567 99", acct.getUserDefinedAttribute(ConstTest.KVP_KEY_ACCT_IBAN));
	}

	// Account, several kvps
	// ::TODO

	// -----------------------------------------------------------------
	// Transaction
	// -----------------------------------------------------------------

	// No kvps
	@Test
	public void test_trx_01() throws Exception {
		KMyMoneyTransaction trx = kmmFile.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		assertEquals(null, trx.getUserDefinedAttributeKeys());
	}

	//    // One kvp
	//    @Test
	//    public void test_trx_02() throws Exception {
	//    	KMyMoneyTransaction trx = kmmFile.getTransactionByID(TRX_2_ID);
	//    	assertNotEquals(null, trx);
	//    	
	//    	assertNotEquals(null, trx.getUserDefinedAttributeKeys());
	//    	assertEquals(1, trx.getUserDefinedAttributeKeys().size());
	//    	assertEquals(ConstTest.SLOT_KEY_TRX_DATE_POSTED, trx.getUserDefinedAttributeKeys().get(0));
	//    	assertEquals("2023-07-01", trx.getUserDefinedAttribute(ConstTest.SLOT_KEY_TRX_DATE_POSTED));
	//    }
	//    
	//    // Several kvps
	//    @Test
	//    public void test_trx_03() throws Exception {
	//    	KMyMoneyTransaction trx = kmmFile.getTransactionByID(TRX_3_ID);
	//    	assertNotEquals(null, trx);
	//    	
	//    	assertNotEquals(null, trx.getUserDefinedAttributeKeys());
	//    	assertEquals(2, trx.getUserDefinedAttributeKeys().size());
	//    	assertEquals(ConstTest.SLOT_KEY_ASSOC_URI, trx.getUserDefinedAttributeKeys().get(0));
	//    	assertEquals(ConstTest.SLOT_KEY_TRX_DATE_POSTED, trx.getUserDefinedAttributeKeys().get(1));
	//    	assertEquals("https://my.transaction.link.01", trx.getUserDefinedAttribute(ConstTest.SLOT_KEY_ASSOC_URI));
	//    	assertEquals("2023-10-01", trx.getUserDefinedAttribute(ConstTest.SLOT_KEY_TRX_DATE_POSTED));
	//    }

	// -----------------------------------------------------------------
	// Transaction Split
	// -----------------------------------------------------------------

	// ::TODO
	// There are none with kvps

	// -----------------------------------------------------------------
	// Security
	// -----------------------------------------------------------------

	// No kvps
	// No such case

	// One kvp
	@Test
	public void test_sec_02() throws Exception {
		KMyMoneySecurity sec = kmmFile.getSecurityByID(SEC_2_ID);
		assertNotEquals(null, sec);

		assertNotEquals(null, sec.getUserDefinedAttributeKeys());
		assertEquals(1, sec.getUserDefinedAttributeKeys().size());
		assertEquals(ConstTest.KVP_KEY_SEC_SECURITY_ID, sec.getUserDefinedAttributeKeys().get(0));
		assertEquals("DE0007100000", sec.getUserDefinedAttribute(ConstTest.KVP_KEY_SEC_SECURITY_ID));
	}

	// Several kvps
	@Test
	public void test_sec_03() throws Exception {
		KMyMoneySecurity sec = kmmFile.getSecurityByID(SEC_3_ID);
		assertNotEquals(null, sec);

		assertNotEquals(null, sec.getUserDefinedAttributeKeys());
		assertEquals(2, sec.getUserDefinedAttributeKeys().size());
		assertEquals(ConstTest.KVP_KEY_SEC_ONLINE_SOURCE, sec.getUserDefinedAttributeKeys().get(0));
		assertEquals(ConstTest.KVP_KEY_SEC_SECURITY_ID, sec.getUserDefinedAttributeKeys().get(1));
		assertEquals("Finanztreff", sec.getUserDefinedAttribute(ConstTest.KVP_KEY_SEC_ONLINE_SOURCE));
		assertEquals("DE0007164600", sec.getUserDefinedAttribute(ConstTest.KVP_KEY_SEC_SECURITY_ID));
	}

	// -----------------------------------------------------------------
	// Stats/Meta
	// -----------------------------------------------------------------

	//    @Test
	//    public void test_meta() throws Exception {
	//    	for ( KMyMoneyTransactionSplit elt : kmmFile.getTransactionSplits() ) {
	//    		if ( elt.getUserDefinedAttributeKeys() != null ) {
	//    			if ( elt.getUserDefinedAttributeKeys().size() == 1 ) {
	//    				System.err.println("yyy splt: " + elt.getID() );
	//    			}
	//    		}
	//    	}
	//
	//    	for ( KMyMoneySecurity elt : kmmFile.getCommodities() ) {
	//    		if ( elt.getUserDefinedAttributeKeys() != null ) {
	//    			if ( elt.getUserDefinedAttributeKeys().size() == 1 ) {
	//    				System.err.println("yyy sec: " + elt.getQualifID() );
	//    			}
	//    		}
	//    	}
	//    }

}
