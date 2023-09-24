package fi.ni.vo;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Jyrki Oraskari
 * @license This work is licensed under a Creative Commons Attribution 3.0 Unported License.
 * http://creativecommons.org/licenses/by/3.0/
 */

public class DiffDescription {

    private final List<String> change_set=new LinkedList<String>();
    private final List<String> replace_set=new LinkedList<String>();
    private final List<String> removed_set=new LinkedList<String>();
    private final List<String> add_set=new LinkedList<String>();
    
    public List<String> getChange_set() {
        return change_set;
    }
    
    
    public List<String> getReplace_set() {
        return replace_set;
    }


    public List<String> getRemoved_set() {
        return removed_set;
    }
    public List<String> getAdd_set() {
        return add_set;
    }

    
    
    
}
