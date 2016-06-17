package com.planet_ink.coffee_mud.Commands;

import java.util.Collections;
import java.util.List;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.database.DBConnection;
import com.planet_ink.coffee_mud.core.database.DBConnector;
import com.planet_ink.fakedb.Backend;

public class DB_ListTables extends StdCommand {
    public DB_ListTables() {
    }

    private final String[] access = I(new String[] { "DB_LISTTABLES" });

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags) throws java.io.IOException {
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
    
    @Override
    public boolean securityCheck(MOB mob) {
        return CMSecurity.isASysOp(mob);
    }

}
