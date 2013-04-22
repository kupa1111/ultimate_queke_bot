package bot;

import bot.state.Attack;
import bot.state.CollectItem;
import bot.state.SearchEnemy;
import bot.state.StateMachine;
import botutils.BOTUtils;
import java.util.Vector;
import soc.qase.state.*;
import soc.qase.ai.waypoint.Waypoint;
import soc.qase.tools.vecmath.Vector3f;

/**
	UltimateBot bot is a Quake II bot. 
	It uses QASE API and BOTUtils class.

	Compile with: javac botutils/UltimateBot.java
	Run with: java botutils.UltimateBot [teamNumber]

	@since 2008-11-25
	@author Daghan DEMIRCI <daghan.demirci@gmail.com>
 			Omer AKYOL <omer.akyol@gmail.com>
*/
public class UltimateBot extends BOTUtils
{	
	// Debug mode flag
	public static final boolean DEBUG_MODE = true; // Enable/Disable debug outputs.
	
	// UltimateBot's StateMachine, which handles and coordinates all states
	private StateMachine stateMachine;
	
	// A reference to singleton Script object
	private Script script; 
	
	// Positions to move to
	private Waypoint destination = null;
	
	// To understand if he is lost
	private Origin tempPosition = null;
	private int waitLimit = 0;
	
	// UltimateBot attributes and flags
	private int MIN_AMMO;				// Minimum ammo value
	private int MIN_HEALTH;				// Minimum health value
	private int PREFERRED_WEAPON;		// Preferred weapon of the bot
	private int PREFERRED_AMMO;			// Ammo of preferred weapon
	private boolean AMMO_LOW;			// If bot doesn't have enough ammo
	private boolean AMMO_HIGH;			// Bot starts with 0 ammo
	private boolean HEALTH_LOW;			// If bot doesn't have enough health
	private boolean HEALTH_HIGH;		// Bot starts with 100 health
	private boolean ENEMY_IN_SIGHT;		// If enemy is in sight
	private boolean NO_ENEMY_IN_SIGHT;	// If no enemy is in sight
	private boolean HAS_WEAPON;			// If bot has the preferred weapon
	private boolean HAS_ARMOR;			// If bot has the COMBAT_ARMOR
	
	public boolean AT_CENTER = false;	// If bot is at the center
	private boolean AT_BASE_A = false;	// If bot is at base A
	private boolean AT_BASE_B = true;	// If bot is at base B
	
	private boolean CROUCH = true;
	
	
	/**
		Start the bot with:
		java UltimateBot [TeamNumber]
		
		@param args UltimateBot arguments, i.e. team number (integer from 0 and up).
	*/
	public static void main(String[] args)
	{	
		if (args.length == 1)
		{
			UltimateBot bot = new UltimateBot("UltimateBot_ " + args[0], args[0]);
		}
		
		else
		{
			UltimateBot bot = new UltimateBot("UltimateBot_0", "0");
		}
	}
	
	
	/**
		Constructor.
		
		@param botName Name of the bot, for example 'UltimateBot'.
		@param ctfTeam Team number for this bot. Integer from 0 and up.
	*/
	public UltimateBot(String botName, String ctfTeam)
	{
		super(botName, "male/chipher", ctfTeam, "localhost", "C:\\quake\\quake2", "qase.bsp", "deneme.dm2", 400);
		
		// Create Finite State Machine (FSM)
		stateMachine = new StateMachine();
		
		// Initialize bot attributes
		initBot();
		
		// Read and apply bot script
		script = Script.getInstance();
		script.readScriptFile("script.txt");
		applyScriptFile();
		
		 // If you see this, everything is fine
		System.out.println("OK!");
	}

	
	/**
		The main run method that is called automatically each game frame.
		
		@param world Current game state received from the Quake II server.
	*/
	public void runAI(World world)
	{	 
		// Do not start AI loop before objects are not ready.
		if(stateMachine != null && script != null)
		{
			// Update UltimateBot's terminal flags. (See script file)
			updateBotAttributes(world);
			
			// update FSM
			stateMachine.update(this, world);
			
			// Check if UltimateBot died or killed someone
			checkBotStatus(world);
			
			// If UltimateBot is lost switch to Search Enemy state...
			if(isLost())
			{
				if(DEBUG_MODE) System.out.println("I STUCK SOMEWHERE!");
				
				// Reset PATROL values
				AT_CENTER = false;	// If bot is at the center
				AT_BASE_A = false;	// If bot is at base A
				AT_BASE_B = true;	// If bot is at base B
				
				// Go to search enemy state!
				stateMachine.setCurrentState(SearchEnemy.getInstance());
			}
		}
	}
	
