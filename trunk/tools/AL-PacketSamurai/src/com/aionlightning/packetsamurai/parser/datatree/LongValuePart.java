package com.aionlightning.packetsamurai.parser.datatree;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;


import com.aionlightning.packetsamurai.parser.DataStructure.DataPacketMode;
import com.aionlightning.packetsamurai.parser.formattree.Part;


/**
 * 
 * @author Gilles Duboscq
 *
 */
public class LongValuePart extends ValuePart
{
    private long _long;
    
    public LongValuePart(DataTreeNodeContainer parent, Part part)
    {
        super(parent, part);
    }
    
    @Override
    public void parse(ByteBuffer buf)
    {
        if(this.getMode() == DataPacketMode.FORGING)
            throw new IllegalStateException("Can not parse on a Forging mode Data Packet Tree element");
        _long = buf.getLong();
        // sets the raw bytes
        _bytes = new byte[8];
        buf.position(buf.position()-8);
        buf.get(_bytes);
    }
    
    @Override
    public void forge(DataOutput stream) throws IOException
    {
        if(this.getMode() == DataPacketMode.PARSING)
            throw new IllegalStateException("Can not call forge on a Parsing mode Data Packet Tree element");
        stream.writeLong(_long);
    }
    
    public long getLongvalue()
    {
        return _long;
    }
    
    public void setLongValue(long l)
    {
        if(this.getMode() == DataPacketMode.PARSING)
            throw new IllegalStateException("Can not set value on a Parsing mode Data Packet Tree element");
        _long = l;
    }
    
    
    @Override
    public String getValueAsString()
    {
        return String.valueOf(_long);
    }
}