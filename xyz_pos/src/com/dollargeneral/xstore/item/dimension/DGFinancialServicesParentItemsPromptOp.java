package com.dollargeneral.xstore.item.dimension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dtv.data2.access.DataFactory;
import dtv.data2.access.IQueryKey;
import dtv.data2.access.QueryKey;
import dtv.pos.common.ConfigurationMgr;
import dtv.pos.common.OpChainKey;
import dtv.pos.framework.op.AbstractListPromptOp;
import dtv.pos.framework.scope.ValueKey;
import dtv.pos.iframework.event.IXstEvent;
import dtv.pos.iframework.op.IOpResponse;
import dtv.xst.dao.itm.IItem;
import xyz.pos.DGConfigurationMgr;

public class DGFinancialServicesParentItemsPromptOp extends AbstractListPromptOp {

	private static final IQueryKey<IItem> FS_PARENT_ITEMS = new QueryKey<IItem>("FS_PARENT_ITEMS", IItem.class);
	private static final ValueKey<String> CURRENT_PARENT_ITEM = new ValueKey<String>(String.class,
			"CURRENT_PARENT_ITEM");

	@Inject
	DGConfigurationMgr configMgr;

	@Override
	public String getDefaultPromptKey() {
		return "FS_PARENT_ITEMS_RESULTS";
	}

	@Override
	public IOpResponse handlePromptResponse(IXstEvent event) {
		Object item = event.getData();
		if (item instanceof IItem) {
			String parentItemId = ((IItem) item).getItemId();
			this._transactionScope.setValue(CURRENT_PARENT_ITEM, parentItemId);

			return this.HELPER.getCompleteStackChainResponse(OpChainKey.valueOf("DG_ITEM_DIMENSIONS"));
		}
		return HELPER.completeResponse();
	}

	@Override
	protected String getEmptyListPromptKey() {
		return "FS_PARENT_ITEMS_NOT_AVAILABLE";
	}

	@Override
	protected Object[] getPromptList(IXstEvent paramIXstEvent) {
		Map<String, Object> params = new HashMap<>();

		params.put("argOrganizationId", Long.valueOf(ConfigurationMgr.getOrganizationId()));
		for (int i = 1; i <= 4; i++) {
			String paramKey = "@argMerchlevel" + i + "Id";
			params.put(paramKey, configMgr.getMerchandiseHierarchyForNonMerchParentItems());
		}

		List<IItem> queryResults = (List<IItem>) DataFactory.getObjectByQueryNoThrow(FS_PARENT_ITEMS, params);

		return queryResults.toArray();
	}

}