	// Called when the bot is started for the first time
	// Initialize bot attributes
	public void initBot()
	{	
		changeWaypointSensivity(20); // Attention!
	
		// Initialize bot attributes
		HAS_WEAPON  = false;
		HAS_ARMOR   = false;
		destination = null;
		AT_CENTER = false;	// If bot is at the center
		AT_BASE_A = false;	// If bot is at base A
		AT_BASE_B = true;	// If bot is at base B
		CROUCH = true;		// Crouch 
		
		// Set default weapon and ammo
		matchWeaponAndAmmo("RAILGUN"); // RAILGUN is default weapon
		MIN_AMMO = 10;	 // 10 is a good value for RAILGUN ammo. 
		MIN_HEALTH = 20; // 20 is the default value for MIN_HEALTH
		
		// Initialize State Machine states
		stateMachine.setPreviousState(CollectItem.getInstance());
		stateMachine.setCurrentState(CollectItem.getInstance());
		stateMachine.setGlobalState(Attack.getInstance());
		
		// Display some startup messages ;)
		sendConsoleCommand("I'm ALIVEEE!");
		System.out.println("I'm ALIVEEE!");
	}
	
	
	// Called when bot is died, it resets necessary values and attributes
	private void botDied()
	{
		System.out.println(getBotName() + " died...");
		
		// Reset bot attributes
		HAS_WEAPON  = false;
		HAS_ARMOR   = false;
		destination = null;
		AT_CENTER = false;	// If bot is at the center
		AT_BASE_A = false;	// If bot is at base A
		AT_BASE_B = true;	// If bot is at base 
		
		if(CROUCH)
			CROUCH = false;
		else
			CROUCH = true;
		
		// Reset State Machine states
		stateMachine.setPreviousState(CollectItem.getInstance());
		stateMachine.setCurrentState(CollectItem.getInstance());
		stateMachine.setGlobalState(Attack.getInstance());
		
		// VERY VERY VERY IMPORTANT!!
		// To prevent some deadlock which prevents respawning.
		stopAllActions();
		
		respawn();
	}
	
	
	// setAction() interface between BotUtils and StateMachine
	public void setBotAction(int actionType, boolean value)
	{
		setAction(actionType, value);
	}
	
	
	// get Finite State Machine - FSM
	public StateMachine getFSM()
	{
		return stateMachine;
	}
	
	
	// Apply script file to UltimateBot
	private void applyScriptFile()
	{
		// Get attributes
		for(int i = 0; i < Script.getInstance().getAttributeList().size(); i++)
		{
			// tempStr1 = used for constants
			// tempStr2 = used for values
			String tempStr1 = (String) script.getAttributeList().get(i).get(0);
			String tempStr2 = (String) script.getAttributeList().get(i).get(1);
			
			if(tempStr1.compareTo("AMMO_LOW") == 0) // Get AMMO_LOW value
				MIN_AMMO = Integer.parseInt(tempStr2);
			
			else if(tempStr1.compareTo("HEALTH_LOW") == 0)// Get HEALTH_LOW value
				MIN_HEALTH = Integer.parseInt(tempStr2);
			
			else if(tempStr1.compareTo("WEAPON") == 0)// Geat WEAPON type
				matchWeaponAndAmmo(tempStr2);
		}
	}
	
	
	// Match weapon type and armor according to given weapon name.
	private void matchWeaponAndAmmo(String weaponName)
	{
		if(weaponName.compareTo("RAILGUN") == 0)
		{
			PREFERRED_WEAPON = Inventory.RAILGUN;
			PREFERRED_AMMO   = Inventory.SLUGS;
		}
		
		else if(weaponName.compareTo("CHAINGUN") == 0)
		{
			PREFERRED_WEAPON = Inventory.CHAINGUN;
			PREFERRED_AMMO   = Inventory.BULLETS;
		}
		
		else if(weaponName.compareTo("ROCKET_LAUNCHER") == 0)
		{
			PREFERRED_WEAPON = Inventory.ROCKET_LAUNCHER;
			PREFERRED_AMMO   = Inventory.ROCKETS;
		}
	}
	
	
	// Updates UltimateBot's all attributes. (all of the flags and attributes)
	private void updateBotAttributes(World world)
	{
		int ammoCnt = world.getInventory().getCount(PREFERRED_AMMO);
	
		// Update AMMO_HIGH and AMMO_LOW
		if(ammoCnt > MIN_AMMO)
		{
			AMMO_HIGH = true;
			AMMO_LOW  = false;
		}
		else
		{
			AMMO_HIGH = false;
			AMMO_LOW  = true;
		}
		
		// Update HEALTH_HIGH and HEALTH_LOW
		if(getHealth() > MIN_HEALTH)
		{
			HEALTH_HIGH = true;
			HEALTH_LOW  = false;
		}
		else
		{
			HEALTH_HIGH = false;
			HEALTH_LOW  = true;
		}
		
		// Update ENEMY_IN_SIGHT and NO_ENEMY_IN_SIGHT
		if(isEnemyVisible())
		{
			ENEMY_IN_SIGHT    = true;
			NO_ENEMY_IN_SIGHT = false;
		}
		else
		{
			ENEMY_IN_SIGHT    = false;
			NO_ENEMY_IN_SIGHT = true;
		}
		
		// Update HAS_WEAPON
		if(hasItem(PREFERRED_WEAPON))
		{
			HAS_WEAPON = true;
		}
		else 
		{
			HAS_WEAPON = false;	
		}
		
		// Update HAS_ARMOR
		if(hasItem(Inventory.COMBAT_ARMOR))
		{
			HAS_ARMOR = true;
		}
		else
		{
			HAS_ARMOR = false;
		}
	}
	
	
	// If UltimateBot lost hiw way and stuck somewhere?
	public boolean isLost()
	{
		if(tempPosition == getPosition())
		{
			waitLimit++;
		}
		
		else
		{
			waitLimit = 0;
			tempPosition = getPosition();
		}
		
		// If UltimateBot waited somewhere in the map more than 5000 mili seconds
		// then something is wrong and UltimateBot got lost. 
		if(waitLimit > 50)// runAI is updated every 100 milisecs
			return true;
		
		else 
			return false;
	}

	
	// ATTACK action of UltimateBot. 
	// It aims and attacks the nearest enemy
	public void attack(World world, boolean attackFlag)
	{
		// If attackFlag is "true" UltimateBot is permitted to atack
		if(attackFlag)
		{
			aimAtEnemy(world);
			setAction(Action.ATTACK, true);
		}
		
		else // Stop attack action
		{
			setAction(Action.ATTACK, false);
		}
	}
	
	
	// Select the best weapon avaliable in the inventory. 
	public void selectBestWeapon()
	{	
		if(hasItem(Inventory.RAILGUN) && hasItem(Inventory.SLUGS))
			changeWeaponByInventoryIndex(Inventory.RAILGUN);
		
		else if(hasItem(Inventory.CHAINGUN) && hasItem(Inventory.BULLETS))
			changeWeaponByInventoryIndex(Inventory.CHAINGUN);
		
		// NOT RECOMMENDED! HE MAY BLEW HIMSELF UP!
		//else if(hasItem(Inventory.ROCKET_LAUNCHER) && hasItem(Inventory.ROCKETS))
		//	changeWeaponByInventoryIndex(Inventory.ROCKET_LAUNCHER);
	}
	
	
	// Set bot posture and walking to NORMAL
	public void normalizeBot()	
	{
		changePosture(PlayerMove.POSTURE_NORMAL);
		changeWalkState(PlayerMove.WALK_RUN);//It is better to RUN
	}
	
	
	// Check if UltimateBot died or fragged someone. 
	// This method is implemented because isBotAlive() doesn't work correctly 
	public boolean checkBotStatus(World world)
	{
		//Check for messages, i.e. if the agent is killed or not.
		Vector msg = world.getMessages();
		
		// Check if bot is alive or not. 
		if(getHealth() <= 0)
		{
			botDied(); 
			msg = world.getMessages(); // Clear messages
			return false;
		}
		
		// isBotAlive doesn't work well, so check the status from the text messages
		else if(msg != null)
		{
			for(int i = 0; i < msg.size(); i++)
			{
				String txt = (String)msg.get(i);
				
				if (txt.indexOf(" by ") > -1 || txt.indexOf(" ate ") > -1 || 
					txt.indexOf(" blew ") > -1 || txt.indexOf(" sank ") > -1 || 
					txt.indexOf(" almost ") > -1)
				{
					if (txt.startsWith(getBotName()))
					{
						botDied();
						return false;
					}
					
					// Not working...
					/*else if (txt.indexOf(getBotName()) > -1)
					{
						//Killed an enemy last frame.
						System.out.println("I GOT FRAG");
						sendConsoleCommand("YEAA! I'M COOL");
						
						// DO NOT FORGET TO CHANGE STATE HERE!!!!
						
						//Update destination.
						destination = null;
					}*/
				} 
			}
		}
		return true;
	}
	
	
	/**
		Moves to and picks up the nearest health pack.
		
		@param world Current game state.
	*/
	public void pickUpHealth(World world)
	{	
		if (DEBUG_MODE) System.out.println("STATE::GetHealth");
		
		if (destination == null)
		{
			destination = findNearestItem(Inventory.HEALTH, world, true);	
		}
		
		if (destination != null) 
		{
			int status = moveTo(world, destination, false);
		
			if (status == BOTUtils.NO_PATH || status == BOTUtils.ARRIVED) 
			{
				stopAllActions();
				destination = null;
			}
		}
	}
	
	
	/**
		Moves to and picks up the nearest preferred ammo.
		
		@param world Current game state.
	*/
	public void pickUpAmmo(World world)
	{	
		if (DEBUG_MODE) System.out.println("STATE::GetAmmo");
		
		if (destination == null)
		{
			destination = findNearestItem(PREFERRED_AMMO, world, true);	
		}
		
		if (destination != null) 
		{
			int status = moveTo(world, destination, false);
		
			if (status == BOTUtils.NO_PATH || status == BOTUtils.ARRIVED) 
			{
				stopAllActions();
				destination = null;
			}
		}
	}
	
	
	/**
		Moves to and picks up the nearest preferred Weapon.
		
		@param world Current game state.
	*/
	public void pickUpWeapon(World world)
	{
		if(DEBUG_MODE) System.out.println("STATE::GetWeapon");

		if (destination == null)
		{
			destination = findNearestWeapon(PREFERRED_WEAPON, world, true);	
		}

		if(destination != null)
		{
			int status = moveTo(world, destination, false);
			
			if(status == BOTUtils.NO_PATH || status == BOTUtils.ARRIVED) 
			{
				stopAllActions();
				destination = null;
			}
		}
	}
	
	
	/**
		Moves to and picks up the nearest COMBAT_ARMOR.
		
		@param world Current game state.
	*/
	public void pickUpArmor(World world)
	{
		if(DEBUG_MODE) System.out.println("STATE::GetCombatArmor");
				
		if(destination == null)
		{
			destination = findNearestItem(Inventory.COMBAT_ARMOR, world, true);	
		}

		if(destination != null)
		{
			int status = moveTo(world, destination, false);

			if(status == BOTUtils.NO_PATH || status == BOTUtils.ARRIVED)
			{
				stopAllActions();
				destination = null;
			}
		}
	}
	
	
	/**
		Wait an enemy to come near UltimateBot. If an enemy comes near, UltimateBot engages
		towards him. If no enemy is found, it stays idle like a player who makes ambush.
		
		@param world Current game state.
	*/
	public void waitEnemy(World world)
	{	
		if(DEBUG_MODE) System.out.println("STATE::WaitEnemy");
		
		//Only update destination every 10:th frame to avoid "thrashing"
		if(destination == null || world.getFrame() % 10 == 0)
		{	
			destination = findNearestEnemy(world);
			
			if(destination != null) 
			{
				if(DEBUG_MODE)
					System.out.println("I GOT YOUR SMELL: " + destination.getPosition().x +
						destination.getPosition().y + destination.getPosition().z);
			}
		}
		
		//Search for an enemy
		if(destination != null) 
		{
			if(DEBUG_MODE)
				System.out.println("COMING FOR YOU: " + destination.getPosition().x +
					destination.getPosition().y + destination.getPosition().z);
			
			moveTo(world, destination, false);
		}
	}
	
	
	/**
		Searches for the nearest enemy. If no enemy is found, the character
		moves to the center of the map and stays there until an enemy is
		detected.
		
		@param world Current game state.
	*/
	public void findEnemy(World world)
	{	
		if(DEBUG_MODE) System.out.println("STATE::FindEnemy");
		
		//Only update destination every 10:th frame to avoid "thrashing"
		if(destination == null || world.getFrame() % 10 == 0)
		{	
			destination = findNearestEnemy(world);
			
			if(destination != null) 
			{
				if(DEBUG_MODE)
					System.out.println("I GOT YOUR SMELL: " + destination.getPosition().x +
						destination.getPosition().y + destination.getPosition().z);
			}
		}
		
		//Search for an enemy
		if(destination != null) 
		{
			if(DEBUG_MODE)
				System.out.println("GOING TO: " + destination.getPosition().x +
					destination.getPosition().y + destination.getPosition().z);
			
			moveTo(world, destination, false);
		}
		else
		{
				moveToMapCenter(world);
		}
	}
	
