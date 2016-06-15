package com.planet_ink.coffee_mud.Commands;

import java.util.List;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.CMSecurity;

public class Su extends StdCommand {
    public Su() {
    }

    private final String[] access = I(new String[] { "SU" });

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags) throws java.io.IOException {
        String option = CMParms.combine(commands, 1);
        if(CMSecurity.isASysOp(mob) && !mob.isSuperUser()) {
            mob.tell("Super user mode is unavailable for archons.");
            return false;
        }
        if("".equals(option.trim())) {
            mob.tell("You are " + (mob.isSuperUser() ? "" : "not ") + "in super user mode. \nUse [on/off] to switch super user mode.");
        }
        else if("on".equals(option.trim())) {
            if(!mob.isSuperUser()) {
                String password = mob.session().prompt("Password:");
                String su_password = CMProps.getVar(CMProps.Str.SU_PASSWORD);
                if(su_password.equals(password)) {
                    mob.setSuperUser(true);
                    mob.tell("You are now in super user mode.");
                }
                else {
                    mob.tell("Validation fails. Wrong password.");
                }
            }
            else {
                mob.tell("You are already in super user mode.");
            }
        }
        else if("off".equals(option.trim())) {
            if(mob.isSuperUser()) {
                mob.setSuperUser(false);
                mob.tell("You are now not in super user mode.");
            }
            else {
                mob.tell("You are not in super user mode.");
            }
        }
        return false;
    }

    @Override
    public boolean canBeOrdered() {
        return true;
    }
}
