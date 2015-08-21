/* *********************************************************************** *
 * project: org.matsim.*
 * BestPlanSelector.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.core.replanning.selectors;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.HasPlansAndId;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.testcases.MatsimTestCase;

/**
 * An abstract TestCase to test basic requirements every implementation of {@link PlanSelector}
 * should fulfull. Every inherited class must override the method <code>getPlanSelector()</code>
 * to provide instances of the specific PlanSelector implementations to test and can define
 * additional tests to ensure the intended behavior of the specific PlanSelector.
 *
 * @author mrieser
 */
public abstract class AbstractPlanSelectorTest extends MatsimTestCase {

	/**
	 * Test how a plan selector reacts when one or more (or even all plans) have an undefined score.
	 * This test only ensures that in all cases a plan is returned, but doesn't distinguish which one.
	 * Currently, the {@link org.matsim.core.replanning.StrategyManager} should never pass a person to
	 * a PlanSelector that still has unscored plans, as such plans would be selected by default
	 * ("optimistic behavior"). But as this may be optional sometimes later, it's best to ensure
	 * already now that PlanSelector's can cope with such a situation.
	 *
	 *  @author mrieser
	 */
	public void testUndefinedScore() {
		PersonImpl person;
		GenericPlanSelector<Plan, Person> selector = getPlanSelector();
		PlanImpl plan;

		// test 1: exactly one plan, with undefined score
		person = new PersonImpl(Id.create(1, Person.class));
		person.createAndAddPlan(false);
		assertNotNull(selector.selectPlan(person));

		// test 2: one plan with undefined score, one with defined score. The one with undefined comes first.
		person = new PersonImpl(Id.create(1, Person.class));
		person.createAndAddPlan(false);
		plan = person.createAndAddPlan(false);
		plan.setScore(10.0);
		assertNotNull(selector.selectPlan(person));

		// test 3: one plan with undefined score, one with defined score. The one with undefined comes last.
		person = new PersonImpl(Id.create(1, Person.class));
		plan = person.createAndAddPlan(false);
		plan.setScore(10.0);
		person.createAndAddPlan(false);
		assertNotNull(selector.selectPlan(person));

		// test 4: one plan with undefined score, two with defined score.
		person = new PersonImpl(Id.create(1, Person.class));
		plan = person.createAndAddPlan(false);
		plan.setScore(10.0);
		person.createAndAddPlan(false);
		plan = person.createAndAddPlan(false);
		plan.setScore(10.0);
		assertNotNull(selector.selectPlan(person));
	}

	/**
	 * Test how a plan selector reacts when a person has no plans at all. The correct behavior would be
	 * to just return null, as stated in
	 * {@link PlanSelector#selectPlan(HasPlansAndId) PlanSelector.selectPlan(Person)}
	 *
	 * @author mrieser
	 */
	public void testNoPlans() {
		PersonImpl person = new PersonImpl(Id.create(1, Person.class));
		assertNull(getPlanSelector().selectPlan(person));
	}

	/**
	 * Test how a plan selector reacts when a plan has a negative score.
	 * This test only ensures that a plan is returned and no Exception occurred when selecting a plan.
	 *
	 * @author mrieser
	 */
	public void testNegativeScore() {
		GenericPlanSelector<Plan, Person> selector = getPlanSelector();
		PlanImpl plan;
		// test with only one plan...
		PersonImpl person = new PersonImpl(Id.create(1, Person.class));
		plan = person.createAndAddPlan(false);
		plan.setScore(-10.0);
		assertNotNull(selector.selectPlan(person));

		// ... test with multiple plans that all have negative score
		person = new PersonImpl(Id.create(1, Person.class));
		plan = person.createAndAddPlan(false);
		plan.setScore(-10.0);
		plan = person.createAndAddPlan(false);
		plan.setScore(-50.0);
		assertNotNull(selector.selectPlan(person));

		// ... and test with multiple plans where the sum of all scores stays negative
		person = new PersonImpl(Id.create(1, Person.class));
		plan = person.createAndAddPlan(false);
		plan.setScore(-10.0);
		plan = person.createAndAddPlan(false);
		plan.setScore(-50.0);
		plan = person.createAndAddPlan(false);
		plan.setScore(20.0);
		assertNotNull(selector.selectPlan(person));

		// test with only one plan, but with NEGATIVE_INFINITY...
		person = new PersonImpl(Id.create(1, Person.class));
		plan = person.createAndAddPlan(false);
		plan.setScore(Double.NEGATIVE_INFINITY);
		assertNotNull(selector.selectPlan(person));
	}

	/**
	 * Test how a plan selector reacts when a plan has a score of zero (0.0).
	 * This test only ensures that a plan is returned and no Exception occurred when selecting a plan.
	 */
	public void testZeroScore() {
		GenericPlanSelector<Plan, Person> selector = getPlanSelector();
		PlanImpl plan;
		PersonImpl person = new PersonImpl(Id.create(1, Person.class));
		plan = person.createAndAddPlan(false);
		plan.setScore(0.0);
		assertNotNull(selector.selectPlan(person));
	}

	/**
	 * @return A new instance of a specific implementation of {@link PlanSelector} for testing.
	 */
	protected abstract GenericPlanSelector<Plan, Person> getPlanSelector();
	
}
