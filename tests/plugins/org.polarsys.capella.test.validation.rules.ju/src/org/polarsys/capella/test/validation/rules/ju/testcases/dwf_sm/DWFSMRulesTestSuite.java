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
package org.polarsys.capella.test.validation.rules.ju.testcases.dwf_sm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import org.polarsys.capella.test.framework.api.BasicTestArtefact;
import org.polarsys.capella.test.framework.api.BasicTestSuite;

public class DWFSMRulesTestSuite extends BasicTestSuite {

  /**
   * Returns the suite. This is required to unary launch this test.
   */
  public static Test suite() {
    return new DWFSMRulesTestSuite();
  }

  /**
   * @see org.polarsys.capella.test.framework.api.BasicTestSuite#getTests()
   */
  protected List<BasicTestArtefact> getTests() {
    List<BasicTestArtefact> tests = new ArrayList<BasicTestArtefact>();
    tests.add(new Rule_DWF_SM_01());
    tests.add(new Rule_DWF_SM_02());
    tests.add(new Rule_DWF_SM_03());
    tests.add(new Rule_DWF_SM_04());
    tests.add(new Rule_DWF_SM_05());
    tests.add(new Rule_DWF_SM_06());
    tests.add(new Rule_DWF_SM_07());
    tests.add(new Rule_DWF_SM_08());
    tests.add(new Rule_DWF_SM_08bis());
    tests.add(new Rule_DWF_SM_09());
    tests.add(new Rule_DWF_SM_09bis());
    tests.add(new Rule_DWF_SM_10());
    tests.add(new Rule_DWF_SM_11());
    tests.add(new Rule_DWF_SM_12());
    tests.add(new Rule_DWF_SM_13());
    tests.add(new Rule_DWF_SM_14());
    tests.add(new Rule_DWF_SM_15());
    return tests;
  }

  @Override
  public List<String> getRequiredTestModels() {
    return Arrays.asList(new String[] { "RulesOnDesignTest", "Project_validation_12" }); //$NON-NLS-1$
  }
}
