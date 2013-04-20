package bot.state;

import bot.Script;
import bot.Selim;
import soc.qase.state.World;

/**
 * SearchEnemy state.
 * 
 * If Script says PATROL then search for enemy in the map.
 * If Script says IDLE then change state to Idle. 
 * 
 */
public class SearchEnemy extends State
{
	private static SearchEnemy instance = null;
	
	private SearchEnemy()
	{
		// Empty
	}
	
	public static SearchEnemy getInstance()
	{
		if(instance == null)
			instance = new SearchEnemy();
		
		return instance;
	}
	
	@Override
	public void enter(Selim selim, World world)
	{
		if(Selim.DEBUG_MODE) System.out.println("ENTERED SearchEnemy STATE");
	}

	@Override
	public void execute(Selim selim, World world)
	{
		if(Selim.DEBUG_MODE) System.out.println("EXECUTING SearchEnemy STATE");
		
		// If Script says PATROL then try to find the enemy in the map
		//if(Script.getInstance().actionDecider(selim) == Script.PATROL)

		if(!selim.AT_CENTER)
			selim.findEnemy(world);
		
		else
			selim.patrolEnemy(world);
		
		// Else if Script says IDLE then go to Idle state 
		//else if(Script.getInstance().actionDecider(selim) == Script.IDLE)
		//	selim.getFSM().changeState(Idle.getInstance());
	}

	@Override
	public void exit(Selim selim, World world)
	{
		if(Selim.DEBUG_MODE) System.out.println("EXIT SearchEnemy STATE");
	}
}
