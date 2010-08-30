package com.aionlightning.packetsamurai.filter.value.number;


import com.aionlightning.packetsamurai.filter.value.Value;
import com.aionlightning.packetsamurai.parser.DataStructure;

/**
 * 
 * @author Gilles Duboscq
 *
 */
public class EvaluableIntegerValue extends IntegerNumberValue
{
    private String _accessStr;
    public EvaluableIntegerValue(String str)
    {
        _accessStr = str;
    }

    @Override
    public long getIntegerValue(DataStructure dp)
    {
        Object obj = Value.getObjectFromAccessString(_accessStr);
        if(!(obj instanceof Long))
            throw new IllegalStateException("Malformed filter, the expression doesnt return an integer for an EvaluableIntegerValue.");
        return (Long) obj;
    }
    
}