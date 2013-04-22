package bot.state;

import bot.UltimateBot;
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
	public void enter(UltimateBot UltimateBot, World world)
	{
		if(UltimateBot.DEBUG_MODE) System.out.println("ENTERED Idle STATE");
	}

	@Override
	public void execute(UltimateBot UltimateBot, World world)
	{
		if(UltimateBot.DEBUG_MODE) System.out.println("EXECUTING Idle STATE");
		
		// Idle state just waits for an enemy comes near. Coward's ambush tactic!!
		//UltimateBot.waitEnemy(world);
		
		UltimateBot.findEnemy(world); // Same state as the Search Enemy.
	}

	@Override
	public void exit(UltimateBot UltimateBot, World world)
	{
		if(UltimateBot.DEBUG_MODE) System.out.println("EXIT Idle STATE");
		
	}
}
