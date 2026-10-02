package com.wackysire;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class TubeMeshTest
{
	@Test
	public void posesStayFiniteAndKeepTheirTopology()
	{
		int count = TubeMesh.pose(0, 0).getFaces().size();
		for (int step = 0; step < 400; step++)
		{
			List<TubeMesh.Triangle> faces = TubeMesh.pose(step * 0.037, step * 0.13).getFaces();
			assertEquals(count, faces.size());
			for (TubeMesh.Triangle face : faces)
			{
				assertVertex(face.a);
				assertVertex(face.b);
				assertVertex(face.c);
				TubeMesh.Vec normal = face.b.subtract(face.a).cross(face.c.subtract(face.a));
				assertTrue(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z > 0.001);
			}
		}
		assertTrue(count * 3 < 4096);
	}

	@Test
	public void theAnimationLoopsWithoutAGeometryJump()
	{
		List<TubeMesh.Triangle> a = TubeMesh.pose(0, 0).getFaces();
		List<TubeMesh.Triangle> b = TubeMesh.pose(0, Math.PI * 2).getFaces();
		for (int i = 0; i < a.size(); i++)
		{
			assertEquals(a.get(i).a.x, b.get(i).a.x, 0.000001);
			assertEquals(a.get(i).a.y, b.get(i).a.y, 0.000001);
			assertEquals(a.get(i).a.z, b.get(i).a.z, 0.000001);
		}
	}

	@Test
	public void eachFabricFaceHasItsReverseWinding()
	{
		List<TubeMesh.Triangle> faces = TubeMesh.pose(0, 0).getFaces();
		for (int i = 0; i < faces.size(); i += 2)
		{
			assertSame(faces.get(i).a, faces.get(i + 1).c);
			assertSame(faces.get(i).b, faces.get(i + 1).b);
			assertSame(faces.get(i).c, faces.get(i + 1).a);
		}
	}

	@Test
	public void animationMovesTheFabricAndLeavesTheBlowerAnchored()
	{
		List<TubeMesh.Triangle> a = TubeMesh.pose(0, 0).getFaces();
		List<TubeMesh.Triangle> b = TubeMesh.pose(0.5, 0).getFaces();
		boolean moved = false;
		for (int i = 0; i < a.size(); i++)
		{
			TubeMesh.Vec va = a.get(i).a;
			TubeMesh.Vec vb = b.get(i).a;
			if (a.get(i).color == TubeMesh.BASE)
			{
				assertEquals(va.x, vb.x, 0);
				assertEquals(va.y, vb.y, 0);
				assertEquals(va.z, vb.z, 0);
			}
			moved |= Math.abs(va.x - vb.x) > 1;
		}
		assertTrue(moved);
	}

	@Test
	public void onlySireTentaclesAreMatched()
	{
		for (int id = 5800; id < 6000; id++)
		{
			assertEquals(id >= 5909 && id <= 5913, TentacleIds.matches(id));
		}
		assertFalse(TentacleIds.matches(5535)); // Kraken
		assertFalse(TentacleIds.matches(12208)); // Whisperer
	}

	private static void assertVertex(TubeMesh.Vec vertex)
	{
		assertTrue(Double.isFinite(vertex.x));
		assertTrue(Double.isFinite(vertex.y));
		assertTrue(Double.isFinite(vertex.z));
		assertTrue(Math.abs(vertex.x) < 240);
		assertTrue(vertex.y <= 15 && vertex.y >= -400);
		assertTrue(Math.abs(vertex.z) < 100);
	}
}
