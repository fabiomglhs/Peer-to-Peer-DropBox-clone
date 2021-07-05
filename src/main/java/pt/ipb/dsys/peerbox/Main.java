package pt.ipb.dsys.peerbox;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.PeerBoxOperations;
import pt.ipb.dsys.peerbox.util.WatchPath;

import java.io.File;
import java.util.Scanner;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int replicas = 2;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");


        //Initialize Peer Box
        PeerBoxOperations pb = new PeerBoxOperations();
        pb.start();

        //Create directories
        new File("dropbox").mkdirs();
        new File("downloads").mkdirs();
        //Thread to watch changes on dropbox dir
        WatchPath wp = new WatchPath(pb,replicas);
        Thread thread = new Thread(wp);
        thread.start();



        while (true) {
            Scanner input = new Scanner(System.in);
            input.useDelimiter("\n");

            if (input.hasNext()) {
                //retrieve all chunks
                if (input.hasNext("chunks")) {
                    pb.listChunks();
                }else if(input.hasNext("files")){
                    System.out.printf("FILES AND CHUNKS : \n");
                    pb.listFiles();
                }else if(input.hasNext("fetch [0-9a-zA-Z._-]+")){
                    String[] fileName = input.next().split("\\s+");
                    pb.fetch(fileName[1]);
                }else if(input.hasNext("delete [0-9a-zA-Z._-]+")){
                    String[] fileName = input.next().split("\\s+");
                    pb.delete(fileName[1]);
                }else{
                    System.out.printf("Command not recognized !!\n");
                }
            }
        }

    }

}




