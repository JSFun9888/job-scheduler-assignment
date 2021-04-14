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

public class AllToLargest {
  //Server Messages
  public static String HELO = "HELO";
  public static String AUTH = "AUTH " + System.getProperty("user.name");
  public static String REDY = "REDY";
  public static String QUIT = "QUIT";
  public static String GETSALL = "GETS All";
  public static String OK = "OK";
  public static String NONE = "NONE";
  public static String JCPL = "JCPL";

  public ArrayList<Servers> serverList = new ArrayList<Servers>();
  public Servers biggestServer = null;
  public int coreCount = -1;
  public Jobs currJob;
  public static int buffSize = 1000;
  public static String IP = "127.0.0.1";
  public static int PORT = 50000;

  public AllToLargest(){
    currJob = new Jobs();
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
      Socket s = new Socket(IP, PORT);
      DataInputStream din = new DataInputStream(s.getInputStream());
      DataOutputStream dout = new DataOutputStream(s.getOutputStream());
      BufferedOutputStream bout = new BufferedOutputStream(dout);
      BufferedInputStream bin = new BufferedInputStream(din);
      System.out.println("connected");

      AllToLargest atl = new AllToLargest();

      //send HELO mesg
      //dout.writeBytes(HELO);
      atl.sendMsg(HELO, bout);

      //read the reply for HELO
      String serverReply = atl.readMsg(new byte[buffSize], bin);
      System.out.println("RCVD in response to HELO: " + serverReply); 

      //send Auth msg to server
      atl.sendMsg(AUTH, bout);

      //read the reply AUTH
      serverReply = atl.readMsg(new byte[buffSize], bin);
      System.out.println("RCVD in response to AUTH: " + serverReply);  
      
        
      //main loop, while there are jobs
      while(!serverReply.trim().equals(NONE)) {


        //send REDY msg
        atl.sendMsg(REDY, bout);

        //read the reply to REDY
        serverReply = atl.readMsg(new byte[buffSize], bin);
        System.out.println("RCVD in response to REDY: " + serverReply);
        
        //job capture
        String[] jobArr = serverReply.split(" ");
        if(jobArr[0].equals(JCPL)){
          while(jobArr[0].equals(JCPL)){
            System.out.println("We got a job completed msg!");

            //send REDY msg
            atl.sendMsg(REDY, bout);

            //read the reply to REDY
            serverReply = atl.readMsg(new byte[1000], bin);
            System.out.println("RCVD in response to REDY(job completed rdy): " + serverReply);

            jobArr = serverReply.split(" ");
          }
        }
        System.out.println("Checking what serverReply is: ");
        System.out.println(serverReply);

        //Exit main loop if no more jobs received "NONE"
        if(jobArr[0].trim().equals(NONE)){
          System.out.println("NONE RCVD!");
          break;
        }

        //get the job info ***Method here***
        atl.currJob.submitTime = Integer.parseInt(jobArr[1]);
        atl.currJob.jobID = Integer.parseInt(jobArr[2]);
        atl.currJob.estRuntime = Integer.parseInt(jobArr[3]);
        atl.currJob.core = Integer.parseInt(jobArr[4]);
        atl.currJob.memory = Integer.parseInt(jobArr[5]);
        //atl.currJob.disk = Integer.parseInt(jobArr[6]);
        System.out.println(atl.currJob.jobID);

        atl.sendMsg(GETSALL, bout);

        serverReply = atl.readMsg(new byte[buffSize], bin);
        System.out.println("RCVD in response to GETS All: " + serverReply);
        String[] dataArr = serverReply.split(" ");
        int getsAllBuffSize = Integer.parseInt(dataArr[1].trim()) * Integer.parseInt(dataArr[2].trim());
        System.out.println("This is the gets buffer size!");
        System.out.println(getsAllBuffSize);
        atl.sendMsg(OK, bout);

        serverReply = atl.readMsg(new byte[getsAllBuffSize], bin); //get all the server info
        System.out.println("RCVD in response to OK after GETS All: " + serverReply);
        String[] arrOfStr = serverReply.split("\n"); //split the response into arr of strings

        //add servers to the server list with their info
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
          atl.serverList.add(serverIndividual);
        }

        System.out.println("This is how many servers in the List");
        System.out.println(atl.serverList.size());
        
        System.out.println("RCVD in response to ok: " + serverReply);

        atl.sendMsg(OK, bout);

        serverReply = atl.readMsg(new byte[buffSize], bin); 
        System.out.println("RCVD in response to ok: " + serverReply);//end of GETS
        
        //find biggest server
        for(Servers serverToInspect: atl.serverList){
          if(atl.coreCount < serverToInspect.cores){
            atl.biggestServer = serverToInspect;
            atl.coreCount = serverToInspect.cores;
          }
        }


        //Schedule the job to the biggest server
        String bigServer = "SCHD " + Integer.toString(atl.currJob.jobID) + " " + atl.biggestServer.serverName + " " + Integer.toString(atl.biggestServer.serverId);
        System.out.println("The biggest Server is: " + bigServer);
          
        atl.sendMsg(bigServer, bout);
          
        serverReply = atl.readMsg(new byte[buffSize], bin);
        System.out.println("RCVD in response to SCHD: " + serverReply);
          
      } 

      //tell server to quit
      atl.sendMsg(QUIT, bout);
      
      //read reply
      serverReply = atl.readMsg(new byte[buffSize], bin);
      System.out.println("RCVD in response to QUIT: " + serverReply); 

      //quit once server acknowledes "QUIT"
      if(serverReply.equals(QUIT)){
        bout.close();
        dout.close();
        s.close();
      }
      
      
    } catch(Exception e){
      System.out.println(e);
    }

    
  }
  public void sendMsg(String msg, BufferedOutputStream bout) {
    try{
      bout.write(msg.getBytes());
      System.out.println("SENT: " + msg);
      bout.flush();
    } catch(Exception e){
      System.out.println(e);
    }

  }
}
