/*******************************************************************************
 * Copyright (c) 2006, 2015 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.core.data.helpers.information.services;

import org.polarsys.capella.core.data.information.ElementKind;
import org.polarsys.capella.core.data.information.ExchangeItemElement;
import org.polarsys.capella.core.data.information.ExchangeMechanism;
import org.polarsys.capella.core.data.information.ParameterDirection;

/**
 */
public class ExchangeItemElementExt {

  /**
   * @param element
   */
  public static void changeExchangeItemElementKind(ExchangeItemElement element, ExchangeMechanism mechanism) {
    if (mechanism == ExchangeMechanism.OPERATION) {
      if (element.getKind() != ElementKind.MEMBER) {
        element.setKind(ElementKind.MEMBER);
        element.setDirection(ParameterDirection.IN);
      }
    } else {
      if (element.getKind() != ElementKind.TYPE) {
        element.setKind(ElementKind.TYPE);
        element.setDirection(ParameterDirection.UNSET);
      }
      if (!element.getDirection().equals(ParameterDirection.UNSET)) {
        element.setDirection(ParameterDirection.UNSET);
      }
    }
  }

  /**
   * @param element
   */
  public static void changeExchangeItemElementDirection(ExchangeItemElement element, ExchangeMechanism mechanism) {
    if (element.getKind() == ElementKind.TYPE) {
      element.setDirection(ParameterDirection.UNSET);
    } 
    if (element.getKind() == ElementKind.MEMBER && mechanism == ExchangeMechanism.OPERATION) {
        element.setDirection(ParameterDirection.IN);
      } 
  }

}
