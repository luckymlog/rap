/*******************************************************************************
 * Copyright (c) 2011, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.remote;

import java.util.Map;

import org.eclipse.rap.rwt.internal.remote.RemoteObjectFactory;


/**
 * Instances of this interface are used to transfer changes to the remote object. To create a remote
 * object, use {@link RemoteObjectFactory#createRemoteObject(String)}.
 *
 * @see RemoteObjectFactory
 * @since 2.0
 */
public interface RemoteObject {

  /**
   * Sets the specified property of the remote object to the given value.
   *
   * @param name the name of the property to set
   * @param value the value to set
   */
  void set( String name, int value );

  /**
   * Sets the specified property of the remote object to the given value.
   *
   * @param name the name of the property to set
   * @param value the value to set
   */
  void set( String name, double value );

  /**
   * Sets the specified property of the remote object to the given value.
   *
   * @param name the name of the property to set
   * @param value the value to set
   */
  void set( String name, boolean value );

  /**
   * Sets the specified property of the remote object to the given value.
   *
   * @param name the name of the property to set
   * @param value the value to set
   */
  void set( String name, String value );

  /**
   * Sets the specified property of the remote object to the given value.
   *
   * @param name the name of the property to set
   * @param value the value to set
   */
  void set( String name, Object value );

  /**
   * Instructs the remote object to listen or to stop listening on the given type
   * of events. When the client is listening, it must notify the server when an
   * event of the given type occurs.
   *
   * @param eventType the name of event type to listen
   * @param listen true to listen to this type of events, false otherwise
   */
  void listen( String eventType, boolean listen );

  /**
   * Calls the method with the given name on the remote object.
   *
   * @param method the name of the method to call
   * @param properties the named properties to pass as arguments to the method call
   */
  void call( String method, Map<String, Object> properties );

  /**
   * Instructs the client to destroy the remote object.
   */
  void destroy();

  /**
   * Set a handler to process incoming operations from the remote object.
   *
   * @param the handler that processes incoming operation
   */
  void setHandler( OperationHandler remoteOperationHandler );

}
