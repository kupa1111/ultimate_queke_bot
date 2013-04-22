package bot.state;

import bot.UltimateBot;
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
	public void enter(UltimateBot UltimateBot, World world)
	{
		if(UltimateBot.DEBUG_MODE) System.out.println("ENTERED Attack STATE");
	}

	
	@Override
	public void execute(UltimateBot UltimateBot, World world)
	{
		if(UltimateBot.DEBUG_MODE) System.out.println("EXECUTING Attack STATE");
		
		// If script says FLEE then change global state to Flee
		if(Script.getInstance().actionDecider(UltimateBot) == Script.FLEE)
		{
			UltimateBot.getFSM().setCurrentState(Flee.getInstance());
			//exit(UltimateBot, world);
		}
		
		// If all conditions OK, than ATTACK!
		if(UltimateBot.isEnemyVisible())  
		   // && Script.getInstance().actionDecider(UltimateBot) == Script.ATTACK)
		{	
			UltimateBot.attack(world, true); // Attack
			
			// If run out of ammo, try to select the best weapon
			if(UltimateBot.isAMMO_LOW()==true)
				UltimateBot.selectBestWeapon();  
		}
		
		else
			exit(UltimateBot, world);
	}

	
	@Override
	public void exit(UltimateBot UltimateBot, World world)
	{
		if(UltimateBot.DEBUG_MODE) System.out.println("EXIT Attack STATE");
		
		UltimateBot.attack(world, false); // Stop attack
		UltimateBot.normalizeBot(); // Normalize bot movements. 
	}
}
