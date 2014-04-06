package com.vladionescu.codejam.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class CodejamSolver<S extends ISolution> {

	private final S solution;
	private final File input;
	private final File output;

	public CodejamSolver(final S solution, final File input, final File output)
			throws IOException {
		this.solution = solution;
		this.input = input;
		this.output = output;

		if (!(input != null && input.exists() && input.isFile())) {
			throw new IllegalArgumentException("File " + input
					+ " cannot be read!");
		}
		if (!(output != null && (output.createNewFile() || output.canWrite()))) {
			throw new IllegalArgumentException("File " + output + " cannot be written!");
		}
	}

	public void solve() throws IOException {
		try (final BufferedReader in = new BufferedReader(new FileReader(input));
				final PrintStream out = new PrintStream(output)) {
			final int testCases = Integer.parseInt(in.readLine());
			for (int i = 1; i <= testCases; i++) {
				out.println("Case #" + i + ": " + solution.solveTestCase(in));
			}
		}
	}

	public static void main(final String args[]) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, IOException {
		@SuppressWarnings("unchecked")
		Class<ISolution> solutionClass = (Class<ISolution>) Class
				.forName(args[0]);
		ISolution solution = solutionClass.newInstance();
		CodejamSolver<ISolution> solver = new CodejamSolver<ISolution>(
				solution, new File(args[1]), new File(args[2]));
		solver.solve();
	}

}
