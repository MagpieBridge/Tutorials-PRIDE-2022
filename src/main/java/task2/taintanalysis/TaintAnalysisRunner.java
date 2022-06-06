package task2.taintanalysis;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.options.Options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class TaintAnalysisRunner {

	public static Collection<TaintResult> doTaintAnalysis(Set<String> classPath, Set<String> libPath) {
		// initialize soot
		G.v();
		G.reset();
		List<String> path = new ArrayList<>();
		path.addAll(classPath);
		path.addAll(libPath);
		Options.v().set_process_dir(path);
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_ignore_resolving_levels(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_prepend_classpath(true);
		Options.v().set_keep_line_number(true);
		SimpleTransformer t = new SimpleTransformer();
		PackManager.v().getPack("jap").add(new Transform("jap.myTransformer", t));
		Scene.v().loadNecessaryClasses();
		PackManager.v().runBodyPacks();
		Collection<TaintResult> results = t.getAnalysisResults();
		return results;
	}


}
