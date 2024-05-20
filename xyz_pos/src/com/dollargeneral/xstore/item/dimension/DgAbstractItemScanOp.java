package com.dollargeneral.xstore.item.dimension;

import dtv.hardware.types.HardwareType;
import dtv.pos.common.ValueKeys;
import dtv.pos.framework.op.Operation;
import dtv.pos.framework.scope.ValueKey;
import dtv.pos.iframework.event.IXstEvent;
import dtv.pos.iframework.op.IOpResponse;
import dtv.pos.register.ItemLocator;
import dtv.xst.dao.itm.IItem;

public class DgAbstractItemScanOp extends Operation {

	private static final ValueKey<String> CURRENT_PARENT_ITEM = new ValueKey<String>(String.class,
			"CURRENT_PARENT_ITEM");

	@Override
	public IOpResponse handleOpExec(IXstEvent var1) {

		String itemId = this._transactionScope.getValue(CURRENT_PARENT_ITEM);

		this.setScopedValue(ValueKeys.ENTERED_ITEM_ID, itemId);
		this.setScopedValue(ValueKeys.VALUE_ENTRY_METHOD, HardwareType.forUse("KEYBOARD", "KEYBOARD"));
		IItem item = ItemLocator.getLocator().lookupItem(itemId);
		this.setScopedValue(ValueKeys.CURRENT_ITEM, item);

		return this.HELPER.completeResponse();

	}

}
