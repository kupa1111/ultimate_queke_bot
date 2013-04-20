package bot.state;

import bot.Selim;
import soc.qase.state.World;

/**
 * Idle state.
 * 
 * Stop moving and wait for the enemy. If enemy comes near, 
 * the global state Attack will perform attack.  
 * 
 */
public class Idle extends State
{
	private static Idle instance = null;
	
	private Idle()
	{
		// Empty
	}
	
	public static Idle getInstance()
	{
		if(instance == null)
			instance = new Idle();
		
		return instance;
	}
	
	@Override
	public void enter(Selim selim, World world)
	{
		if(Selim.DEBUG_MODE) System.out.println("ENTERED Idle STATE");
	}

	@Override
	public void execute(Selim selim, World world)
	{
		if(Selim.DEBUG_MODE) System.out.println("EXECUTING Idle STATE");
		
		// Idle state just waits for an enemy comes near. Coward's ambush tactic!!
		//selim.waitEnemy(world);
		
		selim.findEnemy(world); // Same state as the Search Enemy.
	}

	@Override
	public void exit(Selim selim, World world)
	{
		if(Selim.DEBUG_MODE) System.out.println("EXIT Idle STATE");
		
	}
}
