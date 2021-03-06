package com.kiuwan.plugins.kiuwanJenkinsPlugin.util;

import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class KiuwanUtilsTest {

	private static final String WINDOWS_SCRIPT_FILENAME = "testWindowsEscape.cmd";
	private static final String ARGS_TEST_BATTERY_FILENAME = "testWindowsEscape.txt";
	
	private static Path resourcesPath;
	private static String windowsCmdFile;
	private static List<String> testStrings;
	
	@BeforeClass
	public static void beforeClass() throws URISyntaxException, FileNotFoundException, IOException {
		assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("win"));
		
		String packageName = KiuwanUtilsTest.class.getPackage().getName();
		URL url = KiuwanUtilsTest.class.getResource("/" + packageName.replace(".", "/"));
		resourcesPath = Paths.get(url.toURI());
		windowsCmdFile = resourcesPath.resolve(WINDOWS_SCRIPT_FILENAME).toAbsolutePath().toString();
		
		testStrings = new ArrayList<>();
		try (Stream<String> stream = Files.lines(resourcesPath.resolve(ARGS_TEST_BATTERY_FILENAME))) {
	        stream.forEach(line -> testStrings.add(line));
		}
	}
	
	@Test
	public void testWindowsEscape() throws IOException, URISyntaxException {
		for (String testString : testStrings) {
			System.out.println("-------------");
			System.out.println("Test string \t\t= " + testString);
			String processedTestString = KiuwanUtils.escapeArg(false, testString);
			System.out.println("Processed string \t= " + processedTestString);
			int ret = testCmdCall(testString, processedTestString);
			Assert.assertEquals(0, ret);
		}
	}
	
	private int testCmdCall(String originalString, String processedString) throws IOException, URISyntaxException {
		Process proc = new ProcessBuilder()
			.directory(resourcesPath.toFile())
			.command(windowsCmdFile, processedString)
			.redirectErrorStream(true)
			.start();

		try {
			String s;
			BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			boolean found = false;
			while ((s = stdout.readLine()) != null) {
				System.out.println("Java output\t\t\t= " + s);
				if (originalString.equals(s)) {
					found = true;
					System.out.println("Match found for\t\t= " + originalString);
				}
			}
			Assert.assertTrue(found);

			int exitValue = proc.waitFor();
			return exitValue;

		} catch (InterruptedException e) {
			return -1;

		} finally {
			proc.getInputStream().close();
			proc.getOutputStream().close();
			proc.getErrorStream().close();
		}
	}
	
}
