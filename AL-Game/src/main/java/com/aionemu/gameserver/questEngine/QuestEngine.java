/*
 * This file is part of aion-unique <aion-unique.org>.
 *
 * aion-unique is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-unique is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.questEngine;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.aionemu.commons.scripting.scriptmanager.ScriptManager;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.QuestScriptsData;
import com.aionemu.gameserver.dataholders.QuestsData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.NpcQuestData;
import com.aionemu.gameserver.model.templates.quest.QuestDrop;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.model.templates.quest.QuestWorkItems;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.handlers.QuestHandlerLoader;
import com.aionemu.gameserver.questEngine.handlers.models.QuestScriptData;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author MrPoke
 * 
 */
public class QuestEngine
{
	private static final Logger							log						= Logger.getLogger(QuestEngine.class);

	private static final FastMap<Integer, QuestHandler>	questHandlers			= new FastMap<Integer, QuestHandler>();

	private static ScriptManager						scriptManager			= new ScriptManager();

	public static final File							QUEST_DESCRIPTOR_FILE	= new File(
																					"./data/scripts/system/quest_handlers.xml");
	
	private QuestsData									questData = DataManager.QUEST_DATA;

	private QuestScriptsData							questScriptsData = DataManager.QUEST_SCRIPTS_DATA;

	private TIntObjectHashMap<NpcQuestData>				npcQuestData = new TIntObjectHashMap<NpcQuestData>();
	private TIntObjectHashMap<TIntArrayList>			questItemIds= new TIntObjectHashMap<TIntArrayList>();
	private TIntArrayList								questLvlUp = new TIntArrayList();
	private FastMap<ZoneName, TIntArrayList>			questEnterZone= new FastMap<ZoneName, TIntArrayList>();
	private TIntObjectHashMap<TIntArrayList>			questMovieEndIds= new TIntObjectHashMap<TIntArrayList>();
	private TIntArrayList								questOnDie= new TIntArrayList();
	private TIntArrayList								questOnEnterWorld= new TIntArrayList();
	private TIntObjectHashMap<List<QuestDrop>>			questDrop= new TIntObjectHashMap<List<QuestDrop>>();
	private TIntArrayList								questOnQuestFinish= new TIntArrayList();
	private List<Integer>								questOnQuestTimerEnd= new ArrayList<Integer>();
	
	private final NpcQuestData 							emptyNpcQuestData 	= new NpcQuestData();
	
	public static final QuestEngine getInstance()
	{
		return SingletonHolder.instance;
	}

	// Constructor
	private QuestEngine()
	{
		log.info("Initializing QuestEngine");
	}

	public void load()
	{
		for (QuestTemplate data : questData.getQuestsData())
		{
			for (QuestDrop drop : data.getQuestDrop())
			{
				drop.setQuestId(data.getId());
				setQuestDrop(drop.getNpcId()).add(drop);
			}
		}

		scriptManager = new ScriptManager();
		scriptManager.setGlobalClassListener(new QuestHandlerLoader());

		try
		{
			scriptManager.load(QUEST_DESCRIPTOR_FILE);
		}
		catch (Exception e)
		{
			throw new GameServerError("Can't initialize quest handlers.", e);
		}
		for (QuestScriptData data : questScriptsData.getData())
		{
			data.register(this);
		}

		log.info("Loaded " + questHandlers.size() + " quest handler.");
	}

	public void shutdown()
	{
		scriptManager.shutdown();
		clear();
		scriptManager = null;
		log.info("Quests are shutdown...");
	}

	public boolean onDialog(QuestEnv env)
	{
		QuestHandler questHandler = null;
		if(env.getQuestId() != 0)
		{
			questHandler = getQuestHandlerByQuestId(env.getQuestId());
			if(questHandler != null)
				if(questHandler.onDialogEvent(env))
					return true;
		}
		else
		{
			Npc npc = (Npc) env.getVisibleObject();
			for(int questId : getNpcQuestData(npc == null ? 0 : npc.getNpcId()).getOnTalkEvent())
			{
				questHandler = getQuestHandlerByQuestId(questId);
				if(questHandler != null)
					if(questHandler.onDialogEvent(env))
						return true;
			}
		}
		return false;
	}

