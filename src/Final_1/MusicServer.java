package Final_1;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.text.html.HTMLDocument.Iterator;

public class MusicServer 
{
  ArrayList<ObjectOutputStream> clientOutputStreams;
  public static void main(String[] args) 
  {
	new MusicServer().go();
}
  public class ClientHandler implements Runnable
  {
	  ObjectInputStream in;
	  Socket clientSocket;
	  
	  public ClientHandler(Socket socket)
	  {
		  try
		  {
			  clientSocket =socket;
			  in=new ObjectInputStream(clientSocket.getInputStream());
		  }
		  catch(Exception a)
		  {
			  a.printStackTrace();
		  }
	  }
	  public void run()
	  {
		  Object o2=null;
		  Object o1=null;
		  try
		  {
			  while((o1=in.readObject())!=null)
			  {
				  o2=in.readObject();
				  System.out.println("Read two Objects");
				  tellEveryone(o1,o2);
			  }
		  }
			  catch(Exception b)
			  {
				  b.printStackTrace();
			  }
		  }
	  }
	  public void go()
	  {
		  clientOutputStreams=new ArrayList<ObjectOutputStream>();
		  try
		  {
			  ServerSocket serverSock=new ServerSocket(4242);
			  while(true)
			  {
				  Socket clientSocket=serverSock.accept();
				  ObjectOutputStream out=new ObjectOutputStream(clientSocket.getOutputStream());
				  clientOutputStreams.add(out);
				  
				  Thread t=new Thread(new ClientHandler(clientSocket));
				  t.start();
				  
				  System.out.println("Got a Connection");
			  }
		  }
		  catch(Exception c)
		  {
			  c.printStackTrace();
		  }
	  }
	  public void tellEveryone(Object one,Object two)
	  {
		  java.util.Iterator<ObjectOutputStream> it= clientOutputStreams.iterator();
		  while( it.hasNext())
		  {
			  try
			  {
				  ObjectOutputStream out=(ObjectOutputStream) it.next();
				  out.writeObject(one);
				  out.writeObject(two);
			  }
			  catch(Exception d)
			  {
				  d.printStackTrace();
			  }
		  }
	  }
  }

