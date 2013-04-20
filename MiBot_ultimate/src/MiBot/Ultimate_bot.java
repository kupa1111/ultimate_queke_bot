//--------------------------------------------------
// Name:			Jugador.java
// Authors:			Abraham Romero Quesada
//					José Manuel Martínez García
// Based on:		SampleObserverBot.java
//--------------------------------------------------

package MiBot;

import soc.qase.file.bsp.BSPEntity;
import java.io.*;
import soc.qase.file.bsp.BSPParser;
import soc.qase.file.pak.PAKParser;
import soc.qase.state.*;
import soc.qase.tools.vecmath.*;

import java.util.Vector;
import soc.qase.bot.ObserverBot;

/*-------------------------------------------------------------------*/
/**	A ready-made example of a standalone QASE agent. When connected,
 *	the bot will simply run directly towarwds the closest and visiable 
 *  available item
 *	in the game environment, collect it, and move on. */
/*-------------------------------------------------------------------*/
public final class Ultimate_bot extends ObserverBot
{
	private World world = null;
	private Player player = null;

	private Vector3f pos = null;
	private Vector3f posmapa = new Vector3f();
	private Vector3f prevposmapa = new Vector3f(0,0,0);
	
	//For movment
	private Vector3f DirMov = new Vector3f();
	private Vector3f DirVista = new Vector3f();

	//Access to environmental information
	private BSPParser my_BSP_parser = null;
	private Mapa m = null;
	
	//Constan
	private int speed = 400;
	private final int sensitable_distance = 1500;
	
	//To detect this still
	private int nsinavanzar=0;
	private Vector3f prevPosPlayer=new Vector3f(0,0,0);
	
	private boolean marked=false;
	private int nx=1,ny=1;
	
	private boolean point=false;
	private boolean no_life=false;
	private Vector weapon_collection=new Vector();
	private Vector entities = null;
	
	private ObjectInputStream objin=null; 
/*-------------------------------------------------------------------*/
/**	Constructor allowing the user to specify a name and skin (appearance)
 *	for the agent.
 *	@param botName name of the character during game session
 *	@param botSkin specifies the character's in-game appearance */
/*-------------------------------------------------------------------*/
	public Ultimate_bot(String botName, String botSkin)
	{
		super((botName == null ? "MiSampleObserverBot" : botName), botSkin);
		initBot();
	}

/*-------------------------------------------------------------------*/
/**	Constructor allowing the user to specify a name, skin, and whether
 *	the agent should manually track its inventory.
 *	@param botName name of the character during game session
 *	@param botSkin specifies the character's in-game appearance
 *	@param trackInv if true, the agent will manually track its inventory */
/*-------------------------------------------------------------------*/
	public Ultimate_bot(String botName, String botSkin, boolean trackInv)
	{
		super((botName == null ? "MiSampleObserverBot" : botName), botSkin, trackInv);
		initBot();
	}

/*-------------------------------------------------------------------*/
/**	Constructor allowing the user to specify a name, skin, whether the
 *	agent should operate in high thread safety mode, and whether it
 *	should manually track its inventory.
 *	@param botName name of the character during game session
 *	@param botSkin specifies the character's in-game appearance
 *	@param highThreadSafety if true, enables high thread safety mode
 *	@param trackInv if true, the agent will manually track its inventory */
/*-------------------------------------------------------------------*/
	public Ultimate_bot(String botName, String botSkin, boolean highThreadSafety, boolean trackInv)
	{
		super((botName == null ? "MiSampleObserverBot" : botName), botSkin, highThreadSafety, trackInv);
		initBot();
	}

/*-------------------------------------------------------------------*/
/**	Constructor allowing the user to specify a name, skin, server password,
 *	whether the agent should operate in high thread safety mode, and whether
 *	it should manually track its inventory.
 *	@param botName name of the character during game session
 *	@param botSkin specifies the character's in-game appearance
 *	@param password the password of the server, if necessary
 *	@param highThreadSafety if true, enables high thread safety mode
 *	@param trackInv if true, the agent will manually track its inventory */
/*-------------------------------------------------------------------*/
	public Ultimate_bot(String botName, String botSkin, String password, boolean highThreadSafety, boolean trackInv)
	{
		super((botName == null ? "MiSampleObserverBot" : botName), botSkin, password, highThreadSafety, trackInv);
		initBot();
	}

/*-------------------------------------------------------------------*/
/**	Constructor allowing the user to specify a name, skin, connection
 *	receive rate, type of messages received from server, field of view, 
 *	which hand the agent should hold its gun in, server password,
 *	whether the agent should operate in high thread safety mode, and whether
 *	it should manually track its inventory.
 *	@param botName name of the character during game session
 *	@param botSkin specifies the character's in-game appearance
 *	@param recvRate rate at which the client communicates with server
 *	@param msgLevel specifies which server messages to register interest in
 *	@param fov specifies the agent's field of vision
 *	@param hand specifies the hand in which the agent hold its gun
 *	@param password the password of the server, if necessary
 *	@param highThreadSafety if true, enables high thread safety mode
 *	@param trackInv if true, the agent will manually track its inventory */
/*-------------------------------------------------------------------*/
	public Ultimate_bot(String botName, String botSkin, int recvRate, int msgLevel, int fov, int hand, String password, boolean highThreadSafety, boolean trackInv)
	{
		super((botName == null ? "MiSampleObserverBot" : botName), botSkin, recvRate, msgLevel, fov, hand, password, highThreadSafety, trackInv);
		initBot();
	}

