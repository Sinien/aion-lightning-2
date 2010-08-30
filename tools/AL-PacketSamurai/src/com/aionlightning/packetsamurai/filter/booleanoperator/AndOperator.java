package com.aionlightning.packetsamurai.filter.booleanoperator;

import java.util.List;


import com.aionlightning.packetsamurai.filter.Condition;
import com.aionlightning.packetsamurai.parser.DataStructure;


/**
 * 
 * @author Gilles Duboscq
 *
 */
public class AndOperator implements BooleanOperator
{

    public boolean execute(List<Condition> conditions, DataStructure dp)
    {
        for(Condition cond : conditions)
        {
            if(!cond.evaluate(dp))
                return false;
        }
        return true;
    }
    
}