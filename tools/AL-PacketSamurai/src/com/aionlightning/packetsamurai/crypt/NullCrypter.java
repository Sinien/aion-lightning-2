package com.aionlightning.packetsamurai.crypt;


import com.aionlightning.packetsamurai.protocol.Protocol;
import com.aionlightning.packetsamurai.protocol.protocoltree.PacketFamilly.packetDirection;

/**
 * 
 * @author Gilles Duboscq
 *
 */
public class NullCrypter implements ProtocolCrypter
{

	public boolean decrypt(byte[] raw, packetDirection dir)
	{
		return true;
	}

    public void setProtocol(Protocol protocol)
    {
        
    }
}