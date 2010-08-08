/**
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
package com.aionemu.gameserver.model.gameobjects.player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.PeriodicSaveConfig;
import com.aionemu.gameserver.controllers.FlyController;
import com.aionemu.gameserver.controllers.PlayerController;
import com.aionemu.gameserver.controllers.ReviveController;
import com.aionemu.gameserver.controllers.effect.PlayerEffectController;
import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dao.PlayerQuestListDAO;
import com.aionemu.gameserver.dao.PlayerSkillListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.Monster;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.model.gameobjects.stats.PlayerGameStats;
import com.aionemu.gameserver.model.gameobjects.stats.PlayerLifeStats;
import com.aionemu.gameserver.model.group.PlayerGroup;
import com.aionemu.gameserver.model.items.ItemCooldown;
import com.aionemu.gameserver.model.legion.Legion;
import com.aionemu.gameserver.model.legion.LegionMember;
import com.aionemu.gameserver.model.templates.stats.PlayerStatsTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.services.ExchangeService;
import com.aionemu.gameserver.services.PlayerService;
import com.aionemu.gameserver.skillengine.task.CraftingTask;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.rates.Rates;
import com.aionemu.gameserver.utils.rates.RegularRates;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * This class is representing Player object, it contains all needed data.
 * 
 * 
 * @author -Nemesiss-
 * @author SoulKeeper
 * @author alexa026
 * 
 */
public class Player extends Creature
{
	
	private static final Logger	log	= Logger.getLogger(Player.class);

	private PlayerAppearance	playerAppearance;
	private PlayerAppearance	savedPlayerAppearance;
	private PlayerCommonData	playerCommonData;
	private LegionMember		legionMember;
	private MacroList			macroList;
	private SkillList			skillList;
	private FriendList			friendList;
	private BlockList			blockList;
	private ResponseRequester	requester;
	private boolean				lookingForGroup	= false;
	private Storage				inventory;
	private Storage				regularWarehouse;
	private Storage				accountWarehouse;
	private Equipment			equipment;
	private Mailbox             mailbox;
	private PrivateStore		store;
	private PlayerStatsTemplate	playerStatsTemplate;
	private TitleList			titleList;
	private PlayerSettings		playerSettings;
	private QuestStateList		questStateList;
	private List<Integer>		nearbyQuestList	= new ArrayList<Integer>();
	private ZoneInstance		zoneInstance;
	private PlayerGroup			playerGroup;
	private AbyssRank			abyssRank;
	private Rates				rates;
	private RecipeList			recipeList;
	private int					flyState		= 0;
	private boolean				isTrading;
	private long				prisonTimer		= 0;
	private long				startPrison;
	private boolean				invul;
	private FlyController		flyController;
	private ReviveController	reviveController;
	private CraftingTask		craftingTask;
	private int					flightTeleportId;
	private int					flightDistance;
	private Summon				summon;
	private Kisk				kisk;
	private Prices 				prices;
	private boolean				isGagged		= false;

	private Map<Integer, ItemCooldown>	itemCoolDowns;
	
	/**
	 * Static information for players
	 */
	private static final int		CUBE_SPACE				= 9;
	private static final int		WAREHOUSE_SPACE			= 8;

	/**
	 * Connection of this Player.
	 */
	private AionConnection			clientConnection;

	public Player(PlayerController controller, PlayerCommonData plCommonData, PlayerAppearance appereance)
	{
		super(plCommonData.getPlayerObjId(), controller, null, null, plCommonData.getPosition());
		// TODO may be pcd->visibleObjectTemplate ?
		this.playerCommonData = plCommonData;
		this.playerAppearance = appereance;

		this.prices = new Prices();
		this.requester = new ResponseRequester(this);
		this.questStateList = new QuestStateList();
		this.titleList = new TitleList();
		controller.setOwner(this);
	}

	public PlayerCommonData getCommonData()
	{
		return playerCommonData;
	}

	@Override
	public String getName()
	{
		return playerCommonData.getName();
	}

	public PlayerAppearance getPlayerAppearance()
	{
		return playerAppearance;
	}
	
	
	public void setPlayerAppearance(PlayerAppearance playerAppearance)
	{
		this.playerAppearance = playerAppearance;
	}

