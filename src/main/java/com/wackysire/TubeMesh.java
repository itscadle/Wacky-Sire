package com.wackysire;

import java.util.ArrayList;
import java.util.List;

/** Original procedural geometry. Negative Y points up in the game coordinate system. */
final class TubeMesh
{
	static final int BODY = 0;
	static final int WHITE = 1;
	static final int BLACK = 2;
	static final int BASE = 3;
	private static final int SIDES = 8;
	private final List<Triangle> faces = new ArrayList<>();

	List<Triangle> getFaces()
	{
		return faces;
	}

	static TubeMesh pose(double seconds, double phase)
	{
		TubeMesh mesh = new TubeMesh();
		double t = seconds * 2.6 + phase;
		Vec[] body = new Vec[11];
		for (int i = 0; i < body.length; i++)
		{
			double h = i / 10.0;
			body[i] = new Vec(Math.sin(t + h * 2.1) * 68 * h * h, -h * 340,
				Math.cos(t + h * 1.7) * 35 * h * h);
		}
		mesh.tube(body, 28, BODY);
		mesh.tube(new Vec[]{new Vec(0, 0, 0), new Vec(0, -22, 0)}, 40, BASE);
		Vec shoulder = body[7];
		for (int side : new int[]{-1, 1})
		{
			Vec[] arm = new Vec[6];
			for (int i = 0; i < arm.length; i++)
			{
				double u = i / 5.0;
				arm[i] = shoulder.add(new Vec(side * (20 + u * 105),
					-u * 25 - Math.sin(t * 2 + side + u * 3) * 56 * u,
					Math.cos(t + side + u * 2) * 24 * u));
			}
			mesh.tube(arm, 11, BODY);
		}
		// Face panels on the front of the top section, following the head's lean.
		Vec head = body[9];
		Vec right = new Vec(1, 0, 0);
		Vec up = body[10].subtract(body[8]).unit();
		Vec front = right.cross(up).unit();
		for (int side : new int[]{-1, 1})
		{
			Vec eye = head.add(right.times(side * 11)).add(front.times(29));
			mesh.panel(eye, right, up, 8, 10, WHITE);
			mesh.panel(eye.add(front.times(1)), right, up, 3, 5, BLACK);
		}
		mesh.panel(head.add(up.times(-22)).add(front.times(29)), right, up, 13, 5, BLACK);
		// Five little fabric streamers above the open head.
		for (int i = -2; i <= 2; i++)
		{
			Vec root = body[10].add(new Vec(i * 10, 0, 0));
			Vec tip = root.add(new Vec(Math.sin(t * 2 + i) * 18, -30,
				Math.cos(t * 2 + i) * 16));
			mesh.tube(new Vec[]{root, tip}, 3, BODY);
		}
		return mesh;
	}

	private void panel(Vec center, Vec right, Vec up, double width, double height, int color)
	{
		Vec a = center.add(right.times(-width)).add(up.times(-height));
		Vec b = center.add(right.times(width)).add(up.times(-height));
		Vec c = center.add(right.times(width)).add(up.times(height));
		Vec d = center.add(right.times(-width)).add(up.times(height));
		triangle(a, b, c, color);
		triangle(a, c, d, color);
	}

	private void tube(Vec[] centers, double radius, int color)
	{
		Vec[][] rings = new Vec[centers.length][SIDES];
		// Use one reference axis for the whole tube: switching it between rings
		// twists the surface abruptly where a bent segment crosses a threshold.
		Vec main = centers[centers.length - 1].subtract(centers[0]).unit();
		Vec axis = Math.abs(main.y) > 0.9 ? new Vec(1, 0, 0) : new Vec(0, 1, 0);
		for (int r = 0; r < centers.length; r++)
		{
			Vec tangent = centers[Math.min(r + 1, centers.length - 1)]
				.subtract(centers[Math.max(0, r - 1)]).unit();
			Vec a = tangent.cross(axis).unit();
			Vec b = tangent.cross(a).unit();
			for (int s = 0; s < SIDES; s++)
			{
				double angle = s * Math.PI * 2 / SIDES;
				rings[r][s] = centers[r].add(a.times(Math.cos(angle) * radius))
					.add(b.times(Math.sin(angle) * radius));
			}
		}
		for (int r = 0; r < rings.length - 1; r++)
		{
			for (int s = 0; s < SIDES; s++)
			{
				int n = (s + 1) % SIDES;
				triangle(rings[r][s], rings[r][n], rings[r + 1][n], color);
				triangle(rings[r][s], rings[r + 1][n], rings[r + 1][s], color);
			}
		}
	}

	private void triangle(Vec a, Vec b, Vec c, int color)
	{
		// Both windings keep open fabric tubes visible from inside and outside.
		faces.add(new Triangle(a, b, c, color));
		faces.add(new Triangle(c, b, a, color));
	}

	static final class Triangle
	{
		final Vec a;
		final Vec b;
		final Vec c;
		final int color;

		Triangle(Vec a, Vec b, Vec c, int color)
		{
			this.a = a;
			this.b = b;
			this.c = c;
			this.color = color;
		}
	}

	static final class Vec
	{
		final double x;
		final double y;
		final double z;

		Vec(double x, double y, double z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}

		Vec add(Vec v) { return new Vec(x + v.x, y + v.y, z + v.z); }
		Vec subtract(Vec v) { return new Vec(x - v.x, y - v.y, z - v.z); }
		Vec times(double n) { return new Vec(x * n, y * n, z * n); }
		Vec cross(Vec v) { return new Vec(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x); }
		Vec unit()
		{
			double length = Math.sqrt(x * x + y * y + z * z);
			if (length < 0.000001)
			{
				throw new IllegalArgumentException("Zero-length mesh direction");
			}
			return times(1 / length);
		}
	}
}
