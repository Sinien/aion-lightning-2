package com.aionlightning.packetsamurai.filter.booleanoperator;

import java.util.List;


import com.aionlightning.packetsamurai.filter.Condition;
import com.aionlightning.packetsamurai.parser.DataStructure;


/**
 * 
 * @author Gilles Duboscq
 *
 */
public interface BooleanOperator
{
    public boolean execute(List<Condition> conditions, DataStructure dp);
}