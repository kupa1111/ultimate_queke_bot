package botutils;

import java.util.*;
import java.io.File;
import soc.qase.com.*;
import soc.qase.info.*;
import soc.qase.state.*;
import soc.qase.bot.*;
import soc.qase.ai.waypoint.*;
import soc.qase.tools.vecmath.*;
import soc.qase.file.bsp.*;

/**
	This class contains useful help methods when developing Quake II bots
	using the QASE API.
	
	It extends the soc.qase.bot.PollingBot which in turn extends the
	soc.qase.bot.BasicBot. Both these classes, together with soc.qase.state.World
	and soc.qase.state.Player, contains other useful methods for Quake II bots.
	
	@since 2007-11-13
	@author Johan Hagelback (jhg@bth.se)
*/
public abstract class BOTUtils extends PollingBot {
	
	/**
		Pathfinder didn't find a path to the requested position.
	*/
	public static int NO_PATH = -1;
	
	/**
		Pathfinder has found a path and the character is moving
		towards the destination, but it hasn't arrived yet.
	*/
	public static int NOT_ARRIVED = 0;
	
	/**
		The character has arrived at the destination.
	*/
	public static int ARRIVED = -1;
	
	//Used when finding shortest path to a destination.
	private Waypoint[] currentPath;
	private int pathIndex = 0;
	private float prevDistance;
	private Waypoint destination = null;
	
	//Name of this bot
	private String botname;
	
	//Posture and walkstate for the QII agent.
	private int activePosture = PlayerMove.POSTURE_NORMAL;
	private int activeWalkState = PlayerMove.WALK_NORMAL;
	
	//Determines how class a character has to be to a waypoint
	//before it begins to move to next waypoint.
	private int waypointSensivity = 25;
	
	//Set to true to write debug output to the console window.
	private boolean debug = false;
	
