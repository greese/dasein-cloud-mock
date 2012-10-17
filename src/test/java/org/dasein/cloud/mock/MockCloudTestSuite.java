/**
 * ========= CONFIDENTIAL =========
 *
 * Copyright (C) 2012 enStratus Networks Inc - ALL RIGHTS RESERVED
 *
 * ====================================================================
 *  NOTICE: All information contained herein is, and remains the
 *  property of enStratus Networks Inc. The intellectual and technical
 *  concepts contained herein are proprietary to enStratus Networks Inc
 *  and may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law. Dissemination
 *  of this information or reproduction of this material is strictly
 *  forbidden unless prior written permission is obtained from
 *  enStratus Networks Inc.
 * ====================================================================
 */
package org.dasein.cloud.mock;

import com.google.inject.Guice;
import junit.framework.Test;
import org.dasein.cloud.test.ComprehensiveTestSuite;
import org.dasein.cloud.test.TestConfigurationException;

/**
 * Verifies that the mock cloud can pass the Dasein Cloud test suite.
 * <p>Created by George Reese: 8/23/12 7:56 PM</p>
 * @author George Reese
 * @version 2012.07
 * @since 2012.07
 */
public class MockCloudTestSuite  {
    static public Test suite() throws TestConfigurationException {
        Guice.createInjector(new MockCloudModule());

        return new ComprehensiveTestSuite(MockCloud.class);
    }
}
