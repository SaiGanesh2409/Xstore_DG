package xyz.pos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import dtv.pos.common.SysConfigSettingFactory;

public class DGConfigurationMgr {

	@Inject
	private SysConfigSettingFactory _settingsFactory;

	public List<String> getMerchandiseHierarchyForNonMerchParentItems() {
		String setting = this._settingsFactory.getString(new String[] { "NonMerch---FSParentItemsHierarchyIds" });
		return getStringList(setting);
	}

	private static List<String> getStringList(String param) {
		List<String> result;
		if (!StringUtils.isEmpty(param)) {
			String[] values = param.split(",");
			result = Arrays.asList(values);
		} else {
			result = Collections.emptyList();
		}
		return result;
	}
}
