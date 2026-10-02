package com.wackysire;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.IndexedObjectSet;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.NPC;
import net.runelite.api.RuneLiteObjectController;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.PostClientTick;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.callback.ClientThread;
import org.junit.Test;
import static org.junit.Assert.*;

/** Native rendering still needs an in-game check; these exercise the lifecycle and cache isolation. */
public class WackySirePluginTest
{
	@Test
	public void enablingMidFightCreatesOneReplacementAndDisableRestoresOriginals() throws Exception
	{
		Fixture f = new Fixture();
		f.plugin.startUp();
		assertTrue(f.callbacks.addEntity(f.npc, false)); // No replacement yet: fail open.
		f.plugin.onPostClientTick(new PostClientTick());
		assertEquals(1, f.active.size());
		assertFalse(f.callbacks.addEntity(f.npc, false));
		assertTrue(f.callbacks.addEntity(f.npc, true)); // UI stays available.
		for (int i = 0; i < 100; i++) { f.plugin.onPostClientTick(new PostClientTick()); }
		assertEquals(1, f.active.size());
		f.keepOriginals = true;
		assertTrue(f.callbacks.addEntity(f.npc, false));
		f.plugin.shutDown();
		assertTrue(f.active.isEmpty());
		assertTrue(f.callbacks.addEntity(f.npc, false));
	}

	@Test
	public void npcTransformsPreserveIdentityAndDespawnRemovesTheObject() throws Exception
	{
		Fixture f = new Fixture();
		f.plugin.startUp();
		f.plugin.onPostClientTick(new PostClientTick());
		RuneLiteObjectController first = f.active.iterator().next();
		f.npcId = 5913;
		f.plugin.onNpcChanged(new NpcChanged(f.npc, null));
		assertSame(first, f.active.iterator().next());
		f.plugin.onNpcDespawned(new NpcDespawned(f.npc));
		assertTrue(f.active.isEmpty());
		assertTrue(f.callbacks.addEntity(f.npc, false));
		f.plugin.shutDown();
	}

	@Test
	public void logoutClearsObjectsAndTheNextLoginReseedsThem() throws Exception
	{
		Fixture f = new Fixture();
		f.plugin.startUp();
		f.plugin.onPostClientTick(new PostClientTick());
		GameStateChanged event = new GameStateChanged();
		event.setGameState(GameState.LOGIN_SCREEN);
		f.gameState = GameState.LOGIN_SCREEN;
		f.plugin.onGameStateChanged(event);
		assertTrue(f.active.isEmpty());
		f.plugin.onPostClientTick(new PostClientTick());
		assertTrue(f.active.isEmpty());
		f.gameState = GameState.LOGGED_IN;
		for (int i = 0; i < 25; i++) { f.plugin.onPostClientTick(new PostClientTick()); }
		assertEquals(1, f.active.size());
		f.plugin.shutDown();
	}

	@Test
	public void missingAssetsStayVisibleAndRetryWithoutAPluginRestart() throws Exception
	{
		Fixture f = new Fixture();
		f.ready = false;
		f.plugin.startUp();
		f.plugin.onPostClientTick(new PostClientTick());
		assertTrue(f.active.isEmpty());
		assertTrue(f.callbacks.addEntity(f.npc, false));
		f.ready = true;
		for (int i = 0; i < 25; i++) { f.plugin.onPostClientTick(new PostClientTick()); }
		assertEquals(1, f.active.size());
		f.plugin.shutDown();
	}

	@Test
	public void changedCachePrimitiveFailsOpen() throws Exception
	{
		Fixture f = new Fixture();
		f.seed = new FakeMesh(2);
		f.plugin.startUp();
		f.plugin.onPostClientTick(new PostClientTick());
		assertTrue(f.active.isEmpty());
		assertTrue(f.callbacks.addEntity(f.npc, false));
		f.plugin.shutDown();
	}

	@Test
	public void nonTentacleAndOutOfViewNpcsAreNeverHidden() throws Exception
	{
		Fixture f = new Fixture();
		f.npcId = 5914; // Respiratory system.
		f.plugin.startUp();
		f.plugin.onPostClientTick(new PostClientTick());
		assertTrue(f.active.isEmpty());
		assertTrue(f.callbacks.addEntity(f.npc, false));
		f.npcId = 5912;
		f.worldViewId = 2;
		for (int i = 0; i < 25; i++) { f.plugin.onPostClientTick(new PostClientTick()); }
		assertTrue(f.active.isEmpty());
		assertTrue(f.callbacks.addEntity(f.npc, false));
		f.plugin.shutDown();
	}

	@Test
	public void untexturedCacheModelCreatesAReplacement()
	{
		Fixture f = new Fixture();
		assertNull(f.seed.data().getFaceTextures());
		Model model = new TubeModelFactory(f.client).create(0, 0);
		assertNotNull(model);
		assertEquals(TubeMesh.pose(0, 0).getFaces().size(), model.getFaceCount());
	}

	@Test
	public void meshCreationAndAnimationDoNotMutateTheCacheOrAnotherTentacle()
	{
		Fixture f = new Fixture();
		TubeModelFactory factory = new TubeModelFactory(f.client);
		float[] cacheBefore = f.seed.x.clone();
		Model first = factory.create(0, 0);
		Model second = factory.create(1, 0);
		float[] secondBefore = second.getVerticesX().clone();
		TubeModelFactory.animate(first, 16);
		assertArrayEquals(cacheBefore, f.seed.x, 0);
		assertArrayEquals(secondBefore, second.getVerticesX(), 0);
		assertFalse(Arrays.equals(first.getVerticesX(), second.getVerticesX()));
	}

