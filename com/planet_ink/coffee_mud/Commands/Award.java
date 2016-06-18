package com.planet_ink.coffee_mud.Commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.awards.AwardSystem;
import com.planet_ink.coffee_mud.awards.AwardSystem.AwardHistory;
import com.planet_ink.coffee_mud.awards.AwardSystem.AwardHistoryItem;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.Log;

public class Award extends StdCommand {
    public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
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
    
    private AwardSystem.Award findAward(MOB mob, String selectStr) {
        AwardSystem.Award award = null;
        
        AwardSystem.Award [] awards = AwardSystem.getInstance().getAllAwards();
        try {
            int id = Integer.parseInt(selectStr);
            if(id < 1 || id > awards.length) {
                mob.tell("No." + id + " is out of range.");
                return null;
            }
            else {
                award = awards[id - 1];
            }
        }
        catch(Exception e) {
            for(AwardSystem.Award a : awards) {
                if(a.getName().contains(selectStr)) {
                    award = a;
                    break;
                }
            }
        }
        
        return award;
    }
    
    private boolean grantAward(MOB mob, String selectStr) {
        AwardSystem.Award award = findAward(mob, selectStr);
        
        if(award == null) {
            mob.tell("No award is matched.");
            return false;
        }
        
        try {
            String content = mob.session().prompt("Enter: ");
            if(content == null || "".equals(content.trim())) {
                mob.tell("Content should be provided.");
                return false;
            }
            
            boolean result = AwardSystem.getInstance().grantAward(award, content);
            if(result) {
                mob.tell("Grant award [" + award.getName() + "] successfully.");
                
                int point = award.getPoint();
                mob.setQuestPoint(mob.getQuestPoint() + point);
                mob.tell("Gained " + point + " quest point(s). Now you have " + mob.getQuestPoint() + " quest point(s).");
                
                CMLib.players().savePlayers();
            }
            else {
                mob.tell("Failed to grant award [" + award.getName() + "].");
            }
        }
        catch(Exception e) {
            Log.errOut(e);
            mob.tell("Failed to handle prompts.");
        }
        
        return false;
    }
    
    private int [] getHistoryColWidths(AwardHistory history) {
        int [] result = new int [3];
        result[1] = DEFAULT_TIME_FORMAT.length();
        
        for(int i = 0; i < history.items.size(); i++) {
            AwardHistoryItem item = history.items.get(i);
            int num_width = String.valueOf(i + 1).length();
            if(num_width > result[0]) {
                result[0] = num_width;
            }
            int msg_width = item.getContent().length();
            if(msg_width > result[2]) {
                result[2] = msg_width;
            }
        }
        
        return result;
    }
    
    private boolean listAwardHistory(MOB mob, String selectStr) {
        AwardSystem.Award award = findAward(mob, selectStr);
        
        if(award == null) {
            mob.tell("No award is matched.");
            return false;
        }
        
        AwardHistory history = AwardSystem.getInstance().findHistoryFor(award);
        
        mob.tell("Show history for award [" + award.getName() + "]:");
        
        int [] col_widths = this.getHistoryColWidths(history);
        String num_title = "No.";
        String time_title = "Time";
        String msg_title = "Message";
        int num_width = Math.max(col_widths[0], num_title.length()) + 2;
        int time_width = Math.max(col_widths[1], time_title.length()) + 2;
        int msg_width = Math.max(col_widths[2], msg_title.length()) + 2;
        if(num_width + time_width + msg_width > 80) {
            msg_width = 80 - num_width - time_width;
        }
        
        StringBuilder msg = new StringBuilder("");
        msg.append("^x");
        msg.append(CMStrings.padRight(num_title, num_width));
        msg.append(CMStrings.padRight(time_title, time_width));
        msg.append(CMStrings.padRight(msg_title, msg_width));
        msg.append("^.^N");
        mob.tell(msg.toString());
        
        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_TIME_FORMAT);
        
        for(int i = 0; i < history.items.size(); i++) {
            AwardHistoryItem item = history.items.get(i);
            String num = String.valueOf(i + 1);
            String time = format.format(new Date(item.getTime()));
            String content = CMStrings.truncate(item.getContent(), msg_width);
            msg = new StringBuilder("");
            msg.append(CMStrings.padRight(num, num_width));
            msg.append(CMStrings.padRight(time, time_width));
            msg.append(CMStrings.padRight(content, msg_width));
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
            String subCommand = commands.get(1);
            if("grant".equalsIgnoreCase(subCommand)) {
                String selectStr = CMParms.combine(commands, 2);
                return grantAward(mob, selectStr);
            }
            else if("list".equalsIgnoreCase(subCommand)) {
                String selectStr = CMParms.combine(commands, 2);
                return listAwardHistory(mob, selectStr);
            }
            else {
                mob.tell("Operation " + subCommand + " is not supported.");
            }
        }
        
        return false;
    }

    @Override
    public boolean securityCheck(MOB mob) {
        return CMSecurity.isASysOp(mob);
    }

}