	/**
	 * Only use for the Size admin command
	 * 
	 * @return PlayerAppearance : The saved player's appearance, to rollback his appearance
	 */
	public PlayerAppearance getSavedPlayerAppearance()
	{
		return savedPlayerAppearance;
	}

	/**
	 * Only use for the Size admin command
	 * 
	 * @param playerAppearance PlayerAppearance : The saved player's appearance, to rollback his appearance
	 */
	public void setSavedPlayerAppearance(PlayerAppearance savedPlayerAppearance)
	{
		this.savedPlayerAppearance = savedPlayerAppearance;
	}
	
	/**
	 * Set connection of this player.
	 * 
	 * @param clientConnection
	 */
	public void setClientConnection(AionConnection clientConnection)
	{
		this.clientConnection = clientConnection;
	}

	/**
	 * Get connection of this player.
	 * 
	 * @return AionConnection of this player.
	 * 
	 */
	public AionConnection getClientConnection()
	{
		return this.clientConnection;
	}

	public MacroList getMacroList()
	{
		return macroList;
	}

	public void setMacroList(MacroList macroList)
	{
		this.macroList = macroList;
	}

	public SkillList getSkillList()
	{
		return skillList;
	}

	public void setSkillList(SkillList skillList)
	{
		this.skillList = skillList;
	}

	/**
	 * Gets this players Friend List
	 * 
	 * @return FriendList
	 */
	public FriendList getFriendList()
	{
		return friendList;
	}

	/**
	 * Is this player looking for a group
	 * 
	 * @return true or false
	 */
	public boolean isLookingForGroup()
	{
		return lookingForGroup;
	}

	/**
	 * Sets whether or not this player is looking for a group
	 * 
	 * @param lookingForGroup
	 */
	public void setLookingForGroup(boolean lookingForGroup)
	{
		this.lookingForGroup = lookingForGroup;
	}

	/**
	 * Sets this players friend list. <br />
	 * Remember to send the player the <tt>SM_FRIEND_LIST</tt> packet.
	 * 
	 * @param list
	 */
	public void setFriendList(FriendList list)
	{
		this.friendList = list;
	}

	public BlockList getBlockList()
	{
		return blockList;
	}

	public void setBlockList(BlockList list)
	{
		this.blockList = list;
	}

	/**
	 * @return the playerLifeStats
	 */
	@Override
	public PlayerLifeStats getLifeStats()
	{
		return (PlayerLifeStats) super.getLifeStats();
	}

	/**
	 * @param lifeStats
	 *            the lifeStats to set
	 */
	public void setLifeStats(PlayerLifeStats lifeStats)
	{
		super.setLifeStats(lifeStats);
	}

	/**
	 * @return the gameStats
	 */
	@Override
	public PlayerGameStats getGameStats()
	{
		return (PlayerGameStats) super.getGameStats();
	}

	/**
	 * @param gameStats
	 *            the gameStats to set
	 */
	public void setGameStats(PlayerGameStats gameStats)
	{
		super.setGameStats(gameStats);
	}

	/**
	 * Gets the ResponseRequester for this player
	 * 
	 * @return ResponseRequester
	 */
	public ResponseRequester getResponseRequester()
	{
		return requester;
	}

	public boolean isOnline()
	{
		return getClientConnection() != null;
	}

	public int getCubeSize()
	{
		return this.playerCommonData.getCubeSize();
	}

	public PlayerClass getPlayerClass()
	{
		return playerCommonData.getPlayerClass();
	}

	public Gender getGender()
	{
		return playerCommonData.getGender();
	}

	/**
	 * Return PlayerController of this Player Object.
	 * 
	 * @return PlayerController.
	 */
	@Override
	public PlayerController getController()
	{
		return (PlayerController) super.getController();
	}

	@Override
	public byte getLevel()
	{
		return (byte) playerCommonData.getLevel();
	}

	/**
	 * @return the inventory
	 */

	public Equipment getEquipment()
	{
		return equipment;
	}

	public void setEquipment(Equipment equipment)
	{
		this.equipment = equipment;
	}

	/**
	 * @return the player private store
	 */
	public PrivateStore getStore()
	{
		return store;
	}

	/**
	 * @param store the store that needs to be set
	 */
	public void setStore(PrivateStore store)
	{
		this.store = store;
	}

