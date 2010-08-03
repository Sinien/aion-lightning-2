/*
 * This file is part of aion-unique <aion-unique.org>.
 *
 *  aion-unique is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-unique is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.model.gameobjects.player;

import com.aionemu.gameserver.configs.main.PricesConfig;

/**
 * @author Sarynth
 *
 * Used to get prices for the player.
 *  - Packets: SM_PRICES, SM_TRADELIST, SM_SELL_ITEM
 *  - Services: Godstone socket, teleporter, other fees.
 * TODO: Add Player owner; value and check for PremiumRates or faction price influence.
 */
public class Prices
{
	
	/**
	 * 
	 */
	public Prices()
	{
		
	}
	
	/**
	 * Used in SM_PRICES
	 * @return buyingPrice
	 */
	public int getGlobalPrices()
	{
		return PricesConfig.DEFAULT_PRICES;
	}

	/**
	 * Used in SM_PRICES
	 * @return
	 */
	public int getGlobalPricesModifier()
	{
		return PricesConfig.DEFAULT_MODIFIER;
	}

	/**
	 * Used in SM_PRICES
	 * @return taxes
	 */
	public int getTaxes()
	{
		return PricesConfig.DEFAULT_TAXES;
	}
	
	/**
	 * Used in SM_TRADELIST.
	 * @return buyPriceModifier
	 */
	public int getVendorBuyModifier()
	{
		return PricesConfig.VENDOR_BUY_MODIFIER;
	}

	/**
	 * Used in SM_SELL_ITEM
	 *  - Can be unique per NPC!
	 * @return sellingModifier
	 */
	public int getVendorSellModifier()
	{
		return (int)((int)((int)(PricesConfig.VENDOR_SELL_MODIFIER *
			this.getGlobalPrices() / 100F) *
			this.getGlobalPricesModifier() / 100F) *
			this.getTaxes() / 100F);
	}

	/**
	 * @param basePrice
	 * @return modifiedPrice
	 */
	public long getPriceForService(long basePrice)
	{
		// Tricky. Requires multiplication by Prices, Modifier, Taxes
		// In order, and round down each time to match client calculation.
		return (long)((long)((long)(basePrice *
			this.getGlobalPrices() / 100D) *
			this.getGlobalPricesModifier() / 100D) *
			this.getTaxes() / 100D);
	}

	/**
	 * @param requiredKinah
	 * @return modified requiredKinah
	 */
	public long getKinahForBuy(long requiredKinah)
	{
		// Requires double precision for 2mil+ kinah items
		return (long)((long)((long)((long)(requiredKinah *
			this.getVendorBuyModifier() / 100.0D) *
			this.getGlobalPrices() / 100.0D) *
			this.getGlobalPricesModifier() / 100.0D) *
			this.getTaxes() / 100.0D);
	}

	/**
	 * @param kinahReward
	 * @return
	 */
	public long getKinahForSell(long kinahReward)
	{
		return (long)(kinahReward * this.getVendorSellModifier() / 100D);
	}

}
