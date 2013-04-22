package bot.state;

import bot.Script;
import bot.UltimateBot;
import soc.qase.state.World;

/**
 * Flee state.
 * 
 * Collect necessary amount of health and ammo. 
 * If bot's status is allright, then stop flee and return to previous state.
 * 
 */
public class Flee extends State
{
	private static Flee instance = null;
	
	private Flee()
	{
		// Empty
	}
	
	public static Flee getInstance()
	{
		if(instance == null)
			instance = new Flee();
		
		return instance;
	}
	
	
	@Override
	public void enter(UltimateBot UltimateBot, World world)
	{
		if(UltimateBot.DEBUG_MODE) System.out.println("ENTERED Flee STATE");
	}

	
	@Override
	public void execute(UltimateBot UltimateBot, World world) 
	{
		if(UltimateBot.DEBUG_MODE) System.out.println("EXECUTING Flee STATE");
		
		// If script says ATTACK then change global state to Attack
		if(Script.getInstance().actionDecider(UltimateBot) != Script.FLEE)
		{
			UltimateBot.getFSM().revertToPreviousState();
			exit(UltimateBot, world);
		}
		
		// When Bot is fleeing, collect health and armor. 
		if(UltimateBot.isHEALTH_LOW())
			UltimateBot.pickUpHealth(world);
		
		else if(UltimateBot.isAMMO_LOW())
			UltimateBot.pickUpAmmo(world);	
	}

	
	@Override
	public void exit(UltimateBot UltimateBot, World world) 
	{
		if(UltimateBot.DEBUG_MODE) System.out.println("EXIT Flee STATE");
	}
}
