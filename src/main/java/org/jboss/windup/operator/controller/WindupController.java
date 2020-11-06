package org.jboss.windup.operator.controller;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.windup.operator.model.WindupResource;
import org.jboss.windup.operator.model.WindupResourceDoneable;
import org.jboss.windup.operator.model.WindupResourceList;
import org.jboss.windup.operator.util.WindupDeployment;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Log
@ApplicationScoped
public class WindupController implements Watcher<WindupResource> {
	@Inject
	MixedOperation<WindupResource, WindupResourceList, WindupResourceDoneable, Resource<WindupResource, WindupResourceDoneable>> crClient;

	@ConfigProperty(name = "namespace", defaultValue = "mta")
	String namespace;

	@Inject
	KubernetesClient k8sClient;

	public void onAdd(WindupResource resource) {
		log.info("Event ADD " + resource.getMetadata().getName());
		if (!resource.isDeploying() && !resource.isReady()) {
			new WindupDeployment(resource, crClient, k8sClient).deploy();
		}
	}

	public void onUpdate(WindupResource newResource) {
		log.info("Event UPDATE " + newResource.getMetadata().getName() + " - DR "
				+ newResource.deploymentsReady() + " RD " + newResource.isReady());

		// Consolidate status of the CR
		if (newResource.deploymentsReady() == 3 && !newResource.isReady()) {
			newResource.setReady(true);
			newResource.getStatus().getOrAddConditionByType("Deploy").setStatus(Boolean.FALSE.toString());

			log.info("Setting this CR as Ready");
			crClient.inNamespace(namespace).updateStatus(newResource);
		}
	}

	public void onDelete(WindupResource resource) {
		log.info("Event DELETE [" + resource + "]");
	}

	@Override
	public void eventReceived(Action action, WindupResource resource) {
		log.info("Event received " + action + " received for WindupResource : " + resource.getMetadata().getName());

		if (action == Action.ADDED) onAdd(resource);
		if (action == Action.MODIFIED) onUpdate(resource);
		if (action == Action.DELETED) onDelete(resource);
	}

	@Override
	public void onClose(KubernetesClientException cause) {
		// TODO Auto-generated method stub

	}
}