	private static final class Fixture
	{
		private final WackySirePlugin plugin = new WackySirePlugin();
		private final RenderCallbackManager callbacks = new RenderCallbackManager();
		private final Set<RuneLiteObjectController> active = Collections.newSetFromMap(new IdentityHashMap<>());
		private int npcId = 5912;
		private int worldViewId = -1;
		private boolean ready = true;
		private boolean keepOriginals;
		private GameState gameState = GameState.LOGGED_IN;
		private FakeMesh seed = new FakeMesh(1);
		private final NPC npc = proxy(NPC.class, (obj, method, args) -> {
			switch (method.getName())
			{
				case "getId": return npcId;
				case "getLocalLocation": return new LocalPoint(6400, 6400, worldViewId);
				default: return defaultValue(method);
			}
		});
		private final WorldView view = proxy(WorldView.class, (obj, method, args) -> {
			switch (method.getName())
			{
				case "getId": return -1;
				case "npcs": return new IndexedObjectSet<NPC>() {
					public NPC byIndex(int i) { return npc; }
					public Iterator<NPC> iterator() { return Collections.singletonList(npc).iterator(); }
				};
				default: return defaultValue(method);
			}
		});
		private final Client client = proxy(Client.class, (obj, method, args) -> {
			switch (method.getName())
			{
				case "getGameState": return gameState;
				case "isClientThread": return true;
				case "getTopLevelWorldView": return view;
				case "loadModelData": return ready ? seed.data() : null;
				case "mergeModels": return new FakeMesh(((ModelData[]) args[0]).length).data();
				case "registerRuneLiteObject": active.add((RuneLiteObjectController) args[0]); return null;
				case "removeRuneLiteObject": active.remove(args[0]); return null;
				case "isRuneLiteObjectRegistered": return active.contains(args[0]);
				default: return defaultValue(method);
			}
		});

		private Fixture()
		{
			inject(plugin, "client", client);
			ClientThread clientThread = new ClientThread();
			inject(clientThread, "client", client);
			inject(plugin, "clientThread", clientThread);
			inject(plugin, "config", new WackySireConfig() {
				@Override public boolean keepOriginals() { return keepOriginals; }
			});
			inject(plugin, "renderCallbacks", callbacks);
			inject(plugin, "modelFactory", new TubeModelFactory(client));
		}
	}

	private static final class FakeMesh implements InvocationHandler
	{
		private float[] x, y, z;
		private int[] a, b, c;
		private short[] colors;
		private short[] textures;
		private byte[] alpha;

		private FakeMesh(int faces)
		{
			x = new float[faces * 3]; y = x.clone(); z = x.clone();
			a = new int[faces]; b = new int[faces]; c = new int[faces];
			colors = new short[faces];
			for (int i = 0; i < faces; i++)
			{
				a[i] = i * 3; b[i] = i * 3 + 1; c[i] = i * 3 + 2;
				x[b[i]] = 1; y[c[i]] = 1;
			}
		}

		private ModelData data() { return proxy(ModelData.class, this); }

		@Override
		public Object invoke(Object object, Method method, Object[] args)
		{
			switch (method.getName())
			{
				case "getVerticesCount": return x.length;
				case "getFaceCount": return a.length;
				case "getVerticesX": return x;
				case "getVerticesY": return y;
				case "getVerticesZ": return z;
				case "getFaceIndices1": return a;
				case "getFaceIndices2": return b;
				case "getFaceIndices3": return c;
				case "getFaceColors": return colors;
				case "getFaceTextures": return textures;
				case "getFaceTransparencies": return alpha;
				case "shallowCopy":
					FakeMesh copy = new FakeMesh(a.length);
					copy.x = x; copy.y = y; copy.z = z;
					copy.a = a; copy.b = b; copy.c = c;
					copy.colors = colors; copy.alpha = alpha;
					copy.textures = textures;
					return copy.data();
				case "cloneVertices": x = x.clone(); y = y.clone(); z = z.clone(); return object;
				case "cloneColors": colors = colors.clone(); return object;
				case "cloneTextures": textures = textures.clone(); return object;
				case "cloneTransparencies": alpha = new byte[a.length]; return object;
				case "translate":
					for (int i = 0; i < x.length; i++) { x[i] += (int) args[0]; }
					return object;
				case "light": return proxy(Model.class, this);
				case "scale": return object;
				default: return defaultValue(method);
			}
		}
	}

	private static void inject(Object target, String fieldName, Object value)
	{
		try
		{
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		}
		catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
	}

	private static <T> T proxy(Class<T> type, InvocationHandler handler)
	{
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
	}

	private static Object defaultValue(Method method)
	{
		Class<?> type = method.getReturnType();
		if (type == boolean.class) { return false; }
		if (type == int.class) { return 0; }
		if (type == byte.class) { return (byte) 0; }
		if (type == long.class) { return 0L; }
		if (type == float.class) { return 0f; }
		if (type == double.class) { return 0d; }
		if (type == short.class) { return (short) 0; }
		if (type == char.class) { return '\0'; }
		return null;
	}
}
