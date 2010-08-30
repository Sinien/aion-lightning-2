package com.aionlightning.packetsamurai.parser.parttypes;

import com.aionlightning.packetsamurai.parser.datatree.DataTreeNodeContainer;
import com.aionlightning.packetsamurai.parser.datatree.IntBCValuePart;
import com.aionlightning.packetsamurai.parser.datatree.ValuePart;
import com.aionlightning.packetsamurai.parser.formattree.Part;

/**
 * @author -Nemesiss-
 *
 */
public class IntBSPartType extends IntPartType
{
	public IntBSPartType(String name, intType type)
	{
		super(name, type);
	}

    @Override
    public ValuePart getValuePart(DataTreeNodeContainer parent, Part part)
    {
        return new IntBCValuePart(parent, part, _type);
    }

}
