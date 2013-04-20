package bot.state;

import bot.Selim;
import soc.qase.state.World;

// Abstract class of all States
public abstract class State
{
	// Executed when the state is entered.
	public abstract void enter(Selim selim, World world);
	
	// Main execution method of a state. 
	public abstract void execute(Selim selim, World world);
	
	// Executed when the state is over.
	public abstract void exit(Selim selim, World world);
}
