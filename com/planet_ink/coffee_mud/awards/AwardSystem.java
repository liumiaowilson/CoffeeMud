package com.planet_ink.coffee_mud.awards;

import java.util.ArrayList;
import java.util.List;

import com.planet_ink.coffee_mud.core.CMFile;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.Resources;

public class AwardSystem {
    public static final String RESOURCE_ALL_NAME = "resources/awards/all.txt";
    public static final String RESOURCE_ALL_KEY = "AWARD SYSTEM ALL";
    
    private static AwardSystem instance;

    private AwardSystem() {

    }

    public static AwardSystem getInstance() {
        if (instance == null) {
            instance = new AwardSystem();
        }
        return instance;
    }
    
    public Award [] getAllAwards() {
        Award [] awards = (Award[]) Resources.getResource(RESOURCE_ALL_KEY);
        if(awards == null) {
            awards = loadAwards();
            Resources.submitResource(RESOURCE_ALL_KEY, awards);
        }
        return awards;
    }
    
    private Award [] loadAwards() {
        String resource_all_name = Resources.makeFileResourceName(RESOURCE_ALL_NAME);
        CMFile resourceAllFile = new CMFile(resource_all_name, null, 0);
        if(resourceAllFile.exists() && resourceAllFile.canRead()) {
            String content = resourceAllFile.text().toString();
            if(content != null && !"".equals(content.trim())) {
                String [] lines = content.split("\n");
                List<Award> awards = new ArrayList<Award>();
                for(String line : lines) {
                    String [] items = line.split(",");
                    if(items.length < 3) {
                        Log.warnOut("AwardSystem", "Invalid line for " + line);
                        continue;
                    }
                    else {
                        String id = items[0];
                        String name = items[1];
                        int point = 1;
                        try {
                            point = Integer.parseInt(items[2]);
                        }
                        catch(Exception e) {
                            Log.errOut(e);
                            point = 1;
                        }
                        Award award = new Award();
                        award.setId(id);
                        award.setName(name);
                        award.setPoint(point);
                        awards.add(award);
                    }
                }
                return awards.toArray(new Award[0]);
            }
            else {
                Log.warnOut("AwardSystem", "Load empty resource all file");
                return new Award[0];
            }
        }
        else {
            Log.errOut("AwardSystem", "Cannot load resource all file");
            return new Award[0];
        }
    }

    public class Award {
        private String id;
        private String name;
        private int    point;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPoint() {
            return point;
        }

        public void setPoint(int point) {
            this.point = point;
        }
    }
}
