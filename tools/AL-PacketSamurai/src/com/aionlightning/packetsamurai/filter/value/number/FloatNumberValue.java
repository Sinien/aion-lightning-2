package com.aionlightning.packetsamurai.filter.value.number;

import com.aionlightning.packetsamurai.parser.DataStructure;

/**
 * 
 * @author Gilles Duboscq
 *
 */
public abstract class FloatNumberValue extends NumberValue
{
    public abstract double getFloatValue(DataStructure dp);
}