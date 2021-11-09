package com.legrig.jenkins.shortcut;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertFalse;

public class ShortcutJobTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void notConfiguredTest() throws Exception {
        ShortcutJob job = jenkins.createProject(ShortcutJob.class);
        assertFalse(job.isConfigured());
    }

}
