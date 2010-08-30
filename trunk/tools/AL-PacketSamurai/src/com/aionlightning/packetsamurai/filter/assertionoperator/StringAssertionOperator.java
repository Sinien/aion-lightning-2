package com.aionlightning.packetsamurai.filter.assertionoperator;


import com.aionlightning.packetsamurai.filter.value.string.StringValue;
import com.aionlightning.packetsamurai.parser.DataStructure;

/**
 * 
 * @author Gilles Duboscq
 *
 */
public interface StringAssertionOperator extends AssertionOperator
{
    public boolean execute(StringValue value1, StringValue value2, DataStructure dp);
}