/*******************************************************************************
 * Copyright (c) 2010, 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.commons.repositories.core;

import java.net.Proxy;

import javax.net.ssl.X509TrustManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.repositories.core.auth.AuthenticationCredentials;
import org.eclipse.mylyn.commons.repositories.core.auth.AuthenticationRequest;
import org.eclipse.mylyn.commons.repositories.core.auth.AuthenticationType;
import org.eclipse.mylyn.commons.repositories.core.auth.ICredentialsStore;

/**
 * Provides platform services related that are used by {@link RepositoryLocation} objects.
 * 
 * @author Steffen Pingel
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ILocationService {

	// FIXME e3.5 replace with 3.5 proxy API
	public abstract Proxy getProxyForHost(String host, String proxyType);

	public abstract X509TrustManager getTrustManager();

	public abstract <T extends AuthenticationCredentials> T requestCredentials(
			AuthenticationRequest<AuthenticationType<T>> context, IProgressMonitor monitor);

	public ICredentialsStore getCredentialsStore(String id);

}