	public boolean onKill(QuestEnv env)
	{
		Npc npc = (Npc) env.getVisibleObject();
		for(int questId : getNpcQuestData(npc.getNpcId()).getOnKillEvent())
		{
			QuestHandler questHandler = getQuestHandlerByQuestId(questId);
			if(questHandler != null)
				if(questHandler.onKillEvent(env))
					return true;
		}
		return false;
	}

	public boolean onAttack(QuestEnv env)
	{
		Npc npc = (Npc) env.getVisibleObject();
		
		for(int questId : getNpcQuestData(npc.getNpcId()).getOnAttackEvent())
		{
			QuestHandler questHandler = getQuestHandlerByQuestId(questId);
			if(questHandler != null)
				if(questHandler.onAttackEvent(env))
					return true;
		}
		return false;
	}

	public void onLvlUp(QuestEnv env)
	{
		for (int index=0 ; index<questLvlUp.size(); index++)
		{
			QuestHandler questHandler = getQuestHandlerByQuestId(questLvlUp.get(index));
			if(questHandler != null)
				questHandler.onLvlUpEvent(env);
		}
	}

	public void onDie(QuestEnv env)
	{
		for (int index=0 ; index<questOnDie.size(); index++)
		{
			QuestHandler questHandler = getQuestHandlerByQuestId(questOnDie.get(index));
			if(questHandler != null)
				questHandler.onDieEvent(env);
		}
	}
	
	public void onEnterWorld(QuestEnv env)
	{
		for (int index=0 ; index<questOnEnterWorld.size(); index++)
		{
			QuestHandler questHandler = getQuestHandlerByQuestId(questOnEnterWorld.get(index));
			if(questHandler != null)
				questHandler.onEnterWorldEvent(env);
		}
	}

	public boolean onItemUseEvent(QuestEnv env, Item item)
	{
		TIntArrayList lists = getQuestItemIds(item.getItemTemplate().getTemplateId());
		for (int index=0 ; index<lists.size(); index++)
		{
			QuestHandler questHandler = getQuestHandlerByQuestId(lists.get(index));
			if(questHandler != null)
				if(questHandler.onItemUseEvent(env, item))
					return true;
		}
		return false;
	}

	public boolean onEnterZone(QuestEnv env, ZoneName zoneName)
	{
		TIntArrayList lists = getQuestEnterZone(zoneName);
		for (int index=0 ; index<lists.size(); index++)
		{
			QuestHandler questHandler = getQuestHandlerByQuestId(lists.get(index));
			if(questHandler != null)
				if(questHandler.onEnterZoneEvent(env, zoneName))
					return true;
		}
		return false;
	}

	public boolean onMovieEnd(QuestEnv env, int movieId)
	{
		TIntArrayList lists = getQuestMovieEndIds(movieId);
		for (int index=0 ; index<lists.size(); index++)
		{
			env.setQuestId(lists.get(index));
			QuestHandler questHandler = getQuestHandlerByQuestId(env.getQuestId());
			if(questHandler != null)
				if(questHandler.onMovieEndEvent(env, movieId))
					return true;
		}
		return false;
	}
	
	public void onQuestFinish(QuestEnv env)
	{
		for (int index=0 ; index<questOnQuestFinish.size(); index++)
		{
			QuestHandler questHandler = getQuestHandlerByQuestId(questOnQuestFinish.get(index));
			if(questHandler != null)
				questHandler.onQuestFinishEvent(env);
		}
	}
	
	public void onQuestTimerEnd(QuestEnv env)
	{
		for(int questId : questOnQuestTimerEnd)
		{
			QuestHandler questHandler = getQuestHandlerByQuestId(questId);
			if(questHandler != null)
				questHandler.onQuestTimerEndEvent(env);
		}
	}

	public boolean deleteQuest(Player player, int questId)
	{
		if(questData.getQuestById(questId).isCannotGiveup())
			return false;

		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if(qs == null)
			return false;

		qs.setStatus(QuestStatus.NONE);
		
		//remove all worker list item if abandoned
		QuestWorkItems qwi = questData.getQuestById(questId).getQuestWorkItems();
		
		if(qwi != null)
		{
			long count = 0;
			for(QuestItems qi : qwi.getQuestWorkItem())
			{
				if(qi != null)
				{	
					count = player.getInventory().getItemCountByItemId(qi.getItemId());
					if(count > 0)
						player.getInventory().removeFromBagByItemId(qi.getItemId(), count);					
				}
			}
		}
		
		return true;
	}

