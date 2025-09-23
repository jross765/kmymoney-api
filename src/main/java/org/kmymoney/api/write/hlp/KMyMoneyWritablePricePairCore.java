package org.kmymoney.api.write.hlp;

import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.hlp.KMyMoneyPricePairCore;
import org.kmymoney.api.write.KMyMoneyWritablePrice;
import org.kmymoney.api.write.KMyMoneyWritablePricePair;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;

/**
 * Auxiliary interface that defines methods that -- for practical reasons,
 * and this is not an error -- <strong>both</strong> the interfaces of 
 * writable prices and writable price pair are being derived from.
 * 
 * @see KMyMoneyWritablePrice
 * @see KMyMoneyWritablePricePair
 */
public interface KMyMoneyWritablePricePairCore extends KMyMoneyPricePairCore {

    void setFromSecCurrStr(String secCurr);
    
    void setToCurrStr(String curr);
    
    // ----------------------------
    
    void setFromSecCurrQualifID(KMMQualifSecCurrID qualifID);

    void setFromSecurityQualifID(KMMQualifSecID qualifID);

    void setFromCurrencyQualifID(KMMQualifCurrID qualifID);

    void setFromSecurity(KMyMoneySecurity sec);

    void setFromCurrencyCode(String code);

    void setFromCurrency(KMyMoneyCurrency curr);
    
    // ----------------------------

    void setToCurrencyQualifID(KMMQualifCurrID qualifID);

    void setToCurrencyCode(String code);

    void setToCurrency(KMyMoneyCurrency curr);
    
}
