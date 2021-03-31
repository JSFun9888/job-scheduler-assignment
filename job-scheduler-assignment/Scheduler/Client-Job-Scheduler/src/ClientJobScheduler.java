/*
COMP3100 Distributed Systems group project
Authors: Kelly Flett, Scott Lin, Jaime Sun
Student ID:45350043 , 45985995, 45662398
Practical Session: Wednesday 13:00 - 14:55
*/
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.io.*;
//import Servers.java;

public class ClientJobScheduler {
  public static String HELO = "HELO";
  public static String AUTH = "AUTH alpha";
  public static String REDY = "REDY";
  public static String QUIT = "QUIT";
  public static char[] HI = {'H','E','L','O'};//don't need
  public ArrayList<Servers> serverList = new ArrayList<Servers>();
  public Servers biggestServer;
  public int coreCount = -1;

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
      bout.write("GETS All".getBytes()); //get all server infos
      bout.flush();
      serverReply = cjs.readMsg(new byte[1000], bin);
      System.out.println("RCVD in response to GETS All: " + serverReply);
      bout.write("OK".getBytes());
      bout.flush();
      serverReply = cjs.readMsg(new byte[1000], bin); //get all the server info
      String[] arrOfStr = serverReply.split("\n"); //split the response into arr of strings
      
      for(String server: arrOfStr){
        String[] individualServer = server.split(" ");
        Servers serverIndividual = new Servers();
        serverIndividual.serverName = individualServer[0];
        serverIndividual.serverId = Integer.parseInt(individualServer[1]);
        serverIndividual.state = individualServer[2];
        serverIndividual.currStartTime = Integer.parseInt(individualServer[3]);
        serverIndividual.cores = Integer.parseInt(individualServer[4]);
        serverIndividual.mem = Integer.parseInt(individualServer[5]);
        serverIndividual.disk = Integer.parseInt(individualServer[6]);
        cjs.serverList.add(serverIndividual);
      } // make a list of servers witht their attributes
      
      System.out.println("RCVD in response to ok: " + serverReply);

      bout.write("OK".getBytes());
      bout.flush();
      serverReply = cjs.readMsg(new byte[1000], bin); 
      System.out.println("RCVD in response to ok: " + serverReply);//end of GETS

      //find biggest server
      for(Servers serverToInspect: cjs.serverList){
        if(cjs.coreCount < serverToInspect.cores){
          cjs.biggestServer = serverToInspect;
        }
      }
      String bigServer = "SCHD 0" + cjs.biggestServer.serverName + " " + Integer.toString(cjs.biggestServer.serverId);
      System.out.println("The biggest Server is: " + bigServer);
      bout.write(bigServer.getBytes()); //hard code of SCHD job 0 to server joon 0
      bout.flush();
      serverReply = cjs.readMsg(new byte[1000], bin);
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
