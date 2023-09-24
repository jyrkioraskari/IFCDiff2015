package fi.ni;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fi.ni.ifc2x3.IfcRoot;
import fi.ni.vo.ReturnPair;

/**
 * @author Jyrki Oraskari
 * @license This work is licensed under a Creative Commons Attribution 3.0
 *          Unported License.
 *          http://creativecommons.org/licenses/by/3.0/
 */

public class IFC_handlerShowDetails {
    private void showDetailedModifications(IFC_ClassModel drum1, IFC_ClassModel drum2) {
	Map<String, Thing> guids1 = drum1.getGIDThings(); // gid, thing
	Map<String, Thing> guids2 = drum2.getGIDThings();

	List<String> common_gids = new ArrayList<String>(guids1.keySet());
	common_gids.retainAll(guids2.keySet());
	Map<Long, String> chksums1 = drum1.commonGIDSet_CalculateCRC32ForGuidAreas(common_gids);
	Map<Long, String> chksums2 = drum2.commonGIDSet_CalculateCRC32ForGuidAreas(common_gids);

	Map<String, Long> id_chksums1 = drum1.commonGIDSet_calculateCRC32ForGuidAreas2(common_gids);
	Map<String, Long> id_chksums2 = drum2.commonGIDSet_calculateCRC32ForGuidAreas2(common_gids);

	List<String> change_set = new LinkedList<String>();
	List<String> removed_set = new LinkedList<String>();
	List<String> add_set = new LinkedList<String>();

	for (Map.Entry<String, Thing> entry : guids1.entrySet()) {
	    String key = entry.getKey();
	    if (guids2.get(key) == null) {
		if (chksums2.get(id_chksums1.get(key)) == null) {
		    removed_set.add(key); // add to the removed list to be
					  // checked
		}
	    }

	}

	for (Map.Entry<String, Thing> entry : guids2.entrySet()) {
	    String key = entry.getKey();
	    if (guids1.get(key) == null) {
		// Checksum does not match:
		if (chksums1.get(id_chksums2.get(key)) == null) {
		    add_set.add(key);
		    // Line number from the model 2
		    ReturnPair ret = IFC_ClassModelLibrary.calculate_nearestAddFromRemoveSet(drum1, drum2, entry.getValue(), common_gids, removed_set);
		    if (ret.difference > 8) {
			System.out.println("Added: " + entry.getValue().getClass().getSimpleName() + "(" + key + ")");
			// System.out.println("Nearest unknown. diff:"+ret.difference+
			// " org line was:"+entry.getValue().getLine_num());
			// if(ret.getT()!=null)
			// IFC_ClassModel.explain_differences(drum1,drum2,
			// ret.getT().drum_getLine_number(),entry.getValue().drum_getLine_number(),common_gids);
			// else
			drum2.listGID_Area(entry.getValue().i.drum_getLine_number());
		    } else {
			System.out.println("Changed: " + entry.getValue().getClass().getSimpleName() + "(" + key + ")");

			String ret_guid = ((IfcRoot) ret.getT()).getGlobalId();
			System.out.println("SAME AS: " + key + " = " + ret_guid);
			removed_set.remove(ret_guid); // estetään tuplakäyttö
			change_set.add(ret_guid);

			// System.out.println("c line num: "+ret.getT().drum_getLine_number());
			// System.out.println("Nearest found. diff:"+ret.difference+
			// " org line was:"+entry.getValue().getLine_num()+" nearest:"+ret.getT().getClass().getName());
			// new was found in model 2, nearest in model 1
			IFC_ClassModelLibrary.explain_differences(drum1, drum2, ret.getT().i.drum_getLine_number(), entry.getValue().i.drum_getLine_number(), common_gids);

		    }
		}
	    }
	}

	for (Map.Entry<String, Thing> entry : guids1.entrySet()) {
	    String key = entry.getKey();
	    if (!change_set.contains(key)) {
		if (guids2.get(key) == null) {
		    if (chksums2.get(id_chksums1.get(key)) == null) {
			System.out.println("key not found in change set:" + key);
			System.out.println("change set contains:");
			for (int j = 0; j < change_set.size(); j++) {
			    System.out.println("- " + change_set.get(j));
			}

			// System.out.println("r line num: "+entry.getValue().getLine_num()+" g:"+key);

			System.out.println("Removed: " + entry.getValue().getClass().getSimpleName() + "(" + key + ")");
			// drum1.listGID_Area(entry.getValue().getLine_num());
			ReturnPair ret = IFC_ClassModelLibrary.calculate_nearestRemoveFromAddSet(drum1, drum2, entry.getValue(), common_gids, add_set);
			String ret_guid = ((IfcRoot) ret.getT()).getGlobalId();
			System.out.println("remove SAME AS: " + key + " = " + ret_guid + " diff: " + ret.difference);
			IFC_ClassModelLibrary.explain_differences(drum1, drum2, entry.getValue().i.drum_getLine_number(), ret.getT().i.drum_getLine_number(), common_gids);
		    }
		}
	    }
	}

	for (Map.Entry<String, Thing> entry : guids1.entrySet()) {
	    String key = entry.getKey();
	    if (guids2.get(key) != null) {
		if (chksums2.get(id_chksums1.get(key)) == null) {
		    System.out.println("Changed: " + entry.getValue().getClass().getSimpleName() + "(" + key + ")");
		    IFC_ClassModelLibrary.explain_differences(drum1, drum2, guids1.get(key).i.drum_getLine_number(), guids2.get(key).i.drum_getLine_number(), common_gids);

		}
	    }
	}
    }

}
