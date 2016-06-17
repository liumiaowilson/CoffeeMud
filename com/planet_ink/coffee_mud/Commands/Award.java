package com.planet_ink.coffee_mud.Commands;

import java.util.List;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.awards.AwardSystem;
import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.CMStrings;

public class Award extends StdCommand {
    public Award() {
    }

    private final String[] access = I(new String[] { "AWARD" });

    @Override
    public String[] getAccessWords() {
        return access;
    }
    
    private int [] getColWidths(AwardSystem.Award [] awards) {
        int [] result = new int [3];
        for(int i = 0; i < awards.length; i++) {
            AwardSystem.Award award = awards[i];
            int num_width = String.valueOf(i + 1).length();
            if(num_width > result[0]) {
                result[0] = num_width;
            }
            int name_width = award.getName().length();
            if(name_width > result[1]) {
                result[1] = name_width;
            }
            int point_width = String.valueOf(award.getPoint()).length();
            if(point_width > result[2]) {
                result[2] = point_width;
            }
        }
        return result;
    }
    
    private boolean showAllAwards(MOB mob) {
        AwardSystem.Award [] awards = AwardSystem.getInstance().getAllAwards();
        int [] col_widths = this.getColWidths(awards);
        String num_title = "No.";
        String name_title = "Name";
        String point_title = "Point";
        col_widths[0] = Math.max(col_widths[0], num_title.length()) + 2;
        col_widths[1] = Math.max(col_widths[1], name_title.length()) + 2;
        col_widths[2] = Math.max(col_widths[2], point_title.length()) + 2;
        
        StringBuilder msg = new StringBuilder("");
        msg.append("^x");
        msg.append(CMStrings.padRight(num_title, col_widths[0]));
        msg.append(CMStrings.padRight(name_title, col_widths[1]));
        msg.append(CMStrings.padRight(point_title, col_widths[2]));
        msg.append("^.^N");
        mob.tell(msg.toString());
        
        for(int i = 0; i < awards.length; i++) {
            msg = new StringBuilder("");
            msg.append(CMStrings.padRight(String.valueOf(i + 1), col_widths[0]));
            msg.append(CMStrings.padRight(awards[i].getName(), col_widths[1]));
            msg.append(CMStrings.padLeft(String.valueOf(awards[i].getPoint()), col_widths[2]));
            mob.tell(msg.toString());
        }
        
        return false;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags) throws java.io.IOException {
        if(commands.size() == 1) {
            //show all awards
            return showAllAwards(mob);
        }
        else {
            //TODO
        }
        
        return false;
    }

    @Override
    public boolean securityCheck(MOB mob) {
        return CMSecurity.isASysOp(mob);
    }

}
