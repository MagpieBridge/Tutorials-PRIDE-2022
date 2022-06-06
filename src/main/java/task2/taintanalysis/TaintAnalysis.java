package task2.taintanalysis;

import soot.Body;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A very simple intra-procedural taint analysis.
 * 
 * @author Linghui Luo
 *
 */
public class TaintAnalysis extends ForwardFlowAnalysis<Unit, Set<Taint>> {

	private Collection<TaintResult> results;
	private Body body;

	public TaintAnalysis(Body body) {
		super(new ExceptionalUnitGraph(body));
		this.results = new HashSet<>();
		this.body = body;
	}

	@Override
	protected void flowThrough(Set<Taint> in, Unit unit, Set<Taint> out) {
		out.addAll(in);
		if (unit instanceof AssignStmt) {
			AssignStmt assignStmt = (AssignStmt) unit;
			Value leftOp = assignStmt.getLeftOp();
			Value rightOp = assignStmt.getRightOp();
			for (Taint t : in) {
				if (t.getValue().equals(rightOp)) {
					List<Unit> path = t.getPath();
					path.add(unit);
					Taint newTaint = new Taint(t.getSource(), path, leftOp);
					out.add(newTaint);
				}
			}

			if (rightOp instanceof InvokeExpr) {
				InvokeExpr invoke = (InvokeExpr) rightOp;

				if (invoke.toString().contains("<task2.Demo: java.lang.String source()>")) {
					List<Unit> path = new ArrayList<>();
					path.add(unit);
					Taint newTaint = new Taint(unit, path, leftOp);
					out.add(newTaint);
				}
				if (invoke.toString()
						.contains("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>")) {
					for (Taint t : in) {
						if (invoke.getArgs().contains(t.getValue())) {
							List<Unit> path = t.getPath();
							path.add(unit);
							Taint newTaint = new Taint(t.getSource(), path, leftOp);
							out.add(newTaint);
						}
					}
				}
				if (invoke.toString().contains("<java.lang.StringBuilder: java.lang.String toString()>")) {
					for (Taint t : in) {
						VirtualInvokeExpr virtualInvoke = (VirtualInvokeExpr) invoke;
						if (virtualInvoke.getBase().equals(t.getValue())) {
							List<Unit> path = t.getPath();
							path.add(unit);
							Taint newTaint = new Taint(t.getSource(), path, leftOp);
							out.add(newTaint);
						}
					}
				}
			}

		}

		if (unit instanceof InvokeStmt) {
			InvokeStmt invokeStmt = (InvokeStmt) unit;
			if (invokeStmt.toString().contains("<task2.Demo: void sink(java.lang.String)>")) {
				for (Taint taint : in) {
					if (invokeStmt.getInvokeExpr().getArgs().contains(taint.getValue())) {
						int sinkLn = unit.getJavaSourceStartLineNumber();
						String classSignature = body.getMethod().getDeclaringClass().getName();
						int sourceLn = taint.getSource().getJavaSourceStartLineNumber();
						List<Integer> relatedInfo = new ArrayList<>();
						for (Unit n : taint.getPath()) {
							int linenumber = n.getJavaSourceStartLineNumber();
							if(!relatedInfo.contains(linenumber)) {
								relatedInfo.add(linenumber);
							}
						}
						results.add(new TaintResult(classSignature, sinkLn, sourceLn, relatedInfo));
					}
				}
			}

		}
	}

	public void doAnalysis() {
		super.doAnalysis();
	}

	public Collection<TaintResult> getResults() {
		return results;
	}

	@Override
	protected Set<Taint> newInitialFlow() {
		return new HashSet<>();
	}

	@Override
	protected void merge(Set<Taint> in1, Set<Taint> in2, Set<Taint> out) {
		for (Taint t : in1) {
			if (!out.contains(t))
				out.add(t);
		}

		for (Taint t : in2) {
			if (!out.contains(t))
				out.add(t);
		}
	}

	@Override
	protected void copy(Set<Taint> source, Set<Taint> dest) {
		for (Taint t : source) {
			dest.add(new Taint(t.getSource(), t.getPath(), t.getValue()));
		}
	}

}