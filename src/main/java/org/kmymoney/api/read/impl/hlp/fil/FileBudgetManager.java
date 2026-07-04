package org.kmymoney.api.read.impl.hlp.fil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kmymoney.api.generated.BUDGET;
import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.api.read.impl.KMyMoneyBudgetImpl;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public class FileBudgetManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileBudgetManager.class);

	// ---------------------------------------------------------------

	protected KMyMoneyFileImpl kmmFile;

	protected Map<KMMBdgtID, KMyMoneyBudget> bdgtMap;

	// ---------------------------------------------------------------

	public FileBudgetManager(KMyMoneyFileImpl kmmFile) {
		this.kmmFile = kmmFile;
		init(kmmFile.getRootElement());
	}

	// ---------------------------------------------------------------

	private void init(final KMYMONEYFILE pRootElement) {
		bdgtMap = new HashMap<KMMBdgtID, KMyMoneyBudget>();

		for ( BUDGET jwsdpBdgt : pRootElement.getBUDGETS().getBUDGET() ) {
			try {
				KMyMoneyBudgetImpl bdgt = createBudget(jwsdpBdgt);
				bdgtMap.put(bdgt.getID(), bdgt);
			} catch (RuntimeException e) {
				LOGGER.error("init: [RuntimeException] Problem in " + getClass().getName() + ".init: "
						+ "ignoring illegal Budget-Entry with id=" + jwsdpBdgt.getId(), e);
			}
		} // for

		LOGGER.debug("init: No. of entries in budget map: " + bdgtMap.size());
	}

	protected KMyMoneyBudgetImpl createBudget(final BUDGET jwsdpBdgt) {
		KMyMoneyBudgetImpl bdgt = new KMyMoneyBudgetImpl(jwsdpBdgt, kmmFile);
		LOGGER.debug("createBudget: Generated new budget: " + bdgt.getID());
		return bdgt;
	}

	// ---------------------------------------------------------------

	public KMyMoneyBudget getBudgetByID(final KMMBdgtID bdgtID) {
		if ( bdgtID == null ) {
			throw new IllegalArgumentException("argument <bdgtID> is null");
		}

		if ( ! bdgtID.isSet() ) {
			throw new IllegalArgumentException("argument <bdgtID> is not set");
		}

		if ( bdgtMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		KMyMoneyBudget retval = bdgtMap.get(bdgtID);
		if ( retval == null ) {
			LOGGER.warn("getBudgetByID: No Budget with ID '" + bdgtID + "'. We know " + bdgtMap.size() + " budgets.");
		}

		return retval;
	}

	public List<KMyMoneyBudget> getBudgetsByName(final String expr) {
		if ( expr == null ) {
			throw new IllegalArgumentException("argument <expr> is null");
		}

		if ( expr.isBlank() ) {
			throw new IllegalArgumentException("argument <expr> is blank");
		}

		return getBudgetsByName(expr, true);
	}

	public List<KMyMoneyBudget> getBudgetsByName(final String expr, boolean relaxed) {
		if ( expr == null ) {
			throw new IllegalArgumentException("argument <expr> is null");
		}

		if ( expr.isBlank() ) {
			throw new IllegalArgumentException("argument <expr> is blank");
		}

		if ( bdgtMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		List<KMyMoneyBudget> result = new ArrayList<KMyMoneyBudget>();

		for ( KMyMoneyBudget bdgt : getBudgets() ) {
			if ( bdgt.getName() != null ) {
				if ( relaxed ) {
					if ( bdgt.getName().toLowerCase().contains(expr.trim().toLowerCase()) ) {
						result.add(bdgt);
					}
				} else {
					if ( bdgt.getName().equals(expr) ) {
						result.add(bdgt);
					}
				}
			}
		}

		return result;
	}

	public KMyMoneyBudget getBudgetsByNameUniq(final String expr)
			throws NoEntryFoundException, TooManyEntriesFoundException {
		if ( expr == null ) {
			throw new IllegalArgumentException("argument <expr> is null");
		}

		if ( expr.isBlank() ) {
			throw new IllegalArgumentException("argument <expr> is blank");
		}

		List<KMyMoneyBudget> bdgtList = getBudgetsByName(expr, false);
		if ( bdgtList.size() == 0 )
			throw new NoEntryFoundException();
		else if ( bdgtList.size() > 1 )
			throw new TooManyEntriesFoundException();
		else
			return bdgtList.get(0);
	}

	public Collection<KMyMoneyBudget> getBudgets() {
		if ( bdgtMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		return Collections.unmodifiableCollection(bdgtMap.values());
	}

	// ---------------------------------------------------------------

	public int getNofEntriesBudgetMap() {
		return bdgtMap.size();
	}

}
