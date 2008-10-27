package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/* 
   Copyright 2000-2008 Bo Zimmerman

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
public class BankAccountInfo extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public static synchronized Vector getMakeAccountInfo(ExternalHTTPRequests httpReq, Banker B, MOB playerM, Area playerA)
	{
		Vector info=(Vector)httpReq.getRequestObjects().get("BANKINFO: "+B.bankChain()+": "+playerM.Name());
		if(info!=null) return info;
		info=new Vector();
		if((!playerM.Name().equals(playerM.getClanID()))&&(B.whatIsSold()==Banker.DEAL_CLANBANKER))
		{
			info.addElement(new Double(0.0));
			info.addElement(new Vector());
			info.addElement(new Vector());
		}
		else
		{
			Double bal=new Double(B.getBalance(playerM));
			info.addElement(bal);
			if(bal.doubleValue()<=0.0)
			{
				info.addElement(new Vector());
				info.addElement(new Vector());
			}
			else
			{
				Vector debtV=B.getDebtInfo(playerM);
				if(debtV==null) debtV=new Vector();
				info.addElement(debtV);
				Vector items=B.getDepositedItems(playerM);
				if(items==null) items=new Vector();
				info.addElement(items);
			}
		}
		httpReq.getRequestObjects().put("BANKINFO: "+B.bankChain()+": "+playerM.Name(),info);
		return info;
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		MOB playerM=null;
		Area playerA=null;
		boolean destroyPlayer=false;
		try{
		Hashtable<String,String> parms=parseParms(parm);
		String last=httpReq.getRequestParameter("BANKCHAIN");
		if(last==null) return " @break@";
		MOB M=CMLib.players().getLoadPlayer(Authenticate.getLogin(httpReq));
		String player=httpReq.getRequestParameter("PLAYER");
		if((player==null)||(player.length()==0))
			player=httpReq.getRequestParameter("CLAN");
		Banker B=CMLib.map().getBank(last,last);
		if(B==null) return "BANKER not found?!";
		if((player!=null)&&(player.length()>0))
		{
			if(((M==null)||(!M.Name().equalsIgnoreCase(player)))
			&&(!CMSecurity.isAllowedEverywhere(M,"CMDPLAYERS)")))
				return "";
			Clan C=CMLib.clans().getClan(player);
			if(C!=null)
			{
				playerM=CMClass.getMOB("StdMOB");
				playerM.setName(C.clanID());
				playerM.setLocation(CMLib.map().getStartRoom(B));
				playerM.setStartRoom(CMLib.map().getStartRoom(B));
				playerM.setClanID(C.clanID());
				playerM.setClanRole(Clan.POS_BOSS);
				destroyPlayer=true;
			}
			else
				playerM=CMLib.players().getLoadPlayer(player);
			if(playerM!=null) playerA=CMLib.map().getStartArea(playerM);
			if((playerM==null)||(playerA==null)) 
				return "PLAYER not found!";
		}
		else
			return "PLAYER not set!";
		Vector acct=BankAccountInfo.getMakeAccountInfo(httpReq,B,playerM,playerA);
		double balance=((Double)acct.firstElement()).doubleValue();
		if(parms.containsKey("HASACCT"))
			return (balance>0.0)?"true":"false"; 
		if(balance<=0.0) return "";
		if(parms.containsKey("BALANCE")) 
			return CMLib.beanCounter().nameCurrencyLong(playerM,balance);
		if((parms.containsKey("DEBTAMT"))
		||(parms.containsKey("DEBTRSN"))
		||(parms.containsKey("DEBTDUE"))
		||(parms.containsKey("DEBTINT")))
		{
			Vector debtV=(Vector)acct.elementAt(1);
			if((debtV==null)||(debtV.size()==0)) return "N/A";
			double debt=((Double)debtV.elementAt(MoneyLibrary.DEBT_AMTDBL-1)).doubleValue();
			String reason=((String)debtV.elementAt(MoneyLibrary.DEBT_REASON-1));
			String intRate=CMath.div((int)Math.round(((Double)debtV.elementAt(MoneyLibrary.DEBT_INTDBL-1)).doubleValue()*10000.0),100.0)+"%";
			long dueLong=((Long)debtV.elementAt(MoneyLibrary.DEBT_DUELONG-1)).longValue();
			long timeRemaining=dueLong-dueLong;
			String dueDate="";
			if(timeRemaining<0)
				dueDate="Past due.";
			else
			{
				int mudHoursToGo=(int)(timeRemaining/Tickable.TIME_MILIS_PER_MUDHOUR);
				if(playerA.getTimeObj()==null)
					dueDate="Not available";
				else
				{
					TimeClock T=(TimeClock)playerA.getTimeObj().copyOf();
					T.tickTock(mudHoursToGo);
					dueDate=T.getShortTimeDescription();
				}
			}
			if(parms.containsKey("DEBTAMT")) return CMLib.beanCounter().nameCurrencyLong(playerM,debt);
			if(parms.containsKey("DEBTRSN")) return reason;
			if(parms.containsKey("DEBTDUE")) return dueDate;
			if(parms.containsKey("DEBTINT")) return intRate;
		}
		if(parms.containsKey("NUMITEMS")) return ""+(B.getDepositedItems(playerM).size()-1);
		if(parms.containsKey("ITEMSWORTH")) return CMLib.beanCounter().nameCurrencyLong(playerM,B.totalItemsWorth(playerM));
		if(parms.containsKey("ITEMSLIST"))
		{
			Vector items=(Vector)acct.lastElement();
			StringBuffer list=new StringBuffer("");
			for(int v=0;v<items.size();v++)
				if(!(items.elementAt(v) instanceof Coins))
				{
					list.append(((Environmental)items.elementAt(v)).name());
					if(v<(items.size()-1)) list.append(", ");
				}
			return list.toString();
		}
		return "";
		}finally{if(destroyPlayer) playerM.destroy();}
	}
}