	public void patrolEnemy(World world)
	{	
		if(DEBUG_MODE) System.out.println("STATE::PatrolEnemy");
		
		//Only update destination every 10:th frame to avoid "thrashing"
		if(destination == null || world.getFrame() % 10 == 0)
		{	
			destination = findNearestEnemy(world);
			
			if(destination != null) 
			{
				if(DEBUG_MODE)
					System.out.println("I GOT YOUR SMELL: " + destination.getPosition().x +
						destination.getPosition().y + destination.getPosition().z);
			}
		}
		
		//Search for an enemy
		if(destination != null) 
		{
			if(DEBUG_MODE)
				System.out.println("GOING TO: " + destination.getPosition().x +
					destination.getPosition().y + destination.getPosition().z);
			
			moveTo(world, destination, false);
		}
		else
		{
			if (!AT_BASE_A)
				patrolToBaseA(world);
			
			else if(!AT_BASE_B)
				patrolToBaseB(world);
		}
	}
	
	
	/**
		Moves to the center of the map and guards that position.
		
		@param world Current game state.
	*/	
	private void moveToMapCenter(World world)
	{
		if(DEBUG_MODE) System.out.println("STATE::MoveToMapCenter");
		
		if(destination == null)
		{
			destination = findNearestWeapon(Inventory.ROCKET_LAUNCHER, world, true);	
		}

		if(destination != null)
		{
			int status = moveTo(world, destination, false);
			
			if (status == BOTUtils.NO_PATH || status == BOTUtils.ARRIVED) 
			{	 
				AT_CENTER = true;
				
				if(DEBUG_MODE) System.out.println("I'M AT THE CENTER...");
				stopAllActions();
			}
		}
	}
	
	
	/**
		Checks if the character has all items it needs, e.g.:
		- A preferred weapon
		- Ammo to the preferred weapon
		- COMBAT_ARMOR
	
		@return True if the character has all items, false if not.
	*/
	public boolean hasAllItems()
	{
		// Check all items.
		if(HAS_WEAPON && HAS_ARMOR && AMMO_HIGH)
			return true;
		
		else 
			return false;
	}
	
	
	// Aims to the nearest enemy. 
	private void aimAtEnemy(World world)
	{
		Player player = world.getPlayer();
		Vector enemies = world.getOpponents(false); // true or false?
		Entity nearestEnemy = null;
		float nearestDist = -1;
			
		// Check all enemies to find the nearest one
		for(int i = 0; i < enemies.size(); i++)
		{				
			Entity entity = (Entity)enemies.get(i);
			
			// Do not attack your team mate!!!
		  //if(!isOnSameCTFTeam(entity))
		  //{
			float currentDist = distanceTo(player, entity);

			if(nearestDist == -1 || currentDist < nearestDist)
			{
				nearestDist = currentDist;
				nearestEnemy = entity;	
			}
		    //}
			
			// (getZ() - 12) is to detect crouching enemies ;)
			Entity fixedEntity = nearestEnemy.deepCopy();
			
			if(CROUCH)
				fixedEntity.getOrigin().setZ(fixedEntity.getOrigin().getZ() + 23);
			else
				fixedEntity.getOrigin().setZ(fixedEntity.getOrigin().getZ() - 12);
			
			Vector3f pPos = new Vector3f(player.getPlayerMove().getOrigin());
			Vector3f dir = new Vector3f(fixedEntity.getOrigin());
			dir.sub(pPos);
			
			// When attacking use RUN and NORMAL posture
			if(CROUCH)
				setBotMovement(dir, dir, PlayerMove.WALK_RUN, PlayerMove.POSTURE_CROUCH);
			else
				setBotMovement(dir, dir, PlayerMove.WALK_RUN, PlayerMove.POSTURE_NORMAL);
		}
	}
	
