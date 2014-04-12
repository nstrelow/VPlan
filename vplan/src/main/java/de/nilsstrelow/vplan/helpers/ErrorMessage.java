package de.nilsstrelow.vplan.helpers;

/**
 * ErrorMessage class, for simpler Error sending to handler
 * Created by djnilse on 24.02.14.
 */
public class ErrorMessage {

    private boolean showLinkToPlan;
    private String errorMessage;
    private String errorTitle;

    public ErrorMessage(boolean showLinkToPlan, String errorTitle, String errorMessage) {
        this.showLinkToPlan = showLinkToPlan;
        this.errorMessage = errorMessage;
        this.errorTitle = errorTitle;
    }

    public boolean showLinkToPlan() {
        return showLinkToPlan;
    }

    public void setShowLinkToPlan(boolean showLinkToPlan) {
        this.showLinkToPlan = showLinkToPlan;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public void setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
    }
}
