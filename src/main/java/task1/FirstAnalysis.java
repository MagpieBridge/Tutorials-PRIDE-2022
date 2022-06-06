package task1;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.IMethod.SourcePosition;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.SourceFileModule;
import magpiebridge.core.AnalysisConsumer;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerAnalysis;
import magpiebridge.core.analysis.configuration.ConfigurationAction;
import magpiebridge.core.analysis.configuration.ConfigurationOption;
import magpiebridge.core.analysis.configuration.OptionType;
import magpiebridge.util.SourceCodeReader;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FirstAnalysis implements ServerAnalysis {

	private Set<JsonResult> jsonResults;
	private MagpieServer magpieServer;

	public FirstAnalysis(String pathToFindResults) {
		this.jsonResults = JsonResult.readResults(pathToFindResults);
	}

	public String source() {
		return "First Analysis";
	}

	@Override
	public void analyze(final Collection<? extends Module> files, final AnalysisConsumer server, final boolean rerun) {
		try {
			if (magpieServer == null)
				magpieServer = (MagpieServer) server;
			if (rerun) {
				Set<AnalysisResult> results = searchResults(files, magpieServer);
				magpieServer.consume(results, source());
			}
		} catch (Exception e) {
			MagpieServer.ExceptionLogger.log(e);
		}

	}

	public Set<AnalysisResult> searchResults(Collection<? extends Module> files, MagpieServer server) throws Exception {
		Set<AnalysisResult> results = new HashSet<>();
		for (Module file : files) {
			if (file instanceof SourceFileModule) {
				SourceFileModule sourceFile = (SourceFileModule) file;
				System.err.println(String.format("Touched class %s, source file stored at %s",
						sourceFile.getClassName(), sourceFile.getURL()));
				searchResultForFile(server, results, sourceFile);
			}
		}
		return results;
	}

	private void searchResultForFile(MagpieServer server, Set<AnalysisResult> results, SourceFileModule sourceFile)
			throws Exception {
		for (JsonResult result : this.jsonResults) {
			String className = sourceFile.getClassName();
			if (result.fileName.equals(className + ".java")) {
				// Note: the URL getting from files is at the server side,
				// you need to get client (the code editor) side URL for client to consume the
				// results.
				final URL clientURL = new URL(server.getClientUri(sourceFile.getURL().toString()));
				final Position clientPos = createPosition(result, clientURL);
				final Position serverPos = createPosition(result, sourceFile.getURL());
				String code = SourceCodeReader.getLinesInString(serverPos);
				AnalysisResult r = new FirstResult(result, clientPos, code);
				results.add(r);
			}
		}
	}

	private Position createPosition(final JsonResult result, final URL url) {
		final Position pos = new Position() {

			@Override
			public int getFirstCol() {
				return result.startColumn;
			}

			@Override
			public int getFirstLine() {
				return result.startLine;
			}

			@Override
			public int getFirstOffset() {
				return 0;
			}

			@Override
			public int getLastCol() {
				return result.endColumn;
			}

			@Override
			public int getLastLine() {
				return result.endLine;
			}

			@Override
			public int getLastOffset() {
				return 0;
			}

			@Override
			public int compareTo(SourcePosition arg0) {
				return 0;
			}

			@Override
			public Reader getReader() throws IOException {
				return null;
			}

			@Override
			public URL getURL() {
				return url;
			}
		};
		return pos;
	}

	@Override
	public List<ConfigurationOption> getConfigurationOptions() {
		ConfigurationOption op1 = new ConfigurationOption("setting1", OptionType.checkbox);
		ConfigurationOption op2 = new ConfigurationOption("setting2", OptionType.checkbox);
		List<ConfigurationOption> options = new ArrayList<ConfigurationOption>();
		options.add(op1);
		options.add(op2);
		return options;
	}

	@Override
	public List<ConfigurationAction> getConfiguredActions() {
		List<ConfigurationAction> actions = new ArrayList<ConfigurationAction>();
		ConfigurationAction clearWarnings = new ConfigurationAction("Clear Warnings", () -> {
			final String msg = source() + " warnings are cleared. ";
			this.magpieServer.forwardMessageToClient(new MessageParams(MessageType.Info, msg));
			this.magpieServer.cleanUp();
		});
		actions.add(clearWarnings);
		return actions;
	}

	@Override
	public void configure(List<ConfigurationOption> configuration) {
		// TODO Configure your analysis.
		ServerAnalysis.super.configure(configuration);
	}

	@Override
	public void cleanUp() {
		// TODO clean up when server is shutting down.
		ServerAnalysis.super.cleanUp();
	}

}
