package pt.upa.broker;

import java.util.Timer;
import java.util.TimerTask;

import pt.upa.broker.BrokerDomain;

/**
 * Class that after a given time (milliseconds),
 * notifies the secondary server to ping request
 * the primary
 */

public class BrokerPingReminder {
    Timer timer;
    BrokerDomain domain;

    public BrokerPingReminder(int milliseconds, BrokerDomain domain) {
        this.timer = new Timer();
        this.domain = domain;
        timer.schedule(new RemindTask(), milliseconds);
	}

    class RemindTask extends TimerTask {
        public void run() {
            domain.pingPrimary();
            timer.cancel(); //Terminate the timer thread
        }
    }

}