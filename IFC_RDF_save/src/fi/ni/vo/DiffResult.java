package fi.ni.vo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiffResult {
    List<Triple> triples_added;
    List<Triple> triples_removed;
    Set<String> added_msg_cksums=new HashSet<String>(1000);
    Set<String> removed_msg_cksums=new HashSet<String>(1000);
    
    public DiffResult(List<Triple> triples_added, List<Triple> triples_removed) {
	super();
	this.triples_added = triples_added;
	this.triples_removed = triples_removed;
    }
    public List<Triple> getTriples_added() {
        return triples_added;
    }
    public void setTriples_added(List<Triple> triples_added) {
        this.triples_added = triples_added;
    }
    public List<Triple> getTriples_removed() {
        return triples_removed;
    }
    public void setTriples_removed(List<Triple> triples_removed) {
        this.triples_removed = triples_removed;
    }
    public Set<String> getAdded_msg_cksums() {
        return added_msg_cksums;
    }
    public void setAdded_msg_cksums(Set<String> added_msg_cksums) {
        this.added_msg_cksums = added_msg_cksums;
    }
    public Set<String> getRemoved_msg_cksums() {
        return removed_msg_cksums;
    }
    public void setRemoved_msg_cksums(Set<String> removed_msg_cksums) {
        this.removed_msg_cksums = removed_msg_cksums;
    }

    
    
}
