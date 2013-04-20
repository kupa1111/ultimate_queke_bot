package MiBot;

import java.io.*;

public class Mapa implements Serializable{
	public int mapa[][]=new int[1024][1024];
	public Mapa()
	{
		for(int i=0;i<1024;i++)
			for(int j=0;j<1024;j++)
				mapa[i][j]=0;
	}
}