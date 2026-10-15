package com.rei.examenbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.reminder")
public class ReminderProperties {
    private boolean enabled = true;
    private boolean mailEnabled = true;
    private String from = "no-reply@localhost";
    private String subject = "Your daily Examen reminder";
    private String appUrl = "http://localhost:4200/examination";
    private String inAppTitle = "Daily Examen reminder";
    private String inAppMessage = "It is time for your daily examination. Open Examen to begin.";
    private String cron = "0 * * * * *";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isMailEnabled() {
        return mailEnabled;
    }

    public void setMailEnabled(boolean mailEnabled) {
        this.mailEnabled = mailEnabled;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public String getInAppTitle() {
        return inAppTitle;
    }

    public void setInAppTitle(String inAppTitle) {
        this.inAppTitle = inAppTitle;
    }

    public String getInAppMessage() {
        return inAppMessage;
    }

    public void setInAppMessage(String inAppMessage) {
        this.inAppMessage = inAppMessage;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }
}
