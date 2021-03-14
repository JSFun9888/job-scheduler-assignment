import java.net.*;
import java.io.*;

public class ClientJobScheduler {
  public static String HELO = "HELO";
  public static String AUTH = "AUTH xxx";
  public static String REDY = "REDY";
  public static String QUIT = "QUIT";
  
  public static void main (String args []){
    try{
      Socket s = new Socket("127.0.0.1", 50000);
      DataInputStream din = new DataInputStream(s.getInputStream());
      DataOutputStream dout = new DataOutputStream(s.getOutputStream());
      String str = "";

      //send HELO mesg
      dout.writeUTF("HELO");
      dout.flush();
      //read the ok msg
      str = (String)din.readUTF();
      System.out.print(str); 
      //send Auth msg? used my name for now
      dout.writeUTF("AUTH Jaime");
      dout.flush();
      //read the reply
      str = (String)din.readUTF();
      System.out.print(str); 
      //send REDY msg
      dout.writeUTF("REDY");
      dout.flush();
      //read the reply
      str = (String)din.readUTF();
      System.out.print(str); 
      //tell server to quit
      dout.writeUTF("QUIT");
      dout.flush();
      //read reply
      str = (String)din.readUTF();
      System.out.print(str); 
      
    } catch(Exception e){
      System.out.println(e);
    }
  }
}
