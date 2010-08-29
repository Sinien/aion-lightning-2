/*
 * This file is part of aion-lightning <aion-lightning.com>.
 *
 *  aion-lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-lightning.  If not, see <http://www.gnu.org/licenses/>.
 */
package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.HTMLService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author lord_rex
 * 
 */
public class HTML extends AdminCommand
{
	public HTML()
	{
		super("html");
	}

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.gameserver.utils.chathandlers.AdminCommand#executeCommand(
	 * com.aionemu.gameserver.model.gameobjects.player.Player, java.lang.String[])
	 */
	@Override
	public void executeCommand(Player admin, String[] params)
	{
		HTMLService.pushHTML(admin, "<poll><poll_introduction><![CDATA[<font color='4CB1E5'>Run forest RUN!!!</font>]]></poll_introduction><poll_title><font color='ffc519'></font></poll_title><start_date>2010-08-08 00:00</start_date><end_date>2010-09-14 01:00</end_date><servers></servers><order_num></order_num><race></race><main_class></main_class><world_id></world_id><item_id>164002011</item_id><item_cnt>25</item_cnt><level>1~55</level><questions><question><title><![CDATA[<BR><BR>If you want to survive the war<br>run like your pants are on fire! <br>Damn child... i told you to RUN! <br>RUN FOREST RUN! <br>If you find Gump tell him to bring me beer :)<br><br><br>]]></title><select><input type='radio'>And I ran. I ran so far away...</input></select></question></questions></poll>");
	}

}
