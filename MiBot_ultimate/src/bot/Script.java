package bot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

// Script reader of the bot
public class Script
{
	// Singleton instance of the Script
	private static Script instance = null;
	
	// Action definitions in scripting language
	public static final int ATTACK	= 0;
	public static final int FLEE	= 1;
	public static final int PATROL	= 2;
	public static final int IDLE	= 3;
	
	private ArrayList<ArrayList> attributeList;	// Storage for SET attributes
	private ArrayList<ArrayList> ruleList;		// Storage for if-then rules

	
	private Script()
	{
		ruleList	  = null;
		attributeList = null;
	}
	
	
	// Return singleton object of Script
	public static Script getInstance()
	{
		if(instance == null)
			instance = new Script();
	
		return instance;
	}
	
	
	// Evaluates script rule list and returns an action
	// Action types are defined in Script class.
	public int actionDecider(UltimateBot UltimateBot)
	{
		int resultAction = IDLE;
		
		for(int i = 0; i < ruleList.size(); i++)
		{
			for(int j = 0; j < ruleList.get(i).size(); j++)
			{
				String tempStr = (String) ruleList.get(i).get(j);
				
				if(tempStr.compareTo("AMMO_LOW") == 0)
				{
					if(!UltimateBot.isAMMO_LOW())
						break; // Condition not satisfied. 
				}
				
				else if(tempStr.compareTo("AMMO_HIGH") == 0)
				{
					if(!UltimateBot.isAMMO_HIGH())
						break; // Condition not satisfied. 
				}
				
				else if(tempStr.compareTo("HEALTH_LOW") == 0)
				{
					if(!UltimateBot.isHEALTH_LOW())
						break; // Condition not satisfied. 
				}
				
				else if(tempStr.compareTo("HEALTH_HIGH") == 0)
				{
					if(!UltimateBot.isHEALTH_HIGH())
						break; // Condition not satisfied. 
				}
				
				else if(tempStr.compareTo("ENEMY_IN_SIGHT") == 0)
				{
					if(!UltimateBot.isENEMY_IN_SIGHT())
						break; // Condition not satisfied. 
				}
				
				else if(tempStr.compareTo("NO_ENEMY_IN_SIGHT") == 0)
				{
					if(!UltimateBot.isNO_ENEMY_IN_SIGHT())
						break; // Condition not satisfied. 
				}
				
				// All conditions are satisfied, perform the action
				if(j == ruleList.get(i).size() - 1)
					resultAction = getScriptAction(tempStr);
			
			}
		}
	
		return resultAction;
	}
	
	
	// Return action in Script definiton according to the given string
	private int getScriptAction(String actionStr)
	{
		if(actionStr.compareTo("ATTACK") == 0)
			return ATTACK;
		
		else if(actionStr.compareTo("FLEE") == 0)
			return FLEE;
		
		else if(actionStr.compareTo("PATROL") == 0)
			return PATROL;
		
		else if(actionStr.compareTo("IDLE") == 0)
			return IDLE;
		
		return IDLE; // If no matching action is found. 
	}
	
	
	/**
	 *  Reads script file and generates all rulesets
	 * 
	 * @return ArrayList<ArrayList> ruleSet
	 */
	public void readScriptFile(String scriptFile)
	{
		// Buffered reader
		BufferedReader reader = null;
		String line;
		
		// Create storage for all SET attribute statements
		attributeList = new ArrayList();
		
		// Create storage for all if-and-then statements
		ruleList = new ArrayList();
		
		try
		{
			// open script file for reading. 
			reader = new BufferedReader(new FileReader(scriptFile));
		
			// Parse the script file
			while((line = reader.readLine()) != null)
			{
				line = line.trim();
				
				String[] tempStr = line.split(" ");

				// "SET" command read
				// Ex: SET HEALTH_LOW 40 
				if(tempStr[0].compareTo("SET") == 0)
				{
					ArrayList<String> newAttributeList = new ArrayList();
	
					newAttributeList.add(tempStr[1]); // Add attribuute (Ex: HEALTH_LOW)
					newAttributeList.add(tempStr[2]); // Add value (Ex: 40)
					
					attributeList.add(newAttributeList); // Add new attribute list
				}
				
				// "if" statment begins
				// Ex: if AMMO_HIGH and HEALTH_HIGH and ENEMY_IN_SIGHT then ATTACK 
				else if(tempStr[0].compareTo("if") == 0)
				{
					ArrayList<String> newRuleList = new ArrayList();
					
					// Parse "if" statement
					for(int i = 1; i < tempStr.length; i++)
					{
						newRuleList.add(tempStr[i]); // Add condition rule (Ex: AMMO_HIGH)
						i++;						 // Skip "and" , "then" words
					}
					
					ruleList.add(newRuleList); // Add new rule list
				}
			
			}// while()
			
			reader.close(); // Close file
		}
		
		catch(FileNotFoundException ex) // If file not found
		{
			ex.printStackTrace();
		}
		
		catch(IOException io) // If a reading error occurs
		{
			io.printStackTrace();
		}
		
		// Test the script file if it is read correctly
		if(UltimateBot.DEBUG_MODE)
			testLists();
		
	}// read()
	
	// Get Attribute list
	public ArrayList<ArrayList> getAttributeList()
	{
		return attributeList;
	}

	// Get rule list 
	public ArrayList<ArrayList> getRuleList()
	{
		return ruleList;
	}	
	
	// TEST function of the array lists.
	private void testLists()
	{
		for(int i = 0; i < attributeList.size(); i++)
		{
			for(int j = 0; j < attributeList.get(i).size(); j++)
				System.out.print(attributeList.get(i).get(j) + "-");
			
			System.out.println("\n---");
		}
		
		for(int i = 0; i < ruleList.size(); i++)
		{
			for(int j = 0; j < ruleList.get(i).size(); j++)
				System.out.print(ruleList.get(i).get(j) + "-");
			
			System.out.println("\n---");
		}
	}

}