	/**
		Constructor.
		
		@param botName Name of the bot, for example 'JavaBot'.
		@param skin Skin for the Quake II character, for example 'female/athena'.
		@param ctfTeam Team number, integer from 0 and up. Null if no teams shall be used.
		@param serverHost Server host IP, for example 'localhost'.
		@param quake2Home Home directory for Quake II, for example 'c:\\Q2_QASE\\quake2'.
		@param map Map to play, for example 'qase.bsp'. The map must be located in [Quake2Home]/baseq2/maps/.
		@param demoFileName Demo filename to generate waypoint map from, for example 'qase.dm2'. The demofile must be located in [Quake2Home]/baseq2/demos/.
		@param noWaypoints Number of waypoints to generate from the demo file. 200-300 is usually a good number for smaller maps.
	*/
	public BOTUtils(String botName, String skin, String ctfTeam, String serverHost, String quake2Home, String map, String demoFileName, int noWaypoints) {
		super("[" + botName + "]", skin, true);
		
		this.botname = "[" + botName + "]";
	
		try {
			setQuake2HomeDirectory(quake2Home);
			
			if (!map.endsWith(".bsp")) {
				map = map + ".bsp";	
			}
			if (!demoFileName.endsWith(".dm2")) {
				demoFileName = demoFileName + ".dm2";	
			}
			
			boolean read = readMap("Q2HOME\\baseq2\\maps\\" + map);
			if (!read) {
				throw new Exception("Unable to read map file [Q2HOME\\baseq2\\maps\\" + map + "]");
			}
			
			int team = -1;
			try {
				team = Integer.parseInt(ctfTeam);	
			}
			catch (NumberFormatException ex) {
				team = -1;
			}
			
			
			if (team != -1) {	
				connect(serverHost, 27910, team);
			}
			else {
				connect(serverHost, 27910);
			}
			
			String fname = quake2Home + "\\baseq2\\demos\\" + demoFileName;
			File file = new File(fname);
			if (!file.exists()) {
				throw new Exception("Unable to find demp file [Q2HOME\\baseq2\\demos\\" + demoFileName + "]");
			}
			generateWaypointMap(fname, noWaypoints, true);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);	
		}
	}
	
	/**
		Returns the name of this bot, as specified in the constructor.
		
		@return Name of the bot
	*/
	public String getBotName() {
		return botname;	
	}
	
	/**
		Activates debug mode. In debug mode BOTUtils will output interesting information to the console window.
	*/
	public void activateDebug() {
		debug = true;	
	}
	
	/**
		Deactivates debug mode.
	*/
	public void deactivateDebug() {
		debug = false;	
	}
	
	/**
		Changes the active move posture for the Quake II character.
		
		@param posture PlayerMove.POSTURE_NORMAL (default), PlayerMove.POSTURE_CROUCH or PlayerMove.POSTURE_JUMP.
	*/
	public void changePosture(int posture) {
		if (posture == PlayerMove.POSTURE_NORMAL || posture == PlayerMove.POSTURE_CROUCH || posture == PlayerMove.POSTURE_JUMP) {
			this.activePosture = posture;
		}
	}
	
	/**
		Changes the walk state for the Quake II character.
		
		@param walkState PlayerMove.WALK_NORMAL (default), PlayerMove.WALK_RUN or PlayerMove.WALK_STOPPED,
	*/
	public void changeWalkState(int walkState) {
		if (walkState == PlayerMove.WALK_NORMAL || walkState == PlayerMove.WALK_RUN || walkState == PlayerMove.WALK_STOPPED) {
			this.activeWalkState = walkState;
		}
	}
	
	/**
		Stops all activities for the Quake II character.
	*/
	public void stopAllActions() {
		pacify();	
	}
	
	/**
		Calculates the distance between the player and an entity. Note that the distance
		doesn't take in consideration how the map looks like. There might be walls that makes
		the actual distance longer than this method returns.
		
		@param player The Player object.
		@param entity The entity to calculate distance to.
		@return The distance to the entity.
	*/
	public float distanceTo(Player player, Entity entity) {
		if (entity == null) {
			return -1;	
		}
		
		Origin entityOrigin = entity.getOrigin();
		return distanceTo(player, entityOrigin);
	}
	
	/**
		Calculates the distance between the player and a point. Note that the distance
		doesn't take in consideration how the map looks like. There might be walls that makes
		the actual distance longer than this method returns.
		
		@param player The Player object.
		@param point The point to calculate distance to,
		@return The distance to the point.
	*/
	public float distanceTo(Player player, Origin point) {
		Vector3f playerPos = new Vector3f(player.getPlayerMove().getOrigin());
		Vector3f endPos = new Vector3f(point);
			
		Vector3f directionVec = new Vector3f(0, 0, 0);
		directionVec.sub(endPos, playerPos);
		
		return directionVec.length();	
	}
	
	/**
		Checks if a new path calculation is needed. It happens if no path exists,
		if the destination differs from the old path or if forceNewPath is set.
		
		@param dest Destination for the character.
		@param forceNewPath If true, a new path is always calculated.
		@return True if a new path is needed, false otherwise.
	*/
	private boolean needNewPath(Waypoint dest, boolean forceNewPath) {
		if (forceNewPath) {
			return true;
		}
		
		if (currentPath == null) {
			return true;	
		}
		
		if (destination == null) {
			return true;	
		}
		
		float oldX = destination.getPosition().x;
		float oldY = destination.getPosition().y;
		float newX = dest.getPosition().x;
		float newY = dest.getPosition().y;
		
		if (oldX != newX || oldY != newY) {
			//New path needed!
			if (debug) System.out.println("Set new path to (" + newX + "," + newY + ")");
			clearPath();
			currentPath = null;
			return true;
		}
		
		return false;
	}
	
	/**
		Finds shortest path to the specified destination.
		
		@param dest The destination to find shortest path to.
	*/
	private void setPath(Waypoint dest) {
		destination = dest;
		if (destination == null) {
			return;	
		}
		currentPath = findShortestPath(new Origin((int)destination.getPosition().x, (int)destination.getPosition().y, (int)destination.getPosition().z));
	}
	
	/**
		The agent moves towards a specified destination point.
	
		@param world Current game state.
		@param dest The destination to move to.
		@param forceNewPath If set to true, a new path is always calculated.
		@return BOTUtils.NO_PATH, BOTUtils.NOT_ARRIVED or BOTUtils.ARRIVED.
	*/
	public int moveTo(World world, Waypoint dest, boolean forceNewPath) {
		if (dest == null) {
			return BOTUtils.NO_PATH;	
		}
		
		if (needNewPath(dest, forceNewPath)) {
			setPath(dest);
		}
		return move(world.getPlayer());
	}
	
	/**
		The agent moves towards a specified destination point.
	
		@param world Current game state.
		@param dest The destination to move to.
		@param forceNewPath If set to true, a new path is always calculated.
		@return BOTUtils.NO_PATH, BOTUtils.NOT_ARRIVED or BOTUtils.ARRIVED.
	*/
	public int moveTo(World world, Origin dest, boolean forceNewPath) {
		if (dest == null) {
			return BOTUtils.NO_PATH;	
		}
		
		Waypoint destwp = new Waypoint(dest);
		if (needNewPath(destwp, forceNewPath)) {
			setPath(destwp);
		}
		return move(world.getPlayer());
	}
	
	/**
		Moves the unit along a path of waypoints. The method uses the currentPath
		which has to be set before this method is called.
		
		@param player The Player object.
		@return True if the player has arrived at the destination, false if not.
	*/
	public int move(Player player) {
		if (currentPath == null || destination == null) {
			return BOTUtils.NO_PATH;
		}
		
		try {
			//Move along the path
			Origin playerOrigin = player.getPlayerMove().getOrigin();
			
			Waypoint next = getNextWaypoint(playerOrigin);
			if (next == null) {
				if (debug) System.out.println("Arrived at [" + currentPath[currentPath.length - 1].getPosition().x + "," + currentPath[currentPath.length - 1].getPosition().y + "]");
				stopAllActions();
				return BOTUtils.ARRIVED;	
			}
			
			Vector3f playerPos = new Vector3f(playerOrigin.getX(), playerOrigin.getY(), playerOrigin.getZ());
			
			Vector3f directionVec = new Vector3f(next.getPosition().x, next.getPosition().y, next.getPosition().z);
			directionVec.sub(playerPos);
			
			prevDistance = directionVec.length();
			
			int tempPosture = activePosture;
			float playerZ = playerOrigin.getZ();
			float nextZ = next.getPosition().z;
			if (nextZ - playerZ >= 12) {
				tempPosture = PlayerMove.POSTURE_JUMP;
			}
			
			if (debug) System.out.println("Moving towards [" + next.getPosition().x + "," + next.getPosition().y + "]");
			
			setBotMovement(directionVec, directionVec, activeWalkState, tempPosture);
			
			return BOTUtils.NOT_ARRIVED;
		}
		catch (Exception ex) {
			clearPath();
			return BOTUtils.NO_PATH;
		}
	}
	
	/**
		This method changes the waypoint sensivity. It means how close
		the Q2 character has to be to a waypoint in a list of waypoints
		before it starts to move towards the next waypoint. If it's too high,
		the character can move into obstacles. If it's too low, the character
		will never reach waypoints. The correct sensivity is found using trial
		and error.
		
		@param sensivity Waypoint sensivity. Default is 25.
	*/
	public void changeWaypointSensivity(int sensivity) {
		waypointSensivity = sensivity;
	}
	
	/**
		Help method for moving along the path.
		
		@param playerOrigin Current position of the player.
		@return The next waypoint along the path.
	*/
	private Waypoint getNextWaypoint(Origin playerOrigin) {
		boolean end = false;
		Waypoint next = null;
		
		while (!end) {
			//Safecheck
			if (pathIndex >= currentPath.length) {
				return null;	
			}
			
			next = currentPath[pathIndex];
			
			Vector3f playerPos = new Vector3f(playerOrigin.getX(), playerOrigin.getY(), playerOrigin.getZ());
			Vector3f directionVec = new Vector3f(next.getPosition().x, next.getPosition().y, next.getPosition().z);
			directionVec.sub(playerPos);
			
			int cWaypointSensivity = waypointSensivity;
			if (pathIndex == currentPath.length - 1) {
				cWaypointSensivity = 20;
			}
			
			//Check for switch
			//First check. Check distance to waypoint.
			boolean switchToNext = false;
			if (directionVec.length() <= cWaypointSensivity) {
				switchToNext = true;
			}
			//Second check. Check if agent get stuck around a waypoint.
			if (prevDistance > 0) {
				float cDist = directionVec.length();
				float diff = cDist - prevDistance;
				if (diff < 0 && diff > -15) {
					switchToNext = true;
				}
			}
			//End check
			
			if (switchToNext) {
				pathIndex++;
				
				//End of path
				if (pathIndex >= currentPath.length) {
					return null;
				}
			}
			else {
				return next;	
			}
		}
		
		return null;
	}
	
	/**
		Help method for moving along the path.
		Clears the current path when a new one is requested.
	*/
	private void clearPath() {
		currentPath = null;
		pathIndex = 0;
		prevDistance = -1;
		stopAllActions();
	}
	
	/**
		Help method for moving along the path.
		Creates the direction vector to an entity.
		
		@param player The Player object.
		@param entity The entity to move to.
		@return The direction vector to the entity.
	*/
	private Vector3f getDirectionVector(Player player, Entity entity) {
		try {
			Vector3f playerPos = new Vector3f(player.getPlayerMove().getOrigin());
			
			Vector3f directionVec = new Vector3f(entity.getOrigin());
			directionVec.sub(playerPos);
			
			return directionVec;
		}
		catch (Exception ex) {
			return null;	
		}
	}
	
	/**
		Turns the player to face an entity.
		
		@param player The Player object.
		@param entity The entity to turn to.
	*/
	public void turnTo(Player player, Entity entity)
	{
		if (entity == null)
		{
			return;
		}
		
		Vector3f pPos = new Vector3f(player.getPlayerMove().getOrigin());
		Vector3f dir = new Vector3f(entity.getOrigin());
		dir.sub(pPos);
		
		setBotMovement(dir, dir, PlayerMove.WALK_NORMAL, activePosture);
	}
	
	/**
		Checks if the nearest enemy is visible.
		
		@return True of the nearest enemy is visible for the player, false if not.
	*/
	public boolean isEnemyVisible() {
		try {
			Waypoint dest = findClosestEnemy();
			if (dest == null) {
				return false;	
			}
			
			return isVisible(new Vector3f(dest.getPosition().x, dest.getPosition().y, dest.getPosition().z));
		}
		catch (Exception ex) {
			System.out.println("Warning! " + ex.getMessage());
		}
		return false;
	}
	
	/**
		Search for the nearest enemy within detection range.	
	
		@param world Current game state.
		@return Position of the nearest enemy, or null if no enemy is within detection range.
	*/
	public Waypoint findNearestEnemy(World world) {
		//return findClosestEnemy();
		
		try {
			float bestDist = -1;
			Entity best = null;
			Vector enemies = world.getOpponents();
			
			for (int i = 0; i < enemies.size(); i++) {
				Entity e = (Entity)enemies.get(i);
				
				int eTeam = 1;//getCTFTeamNumber(); // <---- BUGGY?? :-/
				int pTeam = 2;//getCTFTeamNumber();
				
				if (eTeam != pTeam) {
					float cDist = distanceTo(world.getPlayer(), e);
					
					if (bestDist == -1 || cDist < bestDist) {
						bestDist = cDist;
						best = e;	
					}
				}
			}
			
			if (best != null) {
				Waypoint w = new Waypoint(best.getOrigin());
				System.out.println(w.getPosition().x);
				System.out.println(w.getPosition().y);
				System.out.println(w.getPosition().z);
				
				return w;	
			}
		}
		catch (Exception ex) {
			
		}
		
		return null;
	}
	
	/**
		Search for the nearest item of the specified type.
		
		@param item Item type, see Inventory class for more details.
		@param world Current game state.
		@param considerMapLayout Takes in consideration the layout of the map when deciding which item is closest.
		@return Position of the nearest item of the specified type, or null if no item is found.
	*/
	public Waypoint findNearestItem(int item, World world, boolean considerMapLayout) {
		try {
			float bestDist = -1;
			Entity best = null;
			Vector items = world.getItems(true);
			
			for (int i = 0; i < items.size(); i++) {
				Entity e = (Entity)items.get(i);
				if (e.getInventoryIndex() == item) {
					float cDist = 0;
					if (!considerMapLayout) {
						cDist = distanceTo(world.getPlayer(), e);
					}
					else {
						Waypoint[] path = findShortestPath(e.getOrigin());
						if (path != null) {
							cDist = path.length;	
						}	
					}
					
					if (bestDist == -1 || cDist < bestDist) {
						bestDist = cDist;
						best = e;	
					}
				}
			}
			
			if (best != null) {
				Waypoint w = new Waypoint(best.getOrigin());
				return w;	
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	/**
		Search for the nearest weapon of the specified type.
		
		@param weapon Weapon type, see Inventory class for more details.
		@param world Current game state.
		@param considerMapLayout Takes in consideration the layout of the map when deciding which weapon is closest.
		@return Position of the nearest weapon of the specified type, or null if no weapon is found.
	*/
	public Waypoint findNearestWeapon(int weapon, World world, boolean considerMapLayout) {
		try {
			float bestDist = -1;
			Entity best = null;
			Vector items = world.getWeapons(false);
			
			for (int i = 0; i < items.size(); i++) {
				Entity e = (Entity)items.get(i);
				if (e.getInventoryIndex() == weapon) {
					float cDist = 0;
					if (!considerMapLayout) {
						cDist = distanceTo(world.getPlayer(), e);
					}
					else {
						Waypoint[] path = findShortestPath(e.getOrigin());
						if (path != null) {
							cDist = path.length;
						}	
					}
					
					if (bestDist == -1 || cDist < bestDist) {
						bestDist = cDist;
						best = e;	
					}
				}
			}
			
			if (best != null) {
				Waypoint w = new Waypoint(best.getOrigin());
				return w;	
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
}
