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
  public static String AUTH = "AUTH " + System.getProperty("user.name");
  public static String REDY = "REDY";
  public static String QUIT = "QUIT";
  public ArrayList<Servers> serverList = new ArrayList<Servers>();
  public Servers biggestServer = null;
  public int coreCount = -1;
  public Jobs currJob;

  public ClientJobScheduler(){
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
      
        
      //main loop, while there are jobs
      while(!serverReply.trim().equals("NONE")) {


        //send REDY msg
        bout.write(REDY.getBytes());
        bout.flush();

        //read the reply to REDY
        serverReply = cjs.readMsg(new byte[1000], bin);
        System.out.println("RCVD in response to REDY: " + serverReply);
        
        //job capture
        String[] jobArr = serverReply.split(" ");
        if(jobArr[0].equals("JCPL")){
          while(jobArr[0].equals("JCPL")){
            System.out.println("We got a job completed msg!");

            //send REDY msg
            bout.write(REDY.getBytes());
            bout.flush();

            //read the reply to REDY
            serverReply = cjs.readMsg(new byte[1000], bin);
            System.out.println("RCVD in response to REDY(job completed rdy): " + serverReply);

            //check for job complete msg "JCPL" can delete this later
            if(serverReply.trim().equals("NONE")){
              System.out.println("Checking for NONE in loop");
              System.out.println(serverReply.trim().equals("NONE"));
            }
            jobArr = serverReply.split(" ");
          }
        }
        System.out.println("Checking what serverReply is: ");
        System.out.println(serverReply);
        System.out.println(serverReply.trim().equals("NONE"));
        //Exit main loop if no more jobs received "NONE"
        if(jobArr[0].trim().equals("NONE")){
          System.out.println("NONE RCVD!");
          break;
        }
        //get the job info
        cjs.currJob.submitTime = Integer.parseInt(jobArr[1]);
        cjs.currJob.jobID = Integer.parseInt(jobArr[2]);
        cjs.currJob.estRuntime = Integer.parseInt(jobArr[3]);
        cjs.currJob.core = Integer.parseInt(jobArr[4]);
        cjs.currJob.memory = Integer.parseInt(jobArr[5]);
        //cjs.currJob.disk = Integer.parseInt(jobArr[6]);
        System.out.println(cjs.currJob.jobID);

        //new stuff for wk 5 prac
        //send GETS msg new stuff here
          // get the server info... to add a conditional to check later**
          bout.write("GETS All".getBytes()); //get all server infos
          bout.flush();
          serverReply = cjs.readMsg(new byte[1000], bin);
          System.out.println("RCVD in response to GETS All: " + serverReply);
          String[] dataArr = serverReply.split(" ");
          int getsAllBuffSize = Integer.parseInt(dataArr[1].trim()) * Integer.parseInt(dataArr[2].trim());
          System.out.println("This is the gets buffer size!");
          System.out.println(getsAllBuffSize);
          bout.write("OK".getBytes());
          bout.flush();
          serverReply = cjs.readMsg(new byte[getsAllBuffSize], bin); //get all the server info
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
            cjs.serverList.add(serverIndividual);
          }
          System.out.println("This is how many servers in the List");
          System.out.println(cjs.serverList.size());
        
            System.out.println("RCVD in response to ok: " + serverReply);

            bout.write("OK".getBytes());
            bout.flush();
            serverReply = cjs.readMsg(new byte[1000], bin); 
            System.out.println("RCVD in response to ok: " + serverReply);//end of GETS
      
            //find biggest server
            for(Servers serverToInspect: cjs.serverList){
              if(cjs.coreCount < serverToInspect.cores){
                cjs.biggestServer = serverToInspect;
                cjs.coreCount = serverToInspect.cores;
              }
            }


          //Schedule the job to the biggest server
          String bigServer = "SCHD " + Integer.toString(cjs.currJob.jobID) + " " + cjs.biggestServer.serverName + " " + Integer.toString(cjs.biggestServer.serverId);
          System.out.println("The biggest Server is: " + bigServer);
          bout.write(bigServer.getBytes()); //hard code of SCHD job 0 to server joon 0
          bout.flush();
          serverReply = cjs.readMsg(new byte[1000], bin);
          System.out.println("RCVD in response to SCHD: " + serverReply);
          
      } 

      //tell server to quit
      bout.write(QUIT.getBytes());
      bout.flush();
      
      //read reply
      serverReply = cjs.readMsg(new byte[32], bin);
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
}