	/**
	 * @return the questStatesList
	 */
	public QuestStateList getQuestStateList()
	{
		return questStateList;
	}

	/**
	 * @param questStateList
	 *            the QuestStateList to set
	 */
	public void setQuestStateList(QuestStateList questStateList)
	{
		this.questStateList = questStateList;
	}

	/**
	 * @return the playerStatsTemplate
	 */
	public PlayerStatsTemplate getPlayerStatsTemplate()
	{
		return playerStatsTemplate;
	}

	/**
	 * @param playerStatsTemplate
	 *            the playerStatsTemplate to set
	 */
	public void setPlayerStatsTemplate(PlayerStatsTemplate playerStatsTemplate)
	{
		this.playerStatsTemplate = playerStatsTemplate;
	}

	public List<Integer> getNearbyQuests()
	{
		return nearbyQuestList;
	}

	public RecipeList getRecipeList()
	{
		return recipeList;
	}

	public void setRecipeList(RecipeList recipeList)
	{
		this.recipeList = recipeList;
	}

	/**
	 * @param inventory
	 *            the inventory to set Inventory should be set right after player object is created
	 */
	public void setStorage(Storage storage, StorageType storageType)
	{
		if(storageType == StorageType.CUBE)
		{
			this.inventory = storage;
			inventory.setOwner(this);
		}

		if(storageType == StorageType.REGULAR_WAREHOUSE)
		{
			this.regularWarehouse = storage;
			regularWarehouse.setOwner(this);
		}

		if(storageType == StorageType.ACCOUNT_WAREHOUSE)
		{
			this.accountWarehouse = storage;
			accountWarehouse.setOwner(this);
		}
	}

	/**
	 * 
	 * @param storageType
	 * @return
	 */
	public Storage getStorage(int storageType)
	{
		if(storageType == StorageType.REGULAR_WAREHOUSE.getId())
			return regularWarehouse;

		if(storageType == StorageType.ACCOUNT_WAREHOUSE.getId())
			return accountWarehouse;

		if(storageType == StorageType.LEGION_WAREHOUSE.getId())
			return getLegion().getLegionWarehouse();

		if(storageType == StorageType.CUBE.getId())
			return inventory;
		else
			return null;
	}

	/**
	 *  Items from UPDATE_REQUIRED storages and equipment
	 *  
	 * @return
	 */
	public List<Item> getDirtyItemsToUpdate()
	{
		List<Item> dirtyItems = new ArrayList<Item>();

		Storage cubeStorage = getStorage(StorageType.CUBE.getId());
		if(cubeStorage.getPersistentState() == PersistentState.UPDATE_REQUIRED)
		{
			dirtyItems.addAll(cubeStorage.getAllItems());
			dirtyItems.addAll(cubeStorage.getDeletedItems());
			cubeStorage.setPersistentState(PersistentState.UPDATED);
		}

		Storage  regularWhStorage = getStorage(StorageType.REGULAR_WAREHOUSE.getId());
		if(regularWhStorage.getPersistentState() == PersistentState.UPDATE_REQUIRED)
		{
			dirtyItems.addAll(regularWhStorage.getAllItems());
			dirtyItems.addAll(regularWhStorage.getDeletedItems());
			regularWhStorage.setPersistentState(PersistentState.UPDATED);
		}

		Storage  accountWhStorage = getStorage(StorageType.ACCOUNT_WAREHOUSE.getId());
		if(accountWhStorage.getPersistentState() == PersistentState.UPDATE_REQUIRED)
		{
			dirtyItems.addAll(accountWhStorage.getAllItems());
			dirtyItems.addAll(accountWhStorage.getDeletedItems());
			accountWhStorage.setPersistentState(PersistentState.UPDATED);
		}

		Equipment  equipment = getEquipment();
		if(equipment.getPersistentState() == PersistentState.UPDATE_REQUIRED)
		{
			dirtyItems.addAll(equipment.getEquippedItems());
			equipment.setPersistentState(PersistentState.UPDATED);
		}

		return dirtyItems;
	}
	/**
	 *  //TODO probably need to optimize here
	 *  
	 * @return
	 */
	public List<Item> getAllItems()
	{
		List<Item> allItems = new ArrayList<Item>();

		Storage cubeStorage = getStorage(StorageType.CUBE.getId());
		allItems.addAll(cubeStorage.getAllItems());

		Storage  regularWhStorage = getStorage(StorageType.REGULAR_WAREHOUSE.getId());
		allItems.addAll(regularWhStorage.getStorageItems());

		Storage  accountWhStorage = getStorage(StorageType.ACCOUNT_WAREHOUSE.getId());
		allItems.addAll(accountWhStorage.getStorageItems());

		Equipment  equipment = getEquipment();
		allItems.addAll(equipment.getEquippedItems());

		return allItems;
	}

