package pt.ipb.dsys.peerbox.common;


import com.google.common.primitives.Bytes;
import org.jgroups.*;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;
import pt.ipb.dsys.peerbox.util.Sleeper;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerBoxOperations implements PeerBox, Receiver {
    private static final Logger logger = LoggerFactory.getLogger(PeerBoxOperations.class);
    public static final String CLUSTER_NAME = "PeerBox";
    public JChannel channel;
    protected Map<String, List<UUID>> files = new ConcurrentHashMap<>();
    protected Map<UUID, byte[]> chunks = new ConcurrentHashMap<>();
    protected static final short ID = 3500;
    //TMP VARS WHILE FETCHING FILES
    protected String tmpFileName;
    protected int tmpNumberOfChunks;
    List<byte[]> tmpByteArrayList = new ArrayList<byte[]>();


    public void start() throws Exception {
        channel = new JChannel(DefaultProtocols.gossipRouter());
        channel.connect(CLUSTER_NAME);
        channel.setReceiver(this);
    }


    @Override
    public void save(String path, int replicas) throws Exception {
        Sleeper.sleep(1000);
        Random rand = new Random();
        View view = channel.getView();
        FileInputStream in = new FileInputStream("dropbox/" + path);

        List<UUID> chunksList = new ArrayList<UUID>();
        String fileName = path.toString();

        int i = 0;
        for (; ; ) {

            byte[] chunk = new byte[BLOCK_SIZE];

            int bytes = in.read(chunk);
            if (bytes == -1)
                break;
            UUID chunkUUID = UUID.randomUUID();
            chunksList.add(chunkUUID);
            i++;
            for (int j = 0; j < replicas; j++) {
                //Random destination
                int destMember = rand.nextInt(view.size());
                Sleeper.sleep(1000);
                Address dest = view.getMembers().get(destMember);
                //Send Message
                PeerFileID obj = new PeerFileID(chunkUUID, chunk, "SAVE");
                ObjectMessage msg = new ObjectMessage(dest, obj);
                channel.send(msg);
            }
        }
        files.put(fileName, chunksList);
        Sleeper.sleep(500);
        if(files.get(fileName).size()>0){

            logger.info("File uploaded successfully!");
        }else{
            logger.info("Please try again !");
        }


    }

    @Override
    public void fetch(String fileName) throws Exception {
        List<UUID> fileChunks = files.get(fileName);

        if (fileChunks == null) {
            logger.warn("NOT FOUND");
        } else {
            logger.info("FOUND");
            tmpFileName = fileName;
            tmpNumberOfChunks = fileChunks.size();
            for (int i = 0; i < fileChunks.size(); i++) {
                PeerFileID pfi = new PeerFileID(fileChunks.get(i), null, "IF_EXISTS_SEND_ME_BACK");
                pfi.setChunkNumber(i);
                ObjectMessage msg = new ObjectMessage(null, pfi);
                channel.send(msg);
            }

        }


    }

    @Override
    public void delete(String fileName) throws Exception {
        List<UUID> removedChunks = files.remove(fileName);

        if(removedChunks==null){
            logger.info("NOT FOUND!!");
        }else{
            for (int i = 0; i < removedChunks.size(); i++) {
                PeerFileID pfi = new PeerFileID(removedChunks.get(i), null, "IF_EXISTS_DELETE");
                ObjectMessage msg = new ObjectMessage(null, pfi);
                channel.send(msg);
            }
        }

    }


    public void receive(Message msg) {

        if (msg.getObject().getClass().getName() == "pt.ipb.dsys.peerbox.common.PeerFileID") {
            logger.info("Message from {} to {}: length --> {} ", msg.src(), msg.dest(), Util.printBytes(msg.getLength()));
            PeerFileID r = msg.getObject();
            if (r.getOperation().equals("SAVE")) {
                    chunks.put(r.getUUID(), r.getData());
            }
            if (r.getOperation().equals("IF_EXISTS_SEND_ME_BACK")) {
                byte[] chunkBytes = chunks.get(r.getUUID());
                if (chunkBytes != null) {
                    r.setData(chunkBytes);
                    r.getOperation("REBUILD_FILE");
                    ObjectMessage answer = new ObjectMessage(msg.src(), r);
                    try {
                        channel.send(answer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if(r.getOperation().equals("IF_EXISTS_DELETE")){
                chunks.remove(r.getUUID());
            }

            if (r.getOperation().equals("REBUILD_FILE")) {
                tmpByteArrayList.add(r.getChunkNumber(), r.getData());
                if (r.getChunkNumber() == tmpNumberOfChunks - 1) {

                    byte[] fileBytes = new byte[0];
                    File temp = new File("downloads/"+tmpFileName);
                    for (int i = 0; i < tmpNumberOfChunks; i++) {
                        if(i == 0){
                            fileBytes=tmpByteArrayList.get(i);
                        }else{
                            fileBytes = Bytes.concat(fileBytes, tmpByteArrayList.get(i));
                        }
                    }
                    fileBytes=trim(fileBytes);
                    try {
                        FileWriter writer = new FileWriter(temp);
                        InputStream in = new ByteArrayInputStream(fileBytes);
                        writer.flush();
                        int next = in.read();
                        while (next != -1) {

                            writer.write(next);
                            next = in.read();
                        }
                        writer.close();
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        } else if (msg.getObject().equals("chunks")) {
            chunks.forEach((key, value) -> logger.info(key + ":" + value));
        }
    }

    public void listFiles() {
        files.forEach((key, value) -> System.out.println(key + ":" + value));
    }

    public void listChunks() throws Exception {
        channel.send(null,"chunks");
    }


    public byte[] trim(byte[] bytes){
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0)
        {
            --i;
        }
        return Arrays.copyOf(bytes, i + 1);
    }
}