	private void initBot()
	{
		
	
		pos = new Vector3f(0, 0, 0);
		try
		{	
			objin = new ObjectInputStream(new FileInputStream("mapa.aia"));
			m = (Mapa)objin.readObject();
		
		}catch(java.io.IOException ioex){}
		catch(ClassNotFoundException cnfe){}
		System.out.println(PlayerGun.getMaxAmmo(9)+" "+PlayerGun.getMaxAmmo(19));
	}

/*-------------------------------------------------------------------*/
/**	The agent's core AI routine.
 *	@param w a World object representing the current gamestate */
/*-------------------------------------------------------------------*/
	public void runAI(World w)
	{

		//Información del mundo
		world = w;
		my_BSP_parser = this.getBSPParser();		
		my_BSP_parser.getWeapons(weapon_collection);
		entities = world.getItems();
		player = world.getPlayer();
		
		if(player!=null)
		{
			pos.set(player.getPlayerMove().getOrigin());
			posmapa.set((pos.x+32727)/64,(pos.y+32727)/64,0);
			Detect_obsticle();
			select_weapon();	
			System.out.println("Vida " + this.getHealth());
			
			if(this.getHealth()<30)
			{
				this.setAttack(false);
				search_health();
				if(!no_life)
					detect_weapon();
			}
			else
			{
				detect_weapon();
				if(!point)
				{
					if(this.getHealth()<100)
						search_health();
					search_weapon();
					if(this.getArmor()<200)
						search_armor();
				}
			}
			prevposmapa.set(posmapa);
			setBotMovement(DirMov, DirVista, speed, PlayerMove.POSTURE_JUMP);
		}
	}
	
	private void Detect_obsticle()
	{
		double dist = Math.sqrt(Math.pow(prevPosPlayer.y - player.getPlayerMove().getOrigin().getY(),2)+
				 
				Math.pow(prevPosPlayer.x - player.getPlayerMove().getOrigin().getX(),2));
		if (dist < 3 && nsinavanzar>0)
		{
			nsinavanzar++;
			//after 10 times without moving changes direction
			if (nsinavanzar>10)
			{
				nsinavanzar=1;
				if(!marked)
				{
					nx=-nx;
					marked=true;
				}
				else
				{
					ny=-ny;
					marked=false;
				}
			}		
		}
		else// Moved pretty, save the current
		{
			nsinavanzar=1;
			
			prevPosPlayer.set(player.getPlayerMove().getOrigin().getX(),
					player.getPlayerMove().getOrigin().getY(),
					player.getPlayerMove().getOrigin().getZ());			
			
		}
		DirMov.set(nx,ny,0);
		DirVista.set(DirMov);
	}
	
	public void detect_weapon()
	{
		Entity enemy = this.getNearestOpponent();
		Vector3f pos_enemy;
	
		if (enemy != null)
		{
			System.out.print("enemy detectado ");
			pos_enemy = new Vector3f(enemy.getOrigin());
			Vector3f dist=new Vector3f(pos_enemy);
			dist.sub(pos);
			int distance=(int)Math.sqrt(Math.pow(dist.x, 2)+Math.pow(dist.y, 2)+Math.pow(dist.z, 2));
		
			if(my_BSP_parser.isVisible(pos, pos_enemy)&&(distance < sensitable_distance))
			{
					DirVista = new Vector3f(pos_enemy);
					DirVista.z -= 10;
					DirVista.sub(pos);
					DirMov.set(DirVista);
					point = true;
					this.setAttack(true);
					System.out.println("y lo veo, disparando!");
					return;
			}
				
			System.out.println("pero no esta a la vista...");
			point = false;
			this.setAttack(false);
			return;
		}
		System.out.println("No veo enemys cerca...");
		point = false;
		this.setAttack(false);
	}
	
