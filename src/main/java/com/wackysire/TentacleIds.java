package com.wackysire;

import net.runelite.api.gameval.NpcID;

final class TentacleIds
{
	private TentacleIds()
	{
	}

	static boolean matches(int id)
	{
		switch (id)
		{
			case NpcID.ABYSSALSIRE_TENTACLE_SLEEPING_NORTH:
			case NpcID.ABYSSALSIRE_TENTACLE_SLEEPING_SOUTH:
			case NpcID.ABYSSALSIRE_TENTACLE_SLEEPING_UPRIGHT:
			case NpcID.ABYSSALSIRE_TENTACLE_ACTIVE:
			case NpcID.ABYSSALSIRE_TENTACLE_STUNNED:
				return true;
			default:
				return false;
		}
	}
}