	public Storage getInventory()
	{
		return inventory;
	}

	/**
	 * @param CubeUpgrade
	 *            int Sets the cubesize
	 */
	public void setCubesize(int cubesize)
	{
		this.playerCommonData.setCubesize(cubesize);
		getInventory().setLimit(getInventory().getLimit() + (cubesize * CUBE_SPACE));
	}

	/**
	 * @return the playerSettings
	 */
	public PlayerSettings getPlayerSettings()
	{
		return playerSettings;
	}

	/**
	 * @param playerSettings
	 *            the playerSettings to set
	 */
	public void setPlayerSettings(PlayerSettings playerSettings)
	{
		this.playerSettings = playerSettings;
	}

	/**
	 * @return the zoneInstance
	 */
	public ZoneInstance getZoneInstance()
	{
		return zoneInstance;
	}

	/**
	 * @param zoneInstance
	 *            the zoneInstance to set
	 */
	public void setZoneInstance(ZoneInstance zoneInstance)
	{
		this.zoneInstance = zoneInstance;
	}

	public TitleList getTitleList()
	{
		return titleList;
	}

	public void setTitleList(TitleList titleList)
	{
		this.titleList = titleList;
		titleList.setOwner(this);
	}

	/**
	 * @return the playerGroup
	 */
	public PlayerGroup getPlayerGroup()
	{
		return playerGroup;
	}

	/**
	 * @param playerGroup
	 *            the playerGroup to set
	 */
	public void setPlayerGroup(PlayerGroup playerGroup)
	{
		this.playerGroup = playerGroup;
	}

	/**
	 * @return the abyssRank
	 */
	public AbyssRank getAbyssRank()
	{
		return abyssRank;
	}

	/**
	 * @param abyssRank
	 *            the abyssRank to set
	 */
	public void setAbyssRank(AbyssRank abyssRank)
	{
		this.abyssRank = abyssRank;
	}

	@Override
	public PlayerEffectController getEffectController()
	{
		return (PlayerEffectController) super.getEffectController();
	}

	@Override
	public void initializeAi()
	{
		// Empty
	}

	/**
	 * <b><font color='red'>NOTICE: </font>this method is supposed to be called only from
	 * {@link PlayerService#playerLoggedIn(Player)}</b>
	 */
	public void onLoggedIn()
	{
		getController().addTask(TaskId.PLAYER_UPDATE,
		ThreadPoolManager.getInstance().scheduleAtFixedRate(
			new GeneralUpdateTask(this), PeriodicSaveConfig.PLAYER_GENERAL * 1000, PeriodicSaveConfig.PLAYER_GENERAL * 1000));
		getController().addTask(TaskId.INVENTORY_UPDATE,
		ThreadPoolManager.getInstance().scheduleAtFixedRate(
			new ItemUpdateTask(this), PeriodicSaveConfig.PLAYER_ITEMS * 1000, PeriodicSaveConfig.PLAYER_ITEMS * 1000));
	}

	/**
	 * <b><font color='red'>NOTICE: </font>this method is supposed to be called only from
	 * {@link PlayerService#playerLoggedOut(Player)}</b>
	 */
	public void onLoggedOut()
	{
		requester.denyAll();
		friendList.setStatus(FriendList.Status.OFFLINE);
		BrokerService.getInstance().removePlayerCache(this);
		ExchangeService.getInstance().cancelExchange(this);
	}

	/**
	 * Returns true if has valid LegionMember
	 */
	public boolean isLegionMember()
	{
		return legionMember != null;
	}

	/**
	 * @param legionMember
	 *            the legionMember to set
	 */
	public void setLegionMember(LegionMember legionMember)
	{
		this.legionMember = legionMember;
	}

