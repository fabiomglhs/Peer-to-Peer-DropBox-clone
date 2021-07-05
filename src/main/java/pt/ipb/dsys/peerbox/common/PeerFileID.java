package pt.ipb.dsys.peerbox.common;


import java.io.Serializable;
import java.util.UUID;

public class PeerFileID implements Serializable {

    protected UUID uuid ;
    protected byte[] data;
    protected String typeOfOperations;
    protected int chunkNumber;

    public PeerFileID (UUID id , byte[] d,String operation){
        this.uuid = id;
        this.data = d;
        this.typeOfOperations=operation;
    }

    public UUID getUUID(){
        return uuid;
    }

    public byte[] getData(){
        return data;
    }

    public void setData(byte[] b){
        this.data=b;
    }

    public void getOperation(String op){
        this.typeOfOperations=op;
    }

    public String getOperation(){return typeOfOperations;}

    public void setChunkNumber(int number){
        this.chunkNumber=number;
    }

    public int getChunkNumber(){
        return chunkNumber;
    }

}
