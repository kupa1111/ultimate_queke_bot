package bot.state;

import bot.UltimateBot;
import soc.qase.state.World;

/**
 * CollectItem state.
 * 
 * Collect all necessary items and ammunition which are stated in the Script file
 * If all items are collected change state to SearchEnemy state.
 * 
 */
public class CollectItem extends State
{
	private static CollectItem instance = null;
	
	private CollectItem()
	{
		// Empty
	}
	
	public static CollectItem getInstance()
	{
		if(instance == null)
			instance = new CollectItem();
		
		return instance;
	}
	
	
	@Override
	public void enter(UltimateBot UltimateBot, World world)
	{
		if(UltimateBot.DEBUG_MODE) System.out.println("ENTERED CollectItem STATE");
	}

	
	@Override
	public void execute(UltimateBot UltimateBot, World world)
	{
		if(UltimateBot.DEBUG_MODE) System.out.println("EXECUTING CollectItem STATE");
		
		// If UltimateBot has all the items change state to SearchEnemy, else continue
		if(UltimateBot.hasAllItems())	
			UltimateBot.getFSM().changeState(SearchEnemy.getInstance());
		
		else
			UltimateBot.getItems(world);
	}

	
	@Override
	public void exit(UltimateBot UltimateBot, World world)
	{
		if(UltimateBot.DEBUG_MODE) System.out.println("EXIT CollectItem STATE");
	}
}