	/**
	 * @return the legionMember
	 */
	public LegionMember getLegionMember()
	{
		return legionMember;
	}

	/**
	 * @return the legion
	 */
	public Legion getLegion()
	{
		if(legionMember != null)
			return legionMember.getLegion();
		else
			return null;
	}

	/**
	 * Checks if object id's are the same
	 * 
	 * @return true if the object id is the same
	 */
	public boolean sameObjectId(int objectId)
	{
		return this.getObjectId() == objectId;
	}

	/**
	 * @return true if a player has a store opened
	 */
	public boolean hasStore()
	{
		if(getStore() != null)
			return true;
		return false;
	}

	/**
	 * Removes legion from player
	 */
	public void resetLegionMember()
	{
		setLegionMember(null);
	}

	/**
	 * This method will return true if player is in a group
	 * 
	 * @return true or false
	 */
	public boolean isInGroup()
	{
		return playerGroup != null;
	}

	/**
	 * Access level of this player
	 * 
	 * @return byte
	 */
	public byte getAccessLevel()
	{
		return getClientConnection().getAccount().getAccessLevel();
	}

	/**
	 * accountName of this player
	 * 
	 * @return int
	 */
	public String getAcountName()
	{
		return getClientConnection().getAccount().getName();
	}

	/**
	 * @return the rates
	 */
	public Rates getRates()
	{
		if(rates == null)
			rates = new RegularRates();
		return rates;
	}

	/**
	 * @param rates
	 *            the rates to set
	 */
	public void setRates(Rates rates)
	{
		this.rates = rates;
	}

	/**
	 * @return warehouse size
	 */
	public int getWarehouseSize()
	{
		return this.playerCommonData.getWarehouseSize();
	}

	/**
	 * @param warehouseSize
	 */
	public void setWarehouseSize(int warehouseSize)
	{
		this.playerCommonData.setWarehouseSize(warehouseSize);
		getWarehouse().setLimit(getWarehouse().getLimit() + (warehouseSize * WAREHOUSE_SPACE));
	}

	/**
	 * @return regularWarehouse
	 */
	public Storage getWarehouse()
	{
		return regularWarehouse;
	}

	/**
	 * 0: regular, 1: fly, 2: glide
	 */
	public int getFlyState()
	{
		return this.flyState;
	}

	public void setFlyState(int flyState)
	{
		this.flyState = flyState;
	}

	/**
	 * @return the isTrading
	 */
	public boolean isTrading()
	{
		return isTrading;
	}

	/**
	 * @param isTrading the isTrading to set
	 */
	public void setTrading(boolean isTrading)
	{
		this.isTrading = isTrading;
	}

	/**
	 * @return the isInPrison
	 */
	public boolean isInPrison()
	{
		return prisonTimer != 0;
	}

	/**
	 * @param prisonTimer the prisonTimer to set
	 */
	public void setPrisonTimer(long prisonTimer)
	{
		if (prisonTimer < 0)
			prisonTimer = 0;
			
		this.prisonTimer = prisonTimer;
	}

	/**
	 * @return the prisonTimer
	 */
	public long getPrisonTimer()
	{
		return prisonTimer;
	}
	
	/**
	 * @return the time in ms of start prison
	 */
	public long getStartPrison()
	{
		return startPrison;
	}
	
	/**
	 * @param start : The time in ms of start prison
	 */
	public void setStartPrison(long start)
	{
		this.startPrison = start;
	}

	/**
	 * @return
	 */
	public boolean isProtectionActive()
	{
		return isInVisualState(CreatureVisualState.BLINKING);
	}

	/**
	 * Check is player is invul
	 * 
	 * @return boolean
	 **/
	public boolean isInvul()
	{
		return invul;
	}

	/**
	 * Sets invul on player
	 * 
	 * @param invul
	 *            - boolean
	 **/
	public void setInvul(boolean invul)
	{
		this.invul = invul;
	}

	public void setMailbox(Mailbox mailbox)
	{
		this.mailbox = mailbox;
	}

	public Mailbox getMailbox()
	{
		return mailbox;
	}

	/**
	 * @return the flyController
	 */
	public FlyController getFlyController()
	{
		return flyController;
	}

	/**
	 * @param flyController the flyController to set
	 */
	public void setFlyController(FlyController flyController)
	{
		this.flyController = flyController;
	}

