package pt.ipb.dsys.peerbox.util;

import pt.ipb.dsys.peerbox.common.PeerBoxOperations;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class WatchPath implements Runnable {
    private volatile Path filePath =null;
    public PeerBoxOperations pb = null;
    protected int replicas;

    public WatchPath(PeerBoxOperations p,int r){
        this.replicas=r;
        this.pb=p;
    }

    public void run(){
        Path path = Path.of("dropbox");

        //Check if path is a folder
        try {
            Boolean isFolder = (Boolean) Files.getAttribute(path, "basic:isDirectory", NOFOLLOW_LINKS);
            if (!isFolder) {
                throw new IllegalArgumentException("Path: " + path + " is not a folder");
            }
        }catch(IOException e){
            // Folder does not exists
            e.printStackTrace();
        }
        System.out.println("Watching path: " + path);

        // We obtain the file system of the Path
        FileSystem fs = path.getFileSystem();

        // We create the new WatchService using the new try() block
        try (WatchService service = fs.newWatchService()) {

            // We register the path to the service
            // We watch for creation events
            //path.register(service, ENTRY_CREATE,ENTRY_MODIFY, ENTRY_DELETE);
            path.register(service, ENTRY_CREATE);

            // Start the infinite polling loop
            WatchKey key = null;
            while (true) {
                key = service.take();

                // Dequeueing events
                WatchEvent.Kind<?> kind = null;
                for (WatchEvent<?> watchEvent : key.pollEvents()) {

                    // Get the type of the event
                    kind = watchEvent.kind();
                    if (OVERFLOW == kind) {
                        continue; // loop
                    } else if (ENTRY_CREATE == kind) {
                        // A new Path was created
                        Path newPath = ((WatchEvent<Path>) watchEvent).context();
                        // Output
                        setPath(newPath);
                        Sleeper.sleep(1000);
                        pb.save(String.valueOf(getPath()), replicas);
                        System.out.println("New path created: " + newPath);
                    }
                }
                if (!key.reset()) {
                break; // loop
                }
            }



        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Path getPath() {
        return filePath;
    }

    public void setPath(Path path){
        filePath=path;
    }
}
