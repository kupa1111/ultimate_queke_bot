package bot.state;

import bot.Script;
import bot.Selim;
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
	public void enter(Selim selim, World world)
	{
		if(Selim.DEBUG_MODE) System.out.println("ENTERED Flee STATE");
	}

	
	@Override
	public void execute(Selim selim, World world) 
	{
		if(Selim.DEBUG_MODE) System.out.println("EXECUTING Flee STATE");
		
		// If script says ATTACK then change global state to Attack
		if(Script.getInstance().actionDecider(selim) != Script.FLEE)
		{
			selim.getFSM().revertToPreviousState();
			exit(selim, world);
		}
		
		// When Bot is fleeing, collect health and armor. 
		if(selim.isHEALTH_LOW())
			selim.pickUpHealth(world);
		
		else if(selim.isAMMO_LOW())
			selim.pickUpAmmo(world);	
	}

	
	@Override
	public void exit(Selim selim, World world) 
	{
		if(Selim.DEBUG_MODE) System.out.println("EXIT Flee STATE");
	}
}
