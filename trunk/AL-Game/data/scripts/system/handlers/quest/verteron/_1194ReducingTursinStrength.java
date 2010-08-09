package quest.verteron;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Balthazar
 */

public class _1194ReducingTursinStrength extends QuestHandler
{
	private final static int	questId	= 1194;

	public _1194ReducingTursinStrength()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(203098).addOnQuestStart(questId);
		qe.setNpcQuestData(203098).addOnTalkEvent(questId);
		qe.setNpcQuestData(210185).addOnKillEvent(questId);
		qe.setNpcQuestData(210186).addOnKillEvent(questId);
		qe.setQuestEnterZone(ZoneName.TURSIN_GARRISON).add(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs == null || qs.getStatus() == QuestStatus.NONE)
		{
			if(targetId == 203098)
			{
				if(env.getDialogId() == 25)
				{
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
				}
				else
					return defaultQuestStartDialog(env);
			}
		}

		if(qs == null)
			return false;

		if(qs.getStatus() == QuestStatus.START)
		{
			switch(targetId)
			{
			}
		}
		else if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203098)
			{
				switch(env.getDialogId())
				{
					case -1:
					{
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
					}
					case 1009:
					{
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
					}
					default:
						return defaultQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName)
	{
		if(zoneName != ZoneName.TURSIN_GARRISON)
			return false;

		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		if(qs == null)
			return false;

		if(qs.getQuestVarById(0) == 0)
		{
			qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
			updateQuestStatus(player, qs);
			return true;
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if(qs == null || qs.getStatus() != QuestStatus.START)
		{
			return false;
		}

		int var = 0;
		int targetId = 0;

		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(targetId == 210185 || targetId == 210186)
		{
			switch(qs.getQuestVarById(0))
			{
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8:
				case 9:
				{
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(player, qs);
					return true;
				}
				case 10:
				{
					if(var == 0)
					{
						var = 1;
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(player, qs);
						return true;
					}
				}
			}
		}
		return false;
	}
}