	private void pickUpPowerShield(World world)
	{
		if(DEBUG_MODE) System.out.println("STATE::GetPowerShield");
				
		if(destination == null)
		{
			destination = findNearestItem(Inventory.POWER_SHIELD, world, true);	
		}

		if(destination != null)
		{
			int status = moveTo(world, destination, false);

			if(status == BOTUtils.NO_PATH || status == BOTUtils.ARRIVED)
			{
				stopAllActions();
				destination = null;
			}
		}
	}
	
	// BASE A
	public void patrolToBaseA(World world)
	{	
		if(DEBUG_MODE) System.out.println("STATE::PATROL TO A BASE");
				
		if(destination == null)
		{
			destination = new Waypoint(new Vector3f(-1723.0, 957.0, -63.0));
		}

		if(destination != null)
		{
			int status = moveTo(world, destination, false);

			if(status == BOTUtils.NO_PATH || status == BOTUtils.ARRIVED)
			{
				AT_CENTER = false;
				AT_BASE_A = true;
				AT_BASE_B = false;
				stopAllActions();
				destination = null;
			}
		}
	}
	
	// BASE B
	public void patrolToBaseB(World world)
	{	
		if(DEBUG_MODE) System.out.println("STATE::PATROL TO B BASE");
				
		if(destination == null)
		{
			destination = new Waypoint(new Vector3f(1728.0, 56.0, -71.0));
		}

		if(destination != null)
		{
			int status = moveTo(world, destination, false);

			if(status == BOTUtils.NO_PATH || status == BOTUtils.ARRIVED)
			{
				AT_CENTER = false;
				AT_BASE_B = true;
				AT_BASE_A = false;
				stopAllActions();
				destination = null;
			}
		}
	}
	 
	
	
