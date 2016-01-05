import java.net.*;
import java.io.*;
import java.util.*;

public class Server
{
	public static void main(String args[]) throws Exception
	{
		ServerSocket soc=new ServerSocket(2004);
		System.out.println("Server started on port 2004");
		while(true)
		{
			System.out.println("Waiting for Connection ...");
			File curDir = new File("Directory1");
			if (!curDir.exists()) {
				if (curDir.mkdir()) {
					System.out.println("Directory is created!");
				} else {
					System.out.println("Failed to create directory!");
				}
			}
			transferfile t=new transferfile(soc.accept());
			
		}
	}
}

class transferfile extends Thread
{
	Socket ClientSoc;

	DataInputStream din;
	DataOutputStream dout;
	
	transferfile(Socket soc)
	{
		try
		{
			ClientSoc=soc;						
			din=new DataInputStream(ClientSoc.getInputStream());
			dout=new DataOutputStream(ClientSoc.getOutputStream());
			System.out.println("FTP Client Connected ...");
			start();
			
		}
		catch(Exception ex)
		{
		}		
	}
	void SendFile() throws Exception
	{		
		String filename=din.readUTF();
		File dir = new File("Files");
		File f=new File(dir, filename);
		if(!f.exists())
		{
			dout.writeUTF("File Not Found");
			return;
		}
		else
		{
			dout.writeUTF("READY");
			FileInputStream fin=new FileInputStream(f);
			int ch;
			do
			{
				ch=fin.read();
				dout.writeUTF(String.valueOf(ch));
			}
			while(ch!=-1);	
			fin.close();	
			dout.writeUTF("File Receive Successfully");							
		}
	}
	
	void ReceiveFile() throws Exception
	{
		String filename=din.readUTF();
		if(filename.compareTo("File not found")==0)
		{
			return;
		}
		
		File dir = new File("Files");
		dir.mkdirs();
		File f = new File(dir, filename);
		String option;
		
		if(f.exists())
		{
			dout.writeUTF("File Already Exists");
			option=din.readUTF();
		}
		else
		{
			dout.writeUTF("SendFile");
			option="Y";
		}
			
			if(option.compareTo("Y")==0)
			{
				FileOutputStream fout=new FileOutputStream(f);
				int ch;
				String temp;
				do
				{
					temp=din.readUTF();
					ch=Integer.parseInt(temp);
					if(ch!=-1)
					{
						fout.write(ch);					
					}
				}while(ch!=-1);
				fout.close();
				dout.writeUTF("File Send Successfully");
			}
			else
			{
				return;
			}
			
	}


	public void run()
	{
		while(true)
		{
			try
			{
			System.out.println("Waiting for Command ...");
			String Command=din.readUTF();
			if(Command.compareTo("GET")==0)
			{
				System.out.println("\tGET Command Received ...");
				SendFile();
				continue;
			}
			else if(Command.compareTo("SEND")==0)
			{
				System.out.println("\tSEND Command Receiced ...");				
				ReceiveFile();
				continue;
			}
			else if(Command.compareTo("DISCONNECT")==0)
			{
				System.out.println("\tDisconnect Command Received ...");
				System.exit(1);
			}
			}
			catch(Exception ex)
			{
			}
		}
	}
}