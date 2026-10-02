package com.wackysire;

import com.google.inject.Provides;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Model;
import net.runelite.api.NPC;
import net.runelite.api.Renderable;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PostClientTick;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name = "Wacky Sire",
	description = "Cosmetic flailing inflatable tube men in place of Abyssal Sire tentacles",
	tags = {"sire", "abyssal", "tentacle", "cosmetic", "inflatable"})
public class WackySirePlugin extends Plugin implements RenderCallback
{
	private static final Logger log = LoggerFactory.getLogger(WackySirePlugin.class);
	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private WackySireConfig config;
	@Inject private RenderCallbackManager renderCallbacks;
	@Inject private TubeModelFactory modelFactory;
	private final Map<NPC, TubeMan> tubes = new IdentityHashMap<>();
	private volatile boolean running;
	private boolean modelFailure;
	private int ticks;

	@Provides
	WackySireConfig provideConfig(ConfigManager manager)
	{
		return manager.getConfig(WackySireConfig.class);
	}

	@Override
	protected void startUp()
	{
		running = true;
		clientThread.invoke(() ->
		{
			if (running)
			{
				modelFailure = false;
				ticks = 0;
			}
		});
		renderCallbacks.register(this);
		// PostClientTick seeds NPCs already loaded when enabled and retries pending assets.
	}

	@Override
	protected void shutDown()
	{
		running = false;
		renderCallbacks.unregister(this);
		clientThread.invoke(() ->
		{
			clear();
			modelFactory.clear();
		});
	}

	@Override
	public boolean addEntity(Renderable renderable, boolean ui)
	{
		// Keep original UI and fail open if a replacement is not active.
		if (!running || ui || config.keepOriginals() || !(renderable instanceof NPC))
		{
			return true;
		}
		NPC npc = (NPC) renderable;
		TubeMan tube = tubes.get(npc);
		return !TentacleIds.matches(npc.getId()) || tube == null || !tube.object.isActive();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		track(event.getNpc());
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		NPC npc = event.getNpc();
		if (TentacleIds.matches(npc.getId()))
		{
			track(npc);
		}
		else
		{
			remove(npc);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		remove(event.getNpc());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			clear(); // Loading, hopping and logout cannot leave old scene objects behind.
		}
	}

	@Subscribe
	public void onPostClientTick(PostClientTick event)
	{
		if (!running || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		ticks++;
		// Scan twice a second for enable-mid-fight and cache download retries.
		if (ticks == 1 || ticks % 25 == 0)
		{
			for (NPC npc : client.getTopLevelWorldView().npcs())
			{
				track(npc);
			}
		}
		Iterator<Map.Entry<NPC, TubeMan>> iterator = tubes.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<NPC, TubeMan> entry = iterator.next();
			NPC npc = entry.getKey();
			TubeMan tube = entry.getValue();
			LocalPoint point = npc.getLocalLocation();
			if (!TentacleIds.matches(npc.getId()) || point == null ||
				point.getWorldView() != client.getTopLevelWorldView().getId())
			{
				tube.object.setActive(false);
				iterator.remove();
				continue;
			}
			tube.object.setLocation(point, client.getTopLevelWorldView().getPlane());
			tube.object.setOrientation(npc.getCurrentOrientation());
			int frame = TubeModelFactory.frame(ticks * 0.02, tube.phase);
			if (frame != tube.lastFrame)
			{
				TubeModelFactory.animate(tube.model, frame);
				tube.lastFrame = frame;
			}
			if (!tube.object.isActive())
			{
				tube.object.setActive(true);
			}
		}
	}

	private void track(NPC npc)
	{
		if (!running || modelFailure || client.getGameState() != GameState.LOGGED_IN ||
			!TentacleIds.matches(npc.getId()) || tubes.containsKey(npc))
		{
			return;
		}
		LocalPoint point = npc.getLocalLocation();
		if (point == null || point.getWorldView() != client.getTopLevelWorldView().getId())
		{
			return;
		}
		try
		{
			// Stable colors and phases from the location, independent of combat state.
			int seed = point.getSceneX() * 31 + point.getSceneY();
			int phase = Math.floorMod(seed, TubeModelFactory.FRAME_COUNT);
			Model model = modelFactory.create(seed, phase);
			if (model == null)
			{
				return;
			}
			RuneLiteObject object = new RuneLiteObject(client);
			object.setModel(model);
			object.setLocation(point, client.getTopLevelWorldView().getPlane());
			object.setOrientation(npc.getCurrentOrientation());
			object.setRenderMode(Renderable.RENDERMODE_SORTED);
			object.setActive(true);
			tubes.put(npc, new TubeMan(object, model, phase));
		}
		catch (RuntimeException ex)
		{
			modelFailure = true;
			clear();
			log.warn("Wacky Sire model creation failed; keeping original tentacles visible. Re-enable to retry.", ex);
		}
	}

	private void remove(NPC npc)
	{
		TubeMan tube = tubes.remove(npc);
		if (tube != null)
		{
			tube.object.setActive(false);
		}
	}

	private void clear()
	{
		for (TubeMan tube : tubes.values())
		{
			tube.object.setActive(false);
		}
		tubes.clear();
	}

	private static final class TubeMan
	{
		private final RuneLiteObject object;
		private final Model model;
		private final int phase;
		private int lastFrame;

		private TubeMan(RuneLiteObject object, Model model, int phase)
		{
			this.object = object;
			this.model = model;
			this.phase = phase;
			this.lastFrame = phase;
		}
	}
}
