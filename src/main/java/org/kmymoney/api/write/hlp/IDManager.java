package org.kmymoney.api.write.hlp;

import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

public interface IDManager {
	
	KMMInstID getNewInstitutionID();

	KMMAcctID getNewAccountID();

	KMMTrxID getNewTransactionID();

	KMMSpltID getNewSplitID();

	KMMPyeID getNewPayeeID();

	KMMTagID getNewTagID();

	KMMSecID getNewSecurityID();

}
