package com.wackysire;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(WackySireConfig.GROUP)
public interface WackySireConfig extends Config
{
	String GROUP = "wackysire";

	@ConfigItem(keyName = "keepOriginals", name = "Keep original tentacles visible",
		description = "Draw both models. Replacement mode hides the original client clickboxes.", position = 0)
	default boolean keepOriginals()
	{
		return false;
	}
}
