package com.wackysire;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WackySireLauncher
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(WackySirePlugin.class);
		RuneLite.main(args);
	}
}
