package pt.upa.transporter;

import java.util.Timer;
import java.util.TimerTask;

import pt.upa.transporter.TransporterDomain;

/**
 * 
 */

public class TransporterUpdateReminder {
    Timer timer;
    String jobIdentifier;
    TransporterDomain domain;

    public TransporterUpdateReminder(int milliseconds, 
    		String jobIdentifier, TransporterDomain domain) {
        this.timer = new Timer();
        this.jobIdentifier = jobIdentifier;
        this.domain = domain;
        timer.schedule(new RemindTask(), milliseconds);
	}

    class RemindTask extends TimerTask {
        public void run() {
            domain.updateJobState(jobIdentifier);
            timer.cancel(); //Terminate the timer thread
        }
    }

}