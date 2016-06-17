package com.planet_ink.coffee_mud.Commands;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.database.DBConnection;
import com.planet_ink.coffee_mud.core.database.DBConnector;

public class DB_ShowTable extends StdCommand {
    public DB_ShowTable() {
    }

    private final String[] access = I(new String[] { "DB_SHOWTABLE" });

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags) throws java.io.IOException {
        String tablename = CMParms.combine(commands, 1);
        if("".equals(tablename.trim())) {
            mob.tell("No table name is provided.");
            return false;
        }
        
        DBConnector connector = CMLib.database().getConnector();
        tablename = connector.injectionClean(tablename);
        DBConnection con = null;
        try {
            con = connector.DBFetch();
            ResultSet rs = con.query("select * from " + tablename);
            ResultSetMetaData meta = rs.getMetaData();
            
            int col_num = meta.getColumnCount();
            String [] col_names = new String [col_num];
            int [] col_widths = new int [col_num];
            String [] col_types = new String [col_num];
            for(int i = 0; i < col_num; i++) {
                String col_name = meta.getColumnLabel(i + 1);
                col_names[i] = col_name;
                
                int col_width = meta.getColumnDisplaySize(i + 1);
                col_widths[i] = col_width;
                
                String col_type = meta.getColumnTypeName(i + 1);
                col_types[i] = col_type;
            }
            
            String name_title = "Name";
            int name_width = getColWidth(col_names);
            name_width = Math.max(name_width, name_title.length()) + 2;
            
            String type_title = "Type";
            int type_width = getColWidth(col_types);
            type_width = Math.max(type_width, type_title.length()) + 2;
            
            String width_title = "Width";
            int width_width = getColWidth(col_widths);
            width_width = Math.max(width_width, width_title.length()) + 2;

            StringBuilder msg = new StringBuilder("");
            msg.append("^x");
            msg.append(CMStrings.padRight(name_title, name_width));
            msg.append(CMStrings.padRight(type_title, type_width));
            msg.append(CMStrings.padRight(width_title, width_width));
            msg.append("^.^N");
            mob.tell(msg.toString());
            
            for(int i = 0; i < col_num; i++) {
                msg = new StringBuilder("");
                msg.append(CMStrings.padRight(col_names[i], name_width));
                msg.append(CMStrings.padRight(col_types[i], type_width));
                msg.append(CMStrings.padRight(String.valueOf(col_widths[i]), width_width));
                mob.tell(msg.toString());
            }
            
            rs.close();
        }
        catch(Exception e) {
            Log.errOut(e);
            mob.tell(e.getMessage());
        }
        finally {
            connector.DBDone(con);
        }
        
        return false;
    }
    
    private int getColWidth(String [] array) {
        int [] new_array = new int [array.length];
        for(int i = 0; i < array.length; i++) {
            new_array[i] = array[i].length();
        }
        return getColWidth(new_array);
    }
    
    private int getColWidth(int [] array) {
        int max = 0;
        
        for(int i = 0; i < array.length; i++) {
            if(max < array[i]) {
                max = array[i];
            }
        }
        
        return max;
    }

    @Override
    public boolean securityCheck(MOB mob) {
        return CMSecurity.isASysOp(mob);
    }

}
