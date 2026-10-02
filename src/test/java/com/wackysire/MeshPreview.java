package com.wackysire;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Exports the actual procedural animation for an offline artwork preview. Not a game screenshot. */
public class MeshPreview
{
	public static void main(String[] args) throws Exception
	{
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(args[0]), StandardCharsets.UTF_8)))
		{
			writer.print('[');
			for (int frame = 0; frame < 48; frame++)
			{
				if (frame != 0) { writer.print(','); }
				writer.print('[');
				boolean first = true;
				for (TubeMesh.Triangle triangle : TubeMesh.pose(0, frame * Math.PI * 2 / 48).getFaces())
				{
					if (!first) { writer.print(','); }
					first = false;
					writer.print("[" + triangle.color);
					for (TubeMesh.Vec vertex : new TubeMesh.Vec[]{triangle.a, triangle.b, triangle.c})
					{
						writer.print("," + vertex.x + "," + vertex.y + "," + vertex.z);
					}
					writer.print(']');
				}
				writer.print(']');
			}
			writer.print(']');
		}
	}
}
