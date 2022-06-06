package task2;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.util.collections.Pair;
import magpiebridge.core.AnalysisConsumer;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.IProjectService;
import magpiebridge.core.Kind;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerAnalysis;
import magpiebridge.core.analysis.configuration.ConfigurationAction;
import magpiebridge.core.analysis.configuration.ConfigurationOption;
import magpiebridge.core.analysis.configuration.OptionType;
import magpiebridge.core.analysis.dataflow.FlowAnalysisResult;
import magpiebridge.projectservice.java.JavaProjectService;
import magpiebridge.util.SourceCodeInfo;
import magpiebridge.util.SourceCodePositionFinder;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import task2.taintanalysis.TaintAnalysisRunner;
import task2.taintanalysis.TaintResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 
 * @author Linghui Luo
 *
 */
public class SecondAnalysis implements ServerAnalysis {
	private Set<String> classPath;
	private Set<String> libPath;
	private MagpieServer magpieServer;
	private JavaProjectService javaProjectService;
	public SecondAnalysis() { }

	@Override
	public String source() {
		return "Second Analysis";
	}

	@Override
	public void analyze(Collection<? extends Module> files, AnalysisConsumer server, boolean rerun) {
		if (magpieServer == null)
			magpieServer = (MagpieServer) server;
		if (rerun) {
			setClassPath(magpieServer);
			try {
				Process process = new ProcessBuilder().directory(javaProjectService.getRootPath().get().toFile())
						.command("/usr/local/bin/mvn", "compile").start();
				if (process.waitFor() == 0){
					Collection<TaintResult> results = Collections.emptyList();
					if (classPath != null) {
						results = TaintAnalysisRunner.doTaintAnalysis(classPath, libPath);
					}
					Collection<AnalysisResult> converted = convert(results);
					magpieServer.consume(converted, source());
				}
			}catch (IOException | InterruptedException e){
				magpieServer.forwardMessageToClient(new MessageParams(MessageType.Error, e.getMessage()));
			}
		}
	}

	private Collection<AnalysisResult> convert(Collection<TaintResult> results) {
		Set<AnalysisResult> res = new HashSet<>();
		for(TaintResult result : results ) {
			String classSignature = result.classSignature;
			try {
				Optional<Path> sourceFilePath = javaProjectService.getSourceFilePath(classSignature);

				if (sourceFilePath.isPresent()) {
					File sourceFile = sourceFilePath.get().toFile();
					SourceCodeInfo sink = SourceCodePositionFinder.findCode(sourceFile, result.sinkLn);
					SourceCodeInfo source = SourceCodePositionFinder.findCode(sourceFile, result.sourceLn);
					Kind kind = Kind.Diagnostic;
					String msg =  "Find a taint flow from " + source.code + " to "+ sink.code;
					List<Pair<Position, String>> related = new ArrayList<>();
					for (Integer ln: result.related) {
						SourceCodeInfo pos = SourceCodePositionFinder.findCode(sourceFile, ln);
						Pair<Position, String> pair = Pair.make(pos.toPosition(), pos.code);
						related.add(pair);
					}
					related.add(Pair.make(sink.toPosition(), sink.code));
					res.add(new FlowAnalysisResult(kind, sink.toPosition(),
							msg, related, DiagnosticSeverity.Error, null, sink.code));
				}
			}catch (IOException e){
				e.printStackTrace();
			}
		}
		return  res;
	}

	/**
	 * set up class path and library path with the project service provided by
	 * the server.
	 *
	 * @param server
	 */
	public void setClassPath(MagpieServer server) {
		if (classPath == null) {
			Optional<IProjectService> projectService= server.getProjectService("java");
			if (projectService.isPresent()) {
				javaProjectService = (JavaProjectService) projectService.get();

				if (libPath == null) {
					libPath = new HashSet<>();
					for (Path path : javaProjectService.getLibraryPath()) {
						if (path.toFile().exists()) {
							libPath.add(path.toString());
						}
					}
				}
				classPath = new HashSet<>();
				for (Path path : javaProjectService.getClassPath()) {
					if (path.toFile().exists()) {
						classPath.add(path.toString());
					}
				}
			}
		}
	}

	@Override
	public List<ConfigurationOption> getConfigurationOptions() {
		ConfigurationOption op1 = new ConfigurationOption("anotherSetting1", OptionType.checkbox);
		ConfigurationOption op2 = new ConfigurationOption("anothersetting2", OptionType.checkbox);
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
