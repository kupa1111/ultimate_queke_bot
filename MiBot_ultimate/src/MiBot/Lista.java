package MiBot;
import java.util.Vector;
import soc.qase.tools.vecmath.*;
public class Lista {
	private int nele;
	private Vector vector=new Vector();
	public Lista()
	{
		nele=0;
	}
	public Vector3f leer()
	{
		return (Vector3f)vector.elementAt(0);
	}
	public void eliminar()
	{
		vector.removeElementAt(0);
		nele--;
	}
	public void escribir(Vector3f ele)
	{
		vector.addElement(ele);
		nele++;
	}
	public void reiniciar()
	{
		vector.removeAllElements();
		nele=0;
	}
	public int nelem()
	{
		return nele;
	}
	public boolean existe(Vector3f ele)
	{
		for(int i=0;i<nele;i++)
		{
			if((((Vector3f)vector.elementAt(i)).x==ele.x) &&(((Vector3f)vector.elementAt(i)).y==ele.y))
				return true;
		}
		return false;
	}
	public boolean vacio()
	{
		return nele==0;
	}
}
