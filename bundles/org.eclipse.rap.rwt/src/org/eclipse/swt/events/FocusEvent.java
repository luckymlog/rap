/*******************************************************************************
 * Copyright (c) 2007, 2011 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.events;

import org.eclipse.rap.rwt.Adaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;


/**
 * Instances of this class are sent as a result of widgets gaining and losing
 * focus.
 *
 * <p><strong>IMPORTANT:</strong> All <code>public static</code> members of
 * this class are <em>not</em> part of the RWT public API. They are marked
 * public only so that they can be shared within the packages provided by RWT.
 * They should never be accessed from application code.
 * </p>
 *
 * @see FocusListener
 */
public final class FocusEvent extends TypedEvent {

  private static final long serialVersionUID = 1L;

  private static final int[] EVENT_TYPES = { SWT.FocusIn, SWT.FocusOut };

  /**
   * Constructs a new instance of this class based on the
   * information in the given untyped event.
   *
   * @param event the untyped event containing the information
   */
  public FocusEvent( Event event ) {
    super( event );
  }

  /**
   * @since 2.0
   * @deprecated not part of the API, do not use in application code
   */
  @Deprecated
  public static void addListener( Adaptable adaptable, FocusListener listener ) {
    addListener( adaptable, EVENT_TYPES, listener );
  }

  /**
   * @since 2.0
   * @deprecated not part of the API, do not use in application code
   */
  @Deprecated
  public static void removeListener( Adaptable adaptable, FocusListener listener ) {
    removeListener( adaptable, EVENT_TYPES, listener );
  }

  /**
   * @since 2.0
   * @deprecated not part of the API, do not use in application code
   */
  @Deprecated
  public static boolean hasListener( Adaptable adaptable ) {
    return hasListener( adaptable, EVENT_TYPES );
  }

}
