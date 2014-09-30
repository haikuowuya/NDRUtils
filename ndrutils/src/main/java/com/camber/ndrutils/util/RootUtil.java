package com.camber.ndrutils.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import java.util.LinkedList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import eu.chainfire.libsuperuser.Shell.OnCommandResultListener;

/**
 * Created by CamberCreak on 19/04/2014.
 */
public class RootUtil {

    private Shell.Interactive mShell = null;
    private HandlerThread mCallbackThread = null;

    private boolean mCommandRunning = false;
    private int mLastExitCode = -1;
    private List<String> mLastOutput = null;

    public static RootUtil getRoot() {
        return new RootUtil();
    }

    private OnCommandResultListener commandResultListener = new OnCommandResultListener() {
        @Override
        public void onCommandResult(int commandCode, int exitCode, List<String> output) {
            mLastExitCode = exitCode;
            mLastOutput = output;
            synchronized (mCallbackThread) {
                mCommandRunning = false;
                mCallbackThread.notifyAll();
            }
        }
    };

    private void waitForCommandFinished() {
        synchronized (mCallbackThread) {
            while (mCommandRunning) {
                try {
                    mCallbackThread.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }

        if (mLastExitCode == OnCommandResultListener.WATCHDOG_EXIT
                || mLastExitCode == OnCommandResultListener.SHELL_DIED)
            dispose();
    }

    /**
     * Starts an interactive shell with root permissions.
     * Does nothing if already running.
     *
     * @return true if root access is available, false otherwise
     */
    public synchronized boolean startShell() {
        if (mShell != null) {
            if (mShell.isRunning())
                return true;
            else
                dispose();
        }

        mCallbackThread = new HandlerThread("su callback listener");
        mCallbackThread.start();

        mCommandRunning = true;
        mShell = new Shell.Builder()
                .useSU()
                .setHandler(new Handler(mCallbackThread.getLooper()))
                .setWantSTDERR(true)
                .setWatchdogTimeout(10)
                .open(commandResultListener);

        waitForCommandFinished();

        if (mLastExitCode != OnCommandResultListener.SHELL_RUNNING) {
            dispose();
            return false;
        }

        return true;
    }

    /**
     * Closes all resources related to the shell.
     */
    public synchronized void dispose() {
        if (mShell == null)
            return;

        try {
            mShell.close();
        } catch (Exception ignored) {
        }
        mShell = null;

        mCallbackThread.quit();
        mCallbackThread = null;
    }

    /**
     * Executes a single command, waits for its termination and returns the result
     */
    public synchronized int execute(String command, List<String> output) {
        if (mShell == null)
            startShell();
        mCommandRunning = true;
        mShell.addCommand(command, 0, commandResultListener);
        waitForCommandFinished();

        if (output != null)
            output.addAll(mLastOutput);

        return mLastExitCode;
    }

    /**
     * Executes a single command via system Shell
     *
     * @param command root command to be executed
     * @return int exit code
     */
    public int ShellForCode(String command) {
        List<String> output = new LinkedList<String>();
        return execute(command, output);
    }

    /**
     * Executes a single command via system Shell
     *
     * @param command root command to be executed
     * @return String joined string of stdout
     */
    public String ShellForString(String command) {
        List<String> output = new LinkedList<String>();
        execute(command, output);
        return TextUtils.join("\n", output).trim();
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
    }
}