/*******************************************************************************
 * Copyright (c) 2011 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 ******************************************************************************/
package org.eclipse.rwt.application;

/**
 * <strong>Note:</strong> This API is <em>provisional</em>. It is likely to change before the final
 * release.
 *
 * @since 1.5
 */
public interface ApplicationConfigurator {
  
  public static final String CONFIGURATOR_PARAM = "org.eclipse.rwt.Configurator";

  void configure( ApplicationConfiguration configuration );
}