package com.slimequest.server.game;

import com.slimequest.shared.EventAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/11/16.
 */

public class MapObject extends GameObject {
    public Map map;
    public int x;
    public int y;

    @Override
    public String getType() {
        throw new RuntimeException("Don't use MapObject directly, subclass it.");
    }

    @Override
    public void getEvent(GameNetworkEvent event) {
        final String source;

        if (event.getData().isJsonPrimitive()) {
            source = event.getData().getAsString();
        } else {
            source = EventAttr.getId(event);
        }

        boolean isMe = source != null && source.equals(id);

        if (GameEvent.MOVE.equals(event.getType())) {
            if (isMe) {
                x = EventAttr.getX(event);
                y = EventAttr.getY(event);

                return;
            }

            // XXX TODO Verify that it's ok to move here
        } else if (GameEvent.JOIN.equals(event.getType())) {
        } else if (GameEvent.LEAVE.equals(event.getType())) {
        }  else if (GameEvent.EDIT_TILE.equals(event.getType())) {
            if (isMe) {
                return;
            }
        } else {
            // Unknown event
            return;
        }

        if (channel != null) {
            System.out.println("Sending event: " + event.json());
            channel.writeAndFlush(event.json());
        }
    }
}
