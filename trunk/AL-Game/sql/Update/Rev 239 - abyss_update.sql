ALTER TABLE `abyss_rank`
ADD COLUMN `daily_ap`  int(11) NOT NULL AFTER `player_id`,
ADD COLUMN `weekly_ap`  int(11) NOT NULL AFTER `daily_ap`,
ADD COLUMN `top_ranking`  int(4) NOT NULL AFTER `rank`,
ADD COLUMN `daily_kill`  int(5) NOT NULL AFTER `top_ranking`,
ADD COLUMN `weekly_kill`  int(5) NOT NULL AFTER `daily_kill`,
ADD COLUMN `last_kill`  int(5) NOT NULL AFTER `max_rank`,
ADD COLUMN `last_ap`  int(11) NOT NULL AFTER `last_kill`,
ADD COLUMN `last_update`  decimal(20,0) NOT NULL AFTER `last_ap`;