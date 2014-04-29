/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd.rm;

import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;

public class RemoveNodeSourceCommand extends AbstractCommand implements Command {
    private String nodeSource;
    private boolean preempt;

    public RemoveNodeSourceCommand(String nodeSource) {
        this(nodeSource, Boolean.toString(false));
    }

    public RemoveNodeSourceCommand(String nodeSource, String preempt) {
        this.nodeSource = nodeSource;
        this.preempt = Boolean.valueOf(preempt);
    }

    public void execute(ApplicationContext currentContext) throws CLIException {
        if (currentContext.isForced()) {
            preempt = true;
        }
        HttpPost request = new HttpPost(
                currentContext.getResourceUrl("nodesource/remove"));
        StringBuilder requestContent = new StringBuilder();
        requestContent.append("name=").append(nodeSource).append("&preempt=")
                .append(preempt);
        StringEntity entity = new StringEntity(requestContent.toString(),
                APPLICATION_FORM_URLENCODED);
        request.setEntity(entity);
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(response) == statusCode(OK)) {
            boolean success = readValue(response, Boolean.TYPE, currentContext);
            resultStack(currentContext).push(success);
            if (success) {
                writeLine(currentContext,
                        "Node source '%s' deleted successfully.", nodeSource);
            } else {
                writeLine(currentContext, "Cannot delete node source: %s.",
                        nodeSource);

            }
        } else {
            handleError(String.format(
                    "An error occurred while deleting node source: %s",
                    nodeSource), response, currentContext);
        }

    }

}