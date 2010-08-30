package com.aionlightning.packetsamurai.filter.value.string;


import com.aionlightning.packetsamurai.filter.value.Value;
import com.aionlightning.packetsamurai.parser.DataStructure;

/**
 * 
 * @author Gilles Duboscq
 *
 */
public abstract class StringValue extends Value
{
    public abstract String getStringValue(DataStructure dp);
}