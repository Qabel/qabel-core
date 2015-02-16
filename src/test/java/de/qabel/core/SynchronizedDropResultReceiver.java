package de.qabel.core;

import de.qabel.ackack.event.EventActor;
import de.qabel.core.drop.DropResult;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by tox on 2/9/15.
 */
public class SynchronizedDropResultReceiver extends EventActor {
    private final LinkedBlockingQueue<DropResult> resultsList = new LinkedBlockingQueue<>();

    public LinkedBlockingQueue<DropResult> getResultsList() {
        return resultsList;
    }
}
