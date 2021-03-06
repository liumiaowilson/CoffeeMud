package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.MoonPhase;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TidePhase;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/*
   Copyright 2016-2016 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

public class Pirate extends Thief
{
	@Override
	public String ID()
	{
		return "Pirate";
	}

	private final static String localizedStaticName = CMLib.lang().L("Pirate");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	public Pirate()
	{
		super();
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
		maxStatAdj[CharStats.STAT_CHARISMA]=4;
	}
	
	@Override
	public int availabilityCode()
	{
		return 0;//Area.THEME_FANTASY;
	}

	@Override
	public String[] getRequiredRaceList()
	{
		return super.getRequiredRaceList();
	}

	@SuppressWarnings("unchecked")
	private final Pair<String, Integer>[] minimumStatRequirements = new Pair[] 
	{ 
		new Pair<String, Integer>("Dexterity", Integer.valueOf(5)), 
		new Pair<String, Integer>("Charisma", Integer.valueOf(5)) 
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Bonus XP in ship combat, and combat bonus for each fake limb.");
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return L("Get less leniency from the law, and can be paid to leave combat.");
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ThievesCant",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",0,false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_Superstition",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_RopeSwing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_ImprovedBoarding",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_LocateAlcohol",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_HoldYourLiquor",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Belay",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Hide",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_RideTheRigging",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Sneak",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_SeaLegs",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_BuriedTreasure",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_Wenching",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Dodge",false);
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(msg.value()>0)
		&&(msg.source().charStats().getCurrentClass() == this)
		&&(CMLib.map().areaLocation(msg.source()) instanceof BoardableShip)
		&&(msg.source() != msg.target()))
		{
			if(msg.target() instanceof MOB)
				msg.setValue(msg.value() * 2);
			else
			if(msg.target() == null)
			{
				BoardableShip shipArea = (BoardableShip)CMLib.map().areaLocation(msg.source());
				Room R=CMLib.map().roomLocation(shipArea.getShipItem());
				if(R!=null)
				{
					for(Enumeration<Item> i=R.items();i.hasMoreElements();)
					{
						Item I=i.nextElement();
						if((I instanceof BoardableShip)
						&&(I.fetchEffect("Sinking")!=null)
						&&(I!=shipArea.getShipItem()))
						{
							msg.setValue(msg.value() * 2);
							break;
						}
					}
				}
			}
		}
		if((msg.targetMinor()==CMMsg.MSG_LEGALWARRANT)
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).charStats().getCurrentClass()==this)
		&&(((MOB)msg.target()).location()!=null))
		{
			LegalBehavior behav = CMLib.law().getLegalBehavior(((MOB)msg.target()).location());
			Area area = CMLib.law().getLegalObject(((MOB)msg.target()).location());
			List<LegalWarrant> warrants = behav.getWarrantsOf(area, (MOB)msg.target());
			for(LegalWarrant W : warrants)
			{
				if((W.victim()==msg.tool())
				&&(W.crime() == msg.targetMessage()))
					W.setPunishment(W.punishment()+1);
			}
		}
	}
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			int numLimbs = 0;
			for(Enumeration<Item> i=((MOB)affected).items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if((I instanceof FalseLimb)
				&&(!I.amWearingAt(Item.IN_INVENTORY))
				&&(!I.amWearingAt(Wearable.WORN_HELD)))
				{
					numLimbs++;
				}
			}
			if(numLimbs > 0)
			{
				affectableStats.setDamage(affectableStats.damage() + numLimbs);
				affectableStats.setAttackAdjustment(affectableStats.damage() + (5 * numLimbs));
				affectableStats.setArmor(affectableStats.armor() - (5 * numLimbs));
			}
		}
		
	}
}
