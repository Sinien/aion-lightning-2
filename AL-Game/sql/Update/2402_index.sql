ALTER TABLE `legion_members` ADD INDEX `player_id`(`player_id`);
ALTER TABLE `inventory` ADD INDEX `item_owner`(`itemOwner`);
ALTER TABLE `inventory` ADD INDEX `item_location`(`itemLocation`);
ALTER TABLE `inventory` ADD INDEX `is_equiped`(`isEquiped`);