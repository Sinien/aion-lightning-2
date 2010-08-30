package com.aionlightning.packetsamurai.filter.value.number;

import com.aionlightning.packetsamurai.parser.DataStructure;

/**
 * 
 * @author Gilles Duboscq
 *
 */
public abstract class IntegerNumberValue extends NumberValue
{
    public abstract long getIntegerValue(DataStructure dp);
}