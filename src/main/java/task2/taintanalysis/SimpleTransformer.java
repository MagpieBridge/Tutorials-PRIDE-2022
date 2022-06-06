package task2.taintanalysis;

import soot.Body;
import soot.BodyTransformer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class SimpleTransformer extends BodyTransformer {

	private Collection<TaintResult> results;

	public SimpleTransformer() {
		results = new HashSet<>();
	}

	public Collection<TaintResult> getAnalysisResults() {
		return results;
	}

	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
		TaintAnalysis analysis = new TaintAnalysis(b);
		analysis.doAnalysis();
		results.addAll(analysis.getResults());
	}

}