	// Try to get all the items in the map whether they are needed or not
	// Currently not used by UltimateBot. 
	private void getallItems(World world)
	{
		Vector3f pos = new Vector3f(0, 0, 0);
		Vector3f itemPos = new Vector3f(0, 0, 0);
		Vector3f itemDir = new Vector3f(0, 0, 0);
		
		// get the nearest item of any kind
		Entity nearestItem = getNearestItem(null, null);

		if(nearestItem != null)
		{
			pos.set(getPosition());
			itemPos.set(nearestItem.getOrigin());

			itemDir.sub(itemPos, pos);
			setBotMovement(itemDir, null, 200, PlayerMove.POSTURE_NORMAL);
		}
	}
	
	
	/**
		Searches for and picks up the items the character needs, e.g.:
		- A preferred weapon
		- Ammo to the preferred weapon
		- COMBAT_ARMOR
	
		@param world Current game state.
	*/
	public void getItems(World world)
	{
		try
		{
			if(!HAS_WEAPON)
			{
				pickUpWeapon(world);
			}

			else if(!AMMO_HIGH)
			{
				pickUpAmmo(world);
			}

			else if(!HAS_ARMOR)
			{
				pickUpArmor(world);
			}
		}
		
		// Check for exception
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	// GETTERS of terminal flags
	public boolean isAMMO_HIGH()
	{
		return AMMO_HIGH;
	}
	
	public boolean isAMMO_LOW()
	{
		return AMMO_LOW;
	}

	public boolean isHEALTH_HIGH()
	{
		return HEALTH_HIGH;
	}

	public boolean isHEALTH_LOW()
	{
		return HEALTH_LOW;
	}
	
	public boolean isENEMY_IN_SIGHT()
	{
		return ENEMY_IN_SIGHT;
	}
	
	public boolean isNO_ENEMY_IN_SIGHT()
	{
		return NO_ENEMY_IN_SIGHT;
	}
	
}// UltimateBot class
