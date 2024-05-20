package com.dollargeneral.xstore.item.dimension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import dtv.i18n.IFormattable;
import dtv.pos.common.ValueKeys;
import dtv.pos.framework.op.OpState;
import dtv.pos.iframework.event.IXstEvent;
import dtv.pos.iframework.op.IOpResponse;
import dtv.pos.iframework.op.IOpState;
import dtv.pos.inventory.lookup.style.DimensionInfo;
import dtv.pos.register.ItemLocator;
import dtv.pos.register.sale.CheckStyleLookupOp;
import dtv.util.SortOrderComparator;
import dtv.xst.dao.itm.IDimensionSummary;
import dtv.xst.dao.itm.IItem;
import dtv.xst.dao.itm.IItemDimensionType;
import oracle.retail.xstore.inv.impl.lookup.ItemStyleHelper;

public class DGCheckStyleLookupOp extends CheckStyleLookupOp {

	@Inject
	ItemStyleHelper styleHelper;

	private final IOpState POST_PROMPT = new OpState("POST_PROMPT");
	private Iterator<IItemDimensionType> _dimensionIterator;
	private List<DimensionInfo> _dimensionList;

	@Override
	public IOpResponse handleOpExec(final IXstEvent argEvent) {
		IOpState state = this.getOpState();
		IItem style = (IItem) this.getScopedValue(ValueKeys.CURRENT_ITEM);
		if (state == null) {
			List<IItemDimensionType> dimensionSystem = style.getItemDimensionTypes();
			this._dimensionIterator = dimensionSystem.iterator();
			this._dimensionList = new ArrayList<DimensionInfo>();
		} else if (this.POST_PROMPT.equals(state)) {
			IDimensionSummary value = (IDimensionSummary) argEvent.getData();
			int index = this._dimensionList.isEmpty() ? 0 : this._dimensionList.size() - 1;
			DimensionInfo info = (DimensionInfo) this._dimensionList.get(index);
			info.setValue(value.getDimensionValue());
		}

		if (this._dimensionIterator.hasNext()) {
			this.setOpState(this.POST_PROMPT);
			IItemDimensionType dimensionType = (IItemDimensionType) this._dimensionIterator.next();
			DimensionInfo info = new DimensionInfo();
			info.setDimension(dimensionType.getDimension());
			info.setSequence(dimensionType.getSequence());
			this._dimensionList.add(info);
			List<? extends IDimensionSummary> values = getDimensions(dimensionType);
			String promptMessage = dimensionType.getPromptMessage();
			IOpResponse response = null;
			if (!StringUtils.isEmpty(promptMessage)) {
				IFormattable msg = this._formattables.getSimpleFormattable(promptMessage);
				String promptKey = "ENTER_ITEM_DIMENSION_CUSTOM";
				response = this.HELPER.getListPromptResponse(promptKey, values.toArray(), new IFormattable[] { msg });
			} else {
				String dimDesc = dimensionType.getDescription();
				IFormattable arg = this._formattables.getSimpleFormattable(dimDesc);
				IFormattable[] args = new IFormattable[] { arg, this._formattables.getLiteral(style.getDescription()) };
				String promptKey = "ENTER_ITEM_DIMENSION";
				response = this.HELPER.getListPromptResponse(promptKey, values.toArray(), args);
			}

			return response;
		} else {
			return this.handleLookup(style, this._dimensionList);
		}
	}

	List<String> dimensionValuesToConsider = null;

	protected List<? extends IDimensionSummary> getDimensions(IItemDimensionType argDimensionType) {
		IItem style = (IItem) this.getScopedValue(ValueKeys.CURRENT_ITEM);
		List<? extends IDimensionSummary> values = argDimensionType.getDimensionValues();
		List<DimensionInfo> filteredDimensions = this._dimensionList.stream()
				.filter(d -> !StringUtils.isEmpty(d.getValue())).collect(Collectors.toList());
		if (filteredDimensions != null && filteredDimensions.isEmpty()) {
			Collections.sort(values, new SortOrderComparator());
			return values;
		}
		List<IItem> items = ItemLocator.getLocator().lookupWithDimensions(style, filteredDimensions);
		if (this._dimensionList.size() == 1) {
			dimensionValuesToConsider = items.stream().map(item -> item.getDimension1()).distinct()
					.collect(Collectors.toList());
		} else if (this._dimensionList.size() == 2) {
			dimensionValuesToConsider = items.stream().map(item -> item.getDimension2()).distinct()
					.collect(Collectors.toList());
		} else if (this._dimensionList.size() == 3) {
			dimensionValuesToConsider = items.stream().map(item -> item.getDimension3()).distinct()
					.collect(Collectors.toList());
		}
		values = values.stream().filter(dimension -> dimensionValuesToConsider.contains(dimension.getDimensionValue()))
				.collect(Collectors.toList());
		Collections.sort(values, new SortOrderComparator());
		dimensionValuesToConsider = null;
		return values;
	}
}
