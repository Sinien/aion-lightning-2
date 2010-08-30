package com.aionlightning.packetsamurai.parser.parttypes;


import com.aionlightning.packetsamurai.parser.PartType;
import com.aionlightning.packetsamurai.parser.datatree.DataTreeNodeContainer;
import com.aionlightning.packetsamurai.parser.datatree.ValuePart;
import com.aionlightning.packetsamurai.parser.formattree.Part;

/**
 * 
 * @author Gilles Duboscq
 *
 */
public class RawPartType extends PartType
{
    public RawPartType(String name)
    {
        super(name);
    }

    @Override
    public ValuePart getValuePart(DataTreeNodeContainer parent, Part part)
    {
        return new ValuePart(parent, part);
    }

    @Override
    public boolean isBlockType()
    {
        return false;
    }

    @Override
    public boolean isReadableType()
    {
        return true;
    }

    @Override
    public int getTypeByteNumber()
    {
        return 0;
    }

}
