/*******************************************************************************
 * Copyright (c) 2006, 2014 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.common.re.activities;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.polarsys.capella.common.re.handlers.filter.DefaultFilterItem;
import org.polarsys.capella.common.re.handlers.filter.FilterFromReItem;
import org.polarsys.capella.core.transition.common.handlers.filter.CompoundFilteringItems;
import org.polarsys.kitalpha.cadence.core.api.parameter.ActivityParameters;
import org.polarsys.kitalpha.transposer.api.ITransposerWorkflow;
import org.polarsys.kitalpha.transposer.rules.handler.rules.api.IContext;

/**
 * 
 */
public class DifferencesFilteringActivity extends org.polarsys.capella.core.transition.common.activities.DifferencesFilteringActivity implements
    ITransposerWorkflow {

  public static final String ID = DifferencesFilteringActivity.class.getCanonicalName();

  @Override
  protected IStatus initializeFilterItemHandlers(IContext context, CompoundFilteringItems handler, ActivityParameters activityParams) {
    handler.addFilterItem(new DefaultFilterItem(), context);

    handler.addFilterItem(new FilterFromReItem(), context);

//    handler.addFilterItem(new AvoidUnsynchronizedFeatureItem(), context);
    return Status.OK_STATUS;
  }

  @Override
  @SuppressWarnings("unchecked")
  public IStatus _run(ActivityParameters activityParams) {
    return super._run(activityParams);
  }

}
