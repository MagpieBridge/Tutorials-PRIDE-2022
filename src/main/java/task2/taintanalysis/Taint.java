package task2.taintanalysis;

import soot.Unit;
import soot.Value;

import java.util.List;

/**
 * 
 * @author Linghui Luo
 *
 */
public class Taint {

	private Unit source;
	private List<Unit> path;
	private Value value;

	public Taint(Unit source, List<Unit> path, Value value) {
		this.source = source;
		this.path = path;
		this.value = value;
	}

	public Unit getSource() {
		return source;
	}

	public void setSource(Unit source) {
		this.source = source;
	}

	public List<Unit> getPath() {
		return path;
	}

	public void setPath(List<Unit> path) {
		this.path = path;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
