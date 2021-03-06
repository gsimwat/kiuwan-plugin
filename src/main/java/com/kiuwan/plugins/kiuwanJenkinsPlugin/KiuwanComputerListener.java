package com.kiuwan.plugins.kiuwanJenkinsPlugin;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import com.kiuwan.plugins.kiuwanJenkinsPlugin.runnable.KiuwanRunnable;
import com.kiuwan.plugins.kiuwanJenkinsPlugin.util.KiuwanUtils;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.slaves.ComputerListener;
import jenkins.model.Jenkins;

@Extension
public class KiuwanComputerListener extends ComputerListener {

    @Inject
    private KiuwanDownloadable kiuwanDownloadable;

    @Override
    public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
    	// work around the bug where master doesn't call preOnline method
    	if (c.getNode()== Jenkins.getInstance()) {
            process(c, c.getNode().getRootPath(), listener);
        }
    }

    @Override
    public void preOnline(Computer c, Channel channel, FilePath root, TaskListener listener) throws IOException, InterruptedException {
        process(c, root,listener);
    }

    public void process(Computer c, FilePath root, TaskListener listener) throws IOException, InterruptedException {
    	KiuwanDescriptor descriptor = (KiuwanDescriptor) Jenkins.getInstance().getDescriptor(KiuwanRecorder.class);
    	try {
            String installDir = KiuwanUtils.getPathFromConfiguredKiuwanURL(KiuwanRunnable.AGENT_DIRECTORY, descriptor);
    		FilePath remoteDir = root.child(installDir);
            if (!remoteDir.child(KiuwanRunnable.AGENT_HOME).exists()) {
                listener.getLogger().println("Installing KiuwanAnalyzer to "+remoteDir);
                File zip = kiuwanDownloadable.resolve(listener, descriptor);
                remoteDir.mkdirs();
                new FilePath(zip).unzip(remoteDir);
            }
        } catch (IOException e) {
            listener.error("Failed to install KiuwanAnalyzer: " + e);
            // but continuing
        }
    }
    
}