	private void search_weapon()
	{
		int indi=-1,greatest_weapon=0,value;
		Vector3f dist=new Vector3f();
		boolean repetida=true;
		for(int i=0; i< weapon_collection.size(); i++)
		{
			value=this.identifyWeapon(((BSPEntity)weapon_collection.get(i)).className);
			dist.set(pos);
			dist.sub(((BSPEntity)weapon_collection.get(i)).origin);
			
			repetida=false;
			if (world.getInventory().getCount(value)>=1)
				continue;
			if((greatest_weapon<value)&&(my_BSP_parser.isVisible(pos,new Vector3f(((BSPEntity)weapon_collection.get(i)).origin)))&& !repetida)
			{
				greatest_weapon=value;
				indi=i;
			}
		}

		if(indi>=0)
		{
			DirVista.set(((BSPEntity)weapon_collection.get(indi)).origin);
			DirVista.sub(pos);
			DirMov.set(DirVista);
			System.out.println("Buscando arma");
		}
		else
			System.out.println("No hay arma a la vista");
	}

	private void search_health()
	{
		int distancia,less=10000,indi=-1;
		Vector3f dist=new Vector3f();
		for(int i=0; i< entities.size(); i++)
		{
			if(((Entity)entities.elementAt(i)).getType()==Entity.TYPE_HEALTH)
			{
					dist.set(pos);
					dist.sub(((Entity)entities.elementAt(i)).getOrigin());
					distancia=(int)Math.sqrt(Math.pow(dist.x, 2)+Math.pow(dist.y, 2)+Math.pow(dist.z, 2));
					if((distancia<less)&&(my_BSP_parser.isVisible(pos,new Vector3f(((Entity)entities.elementAt(i)).getOrigin())))&&(((Entity)entities.elementAt(i)).getActive()))//(distancia<menor)&&(
					{
						less=distancia;
						indi=i;
					}
			}
		}
		if(indi>=0)
		{
			System.out.println("Buscando vida");
			DirVista.set(((Entity)entities.elementAt(indi)).getOrigin());
			DirVista.sub(pos);
			DirMov.set(DirVista);
			no_life=true;
		}
		else
		{
			no_life=false;
			System.out.println("No hay vida visible");
		}
	}
	
	private void search_armor()
	{
		int distancia,menor=10000,indi=-1;
		Vector3f dist=new Vector3f();
		for(int i=0; i< entities.size(); i++)
		{
			if(((Entity)entities.elementAt(i)).getType()==Entity.TYPE_ARMOR)
			{
					dist.set(pos);
					dist.sub(((Entity)entities.elementAt(i)).getOrigin());
					distancia=(int)Math.sqrt(Math.pow(dist.x, 2)+Math.pow(dist.y, 2)+Math.pow(dist.z, 2));
					if((distancia<menor)&&(my_BSP_parser.isVisible(pos,new Vector3f(((Entity)entities.elementAt(i)).getOrigin())))&&(((Entity)entities.elementAt(i)).getActive()))//(distancia<menor)&&(
					{
						menor=distancia;
						indi=i;
					}
			}
		}
		if(indi>=0)
		{
			System.out.println("Buscando vida");
			DirVista.set(((Entity)entities.elementAt(indi)).getOrigin());
			DirVista.sub(pos);
			DirMov.set(DirVista);
		}
		else
			System.out.println("No hay vida visible");
	}
	
	public int identifyWeapon(String name)
	{
		int res = 0;
		if (name.equals("weapon_blaster"))
			res = 7;
		if (name.equals("weapon_shotgun"))
			res = 8;
		if (name.equals("weapon_supershotgun"))
			res = 9;
		if (name.equals("weapon_machinegun"))
			res = 10;
		if (name.equals("weapon_chaingun"))
			res = 11;
		if (name.equals("weapon_grenades"))
			res = 12;
		if (name.equals("weapon_grenadelauncher"))
			res = 13;
		if (name.equals("weapon_rocketlauncher"))
			res = 14;
		if (name.equals("weapon_hyperblaster"))
			res = 15;
		if (name.equals("weapon_railgun"))
			res = 16;
		if (name.equals("weapon_bfg"))
			res = 17;
		return res;
	}
	
	public int identifyWeapon2(String name)
	{
		int res = 0;
		if (name.equals("/g_shotg/"))
			res = 7;
		if (name.equals("/g_shotg2/"))
			res = 8;
		if (name.equals("/g_machn/"))
			res = 9;
		if (name.equals("/g_chain/"))
			res = 10;
		if (name.equals("/grenades/"))
			res = 11;
		if (name.equals("/g_launch/"))
			res = 12;
		if (name.equals("/g_rocket/"))
			res = 13;
		if (name.equals("/g_hyperb/"))
			res = 14;
		if (name.equals("/g_bfg/"))
			res = 15;
		return res;
	}
	
	private void select_weapon()
	{
		for (int i=7;i<18;i++){
			if (world.getInventory().getCount(i)>=1)
			{
				if (i!=16){
					if (world.getInventory().getCount(player.getPlayerGun().getAmmoInventoryIndexByGun(i))>0){
						this.changeWeaponByInventoryIndex(i);
						//break;
				 	}
				}
				else{
					this.changeWeaponByInventoryIndex(i);
					break;
				}
			}
		}
		System.out.println("Actual weapon " + player.getPlayerGun().getInventoryIndex());
	}
}