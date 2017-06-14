//package org.bioontology.ncitProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class NCIVerify {
	static private Project _sourceProject = null;
	static private KnowledgeBase _sourceKB = null;
	
	public static void main(String[] args) {
		Collection errors = new ArrayList();
		Log.getLogger().info("Start conversion at " + new Date ());
		
		String sourceFileName =  args[0];
		
		_sourceProject = new Project(sourceFileName, errors);
		if (errors.size() != 0) {
			displayErrors(errors);
			return;
		}
		
		_sourceKB = _sourceProject.getKnowledgeBase();
		
		process();
		
		Log.getLogger().info("DONE: " + new Date());
	}
	
	private static void process () {
		Collection allClasses = new ArrayList ();
		allClasses.addAll(((OWLModel) _sourceKB).getUserDefinedRDFSNamedClasses());
		
		Log.getLogger().info(allClasses.toString());
		
//		Queue<Cls> clses = new LinkedList<Cls> ();
//		Collection<Cls> roots = _sourceKB.getRootClses();
//		clses.addAll(roots);
//		
//	
//		for (int count = 0; count < 100000; count ++) {
//			Cls next = clses.poll();
//			if (next == null) return;
//			
//			Collection<Cls> subs = next.getDirectSubclasses();
//			clses.addAll(subs);
//		}
	}

	private static void displayErrors(Collection errors) {
		Iterator i = errors.iterator();
		while (i.hasNext()) {
			System.out.println("Error: " + i.next());
		}
	}
	

}
