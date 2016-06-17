package com.planet_ink.coffee_mud.Commands;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.database.DBConnection;
import com.planet_ink.coffee_mud.core.database.DBConnector;
import com.planet_ink.fakedb.Backend;

public class DB extends StdCommand {
    public DB() {
    }

    private final String[] access = I(new String[] { "DB" });

    @Override
    public String[] getAccessWords() {
        return access;
    }
    
    private boolean showTable(MOB mob, String tablename) {
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
         }     connector.DBDone(con);
      
        
        return false;
    }
    
    private boolean listTables(MOB mob) {
        DBConnector connector = CMLib.database().getConnector();
        if(!connector.isFakeDB()) {
            mob.tell("Only able to list table names for fake db.");
            return false;
        }
        
        DBConnection con = null;
        try {
            con = connector.DBFetch();
            java.sql.Connection realCon = con.getRealConnection();
            if(realCon instanceof com.planet_ink.fakedb.Connection) {
                com.planet_ink.fakedb.Connection fakeCon = (com.planet_ink.fakedb.Connection)realCon;
                Backend backend = fakeCon.getBackend();
                List<String> tableNames = backend.getTableNames();
                Collections.sort(tableNames);
                StringBuilder msg = CMLib.lister().fourColumns(mob, tableNames);
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
    
    private boolean query(MOB mob, String sql) {
        if("".equals(sql.trim())) {
            mob.tell("No sql is provided.");
            return false;
        }
        
        String [] testItems = sql.split(" ");
        
        String test = testItems[testItems.length - 2];
        int limit = -1;
        if("limit".equalsIgnoreCase(test)) {
            try {
                limit = Integer.parseInt(testItems[testItems.length - 1]);
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
                        items[i] = String.valueOf(rs.getInt(i + 1));
                    }
                    else if(Types.BIGINT == col_types[i]) {
                        items[i] = String.valueOf(rs.getLong(i + 1));
                    }
                    else if(Types.VARCHAR == col_types[i]) {
                        items[i] = String.valueOf(rs.getString(i + 1));
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
            if(actual_col_widths == null) {
                actual_col_widths = new int [col_widths.length];
            }
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
    public boolean execute(MOB mob, List<String> commands, int metaFlags) throws java.io.IOException {
        if(commands.size() == 1) {
            //list tables
            return this.listTables(mob);
        }
        else {
            String subCommand = commands.get(1);
            if("show".equalsIgnoreCase(subCommand)) {
                //show table
                String tablename = CMParms.combine(commands, 2);
                return this.showTable(mob, tablename);
            }
            else if("query".equalsIgnoreCase(subCommand)) {
                //do query
                String sql = CMParms.combine(commands, 2);
                return this.query(mob, sql);
            }
            else {
                mob.tell("Operation " + subCommand + " is not supported.");
            }
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
