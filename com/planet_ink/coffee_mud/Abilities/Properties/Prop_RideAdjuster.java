package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RideAdjuster extends Property
{
	public String ID() { return "Prop_RideAdjuster"; }
	public String name(){ return "Adjustments to stats when ridden";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}
	public boolean bubbleAffect(){return true;}
	private CharStats adjCharStats=null;
	private CharState adjCharState=null;
	private EnvStats  adjEnvStats=null;
	boolean gotClass=false;
	boolean gotRace=false;
	boolean gotSex=false;
	
	public Environmental newInstance(){	Prop_RideAdjuster BOB=new Prop_RideAdjuster();	BOB.setMiscText(text()); return BOB;}

	public String accountForYourself()
	{
		return Prop_HaveAdjuster.fixAccoutings("Affects on the mounted: "+text());
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		this.adjCharStats=new DefaultCharStats();
		this.adjEnvStats=new DefaultEnvStats();
		this.adjCharState=new DefaultCharState();
		int gotit=Prop_HaveAdjuster.setAdjustments(newText,adjEnvStats,adjCharStats,adjCharState);
		gotClass=((gotit&1)==1);
		gotRace=((gotit&2)==2);
		gotSex=((gotit&4)==4);
	}

	private void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}
	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		ensureStarted();
		if((affected !=null)
		&&(affectedMOB instanceof Rider)
		&&(((Rider)affectedMOB).riding()==affected))
			Prop_HaveAdjuster.envStuff(affectableStats,adjEnvStats);
		super.affectEnvStats(affectedMOB,affectableStats);
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
		if((affected !=null)
		&&(((Rider)affectedMOB).riding()==affected))
			Prop_HaveAdjuster.adjCharStats(affectedStats,gotClass,gotRace,gotSex,adjCharStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}
	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		ensureStarted();
		if((affected !=null)
		&&(((Rider)affectedMOB).riding()==affected))
			Prop_HaveAdjuster.adjCharState(affectedState,adjCharState);
		super.affectCharState(affectedMOB,affectedState);
	}
}