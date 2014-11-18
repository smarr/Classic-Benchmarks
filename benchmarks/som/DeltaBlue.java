package som;

import som.deltablue.Planner;
import som.deltablue.Strength;

/*
 * CL_SUN_COPYRIGHT_JVM_BEGIN
 *   If you or the company you represent has a separate agreement with both
 *   CableLabs and Sun concerning the use of this code, your rights and
 *   obligations with respect to this code shall be as set forth therein. No
 *   license is granted hereunder for any other purpose.
 * CL_SUN_COPYRIGHT_JVM_END
 */

/*
 * @(#)DeltaBlue.java	1.6 06/10/10
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 *
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 *
 */
/*

 This is a Java implemention of the DeltaBlue algorithm described in:
 "The DeltaBlue Algorithm: An Incremental Constraint Hierarchy Solver"
 by Bjorn N. Freeman-Benson and John Maloney
 January 1990 Communications of the ACM,
 also available as University of Washington TR 89-08-06.

 This implementation by Mario Wolczko, Sun Microsystems, Sep 1996,
 based on the Smalltalk implementation by John Maloney.

 */

public class DeltaBlue extends Benchmark {

  public static void main(final String[] args) {
    (new DeltaBlue()).run(args);
  }

  @Override
  public Object innerBenchmarkLoop() {
    Strength.initialize();
    Planner.chainTest(innerIterations);
    Planner.projectionTest(innerIterations);
    return null;
  }

  @Override
  public Object benchmark() {
    throw new RuntimeException("Should not be reachable.");
  }
}
