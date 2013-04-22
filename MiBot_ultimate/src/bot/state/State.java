package bot.state;

import bot.UltimateBot;
import soc.qase.state.World;

// Abstract class of all States
public abstract class State
{
	// Executed when the state is entered.
	public abstract void enter(UltimateBot UltimateBot, World world);
	
	// Main execution method of a state. 
	public abstract void execute(UltimateBot UltimateBot, World world);
	
	// Executed when the state is over.
	public abstract void exit(UltimateBot UltimateBot, World world);
}
