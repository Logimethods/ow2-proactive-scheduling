/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.body.ft.servers.faultdetection;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.exception.NotImplementedException;
import org.objectweb.proactive.core.body.ft.internalmsg.Heartbeat;
import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.body.ft.servers.util.ActiveQueueJob;


/**
 * An implementation of the FaultDetector
 * @author cdelbe
 */
public class FaultDetectorImpl implements FaultDetector {
    //logger
    protected static Logger logger = Logger.getLogger(FaultDetector.class.getName());

    // global server
    private FTServer server;

    // detection thread
    private FaultDetectorThread fdt;

    // static heartbeat message
    private static final Heartbeat hbe = new Heartbeat();

    // detection period
    private long faultDetectionPeriod;

    /**
     *
     */
    public FaultDetectorImpl(FTServer server, long faultDetectPeriod) {
        this.faultDetectionPeriod = faultDetectPeriod;
        this.server = server;
        this.fdt = new FaultDetectorThread();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector#isUnreachable(org.objectweb.proactive.core.body.UniversalBody)
     */
    public boolean isUnreachable(UniversalBody body) throws RemoteException {
        Object res = null;
        try {
            res = body.receiveFTMessage(FaultDetectorImpl.hbe);
        } catch (IOException e) {
            // object is unreachable
            return true;
        }
        if (res.equals(FaultDetector.OK)) {
            // object is OK
            return false;
        } else {
            // object is dead
            return true;
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector#startFailureDetector(org.objectweb.proactive.core.body.ft.servers.location.LocationServer, org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess)
     */
    public void startFailureDetector() throws RemoteException {
        this.fdt.start();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector#suspendFailureDetector()
     */
    public void suspendFailureDetector() throws RemoteException {
        throw new NotImplementedException();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector#stopFailureDetector()
     */
    public void stopFailureDetector() throws RemoteException {
        throw new NotImplementedException();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector#forceDetection()
     */
    public void forceDetection() throws RemoteException {
        this.fdt.wakeUp();
    }

    /*
     * Thread for fault detection. One unique thread scans all active objects
     * @author cdelbe
     */
    private class FaultDetectorThread extends Thread {
        public FaultDetectorThread() {
            this.setName("FaultDetectorThread");
        }

        public synchronized void wakeUp() {
            notifyAll();
        }

        public synchronized void pause() {
            try {
                this.wait(FaultDetectorImpl.this.faultDetectionPeriod);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (true) {
                try {
                    //synchronized (FaultDetectorImpl.this.server){
                    ArrayList al = FaultDetectorImpl.this.server.getAllLocations();
                    Iterator it = al.iterator();
                    logger.info("[FAULT DETECTOR] Scanning " + al.size() +
                        " objects ...");
                    while (it.hasNext()) {
                        UniversalBody current = (UniversalBody) (it.next());

                        //((RemoteServer)current)
                        if (FaultDetectorImpl.this.server.isUnreachable(current)) {
                            FaultDetectorImpl.this.server.failureDetected(current.getID());
                            // other failures may be detected by the recoveryProcess
                            break;
                        }
                    }
                    logger.info("[FAULT DETECTOR] End scanning.");
                    //}
                    this.pause();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //	private class HeartbeatSenderJob implements ActiveQueueJob {
    //
    //	    private FTServer server;
    //	    private UniversalBody toTest;
    //	    
    //	    public HeartbeatSenderJob (FTServer server, UniversalBody toTest){
    //	        this.server = server;
    //	        this.toTest = toTest;
    //	    }
    //	    
    //        /* (non-Javadoc)
    //         * @see org.objectweb.proactive.core.body.ft.servers.util.ActiveQueueJob#doTheJob()
    //         */
    //        public void doTheJob() {
    //            // TODO Auto-generated method stub
    //            
    //        }
    //	    
    //	}
    //    
}
