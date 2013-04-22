package bot;

public class MiBot_ultimate {

	//	Bots
	static UltimateBot MiBot; 
	
	public static void main (String[] args) 
	{				
		Init();	
	}
	
	public static void Init()
	{		
		//Establece la ruta del quake2, necesaria para tener información sobre los mapas.
		//Observa la doble barra
		String quake2_path="C:\\quake\\quake2";
		System.setProperty("QUAKE2", quake2_path); 
		
		//Creación del bot
		MiBot = new UltimateBot("UltimateBot","female/athena");
		//MiBot = new MiBotseMueve("MiBotseMueve","male/tank");
		
		//Conecta con el localhost (si el servidor estuviera en otra máquina podríamos aquí indicar su dirección IP)
		MiBot.connect("127.0.0.1",27910);
		//MiBot.connect("10.22.146.185",27910);
	}
}
