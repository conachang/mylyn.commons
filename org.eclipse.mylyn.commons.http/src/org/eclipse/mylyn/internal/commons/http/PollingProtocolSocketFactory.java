/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.commons.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * @author Steffen Pingel
 */
public class PollingProtocolSocketFactory implements SchemeSocketFactory {

	private final static SocketFactory factory = SocketFactory.getDefault();

	public Socket createSocket(HttpParams params) throws IOException {
		return factory.createSocket();
	}

	public Socket connectSocket(Socket sock, InetSocketAddress remoteAddress, InetSocketAddress localAddress,
			HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {

		if (params == null) {
			throw new IllegalArgumentException("Parameters may not be null"); //$NON-NLS-1$
		}

		final Socket socket = sock != null ? sock : factory.createSocket();

		int connTimeout = HttpConnectionParams.getConnectionTimeout(params);

		socket.bind(localAddress);
		MonitoredRequest.connect(socket, remoteAddress, connTimeout);
		return socket;
	}

	public boolean isSecure(Socket sock) throws IllegalArgumentException {
		return false;
	}
}