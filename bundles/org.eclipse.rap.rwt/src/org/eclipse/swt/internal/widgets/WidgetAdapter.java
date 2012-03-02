/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rwt.Adaptable;
import org.eclipse.rwt.internal.lifecycle.DisposedWidgets;
import org.eclipse.rwt.internal.lifecycle.IRenderRunnable;
import org.eclipse.rwt.internal.protocol.ClientObjectAdapter;
import org.eclipse.rwt.internal.protocol.IClientObjectAdapter;
import org.eclipse.rwt.lifecycle.IWidgetAdapter;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Widget;


public final class WidgetAdapter
  implements IWidgetAdapter, IClientObjectAdapter, SerializableCompatibility
{

  private final String id;
  private boolean initialized;
  private transient Map<String,Object> preservedValues;
  private String jsParent;
  private transient IRenderRunnable renderRunnable;
  private transient String cachedVariant;
  private ClientObjectAdapter gcObjectAdapter;

  public WidgetAdapter() {
    this( IdGenerator.getInstance().newId( "w" ) );
  }

  public WidgetAdapter( String id ) {
    this.id = id;
    initialize();
  }

  private void initialize() {
    preservedValues = new HashMap<String,Object>();
  }

  public String getId() {
    return id;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public void setInitialized( boolean initialized ) {
    this.initialized = initialized;
  }

  public void preserve( String propertyName, Object value ) {
    preservedValues.put( propertyName, value );
  }

  public Object getPreserved( String propertyName ) {
    return preservedValues.get( propertyName );
  }

  public void clearPreserved() {
    preservedValues.clear();
  }

  public String getJSParent() {
    return jsParent;
  }

  public void setJSParent( String jsParent ) {
    this.jsParent = jsParent;
  }

  public void setRenderRunnable( IRenderRunnable renderRunnable ) {
    if( this.renderRunnable != null ) {
      throw new IllegalStateException( "A renderRunnable was already set." );
    }
    this.renderRunnable = renderRunnable;
  }

  public IRenderRunnable getRenderRunnable() {
    return renderRunnable;
  }

  public void clearRenderRunnable() {
    renderRunnable = null;
  }

  public String getCachedVariant() {
    return cachedVariant;
  }

  public void setCachedVariant( String cachedVariant ) {
    this.cachedVariant = cachedVariant;
  }

  public void markDisposed( Widget widget ) {
    if( initialized ) {
      DisposedWidgets.add( widget );
    }
  }

  public Adaptable getGCForClient() {
    if( gcObjectAdapter == null ) {
      gcObjectAdapter = new ClientObjectAdapter( "gc" );
    }
    return createGcAdaptable();
  }

  @SuppressWarnings("unchecked")
  private Adaptable createGcAdaptable() {
    return new Adaptable() {

      public <T> T getAdapter( Class<T> adapter ) {
        if( adapter == IClientObjectAdapter.class ) {
          return ( T )gcObjectAdapter;
        }
        return null;
      }
    };
  }

  private Object readResolve() {
    initialize();
    return this;
  }
}
