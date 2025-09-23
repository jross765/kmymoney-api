package org.kmymoney.api.write.hlp;

import org.kmymoney.api.read.aux.KMMAddress;
import org.kmymoney.api.write.aux.KMMWritableAddress;

public interface HasWritableAddress {

    KMMWritableAddress getWritableAddress();
    
//  sic, not necessary / counter-productive:
//  KMMWritableAddress createWritableAddress();
    
//  dto.    
//	void removeAddress(KMMWritableAddress impl);

    void setAddress(KMMAddress adr);
    
}
