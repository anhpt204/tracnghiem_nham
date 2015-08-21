/* *********************************************************************** *
 * project: org.matsim.*
 * WorstPlanRespectingPlanTypeSelector.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;

public class WorstPlanForRemovalSelectorTest extends AbstractPlanSelectorTest {

	@Override
	protected GenericPlanSelector<Plan, Person> getPlanSelector() {
		return new WorstPlanForRemovalSelector();
	}

	/**
	 * Tests plan selection when all plans have the type <code>null</code>.
	 *
	 * @author mrieser
	 */
	public void testRemoveWorstPlans_nullType() {
		GenericPlanSelector<Plan, Person> selector = getPlanSelector();
		PersonImpl person = new PersonImpl(Id.create(1, Person.class));

		PlanImpl plan1 = new org.matsim.core.population.PlanImpl(person);
		plan1.setScore(15.0);
		PlanImpl plan2 = new org.matsim.core.population.PlanImpl(person);
		plan2.setScore(22.0);
		PlanImpl plan3 = new org.matsim.core.population.PlanImpl(person);
		PlanImpl plan4 = new org.matsim.core.population.PlanImpl(person);
		plan4.setScore(1.0);
		PlanImpl plan5 = new org.matsim.core.population.PlanImpl(person);
		plan5.setScore(18.0);
		person.addPlan(plan1);
		person.addPlan(plan2);
		person.addPlan(plan3);
		person.addPlan(plan4);
		person.addPlan(plan5);

		assertEquals("test we have all plans we want", 5, person.getPlans().size());

		person.getPlans().remove(selector.selectPlan(person));
		assertEquals("test that a plan was removed", 4, person.getPlans().size());
		assertFalse("test that plan with undefined score was removed.", person.getPlans().contains(plan3));

		person.getPlans().remove(selector.selectPlan(person));
		assertEquals("test that a plan was removed", 3, person.getPlans().size());
		assertFalse("test that the plan with minimal score was removed", person.getPlans().contains(plan4));

		person.getPlans().remove(selector.selectPlan(person));
		person.getPlans().remove(selector.selectPlan(person));
		assertEquals("test that two plans were removed", 1, person.getPlans().size());
		assertTrue("test that the plan left has highest score", person.getPlans().contains(plan2));
	}

	/**
	 * Tests plan selection when the plans have different types set.
	 *
	 * @author mrieser
	 */
	public void testRemoveWorstPlans_withTypes() {
		GenericPlanSelector<Plan, Person> selector = getPlanSelector();
		/* The used plans, ordered by score:
		 * plan2: b, 22.0
		 * plan6: b, 21.0
		 * plan5: a, 18.0
		 * plan1: a, 15.0
		 * plan4: b,  1.0
		 * plan3: a, null
		 */
		PersonImpl person = new PersonImpl(Id.create(1, Person.class));

		PlanImpl plan1 = new org.matsim.core.population.PlanImpl(person);
		plan1.setScore(15.0);
		PlanImpl plan2 = new org.matsim.core.population.PlanImpl(person);
		plan2.setScore(22.0);
		PlanImpl plan3 = new org.matsim.core.population.PlanImpl(person);
		PlanImpl plan4 = new org.matsim.core.population.PlanImpl(person);
		plan4.setScore(1.0);
		PlanImpl plan5 = new org.matsim.core.population.PlanImpl(person);
		plan5.setScore(18.0);
		PlanImpl plan6 = new org.matsim.core.population.PlanImpl(person);
		plan6.setScore(21.0);

		plan1.setType("type1");
		plan2.setType("type2");
		plan3.setType("type1");
		plan4.setType("type2");
		plan5.setType("type1");
		plan6.setType("type2");
		person.addPlan(plan1);
		person.addPlan(plan2);
		person.addPlan(plan3);
		person.addPlan(plan4);
		person.addPlan(plan5);
		person.addPlan(plan6);

		assertEquals("test we have all plans we want", 6, person.getPlans().size());

		person.getPlans().remove(selector.selectPlan(person));
		person.getPlans().remove(selector.selectPlan(person));
		assertEquals("test that two plans were removed", 4, person.getPlans().size());
		assertFalse("test that plan with undefined score was removed.", person.getPlans().contains(plan3));
		assertFalse("test that plan with worst score was removed.", person.getPlans().contains(plan4));

		person.getPlans().remove(selector.selectPlan(person));
		person.getPlans().remove(selector.selectPlan(person));
		assertEquals("test that two plans were removed", 2, person.getPlans().size());
		assertFalse("test that the plan with worst score was removed", person.getPlans().contains(plan1));
		assertTrue("test that the now only plan of type a was not removed", person.getPlans().contains(plan5));
		assertFalse("test that the plan with the 2nd-worst score was removed", person.getPlans().contains(plan6));

		person.getPlans().remove(selector.selectPlan(person));
		assertEquals("test that one plan was removed", 1, person.getPlans().size());
		assertTrue("test that the plan with highest score of type b was not removed", person.getPlans().contains(plan2));
	}

}
