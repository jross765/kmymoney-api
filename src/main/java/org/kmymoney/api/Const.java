package org.kmymoney.api;

import org.kmymoney.api.read.KMMSecCurr;

public class Const {
  
  public static final String KVP_KEY_ACCT_IBAN         = "iban";
  
  public static final String KVP_KEY_SEC_SECURITY_ID   = "kmm-security-id";  
  public static final String KVP_KEY_SEC_ONLINE_SOURCE = "kmm-online-source";
  
  public static final String KVP_KEY_ASSOC_URI         = "assoc_uri";
  public static final String KVP_KEY_DUMMY             = "dummy";

  // -----------------------------------------------------------------

  public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd";

  public static final String STANDARD_DATE_FORMAT_BOOK = "yyyy-MM-dd";

  // -----------------------------------------------------------------

  public static final String DEFAULT_CURRENCY = "EUR";

  // -----------------------------------------------------------------

  public static final double DIFF_TOLERANCE = 0.005;

  // -----------------------------------------------------------------

  public static final KMMSecCurr.Type           SEC_TYPE_DEFAULT         = KMMSecCurr.Type.STOCK;
  public static final int                       SEC_PP_DEFAULT           = 2;
  public static final int                       SEC_SAF_DEFAULT          = 100;
  public static final KMMSecCurr.RoundingMethod SEC_ROUNDMETH_DEFAULT    = KMMSecCurr.RoundingMethod.ROUND;
  public static final String                    SEC_SYMBOL_DEFAULT       = "DE000000001"; // pseudo-ISIN
  
}
