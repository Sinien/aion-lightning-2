package com.aionlightning.packetsamurai.filter.value.number;

import com.aionlightning.packetsamurai.parser.DataStructure;

/**
 * 
 * @author Gilles Duboscq
 *
 */
public class ConstantFloatValue extends FloatNumberValue
{
    private double _value;
    
    public ConstantFloatValue(double val)
    {
        _value = val;
    }

    @Override
    public double getFloatValue(DataStructure dp)
    {
        return _value;
    }
    
}