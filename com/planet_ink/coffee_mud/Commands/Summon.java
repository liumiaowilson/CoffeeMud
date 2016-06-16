package com.planet_ink.coffee_mud.Commands;

import java.util.List;

import com.planet_ink.coffee_mud.Common.interfaces.CMMsg;
import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.CMSecurity;

public class Summon extends StdCommand {
    public Summon() {
    }

    private final String[] access = I(new String[] { "SUMMON" });

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags) throws java.io.IOException {
        String name = CMParms.combine(commands, 1);
        if("".equals(name.trim())) {
            mob.tell("Name should be provided.");
            return false;
        }
        
        //check similar logic in CharCreation.completeCharacterLogin
        MOB new_mob = CMLib.players().getLoadPlayer(name);
        if(new_mob != null) {
            if(new_mob.session() != null) {
                mob.tell("Sorry, cannot summon a MOB which has been occupied by a session.");
                return false;
            }
            Session new_session = (Session)CMClass.getCommon("FakeSession");
            new_session.initializeSession(null, Thread.currentThread().getThreadGroup().getName(), "MEMORY");
            new_session.setMob(new_mob);
            new_mob.setSession(new_session);
            Room startRoom = CMLib.map().getRoom(mob.location());
            new_mob.bringToLife(startRoom, false);
            new_mob.location().showOthers(new_mob,startRoom,CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,L("<S-NAME> appears!"));
            
            for(int f=0;f<new_mob.numFollowers();f++)
            {
                final MOB follower=new_mob.fetchFollower(f);
                if(follower==null)
                    continue;
                follower.setLocation(startRoom);
                follower.setFollowing(new_mob); // before for bestow names sake
                follower.bringToLife(startRoom,false);
                follower.setFollowing(new_mob);
                startRoom.showOthers(follower,startRoom,CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,L("<S-NAME> appears!"));
            }
            
            setGlobalBitmaps(new_mob);
        }
        else {
            mob.tell("Sorry, cannot find a MOB named " + name + ".");
        }
        
        return false;
    }
    
    private void setGlobalBitmaps(MOB mob)
    {
        if(mob==null)
            return;
        final List<String> defaultFlagsV=CMParms.parseCommas(CMProps.getVar(CMProps.Str.DEFAULTPLAYERFLAGS).toUpperCase(),true);
        for(int v=0;v<defaultFlagsV.size();v++)
        {
            final String flagName = defaultFlagsV.get(v); 
            for(MOB.Attrib a : MOB.Attrib.values())
            {
                if(a.getName().equals(flagName) || a.name().equals(flagName))
                    mob.setAttribute(a,true);
            }
        }
    }

    @Override
    public boolean securityCheck(MOB mob) {
        return CMSecurity.isASysOp(mob);
    }

}
