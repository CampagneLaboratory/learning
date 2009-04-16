/*
 * Copyright (C) 2006-2009 Institute for Computational Biomedicine,
 *                         Weill Medical College of Cornell University
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.cornell.med.icb.tools.svmlight;

import cern.colt.Timer;
import edu.mssm.crover.cli.CLI;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author campagne Date: Mar 1, 2006 Time: 5:28:43 PM
 */
public class SVMLightDriver {
    private boolean installed;
    private final String fileSeparator = System.getProperty("file.separator");
    private int timeout = 3600 * 1000; // one hour.
    private String commandPath;
    private String executableName;

    public EvaluationMeasure run(final String inputFilename)
            throws InterruptedException, IOException {

        // get the properties, if path and binary name haven't already beed set.
        // Revert to defaults if properties are not set.

        if (getCommandPath() == null) {
            if (CLI.getProperty("svmlight.executable.path") != null) {
                setCommandPath(CLI.getProperty("svmlight.executable.path"));
            } else {
                setCommandPath("/usr/local/softs/svmlight");
            }
        }

        if (getExecutableName() == null) {
            if (CLI.getProperty("svmlight.executable.name") != null) {
                setExecutableName(CLI.getProperty("svmlight.executable.name"));
            } else {
                setExecutableName("svm_learn");
            }
        }

        if (!installed()) {
            throw new IllegalStateException("svmlight not installed");
        }
        File model = null;
        try {

            model = File.createTempFile("svmlight", ".svm");
            final StringBuilder command = new StringBuilder();
            command.append(getCommandPath());
            command.append(fileSeparator);
            command.append(getExecutableName());
            command.append(" -x 1 ");
            command.append(" -# 100 "); // Terminate optimization if no progress after that many steps.
            command.append(inputFilename);
            command.append(' ');
            command.append(model.getCanonicalFile());
            //  System.out.println("command: " + command.toString());
            final Process process = Runtime.getRuntime().exec(command.toString());

            final InputStream is = process.getInputStream();
            final EvaluationMeasure measure = extractPerfMeasures(is, process);

            process.waitFor();

            //  System.out.print("waiting for training/evaluation to complete...");
            //  System.out.flush();

            //   System.out.println(" done");
            //   System.out.flush();
            return measure;

        } finally {
            if (model != null) {
                model.delete();
            }
        }
    }

    /**
     * Set the timeout for the svmLight process to terminate.
     *
     * @param timeout Timeout in milliseconds.
     */
    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public EvaluationMeasure extractPerfMeasures(final InputStream is, final Process process) throws IOException {

        final Timer timer = new Timer();
        timer.start();

        final EvaluationMeasureCache measures = new EvaluationMeasureCache();
        final BufferedReader br = new BufferedReader(new InputStreamReader(is));
        int lineRead = 0;
        String line;
        int code;
        StringBuffer lineBuffer = new StringBuffer();

        while ((code = br.read()) != -1) {
            if (timer.millis() > timeout) {

                // if the process has not finished within the time imparted.
                process.destroy();
                System.err.println("process aborted after timeout: " + timeout + " (ms)");
                return measures;
            }
            lineBuffer.append((char) code);
            if (code != '\n') {
                continue;
            } else {
                line = lineBuffer.toString();
                lineBuffer = new StringBuffer();

                if (line.startsWith("Leave-one-out estimate of the ")) {
                    final String[] tokens = line.split("[=\\%]");
                    final double value;
                    try {
                        value = Double.parseDouble(tokens[1]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    if (tokens[0].contains("error")) {
                        measures.setErrorRate(value);
                    } else if (tokens[0].contains("precision")) {
                        measures.setPrecision(value);
                    } else if (tokens[0].contains("recall")) {
                        measures.setRecall(value);
                    }
                }
            }

            lineRead++;
        }
        assert lineRead
                > 0 : "We must be able to read something from the svm light stdout. If not, there is a problem. ";
        return measures;
    }

    /**
     * Checks if the command is installed on this machine. Check to see if both /usr/local/softs/blast/blastall (or
     * blast.executable.path/blast.executable.name) and /data/db/blast/<database> (or blast.database.path/<database>)
     *
     * @return True when blast is installed on the machine, False otherwise.
     */
    public boolean installed(final String
            cmdPath, final String
            execName) {
        if (installed) {
            return true;
        }

        final File prog = new File(cmdPath, execName);
        if (!prog.exists()) {
            return false;
        }
        if (!prog.canRead()) {
            return false;
        }
        installed = true;
        return true;
    }

    public boolean installed() {
        return installed(getCommandPath(), getExecutableName());
    }

    public String getCommandPath() {
        return commandPath;
    }

    public void setCommandPath(final String commandPath) {
        this.commandPath = commandPath;
    }

    public String getExecutableName() {
        return executableName;
    }

    public void setExecutableName(final String executableName) {
        this.executableName = executableName;
    }
}