	public ReviveController getReviveController()
	{
		return reviveController;
	}

	public void setReviveController(ReviveController reviveController)
	{
		this.reviveController = reviveController;
	}

	public int getLastOnline()
	{
		Timestamp lastOnline = playerCommonData.getLastOnline();
		if(lastOnline == null || isOnline())
			return 0;

		return (int) (lastOnline.getTime() / 1000);
	}

	/**
	 * 
	 * @param craftingTask
	 */
	public void setCraftingTask(CraftingTask craftingTask)
	{
		this.craftingTask = craftingTask;
	}

	/**
	 * 
	 * @return
	 */
	public CraftingTask getCraftingTask()
	{
		return craftingTask;
	}

	/**
	 * 
	 * @param flightTeleportId
	 */
	public void setFlightTeleportId(int flightTeleportId)
	{
		this.flightTeleportId = flightTeleportId;
	}

	/**
	 * 
	 * @return flightTeleportId
	 */
	public int getFlightTeleportId()
	{
		return flightTeleportId;
	}

	/**
	 * 
	 * @param flightDistance
	 */
	public void setFlightDistance(int flightDistance)
	{
		this.flightDistance = flightDistance;
	}

	/**
	 * 
	 * @return flightDistance
	 */
	public int getFlightDistance()
	{
		return flightDistance;
	}

	/**
	 * @return
	 */
	public boolean isUsingFlyTeleport()
	{
		return isInState(CreatureState.FLIGHT_TELEPORT) && flightTeleportId != 0;
	}
	
	public boolean isGM()
	{
		return getAccessLevel() > 0;
	}
	
	/**
	 * Npc enemies:<br>
	 * - monsters<br>
	 * - aggressive npcs<br>
	 * @param npc
	 * @return
	 */
	@Override
	public boolean isEnemyNpc(Npc npc)
	{
		return npc instanceof Monster || npc.isAggressiveTo(this);
	}
	
	/**
	 * Player enemies:<br>
	 * - different race<br>
	 * - duel partner<br>
	 * 
	 * @param player
	 * @return
	 */
	@Override
	public boolean isEnemyPlayer(Player player)
	{
		return player.getCommonData().getRace() != getCommonData().getRace()
			|| getController().isDueling(player);
	}
	
	/**
	 * Summon enemies:<br>
	 * - master not null and master is enemy<br>
	 */
	@Override
	public boolean isEnemySummon(Summon summon)
	{
		return  summon.getMaster() != null && isEnemyPlayer(summon.getMaster());
	}

	/**
	 * Player-player friends:<br>
	 * - not in duel<br>
	 * - same race<br>
	 * 
	 * @param player
	 * @return
	 */
	public boolean isFriend(Player player)
	{
		return player.getCommonData().getRace() == getCommonData().getRace() && !getController().isDueling(player);
	}

	@Override
	public String getTribe()
	{
		switch(getCommonData().getRace())
		{
			case ELYOS:
				return "PC";
			default:
				return "PC_DARK";
		}
	}
	
	@Override
	public boolean isAggressiveTo(Creature creature)
	{
		return creature.isAggroFrom(this);
	}

	@Override
	public boolean isAggroFrom(Npc npc)
	{
		String currentTribe = npc.getTribe();
		// npc's that are 10 or more levels lower don't get aggro on players
		if(npc.getLevel() + 10 <= getLevel())
			return false;
		
		return isAggroIconTo(currentTribe);
	}

	/**
	 * Used in SM_NPC_INFO to check aggro irrespective to level
	 * 
	 * @param npcTribe
	 * @return
	 */
	public boolean isAggroIconTo(String npcTribe)
	{
		switch(getCommonData().getRace())
		{
			case ELYOS:
				if (DataManager.TRIBE_RELATIONS_DATA.isGuardDark(npcTribe))
					return true;
				return DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(npcTribe, "PC");
			case ASMODIANS:
				if (DataManager.TRIBE_RELATIONS_DATA.isGuardLight(npcTribe))
					return true;
				return DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(npcTribe, "PC_DARK");
		}
		return false;
	}

	@Override
	protected boolean canSeeNpc(Npc npc)
	{
		return true; //TODO
	}

