package org.kmymoney.api.read.hlp;

import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;

/*
 * Auxiliary interface that defines methods that -- for practical reasons,
 * and this is not an error -- <strong>both</strong> the interfaces of 
 * prices and price pair are being derived from.
 * 
 * @see KMyMoneyPrice
 * @see KMyMoneyPricePair
 */
public interface KMyMoneyPricePairCore {

    /**
     * @return
     */
    String getFromSecCurrStr();
    
    /**
     * @return
     */
    String getToCurrStr();
    
    // ----------------------------
    
    /**
     * @return
     */
    KMMQualifSecCurrID getFromSecCurrQualifID();

    /**
     * @return
     */
    KMMQualifSecID getFromSecurityQualifID();

    /**
     * @return
     */
    KMMQualifCurrID getFromCurrencyQualifID();

    /**
     * @return
     */
    KMyMoneySecurity getFromSecurity();

    /**
     * @return
     */
    String getFromCurrencyCode();

    /**
     * @return
     */
    KMyMoneyCurrency getFromCurrency();
    
    // ----------------------------

    /**
     * @return
     */
    KMMQualifCurrID getToCurrencyQualifID();

    /**
     * @return
     */
    String getToCurrencyCode();

    /**
     * @return
     */
    KMyMoneyCurrency getToCurrency();
    
}
