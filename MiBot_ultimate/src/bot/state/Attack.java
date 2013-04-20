package bot.state;

import bot.Selim;
import bot.Script;
import soc.qase.state.World;

/**
 * Attack state.
 * 
 * This is a global state and controlled everytime. 
 * If enemy is visible and Script says ATTACK then perform attack.
 * If Script says FLEE then change current state to Flee.
 * 
 */
public class Attack extends State
{
	private static Attack instance = null;
	
	private Attack()
	{
		// Empty
	}
	
	public static Attack getInstance()
	{
		if(instance == null)
			instance = new Attack();
		
		return instance;
	}
	
	
	@Override
	public void enter(Selim selim, World world)
	{
		if(Selim.DEBUG_MODE) System.out.println("ENTERED Attack STATE");
	}

	
	@Override
	public void execute(Selim selim, World world)
	{
		if(Selim.DEBUG_MODE) System.out.println("EXECUTING Attack STATE");
		
		// If script says FLEE then change global state to Flee
		if(Script.getInstance().actionDecider(selim) == Script.FLEE)
		{
			selim.getFSM().setCurrentState(Flee.getInstance());
			//exit(selim, world);
		}
		
		// If all conditions OK, than ATTACK!
		if(selim.isEnemyVisible())  
		   // && Script.getInstance().actionDecider(selim) == Script.ATTACK)
		{	
			selim.attack(world, true); // Attack
			
			// If run out of ammo, try to select the best weapon
			if(selim.isAMMO_LOW()==true)
				selim.selectBestWeapon();  
		}
		
		else
			exit(selim, world);
	}

	
	@Override
	public void exit(Selim selim, World world)
	{
		if(Selim.DEBUG_MODE) System.out.println("EXIT Attack STATE");
		
		selim.attack(world, false); // Stop attack
		selim.normalizeBot(); // Normalize bot movements. 
	}
}
