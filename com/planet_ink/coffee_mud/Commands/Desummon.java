package com.planet_ink.coffee_mud.Commands;

import java.util.List;

import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMSecurity;

public class Desummon extends StdCommand {
    public Desummon() {
    }

    private final String[] access = I(new String[] { "DESUMMON" });

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
        
        Room room = CMLib.map().getRoom(mob.location());
        MOB target = null;
        for(int i = 0; i < room.numInhabitants(); i++) {
            MOB inhabitant = room.fetchInhabitant(i);
            if(inhabitant.name().equals(name)) {
                target = inhabitant;
                break;
            }
        }
        if(target != null) {
            if(target.isPlayer()) {
                target.removeFromGame(true, true);
                mob.tell("NPCPlayer " + name + " has been desummoned.");
            }
            else {
                mob.tell("MOB " + name + " is not a NPCPlayer. Cannot be desummoned.");
            }
        }
        else {
            mob.tell("Cannot find a MOB named " + name + ".");
        }
        
        return false;
    }
    
    @Override
    public boolean securityCheck(MOB mob) {
        return CMSecurity.isASysOp(mob);
    }

}
