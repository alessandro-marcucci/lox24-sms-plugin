package lox24sms.lox24sms;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

/**
 * @author Alessandro Marcucci
 */
public class LOX24SMSNotification extends Notifier {

	private final String recipients;
	private final boolean firstFail;
	private final boolean firstSuccess;

	@DataBoundConstructor
	public LOX24SMSNotification(String recipients, boolean firstFail, boolean firstSuccess) {
		this.recipients = recipients;
		this.firstFail = firstFail;
		this.firstSuccess = firstSuccess;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
		String server = "www.lox24.eu";
		String from = "OMD-Team";
		String konto = getDescriptor().getKonto();
		String password = getDescriptor().getPassword();
		String service = getDescriptor().getService();

		if (build.getResult() == Result.FAILURE || build.getResult() == Result.UNSTABLE) {
			try {
				Result previousResult = build.getPreviousBuild().getResult();
				if (firstFail) {

					if (previousResult == Result.SUCCESS) {
						sendMessage(server, konto, password, service, from, getFailureMessage(build), listener);
					}
				} else {
					sendMessage(server, konto, password, service, from, getFailureMessage(build), listener);
				}
			} catch (NullPointerException e) {
				sendMessage(server, konto, password, service, from, getFailureMessage(build), listener);
			}
		} else if (build.getResult() == Result.SUCCESS) {
			try {
				Result previousResult = build.getPreviousBuild().getResult();
				if (firstSuccess) {
					if (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE) {
						sendMessage(server, konto, password, service, from, getFixedMessage(build), listener);
					}
				} else {
					if (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE) {
						sendMessage(server, konto, password, service, from, getFixedMessage(build), listener);
					} else {
						sendMessage(server, konto, password, service, from, getSuccessMessage(build), listener);
					}
				}
			} catch (NullPointerException e) {
				sendMessage(server, konto, password, service, from, getSuccessMessage(build), listener);
			}
		}
		return true;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	public String getRecipients() {
		return recipients;
	}

	public boolean getFirstFail() {
		return firstFail;
	}

	public boolean getFirstSuccess() {
		return firstSuccess;
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		private String konto;
		private String password;
		private String service;

		public DescriptorImpl() {
			super(LOX24SMSNotification.class);
			load();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "LOX24 SMS Notification";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			konto = formData.getString("konto");
			password = formData.getString("password");
			service = formData.getString("service");
			save();
			return super.configure(req, formData);
		}

		public FormValidation doNumberCheck(@QueryParameter("recipients") String param) throws IOException, ServletException {
			if (param == null || param.trim().length() == 0) {
				return FormValidation.warning("You must fill recipients' numbers!");
			}

			param = param.trim().replaceAll("\\s", "");
			for (String p : param.split(",")) {
				if (!PhoneNumberValidator.validatePhoneNumber(p)) {
					return FormValidation.error("Formats of some recipients' numbers are invalid.");
				}
			}

			return FormValidation.ok();
		}

		public String getKonto() {
			return konto;
		}

		public String getPassword() {
			return password;
		}

		public String getService() {
			return service;
		}
	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	private String getDateString(Date d) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
	}

	private String getRequest(String server, String konto, String password, String service, String message, String from, String to) {
		return "https://" + server + "/API/httpsms.php?konto=" + konto + "&password=" + password + "&service=" + service + "&text=" + message
				+ "&from=" + from + "&to=" + to + "&httphead=0";
	}

	private String getFailureMessage(AbstractBuild<?, ?> build) {
		String message = URLEncoder.encode("Jenkins Build failed: " + build.getProject().getDisplayName() + " at " + getDateString(build.getTime()));

		if (message.length() > 150) {
			message =
				URLEncoder.encode("Jenkins Build failed: " + build.getProject().getDisplayName().substring(0, 100) + "... " + " at "
						+ getDateString(build.getTime()));
		}
		return message;
	}

	private String getFixedMessage(AbstractBuild<?, ?> build) {
		String message = URLEncoder.encode("Jenkins Build fixed: " + build.getProject().getDisplayName() + " at " + getDateString(build.getTime()));

		if (message.length() > 150) {
			message =
				URLEncoder.encode("Jenkins Build fixed: " + build.getProject().getDisplayName().substring(0, 100) + "... " + " at "
						+ getDateString(build.getTime()));
		}
		return message;
	}

	private String getSuccessMessage(AbstractBuild<?, ?> build) {
		String message = URLEncoder.encode("Jenkins Build succeed: " + build.getProject().getDisplayName() + " at " + getDateString(build.getTime()));

		if (message.length() > 150) {
			message =
				URLEncoder.encode("Jenkins Build succeed: " + build.getProject().getDisplayName().substring(0, 100) + "... " + " at "
						+ getDateString(build.getTime()));
		}
		return message;
	}

	private boolean sendMessage(String server, String konto, String password, String service, String from, String message, BuildListener listener) {
		int result = 401;
		if (isEmpty(recipients)) {
			listener.error("No recipients");
		}

		if (isEmpty(konto) || isEmpty(password) || isEmpty(service)) {
			listener.error("LOX24 credentials not configured.");
		}

		List<String> receiverList = new ArrayList<String>();

		String recipientStr = recipients.trim().replaceAll("\\s", "");
		receiverList.addAll(Arrays.asList(recipientStr.split(",")));
		System.out.println(receiverList);
		HttpClient client = new HttpClient();

		for (String n : receiverList) {
			String request = getRequest(server, konto, password, service, message, from, n);
			System.out.println(request);
			GetMethod get = new GetMethod(request);
			try {
				result = client.executeMethod(get);
			} catch (Exception e) {
				listener.error("Failed to send SMS notification to :" + n);
			} finally {
				get.releaseConnection();
			}
			if (result != 200) {
				listener.error("Failed to send SMS notification to :" + n);
			}
		}
		return true;
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
}
