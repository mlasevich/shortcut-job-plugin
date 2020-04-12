/**
 *
 */
package com.legrig.jenkins.shortcut;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkinsci.Symbol;
import org.jvnet.localizer.LocaleProvider;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.AbstractStatusIcon;
import hudson.model.Descriptor;
import hudson.model.HealthReport;
import hudson.model.Hudson;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.util.FormApply;
import hudson.util.QuotedStringTokenizer;
import jenkins.model.Jenkins;
import jenkins.model.item_category.StandaloneProjectsCategory;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class ShortcutJob extends AbstractItem implements TopLevelItem {

    private static final String ID = "shortcut-job";

    private static final String ICON_16 = "plugin/" + ID + "/images/16x16/shortcutjob.png";
    private static final String ICON_24 = "plugin/" + ID + "/images/24x24/shortcutjob.png";
    private static final String ICON_32 = "plugin/" + ID + "/images/32x32/shortcutjob.png";
    private static final String ICON_48 = "plugin/" + ID + "/images/48x48/shortcutjob.png";

    private static final Logger log = Logger.getLogger(ShortcutJob.class.getName());

    private volatile String targetUrl;
    private volatile boolean enabled;

    @DataBoundConstructor
    public ShortcutJob(ItemGroup parent, String name) {
        super(parent, name);
        this.targetUrl = null;
        this.enabled = true;
    }

    public ShortcutIcon getIconColor() {
        return new ShortcutIcon();
    }

    /**
     * Get target URL
     *
     * @return configured Target URL
     */
    public String getTargetUrl() {
        return targetUrl;
    }

    public String getLargeIcon() {
        return ICON_48;
    }

    public String getRedirectionUrl() {
        String target = getTargetUrl();
        if (StringUtils.isNotBlank(target)) {
            // If proper HTTP(*) url, return as is
            if (target.startsWith("http://") || target.startsWith("https://")) {
                return target;
            }
            // If fully qualified relative url, return as is
            if (target.startsWith("/")) {
                return target;
            }
            // Return prefixed with jenkins URL
            String rootUrl = Jenkins.getInstance().getRootUrl();
            if (rootUrl == null) {
                // TODO: do something
                log.log(Level.WARNING, "Unexpected NULL from getRootUrl()");
                rootUrl = "";
            }
            return "" + Jenkins.getInstance().getRootUrl() + target;
        }
        // If not set
        return Jenkins.getInstance().getRootUrl();
    }

    public boolean isConfigured() {
        return (!(StringUtils.isEmpty(this.targetUrl)));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    public boolean isRedirect() {
        return (isEnabled() && isConfigured());
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return new DescriptorImpl();
    }

    @Extension(ordinal = 1000)
    @Symbol({ "linkJob", "linkItemJob" })
    public static class DescriptorImpl extends TopLevelItemDescriptor {
        public DescriptorImpl() {
        }

        @Override
        public String getDisplayName() {
            return Messages.ShortcutJob_DescriptorImpl_DisplayName();
        }

        @Override
        public ShortcutJob newInstance(ItemGroup parent, String name) {
            return new ShortcutJob(parent, name);
        }

        @Override
        public String getDescription() {
            return Messages.ShortcutJob_DescriptorImpl_DescriptionText();
        }

        @Override
        public String getCategoryId() {
            return StandaloneProjectsCategory.ID;
        }

        @Override
        public String getIconClassName() {
            return "icon-shortcutjob-project";
        }

        static {
            IconSet.icons.addIcon(new Icon("icon-shortcutjob-project icon-sm", ICON_16, Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(new Icon("icon-shortcutjob-project icon-md", ICON_24, Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(new Icon("icon-shortcutjob-project icon-lg", ICON_32, Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(new Icon("icon-shortcutjob-project icon-xlg", ICON_48, Icon.ICON_XLARGE_STYLE));
        }
    }

    public final static class ShortcutIcon extends AbstractStatusIcon {
        private final Localizable description;
        private final String image;

        public ShortcutIcon() {
            image = ICON_48;
            description = Messages._ShortcutJob_ShortcutIcon_Description();
        }

        @Override
        public String getImageOf(String size) {
            String icon = image;
            switch (size) {
            case "16x16":
                icon = ICON_16;
                break;
            case "24x24":
                icon = ICON_24;
                break;
            case "32x32":
                icon = ICON_32;
                break;
            default:
                icon = ICON_48;
                break;
            }
            return Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH + "/" + icon;
        }

        @Override
        public String getDescription() {
            return description.toString(LocaleProvider.getLocale());
        }
    }

    @Override
    public Collection<? extends Job> getAllJobs() {
        return Collections.emptyList();
    }

    public boolean isNameEditable() {
        return true;
    }

    public synchronized void doConfigSubmit(final StaplerRequest req, final StaplerResponse rsp)
            throws IOException, ServletException, Descriptor.FormException {
        this.checkPermission(ShortcutJob.CONFIGURE);
        try {
            final JSONObject json = req.getSubmittedForm();
            final String targetUrl = json.optString("targetUrl", this.targetUrl);
            final boolean enabled = json.optBoolean("enabled", this.enabled);
            if ((!StringUtils.equals(this.targetUrl, targetUrl)) || (this.enabled != enabled)) {
                this.targetUrl = targetUrl;
                this.enabled = enabled;
                this.save();
            }
            final String newName = req.getParameter("name");
            if (newName != null && !newName.equals(this.name)) {
                Hudson.checkGoodName(newName);
                if (FormApply.isApply(req)) {
                    FormApply.applyResponse("notificationBar.show("
                            + QuotedStringTokenizer.quote("You must use the Save button if you wish to rename a job")
                            + ",notificationBar.WARNING)").generateResponse(req, rsp, (Object) null);
                } else {
                    rsp.sendRedirect("rename?newName=" + URLEncoder.encode(newName, "UTF-8"));
                }
            } else {
                FormApply.success(".").generateResponse(req, rsp, (Object) null);
            }
        } catch (JSONException e) {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            pw.println("Failed to parse form data. Please report this problem as a bug");
            pw.println("JSON=" + req.getSubmittedForm());
            pw.println();
            e.printStackTrace(pw);
            rsp.setStatus(400);
            this.sendError(sw.toString(), req, rsp, true);
        }
    }

    public void doLastBuild(final StaplerRequest req, final StaplerResponse rsp) throws IOException, ServletException {
        log.log(Level.WARNING, "doLastBuild: run!");
        if (rsp != null) {
            rsp.sendRedirect2(".");
        }
    }

    public HealthReport getBuildHealth() {
        HealthReport health = new HealthReport();
        health.setIconUrl(ICON_48);
        return health;
    }

    public static String getIcon() {
        return ICON_48;
    }

    @RequirePOST
    public void doDoRename(final StaplerRequest req, final StaplerResponse rsp) throws IOException, ServletException {
        this.checkPermission(ShortcutJob.CREATE);
        this.checkPermission(ShortcutJob.DELETE);
        final String newName = req.getParameter("newName");
        Hudson.checkGoodName(newName);
        this.renameTo(newName);
        rsp.sendRedirect2(req.getContextPath() + '/' + this.getParent().getUrl() + this.getShortUrl());
    }
}
