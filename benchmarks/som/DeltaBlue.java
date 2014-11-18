package som;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import som.DeltaBlue.Constraint.BlockFunction.Return;
import som.DeltaBlue.Constraint.ConstraintBlockFunction;

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

  /*
   * Strengths are used to measure the relative importance of constraints. New
   * strengths may be inserted in the strength hierarchy without disrupting
   * current constraints. Strengths cannot be created outside this class, so
   * pointer comparison can be used for value comparison.
   */

  static class Strength {

    private int    arithmeticValue;
    private String symbolicValue;

    private Strength(final String symbolicValue) {
      this.symbolicValue = symbolicValue;
      this.arithmeticValue = strengthTable.get(symbolicValue);
    }

    public boolean sameAs(final Strength s) {
      return arithmeticValue == s.getArithmeticValue();
    }

    public boolean stronger(final Strength s) {
      return arithmeticValue < s.getArithmeticValue();
    }

    public boolean weaker(final Strength s) {
      return arithmeticValue > s.getArithmeticValue();
    }

    public Strength strongest(final Strength s) {
      return s.stronger(this) ? s : this;
    }

    public Strength weakest(final Strength s) {
      return s.weaker(this) ? s : this;
    }

    public int getArithmeticValue() {
      return arithmeticValue;
    }

    public void print() {
      System.out.print("strength[" + Integer.toString(arithmeticValue) + "]");
    }

    public HashMap<String, Integer> strengthTable() {
      return strengthTable;
    }

    public static Strength of(final String aSymbol) {
      return strengthConstant.get(aSymbol);
    }

    public static void initialize() {
      strengthTable = new HashMap<>();
      strengthTable.put("absoluteStrongest", -10000);
      strengthTable.put("required",          -800);
      strengthTable.put("strongPreferred",   -600);
      strengthTable.put("preferred",         -400);
      strengthTable.put("strongDefault",     -200);
      strengthTable.put("default",            0);
      strengthTable.put("weakDefault",        500);
      strengthTable.put("absoluteWeakest",    10000);

      strengthConstant = new HashMap<>();
      strengthTable.keySet().forEach(key ->
        strengthConstant.put(key, new Strength(key))
      );

      absoluteStrongest = of("absoluteStrongest");
      absoluteWeakest   = of("absoluteWeakest");
      required          = of("required");
    }

    public static Strength absoluteStrongest() {
      return absoluteStrongest;
    }

    public static Strength absoluteWeakest() {
      return absoluteWeakest;
    }

    public static Strength required() {
      return required;
    }

    private static Strength absoluteStrongest;
    private static Strength absoluteWeakest;
    private static Strength required;

    private static HashMap<String, Integer>  strengthTable;
    private static HashMap<String, Strength> strengthConstant;
  }

  // ------------------------------ variables ------------------------------

  // I represent a constrained variable. In addition to my value, I
  // maintain the structure of the constraint graph, the current
  // dataflow graph, and various parameters of interest to the DeltaBlue
  // incremental constraint solver.

  static class Variable {

    private int        value;       // my value; changed by constraints
    private Vector<Constraint> constraints; // normal constraints that reference me
    private Constraint determinedBy; // the constraint that currently determines
    // my value (or null if there isn't one)
    private int        mark;        // used by the planner to mark constraints
    private Strength   walkStrength; // my walkabout strength
    private boolean    stay;        // true if I am a planning-time constant

    public static Variable value(final int aValue) {
      Variable v = new Variable();
      v.setValue(aValue);
      return v;
    }

    public Variable() {
      value = 0;
      constraints = new Vector<>(2);
      determinedBy = null;
      walkStrength = Strength.absoluteWeakest();
      stay = true;
      mark = 0;
    }

    // Add the given constraint to the set of all constraints that refer to me.
    public void addConstraint(final Constraint c) {
      constraints.add(c);
    }

    public Vector<Constraint> getConstraints() {
      return constraints;
    }

    public Constraint getDeterminedBy() {
      return determinedBy;
    }

    public void setDeterminedBy(final Constraint c) {
      determinedBy = c;
    }

    public int getMark() {
      return mark;
    }

    public void setMark(final int markValue) {
      mark = markValue;
    }

    // Remove all traces of c from this variable.
    public void removeConstraint(final Constraint c) {
      constraints.remove(c);
      if (determinedBy == c) {
        determinedBy = null;
      }
    }

    public boolean getStay() {
      return stay;
    }

    public void setStay(final boolean v) {
      stay = v;
    }

    public int getValue() {
      return value;
    }

    public void setValue(final int value) {
      this.value = value;
    }

    public Strength getWalkStrength() {
      return walkStrength;
    }

    public void setWalkStrength(final Strength strength) {
      walkStrength = strength;
    }
  }

  // ------------------------ constraints ------------------------------------

  // I am an abstract class representing a system-maintainable
  // relationship (or "constraint") between a set of variables. I supply
  // a strength instance variable; concrete subclasses provide a means
  // of storing the constrained variables and other information required
  // to represent a constraint.

  abstract static class Constraint {

    protected Strength strength; // the strength of this constraint

    public Strength getStrength() {
      return strength;
    }

    public void setStrength(final String strength) {
      this.strength = Strength.of(strength);
    }

    // Normal constraints are not input constraints. An input constraint
    // is one that depends on external state, such as the mouse, the
    // keyboard, a clock, or some arbitrary piece of imperative code.
    public boolean isInput() {
      return false;
    }

    // Answer true if this constraint is satisfied in the current solution.
    public abstract boolean isSatisfied();

    // Activate this constraint and attempt to satisfy it.
    protected void addConstraint() {
      addToGraph();
      Planner.getCurrent().incrementalAdd(this);
    }

    // Add myself to the constraint graph.
    public abstract void addToGraph();

    // Deactivate this constraint, remove it from the constraint graph,
    // possibly causing other constraints to be satisfied, and destroy
    // it.
    public void destroyConstraint() {
      if (isSatisfied()) {
        Planner.getCurrent().incrementalRemove(this);
      }
      removeFromGraph();
    }

    // Remove myself from the constraint graph.
    public abstract void removeFromGraph();

    // Decide if I can be satisfied and record that decision. The output
    // of the choosen method must not have the given mark and must have
    // a walkabout strength less than that of this constraint.
    protected abstract String chooseMethod(int mark);

    // Enforce this constraint. Assume that it is satisfied.
    public abstract void execute();

    @FunctionalInterface
    public interface BlockFunction {
      class Return extends Exception {
        private static final long serialVersionUID = 5527046579317358033L;
        private final Object value;
        public Return(final Object value) {
          this.value = value;
        }
        public Object getValue() {
          return value;
        }
      }
      void apply(final Variable var) throws Return;
    }

    @FunctionalInterface
    public interface ConstraintBlockFunction {
      void apply(final Constraint c);
    }

    public abstract void inputsDo(BlockFunction block) throws Return;

    // Assume that I am satisfied. Answer true if all my current inputs
    // are known. A variable is known if either a) it is 'stay' (i.e. it
    // is a constant at plan execution time), b) it has the given mark
    // (indicating that it has been computed by a constraint appearing
    // earlier in the plan), or c) it is not determined by any
    // constraint.
    public boolean inputsKnown(final int mark) {
      try {
        inputsDo(v -> {
          if (!(v.getMark() == mark ||
              v.getStay() ||
              v.getDeterminedBy() == null)) { throw new Return(false); } });
      } catch (Return r) {
        return false;
      }
      return true;
    }

    // Record the fact that I am unsatisfied.
    public abstract void markUnsatisfied();

    // Answer my current output variable. Raise an error if I am not
    // currently satisfied.
    public abstract Variable output();

    // Calculate the walkabout strength, the stay flag, and, if it is
    // 'stay', the value for the current output of this
    // constraint. Assume this constraint is satisfied.
    public abstract void recalculate();

    // Attempt to find a way to enforce this constraint. If successful,
    // record the solution, perhaps modifying the current dataflow
    // graph. Answer the constraint that this constraint overrides, if
    // there is one, or nil, if there isn't.
    // Assume: I am not already satisfied.
    //
    public Constraint satisfy(final int mark) {
      Constraint overridden;

      chooseMethod(mark);

      if (isSatisfied()) {
        // constraint can be satisfied
        // mark inputs to allow cycle detection in addPropagate
        try {
          inputsDo(in -> in.setMark(mark));
        } catch (Return e) {/* Doesn't throw return... */}

        Variable out = output();
        overridden = out.getDeterminedBy();
        if (overridden != null) {
          overridden.markUnsatisfied();
        }
        out.setDeterminedBy(this);
        if (!Planner.getCurrent().addPropagate(this, mark)) {
          System.out.println("Cycle encountered");
          return null;
        }
        out.mark = mark;
      } else {
        overridden = null;
        if (strength.sameAs(Strength.required)) {
          throw new RuntimeException("Could not satisfy a required constraint");
        }
      }
      return overridden;
    }
  }

  // -------------unary constraints-------------------------------------------

  // I am an abstract superclass for constraints having a single
  // possible output variable.
  //
  abstract static class UnaryConstraint extends Constraint {

    protected Variable myOutput; // possible output variable
    protected boolean  satisfied; // true if I am currently satisfied

    public void set(final Variable v, final String strength) {
      this.strength = Strength.of(strength);
      myOutput = v;
      satisfied = false;
      addConstraint();
    }

    // Answer true if this constraint is satisfied in the current solution.
    @Override
    public boolean isSatisfied() {
      return satisfied;
    }

    // Add myself to the constraint graph.
    @Override
    public void addToGraph() {
      myOutput.addConstraint(this);
      satisfied = false;
    }

    // Remove myself from the constraint graph.
    @Override
    public void removeFromGraph() {
      if (myOutput != null) {
        myOutput.removeConstraint(this);
      }
      satisfied = false;
    }

    // Decide if I can be satisfied and record that decision.
    @Override
    protected String chooseMethod(final int mark) {
      satisfied = myOutput.getMark() != mark
          && strength.stronger(myOutput.getWalkStrength());
      return null;
    }

    @Override
    public abstract void execute();

    @Override
    public void inputsDo(final BlockFunction block) throws Return {
      // I have no input variables
    }

    // Record the fact that I am unsatisfied.
    @Override
    public void markUnsatisfied() {
      satisfied = false;
    }

    // Answer my current output variable.
    @Override
    public Variable output() {
      return myOutput;
    }

    // Calculate the walkabout strength, the stay flag, and, if it is
    // 'stay', the value for the current output of this
    // constraint. Assume this constraint is satisfied."
    @Override
    public void recalculate() {
      myOutput.setWalkStrength(strength);
      myOutput.setStay(!isInput());
      if (myOutput.getStay()) {
        execute(); // stay optimization
      }
    }
  }

  // I am a unary input constraint used to mark a variable that the
  // client wishes to change.
  static class EditConstraint extends UnaryConstraint {

    public static EditConstraint var(final Variable v, final String strength) {
      EditConstraint c = new EditConstraint();
      c.set(v, strength);
      return c;
    }

    // I indicate that a variable is to be changed by imperative code.
    @Override
    public boolean isInput() {
      return true;
    }

    @Override
    public void execute() {} // Edit constraints do nothing.
  }

  // I mark variables that should, with some level of preference, stay
  // the same. I have one method with zero inputs and one output, which
  // does nothing. Planners may exploit the fact that, if I am
  // satisfied, my output will not change during plan execution. This is
  // called "stay optimization".
  //
  static class StayConstraint extends UnaryConstraint {

    // Install a stay constraint with the given strength on the given variable.
    public static StayConstraint var(final Variable v, final String strength) {
      StayConstraint c = new StayConstraint();
      c.set(v, strength);
      return c;
    }

    @Override
    public void execute() {} // Stay constraints do nothing.
  }

  // -------------binary constraints-------------------------------------------

  // I am an abstract superclass for constraints having two possible
  // output variables.
  //
  abstract static class BinaryConstraint extends Constraint {

    protected Variable v1, v2;          // possible output variables
    protected String   direction;       // one of the following...

    public void set(final Variable var1, final Variable var2, final String strength) {
      this.strength = Strength.of(strength);
      v1 = var1;
      v2 = var2;
      direction = null;
      addConstraint();
    }

    // Answer true if this constraint is satisfied in the current solution.
    @Override
    public boolean isSatisfied() {
      return direction != null;
    }

    // Add myself to the constraint graph.
    @Override
    public void addToGraph() {
      v1.addConstraint(this);
      v2.addConstraint(this);
      direction = null;
    }

    // Remove myself from the constraint graph.
    @Override
    public void removeFromGraph() {
      if (v1 != null) {
        v1.removeConstraint(this);
      }
      if (v2 != null) {
        v2.removeConstraint(this);
      }
      direction = null;
    }

    // Decide if I can be satisfied and which way I should flow based on
    // the relative strength of the variables I relate, and record that
    // decision.
    //
    @Override
    protected String chooseMethod(final int mark) {
      if (v1.getMark() == mark) {
        if (v2.getMark() != mark && strength.stronger(v2.getWalkStrength())) {
          direction = "forward";
          return direction;
        } else {
          direction = null;
          return direction;
        }
      }

      if (v2.mark == mark) {
        if (v1.getMark() != mark && strength.stronger(v1.getWalkStrength())) {
          direction = "backward";
          return direction;
        } else {
          direction = null;
          return direction;
        }
      }

      // If we get here, neither variable is marked, so we have a choice.
      if (v1.getWalkStrength().weaker(v2.getWalkStrength())) {
        if (strength.stronger(v1.getWalkStrength())) {
          direction = "backward";
          return direction;
        } else {
          direction = null;
          return direction;
        }
      } else {
        if (strength.stronger(v2.getWalkStrength())) {
          direction = "forward";
          return direction;
        } else {
          direction = null;
          return direction;
        }
      }
    }

    @Override
    public void inputsDo(final BlockFunction block) throws Return {
      if (direction == "forward") {
        block.apply(v1);
      } else {
        block.apply(v2);
      }
    }

    // Record the fact that I am unsatisfied.
    @Override
    public void markUnsatisfied() {
      direction = null;
    }


    // Answer my current output variable.
    @Override
    public Variable output() {
      return direction == "forward" ? v2 : v1;
    }

    // Calculate the walkabout strength, the stay flag, and, if it is
    // 'stay', the value for the current output of this
    // constraint. Assume this constraint is satisfied.
    //
    @Override
    public void recalculate() {
      Variable in, out;

      if (direction == "forward") {
        in = v1; out = v2;
      } else {
        in = v2; out = v1;
      }

      out.setWalkStrength(strength.weakest(in.getWalkStrength()));
      out.setStay(in.getStay());
      if (out.getStay()) {
        execute();
      }
    }
  }

  // I constrain two variables to have the same value: "v1 = v2".
  static class EqualityConstraint extends BinaryConstraint {

    // Install a constraint with the given strength equating the given
    // variables.
    public static EqualityConstraint var(final Variable var1, final Variable var2,
        final String strength) {
      EqualityConstraint c = new EqualityConstraint();
      c.set(var1, var2, strength);
      return c;
    }

    // Enforce this constraint. Assume that it is satisfied.
    @Override
    public void execute() {
      if (direction == "forward") {
        v2.setValue(v1.getValue());
      } else {
        v1.setValue(v2.getValue());
      }
    }
  }

  // I relate two variables by the linear scaling relationship: "v2 =
  // (v1 * scale) + offset". Either v1 or v2 may be changed to maintain
  // this relationship but the scale factor and offset are considered
  // read-only.
  //
  static class ScaleConstraint extends BinaryConstraint {

    protected Variable scale; // scale factor input variable
    protected Variable offset; // offset input variable

    // Install a scale constraint with the given strength on the given
    // variables.
    public void set(final Variable src, final Variable scale,
        final Variable offset, final Variable dest, final String strength) {
      this.strength = Strength.of(strength);
      v1 = src;
      v2 = dest;
      this.scale = scale;
      this.offset = offset;
      direction = null;
      addConstraint();
    }

    // Add myself to the constraint graph.
    @Override
    public void addToGraph() {
      v1.addConstraint(this);
      v2.addConstraint(this);
      scale.addConstraint(this);
      offset.addConstraint(this);
      direction = null;
    }

    // Remove myself from the constraint graph.
    @Override
    public void removeFromGraph() {
      if (v1 != null) { v1.removeConstraint(this); }
      if (v2 != null) { v2.removeConstraint(this); }
      if (scale  != null) { scale.removeConstraint(this); }
      if (offset != null) { offset.removeConstraint(this); }
      direction = null;
    }

    // Enforce this constraint. Assume that it is satisfied.
    @Override
    public void execute() {
      if (direction == "forward") {
        v2.setValue(v1.getValue() * scale.getValue() + offset.getValue());
      } else {
        v1.setValue((v2.getValue() - offset.getValue()) / scale.getValue());
      }
    }

    @Override
    public void inputsDo(final BlockFunction block) throws Return {
      if (direction == "forward") {
        block.apply(v1);
        block.apply(scale);
        block.apply(offset);
      } else {
        block.apply(v2);
        block.apply(scale);
        block.apply(offset);
      }
    }

    // Calculate the walkabout strength, the stay flag, and, if it is
    // 'stay', the value for the current output of this
    // constraint. Assume this constraint is satisfied.
    @Override
    public void recalculate() {
      Variable in, out;

      if (direction == "forward") {
        in  = v1; out = v2;
      } else {
        out = v1; in  = v2;
      }

      out.setWalkStrength(strength.weakest(in.walkStrength));
      out.setStay(in.getStay() && scale.getStay() && offset.getStay());
      if (out.getStay()) {
        execute(); // stay optimization
      }
    }

    public static ScaleConstraint var(final Variable src, final Variable scale,
        final Variable offset, final Variable dest, final String strength) {
      ScaleConstraint c = new ScaleConstraint();
      c.set(src, scale, offset, dest, strength);
      return c;
    }
  }

  // ------------------------------------------------------------

  // A Plan is an ordered list of constraints to be executed in sequence
  // to resatisfy all currently satisfiable constraints in the face of
  // one or more changing inputs.

  static class Plan extends Vector<Constraint> {
    private static final long serialVersionUID = -5753541792336307202L;

    public void execute() {
      this.forEach(c -> c.execute());
    }
  }

  // ------------------------------------------------------------

  // The DeltaBlue planner

  static class Planner {
    private int currentMark;

    public void initialize() {
      currentMark = 0;
    }

    // Attempt to satisfy the given constraint and, if successful,
    // incrementally update the dataflow graph. Details: If satifying
    // the constraint is successful, it may override a weaker constraint
    // on its output. The algorithm attempts to resatisfy that
    // constraint using some other method. This process is repeated
    // until either a) it reaches a variable that was not previously
    // determined by any constraint or b) it reaches a constraint that
    // is too weak to be satisfied using any of its methods. The
    // variables of constraints that have been processed are marked with
    // a unique mark value so that we know where we've been. This allows
    // the algorithm to avoid getting into an infinite loop even if the
    // constraint graph has an inadvertent cycle.
    //
    public void incrementalAdd(final Constraint c) {
      int mark = newMark();
      Constraint overridden = c.satisfy(mark);
      while (overridden != null) {
        overridden = overridden.satisfy(mark);
      }
    }

    // Entry point for retracting a constraint. Remove the given
    // constraint and incrementally update the dataflow graph.
    // Details: Retracting the given constraint may allow some currently
    // unsatisfiable downstream constraint to be satisfied. We therefore collect
    // a list of unsatisfied downstream constraints and attempt to
    // satisfy each one in turn. This list is traversed by constraint
    // strength, strongest first, as a heuristic for avoiding
    // unnecessarily adding and then overriding weak constraints.
    // Assume: c is satisfied.
    //
    public void incrementalRemove(final Constraint c) {
      Variable out = c.output();
      c.markUnsatisfied();
      c.removeFromGraph();

      Vector<Constraint> unsatisfied = removePropagateFrom(out);
      unsatisfied.forEach(u -> incrementalAdd(u));
    }

    // Extract a plan for resatisfaction starting from the outputs of
    // the given constraints, usually a set of input constraints.
    //
    protected Plan extractPlanFromConstraints(final Vector<Constraint> constraints) {
      Vector<Constraint> sources = new Vector<>();

      constraints.forEach(c -> {
        if (c.isInput() && c.isSatisfied()) {
          sources.add(c);
        }});

      return makePlan(sources);
    }

    // Extract a plan for resatisfaction starting from the given source
    // constraints, usually a set of input constraints. This method
    // assumes that stay optimization is desired; the plan will contain
    // only constraints whose output variables are not stay. Constraints
    // that do no computation, such as stay and edit constraints, are
    // not included in the plan.
    // Details: The outputs of a constraint are marked when it is added
    // to the plan under construction. A constraint may be appended to
    // the plan when all its input variables are known. A variable is
    // known if either a) the variable is marked (indicating that has
    // been computed by a constraint appearing earlier in the plan), b)
    // the variable is 'stay' (i.e. it is a constant at plan execution
    // time), or c) the variable is not determined by any
    // constraint. The last provision is for past states of history
    // variables, which are not stay but which are also not computed by
    // any constraint.
    // Assume: sources are all satisfied.
    //
    protected Plan makePlan(final Vector<Constraint> sources) {
      int mark = newMark();
      Plan plan = new Plan();
      Vector<Constraint> todo = sources;

      while (!todo.isEmpty()) {
        Constraint c = todo.remove(0);

        if (c.output().getMark() != mark && c.inputsKnown(mark)) {
          // not in plan already and eligible for inclusion
          plan.add(c);
          c.output().setMark(mark);
          addConstraintsConsumingTo(c.output(), todo);
        }
      }
      return plan;
    }

    // The given variable has changed. Propagate new values downstream.
    public void propagateFrom(final Variable v) {
      Vector<Constraint> todo = new Vector<>();
      addConstraintsConsumingTo(v, todo);
      while (!todo.isEmpty()) {
        Constraint c = todo.remove(0);
        c.execute();
        addConstraintsConsumingTo(c.output(), todo);
      }
    }

    protected void addConstraintsConsumingTo(final Variable v, final Vector<Constraint> coll) {
      Constraint determiningC = v.getDeterminedBy();
      v.getConstraints().forEach(c -> {
        if (c != determiningC && c.isSatisfied()) {
          coll.add(c);
        }
      });
    }

    // Recompute the walkabout strengths and stay flags of all variables
    // downstream of the given constraint and recompute the actual
    // values of all variables whose stay flag is true. If a cycle is
    // detected, remove the given constraint and answer
    // false. Otherwise, answer true.
    // Details: Cycles are detected when a marked variable is
    // encountered downstream of the given constraint. The sender is
    // assumed to have marked the inputs of the given constraint with
    // the given mark. Thus, encountering a marked node downstream of
    // the output constraint means that there is a path from the
    // constraint's output to one of its inputs.
    //
    public boolean addPropagate(final Constraint c, final int mark) {
      Vector<Constraint> todo = new Vector<>();
      todo.add(c);
      while (!todo.isEmpty()) {
        Constraint d = todo.remove(0);

        if (d.output().getMark() == mark) {
          incrementalRemove(c);
          return false;
        }
        d.recalculate();
        addConstraintsConsumingTo(d.output(), todo);
      }
      return true;
    }

    private void change(final Variable var, final int newValue) {
      EditConstraint editC = EditConstraint.var(var, "preferred");

      Vector<Constraint> editV = new Vector<>();
      editV.add(editC);
      Plan plan = extractPlanFromConstraints(editV);
      for (int i = 0; i < 10; i++) {
        var.setValue(newValue);
        plan.execute();
      }
      editC.destroyConstraint();
    }

    private void constraintsConsuming(final Variable v,
        final ConstraintBlockFunction block) {
      Constraint determiningC = v.getDeterminedBy();
      v.getConstraints().forEach(c -> {
        if (c != determiningC && c.isSatisfied()) {
          block.apply(c);
        }
      });
    }

    // Select a previously unused mark value.
    private int newMark() {
      return ++currentMark;
    }

    // Update the walkabout strengths and stay flags of all variables
    // downstream of the given constraint. Answer a collection of
    // unsatisfied constraints sorted in order of decreasing strength.
    protected Vector<Constraint> removePropagateFrom(final Variable out) {
      Vector<Constraint> unsatisfied = new Vector<>();

      out.setDeterminedBy(null);
      out.setWalkStrength(Strength.absoluteWeakest);
      out.setStay(true);

      Vector<Variable> todo = new Vector<>();
      todo.add(out);

      while (!todo.isEmpty()) {
        Variable v = todo.remove(0);

        v.getConstraints().forEach(c -> {
          if (!c.isSatisfied()) { unsatisfied.add(c); }});

        constraintsConsuming(v, c -> {
          c.recalculate();
          todo.add(c.output());
        });
      }

      unsatisfied.sort((c1, c2) ->
        c1.getStrength().stronger(c2.getStrength()) ?
            -1 : c1.getStrength().sameAs(c2.getStrength()) ? 0 : 1);
      return unsatisfied;
    }


    public static void error(final String s) {
      System.err.println(s);
      System.exit(1);
    }

    private static Planner currentPlanner;

    public Planner() {
      initialize();
      currentPlanner = this;
    }

    // This is the standard DeltaBlue benchmark. A long chain of
    // equality constraints is constructed with a stay constraint on
    // one end. An edit constraint is then added to the opposite end
    // and the time is measured for adding and removing this
    // constraint, and extracting and executing a constraint
    // satisfaction plan. There are two cases. In case 1, the added
    // constraint is stronger than the stay constraint and values must
    // propagate down the entire length of the chain. In case 2, the
    // added constraint is weaker than the stay constraint so it cannot
    // be accomodated. The cost in this case is, of course, very
    // low. Typical situations lie somewhere between these two
    // extremes.
    //
    public static void chainTest(final int n) {
      Planner planner = new Planner();
      Variable[] vars = new Variable[n+1];
      Arrays.setAll(vars, i -> new Variable());

      // Build chain of n equality constraints
      for (int i = 0; i < n; i++) {
        Variable v1 = vars[i];
        Variable v2 = vars[i + 1];
        EqualityConstraint.var(v1, v2, "required");
      }

      StayConstraint.var(vars[n], "strongDefault");
      Constraint editC = EditConstraint.var(vars[0], "preferred");

      Vector<Constraint> editV = new Vector<>();
      editV.addElement(editC);
      Plan plan = planner.extractPlanFromConstraints(editV);
      for (int i = 0; i < 100; i++) {
        vars[0].setValue(i);
        plan.execute();
        if (vars[n].getValue() != i) {
          error("Chain test failed!");
        }
      }
      editC.destroyConstraint();
    }

    // This test constructs a two sets of variables related to each
    // other by a simple linear transformation (scale and offset). The
    // time is measured to change a variable on either side of the
    // mapping and to change the scale and offset factors.
    //
    public static void projectionTest(final int n) {
      Planner planner = new Planner();

      Vector<Variable> dests = new Vector<>();

      Variable scale  = Variable.value(10);
      Variable offset = Variable.value(1000);

      Variable src = null, dst = null;
      for (int i = 0; i < n; ++i) {
        src = Variable.value(i);
        dst = Variable.value(i);
        dests.add(dst);
        StayConstraint.var(src, "default");
        ScaleConstraint.var(src, scale, offset, dst, "required");
      }

      planner.change(src, 17);
      if (dst.getValue() != 1170) {
        error("Projection test 1 failed!");
      }

      planner.change(dst, 1050);
      if (src.value != 5) {
        error("Projection test 2 failed!");
      }

      planner.change(scale, 5);
      for (int i = 0; i < n - 1; ++i) {
        if (dests.elementAt(i).getValue() != i * 5 + 1000) {
          error("Projection test 3 failed!");
        }
      }

      planner.change(offset, 2000);
      for (int i = 0; i < n - 1; ++i) {
        if (dests.elementAt(i).value != i * 5 + 2000) {
          error("Projection test 4 failed!");
        }
      }
    }

    public static Planner getCurrent() {
      return currentPlanner;
    }
  }

  // ------------------------------------------------------------

}
