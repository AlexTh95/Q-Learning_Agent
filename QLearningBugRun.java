import actor.*;
import grid.Location;

import java.awt.Color;

/**
 * This class runs a world that contains q-learning bugs.
 */

public class QLearningBugRun {

	    public static void main(String[] args)
	    {
	        ActorWorld world = new ActorWorld();
	        world.add(new Location(7,7), new Flower());
	        world.add(new Location(6,6), new Critter());
	        for(int i=0;i<20;i++)     world.add(new Rock());
	        
	        QLearningBug alice = new QLearningBug(world, 1, 1);
	        world.add(new Location(1, 1), alice);
	        world.show();
	    }

}
