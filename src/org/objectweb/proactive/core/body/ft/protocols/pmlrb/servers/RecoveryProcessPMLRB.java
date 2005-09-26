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
package org.objectweb.proactive.core.body.ft.protocols.pmlrb.servers;

import java.rmi.RemoteException;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.protocols.pmlrb.managers.FTManagerPMLRB;
import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryJob;
import org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcessImpl;
import org.objectweb.proactive.core.node.Node;


/**
 * @author cdelbe
 * @since 2.2
 */
public class RecoveryProcessPMLRB extends RecoveryProcessImpl {

    /**
     * @param server
     */
    public RecoveryProcessPMLRB(FTServer server) {
        super(server);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcessImpl#recover(org.objectweb.proactive.core.UniqueID)
     */
    protected void recover(UniqueID failed) {
        try {
            Checkpoint toSend = this.server.getLastCheckpoint(failed);

            //look for a new Runtime for this oa
            Node node = this.server.getFreeNode();
            RecoveryJob job = new RecoveryJob(toSend,
                    FTManagerPMLRB.DEFAULT_TTC_VALUE, node);
            this.submitJob(job);
        } catch (RemoteException e) {
            logger.error("[RECOVERY] **ERROR** Cannot contact other servers : ");
            e.printStackTrace();
        }
    }
}
