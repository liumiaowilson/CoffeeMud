package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_HaveSpellCast extends Property
{
	private Item myItem=null;
	private MOB lastMOB=null;
	boolean processing=false;

	public Prop_HaveSpellCast()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Casting spells when owned";
	}

	public Environmental newInstance()
	{
		Prop_HaveSpellCast BOB=new Prop_HaveSpellCast();
		BOB.setMiscText(text());
		return BOB;
	}


	public String accountForYourself()
	{
		String id="";
		Vector V=Prop_SpellAdder.getMySpellsV(this);
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			if(V.size()==1)
				id+=A.name();
			else
			if(v==(V.size()-1))
				id+="and "+A.name();
			else
				id+=A.name()+", ";
		}
		if(V.size()>0)
			id="Casts "+id+" on the owner.";
		return id;
	}

	public void addMeIfNeccessary(MOB newMOB)
	{
		Vector V=Prop_SpellAdder.getMySpellsV(this);
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			Ability EA=newMOB.fetchAffect(A.ID());
			if((EA==null)&&(Prop_SpellAdder.didHappen(100,this)))
			{
				A.invoke(newMOB,newMOB,true);
				EA=newMOB.fetchAffect(A.ID());
			}
			if(EA!=null)
				EA.makeLongLasting();
		}
		lastMOB=newMOB;
	}

	public void removeMyAffectsFromLastMob()
	{
		Hashtable h=Prop_SpellAdder.getMySpellsH(this);
		int x=0;
		while(x<lastMOB.numAffects())
		{
			int y=lastMOB.numAffects();
			Ability thisAffect=lastMOB.fetchAffect(x);
			String ID=(String)h.get(thisAffect.ID());
			if((ID!=null)&&(thisAffect.invoker()==lastMOB))
			{
				thisAffect.unInvoke();
				x=0;
			}
			else
				x++;
		}
		lastMOB=null;
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		if(processing) return;
		processing=true;
		if((affectedMOB!=null)&&(affectedMOB instanceof Item))
		{
			myItem=(Item)affectedMOB;

			if((lastMOB!=null)&&(myItem.myOwner()!=lastMOB)
			&&(lastMOB.location()!=null))
				removeMyAffectsFromLastMob();

			if((lastMOB==null)&&(myItem.myOwner()!=null)
			&&(myItem.myOwner() instanceof MOB)&&(((MOB)myItem.myOwner()).location()!=null))
				addMeIfNeccessary((MOB)myItem.myOwner());
		}
		super.affectEnvStats(affectedMOB,affectableStats);
		processing=false;
	}
}