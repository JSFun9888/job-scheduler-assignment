/*
COMP3100 Distributed Systems group project
Authors: Kelly Flett, Scott Lin, Jaime Sun
Student ID:45350043 , 45985995, 45662398
Practical Session: Wednesday 13:00 - 14:55
*/
import java.net.*;
import java.io.*;

public class ClientJobScheduler {
  public static String HELO = "HELO";
  public static String AUTH = "AUTH alpha";
  public static String REDY = "REDY";
  public static String QUIT = "QUIT";
  public static char[] HI = {'H','E','L','O'};
  
  public static void main (String args []){
    try{
      Socket s = new Socket("127.0.0.1", 50000);
      DataInputStream din = new DataInputStream(s.getInputStream());
      DataOutputStream dout = new DataOutputStream(s.getOutputStream());
      BufferedOutputStream bout = new BufferedOutputStream(dout);
      BufferedInputStream bin = new BufferedInputStream(din);
      String str = "";
      System.out.println("connected");

      

      //send HELO mesg
      //dout.writeBytes(HELO);
      bout.write(HELO.getBytes());
      System.out.println("SENT HELO");
      bout.flush();

      //read the reply for HELO
      int read = bin.read();
      System.out.println("RCVD " + read); 

      //send Auth msg to server
      bout.write(AUTH.getBytes());
      bout.flush();

      //read the reply AUTH
      read = bin.read();
      System.out.println("RCVD " + read);
      read = bin.read();
      System.out.println("RCVD " + read); 
      
      //send REDY msg
      bout.write(REDY.getBytes());
      bout.flush();

      //read the reply to REDY
      read = bin.read();
      System.out.println("RCVD " + read); 

      //tell server to quit
      bout.write(QUIT.getBytes());
      bout.flush();
      
      //read reply
      str = (String)din.readUTF();
      System.out.println(str); 

      bout.close();
      dout.close();
      s.close();
      
    } catch(Exception e){
      System.out.println(e);
    }

    
  }
}