	public NpcQuestData getNpcQuestData(int npcTemplateId)
	{
		if(npcQuestData.containsKey(npcTemplateId))
		{
			return npcQuestData.get(npcTemplateId);
		}
		return emptyNpcQuestData;
		
	}
	public NpcQuestData setNpcQuestData(int npcTemplateId)
	{
		if(!npcQuestData.containsKey(npcTemplateId))
		{
			npcQuestData.put(npcTemplateId, new NpcQuestData());
		}
		return npcQuestData.get(npcTemplateId);
	}

	public TIntArrayList getQuestItemIds(int itemId)
	{
		if(questItemIds.containsKey(itemId))
		{
			return questItemIds.get(itemId);
		}
		return new TIntArrayList();
	}

	public TIntArrayList setQuestItemIds(int itemId)
	{
		if(!questItemIds.containsKey(itemId))
		{
			questItemIds.put(itemId, new TIntArrayList());
		}
		return questItemIds.get(itemId);
	}

	public List<QuestDrop> setQuestDrop(int npcId)
	{
		if(!questDrop.containsKey(npcId))
		{
			questDrop.put(npcId, new ArrayList<QuestDrop>());
		}
		return questDrop.get(npcId);
	}

	public List<QuestDrop> getQuestDrop(int npcId)
	{
		if(questDrop.containsKey(npcId))
		{
			return questDrop.get(npcId);
		}
		return new ArrayList<QuestDrop>();
	}

	public void addQuestLvlUp(int questId)
	{
		if(!questLvlUp.contains(questId))
			questLvlUp.add(questId);
	}

	public void addOnEnterWorld(int questId)
	{
		if(!questOnEnterWorld.contains(questId))
			questOnEnterWorld.add(questId);
	}

	public void addOnDie(int questId)
	{
		if(!questOnDie.contains(questId))
			questOnDie.add(questId);
	}

	public TIntArrayList getQuestEnterZone(ZoneName zoneName)
	{
		if(questEnterZone.containsKey(zoneName))
		{
			return questEnterZone.get(zoneName);
		}
		return new TIntArrayList();
	}

	public TIntArrayList setQuestEnterZone(ZoneName zoneName)
	{
		if(!questEnterZone.containsKey(zoneName))
		{
			questEnterZone.put(zoneName, new TIntArrayList());
		}
		return questEnterZone.get(zoneName);
	}

	public TIntArrayList getQuestMovieEndIds(int moveId)
	{
		if(questMovieEndIds.containsKey(moveId))
		{
			return questMovieEndIds.get(moveId);
		}
		return new TIntArrayList();
	}

	public TIntArrayList setQuestMovieEndIds(int moveId)
	{
		if(!questMovieEndIds.containsKey(moveId))
		{
			questMovieEndIds.put(moveId, new TIntArrayList());
		}
		return questMovieEndIds.get(moveId);
	}
	
	public void addOnQuestFinish(int questId)
	{
		if(!questOnQuestFinish.contains(questId))
			questOnQuestFinish.add(questId);
	}
	
	public void addOnQuestTimerEnd(int questId)
	{
		if(!questOnQuestTimerEnd.contains(questId))
			questOnQuestTimerEnd.add(questId);
	}
	
	public void clear()
	{
		npcQuestData.clear();
		questItemIds.clear();
		questLvlUp.clear();
		questOnEnterWorld.clear();
		questOnDie.clear();
		questEnterZone.clear();
		questMovieEndIds.clear();
		questDrop.clear();
		questOnQuestFinish.clear();
		questOnQuestTimerEnd.clear();		
		questHandlers.clear();
	}
	
	public void addQuestHandler (QuestHandler questHandler)
	{
		questHandler.register();
		if (questHandlers.containsKey(questHandler.getQuestId()))
			log.warn("Duplicate quest: "+questHandler.getQuestId());
		questHandlers.put(questHandler.getQuestId(), questHandler);
	}
	
	private QuestHandler getQuestHandlerByQuestId(int questId)
	{
		return questHandlers.get(questId);
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final QuestEngine instance = new QuestEngine();
	}
}