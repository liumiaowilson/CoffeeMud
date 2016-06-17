package com.planet_ink.coffee_mud.Commands;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.database.DBConnection;
import com.planet_ink.coffee_mud.core.database.DBConnector;

public class DB_Query extends StdCommand {
    public DB_Query() {
    }

    private final String[] access = I(new String[] { "DB_QUERY" });

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags) throws java.io.IOException {
        String sql = CMParms.combine(commands, 1);
        if("".equals(sql.trim())) {
            mob.tell("No sql is provided.");
            return false;
        }
        
        String test = commands.get(commands.size() - 2);
        int limit = -1;
        if("limit".equalsIgnoreCase(test)) {
            try {
                limit = Integer.parseInt(commands.get(commands.size() - 1));
            }
            catch(Exception e) {
                Log.errOut(e);
            }
            
            int pos = sql.lastIndexOf(test);
            sql = sql.substring(0, pos - 1);
        }
        
        DBConnector connector = CMLib.database().getConnector();
        DBConnection con = null;
        try {
            con = connector.DBFetch();
            ResultSet rs = con.query(sql);
            ResultSetMetaData meta = rs.getMetaData();
            
            int col_num = meta.getColumnCount();
            String [] col_names = new String [col_num];
            int [] col_widths = new int [col_num];
            int [] col_types = new int [col_num];
            for(int i = 0; i < col_num; i++) {
                String col_name = meta.getColumnLabel(i + 1);
                col_names[i] = col_name;
                
                int col_width = meta.getColumnDisplaySize(i + 1);
                col_widths[i] = col_width;
                
                int col_type = meta.getColumnType(i + 1);
                col_types[i] = col_type;
            }
            
            List<String []> rows = new ArrayList<String []>();
            while(rs.next()) {
                String [] items = new String [col_num];
                for(int i = 0; i < col_num; i++) {
                    if(Types.INTEGER == col_types[i]) {
                        items[i] = String.valueOf(rs.getInt(i));
                    }
                    else if(Types.BIGINT == col_types[i]) {
                        items[i] = String.valueOf(rs.getLong(i));
                    }
                    else if(Types.VARCHAR == col_types[i]) {
                        items[i] = String.valueOf(rs.getString(i));
                    }
                    else {
                        items[i] = "Unknown";
                    }
                }
                rows.add(items);
                if(limit > 0 && rows.size() >= limit) {
                    break;
                }
            }
            rs.close();
            
            int [] actual_col_widths = getColWidths(rows);
            for(int i = 0; i < actual_col_widths.length; i++) {
                actual_col_widths[i] = Math.max(actual_col_widths[i], col_names[i].length());
                actual_col_widths[i] = Math.min(actual_col_widths[i], col_widths[i]) + 2;
            }
            
            StringBuilder msg = new StringBuilder("");
            msg.append("^x");
            for(int i = 0; i < col_names.length; i++) {
                msg.append(CMStrings.padRight(col_names[i], actual_col_widths[i]));
            }
            msg.append("^.^N");
            mob.tell(msg.toString());
            
            for(int i = 0; i < rows.size(); i++) {
                String [] items = rows.get(i);
                msg = new StringBuilder("");
                for(int j = 0; j < items.length; j++) {
                    msg.append(CMStrings.padRight(items[j], actual_col_widths[j]));
                }
                mob.tell(msg.toString());
            }
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
    
    private int [] getColWidths(List<String []> rows) {
        if(rows == null || rows.isEmpty()) {
            return null;
        }
        
        int [] result = new int [rows.get(0).length];
        
        for(String [] items : rows) {
            for(int i = 0; i < items.length; i++) {
                if(items[i].length() > result[i]) {
                    result[i] = items[i].length();
                }
            }
        }
        
        return result;
    }

    @Override
    public boolean securityCheck(MOB mob) {
        return CMSecurity.isASysOp(mob);
    }

}
