/*
COMP3100 Distributed Systems group project
Authors: Kelly Flett, Scott Lin, Jaime Sun
Student ID:45350043 , 45985995, 45662398
Practical Session: Wednesday 13:00 - 14:55
*/
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;

public class ClientJobScheduler {
  public static String HELO = "HELO";
  public static String AUTH = "AUTH alpha";
  public static String REDY = "REDY";
  public static String QUIT = "QUIT";
  public static char[] HI = {'H','E','L','O'};//don't need
  

  public ClientJobScheduler(){
    
  }

  //Method to read a msg from the server, returns the string
  public String readMsg(byte[] b, BufferedInputStream bis) {
    try {
      bis.read(b);
      String str = new String(b, StandardCharsets.UTF_8);
      return str;
    } catch (Exception e){
      System.out.println(e);
    }
    
    

    return "error";
  }
  
  public static void main (String args []){
    try{
      Socket s = new Socket("127.0.0.1", 50000);
      DataInputStream din = new DataInputStream(s.getInputStream());
      DataOutputStream dout = new DataOutputStream(s.getOutputStream());
      BufferedOutputStream bout = new BufferedOutputStream(dout);
      BufferedInputStream bin = new BufferedInputStream(din);
      System.out.println("connected");

      ClientJobScheduler cjs = new ClientJobScheduler();

      //send HELO mesg
      //dout.writeBytes(HELO);
      bout.write(HELO.getBytes());
      System.out.println("SENT HELO");
      bout.flush();

      //read the reply for HELO
      String serverReply = cjs.readMsg(new byte[32], bin);
      System.out.println("RCVD in response to HELO: " + serverReply); 

      //send Auth msg to server
      bout.write(AUTH.getBytes());
      bout.flush();

      //read the reply AUTH
      serverReply = cjs.readMsg(new byte[32], bin);
      System.out.println("RCVD in response to AUTH: " + serverReply);  
      
      //send REDY msg
      bout.write(REDY.getBytes());
      bout.flush();

      //read the reply to REDY
      serverReply = cjs.readMsg(new byte[32], bin);
      System.out.println("RCVD in response to REDY: " + serverReply);  

      //new stuff for wk 5 prac
      //send GETS msg new stuff here
      bout.write("GETS All".getBytes());
      bout.flush();
      serverReply = cjs.readMsg(new byte[100], bin);
      System.out.println("RCVD in response to GETS All: " + serverReply);
      bout.write("OK".getBytes());
      bout.flush();
      serverReply = cjs.readMsg(new byte[100], bin); //get all the server info
      String[] arrOfStr = serverReply.split("\n"); //split the response into arr of strings
      System.out.println("RCVD in response to ok: " + serverReply);
      bout.write("OK".getBytes());
      bout.flush();
      serverReply = cjs.readMsg(new byte[100], bin); 
      System.out.println("RCVD in response to ok: " + serverReply);//end of GETS

      bout.write("SCHD 0 joon 0".getBytes()); //hard code of SCHD job 0 to server joon 0
      bout.flush();
      serverReply = cjs.readMsg(new byte[100], bin);
      System.out.println("RCVD in response to SCHD: " + serverReply);

      //send REDY msg
      bout.write(REDY.getBytes()); //2nd job dispatch
      bout.flush();
      //read the reply to REDY
      serverReply = cjs.readMsg(new byte[32], bin);
      System.out.println("RCVD in response to REDY: " + serverReply); 
      //end of new stuff for wk 5 prac
      //need a loop for REDY
      //Also need to store server and job info

      //tell server to quit
      bout.write(QUIT.getBytes());
      bout.flush();
      
      //read reply
      serverReply = cjs.readMsg(new byte[32], bin);
      System.out.println("RCVD in response to QUIT: " + serverReply); 

      if(serverReply.equals(QUIT)){
        bout.close();
        dout.close();
        s.close();
      }
      
      
    } catch(Exception e){
      System.out.println(e);
    }

    
  }
}
