package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMInstID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyAccountImpl {
	public static final KMMComplAcctID ACCT_1_ID = new KMMComplAcctID("A000004"); // Asset:Girokonto
	public static final KMMComplAcctID ACCT_2_ID = new KMMComplAcctID("A000062"); // Asset:Finanzanlagen::Depot RaiBa
	public static final KMMComplAcctID ACCT_3_ID = new KMMComplAcctID("A000049"); // Fremdkapital
	public static final KMMComplAcctID ACCT_4_ID = new KMMComplAcctID("A000064"); // Aktiva:Depots:Depot RaiBa:DE0007100000 Mercedes-Benz
	public static final KMMComplAcctID ACCT_8_ID = new KMMComplAcctID("A000063"); // Aktiva:Depots:Depot RaiBa:DE0007164600 SAP
	public static final KMMComplAcctID ACCT_9_ID = new KMMComplAcctID("A000046"); // Ausgabe:Wohnen:Nebenkosten:Gas

	// Top-level accounts
	public static final KMMComplAcctID ACCT_10_ID = KMMComplAcctID.get(KMMComplAcctID.Top.ASSET);
	public static final KMMComplAcctID ACCT_11_ID = KMMComplAcctID.get(KMMComplAcctID.Top.LIABILITY);
	public static final KMMComplAcctID ACCT_12_ID = KMMComplAcctID.get(KMMComplAcctID.Top.INCOME);
	public static final KMMComplAcctID ACCT_13_ID = KMMComplAcctID.get(KMMComplAcctID.Top.EXPENSE);
	public static final KMMComplAcctID ACCT_14_ID = KMMComplAcctID.get(KMMComplAcctID.Top.EQUITY);
	
	public static final KMMInstID INST_1_ID = TestKMyMoneyInstitutionImpl.INST_1_ID;
	public static final KMMInstID INST_2_ID = TestKMyMoneyInstitutionImpl.INST_2_ID;

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyAccount acct = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyAccountImpl.class);
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
	public void test01_1() throws Exception {
		acct = kmmFile.getAccountByID(ACCT_1_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_1_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.CHECKING, acct.getType());
		assertEquals(INST_1_ID, acct.getInstitutionID());
		assertEquals("RaiBa", acct.getInstitution().getName());
		assertEquals("Giro RaiBa", acct.getName());
		assertEquals("Anlagen:Barvermögen:Giro RaiBa", acct.getQualifiedName());
		assertEquals("Girokonto 1", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals("A000002", acct.getParentAccountID().toString());
		assertEquals(0, acct.getChildren().size());

		assertEquals(11674.50, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(11674.50, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(17, acct.getTransactions().size());
		assertEquals("T000000000000000001", acct.getTransactions().get(0).getID().toString());
		assertEquals("T000000000000000002", acct.getTransactions().get(1).getID().toString());
	}

	@Test
	public void test01_2() throws Exception {
		acct = kmmFile.getAccountByID(ACCT_2_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_2_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.INVESTMENT, acct.getType());
		assertEquals(INST_1_ID, acct.getInstitutionID());
		assertEquals("RaiBa", acct.getInstitution().getName());
		assertEquals("Depot RaiBa", acct.getName());
		assertEquals("Anlagen:Finanzanlagen:Depot RaiBa", acct.getQualifiedName());
		assertEquals("Aktiendepot 1", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals("A000061", acct.getParentAccountID().toString());
		assertEquals(3, acct.getChildren().size());
		Object[] acctArr = acct.getChildren().toArray();
		assertEquals("A000064", ((KMyMoneyAccount) acctArr[0]).getID().toString());
		assertEquals("A000063", ((KMyMoneyAccount) acctArr[1]).getID().toString());

		// ::TODO
		assertEquals(0.0, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(3773.0, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		// ::TODO
		assertEquals(0, acct.getTransactions().size());
		//    assertEquals("568864bfb0954897ab8578db4d27372f", acct.getTransactions().get(0).getID());
		//    assertEquals("18a45dfc8a6868c470438e27d6fe10b2", acct.getTransactions().get(1).getID());
	}

	@Test
	public void test01_3() throws Exception {
		acct = kmmFile.getAccountByID(ACCT_3_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_3_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.INCOME, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("Gehalt", acct.getName());
		assertEquals("Einnahme:Gehalt", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals(ACCT_12_ID, acct.getParentAccountID());

		List<KMyMoneyAccount> acctList = acct.getChildren();
		assertEquals(2, acctList.size());
		assertEquals("A000050", acctList.get(0).getID().toString());
		assertEquals("A000051", acctList.get(1).getID().toString());

		// ::CHECK: Really negative?
		assertEquals(0.00, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(-6500.00, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(0, acct.getTransactions().size());
	}

	@Test
	public void test01_4() throws Exception {
		acct = kmmFile.getAccountByID(ACCT_4_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_4_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.STOCK, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("DE0007100000 Mercedes-Benz Group AG", acct.getName());
		assertEquals("Anlagen:Finanzanlagen:Depot RaiBa:DE0007100000 Mercedes-Benz Group AG", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("SECURITY:E000002", acct.getQualifSecCurrID().toString());

		assertEquals(ACCT_2_ID, acct.getParentAccountID());
		assertEquals(0, acct.getChildren().size());

		assertEquals(34, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(34, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(1980.50, acct.getBalance(LocalDate.now(), Currency.getInstance("EUR")).doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(1980.50, acct.getBalanceRecursive(LocalDate.now(), Currency.getInstance("EUR")).doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(2, acct.getTransactions().size());
	}

	// -----------------------------------------------------------------
	// Top-level accounts

	@Test
	public void test01_10() throws Exception {
		acct = kmmFile.getAccountByID(ACCT_10_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_10_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.ASSET, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("Anlagen", acct.getName());
		assertEquals("Anlagen", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals(null, acct.getParentAccountID());

		List<KMyMoneyAccount> acctList = acct.getChildren();
		assertEquals(2, acctList.size());
		assertEquals("A000002", acctList.get(0).getID().toString());
		assertEquals("A000061", acctList.get(1).getID().toString());

		assertEquals(0.00, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(15597.50, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(0, acct.getTransactions().size());
	}

	@Test
	public void test01_11() throws Exception {
		acct = kmmFile.getAccountByID(ACCT_11_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_11_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.LIABILITY, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("Verbindlichkeiten", acct.getName());
		assertEquals("Verbindlichkeiten", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals(null, acct.getParentAccountID());
		assertEquals(1, acct.getChildren().size());
		Object[] acctArr = acct.getChildren().toArray();
		assertEquals("A000058", ((KMyMoneyAccount) acctArr[0]).getID().toString());

		assertEquals(0.00, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0.00, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(0, acct.getTransactions().size());
	}

	@Test
	public void test01_12() throws Exception {
		acct = kmmFile.getAccountByID(ACCT_12_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_12_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.INCOME, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("Einnahme", acct.getName());
		assertEquals("Einnahme", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals(null, acct.getParentAccountID());

		List<KMyMoneyAccount> acctList = acct.getChildren();
		assertEquals(4, acctList.size());
		assertEquals("A000049", acctList.get(0).getID().toString());
		assertEquals("A000052", acctList.get(1).getID().toString());
		assertEquals("A000068", acctList.get(2).getID().toString());
		assertEquals("A000053", acctList.get(3).getID().toString());

		// ::CHECK: Really negative?
		assertEquals(0.00, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(-16500.00, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(0, acct.getTransactions().size());
	}

	@Test
	public void test01_13() throws Exception {
		acct = kmmFile.getAccountByID(ACCT_13_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_13_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.EXPENSE, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("Ausgabe", acct.getName());
		assertEquals("Ausgabe", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals(null, acct.getParentAccountID());

		List<KMyMoneyAccount> acctList = acct.getChildren();
		assertEquals(16, acctList.size());
		assertEquals("A000072", acctList.get(0).getID().toString());
		assertEquals("A000006", acctList.get(1).getID().toString());
		assertEquals("A000011", acctList.get(2).getID().toString());
		// etc.
		// assertEquals("A000xyz", ((KMyMoneyAccount) acctArr[3]).getID().toString());

		assertEquals(0.00, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(920.5, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(0, acct.getTransactions().size());
	}

	@Test
	public void test01_14() throws Exception {
		acct = kmmFile.getAccountByID(ACCT_14_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_14_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.EQUITY, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("Eigenkapital", acct.getName());
		assertEquals("Eigenkapital", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals(null, acct.getParentAccountID());
		assertEquals(0, acct.getChildren().size());

		assertEquals(0.00, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0.00, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(0, acct.getTransactions().size());
	}

}
