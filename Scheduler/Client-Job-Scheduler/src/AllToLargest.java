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
  public String serverReply = "";
  public Servers biggestServer = null;
  public int coreCount = -1;
  public Jobs currJob;
  public String bigServer = "";
  public String[] jobArr = null;
  public static int buffSize = 1000;
  public static String IP = "127.0.0.1";
  public static int PORT = 50000;

  public AllToLargest(){
    currJob = new Jobs();
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

      //perform server handshake
      atl.handshake(atl, bout, bin);
        
      //main loop, while there are jobs
      while(!atl.serverReply.trim().equals(NONE)) {

        //send REDY msg
        atl.sendMsg(REDY, bout);

        //read the reply to REDY
        atl.readServerMsg(atl, bin);
       
        //job capture
        atl.jobCapture(atl, bout, bin);
       
        System.out.println("Checking what serverReply is: ");
        System.out.println(atl.serverReply);

        //Exit main loop if no more jobs received "NONE"
        if(atl.jobArr[0].trim().equals(NONE)){
          System.out.println("NO MORE JOBS RCVD!");
          break;
        }

        //get the job info
        atl.extractJobInfo(atl);

        //send GETS All msg to get a list of servers 
        //but only if not already done
        atl.getServers(atl, bin, bout);
        
        //find biggest server if not found already
        if(atl.biggestServer==null){
          atl.findBiggestServer(atl);
        }

        //create biggest server schedule message then
        //Schedule the job to the biggest server
        atl.scheduleJob(atl, bin, bout);
      } 

      //tell server to quit
      atl.sendMsg(QUIT, bout);
      
      //read reply
      atl.readServerMsg(atl, bin);
      
      //quit once server acknowledes "QUIT"
      atl.quit(atl, dout, bout, s);
      
      
    } catch(Exception e){
      System.out.println(e);
    }
  }


  /*
        **Methods Below**
  */


  //performs the server handshake
  public void handshake(AllToLargest atl, BufferedOutputStream bout, BufferedInputStream bin){
      //send HELO mesg
      //dout.writeBytes(HELO);
      atl.sendMsg(HELO, bout);

      //read the reply for HELO
      atl.readServerMsg(atl, bin); 

      //send Auth msg to server
      atl.sendMsg(AUTH, bout);

      //read the reply AUTH
      atl.readServerMsg(atl, bin); 
  }

  //Method to read a byte[] msg from the server, returns the string
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
  
  //send a message to the server
  public void sendMsg(String msg, BufferedOutputStream bout) {
    try{
      bout.write(msg.getBytes());
      System.out.println("SENT: " + msg);
      bout.flush();
    } catch(Exception e){
      System.out.println(e);
    }

  }

  //read and print out the server msg
  public void readServerMsg(AllToLargest atl, BufferedInputStream bin){
    atl.serverReply = atl.readMsg(new byte[buffSize], bin);
    System.out.println("RCVD in response: " + atl.serverReply);
  }

  //read and print out server msg with a dynamic buffer size
  public void readServerMsgDynamic(AllToLargest atl, BufferedInputStream bin, int getsAllBuffSize){
    atl.serverReply = atl.readMsg(new byte[getsAllBuffSize], bin);
    System.out.println("RCVD in response: " + atl.serverReply);
  }

  //create a list of servers from a string array
  public void populateServerList(String[] arrOfStr, AllToLargest atl) {
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
  }

  //determine which server is biggest
  public void findBiggestServer(AllToLargest atl){
    for(Servers serverToInspect: atl.serverList){
      if(atl.coreCount < serverToInspect.cores){
        atl.biggestServer = serverToInspect;
        atl.coreCount = serverToInspect.cores;
      }
    }
  }

  //create the schedule message to send
  public void biggestServerMsg(AllToLargest atl){
    atl.bigServer = "SCHD " + Integer.toString(atl.currJob.jobID) + " " + atl.biggestServer.serverName + " " + Integer.toString(atl.biggestServer.serverId);
    System.out.println("The biggest Server is: " + atl.bigServer);
  }

  //get the job information
  public void extractJobInfo(AllToLargest atl){
    atl.currJob.submitTime = Integer.parseInt(atl.jobArr[1]);
    atl.currJob.jobID = Integer.parseInt(atl.jobArr[2]);
    atl.currJob.estRuntime = Integer.parseInt(atl.jobArr[3]);
    atl.currJob.core = Integer.parseInt(atl.jobArr[4]);
    atl.currJob.memory = Integer.parseInt(atl.jobArr[5]);
    System.out.println(atl.currJob.jobID);
  }

  //gets the next job and filters out job completed msgs
  public void jobCapture(AllToLargest atl, BufferedOutputStream bout, BufferedInputStream bin){
    atl.jobArr = atl.serverReply.split(" ");
    if(atl.jobArr[0].equals(JCPL)){
      while(atl.jobArr[0].equals(JCPL)){
        System.out.println("We got a job completed msg!");

        //send REDY msg
        atl.sendMsg(REDY, bout);

        //read the reply to REDY
        atl.serverReply = atl.readMsg(new byte[buffSize], bin);
        System.out.println("RCVD in response to REDY(job completed rdy): " + atl.serverReply);

        atl.jobArr = atl.serverReply.split(" ");
      }
    }
  }

  //gets the list of servers from the server
  public void getServers(AllToLargest atl, BufferedInputStream bin, BufferedOutputStream bout){
    if(atl.serverList.isEmpty()){
      atl.sendMsg(GETSALL, bout);

      atl.readServerMsg(atl, bin);
      
      String[] dataArr = atl.serverReply.split(" "); //split response into words
      int getsAllBuffSize = Integer.parseInt(dataArr[1].trim()) * Integer.parseInt(dataArr[2].trim());
      System.out.println("This is the gets buffer size!");
      System.out.println(getsAllBuffSize);
      atl.sendMsg(OK, bout);

      atl.readServerMsgDynamic(atl, bin, getsAllBuffSize);

      String[] arrOfStr = atl.serverReply.split("\n"); //split the response into arr of strings

      //add servers to the server list with their info
      atl.populateServerList(arrOfStr, atl);

      System.out.println("This is how many servers in the List");
      System.out.println(atl.serverList.size());
    
      System.out.println("RCVD in response to ok: " + atl.serverReply);

      atl.sendMsg(OK, bout);
      //get reply from server
      atl.readServerMsg(atl, bin);
    }
  }

  //creates and sends schedule message to server and reads reply from server
  public void scheduleJob(AllToLargest atl, BufferedInputStream bin, BufferedOutputStream bout){
    atl.biggestServerMsg(atl);   
    atl.sendMsg(atl.bigServer, bout);
    atl.readServerMsg(atl, bin);
  }

  //has the AllToLargest Client quit communicating with the server
  public void quit(AllToLargest atl, DataOutputStream dout, BufferedOutputStream bout, Socket s){
    try{
      if(atl.serverReply.equals(QUIT)){
        bout.close();
        dout.close();
        s.close();
      }
    } catch(Exception e){
      System.out.println(e);
    }
  }
}
