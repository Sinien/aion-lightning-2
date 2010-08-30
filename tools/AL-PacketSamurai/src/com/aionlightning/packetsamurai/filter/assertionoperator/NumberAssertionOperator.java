package com.aionlightning.packetsamurai.filter.assertionoperator;


import com.aionlightning.packetsamurai.filter.value.number.NumberValue;
import com.aionlightning.packetsamurai.parser.DataStructure;

/**
 * 
 * @author Gilles Duboscq
 *
 */
public interface NumberAssertionOperator extends AssertionOperator
{
    public boolean execute(NumberValue value1, NumberValue value2, DataStructure dp);
}