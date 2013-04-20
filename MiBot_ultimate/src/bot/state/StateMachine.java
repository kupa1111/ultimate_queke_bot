package bot.state;

import bot.Selim;
import soc.qase.state.World;

/**
 * The State Machine
 * 
 * This is where all the state transitions are handled. 
 * 
 * changeState(State newState)
 * update(Selim selim, World world)
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
	private Selim selim;
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

	public void update(Selim selim, World world)
	{
		this.selim = selim;
		this.world = world;
		
		if(selim != null && selim.checkBotStatus(this.world))
		{
			// Execute current state
			if(currentState != null)
				currentState.execute(selim, world);
			
			// Attack is the global state.
			// It is better to execute global state after current state.
			if(globalState != null)
				globalState.execute(selim, world);
		}
		
	}
	
	public void changeState(State newState)
	{
		previousState = currentState;
		currentState.exit(selim, world);
		currentState = newState;
		currentState.enter(selim, world);
	}
	
	public void revertToPreviousState()
	{
		if(previousState != null)
			changeState(previousState);
	}
}
