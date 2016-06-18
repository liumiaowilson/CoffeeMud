package com.planet_ink.coffee_mud.awards;

import java.util.ArrayList;
import java.util.List;

import com.planet_ink.coffee_mud.core.CMFile;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.Resources;

public class AwardSystem {
    public static final String RESOURCE_ALL_NAME = "resources/awards/all.txt";
    public static final String RESOURCE_ALL_KEY  = "AWARD SYSTEM ALL";

    private static AwardSystem instance;

    private AwardSystem() {

    }

    public static AwardSystem getInstance() {
        if (instance == null) {
            instance = new AwardSystem();
        }
        return instance;
    }

    public Award[] getAllAwards() {
        Award[] awards = (Award[]) Resources.getResource(RESOURCE_ALL_KEY);
        if (awards == null) {
            awards = loadAwards();
            Resources.submitResource(RESOURCE_ALL_KEY, awards);
        }
        return awards;
    }
    
    public AwardHistory [] getAllAwardHistories() {
        AwardHistory [] histories = (AwardHistory[])Resources.getResource("AwardSystem AwardHistories");
        if(histories == null) {
            histories = loadAwardHistories();
            Resources.submitResource("AwardSystem AwardHistories", histories);
        }
        return histories;
    }
    
    public AwardHistory findHistoryFor(Award award) {
        if(award == null) {
            return null;
        }
        
        AwardHistory [] histories = this.getAllAwardHistories();
        AwardHistory history = null;
        for(AwardHistory h : histories) {
            if(h.id.equals(award.getId())) {
                history = h;
                break;
            }
        }
        
        return history;
    }
    
    public boolean grantAward(Award award, String content) {
        if(award == null || content == null || "".equals(content.trim())) {
            return false;
        }
        
        AwardHistory history = this.findHistoryFor(award);
        
        if(history == null) {
            return false;
        }
        
        AwardHistoryItem item = new AwardHistoryItem();
        item.setTime(System.currentTimeMillis());
        item.setContent(content);
        history.items.add(item);
        
        return this.saveAwardHistory(history);
    }
    
    private boolean saveAwardHistory(AwardHistory history) {
        if(history == null) {
            return false;
        }
        
        String resource_name = Resources.makeFileResourceName("resources/awards/award_" + history.id + ".txt");
        CMFile resourceFile = new CMFile(resource_name, null, 0);
        
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < history.items.size(); i++) {
            AwardHistoryItem item = history.items.get(i);
            sb.append(item.getTime() + " " + item.getContent());
            if(i != history.items.size() - 1) {
                sb.append("\n");
            }
        }
        
        return resourceFile.saveText(sb);
    }
    
    private AwardHistory [] loadAwardHistories() {
        Award [] awards = this.getAllAwards();
        AwardHistory [] result = new AwardHistory [awards.length];
        
        for(int i = 0; i < awards.length; i++) {
            Award award = awards[i];
            String id = award.getId();
            String resource_name = Resources.makeFileResourceName("resources/awards/award_" + id + ".txt");
            CMFile resourceFile = new CMFile(resource_name, null, 0);
            String content = null;
            if(resourceFile.exists() && resourceFile.canRead()) {
                content = resourceFile.text().toString();
            }
            AwardHistory history = new AwardHistory();
            history.id = id;
            if(content != null && !"".equals(content.trim())) {
                String [] lines = content.split("\n\r");
                for(String line : lines) {
                    if(line == null || "".equals(line.trim())) {
                        continue;
                    }
                    try {
                        int pos = line.indexOf(" ");
                        String timeStr = line.substring(0, pos);
                        String message = line.substring(pos + 1);
                        long time = Long.parseLong(timeStr);
                        AwardHistoryItem item = new AwardHistoryItem();
                        item.setTime(time);
                        item.setContent(message);
                        history.items.add(item);
                    }
                    catch(Exception e) {
                        Log.errOut(e);
                    }
                }
            }
            result[i] = history;
        }
        
        return result;
    }

    private Award[] loadAwards() {
        String resource_all_name = Resources.makeFileResourceName(RESOURCE_ALL_NAME);
        CMFile resourceAllFile = new CMFile(resource_all_name, null, 0);
        if (resourceAllFile.exists() && resourceAllFile.canRead()) {
            String content = resourceAllFile.text().toString();
            if (content != null && !"".equals(content.trim())) {
                String[] lines = content.split("\n\r");
                List<Award> awards = new ArrayList<Award>();
                for (String line : lines) {
                    if(line == null || "".equals(line.trim())) {
                        continue;
                    }
                    String[] items = line.split(",");
                    if (items.length < 3) {
                        Log.warnOut("AwardSystem", "Invalid line for " + line);
                        continue;
                    } else {
                        String id = items[0];
                        String name = items[1];
                        int point = 1;
                        try {
                            point = Integer.parseInt(items[2]);
                        } catch (Exception e) {
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
            } else {
                Log.warnOut("AwardSystem", "Load empty resource all file");
                return new Award[0];
            }
        } else {
            Log.errOut("AwardSystem", "Cannot load resource all file");
            return new Award[0];
        }
    }
    
    public class AwardHistory {
        public String id;
        
        public List<AwardHistoryItem> items = new ArrayList<AwardHistoryItem>();
    }

    public class AwardHistoryItem {
        private long time;
        private String content;

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
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
