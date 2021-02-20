package com.kosmx.emotes.common.network.objects;

import com.kosmx.emotes.common.CommonData;
import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.common.network.CommonNetwork;
import com.kosmx.emotes.common.tools.Ease;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * It should be placed into emotecraftCommon but it has too many references to minecraft codes...
 */
public class EmoteDataPacket extends AbstractNetworkPacket {
    protected boolean valid = true;
    private int version;
    public int tick = 0;


    public EmoteDataPacket(){
    }

    @Override
    public void write(ByteBuffer buf, NetData config){
        this.version = Math.min(config.versions.get(getVer()), CommonData.networkingVersion);
        EmoteData emote = config.emoteData;
        buf.putInt(config.tick);
        buf.putInt(emote.beginTick);
        buf.putInt(emote.endTick);
        buf.putInt(emote.stopTick);
        putBoolean(buf, emote.isInfinite);
        putBoolean(buf, emote.isInfinite);
        buf.putInt(emote.returnToTick);
        putBoolean(buf, emote.isEasingBefore);
        writeBodyPartInfo(buf, emote.head, false, emote);
        writeBodyPartInfo(buf, emote.torso, true, emote);
        writeBodyPartInfo(buf, emote.rightArm, true, emote);
        writeBodyPartInfo(buf, emote.leftArm, true, emote);
        writeBodyPartInfo(buf, emote.rightLeg, true, emote);
        writeBodyPartInfo(buf, emote.leftLeg, true, emote);

    }

    private void writeBodyPartInfo(ByteBuffer buf, EmoteData.StateCollection part, boolean bending, EmoteData emoteData){
        writePartInfo(buf, part.x, emoteData);
        writePartInfo(buf, part.y, emoteData);
        writePartInfo(buf, part.z, emoteData);
        writePartInfo(buf, part.pitch, emoteData);
        writePartInfo(buf, part.yaw, emoteData);
        writePartInfo(buf, part.roll, emoteData);
        if(bending) {
            writePartInfo(buf, part.bendDirection, emoteData);
            writePartInfo(buf, part.bend, emoteData);
        }
    }

    private void writePartInfo(ByteBuffer buf, EmoteData.StateCollection.State part, EmoteData emoteData){
        List<EmoteData.KeyFrame> list = part.keyFrames;
        buf.putInt(list.size());
        for(EmoteData.KeyFrame move : list){
            buf.putInt(move.tick);
            buf.putFloat(move.value);
            buf.put(move.ease.getId());
        }
    }

    @Override
    public boolean read(ByteBuffer buf, NetData config, int version){
        this.version = version;
        EmoteData.EmoteBuilder builder = new EmoteData.EmoteBuilder();
        builder.validationThreshold = config.threshold;
        builder.beginTick = buf.getInt();
        builder.endTick = buf.getInt();
        builder.stopTick = buf.getInt();
        builder.isLooped = getBoolean(buf);
        builder.returnTick = buf.getInt();
        builder.isEasingBefore = getBoolean(buf);
        getBodyPartInfo(buf, builder.head, false);
        getBodyPartInfo(buf, builder.torso, true);
        getBodyPartInfo(buf, builder.rightArm, true);
        getBodyPartInfo(buf, builder.leftArm, true);
        getBodyPartInfo(buf, builder.rightLeg, true);
        getBodyPartInfo(buf, builder.leftLeg, true);

        EmoteData emote = builder.build();
        valid = valid && emote.beginTick >= 0 && emote.beginTick < emote.endTick && (! emote.isInfinite || emote.returnToTick <= emote.endTick && emote.returnToTick >= 0);

        config.emoteData = emote;
        config.valid = valid;

        return true;
    }

    private void getBodyPartInfo(ByteBuffer buf, EmoteData.StateCollection part, boolean bending){
        getPartInfo(buf, part.x);
        getPartInfo(buf, part.y);
        getPartInfo(buf, part.z);
        getPartInfo(buf, part.pitch);
        getPartInfo(buf, part.yaw);
        getPartInfo(buf, part.roll);
        if(bending) {
            getPartInfo(buf, part.bendDirection);
            getPartInfo(buf, part.bend);
        }
    }

    private void getPartInfo(ByteBuffer buf, EmoteData.StateCollection.State part){
        int len = buf.getInt();
        for(int i = 0; i < len; i++){
            if(! part.addKeyFrame(buf.getInt(), buf.getFloat(), Ease.getEase(buf.get()))){
                this.valid = false;
            }
        }
    }

    @Override
    public byte getID() {
        return 0;
    }

    @Override
    public byte getVer() {
        return 0;
    }

    @Override
    public boolean doWrite(NetData data) {
        return data.emoteData != null && data.stopEmoteID == null;
    }

    /*
    Data types in comment:
    I int, 4 bytes
    L Long 8 bytes (1 uuid = 2 L)
    B byte, ...1 byte
    F float, 4 bytes
     */
    @Override
    public int calculateSize(NetData config) {
        if(config.emoteData == null)return 0;
        //I will create less efficient loops but these will be more easily fixable
        int size = 30;//The header makes 46 bytes IIIIBBIB
        size += partSize(config.emoteData.head);
        size += partSize(config.emoteData.torso);
        size += partSize(config.emoteData.rightArm);
        size += partSize(config.emoteData.leftArm);
        size += partSize(config.emoteData.rightLeg);
        size += partSize(config.emoteData.leftLeg);
        //The size of an empty emote is 230 bytes.
        //but that makes the size to be 230 + keyframes count*9 bytes.
        //46 axis, including bends for every body-part except head.
        return size;
    }

    int partSize(EmoteData.StateCollection part){
        int size = 0;
        size += axisSize(part.x);
        size += axisSize(part.y);
        size += axisSize(part.z);
        size += axisSize(part.pitch);
        size += axisSize(part.yaw);
        size += axisSize(part.roll);
        if(part.isBendable) {
            size += axisSize(part.bend);
            size += axisSize(part.bendDirection);
        }
        return size;
    }
    int axisSize(EmoteData.StateCollection.State axis){
        return axis.keyFrames.size()*9 + 4;// count*IFB + I (for count)
    }
}