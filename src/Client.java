import java.net.*;
import java.io.*;
import java.util.*;


public class Client
{
 	
	public static void main(String args[]) throws Exception
	{
		
		
		
		Scanner stdin = new Scanner(System.in);
		try{
			//1. creating a socket to connect to the server
			System.out.println("Please Enter your IP Address");
			String ipaddress = stdin.next();
			Socket soc= new Socket(ipaddress, 2004);
			System.out.println("Connected to "+ipaddress+" in port 2004");
			transferfileClient t=new transferfileClient(soc);
			File curDir = new File(".");
			
			t.displayMenu();
			
			//2. get Input and Output streams
			ObjectOutputStream  out = new ObjectOutputStream(soc.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(soc.getInputStream());
			
			
		}
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	public static void getAllFiles(File curDir) {

	    File[] filesList = curDir.listFiles();
	    for(File f : filesList){
	        if(f.isDirectory())
	            System.out.println(f.getName());
	        if(f.isFile()){
	            System.out.println(f.getName());
	        }
	    }

	}
}
		
	
	
	


class transferfileClient
{
	Socket ClientSoc;
	File curDir = new File("Directory1");
	DataInputStream din;
	DataOutputStream dout;
	BufferedReader br;
	transferfileClient(Socket soc)
	{
		try
		{
			ClientSoc=soc;
			din=new DataInputStream(ClientSoc.getInputStream());
			dout=new DataOutputStream(ClientSoc.getOutputStream());
			br=new BufferedReader(new InputStreamReader(System.in));
		}
		catch(Exception ex)
		{
		}		
	}
	void SendFile() throws Exception
	{	
		
		String filename;
		System.out.print("Enter File Name :");
		filename=br.readLine();
			
		File f=new File(filename);
		if(!f.exists())
		{
			System.out.println("File not Exists...");
			dout.writeUTF("File not found");
			return;
		}
		
		dout.writeUTF(filename);
		
		String msgFromServer=din.readUTF();
		if(msgFromServer.compareTo("File Already Exists")==0)
		{
			String Option;
			System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
			Option=br.readLine();			
			if(Option=="Y")	
			{
				dout.writeUTF("Y");
			}
			else
			{
				dout.writeUTF("N");
				return;
			}
		}
		
		System.out.println("Sending File ...");
		FileInputStream fin=new FileInputStream(f);
		int ch;
		do
		{
			ch=fin.read();
			dout.writeUTF(String.valueOf(ch));
		}
		while(ch!=-1);
		fin.close();
		System.out.println(din.readUTF());
		
	}
	
	


	
	void ReceiveFile() throws Exception
	{
		String fileName;
		System.out.print("Enter File Name :");
		fileName=br.readLine();
		dout.writeUTF(fileName);
		String msgFromServer=din.readUTF();
		
		if(msgFromServer.compareTo("File Not Found")==0)
		{
			System.out.println("File not found on Server ...");
			return;
		}
		else if(msgFromServer.compareTo("READY")==0)
		{
			System.out.println("Receiving File ...");
			File f=new File(fileName);
			if(f.exists())
			{
				String Option;
				System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
				Option=br.readLine();			
				if(Option=="N")	
				{
					dout.flush();
					return;	
				}				
			}
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
			System.out.println(din.readUTF());
				
		}
		
		
	}

	public void displayMenu() throws Exception
	{
		while(true)
		{	
			System.out.println("[ MENU ]");
			System.out.println("1. Send File");
			System.out.println("2. Receive File");
			System.out.println("3. List all files");
			System.out.println("4. Disconnect from server");
			System.out.print("\nEnter Choice :");
			int choice;
			choice=Integer.parseInt(br.readLine());
			if(choice==1)
			{
				dout.writeUTF("SEND");
				SendFile();
			}
			else if(choice==2)
			{
				dout.writeUTF("GET");
				ReceiveFile();
			}
			else if(choice==3)
			{
				
				Client.getAllFiles(curDir);
				//8Thread.sleep(1000);
			}
			
			else
			{
				dout.writeUTF("DISCONNECT");
				System.exit(1);
			}
		}
	}
}