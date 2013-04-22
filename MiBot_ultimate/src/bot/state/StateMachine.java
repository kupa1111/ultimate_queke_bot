package bot.state;

import bot.UltimateBot;
import soc.qase.state.World;

/**
 * The State Machine
 * 
 * This is where all the state transitions are handled. 
 * 
 * changeState(State newState)
 * update(UltimateBot UltimateBot, World world)
 * revertToPreviousState()
 * 
 * Methods are used to make the transitions between states. In some situations
 * like initializing, direct change to global and current state is performed.
 * 
 * Global state is executed every time and Attack state is the global state.
 * Current state is changed according to game conditions and script file.
 * Previous state is used for remembering the previous state. So we can go back to it.  
 */
public class StateMachine
{
	protected State currentState;	// Current state which is currently executed.
	protected State previousState;	// Previous state which was the previous current state.
	protected State globalState;	// Global state which is executed all the time.
	private UltimateBot UltimateBot;
	private World world;
	
	/**
	 *	Initialize StateMachibe variables. 
	 */
	public StateMachine()
	{
		currentState = null;
		previousState = null;
		globalState = null;
	}
	
	public void setCurrentState(State state)
	{
		currentState = state;
	}
	
	public void setPreviousState(State state)
	{
		previousState = state;
	}
	
	public void setGlobalState(State state)
	{
		globalState = state;
	}

	public void update(UltimateBot UltimateBot, World world)
	{
		this.UltimateBot = UltimateBot;
		this.world = world;
		
		if(UltimateBot != null && UltimateBot.isAlive())
		{
			// Execute current state
			if(currentState != null)
				currentState.execute(UltimateBot, world);
			
			// Attack is the global state.
			// It is better to execute global state after current state.
			if(globalState != null)
				globalState.execute(UltimateBot, world);
		}
		
	}
	
	public void changeState(State newState)
	{
		previousState = currentState;
		currentState.exit(UltimateBot, world);
		currentState = newState;
		currentState.enter(UltimateBot, world);
	}
	
	public void revertToPreviousState()
	{
		if(previousState != null)
			changeState(previousState);
	}
}
