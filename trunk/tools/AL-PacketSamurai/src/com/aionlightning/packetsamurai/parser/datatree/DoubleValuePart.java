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
public class DoubleValuePart extends ValuePart
{
    private double _double;

    public DoubleValuePart(DataTreeNodeContainer parent, Part part)
    {
        super(parent, part);
    }
    
    @Override
    public void parse(ByteBuffer buf)
    {
        if(this.getMode() == DataPacketMode.FORGING)
            throw new IllegalStateException("Can not parse on a Forging mode Data Packet Tree element");
        _double = buf.getDouble();
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
        stream.writeDouble(_double);
    }
    
    public void setDoubleValue(double d)
    {
        if(this.getMode() == DataPacketMode.PARSING)
            throw new IllegalStateException("Can not set value on a Parsing mode Data Packet Tree element");
        _double = d;
    }
    
    public double getDoubleValue()
    {
        return _double;
    }
    
    @Override
    public String getValueAsString()
    {
        return String.valueOf(_double);
    }
}