	@Override
	protected boolean canSeePlayer(Player player)
	{
		return player.getVisualState() <= getSeeState();
	}

	/**
	 * @return the summon
	 */
	public Summon getSummon()
	{
		return summon;
	}

	/**
	 * @param summon the summon to set
	 */
	public void setSummon(Summon summon)
	{
		this.summon = summon;
	}

	/**
	 * @param new kisk to bind to (null if unbinding)
	 */
	public void setKisk(Kisk newKisk)
	{
		this.kisk = newKisk;
	}

	/**
	 * @return
	 */
	public Kisk getKisk()
	{
		return this.kisk;
	}
	
	/**
	 * 
	 * @param delayId
	 * @return
	 */
	public boolean isItemUseDisabled(int delayId)
	{
		if(itemCoolDowns == null || !itemCoolDowns.containsKey(delayId))
			return false;
		
		Long coolDown = itemCoolDowns.get(delayId).getReuseTime();
		if(coolDown == null)
			return false;
		
		
		if (coolDown < System.currentTimeMillis())
		{
			itemCoolDowns.remove(delayId);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param itemMask
	 * @return
	 */
	public long getItemCoolDown(int itemMask)
	{
		if(itemCoolDowns == null || !itemCoolDowns.containsKey(itemMask))
			return 0;
		
		return itemCoolDowns.get(itemMask).getReuseTime();
	}
	
	/**
	 * @return the itemCoolDowns
	 */
	public Map<Integer, ItemCooldown> getItemCoolDowns()
	{
		return itemCoolDowns;
	}

	/**
	 * 
	 * @param delayId
	 * @param time
	 * @param useDelay
	 */
	public void addItemCoolDown(int delayId, long time, int useDelay)
	{
		if(itemCoolDowns == null)
			itemCoolDowns = new FastMap<Integer, ItemCooldown>().shared();
		
		itemCoolDowns.put(delayId, new ItemCooldown(time, useDelay));
	}
	
	/**
	 * 
	 * @param itemMask
	 */
	public void removeItemCoolDown(int itemMask)
	{
		if(itemCoolDowns == null)
			return;
		itemCoolDowns.remove(itemMask);
	}
	
	/**
	 * @return prices
	 */
	public Prices getPrices()
	{
		return this.prices;
	}

	/**
	 * @param isGagged the isGagged to set
	 */
	public void setGagged(boolean isGagged)
	{
		this.isGagged = isGagged;
	}

	/**
	 * @return the isGagged
	 */
	public boolean isGagged()
	{
		return isGagged;
	}

	private class GeneralUpdateTask implements Runnable
	{
		private Player	player;

		private GeneralUpdateTask(Player player)
		{
			this.player = player;
		}

		@Override
		public void run()
		{
			try
			{
				DAOManager.getDAO(AbyssRankDAO.class).storeAbyssRank(player);
				DAOManager.getDAO(PlayerSkillListDAO.class).storeSkills(player);
				DAOManager.getDAO(PlayerQuestListDAO.class).store(player);
				DAOManager.getDAO(PlayerDAO.class).storePlayer(player);
			}
			catch(Exception ex)
			{
				log.error("Exception during periodic saving of player " + player.getName() + " " 
					+ ex.getCause() != null ? ex.getCause().getMessage() : "null");
			}
		}
	}
	
	private class ItemUpdateTask implements Runnable
	{
		private Player	player;

		private ItemUpdateTask(Player player)
		{
			this.player = player;
		}

		@Override
		public void run()
		{
			try
			{
				DAOManager.getDAO(InventoryDAO.class).store(player);
				DAOManager.getDAO(ItemStoneListDAO.class).save(player);
			}
			catch(Exception ex)
			{
				log.error("Exception during periodic saving of player items " + player.getName() + " "
					+ ex.getCause() != null ? ex.getCause().getMessage() : "null");
			}
		}
	}

	public void setPlayerAlliance(PlayerAlliance playerAlliance)
	{
		this.playerAlliance = playerAlliance;
	}
	private PlayerAlliance playerAlliance;
	/**
	 * @return
	 */
	public PlayerAlliance getPlayerAlliance()
	{
		return playerAlliance;
	}

	/**
	 * @return boolean value is in alliance
	 */
	public boolean isInAlliance()
	{
		return (this.playerAlliance != null);
	}
}
