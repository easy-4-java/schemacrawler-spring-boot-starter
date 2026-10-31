/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2016, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------
 
SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 
SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.
 
You may elect to redistribute this code under any of these licenses.
 
The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html
 
The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/
 
========================================================================
*/
package schemacrawler.spring.boot.ext.command;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.util.StringUtils;

import us.fatehi.utility.string.StringFormat;

/**
 * Executes external commands as a {@link Callable} and captures stdout/stderr.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class ProcessExecutor implements Callable<Integer> {

	final class StreamReader implements Callable<String> {

		private final InputStream in;

		private StreamReader(final InputStream in) {
			if (in == null) {
				throw new RuntimeException("No input stream provided");
			}
			this.in = in;
		}

		@Override
    /**
     * <p>Call.</p>
     * @return the call
     */
		public String call() throws Exception {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}

	}

	private static final Logger LOGGER = Logger.getLogger(ProcessExecutor.class.getName());

	private List<String> command;
	private String processOutput;
	private String processError;
	private int exitCode;

	@Override
    /**
     * <p>Call.</p>
     * @return the call
     */
	public Integer call() throws IOException {
		requireNonNull(command, "No command provided");
		if (command.isEmpty()) {
			throw new IOException("No command provided");
		}

		LOGGER.log(Level.CONFIG, new StringFormat("Executing:%n%s", command));

		final ExecutorService threadPool = Executors.newFixedThreadPool(2);
		try {

			final ProcessBuilder processBuilder = new ProcessBuilder(command);
			final Process process = processBuilder.start();

			final FutureTask<String> inReaderTask = new FutureTask<>(new StreamReader(process.getInputStream()));
			threadPool.execute(inReaderTask);
			final FutureTask<String> errReaderTask = new FutureTask<>(new StreamReader(process.getErrorStream()));
			threadPool.execute(errReaderTask);

			exitCode = process.waitFor();

			processOutput = inReaderTask.get();
			processError = errReaderTask.get();

			return exitCode;
		} catch (final SecurityException | ExecutionException | InterruptedException e) {
			throw new IOException(e.getMessage(), e);
		} catch (final Throwable t) {
			LOGGER.log(Level.SEVERE, t.getMessage(), t);
			throw new IOException(t.getMessage(), t);
		} finally {
			threadPool.shutdown();
		}
	}

    /**
     * <p>Returns the command.</p>
     * @return the get command
     */
	public List<String> getCommand() {
		return command;
	}

    /**
     * <p>Returns the exit code.</p>
     * @return the get exit code
     */
	public int getExitCode() {
		return exitCode;
	}

    /**
     * <p>Returns the process error.</p>
     * @return the get process error
     */
	public String getProcessError() {
		return processError;
	}

    /**
     * <p>Returns the process output.</p>
     * @return the get process output
     */
	public String getProcessOutput() {
		return processOutput;
	}

    /**
     * <p>Sets the command line.</p>
     * @param args
     */
	public void setCommandLine(final List<String> args) {
		requireNonNull(args, "No command provided");
		if (args.isEmpty() || !StringUtils.hasText(args.get(0))) {
			throw new IllegalArgumentException("No executable provided");
		}
		for (final String arg : args) {
			if (arg == null) {
				throw new IllegalArgumentException("Command arguments must not be null");
			}
		}
		// ProcessBuilder takes raw argument values. Empty and whitespace arguments
		// are significant and must retain their positions.
		command = new ArrayList<String>(args);
	}

}