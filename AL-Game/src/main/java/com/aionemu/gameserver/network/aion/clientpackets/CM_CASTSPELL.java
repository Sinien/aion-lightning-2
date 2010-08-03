package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
/**
 * @author alexa026
 * @author rhys2002
 */
public class CM_CASTSPELL extends AionClientPacket
{
	private int					spellid;
	private int					targetType; // 0 - obj id, 1 - point location
    private float 				x, y, z;
	
    @SuppressWarnings("unused")
	private int					targetObjectId;
	@SuppressWarnings("unused")
	private int					time;
	@SuppressWarnings("unused")
	private int					level;
	
	
	/**
	 * Constructs new instance of <tt>CM_CM_REQUEST_DIALOG </tt> packet
	 * @param opcode
	 */
	public CM_CASTSPELL(int opcode)
	{
		super(opcode);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl()
	{
		spellid = readH();
		level = readC();
		
		targetType = readC();
		
		switch(targetType)
		{
			case 0:
				targetObjectId = readD();
				break;
			case 1:
				x = readF();
				y = readF();
				z = readF();
				break;
			default:
				break;
		}
		
		time = readH();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl()
	{
		Player player = getConnection().getActivePlayer();
		
		if(player.isProtectionActive())
		{
			player.getController().stopProtectionActiveTask();
		}
		
		if(!player.getLifeStats().isAlreadyDead())
		{
			SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(spellid);
			if (template == null || template.isPassive())
				return;
			if (!player.getSkillList().isSkillPresent(spellid))
				return;
			player.getController().useSkill(spellid, targetType, x, y, z);
		}
	}
}
