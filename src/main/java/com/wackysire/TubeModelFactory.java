package com.wackysire;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.JagexColor;
import net.runelite.api.Model;
import net.runelite.api.ModelData;

/** Uses a cache triangle only as an allocation primitive; no cache arrays are modified. */
final class TubeModelFactory
{
	// Cache model 823 is a single untextured triangle (3 vertices, 1 face).
	private static final int TRIANGLE_MODEL = 823;
	private static final int[] HUES = {0, 10, 22, 33, 43, 53};
	static final int FRAME_COUNT = 64;
	// Share immutable poses across every tentacle. No per-tick mesh allocation.
	private static final TubeMesh[] POSES = makePoses();
	private final Client client;
	private ModelData template;

	@Inject
	TubeModelFactory(Client client)
	{
		this.client = client;
	}

	Model create(int palette, int phase)
	{
		TubeMesh pose = POSES[Math.floorMod(phase, FRAME_COUNT)];
		if (template == null)
		{
			ModelData seed = client.loadModelData(TRIANGLE_MODEL);
			if (seed == null)
			{
				return null; // Asset download may still be in progress; the plugin retries.
			}
			if (seed.getVerticesCount() != 3 || seed.getFaceCount() != 1)
			{
				throw new IllegalStateException("Cache triangle 823 changed; originals will stay visible");
			}
			ModelData[] parts = new ModelData[pose.getFaces().size()];
			for (int i = 0; i < parts.length; i++)
			{
				// mergeModels deduplicates matching coordinates. Separate each seed by
				// a large translation so each output triangle owns three vertices.
				parts[i] = seed.shallowCopy().cloneVertices().translate(i * 1024, 0, 0);
			}
			template = client.mergeModels(parts);
			if (template.getFaceCount() != parts.length || template.getVerticesCount() != parts.length * 3)
			{
				template = null;
				throw new IllegalStateException("Unexpected triangle merge shape; originals will stay visible");
			}
		}
		ModelData data = template.shallowCopy().cloneVertices().cloneColors()
			.cloneTransparencies(true);
		// Untextured cache models have no texture array; cloneTextures requires one.
		if (data.getFaceTextures() != null)
		{
			data.cloneTextures();
		}
		writeVertices(data.getVerticesX(), data.getVerticesY(), data.getVerticesZ(),
			data.getFaceIndices1(), data.getFaceIndices2(), data.getFaceIndices3(), pose);
		short[] colors = {
			JagexColor.packHSL(HUES[Math.floorMod(palette, HUES.length)], 7, 64),
			JagexColor.packHSL(0, 0, 120),
			JagexColor.packHSL(0, 0, 8),
			JagexColor.packHSL(0, 0, 28)
		};
		for (int i = 0; i < data.getFaceCount(); i++)
		{
			data.getFaceColors()[i] = colors[pose.getFaces().get(i).color];
		}
		Arrays.fill(data.getFaceTransparencies(), (byte) 0);
		if (data.getFaceTextures() != null)
		{
			Arrays.fill(data.getFaceTextures(), (short) -1);
		}
		return data.light();
	}

	static int frame(double seconds, int phase)
	{
		return Math.floorMod((int) (seconds * 2.6 * FRAME_COUNT / (Math.PI * 2)) + phase, FRAME_COUNT);
	}

	static void animate(Model model, int frame)
	{
		writeVertices(model.getVerticesX(), model.getVerticesY(), model.getVerticesZ(),
			model.getFaceIndices1(), model.getFaceIndices2(), model.getFaceIndices3(),
			POSES[Math.floorMod(frame, FRAME_COUNT)]);
		// A no-op scale invalidates native model bounds after direct vertex writes.
		model.scale(128, 128, 128);
		model.calculateBoundsCylinder();
	}

	private static TubeMesh[] makePoses()
	{
		TubeMesh[] poses = new TubeMesh[FRAME_COUNT];
		for (int i = 0; i < poses.length; i++)
		{
			poses[i] = TubeMesh.pose(0, i * Math.PI * 2 / FRAME_COUNT);
		}
		return poses;
	}

	private static void writeVertices(float[] x, float[] y, float[] z,
		int[] a, int[] b, int[] c, TubeMesh pose)
	{
		List<TubeMesh.Triangle> faces = pose.getFaces();
		for (int i = 0; i < faces.size(); i++)
		{
			TubeMesh.Triangle face = faces.get(i);
			put(x, y, z, a[i], face.a);
			put(x, y, z, b[i], face.b);
			put(x, y, z, c[i], face.c);
		}
	}

	private static void put(float[] x, float[] y, float[] z, int i, TubeMesh.Vec v)
	{
		x[i] = (float) v.x;
		y[i] = (float) v.y;
		z[i] = (float) v.z;
	}

	void clear()
	{
		template = null;
	}
}
