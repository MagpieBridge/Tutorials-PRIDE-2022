package task1;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class JsonResult {

	protected String fileName;
	protected String msg;
	protected int startLine;
	protected int startColumn;
	protected int endLine;
	protected int endColumn;
	protected String repair;

	/**
	 * Read results from a JSON file.
	 * 
	 * @param pathToFindResults the path to the JSON file.
	 * @return a set of result read from the JSON file.
	 */
	public static Set<JsonResult> readResults(String pathToFindResults) {
		Set<JsonResult> ret = new HashSet<>();
		try {
			JsonObject obj = JsonParser.parseReader(new FileReader(new File(pathToFindResults))).getAsJsonObject();
			JsonArray results = obj.get("results").getAsJsonArray();
			for (int i = 0; i < results.size(); i++) {
				JsonObject result = results.get(i).getAsJsonObject();
				JsonResult res = new JsonResult();
				res.fileName = result.get("fileName").getAsString();
				res.msg = result.get("msg").getAsString();
				res.startLine = result.get("startLine").getAsInt();
				res.startColumn = result.get("startColumn").getAsInt();
				res.endLine = result.get("endLine").getAsInt();
				res.endColumn = result.get("endColumn").getAsInt();
				res.repair = result.get("repair").getAsString();
				ret.add(res);
			}
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}
}
