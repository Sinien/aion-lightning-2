/*
 * This file is part of aion-emu <aion-emu.com>.
 *
 *  aion-emu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-emu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package admincommands;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * Admin moveto command
 * 
 * @author Dwarfpicker
 */

public class GoTo extends AdminCommand
{
	
	/**
	 * Constructor.
	 */
	public GoTo()
	{
		super("goto");
	}

	@Override
	public void executeCommand(Player admin, String[] params)
	{
		if(admin.getAccessLevel() < AdminConfig.COMMAND_GOTO)
		{
			PacketSendUtility.sendMessage(admin, "You dont have enough rights to execute this command!");
			return;
		}
		
		if(params == null || params.length < 1)
		{
			PacketSendUtility.sendMessage(admin, "syntax //goto <location>");
			return;
		}

		if(params[0].toLowerCase().equals("poeta"))
		{
			TeleportService.teleportTo(admin, WorldMapType.POETA.getId(), 806, 1242, 119, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Poeta.");
		}

		else if(params[0].toLowerCase().equals("verteron"))
		{
			TeleportService.teleportTo(admin, WorldMapType.VERTERON.getId(), 1643, 1500, 119, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Verteron.");
		}

		else if(params[0].toLowerCase().equals("eltnen"))
		{
			TeleportService.teleportTo(admin, WorldMapType.ELTNEN.getId(), 343, 2724, 264, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Eltnen.");
		}

		else if(params[0].toLowerCase().equals("theobomos"))
		{
			TeleportService.teleportTo(admin, WorldMapType.THEOMOBOS.getId(), 1398, 1557, 31, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Theobomos.");
		}

		else if(params[0].toLowerCase().equals("heiron"))
		{
			TeleportService.teleportTo(admin, WorldMapType.HEIRON.getId(), 2540, 343, 411, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Heiron.");
		}

		else if(params[0].toLowerCase().equals("sanctum"))
		{
			TeleportService.teleportTo(admin, WorldMapType.SANCTUM.getId(), 1322, 1511, 568, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Sanctum.");
		}

		else if(params[0].toLowerCase().equals("ishalgen"))
		{
			TeleportService.teleportTo(admin, WorldMapType.ISHALGEN.getId(), 529, 2449, 281, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Ishalgen.");
		}

		else if(params[0].toLowerCase().equals("altgard"))
		{
			TeleportService.teleportTo(admin, WorldMapType.ALTGARD.getId(), 1748, 1807, 254, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Altgard.");
		}

		else if(params[0].toLowerCase().equals("morheim"))
		{
			TeleportService.teleportTo(admin, WorldMapType.MORHEIM.getId(), 308, 2274, 449, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Morheim.");
		}

		else if(params[0].toLowerCase().equals("brusthonin"))
		{
			TeleportService.teleportTo(admin, WorldMapType.BRUSTHONIN.getId(), 2917, 2421, 15, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Brusthonin.");
		}

		else if(params[0].toLowerCase().equals("beluslan"))
		{
			TeleportService.teleportTo(admin, WorldMapType.BELUSLAN.getId(), 325, 336, 229, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Beluslan.");
		}

		else if(params[0].toLowerCase().equals("pandaemonium"))
		{
			TeleportService.teleportTo(admin, WorldMapType.PANDAEMONIUM.getId(), 1679, 1400, 195, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Pandaemonium.");
		}

		else if(params[0].toLowerCase().equals("abyss1"))
		{
			TeleportService.teleportTo(admin, 400010000, 2867, 1034, 1528, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Latis Plazza bottom Elyos.");
		}

		else if(params[0].toLowerCase().equals("abyss2"))
		{
			TeleportService.teleportTo(admin, 400010000, 1078, 2839, 1636, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Russet Plazza bottom Asmodians.");
		}

		else if(params[0].toLowerCase().equals("abyss3"))
		{
			TeleportService.teleportTo(admin, 400010000, 1596, 2952, 2943, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Top Asmodians.");
		}

		else if(params[0].toLowerCase().equals("abyss4"))
		{
			TeleportService.teleportTo(admin, 400010000, 2054, 660, 2843, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Top Elyos.");
		}

		else if(params[0].toLowerCase().equals("abyssfortress"))
		{
			TeleportService.teleportTo(admin, 400010000, 2130, 1925, 2322, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Core Fortress.");
		}

		else if(params[0].toLowerCase().equals("senza"))
		{
			TeleportService.teleportTo(admin, 300010000, 270, 200, 206, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Senza Nome.");
		}

		else if(params[0].toLowerCase().equals("karamatis1"))
		{
			TeleportService.teleportTo(admin, 300010000, 270, 200, 206, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Karamatis 1.");
		}

		else if(params[0].toLowerCase().equals("karamatis2"))
		{
			TeleportService.teleportTo(admin, 300010000, 270, 200, 206, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Karamatis 2.");
		}

		else if(params[0].toLowerCase().equals("aerdina"))
		{
			TeleportService.teleportTo(admin, 300010000, 270, 200, 206, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Aerdina.");
		}

		else if(params[0].toLowerCase().equals("gerania"))
		{
			TeleportService.teleportTo(admin, 300010000, 270, 200, 206, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Gerania.");
		}

		else if(params[0].toLowerCase().equals("lepharist"))
		{
			TeleportService.teleportTo(admin, 310050000, 225, 244, 133, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Lerpharist Secret Labratory.");
		}

		else if(params[0].toLowerCase().equals("fragment"))
		{
			TeleportService.teleportTo(admin, 310070000, 247, 249, 1392, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Fragment of Darkness.");
		}

		else if(params[0].toLowerCase().equals("sanctumarena"))
		{		
			TeleportService.teleportTo(admin, 310080000, 275, 242, 159, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Sanctum Underground Arena.");
		}

		else if(params[0].toLowerCase().equals("idratu"))
		{
			TeleportService.teleportTo(admin, 310090000, 562, 335, 1015, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Idratu Fortress.");
		}

		else if(params[0].toLowerCase().equals("azoturan"))
		{
			TeleportService.teleportTo(admin, 310100000, 458, 428, 1039, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Azoturan.");
		}

		else if(params[0].toLowerCase().equals("ataxiar1"))
		{
			TeleportService.teleportTo(admin, 320010000, 229, 237, 206, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Narsass 1.");
		}

		else if(params[0].toLowerCase().equals("ataxiar2"))
		{
			TeleportService.teleportTo(admin, 320020000, 229, 237, 206, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Narsass 2.");
		}

		else if(params[0].toLowerCase().equals("bregirun"))
		{
			TeleportService.teleportTo(admin, 320030000, 264, 214, 210, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Bregirun.");
		}

		else if(params[0].toLowerCase().equals("nidalber"))
		{
			TeleportService.teleportTo(admin, 320040000, 264, 214, 210, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Nidalber.");
		}

		else if(params[0].toLowerCase().equals("skytemple"))
		{
			TeleportService.teleportTo(admin, 320050000, 177, 229, 536, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Inside of the Sky Temple of Arkanis.");
		}

		else if(params[0].toLowerCase().equals("space"))
		{
			TeleportService.teleportTo(admin, 320070000, 246, 246, 125, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Space of Destiny.");
		}

		else if(params[0].toLowerCase().equals("trinielarena"))
		{
			TeleportService.teleportTo(admin, 320090000, 275, 239, 159, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Triniel Underground Arena.");
		}

		else if(params[0].toLowerCase().equals("firetemple"))
		{
			TeleportService.teleportTo(admin, 320100000, 144, 312, 123, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Fire Temple.");
		}

		else if(params[0].toLowerCase().equals("reshanta"))
		{
			TeleportService.teleportTo(admin, 400010000, 951, 936, 1667, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Reshanta.");
		}

		else if(params[0].toLowerCase().equals("prison1"))
		{
			TeleportService.teleportTo(admin, 510010000, 256, 256, 49, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to LF Prison.");
		}

		else if(params[0].toLowerCase().equals("prison2"))
		{
			TeleportService.teleportTo(admin, 520010000, 256, 256, 49, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to DF Prison.");
		}

		else if(params[0].toLowerCase().equals("test1"))
		{
			TeleportService.teleportTo(admin, 900100000, 196, 187, 20, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Test Giant Monster.");
		}

		else if(params[0].toLowerCase().equals("test2"))
		{
			TeleportService.teleportTo(admin, 900020000, 144, 136, 20, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Test Basic.");
		}

		else if(params[0].toLowerCase().equals("test3"))
		{			
			TeleportService.teleportTo(admin, 900030000, 228, 171, 49, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Test Server.");
		}
		else if(params[0].toLowerCase().equals("steelrake"))
		{
			TeleportService.teleportTo(admin, 300100000, 237, 505, 949, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Steel Rake.");
		}
		/*
		else if(params[0].toLowerCase().equals("inggison"))
		{
			TeleportService.teleportTo(admin, 210050000, 1335, 276, 590, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Inggison.");
		}
		else if(params[0].toLowerCase().equals("gelkmaros"))
		{
			TeleportService.teleportTo(admin, 220070000, 795, 1684, 362, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Gelkmaros.");
		}
		*/
		else if(params[0].toLowerCase().equals("kaisinel"))
		{
			TeleportService.teleportTo(admin, 110020000, 2155, 1567, 1205, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Kaisinel.");
		}
		else if(params[0].toLowerCase().equals("marchutan"))
		{
			TeleportService.teleportTo(admin, 120020000, 1557, 1429, 266, 0);
			PacketSendUtility.sendMessage(admin, "Teleported to Marchutan.");
		}
		else 
			PacketSendUtility.sendMessage(admin, "Target location was not found!");
	}
}
