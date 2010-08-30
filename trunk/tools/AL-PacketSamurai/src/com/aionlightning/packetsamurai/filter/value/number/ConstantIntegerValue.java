package com.aionlightning.packetsamurai.filter.value.number;

import com.aionlightning.packetsamurai.parser.DataStructure;

/**
 * 
 * @author Gilles Duboscq
 *
 */
public class ConstantIntegerValue extends IntegerNumberValue
{
    private long _value;
    
    public ConstantIntegerValue(long val)
    {
        _value = val;
    }
    
    @Override
    public long getIntegerValue(DataStructure dp)
    {
        return _value;
    }
    
}