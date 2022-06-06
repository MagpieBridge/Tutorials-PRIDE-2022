package task2.taintanalysis;

import java.util.List;

/**
 * 
 * @author Linghui Luo
 *
 */
public class TaintResult {
	public String classSignature;
	public int sinkLn;
	public int sourceLn;
	public List<Integer> related;

	public TaintResult(String classSignature, int sinkLn, int sourceLn, List<Integer> related) {
		this.classSignature = classSignature;
		this.sinkLn = sinkLn;
		this.sourceLn = sourceLn;
		this.related = related;
	}

	@Override
	public String toString() {
		return "SecondResult{" +
				"classSignature='" + classSignature + '\'' +
				", sinkLn=" + sinkLn +
				", sourceLn=" + sourceLn +
				", related=" + related +
				'}';
	}
}