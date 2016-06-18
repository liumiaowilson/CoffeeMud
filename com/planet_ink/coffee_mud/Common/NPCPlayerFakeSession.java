package com.planet_ink.coffee_mud.Common;

public class NPCPlayerFakeSession extends FakeSession {

    @Override
    public String ID() {
        return "NPCPlayerFakeSession";
    }

    @Override
    public boolean isFake() {
        return false;
    }

}
