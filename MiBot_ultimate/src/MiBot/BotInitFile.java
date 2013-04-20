package MiBot;

public class BotInitFile
{
	//My bot
	static Ultimate_bot bot_entity; 
	
	
	public static void main (String[] args) 
	{				
		Init();	
	}
	
	public static void Init()
	{		
		// Sets the path quake2 necessary to have information on the maps.
		// Note the double slash
		String quake2_path="C:\\quake\\quake2";
		System.setProperty("QUAKE2", quake2_path); 
		
		//Initialize the bot
		bot_entity = new Ultimate_bot("Aljaz","female/athena");
		
		//Connect to localhost (if the server was on another machine could here indicate IP address)
		bot_entity.connect("localhost",27910);
		
		
		//Dela
		
		//PONOVNO
	}
	
}
