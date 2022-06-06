import magpiebridge.core.IProjectService;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerAnalysis;
import magpiebridge.core.ServerConfiguration;
import magpiebridge.core.ToolAnalysis;
import magpiebridge.projectservice.java.JavaProjectService;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import task1.FirstAnalysis;
import task2.SecondAnalysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class TutorialMain {

	public static void main(String... args) {
		String preparedFile = "./testProject/preparedResults.json";
		// launch on Standard I/O. Note don't use System.out
		// to print text messages to console, it will block the channel.

		// createServer(preparedFile).launchOnStdio();

		// launch on Socket, good for debugging
		MagpieServer.launchOnSocketPort(5007, () -> createServer(preparedFile));
	}

	private static MagpieServer createServer(String preparedFile) {
		// Step 1: Create a MagpieServer and configure it

		ServerConfiguration config = setConfig();
		MagpieServer server = new MagpieServer(config);

		// Step 2: Create your analysis and add to Server.
		ServerAnalysis firstAnalysis = new FirstAnalysis(preparedFile);
		String language = "java";
		Either<ServerAnalysis, ToolAnalysis> first = Either.forLeft(firstAnalysis);
		server.addAnalysis(first, language);

		// Step 3 (Task 2): Add a project service.
		IProjectService javaProjectService = new JavaProjectService();
		server.addProjectService(language, javaProjectService);

		ServerAnalysis secondAnalysis = new SecondAnalysis();
		Either<ServerAnalysis, ToolAnalysis> second = Either.forLeft(secondAnalysis);
		server.addAnalysis(second, language);

		return server;
	}

	private static ServerConfiguration setConfig() {
		ServerConfiguration config = new ServerConfiguration();
		try {
			// log the communications

			File traceFile = new File (System.getProperty("user.dir") + File.separator + "magpie_server_trace.lsp");
			config.setLSPMessageTracer(new PrintWriter(traceFile));

			// Task 1
			config.setDoAnalysisByOpen(true);

			// Task 2
			// config.setDoAnalysisBySave(true);
			// config.setShowDataFlowGraph(true);

			// Task 3
			// config.setDoAnalysisByFirstOpen(false);
			// config.setDoAnalysisBySave(false);
			// config.setShowConfigurationPage(true, true);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return config;
	}

}
