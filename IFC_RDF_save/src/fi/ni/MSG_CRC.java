package fi.ni;

import java.util.List;

import fi.ni.vo.Triple;

public class MSG_CRC {
    List<Triple> msg;
    long crc32;
    
    public MSG_CRC(List<Triple> msg, long crc32) {
	super();
	this.msg = msg;
	this.crc32 = crc32;
    }
    public List<Triple> getMsg() {
        return msg;
    }
    public void setMsg(List<Triple> msg) {
        this.msg = msg;
    }
    public long getCrc32() {
        return crc32;
    }
    public void setCrc32(long crc32) {
        this.crc32 = crc32;
    }
    
    
    
}
