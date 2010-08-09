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
package com.aionemu.gameserver.services;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.CacheConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.controllers.FlyController;
import com.aionemu.gameserver.controllers.PlayerController;
import com.aionemu.gameserver.controllers.ReviveController;
import com.aionemu.gameserver.controllers.SummonController.UnsummonType;
import com.aionemu.gameserver.controllers.effect.PlayerEffectController;
import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.dao.BlockListDAO;
import com.aionemu.gameserver.dao.FriendListDAO;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.ItemCooldownsDAO;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dao.MailDAO;
import com.aionemu.gameserver.dao.PlayerAppearanceDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dao.PlayerEffectsDAO;
import com.aionemu.gameserver.dao.PlayerLifeStatsDAO;
import com.aionemu.gameserver.dao.PlayerMacrossesDAO;
import com.aionemu.gameserver.dao.PlayerPunishmentsDAO;
import com.aionemu.gameserver.dao.PlayerQuestListDAO;
import com.aionemu.gameserver.dao.PlayerRecipesDAO;
import com.aionemu.gameserver.dao.PlayerSettingsDAO;
import com.aionemu.gameserver.dao.PlayerSkillListDAO;
import com.aionemu.gameserver.dao.PlayerTitleListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PlayerInitialData;
import com.aionemu.gameserver.dataholders.PlayerStatsData;
import com.aionemu.gameserver.dataholders.PlayerInitialData.LocationData;
import com.aionemu.gameserver.dataholders.PlayerInitialData.PlayerCreationData;
import com.aionemu.gameserver.dataholders.PlayerInitialData.PlayerCreationData.ItemType;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.MacroList;
import com.aionemu.gameserver.model.gameobjects.player.Mailbox;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Storage;
import com.aionemu.gameserver.model.gameobjects.player.StorageType;
import com.aionemu.gameserver.model.gameobjects.player.FriendList.Status;
import com.aionemu.gameserver.model.gameobjects.stats.PlayerGameStats;
import com.aionemu.gameserver.model.gameobjects.stats.PlayerLifeStats;
import com.aionemu.gameserver.model.gameobjects.stats.listeners.TitleChangeListener;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.legion.LegionMember;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.clientpackets.CM_ENTER_WORLD;
import com.aionemu.gameserver.network.aion.clientpackets.CM_QUIT;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.collections.cachemap.CacheMap;
import com.aionemu.gameserver.utils.collections.cachemap.CacheMapFactory;
import com.aionemu.gameserver.world.KnownList;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * 
 * This class is designed to do all the work related with loading/storing players.<br>
 * Same with storing, {@link #storePlayer(com.aionemu.gameserver.model.gameobjects.player.Player)} stores all player
 * data like appearance, items, etc...
 * 
 * @author SoulKeeper, Saelya
 */
public class PlayerService
{
	private static final Logger			log			= Logger.getLogger(PlayerService.class);
	private static final CacheMap<Integer, Player>	playerCache	= CacheMapFactory.createSoftCacheMap("Player", "player");


	/**
	 * Checks if name is already taken or not
	 * 
	 * @param name
	 *            character name
	 * @return true if is free, false in other case
	 */
	public static boolean isFreeName(String name)
	{
		return !DAOManager.getDAO(PlayerDAO.class).isNameUsed(name);
	}

	/**
	 * Checks if a name is valid. It should contain only english letters
	 * 
	 * @param name
	 *            character name
	 * @return true if name is valid, false overwise
	 */
	public static boolean isValidName(String name)
	{
		return GSConfig.CHAR_NAME_PATTERN.matcher(name).matches();
	}

	/**
	 * Stores newly created player
	 * 
	 * @param player
	 *            player to store
	 * @return true if character was successful saved.
	 */
	public static boolean storeNewPlayer(Player player, String accountName, int accountId)
	{
		return DAOManager.getDAO(PlayerDAO.class).saveNewPlayer(player.getCommonData(), accountId, accountName)
			&& DAOManager.getDAO(PlayerAppearanceDAO.class).store(player)
			&& DAOManager.getDAO(PlayerSkillListDAO.class).storeSkills(player)
			&& DAOManager.getDAO(InventoryDAO.class).store(player)
			&& DAOManager.getDAO(PlayerTitleListDAO.class).storeTitles(player);
	}

	/**
	 * Stores player data into db
	 * 
	 * @param player
	 */
	private static void storePlayer(Player player)
	{
		DAOManager.getDAO(PlayerDAO.class).storePlayer(player);
		DAOManager.getDAO(PlayerSkillListDAO.class).storeSkills(player);
		DAOManager.getDAO(PlayerSettingsDAO.class).saveSettings(player);
		DAOManager.getDAO(PlayerQuestListDAO.class).store(player);
		DAOManager.getDAO(PlayerTitleListDAO.class).storeTitles(player);
		DAOManager.getDAO(AbyssRankDAO.class).storeAbyssRank(player);
		DAOManager.getDAO(PlayerPunishmentsDAO.class).storePlayerPunishments(player);
		DAOManager.getDAO(InventoryDAO.class).store(player);
		DAOManager.getDAO(ItemStoneListDAO.class).save(player);
		DAOManager.getDAO(MailDAO.class).storeMailbox(player);
	}

	/**
	 * Returns the player with given objId (if such player exists)
	 * 
	 * @param playerObjId
	 * @param account 
	 * @return Player
	 */
	public static Player getPlayer(int playerObjId, Account account)
	{
		Player player = playerCache.get(playerObjId);
		if(player != null)
			return player;
		
		/**
		 * Player common data and appearance should be already loaded in account
		 */
		
		PlayerAccountData playerAccountData = account.getPlayerAccountData(playerObjId);
		PlayerCommonData pcd = playerAccountData.getPlayerCommonData();
		PlayerAppearance appearance = playerAccountData.getAppereance();

		player = new Player(new PlayerController(), pcd, appearance);		
		
		LegionMember legionMember = LegionService.getInstance().getLegionMember(player.getObjectId());
		if(legionMember != null)
			player.setLegionMember(legionMember);

		if(GroupService.getInstance().isGroupMember(playerObjId))
			GroupService.getInstance().setGroup(player);
		
		if (AllianceService.getInstance().isAllianceMember(playerObjId))
			AllianceService.getInstance().setAlliance(player);
		
		MacroList macroses = DAOManager.getDAO(PlayerMacrossesDAO.class).restoreMacrosses(playerObjId);
		player.setMacroList(macroses);

		player.setSkillList(DAOManager.getDAO(PlayerSkillListDAO.class).loadSkillList(playerObjId));
		player.setKnownlist(new KnownList(player));
		player.setFriendList(DAOManager.getDAO(FriendListDAO.class).load(player));
		player.setBlockList(DAOManager.getDAO(BlockListDAO.class).load(player));
		player.setTitleList(DAOManager.getDAO(PlayerTitleListDAO.class).loadTitleList(playerObjId));

		DAOManager.getDAO(PlayerSettingsDAO.class).loadSettings(player);
		DAOManager.getDAO(AbyssRankDAO.class).loadAbyssRank(player);
		PlayerStatsData playerStatsData = DataManager.PLAYER_STATS_DATA;
		player.setPlayerStatsTemplate(playerStatsData.getTemplate(player));

		player.setGameStats(new PlayerGameStats(playerStatsData, player));

		Equipment equipment = DAOManager.getDAO(InventoryDAO.class).loadEquipment(player);
		ItemService.loadItemStones(equipment.getEquippedItemsWithoutStigma());
		equipment.setOwner(player);
		player.setEquipment(equipment);

		player.setLifeStats(new PlayerLifeStats(player));
		player.setEffectController(new PlayerEffectController(player));
		player.setFlyController(new FlyController(player));
		player.setReviveController(new ReviveController(player));
		
		player.setQuestStateList(DAOManager.getDAO(PlayerQuestListDAO.class).load(player));
		player.setRecipeList(DAOManager.getDAO(PlayerRecipesDAO.class).load(player.getObjectId()));
		
		/**
		 * Account warehouse should be already loaded in account
		 */
		Storage accWarehouse = account.getAccountWarehouse();
		
		player.setStorage(accWarehouse, StorageType.ACCOUNT_WAREHOUSE);
		
		Storage inventory = DAOManager.getDAO(InventoryDAO.class).loadStorage(player, StorageType.CUBE);
		ItemService.loadItemStones(inventory.getStorageItems());

		player.setStorage(inventory, StorageType.CUBE);
		
		Storage warehouse = DAOManager.getDAO(InventoryDAO.class).loadStorage(player, StorageType.REGULAR_WAREHOUSE);
		ItemService.loadItemStones(warehouse.getStorageItems());

		player.setStorage(warehouse, StorageType.REGULAR_WAREHOUSE);
		
		/**
		 * Apply equipment stats (items and manastones were loaded in account) 
		 */
		player.getEquipment().onLoadApplyEquipmentStats();
		
		DAOManager.getDAO(PlayerPunishmentsDAO.class).loadPlayerPunishments(player);

		ItemService.restoreKinah(player);

		// update passive stats after effect controller, stats and equipment are initialized
		player.getController().updatePassiveStats();
		// load saved effects
		DAOManager.getDAO(PlayerEffectsDAO.class).loadPlayerEffects(player);
		// load item cooldowns
		DAOManager.getDAO(ItemCooldownsDAO.class).loadItemCooldowns(player);

		if(player.getCommonData().getTitleId() > 0)
		{
			TitleChangeListener.onTitleChange(player.getGameStats(), player.getCommonData().getTitleId(), true);
		}
		player.getGameStats().recomputeStats();
		
		DAOManager.getDAO(PlayerLifeStatsDAO.class).loadPlayerLifeStat(player);
		//analyze current instance
		InstanceService.onPlayerLogin(player);
		
		if(CacheConfig.CACHE_PLAYERS)
			playerCache.put(playerObjId, player);

		return player;
	}

	/**
	 * This method is used for creating new players
	 * 
	 * @param playerCommonData
	 * @param playerAppearance
	 * @return Player
	 */
	public static Player newPlayer(PlayerCommonData playerCommonData, PlayerAppearance playerAppearance)
	{
		PlayerInitialData playerInitialData = DataManager.PLAYER_INITIAL_DATA;
		LocationData ld = playerInitialData.getSpawnLocation(playerCommonData.getRace());

		WorldPosition position = World.getInstance().createPosition(ld.getMapId(), ld.getX(), ld.getY(), ld.getZ(), ld.getHeading());
		playerCommonData.setPosition(position);

		Player newPlayer = new Player(new PlayerController(), playerCommonData, playerAppearance);

		// Starting skills
		SkillLearnService.addNewSkills(newPlayer, true);

		// Starting items
		PlayerCreationData playerCreationData = playerInitialData.getPlayerCreationData(playerCommonData
			.getPlayerClass());

		List<ItemType> items = playerCreationData.getItems();

		Storage playerInventory = new Storage(newPlayer, StorageType.CUBE);
		Storage regularWarehouse = new Storage(newPlayer, StorageType.REGULAR_WAREHOUSE);
		Storage accountWarehouse = new Storage(newPlayer, StorageType.ACCOUNT_WAREHOUSE);
		
		Equipment equipment = new Equipment(newPlayer);
		newPlayer.setStorage(playerInventory, StorageType.CUBE);
		newPlayer.setStorage(regularWarehouse, StorageType.REGULAR_WAREHOUSE);
		newPlayer.setStorage(accountWarehouse, StorageType.ACCOUNT_WAREHOUSE);
		newPlayer.setEquipment(equipment);
		newPlayer.setMailbox(new Mailbox());

		for(ItemType itemType : items)
		{
			int itemId = itemType.getTemplate().getTemplateId();
			Item item = ItemService.newItem(itemId, itemType.getCount());
			if(item == null)
				continue;

			// When creating new player - all equipment that has slot values will be equipped
			// Make sure you will not put into xml file more items than possible to equip.
			ItemTemplate itemTemplate = item.getItemTemplate();

			if(itemTemplate.isArmor() || itemTemplate.isWeapon())
			{
				item.setEquipped(true);
				List<ItemSlot> itemSlots = ItemSlot.getSlotsFor(itemTemplate.getItemSlot());
				item.setEquipmentSlot(itemSlots.get(0).getSlotIdMask());
				equipment.onLoadHandler(item);
			}
			else
				playerInventory.onLoadHandler(item);
		}
		equipment.onLoadApplyEquipmentStats();
		/**
		 * Mark inventory and equipment as UPDATE_REQUIRED to be saved during 
		 * character creation
		 */
		playerInventory.setPersistentState(PersistentState.UPDATE_REQUIRED);
		equipment.setPersistentState(PersistentState.UPDATE_REQUIRED);
		return newPlayer;
	}

	/**
	 * This method is called just after player logged in to the game.<br>
	 * <br>
	 * <b><font color='red'>NOTICE: </font> This method called only from {@link CM_ENTER_WORLD} and must not be called
	 * from anywhere else.</b>
	 * 
	 * @param player
	 */
	public static void playerLoggedIn(Player player)
	{
		log.info("Player logged in: " + player.getName() + " Account: " + player.getClientConnection().getAccount().getName());
		player.getCommonData().setOnline(true);
		DAOManager.getDAO(PlayerDAO.class).onlinePlayer(player, true);
		player.getFriendList().setStatus(Status.ONLINE);
		player.onLoggedIn();
	}

	/**
	 * This method is called when player leaves the game, which includes just two cases: either player goes back to char
	 * selection screen or it's leaving the game [closing client].<br>
	 * <br>
	 * 
	 * <b><font color='red'>NOTICE: </font> This method is called only from {@link AionConnection} and {@link CM_QUIT}
	 * and must not be called from anywhere else</b>
	 * 
	 * @param player
	 */
	public static void playerLoggedOut(final Player player)
	{
		log.info("Player logged out: " + player.getName() + " Account: " + player.getClientConnection().getAccount().getName());
		player.onLoggedOut();
		
		// Update prison timer
		if (player.isInPrison())
		{
			long prisonTimer = System.currentTimeMillis() - player.getStartPrison();
			prisonTimer = player.getPrisonTimer() - prisonTimer;
			
			player.setPrisonTimer(prisonTimer);
			
			log.debug("Update prison timer to " + prisonTimer/1000 + " seconds !");
		}
		
		//store current effects
		DAOManager.getDAO(PlayerEffectsDAO.class).storePlayerEffects(player);
		DAOManager.getDAO(ItemCooldownsDAO.class).storeItemCooldowns(player);
		DAOManager.getDAO(PlayerLifeStatsDAO.class).updatePlayerLifeStat(player);
		player.getEffectController().removeAllEffects();
		
		player.getLifeStats().cancelAllTasks();
		
		if(player.getLifeStats().isAlreadyDead())
			TeleportService.moveToBindLocation(player, false);

		if(DuelService.getInstance().isDueling(player.getObjectId()))
			DuelService.getInstance().loseDuel(player);
		
		if(player.getSummon() != null)
			player.getSummon().getController().release(UnsummonType.LOGOUT);

		PunishmentService.stopPrisonTask(player, true);

		player.getCommonData().setOnline(false);
		player.getCommonData().setLastOnline(new Timestamp(System.currentTimeMillis()));
		
		player.setClientConnection(null);

		if(player.isLegionMember())
			LegionService.getInstance().onLogout(player);

		if(player.isInGroup())
			GroupService.getInstance().scheduleRemove(player);
		
		if (player.isInAlliance())
			AllianceService.getInstance().onLogout(player);
		
		player.getController().delete();
		DAOManager.getDAO(PlayerDAO.class).onlinePlayer(player, false);
		
		if(!GSConfig.DISABLE_CHAT_SERVER)
			ChatService.onPlayerLogout(player);

		storePlayer(player);
		
		player.getEquipment().setOwner(null);
		player.getInventory().setOwner(null);
		player.getWarehouse().setOwner(null);
	}

	public static void playerLoggedOutDelay(final Player player, int delay)
	{
		// force stop movement of player
		player.getController().stopMoving();
		
		ThreadPoolManager.getInstance().scheduleTaskManager(new Runnable(){
			@Override
			public void run()
			{
				playerLoggedOut(player);
			}
		}, delay);
	}

	/**
	 * Cancel Player deletion process if its possible.
	 * 
	 * @param accData
	 *            PlayerAccountData
	 * 
	 * @return True if deletion was successful canceled.
	 */
	public static boolean cancelPlayerDeletion(PlayerAccountData accData)
	{
		if(accData.getDeletionDate() == null)
			return true;

		if(accData.getDeletionDate().getTime() > System.currentTimeMillis())
		{
			accData.setDeletionDate(null);
			storeDeletionTime(accData);
			return true;
		}
		return false;
	}

	/**
	 * Starts player deletion process if its possible. If deletion is possible character should be deleted after 5
	 * minutes.
	 * 
	 * @param accData
	 *            PlayerAccountData
	 */
	public static void deletePlayer(PlayerAccountData accData)
	{
		if(accData.getDeletionDate() != null)
			return;

		accData.setDeletionDate(new Timestamp(System.currentTimeMillis() + 5 * 60 * 1000));
		storeDeletionTime(accData);
	}

	/**
	 * Completely removes player from database
	 * 
	 * @param playerId
	 *            id of player to delete from db
	 */
	public static void deletePlayerFromDB(int playerId)
	{
		DAOManager.getDAO(PlayerDAO.class).deletePlayer(playerId);
		DAOManager.getDAO(InventoryDAO.class).deletePlayerItems(playerId);
		DAOManager.getDAO(PlayerLifeStatsDAO.class).deletePlayerLifeStat(playerId);
	}

	/**
	 * Updates deletion time in database
	 * 
	 * @param accData
	 *            PlayerAccountData
	 */
	private static void storeDeletionTime(PlayerAccountData accData)
	{
		DAOManager.getDAO(PlayerDAO.class).updateDeletionTime(accData.getPlayerCommonData().getPlayerObjId(),
			accData.getDeletionDate());
	}

	/**
	 * 
	 * @param objectId
	 * @param creationDate
	 */
	public static void storeCreationTime(int objectId, Timestamp creationDate)
	{
		DAOManager.getDAO(PlayerDAO.class).storeCreationTime(objectId, creationDate);
	}

	/**
	 * Add macro for player
	 * 
	 * @param player
	 *            Player
	 * @param macroOrder
	 *            Macro order
	 * @param macroXML
	 *            Macro XML
	 */
	public static void addMacro(Player player, int macroOrder, String macroXML)
	{
		if(player.getMacroList().addMacro(macroOrder, macroXML))
		{
			DAOManager.getDAO(PlayerMacrossesDAO.class).addMacro(player.getObjectId(), macroOrder, macroXML);
		}
	}

	/**
	 * Remove macro with specified index from specified player
	 * 
	 * @param player
	 *            Player
	 * @param macroOrder
	 *            Macro order index
	 */
	public static void removeMacro(Player player, int macroOrder)
	{
		if(player.getMacroList().removeMacro(macroOrder))
		{
			DAOManager.getDAO(PlayerMacrossesDAO.class).deleteMacro(player.getObjectId(), macroOrder);
		}
	}

	/**
	 * Gets a player ONLY if he is in the cache
	 * 
	 * @return Player or null if not cached
	 */
	public static Player getCachedPlayer(int playerObjectId)
	{
		return playerCache.get(playerObjectId);
	}
}
